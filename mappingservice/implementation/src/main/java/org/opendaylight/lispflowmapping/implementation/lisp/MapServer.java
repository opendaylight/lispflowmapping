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

import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceNoMaskKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer implements IMapServerAsync {
    private ILispDAO dao;
    protected static final Logger logger = LoggerFactory.getLogger(MapServer.class);

    public MapServer(ILispDAO dao) {
        this.dao = dao;
    }

    public void handleMapRegister(MapRegister mapRegister, IMapNotifyHandler callback) {
        if (dao == null) {
            logger.warn("handleMapRegister called while dao is uninitialized");
        } else if (!LispAuthenticationUtil.validate(mapRegister)) {
            logger.debug("Authentication failed");
        } else {
            EidToLocatorRecord eidRecord = mapRegister.getEidToLocatorRecords().get(0);
            List<MappingEntry<?>> rlocs = new ArrayList<ILispDAO.MappingEntry<?>>();
            rlocs.add(new MappingEntry<Integer>("NumRLOCs", eidRecord.getLocators().size()));
            int i = 0;
            for (LocatorRecord locatorRecord : eidRecord.getLocators()) {
                rlocs.add(new MappingEntry<MappingServiceValue>("RLOC" + (i++), new MappingServiceValue(locatorRecord, eidRecord.getRecordTtl())));
            }
            if (eidRecord.getPrefix() instanceof IMaskable && eidRecord.getMaskLength() > 0
                    && eidRecord.getMaskLength() < ((IMaskable) eidRecord.getPrefix()).getMaxMask()) {
                ((IMaskable) eidRecord.getPrefix()).normalize(eidRecord.getMaskLength());
                dao.put(new MappingServiceKey(eidRecord.getPrefix(), (byte) eidRecord.getMaskLength()), rlocs.toArray(new MappingEntry[rlocs.size()]));
            } else {
                dao.put(new MappingServiceNoMaskKey(eidRecord.getPrefix()), rlocs.toArray(new MappingEntry[rlocs.size()]));

            }
            MapNotify mapNotify = null;
            if (mapRegister.isWantMapNotify()) {
                logger.trace("MapRegister wants MapNotify");
                mapNotify = new MapNotify();
                mapNotify.setFromMapRegister(mapRegister);
                mapNotify.setAuthenticationData(LispAuthenticationUtil.getAuthenticationData(mapNotify));
                callback.handleMapNotify(mapNotify);
            }
        }

    }

    public String getAuthenticationKey(LispAddress address, int maskLen) {
        return null;
    }

    public boolean removeAuthenticationKey(LispAddress address, int maskLen) {
        return false;
    }

    public boolean addAuthenticationKey(LispAddress address, int maskLen, String key) {
        return false;
    }

}
