/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.Map;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceNoMaskKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapResolver implements IMapResolver {
    private ILispDAO dao;
    protected static final Logger logger = LoggerFactory.getLogger(MapResolver.class);

    public MapResolver(ILispDAO dao) {
        this.dao = dao;
    }

    public MapReply handleMapRequest(MapRequest request) {
        if (dao == null) {
            logger.warn("handleMapRequest called while dao is uninitialized");
            return null;
        }
        MapReply mapReply = new MapReply();
        mapReply.setNonce(request.getNonce());
        
        for (EidRecord eid : request.getEids()) {
            EidToLocatorRecord eidToLocators = new EidToLocatorRecord();
            eidToLocators.setMaskLength(eid.getMaskLength())//
            .setPrefix(eid.getPrefix());
            IMappingServiceKey key = null;
            if (!(eid.getPrefix() instanceof IMaskable) || eid.getMaskLength() == 0 || eid.getMaskLength() == ((IMaskable)eid.getPrefix()).getMaxMask()) {
                key = new MappingServiceNoMaskKey(eid.getPrefix());
            } else {
                key = new MappingServiceKey(eid.getPrefix(),(byte)eid.getMaskLength());
            }
            Map<String, ?> locators = dao.get(key);
            if (locators == null && key.getEID() instanceof IMaskable) {
                locators = findMaskLocators(key);
            }
            if (locators != null) {
                addLocators(eidToLocators, locators);
            }
            mapReply.addEidToLocator(eidToLocators);
        }

        return mapReply;
    }

    private Map<String, ?> findMaskLocators(IMappingServiceKey key) {
        int mask = key.getMask();
        if (mask == 0) {
            mask = ((IMaskable)key.getEID()).getMaxMask() - 1;
            key = new MappingServiceKey(key.getEID(), (byte)mask);
        }
        while (mask > 0) {
            ((IMaskable)key.getEID()).normalize(mask);
            mask--;
            Map<String, ?> locators = dao.get(key);
            if (locators != null) {
                return locators;
            } 
        }
        return null;
    }

    private void addLocators(EidToLocatorRecord eidToLocators, Map<String, ?> locators) {
        try {
            Integer numRLOCs = (Integer) locators.get("NumRLOCs");
            if (numRLOCs == null) {
                return;
            }
            for (int i = 0; i < numRLOCs; i++) {
                addLocator(eidToLocators, locators.get("RLOC" + i));
            }
        } catch (ClassCastException cce) {
        }
    }

    private void addLocator(EidToLocatorRecord eidToLocators, Object locatorObject) {
        if (locatorObject == null) {
            return;
        }
        try {
            LispAddress locator = ((MappingServiceValue) locatorObject).getRecord().getLocator();
            eidToLocators.addLocator(new LocatorRecord().setLocator(locator).setRouted(true));
        } catch (ClassCastException cce) {
        }
    }
}
