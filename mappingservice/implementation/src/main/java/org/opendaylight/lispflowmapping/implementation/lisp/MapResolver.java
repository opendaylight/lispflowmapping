/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
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

public class MapResolver extends AbstractLispComponent implements IMapResolverAsync {
    private static final int TTL_RLOC_TIMED_OUT = 1;
    private static final int TTL_NO_RLOC_KNOWN = 15;
    protected static final Logger logger = LoggerFactory.getLogger(MapResolver.class);

    public MapResolver(ILispDAO dao) {
        this(dao, true, true);
    }

    public MapResolver(ILispDAO dao, boolean authenticate, boolean iterateMask) {
        super(dao, authenticate, iterateMask);
    }

    public void handleMapRequest(MapRequest request, IMapRequestResultHandler callback) {
        if (dao == null) {
            logger.warn("handleMapRequest called while dao is uninitialized");
            return;
        }
        if (request.isPitr()) {
            if (request.getEidRecord().size() > 0) {
                EidRecord eid = request.getEidRecord().get(0);
                Object result = getLocatorsSpecific(eid, "address");
                if (result != null && result instanceof List) {
                    List<MappingServiceRLOC> locators = (List<MappingServiceRLOC>) result;
                    if (locators != null && locators.size() > 0) {
                        callback.handleNonProxyMapRequest(request,
                                LispNotificationHelper.getInetAddressFromContainer(locators.iterator().next().getRecord().getLispAddressContainer()));
                    }
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
                List<MappingServiceRLOC> locators = getLocators(eid);
                if (locators != null && locators.size() > 0) {
                    addLocators(recordBuilder, locators);
                } else {
                    recordBuilder
                            .setAction(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action.NativelyForward);
                    if (getPassword(eid.getLispAddressContainer(), eid.getMask()) != null) {
                        recordBuilder.setRecordTtl(TTL_RLOC_TIMED_OUT);
                    } else {
                        recordBuilder.setRecordTtl(TTL_NO_RLOC_KNOWN);

                    }
                }
                builder.getEidToLocatorRecord().add(recordBuilder.build());
            }

            callback.handleMapReply(builder.build());
        }
    }

    private List<MappingServiceRLOC> getLocators(EidRecord eid) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        Map<String, ?> locators = dao.get(key);
        List<MappingServiceRLOC> result = aggregateLocators(locators);
        if (shouldIterateMask() && result.isEmpty() && MaskUtil.isMaskable(key.getEID().getAddress())) {
            result = findMaskLocators(key);
        }
        return result;
    }

    private List<MappingServiceRLOC> aggregateLocators(Map<String, ?> locators) {
        List<MappingServiceRLOC> result = new ArrayList<MappingServiceRLOC>();
        if (locators != null) {
            for (Iterator iterator = locators.values().iterator(); iterator.hasNext();) {
                Object value = iterator.next();
                if (value != null && value instanceof List && !((List) value).isEmpty()) {
                    if (((List) value).iterator().next() instanceof MappingServiceRLOC) {
                        result.addAll((List<MappingServiceRLOC>) value);
                    }
                }
            }
        }
        return result;
    }

    private Object getLocatorsSpecific(EidRecord eid, String subkey) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        Object locators = dao.getSpecific(key, subkey);
        if (shouldIterateMask() && locators == null && MaskUtil.isMaskable(key.getEID().getAddress())) {
            locators = findMaskLocatorsSpecific(key, subkey);
        }
        return locators;
    }

    private Object findMaskLocatorsSpecific(IMappingServiceKey key, String subkey) {
        int mask = key.getMask();
        while (mask > 0) {
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    new LispAddressContainerBuilder().setAddress(MaskUtil.normalize(key.getEID().getAddress(), mask)).build(), mask);
            mask--;
            Object locators = dao.getSpecific(key, subkey);
            if (locators != null) {
                return locators;
            }
        }
        return null;
    }

    private List<MappingServiceRLOC> findMaskLocators(IMappingServiceKey key) {
        int mask = key.getMask();
        while (mask > 0) {
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    new LispAddressContainerBuilder().setAddress(MaskUtil.normalize(key.getEID().getAddress(), mask)).build(), mask);
            mask--;
            Map<String, ?> locators = dao.get(key);
            if (locators != null) {
                List<MappingServiceRLOC> result = aggregateLocators(locators);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }
        return null;
    }

    private void addLocators(EidToLocatorRecordBuilder recordBuilder, List<MappingServiceRLOC> rlocs) {
        for (MappingServiceRLOC rloc : rlocs) {
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

}
