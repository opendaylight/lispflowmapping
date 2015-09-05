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
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LcafSourceDestHelper;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafTrafficEngineering;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class MapResolver implements IMapResolverAsync {
    protected static final Logger LOG = LoggerFactory.getLogger(MapResolver.class);

    private static final int TTL_RLOC_TIMED_OUT = 1;
    private static final int TTL_NO_RLOC_KNOWN = 15;

    private IMappingService mapService;
    private boolean subscriptionService;
    private String elpPolicy;
    private IMapRequestResultHandler requestHandler;
    private boolean authenticate = true;

    public MapResolver(IMappingService mapService, boolean smr, String elpPolicy,
            IMapRequestResultHandler requestHandler) {
        Preconditions.checkNotNull(mapService);
        this.subscriptionService = smr;
        this.mapService = mapService;
        this.elpPolicy = elpPolicy;
        this.requestHandler = requestHandler;
    }

    public void handleMapRequest(MapRequest request) {
        LispAddressContainer srcEid = null;
        if (request.getSourceEid() != null) {
            srcEid = request.getSourceEid().getLispAddressContainer();
        }
        MapReplyBuilder replyBuilder = new MapReplyBuilder();
        replyBuilder.setEchoNonceEnabled(false);
        replyBuilder.setProbe(false);
        replyBuilder.setSecurityEnabled(false);
        replyBuilder.setNonce(request.getNonce());
        replyBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        for (EidRecord eidRecord : request.getEidRecord()) {
            EidToLocatorRecord mapping = (EidToLocatorRecord) mapService.getMapping(srcEid,
                    eidRecord.getLispAddressContainer());
            if (mapping != null) {
                List<ItrRloc> itrRlocs = request.getItrRloc();
                if (itrRlocs != null && itrRlocs.size() != 0) {
                    if (subscriptionService) {
                        updateSubscribers(itrRlocs.get(0).getLispAddressContainer(), mapping.getLispAddressContainer(),
                                srcEid);
                    }
                    mapping = updateLocators(mapping, itrRlocs);
                }
                mapping = fixIfNotSDRequest(mapping, eidRecord.getLispAddressContainer());
            } else {
                mapping = getNegativeMapping(eidRecord.getLispAddressContainer(), eidRecord.getMask());
            }
            replyBuilder.getEidToLocatorRecord().add(mapping);
        }
        requestHandler.handleMapReply(replyBuilder.build());
    }

    private EidToLocatorRecord getNegativeMapping(LispAddressContainer eid, short mask) {
        EidToLocatorRecordBuilder recordBuilder = new EidToLocatorRecordBuilder();
        recordBuilder.setAuthoritative(false);
        recordBuilder.setMapVersion((short) 0);
        recordBuilder.setMaskLength(mask);
        recordBuilder.setLispAddressContainer(eid);
        recordBuilder.setAction(Action.NativelyForward);
        if (authenticate && mapService.getAuthenticationKey(eid) != null) {
            recordBuilder.setRecordTtl(TTL_RLOC_TIMED_OUT);
        } else {
            recordBuilder.setRecordTtl(TTL_NO_RLOC_KNOWN);
        }
        return recordBuilder.build();
    }

    private void updateSubscribers(LispAddressContainer itrRloc, LispAddressContainer dstEid,
            LispAddressContainer srcEid) {
        SubscriberRLOC subscriberRloc = new SubscriberRLOC(itrRloc, srcEid);
        Set<SubscriberRLOC> subscribers = getSubscribers(dstEid);
        if (subscribers == null) {
            subscribers = Sets.newConcurrentHashSet();
        } else if (subscribers.contains(subscriberRloc)) {
            // If there is an entry already for this subscriberRloc, remove it, so that it gets the new
            // timestamp
            subscribers.remove(subscriberRloc);
        }
        LOG.trace("Adding new subscriber: " + subscriberRloc.toString());
        subscribers.add(subscriberRloc);
        addSubscribers(dstEid, subscribers);
    }

    // Fixes mapping if request was for simple dst EID but the matched mapping is a SourceDest
    private EidToLocatorRecord fixIfNotSDRequest(EidToLocatorRecord mapping, LispAddressContainer dstEid) {
        if (mapping.getLispAddressContainer().getAddress() instanceof LcafSourceDest
                && !(dstEid.getAddress() instanceof LcafSourceDest)) {
            return new EidToLocatorRecordBuilder(mapping).setLispAddressContainer(
                    LcafSourceDestHelper.getDst(mapping.getLispAddressContainer())).build();
        }
        return mapping;
    }

    private boolean locatorsNeedFixing(List<LocatorRecord> locatorRecords) {
        for (LocatorRecord record : locatorRecords) {
            if (record.getLispAddressContainer().getAddress() instanceof LcafTrafficEngineering) {
                return true;
            }
        }
        return false;
    }

    // Process locators according to configured policy
    private EidToLocatorRecord updateLocators(EidToLocatorRecord mapping, List<ItrRloc> itrRlocs) {
        // no fixing if elpPolicy is default
        if (elpPolicy.equalsIgnoreCase("default")) {
            return mapping;
        }

        List<LocatorRecord> locatorRecords = mapping.getLocatorRecord();

        // if no updated is needed, just return the mapping
        if (!locatorsNeedFixing(locatorRecords)) {
            return mapping;
        }

        EidToLocatorRecordBuilder recordBuilder = new EidToLocatorRecordBuilder(mapping);
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        try {
            for (LocatorRecord record : locatorRecords) {
                LispAddressContainer container = record.getLispAddressContainer();

                // For non-ELP RLOCs, or when ELP policy is default, or itrRlocs is null, just add the locator and be
                // done
                if ((!(container.getAddress() instanceof LcafTrafficEngineering))
                        || elpPolicy.equalsIgnoreCase("default") || itrRlocs == null) {
                    recordBuilder.getLocatorRecord().add(
                            new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                    .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                    .setPriority(record.getPriority()).setMulticastWeight(record.getMulticastWeight())
                                    .setMulticastPriority(record.getMulticastPriority()).setRouted(record.isRouted())
                                    .setLispAddressContainer(container).setName(record.getName()).build());
                    continue;
                }

                LcafTrafficEngineeringAddress teAddress = ((LcafTrafficEngineering) container.getAddress())
                        .getLcafTrafficEngineeringAddr();
                LispAddressContainer nextHop = getNextELPHop(teAddress, itrRlocs);
                if (nextHop != null) {
                    java.lang.Short priority = record.getPriority();
                    if (elpPolicy.equalsIgnoreCase("both")) {
                        recordBuilder.getLocatorRecord().add(
                                new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                        .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                        .setPriority(record.getPriority())
                                        .setMulticastWeight(record.getMulticastWeight())
                                        .setMulticastPriority(record.getMulticastPriority())
                                        .setRouted(record.isRouted()).setLispAddressContainer(container)
                                        .setName(record.getName()).build());
                        // Make the priority of the added simple locator lower so that ELP is used by default if
                        // the xTR understands ELP. Exclude 255, since that means don't use for unicast forwarding
                        // XXX Complex cases like several ELPs with different priorities are not handled
                        if (priority != 254 || priority != 255) {
                            priority++;
                        }
                    }
                    // Build and add the simple RLOC
                    recordBuilder.getLocatorRecord().add(
                            new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                    .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                    .setPriority(priority).setMulticastWeight(record.getMulticastWeight())
                                    .setMulticastPriority(record.getMulticastPriority()).setRouted(record.isRouted())
                                    .setLispAddressContainer(nextHop).setName(record.getName()).build());
                }
            }
        } catch (ClassCastException cce) {
            LOG.error("Class Cast Exception while building EidToLocatorRecord: {}", ExceptionUtils.getStackTrace(cce));
        }

        return recordBuilder.build();
    }

    private LispAddressContainer getNextELPHop(LcafTrafficEngineeringAddress elp, List<ItrRloc> itrRlocs) {
        LispAddressContainer nextHop = null;
        List<Hops> hops = elp.getHops();

        if (hops != null && hops.size() > 0) {
            // By default we return the first hop
            nextHop = LispAFIConvertor.toContainer(LispAFIConvertor.toAFIfromPrimitive(hops.get(0).getHop()
                    .getPrimitiveAddress()));
            for (Hops hop : hops) {
                LispAddressContainer hopContainer = LispAFIConvertor.toContainer(LispAFIConvertor
                        .toAFIfromPrimitive(hop.getHop().getPrimitiveAddress()));
                for (ItrRloc itrRloc : itrRlocs) {
                    if (itrRloc.getLispAddressContainer().equals(hopContainer)) {
                        int i = hops.indexOf(hop);
                        if (i < hops.size() - 1) {
                            nextHop = LispAFIConvertor.toContainer(LispAFIConvertor.toAFIfromPrimitive(hops.get(i + 1)
                                    .getHop().getPrimitiveAddress()));
                            return nextHop;
                        }
                    }
                }
            }
        }

        return nextHop;
    }

    @SuppressWarnings("unchecked")
    private Set<SubscriberRLOC> getSubscribers(LispAddressContainer address) {
        return (Set<SubscriberRLOC>) mapService.getData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS);
    }

    private void addSubscribers(LispAddressContainer address, Set<SubscriberRLOC> subscribers) {
        mapService.addData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS, subscribers);
    }

    @Override
    public void setSubscriptionService(boolean smr) {
        subscriptionService = smr;
    }

    @Override
    public void setElpPolicy(String elpPolicy) {
        this.elpPolicy = elpPolicy;
    }

    @Override
    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.authenticate = shouldAuthenticate;
    }
}
