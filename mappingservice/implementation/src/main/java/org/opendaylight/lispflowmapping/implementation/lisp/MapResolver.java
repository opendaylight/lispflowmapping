/*
 * Copyright (c) 2014, 2017 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.ISmrNotificationListener;
import org.opendaylight.lispflowmapping.interfaces.lisp.SmrEvent;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MappingRecordUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.lispflowmapping.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapResolver implements IMapResolverAsync {
    protected static final Logger LOG = LoggerFactory.getLogger(MapResolver.class);

    private IMappingService mapService;
    private boolean subscriptionService;
    private String elpPolicy;
    private IMapRequestResultHandler requestHandler;
    private boolean authenticate = true;
    private ISmrNotificationListener smrNotificationListener;
    private static final int TTL_DELETE_MAPPING = 0;

    public MapResolver(IMappingService mapService, boolean smr, String elpPolicy,
                       IMapRequestResultHandler requestHandler) {
        Preconditions.checkNotNull(mapService);
        this.subscriptionService = smr;
        this.mapService = mapService;
        this.elpPolicy = elpPolicy;
        this.requestHandler = requestHandler;
    }

    public void handleMapRequest(MapRequest request) {
        LOG.trace("Map-Request received: {}", request);
        // SMRs and RLOC probes are directed towards xTRs and we're a Map-Resolver here, so ignore them
        if (request.isSmr() != null && request.isSmr()) {
            LOG.debug("Map-Resolver ignoring incoming SMR control message.");
            return;
        }
        if (request.isProbe() != null && request.isProbe()) {
            LOG.debug("Map-Resolver ignoring incoming RLOC probe control message.");
            return;
        }
        if (request.isSmrInvoked()) {
            LOG.debug("SMR-invoked request received.");
            LOG.trace("Map-Request object: {}", request);
            for (EidItem eidItem : request.getEidItem()) {
                final SmrEvent event = new SmrEvent(
                        subscriberListFromItrRlocs(request.getItrRloc(), request.getSourceEid().getEid()),
                        eidItem.getEid(),
                        request.getNonce());
                smrNotificationListener.onSmrInvokedReceived(event);
            }
        }
        Eid srcEid = null;
        if (request.getSourceEid() != null) {
            srcEid = request.getSourceEid().getEid();
        }
        MapReplyBuilder replyBuilder = new MapReplyBuilder();
        replyBuilder.setEchoNonceEnabled(false);
        replyBuilder.setProbe(false);
        replyBuilder.setSecurityEnabled(false);
        replyBuilder.setNonce(request.getNonce());
        replyBuilder.setMappingRecordItem(new ArrayList<>());
        List<ItrRloc> itrRlocs = request.getItrRloc();
        final IpAddressBinary sourceRloc = request.getSourceRloc();

        for (EidItem eidRecord : request.getEidItem()) {
            MappingData mappingData = mapService.getMapping(srcEid, eidRecord.getEid());
            MappingRecord mapping;
            Set<MappingOrigin> origins;
            if (mappingData == null) {
                mappingData = mapService.addNegativeMapping(eidRecord.getEid());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No mapping found for EID {}, adding negative mapping.",
                            LispAddressStringifier.getString(eidRecord.getEid()));
                }
            }
            mapping = mappingData.getRecord();
            origins = mappingData.getOrigin();

            if (itrRlocs != null && itrRlocs.size() != 0) {
                if (subscriptionService) {
                    final Rloc resolvedRloc = resolveRloc(itrRlocs, sourceRloc);
                    updateSubscribers(origins, resolvedRloc, eidRecord.getEid(), mapping.getEid(),
                            srcEid, mapping.getRecordTtl());
                }
                mapping = updateLocators(mapping, itrRlocs);
            }
            mapping = fixIfNotSDRequest(mapping, eidRecord.getEid());
            mapping = fixTtlIfSmrInvoked(request, mapping);
            replyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(mapping).build());
        }
        requestHandler.handleMapReply(replyBuilder.build());
    }

    private static boolean isEqualIpVersion(IpAddressBinary srcRloc, Rloc rloc) {
        if (srcRloc.getIpv4AddressBinary() != null) {
            if (rloc.getAddressType() == Ipv4Afi.class
                    || rloc.getAddressType() == Ipv4BinaryAfi.class
                    || rloc.getAddressType() == Ipv4PrefixAfi.class
                    || rloc.getAddressType() == Ipv4PrefixBinaryAfi.class) {
                return true;
            }
        } else if (rloc.getAddressType() == Ipv6Afi.class
                || rloc.getAddressType() == Ipv6BinaryAfi.class
                || rloc.getAddressType() == Ipv6PrefixAfi.class
                || rloc.getAddressType() == Ipv6PrefixBinaryAfi.class) {
            return true;
        }
        return false;
    }

    private Rloc resolveRloc(List<ItrRloc> itrRlocList, IpAddressBinary srcRloc) {
        if (srcRloc == null) {
            return itrRlocList.get(0).getRloc();
        }
        byte[] srcRlocByte;
        if (srcRloc.getIpv4AddressBinary() != null) {
            srcRlocByte = srcRloc.getIpv4AddressBinary().getValue();
        } else {
            srcRlocByte = srcRloc.getIpv6AddressBinary().getValue();
        }

        Rloc equalIpvRloc = null;
        for (ItrRloc itrRloc : itrRlocList) {
            final Rloc rloc = itrRloc.getRloc();
            final byte[] itrRlocByte = LispAddressUtil.ipAddressToByteArray(rloc.getAddress());

            // return an Rloc equal to the source Rloc
            if (itrRlocByte != null && LispAddressUtil.compareIpAddressByteArrays(srcRlocByte, itrRlocByte) == 0) {
                return rloc;
            }
            // else lookup the first Rloc with identical Ip version
            if (equalIpvRloc == null && isEqualIpVersion(srcRloc, rloc)) {
                equalIpvRloc = rloc;
            }
        }
        if (equalIpvRloc != null) {
            return equalIpvRloc;
        } else {
            // if none of the above, return the first Rloc
            return itrRlocList.get(0).getRloc();
        }
    }

    private void updateSubscribers(Set<MappingOrigin> origins, Rloc itrRloc, Eid reqEid, Eid mapEid, Eid srcEid,
            Integer recordTtl) {
        Subscriber subscriber = new Subscriber(itrRloc, srcEid, Subscriber.recordTtlToSubscriberTime(recordTtl));
        Eid subscribedEid = mapEid;

        // If the eid in the matched mapping is SourceDest and the requested eid IS NOT then we subscribe itrRloc only
        // to dst from the src/dst since that what's been requested. Note though that any updates to to the src/dst
        // mapping will be pushed to dst as well (see sendSMRs in MapServer)
        if (mapEid.getAddressType().equals(SourceDestKeyLcaf.class)
                && !reqEid.getAddressType().equals(SourceDestKeyLcaf.class)) {
            subscribedEid = SourceDestKeyHelper.getDstBinary(mapEid);
        }

        LOG.debug("Adding subscriber from source EID {}, RLOC {} for EID {}\nOrigins: {}",
                LispAddressStringifier.getString(srcEid),
                LispAddressStringifier.getString(itrRloc),
                LispAddressStringifier.getString(subscribedEid),
                origins);

        mapService.updateSubscribers(origins, subscribedEid, subscriber);
    }

    // Fixes mapping if request was for simple dst EID but the matched mapping is a SourceDest
    private MappingRecord fixIfNotSDRequest(MappingRecord mapping, Eid dstEid) {
        if (mapping.getEid().getAddress() instanceof SourceDestKey
                && !(dstEid.getAddress() instanceof SourceDestKey)) {
            return new MappingRecordBuilder(mapping).setEid(
                    SourceDestKeyHelper.getDstBinary(mapping.getEid())).build();
        }
        return mapping;
    }

    // When an SMR-invoked Map-Request is asking for a mapping that is negative, it is most likely an attempt to delete
    // that mapping.
    private MappingRecord fixTtlIfSmrInvoked(MapRequest request, MappingRecord mapping) {
        if (request.isSmrInvoked() && MappingRecordUtil.isNegativeMapping(mapping)) {
            return new MappingRecordBuilder(mapping).setRecordTtl(TTL_DELETE_MAPPING).build();
        }
        return mapping;
    }

    private boolean locatorsNeedFixing(List<LocatorRecord> locatorRecords) {
        // no locators - no fixing needed ;)
        if (locatorRecords == null) {
            return false;
        }

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

        MappingRecordBuilder recordBuilder = new MappingRecordBuilder(mapping);
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
                                    .setRloc(container).setLocatorId(record.getLocatorId()).build());
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
                                        .setLocatorId(record.getLocatorId()).build());
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
                                    .setRloc(LispAddressUtil.toRloc(nextHop))
                                    .setLocatorId(record.getLocatorId()).build());
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
                Address hopAddress = LispAddressUtil.addressFromSimpleAddress(hop.getAddress());
                for (ItrRloc itrRloc : itrRlocs) {
                    if (itrRloc.getRloc().getAddress().equals(hopAddress)) {
                        int iterator = hops.indexOf(hop);
                        if (iterator < hops.size() - 1) {
                            nextHop = hops.get(iterator + 1).getAddress();
                            return nextHop;
                        }
                    }
                }
            }
        }

        return nextHop;
    }

    private static List<Subscriber> subscriberListFromItrRlocs(List<ItrRloc> itrRlocs, Eid srcEid) {
        List<Subscriber> subscriberList = Lists.newArrayList();
        for (ItrRloc itrRloc : itrRlocs) {
            subscriberList.add(new Subscriber(itrRloc.getRloc(), srcEid, Subscriber.DEFAULT_SUBSCRIBER_TIMEOUT));
        }
        return subscriberList;
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

    @Override
    public void setSmrNotificationListener(ISmrNotificationListener smrNotificationListener) {
        this.smrNotificationListener = smrNotificationListener;
    }
}
