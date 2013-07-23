/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.Map;

import org.opendaylight.lispflowmapping.dao.ILispDAO;
import org.opendaylight.lispflowmapping.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
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

        EidRecord eid = request.getEids().get(0);
        mapReply.setNonce(request.getNonce());
        EidToLocatorRecord eidToLocators = new EidToLocatorRecord();
        eidToLocators.setMaskLength(eid.getMaskLength())//
                .setPrefix(eid.getPrefix());
        Map<String, ?> locators = dao.get(eid.getPrefix());
        if (locators != null) {
            addLocators(eidToLocators, locators);
        }
        mapReply.addEidToLocator(eidToLocators);
        return mapReply;
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
            LispAddress locator = (LispAddress) locatorObject;
            eidToLocators.addLocator(new LocatorRecord().setLocator(locator).setRouted(true));
        } catch (ClassCastException cce) {
        }
    }
}
