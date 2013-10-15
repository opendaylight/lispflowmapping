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

import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceKeyFactory;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServer;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer implements IMapServer {
    private ILispDAO dao;
    private boolean authenticate;
    private boolean iterateAuthenticationMask;
    protected static final Logger logger = LoggerFactory.getLogger(MapServer.class);

    public MapServer(ILispDAO dao) {
        this(dao, true);
    }

    public MapServer(ILispDAO dao, boolean authenticate) {
        this(dao, authenticate, true);
    }

    public MapServer(ILispDAO dao, boolean authenticate, boolean iterateAuthenticationMask) {
        this.dao = dao;
        this.authenticate = authenticate;
        this.iterateAuthenticationMask = iterateAuthenticationMask;
    }

    public MapNotify handleMapRegister(MapRegister mapRegister) {
        if (dao == null) {
            logger.warn("handleMapRegister called while dao is uninitialized");
            return null;
        }
        String password = null;
        EidToLocatorRecord eidRecord = mapRegister.getEidToLocatorRecords().get(0);
        if (authenticate) {
            password = getPassword(eidRecord.getPrefix(), eidRecord.getMaskLength());
            if (!LispAuthenticationUtil.validate(mapRegister, password)) {
                logger.debug("Authentication failed");
                return null;
            }
        }
        MappingServiceValue value = new MappingServiceValue();
        MappingEntry<MappingServiceValue> entry = new MappingEntry<MappingServiceValue>("value", value);
        List<MappingServiceRLOC> rlocs = new ArrayList<MappingServiceRLOC>();
        for (LocatorRecord locatorRecord : eidRecord.getLocators()) {
            rlocs.add(new MappingServiceRLOC(locatorRecord, eidRecord.getRecordTtl()));
        }
        value.setRlocs(rlocs);
        IMappingServiceKey key = MappingServiceKeyFactory.generateMappingServiceKey(eidRecord.getPrefix(), eidRecord.getMaskLength());
        dao.put(key, entry);

        MapNotify mapNotify = null;
        if (mapRegister.isWantMapNotify()) {
            logger.trace("MapRegister wants MapNotify");
            mapNotify = new MapNotify();
            mapNotify.setFromMapRegister(mapRegister);
            if (authenticate) {
                mapNotify.setAuthenticationData(LispAuthenticationUtil.getAuthenticationData(mapNotify, password));
            }
        }
        return mapNotify;
    }

    private String getPassword(LispAddress prefix, int maskLength) {
        if (prefix instanceof IMaskable) {
            prefix = ((IMaskable) prefix).clone();
        }
        while (maskLength > 0) {
            IMappingServiceKey key = MappingServiceKeyFactory.generateMappingServiceKey(prefix, maskLength);
            Map<String, ?> daoMap = dao.get(key);
            if (daoMap != null) {
                MappingServiceValue value = (MappingServiceValue) daoMap.get("value");
                if (value != null && value.getKey() != null) {
                    return value.getKey();
                } else if (iterateAuthenticationMask()) {
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

    public String getAuthenticationKey(LispAddress address, int maskLen) {
        return getPassword(address, maskLen);
    }

    public boolean removeAuthenticationKey(LispAddress address, int maskLen) {
        IMappingServiceKey key = MappingServiceKeyFactory.generateMappingServiceKey(address, maskLen);
        MappingServiceValue value = (MappingServiceValue) dao.get(key).get("value");
        value.setKey(null);
        if (value.isEmpty()) {
            dao.remove(key);
        }
        return true;
    }

    public boolean addAuthenticationKey(LispAddress address, int maskLen, String key) {
        IMappingServiceKey mappingServiceKey = MappingServiceKeyFactory.generateMappingServiceKey(address, maskLen);
        MappingServiceValue value = (MappingServiceValue) dao.get(mappingServiceKey).get("value");
        if (value == null) {
            value = new MappingServiceValue();
            MappingEntry<MappingServiceValue> entry = new MappingEntry<MappingServiceValue>("value", value);
            dao.put(mappingServiceKey, entry);
        }
        value.setKey(key);
        return true;
    }

    public boolean authenticate() {
        return authenticate;
    }

    public boolean iterateAuthenticationMask() {
        return iterateAuthenticationMask;
    }

    public void setIterateAuthenticationMask(boolean iterateAuthenticationMask) {
        this.iterateAuthenticationMask = iterateAuthenticationMask;
    }

}
