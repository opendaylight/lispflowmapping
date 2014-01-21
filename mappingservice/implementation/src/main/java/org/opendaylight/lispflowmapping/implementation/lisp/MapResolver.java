/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.Map;

import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapreplymessage.MapReplyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapResolver implements IMapResolverAsync {
    private static final int TTL_RLOC_TIMED_OUT = 1;
    private static final int TTL_NO_RLOC_KNOWN = 15;
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

    public void handleMapRequest(MapRequest request, IMapRequestResultHandler callback) {
        if (dao == null) {
            logger.warn("handleMapRequest called while dao is uninitialized");
            return;
        }
        if (request.isPitr()) {
            if (request.getEidRecord().size() > 0) {
                EidRecord eid = request.getEidRecord().get(0);
                Map<String, ?> locators = getLocators(eid);
                MappingServiceValue value = (MappingServiceValue) locators.get("value");
                if (value.getRlocs() != null && value.getRlocs().size() > 0) {
                    callback.handleNonProxyMapRequest(request,
                            LispNotificationHelper.getInetAddressFromContainer(value.getRlocs().get(0).getRecord().getLispAddressContainer()));
                }
            }

        } else {
            MapReplyBuilder builder = new MapReplyBuilder();
            builder.setEchoNonceEnabled(false);
            builder.setProbe(false);
            builder.setSecurityEnabled(false);
            builder.setNonce(request.getNonce());
            builder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
            for (EidRecord eid : request.getEidRecord()) {
                EidToLocatorRecordBuilder recordBuilder = new EidToLocatorRecordBuilder();
                recordBuilder.setRecordTtl(0);
                recordBuilder.setAction(Action.NoAction);
                recordBuilder.setAuthoritative(false);
                recordBuilder.setMapVersion((short) 0);
                recordBuilder.setMaskLength(eid.getMask());
                recordBuilder.setLispAddressContainer(eid.getLispAddressContainer());
                recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
                Map<String, ?> locators = getLocators(eid);
                if (locators != null) {
                    MappingServiceValue value = (MappingServiceValue) locators.get("value");

                    if (value.getRlocs() != null && value.getRlocs().size() > 0) {
                        addLocators(recordBuilder, value);
                    } else {
                        recordBuilder
                                .setAction(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action.NativelyForward);
                        recordBuilder.setRecordTtl(TTL_RLOC_TIMED_OUT);
                    }
                } else {
                    recordBuilder
                            .setAction(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action.NativelyForward);
                    recordBuilder.setRecordTtl(TTL_NO_RLOC_KNOWN);
                }
                builder.getEidToLocatorRecord().add(recordBuilder.build());
            }
            callback.handleMapReply(builder.build());
        }
    }

    private Map<String, ?> getLocators(EidRecord eid) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        Map<String, ?> locators = dao.get(key);
        if (shouldIterateMask() && locators == null && MaskUtil.isMaskable(key.getEID().getAddress())) {
            locators = findMaskLocators(key);
        }
        return locators;
    }

    private Map<String, ?> findMaskLocators(IMappingServiceKey key) {
        int mask = key.getMask();
        while (mask > 0) {
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    new LispAddressContainerBuilder().setAddress(MaskUtil.normalize(key.getEID().getAddress(), mask)).build(), mask);
            mask--;
            Map<String, ?> locators = dao.get(key);
            if (locators != null) {
                return locators;
            }
        }
        return null;
    }

    private void addLocators(EidToLocatorRecordBuilder recordBuilder, MappingServiceValue value) {
        for (MappingServiceRLOC rloc : value.getRlocs()) {
            addLocator(recordBuilder, rloc);
            recordBuilder.setRecordTtl(rloc.getTtl());
        }
    }

    private void addLocator(EidToLocatorRecordBuilder recordBuilder, MappingServiceRLOC locatorObject) {
        if (locatorObject == null) {
            return;
        }
        try {
            recordBuilder.getLocatorRecord().add(
                    new LocatorRecordBuilder().setLocalLocator(locatorObject.getRecord().isLocalLocator())
                            .setRlocProbed(locatorObject.getRecord().isRlocProbed()).setWeight(locatorObject.getRecord().getWeight())
                            .setPriority(locatorObject.getRecord().getPriority()).setMulticastWeight(locatorObject.getRecord().getMulticastWeight())
                            .setMulticastPriority(locatorObject.getRecord().getMulticastPriority()).setRouted(true)
                            .setLispAddressContainer(locatorObject.getRecord().getLispAddressContainer()).build());
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
