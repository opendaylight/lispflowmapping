/*
 * Copyright (c) 2014, 2017 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.util.LoggingUtil;
import org.opendaylight.lispflowmapping.implementation.util.MSNotificationInputUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.ISmrNotificationListener;
import org.opendaylight.lispflowmapping.interfaces.lisp.SmrEvent;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.lisp.util.MappingRecordUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer implements IMapServerAsync, OdlMappingserviceListener, ISmrNotificationListener {

    private static final Logger LOG = LoggerFactory.getLogger(MapServer.class);
    private static final byte[] ALL_ZEROES_XTR_ID = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0};
    private final IMappingService mapService;
    private boolean subscriptionService;
    private final IMapNotifyHandler notifyHandler;
    private final NotificationService notificationService;
    private ListenerRegistration<MapServer> mapServerListenerRegistration;
    private final SmrScheduler scheduler;

    public MapServer(IMappingService mapService, boolean subscriptionService,
                     IMapNotifyHandler notifyHandler, NotificationService notificationService) {
        Preconditions.checkNotNull(mapService);
        this.mapService = mapService;
        this.subscriptionService = subscriptionService;
        this.notifyHandler = notifyHandler;
        this.notificationService = notificationService;
        if (notificationService != null) {
            notificationService.registerNotificationListener(this);
        }
        scheduler = new SmrScheduler();
    }

    @Override
    public void setSubscriptionService(boolean subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMapRegister(MapRegister mapRegister) {
        boolean mappingUpdated = false;
        boolean merge = ConfigIni.getInstance().mappingMergeIsSet() && mapRegister.isMergeEnabled();
        MappingRecord oldMapping;

        if (merge) {
            if (!mapRegister.isXtrSiteIdPresent() || mapRegister.getXtrId() == null) {
                LOG.error("Merge bit is set in Map-Register, but xTR-ID is not present. Will not merge.");
                merge = false;
            } else if (Arrays.equals(mapRegister.getXtrId().getValue(), ALL_ZEROES_XTR_ID)) {
                LOG.warn("Merge bit is set in Map-Register, but xTR-ID is all zeroes.");
            }
        }

        for (MappingRecordItem record : mapRegister.getMappingRecordItem()) {
            MappingRecord mapping = record.getMappingRecord();
            Eid eid = mapping.getEid();
            MappingData mappingData = new MappingData(mapping, System.currentTimeMillis());
            mappingData.setMergeEnabled(merge);
            mappingData.setXtrId(mapRegister.getXtrId());

            oldMapping = getMappingRecord(mapService.getMapping(MappingOrigin.Southbound, eid));
            mapService.addMapping(MappingOrigin.Southbound, eid, getSiteId(mapRegister), mappingData);
            if (merge) {
                MappingRecord newMapping = getMappingRecord(mapService.getMapping(MappingOrigin.Southbound, eid));
                if (MappingRecordUtil.mappingChanged(oldMapping, newMapping)) {
                    // If there is a SB mapping change with merge on, Map-Notify will be sent to ALL xTRs, not jus the
                    // one registering (merging is done in the MappingSystem code)
                    mappingUpdated = true;
                }
            }
        }
        if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
            LOG.trace("MapRegister wants MapNotify");
            MapNotifyBuilder builder = new MapNotifyBuilder();
            List<TransportAddress> rlocs = null;
            if (merge) {
                Set<IpAddressBinary> notifyRlocs = new LinkedHashSet<>();
                List<MappingRecordItem> mergedMappings = new ArrayList<>();
                for (MappingRecordItem record : mapRegister.getMappingRecordItem()) {
                    MappingRecord mapping = record.getMappingRecord();
                    MappingRecord currentRecord = getMappingRecord(mapService.getMapping(MappingOrigin.Southbound,
                            mapping.getEid()));
                    mergedMappings.add(new MappingRecordItemBuilder().setMappingRecord(currentRecord).build());
                    Set<IpAddressBinary> sourceRlocs = (Set<IpAddressBinary>) mapService.getData(
                            MappingOrigin.Southbound, mapping.getEid(), SubKeys.SRC_RLOCS);
                    if (sourceRlocs != null) {
                        notifyRlocs.addAll(sourceRlocs);
                    }
                }
                MapNotifyBuilderHelper.setFromMapRegisterAndMappingRecordItems(builder, mapRegister, mergedMappings);
                // send map-notify to merge group only when mapping record is changed
                if (mappingUpdated) {
                    rlocs = getTransportAddresses(notifyRlocs);
                }
            } else {
                MapNotifyBuilderHelper.setFromMapRegister(builder, mapRegister);
            }
            List<MappingRecordItem> mappings = builder.getMappingRecordItem();
            if (mappings != null && mappings.get(0) != null && mappings.get(0).getMappingRecord() != null
                    && mappings.get(0).getMappingRecord().getEid() != null) {
                MappingAuthkey authkey = mapService.getAuthenticationKey(mappings.get(0).getMappingRecord().getEid());
                if (authkey != null) {
                    builder.setAuthenticationData(LispAuthenticationUtil.createAuthenticationData(builder.build(),
                            authkey.getKeyString()));
                }
            }
            notifyHandler.handleMapNotify(builder.build(), rlocs);
        }
    }

    private static List<TransportAddress> getTransportAddresses(Set<IpAddressBinary> addresses) {
        List<TransportAddress> rlocs = new ArrayList<>();
        for (IpAddressBinary address : addresses) {
            TransportAddressBuilder tab = new TransportAddressBuilder();
            tab.setIpAddress(address);
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
            rlocs.add(tab.build());
        }
        return rlocs;
    }

    private static SiteId getSiteId(MapRegister mapRegister) {
        return mapRegister.getSiteId() != null ? new SiteId(mapRegister.getSiteId()) : null;
    }

    private static MappingRecord getMappingRecord(MappingData mappingData) {
        return mappingData != null ? mappingData.getRecord() : null;
    }

    @Override
    public void onMappingChanged(MappingChanged notification) {
        if (subscriptionService) {
            Eid eid = notification.getEid();
            if (eid == null) {
                eid = notification.getMappingRecord().getEid();
            }
            LOG.trace("MappingChanged event for {} of type: `{}'", LispAddressStringifier.getString(eid),
                    notification.getChangeType());
            Set<Subscriber> subscribers = MSNotificationInputUtil.toSubscriberSet(notification.getSubscriberItem());
            LoggingUtil.logSubscribers(LOG, eid, subscribers);
            if (mapService.isMaster()) {
                sendSmrs(eid, subscribers);
                if (eid.getAddress() instanceof SourceDestKey) {
                    Set<Subscriber> dstSubscribers = MSNotificationInputUtil.toSubscriberSetFromDst(
                            notification.getDstSubscriberItem());
                    LoggingUtil.logSubscribers(LOG, SourceDestKeyHelper.getDstBinary(eid), dstSubscribers);
                    sendSmrs(SourceDestKeyHelper.getDstBinary(eid), dstSubscribers);
                }
            }
        }
    }

    private void handleSmr(Eid eid, Set<Subscriber> subscribers) {
        sendSmrs(eid, subscribers);

        // For SrcDst LCAF also send SMRs to Dst prefix
        if (eid.getAddress() instanceof SourceDestKey) {
            Eid dstAddr = SourceDestKeyHelper.getDstBinary(eid);
            Set<Subscriber> dstSubs = mapService.getSubscribers(dstAddr);
            sendSmrs(dstAddr, dstSubs);
        }
    }

    private void sendSmrs(Eid eid, Set<Subscriber> subscribers) {
        if (subscribers == null) {
            return;
        }
        final MapRequestBuilder mrb = MapRequestUtil.prepareSMR(eid, LispAddressUtil.toRloc(getLocalAddress()));
        LOG.trace("Built SMR packet template (EID field will be set later): " + mrb.build().toString());

        scheduler.scheduleSmrs(mrb, subscribers.iterator());
    }

    private static InetAddress getLocalAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                LOG.trace("Interface " + current.toString());
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress currentAddr = addresses.nextElement();
                    // Skip loopback and link local addresses
                    if (currentAddr.isLoopbackAddress() || currentAddr.isLinkLocalAddress()) {
                        continue;
                    }
                    LOG.debug(currentAddr.getHostAddress());
                    return currentAddr;
                }
            }
        } catch (SocketException se) {
            LOG.debug("Caught socket exception", se);
        }
        return null;
    }

    @Override
    public void onSmrInvokedReceived(SmrEvent event) {
        scheduler.smrReceived(event);
    }

    /**
     * Task scheduler is responsible for resending SMR messages to a subscriber (xTR)
     * {@value ConfigIni#LISP_SMR_RETRY_COUNT} times, or until {@link ISmrNotificationListener#onSmrInvokedReceived}
     * is triggered.
     */
    private class SmrScheduler {
        final int cpuCores = Runtime.getRuntime().availableProcessors();
        private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("smr-executor-%d").build();
        private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(cpuCores * 2, threadFactory);
        private final Map<Eid, Map<Subscriber, ScheduledFuture<?>>> eidFutureMap = Maps.newConcurrentMap();

        void scheduleSmrs(MapRequestBuilder mrb, Iterator<Subscriber> subscribers) {
            final Eid srcEid = fixSrcEidMask(mrb.getSourceEid().getEid());
            cancelExistingFuturesForEid(srcEid);

            final Map<Subscriber, ScheduledFuture<?>> subscriberFutureMap = Maps.newConcurrentMap();

            // Using Iterator ensures that we don't get a ConcurrentModificationException when removing a Subscriber
            // from a Set.
            while (subscribers.hasNext()) {
                Subscriber subscriber = subscribers.next();
                if (subscriber.timedOut()) {
                    LOG.debug("Lazy removing expired subscriber entry " + subscriber.getString());
                    subscribers.remove();
                } else {
                    final ScheduledFuture<?> future = executor.scheduleAtFixedRate(new CancellableRunnable(
                            mrb, subscriber), 0L, ConfigIni.getInstance().getSmrTimeout(), TimeUnit.MILLISECONDS);
                    subscriberFutureMap.put(subscriber, future);
                }
            }

            if (subscriberFutureMap.isEmpty()) {
                return;
            }
            eidFutureMap.put(srcEid, subscriberFutureMap);
        }

        void smrReceived(SmrEvent event) {
            final List<Subscriber> subscriberList = event.getSubscriberList();
            for (Subscriber subscriber : subscriberList) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("SMR-invoked event, EID {}, subscriber {}",
                            LispAddressStringifier.getString(event.getEid()),
                            subscriber.getString());
                    LOG.trace("eidFutureMap: {}", eidFutureMap);
                }
                final Map<Subscriber, ScheduledFuture<?>> subscriberFutureMap = eidFutureMap.get(event.getEid());
                if (subscriberFutureMap != null) {
                    final ScheduledFuture<?> future = subscriberFutureMap.get(subscriber);
                    if (future != null && !future.isCancelled()) {
                        future.cancel(true);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("SMR-invoked MapRequest received, scheduled task for subscriber {}, EID {} with"
                                    + " nonce {} has been cancelled", subscriber.getString(),
                                    LispAddressStringifier.getString(event.getEid()), event.getNonce());
                        }
                        subscriberFutureMap.remove(subscriber);
                    } else {
                        if (future == null) {
                            LOG.trace("No outstanding SMR tasks for EID {}, subscriber {}",
                                    LispAddressStringifier.getString(event.getEid()), subscriber.getString());
                        } else {
                            LOG.trace("Future {} is cancelled", future);
                        }
                    }
                    if (subscriberFutureMap.isEmpty()) {
                        eidFutureMap.remove(event.getEid());
                    }
                } else {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("No outstanding SMR tasks for EID {}",
                                LispAddressStringifier.getString(event.getEid()));
                    }
                }
            }
        }

        private void cancelExistingFuturesForEid(Eid eid) {
            synchronized (eidFutureMap) {
                if (eidFutureMap.containsKey(eid)) {
                    final Map<Subscriber, ScheduledFuture<?>> subscriberFutureMap = eidFutureMap.get(eid);
                    Iterator<Subscriber> oldSubscribers = subscriberFutureMap.keySet().iterator();
                    while (oldSubscribers.hasNext()) {
                        Subscriber subscriber = oldSubscribers.next();
                        ScheduledFuture<?> subscriberFuture = subscriberFutureMap.get(subscriber);
                        subscriberFuture.cancel(true);
                    }
                    eidFutureMap.remove(eid);
                }
            }
        }

        /*
         * See https://bugs.opendaylight.org/show_bug.cgi?id=8469#c1 why this is necessary.
         *
         * TL;DR  The sourceEid field in the MapRequestBuilder object will be serialized to a packet on the wire, and
         * a Map-Request can't store the prefix length in the source EID.
         *
         * Since we store all prefixes as binary internally, we only care about and fix those address types.
         */
        private Eid fixSrcEidMask(Eid eid) {
            Address address = eid.getAddress();
            if (address instanceof Ipv4PrefixBinary) {
                return new EidBuilder(eid).setAddress(new Ipv4PrefixBinaryBuilder((Ipv4PrefixBinary) address)
                        .setIpv4MaskLength(MaskUtil.IPV4_MAX_MASK).build()).build();
            } else if (address instanceof Ipv6PrefixBinary) {
                return new EidBuilder(eid).setAddress(new Ipv6PrefixBinaryBuilder((Ipv6PrefixBinary) address)
                        .setIpv6MaskLength(MaskUtil.IPV6_MAX_MASK).build()).build();
            }
            return eid;
        }

        private final class CancellableRunnable implements Runnable {
            private final MapRequestBuilder mrb;
            private final Subscriber subscriber;
            private int executionCount = 1;

            CancellableRunnable(MapRequestBuilder mrb, Subscriber subscriber) {
                this.mrb = mrb;
                this.subscriber = subscriber;
            }

            @SuppressWarnings("checkstyle:IllegalCatch")
            @Override
            public void run() {
                final Eid srcEid = mrb.getSourceEid().getEid();

                try {
                    // The address stored in the SMR's EID record is used as Source EID in the SMR-invoked
                    // Map-Request. To ensure consistent behavior it is set to the value used to originally request
                    // a given mapping.
                    if (executionCount <= ConfigIni.getInstance().getSmrRetryCount()) {
                        synchronized (mrb) {
                            mrb.setEidItem(new ArrayList<EidItem>());
                            mrb.getEidItem().add(new EidItemBuilder().setEid(subscriber.getSrcEid()).build());
                            notifyHandler.handleSMR(mrb.build(), subscriber.getSrcRloc());
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Attempt #{} to send SMR to subscriber {} for EID {}",
                                        executionCount,
                                        subscriber.getString(),
                                        LispAddressStringifier.getString(mrb.getSourceEid().getEid()));
                            }
                        }
                    } else {
                        LOG.trace("Cancelling execution of a SMR Map-Request after {} failed attempts.",
                                executionCount - 1);
                        cancelAndRemove(subscriber, srcEid);
                        return;
                    }
                } catch (Exception e) {
                    LOG.error("Errors encountered while handling SMR:", e);
                    cancelAndRemove(subscriber, srcEid);
                    return;
                }
                executionCount++;
            }

            private void cancelAndRemove(Subscriber sub, Eid eid) {
                final Map<Subscriber, ScheduledFuture<?>> subscriberFutureMap = eidFutureMap.get(eid);
                if (subscriberFutureMap == null) {
                    LOG.warn("Couldn't find subscriber {} in SMR scheduler internal list", sub);
                    return;
                }

                if (subscriberFutureMap.containsKey(sub)) {
                    ScheduledFuture<?> eidFuture = subscriberFutureMap.get(sub);
                    subscriberFutureMap.remove(sub);
                    eidFuture.cancel(false);
                }
                if (subscriberFutureMap.isEmpty()) {
                    eidFutureMap.remove(eid);
                }
            }
        }
    }
}
