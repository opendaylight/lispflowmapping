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

import org.opendaylight.lispflowmapping.implementation.authentication.ILispAuthentication;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthentication;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationFactory;
import org.opendaylight.lispflowmapping.implementation.authentication.LispKeyIDEnum;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServer;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer implements IMapServer {
    private ILispDAO dao;
    protected static final Logger logger = LoggerFactory.getLogger(MapServer.class);

    public MapServer(ILispDAO dao) {
        this.dao = dao;
    }

    public MapNotify handleMapRegister(MapRegister mapRegister) {
        if (dao == null) {
            logger.warn("handleMapRegister called while dao is uninitialized");
            return null;
        }
        if (!LispAuthentication.getInstance().validate(mapRegister)) {
            return null;
        }
        EidToLocatorRecord eidRecord = mapRegister.getEidToLocatorRecords().get(0);
        List<MappingEntry<?>> rlocs = new ArrayList<ILispDAO.MappingEntry<?>>();
        rlocs.add(new MappingEntry<Integer>("NumRLOCs", eidRecord.getLocators().size()));
        int i = 0;
        for (LocatorRecord locatorRecord : eidRecord.getLocators()) {
            rlocs.add(new MappingEntry<LispAddress>("RLOC" + (i++), locatorRecord.getLocator()));
        }
        dao.put(eidRecord.getPrefix(), rlocs.toArray(new MappingEntry[rlocs.size()]));
        MapNotify mapNotify = null;
        if (mapRegister.isWantMapNotify()) {
            mapNotify = new MapNotify();
            mapNotify.setFromMapRegister(mapRegister);
            ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapNotify.getKeyId()));
            mapNotify.setAuthenticationData(authentication.getAuthenticationData(mapNotify));
        }
        return mapNotify;
    }
    
    
    private boolean validateMapRegisterAuthentication(MapRegister mapRegister) {
        return LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapRegister.getKeyId())).validate(mapRegister);
    }
}
