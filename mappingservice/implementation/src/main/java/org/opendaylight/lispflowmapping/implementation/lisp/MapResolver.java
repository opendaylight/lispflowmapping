/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOCGroup;
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
                Object result = getLocatorsSpecific(eid, ADDRESS_SUBKEY);
                if (result != null && result instanceof MappingServiceRLOCGroup) {
                    MappingServiceRLOCGroup locatorsGroup = (MappingServiceRLOCGroup) result;
                    if (locatorsGroup != null && locatorsGroup.getRecords().size() > 0) {
                        callback.handleNonProxyMapRequest(request,
                                LispNotificationHelper.getInetAddressFromContainer(locatorsGroup.getRecords().get(0).getLispAddressContainer()));
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
                List<MappingServiceRLOCGroup> locators = getLocators(eid);
                if (locators != null && locators.size() > 0) {
                    addLocatorGroups(recordBuilder, locators);
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

    private List<MappingServiceRLOCGroup> getLocators(EidRecord eid) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        Map<String, ?> locators = dao.get(key);
        List<MappingServiceRLOCGroup> result = aggregateLocators(locators);
        if (shouldIterateMask() && result.isEmpty() && MaskUtil.isMaskable(key.getEID().getAddress())) {
            result = findMaskLocators(key);
        }
        return result;
    }

    private List<MappingServiceRLOCGroup> aggregateLocators(Map<String, ?> locators) {
        List<MappingServiceRLOCGroup> result = new ArrayList<MappingServiceRLOCGroup>();
        if (locators != null) {
            for (Object value : locators.values()) {
                if (value != null && value instanceof MappingServiceRLOCGroup) {
                    if (!((MappingServiceRLOCGroup) value).getRecords().isEmpty()) {
                        result.add((MappingServiceRLOCGroup) value);
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

    private List<MappingServiceRLOCGroup> findMaskLocators(IMappingServiceKey key) {
        int mask = key.getMask();
        while (mask > 0) {
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    new LispAddressContainerBuilder().setAddress(MaskUtil.normalize(key.getEID().getAddress(), mask)).build(), mask);
            mask--;
            Map<String, ?> locators = dao.get(key);
            if (locators != null) {
                List<MappingServiceRLOCGroup> result = aggregateLocators(locators);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }
        return null;
    }

    private void addLocatorGroups(EidToLocatorRecordBuilder recordBuilder, List<MappingServiceRLOCGroup> rlocs) {
        for (MappingServiceRLOCGroup rloc : rlocs) {
            addLocators(recordBuilder, rloc);
            recordBuilder.setRecordTtl(rloc.getTtl());
        }
    }

    private void addLocators(EidToLocatorRecordBuilder recordBuilder, MappingServiceRLOCGroup locatorObject) {
        if (locatorObject == null) {
            return;
        }
        try {
            for (LocatorRecord record : locatorObject.getRecords()) {
                recordBuilder.getLocatorRecord().add(
                        new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator()).setRlocProbed(record.isRlocProbed())
                                .setWeight(record.getWeight()).setPriority(record.getPriority()).setMulticastWeight(record.getMulticastWeight())
                                .setMulticastPriority(record.getMulticastPriority()).setRouted(true)
                                .setLispAddressContainer(record.getLispAddressContainer()).build());
            }
            recordBuilder.setAction(locatorObject.getAction());
            recordBuilder.setAuthoritative(locatorObject.isAuthoritative());
            recordBuilder.setRecordTtl(locatorObject.getTtl());
        } catch (ClassCastException cce) {
        }
    }

}
