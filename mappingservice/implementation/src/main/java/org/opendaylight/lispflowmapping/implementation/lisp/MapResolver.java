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
import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapreplymessage.MapReplyBuilder;
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
            MapReplyBuilder builder = new MapReplyBuilder();
            builder.setNonce(request.getNonce());
            for (EidRecord eid : request.getEidRecord()) {
                EidToLocatorRecordBuilder recordBuilder = new EidToLocatorRecordBuilder();
                recordBuilder.setMaskLength(eid.getMask());
                recordBuilder.setLispAddressContainer(eid.getLispAddressContainer());
                IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
                Map<String, ?> locators = dao.get(key);
                if (shouldIterateMask() && locators == null && key.getEID() instanceof IMaskable) {
                    locators = findMaskLocators(key);
                }
                if (locators != null) {
                    addLocators(recordBuilder, locators);
                }
                builder.getEidToLocatorRecord().add(recordBuilder.build());
            }
            callback.handleMapReply(builder.build());
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

    private void addLocators(EidToLocatorRecordBuilder recordBuilder, Map<String, ?> locators) {
        try {
            MappingServiceValue value = (MappingServiceValue) locators.get("value");
            for (MappingServiceRLOC rloc : value.getRlocs()) {
                addLocator(recordBuilder, rloc);

            }
        } catch (ClassCastException cce) {
        }
    }

    private void addLocator(EidToLocatorRecordBuilder recordBuilder, MappingServiceRLOC locatorObject) {
        if (locatorObject == null) {
            return;
        }
        try {
            recordBuilder.getLocatorRecord().add(
                    new LocatorRecordBuilder().setRouted(true).setLispAddressContainer(locatorObject.getRecord().getLispAddressContainer()).build());
            recordBuilder.setAction(locatorObject.getAction());
            recordBuilder.setAuthoritative(locatorObject.isAuthoritative());
            recordBuilder.setRecordTtl(locatorObject.getTtl());
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
