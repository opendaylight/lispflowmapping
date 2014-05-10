/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.DAOMappingUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOCGroup;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceSubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
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

    public void handleMapRequest(MapRequest request, boolean smr, IMapRequestResultHandler callback) {
        if (dao == null) {
            logger.warn("handleMapRequest called while dao is uninitialized");
            return;
        }
        if (request.isPitr()) {
            if (request.getEidRecord().size() > 0) {
                EidRecord eid = request.getEidRecord().get(0);
                Object result = DAOMappingUtil.getLocatorsSpecificByEidRecord(eid, dao, ADDRESS_SUBKEY, shouldIterateMask());
                if (result != null && result instanceof MappingServiceRLOCGroup) {
                    MappingServiceRLOCGroup locatorsGroup = (MappingServiceRLOCGroup) result;
                    if (locatorsGroup != null && locatorsGroup.getRecords().size() > 0) {
                        callback.handleNonProxyMapRequest(request,
                                LispNotificationHelper.getTransportAddressFromContainer(locatorsGroup.getRecords().get(0).getLispAddressContainer()));
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
                List<MappingServiceRLOCGroup> locators = DAOMappingUtil.getLocatorsByEidRecord(eid, dao, shouldIterateMask());
                if (locators != null && locators.size() > 0) {
                    addLocatorGroups(recordBuilder, locators);
                    if (request.getItrRloc() != null && request.getItrRloc().size() > 0) {
                        LispAddressContainer itrRloc = request.getItrRloc().get(0).getLispAddressContainer();
                        MappingServiceSubscriberRLOC subscriberRloc = new MappingServiceSubscriberRLOC(itrRloc);
                        HashSet<MappingServiceSubscriberRLOC> subscribers = getSubscribers(eid.getLispAddressContainer(), eid.getMask());
                        if (subscribers == null) {
                            subscribers = new HashSet<MappingServiceSubscriberRLOC>();
                        } else if (subscribers.contains(subscriberRloc)) {
                            /*
                             * If there is an entry already for this
                             * subscriberRloc, remove it, so that it gets the
                             * new timestamp
                             */
                            subscribers.remove(subscriberRloc);
                        }
                        if (smr) {
                            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
                            logger.trace("Adding new subscriber: " + subscriberRloc.toString());
                            subscribers.add(subscriberRloc);
                            dao.put(key, new MappingEntry<HashSet<MappingServiceSubscriberRLOC>>(SUBSCRIBERS_SUBKEY, subscribers));
                        }
                    }
                } else {
                    recordBuilder
                            .setAction(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action.NativelyForward);
                    if (shouldAuthenticate() && getPassword(eid.getLispAddressContainer(), eid.getMask()) != null) {
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
