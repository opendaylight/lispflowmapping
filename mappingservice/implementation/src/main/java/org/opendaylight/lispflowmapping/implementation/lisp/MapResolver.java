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
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapReplyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapResolver implements IMapResolverAsync {
    private ILispDAO dao;
    private volatile boolean shouldAuthenticate;
    private volatile boolean shouldIterateMask;
    protected static final Logger logger = LoggerFactory.getLogger(MapResolver.class);

    public MapResolver(ILispDAO dao) {
        this(dao, true, true);
    }

    public MapResolver(ILispDAO dao, boolean authenticate, boolean iterateMask) {
        this.dao = dao;
        this.shouldAuthenticate = authenticate;
        this.shouldIterateMask = iterateMask;
    }

    public void handleMapRequest(MapRequest request, IMapReplyHandler callback) {
        if (dao == null) {
            logger.warn("handleMapRequest called while dao is uninitialized");
        } else {
            MapReply mapReply = new MapReply();
            mapReply.setNonce(request.getNonce());
            for (EidRecord eid : request.getEids()) {
                EidToLocatorRecord eidToLocators = new EidToLocatorRecord();
                eidToLocators.setMaskLength(eid.getMaskLength())//
                        .setPrefix(eid.getPrefix());
                IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getPrefix(), eid.getMaskLength());
                Map<String, ?> locators = dao.get(key);
                if (shouldIterateMask() && locators == null && key.getEID() instanceof IMaskable) {
                    locators = findMaskLocators(key);
                }
                if (locators != null) {
                    addLocators(eidToLocators, locators);
                }
                mapReply.addEidToLocator(eidToLocators);
            }
            callback.handleMapReply(mapReply);
        }
    }

    private Map<String, ?> findMaskLocators(IMappingServiceKey key) {
        int mask = key.getMask();
        while (mask > 0) {
            key = MappingServiceKeyUtil.generateMappingServiceKey(key.getEID(), (byte) mask);

            ((IMaskable) key.getEID()).normalize(mask);
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
            MappingServiceValue value = (MappingServiceValue) locators.get("value");
            for (MappingServiceRLOC rloc : value.getRlocs()) {
                addLocator(eidToLocators, rloc);

            }
        } catch (ClassCastException cce) {
        }
    }

    private void addLocator(EidToLocatorRecord eidToLocators, MappingServiceRLOC locatorObject) {
        if (locatorObject == null) {
            return;
        }
        try {
            eidToLocators.addLocator(locatorObject.getRecord().clone().setRouted(true));
            eidToLocators.setAction(locatorObject.getAction());
            eidToLocators.setAuthoritative(locatorObject.isAuthoritative());
            eidToLocators.setRecordTtl(locatorObject.getTtl());
        } catch (ClassCastException cce) {
        }
    }

    public boolean shouldIterateMask() {
        return shouldIterateMask;
    }

    public boolean shouldAuthenticate() {
        return shouldAuthenticate;
    }

    public void setShouldIterateMask(boolean shouldIterateMask) {
        this.shouldIterateMask = shouldIterateMask;
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.shouldAuthenticate = shouldAuthenticate;
    }

}
