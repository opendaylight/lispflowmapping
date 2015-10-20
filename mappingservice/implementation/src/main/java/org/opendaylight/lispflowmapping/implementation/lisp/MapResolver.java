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
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lcaf.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lcaf.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lcaf.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.rloc.container.Rloc;
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
        Eid srcEid = null;
        if (request.getSourceEid() != null) {
            srcEid = request.getSourceEid().getEid();
        }
        MapReplyBuilder replyBuilder = new MapReplyBuilder();
        replyBuilder.setEchoNonceEnabled(false);
        replyBuilder.setProbe(false);
        replyBuilder.setSecurityEnabled(false);
        replyBuilder.setNonce(request.getNonce());
        replyBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        for (EidRecord eidRecord : request.getEidRecord()) {
            MappingRecord mapping = (MappingRecord) mapService.getMapping(srcEid,
                    eidRecord.getEid());
            if (mapping != null) {
                List<ItrRloc> itrRlocs = request.getItrRloc();
                if (itrRlocs != null && itrRlocs.size() != 0) {
                    if (subscriptionService) {
                        updateSubscribers(itrRlocs.get(0).getRloc(), mapping.getEid(),
                                srcEid);
                    }
                    mapping = updateLocators(mapping, itrRlocs);
                }
                mapping = fixIfNotSDRequest(mapping, eidRecord.getEid());
            } else {
                mapping = getNegativeMapping(eidRecord.getEid());
            }
            replyBuilder.getEidToLocatorRecord().add(new EidToLocatorRecordBuilder().setMappingRecord(mapping).build());
        }
        requestHandler.handleMapReply(replyBuilder.build());
    }

    private MappingRecord getNegativeMapping(Eid eid) {
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setAuthoritative(false);
        recordBuilder.setMapVersion((short) 0);
        recordBuilder.setEid(eid);
        recordBuilder.setAction(Action.NativelyForward);
        if (authenticate && mapService.getAuthenticationKey(eid) != null) {
            recordBuilder.setRecordTtl(TTL_RLOC_TIMED_OUT);
        } else {
            recordBuilder.setRecordTtl(TTL_NO_RLOC_KNOWN);
        }
        return recordBuilder.build();
    }

    private void updateSubscribers(Rloc itrRloc, Eid dstEid,
            Eid srcEid) {
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
    private MappingRecord fixIfNotSDRequest(MappingRecord mapping, Eid dstEid) {
        if (mapping.getEid().getAddress() instanceof SourceDestKey
                && !(dstEid.getAddress() instanceof SourceDestKey)) {
            return new MappingRecordBuilder(mapping).setEid(
                    SourceDestKeyHelper.getDst(mapping.getEid())).build();
        }
        return mapping;
    }

    private boolean locatorsNeedFixing(List<LocatorRecord> locatorRecords) {
        for (LocatorRecord record : locatorRecords) {
            if (record.getRloc().getAddress() instanceof ExplicitLocatorPath) {
                return true;
            }
        }
        return false;
    }

    // Process locators according to configured policy
    private MappingRecord updateLocators(MappingRecord mapping, List<ItrRloc> itrRlocs) {
        // no fixing if elpPolicy is default
        if (elpPolicy.equalsIgnoreCase("default")) {
            return mapping;
        }

        List<LocatorRecord> locatorRecords = mapping.getLocatorRecord();

        // if no updated is needed, just return the mapping
        if (!locatorsNeedFixing(locatorRecords)) {
            return mapping;
        }

        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        try {
            for (LocatorRecord record : locatorRecords) {
                Rloc container = record.getRloc();

                // For non-ELP RLOCs, or when ELP policy is default, or itrRlocs is null, just add the locator and be
                // done
                if ((!(container.getAddress() instanceof ExplicitLocatorPath))
                        || elpPolicy.equalsIgnoreCase("default") || itrRlocs == null) {
                    recordBuilder.getLocatorRecord().add(
                            new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                    .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                    .setPriority(record.getPriority()).setMulticastWeight(record.getMulticastWeight())
                                    .setMulticastPriority(record.getMulticastPriority()).setRouted(record.isRouted())
                                    .setRloc(container).setName(record.getName()).build());
                    continue;
                }

                ExplicitLocatorPath teAddress = ((ExplicitLocatorPath) container.getAddress());
                SimpleAddress nextHop = getNextELPHop(teAddress, itrRlocs);
                if (nextHop != null) {
                    java.lang.Short priority = record.getPriority();
                    if (elpPolicy.equalsIgnoreCase("both")) {
                        recordBuilder.getLocatorRecord().add(
                                new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                        .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                        .setPriority(record.getPriority())
                                        .setMulticastWeight(record.getMulticastWeight())
                                        .setMulticastPriority(record.getMulticastPriority())
                                        .setRouted(record.isRouted()).setRloc(container)
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
                                    .setRloc(nextHop).setName(record.getName()).build());
                }
            }
        } catch (ClassCastException cce) {
            LOG.error("Class Cast Exception while building EidToLocatorRecord: {}", ExceptionUtils.getStackTrace(cce));
        }

        return recordBuilder.build();
    }

    private SimpleAddress getNextELPHop(ExplicitLocatorPath elp, List<ItrRloc> itrRlocs) {
        SimpleAddress nextHop = null;
        List<Hop> hops = elp.getExplicitLocatorPath().getHop();

        if (hops != null && hops.size() > 0) {
            // By default we return the first hop
            nextHop = hops.get(0).getAddress();
            for (Hop hop : hops) {
                SimpleAddress hopContainer = hop.getAddress();
                for (ItrRloc itrRloc : itrRlocs) {
                    // XXX Need conversion
                    if (itrRloc.getRloc().equals(hopContainer)) {
                        int i = hops.indexOf(hop);
                        if (i < hops.size() - 1) {
                            nextHop = hops.get(i + 1).getAddress();
                            return nextHop;
                        }
                    }
                }
            }
        }

        return nextHop;
    }

    @SuppressWarnings("unchecked")
    private Set<SubscriberRLOC> getSubscribers(Eid address) {
        return (Set<SubscriberRLOC>) mapService.getData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS);
    }

    private void addSubscribers(Eid address, Set<SubscriberRLOC> subscribers) {
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
