/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifymessage.MapNotifyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer implements IMapServerAsync {
    private ILispDAO dao;
    private volatile boolean shouldAuthenticate;
    private volatile boolean shouldIterateMask;
    protected static final Logger logger = LoggerFactory.getLogger(MapServer.class);

    public MapServer(ILispDAO dao) {
        this(dao, true);
    }

    public MapServer(ILispDAO dao, boolean authenticate) {
        this(dao, authenticate, true);
    }

    public MapServer(ILispDAO dao, boolean authenticate, boolean iterateAuthenticationMask) {
        this.dao = dao;
        this.shouldAuthenticate = authenticate;
        this.shouldIterateMask = iterateAuthenticationMask;
    }

    public void handleMapRegister(MapRegister mapRegister, IMapNotifyHandler callback) {
        logger.info("Got MapRegister");
        if (dao == null) {
            logger.warn("handleMapRegister called while dao is uninitialized");
        } else {
            boolean failed = false;
            String password = null;
            for (EidToLocatorRecord eidRecord : mapRegister.getEidToLocatorRecord()) {
                if (shouldAuthenticate) {
                    password = getPassword(eidRecord.getLispAddressContainer(), eidRecord.getMaskLength());
                    if (!LispAuthenticationUtil.validate(mapRegister, password)) {
                        logger.debug("Authentication failed");
                        failed = true;
                        break;
                    }
                }
                MappingServiceValue value = new MappingServiceValue();
                MappingEntry<MappingServiceValue> entry = new MappingEntry<MappingServiceValue>("value", value);
                List<MappingServiceRLOC> rlocs = new ArrayList<MappingServiceRLOC>();
                if (eidRecord.getLocatorRecord() != null) {
                    for (LocatorRecord locatorRecord : eidRecord.getLocatorRecord()) {
                        rlocs.add(new MappingServiceRLOC(locatorRecord, eidRecord.getRecordTtl(), eidRecord.getAction(), eidRecord.isAuthoritative()));
                    }
                }
                value.setRlocs(rlocs);
                IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eidRecord.getLispAddressContainer(),
                        eidRecord.getMaskLength());
                dao.put(key, entry);

            }
            if (!failed) {
                MapNotifyBuilder builder = new MapNotifyBuilder();
                if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
                    logger.trace("MapRegister wants MapNotify");
                    MapNotifyBuilderHelper.setFromMapRegister(builder, mapRegister);
                    if (shouldAuthenticate) {
                        builder.setAuthenticationData(LispAuthenticationUtil.createAuthenticationData(builder.build(), password));
                    }
                    callback.handleMapNotify(builder.build());
                }
            }
        }
    }

    private String getPassword(LispAddressContainer prefix, int maskLength) {
        while (maskLength >= 0) {
            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(prefix, maskLength);
            Map<String, ?> daoMap = dao.get(key);
            if (daoMap != null) {
                MappingServiceValue value = (MappingServiceValue) daoMap.get("value");
                if (value != null && value.getKey() != null) {
                    return value.getKey();
                } else if (shouldIterateMask()) {
                    maskLength -= 1;
                } else {
                    return null;
                }
            } else {
                maskLength -= 1;

            }
        }
        return null;
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        return getPassword(address, maskLen);
    }

    public boolean removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
        Map<String, ?> daoMap = dao.get(key);
        if (daoMap != null) {
            MappingServiceValue value = (MappingServiceValue) daoMap.get("value");
            if (value != null) {
                value.setKey(null);
                if (value.isEmpty()) {
                    dao.remove(key);
                } else {
                    dao.put(key, new MappingEntry<MappingServiceValue>("value", value));
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        IMappingServiceKey mappingServiceKey = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
        Map<String, ?> daoMap = dao.get(mappingServiceKey);
        MappingServiceValue value = null;
        if (daoMap != null) {
            value = (MappingServiceValue) daoMap.get("value");
            if (value == null) {
                value = new MappingServiceValue();
            }
        } else {
            value = new MappingServiceValue();
        }
        value.setKey(key);
        MappingEntry<MappingServiceValue> entry = new MappingEntry<MappingServiceValue>("value", value);
        dao.put(mappingServiceKey, entry);
        return true;
    }

    public boolean shouldAuthenticate() {
        return shouldAuthenticate;
    }

    public boolean shouldIterateMask() {
        return shouldIterateMask;
    }

    public void setShouldIterateMask(boolean shouldIterateMask) {
        this.shouldIterateMask = shouldIterateMask;
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.shouldAuthenticate = shouldAuthenticate;
    }

}
