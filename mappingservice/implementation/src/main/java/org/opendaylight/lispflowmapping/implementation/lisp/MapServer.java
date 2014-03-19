/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOCGroup;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.DistinguishedName;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifymessage.MapNotifyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer extends AbstractLispComponent implements IMapServerAsync {

    protected static final Logger logger = LoggerFactory.getLogger(MapServer.class);
    private boolean overwrite;

    public MapServer(ILispDAO dao) {
        this(dao, true);
    }

    public MapServer(ILispDAO dao, boolean overwrite) {
        this(dao, overwrite, true);
    }

    public MapServer(ILispDAO dao, boolean overwrite, boolean authenticate) {
        this(dao, overwrite, authenticate, true);
    }

    public MapServer(ILispDAO dao, boolean overwrite, boolean authenticate, boolean iterateAuthenticationMask) {
        super(dao, authenticate, iterateAuthenticationMask);
        this.overwrite = overwrite;
    }

    public void handleMapRegister(MapRegister mapRegister, IMapNotifyHandler callback) {
        if (dao == null) {
            logger.warn("handleMapRegister called while dao is uninitialized");
        } else {
            boolean failed = false;
            String password = null;
            for (EidToLocatorRecord eidRecord : mapRegister.getEidToLocatorRecord()) {
                if (shouldAuthenticate()) {
                    password = getPassword(eidRecord.getLispAddressContainer(), eidRecord.getMaskLength());
                    if (!LispAuthenticationUtil.validate(mapRegister, password)) {
                        logger.warn("Authentication failed");
                        failed = true;
                        break;
                    }
                }
                saveRlocs(eidRecord);

            }
            if (!failed) {
                MapNotifyBuilder builder = new MapNotifyBuilder();
                if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
                    logger.trace("MapRegister wants MapNotify");
                    MapNotifyBuilderHelper.setFromMapRegister(builder, mapRegister);
                    if (shouldAuthenticate()) {
                        builder.setAuthenticationData(LispAuthenticationUtil.createAuthenticationData(builder.build(), password));
                    }
                    callback.handleMapNotify(builder.build());
                }
            }
        }
    }

    public void saveRlocs(EidToLocatorRecord eidRecord) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eidRecord.getLispAddressContainer(), eidRecord.getMaskLength());
        Map<String, MappingServiceRLOCGroup> rlocGroups = new HashMap<String, MappingServiceRLOCGroup>();
        if (eidRecord.getLocatorRecord() != null) {
            for (LocatorRecord locatorRecord : eidRecord.getLocatorRecord()) {
                String subkey = getLocatorKey(locatorRecord);
                if (!rlocGroups.containsKey(subkey)) {
                    rlocGroups.put(subkey, new MappingServiceRLOCGroup(eidRecord.getRecordTtl(), eidRecord.getAction(), eidRecord.isAuthoritative()));
                }
                rlocGroups.get(subkey).addRecord(locatorRecord);
            }
        }
        List<MappingEntry<MappingServiceRLOCGroup>> entries = new ArrayList<>();
        for (String subkey : rlocGroups.keySet()) {
            entries.add(new MappingEntry<>(subkey, rlocGroups.get(subkey)));
        }
        dao.put(key, entries.toArray(new MappingEntry[entries.size()]));
    }

    private String getLocatorKey(LocatorRecord locatorRecord) {
        if (locatorRecord.getLispAddressContainer().getAddress() instanceof LcafKeyValue) {
            LcafKeyValue keyVal = (LcafKeyValue) locatorRecord.getLispAddressContainer().getAddress();
            if (keyVal.getKey().getPrimitiveAddress() instanceof DistinguishedName) {
                return ((DistinguishedName) keyVal.getKey().getPrimitiveAddress()).getDistinguishedName();
            }
        }
        if (shouldOverwrite()) {
            return ADDRESS_SUBKEY;
        } else {
            return String.valueOf(locatorRecord.getLispAddressContainer().getAddress().hashCode());
        }
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        return getPassword(address, maskLen);
    }

    public boolean removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
        dao.removeSpecific(key, PASSWORD_SUBKEY);
        return true;
    }

    public boolean addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        IMappingServiceKey mappingServiceKey = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
        dao.put(mappingServiceKey, new MappingEntry<String>(PASSWORD_SUBKEY, key));
        return true;
    }

    public boolean shouldOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

}
