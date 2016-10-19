/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.ISmrNotificationListener;
import org.opendaylight.lispflowmapping.interfaces.lisp.SmrEvent;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer implements IMapServerAsync, OdlMappingserviceListener, ISmrNotificationListener {

    protected static final Logger LOG = LoggerFactory.getLogger(MapServer.class);
    private static final byte[] ALL_ZEROES_XTR_ID = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0};
    private IMappingService mapService;
    private boolean subscriptionService;
    private IMapNotifyHandler notifyHandler;
    private NotificationService notificationService;
    private ListenerRegistration<MapServer> mapServerListenerRegistration;
    private SmrScheduler scheduler;

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

    @SuppressWarnings("unchecked")
    public void handleMapRegister(MapRegister mapRegister) {
        boolean mappingUpdated = false;
        boolean merge = ConfigIni.getInstance().mappingMergeIsSet() && mapRegister.isMergeEnabled();
        Set<SubscriberRLOC> subscribers = null;
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

            oldMapping = (MappingRecord) mapService.getMapping(MappingOrigin.Southbound, mapping.getEid());
            mapService.addMapping(MappingOrigin.Southbound, mapping.getEid(), getSiteId(mapRegister), mapping, merge);

            if (subscriptionService) {
                MappingRecord newMapping = merge
                        ? (MappingRecord) mapService.getMapping(MappingOrigin.Southbound, mapping.getEid()) : mapping;

                if (mappingChanged(oldMapping, newMapping)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Mapping update occured for {} SMRs will be sent for its subscribers.",
                                LispAddressStringifier.getString(mapping.getEid()));
                    }
                    subscribers = getSubscribers(mapping.getEid());
                    sendSmrs(mapping, subscribers);
                    mappingUpdated = true;
                }
            }
        }
        if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
            LOG.trace("MapRegister wants MapNotify");
            MapNotifyBuilder builder = new MapNotifyBuilder();
            List<TransportAddress> rlocs = null;
            if (merge) {
                Set<IpAddressBinary> notifyRlocs = new HashSet<IpAddressBinary>();
                List<MappingRecordItem> mergedMappings = new ArrayList<MappingRecordItem>();
                for (MappingRecordItem record : mapRegister.getMappingRecordItem()) {
                    MappingRecord mapping = record.getMappingRecord();
                    MappingRecord currentRecord = (MappingRecord) mapService.getMapping(MappingOrigin.Southbound,
                            mapping.getEid());
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
        List<TransportAddress> rlocs = new ArrayList<TransportAddress>();
        for (IpAddressBinary address : addresses) {
            TransportAddressBuilder tab = new TransportAddressBuilder();
            tab.setIpAddress(address);
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
            rlocs.add(tab.build());
        }
        return rlocs;
    }

    private SiteId getSiteId(MapRegister mapRegister) {
        return (mapRegister.getSiteId() != null) ? new SiteId(mapRegister.getSiteId()) : null;
    }

    @Override
    public void onMappingChanged(MappingChanged notification) {
        if (subscriptionService) {
            if (mapService.isMaster()) {
                sendSmrs(notification.getMappingRecord(), getSubscribers(notification.getMappingRecord().getEid()));
            }
            if (notification.getChangeType().equals(MappingChange.Removed)) {
                removeSubscribers(notification.getMappingRecord().getEid());
            }
        }
    }

    private static boolean mappingChanged(MappingRecord oldMapping, MappingRecord newMapping) {
        // We only check for fields we care about
        // XXX: This code needs to be checked and updated when the YANG model is modified
        Preconditions.checkNotNull(newMapping, "The new mapping should never be null");
        if (oldMapping == null) {
            LOG.trace("mappingChanged(): old mapping is null");
            return true;
        } else if (!Objects.equals(oldMapping.getEid(), newMapping.getEid())) {
            LOG.trace("mappingChanged(): EID");
            return true;
        } else if (!Objects.equals(oldMapping.getLocatorRecord(), newMapping.getLocatorRecord())) {
            LOG.trace("mappingChanged(): RLOC");
            return true;
        } else if (!Objects.equals(oldMapping.getAction(), newMapping.getAction())) {
            LOG.trace("mappingChanged(): action");
            return true;
        } else if (!Objects.equals(oldMapping.getRecordTtl(), newMapping.getRecordTtl())) {
            LOG.trace("mappingChanged(): TTL");
            return true;
        } else if (!Objects.equals(oldMapping.getMapVersion(), newMapping.getMapVersion())) {
            LOG.trace("mappingChanged(): mapping version");
            return true;
        }
        return false;
    }

    private void sendSmrs(MappingRecord record, Set<SubscriberRLOC> subscribers) {
        Eid eid = record.getEid();
        handleSmr(eid, subscribers);

        // For SrcDst LCAF also send SMRs to Dst prefix
        if (eid.getAddress() instanceof SourceDestKey) {
            Eid dstAddr = SourceDestKeyHelper.getDstBinary(eid);
            Set<SubscriberRLOC> dstSubs = getSubscribers(dstAddr);
            MappingRecord newRecord = new MappingRecordBuilder(record).setEid(dstAddr).build();
            handleSmr(newRecord.getEid(), dstSubs);
        }
    }

    private void handleSmr(Eid eid, Set<SubscriberRLOC> subscribers) {
        if (subscribers == null) {
            return;
        }
        final MapRequestBuilder mrb = MapRequestUtil.prepareSMR(eid, LispAddressUtil.toRloc(getLocalAddress()));
        LOG.trace("Built SMR packet: " + mrb.build().toString());

        scheduler.scheduleSmrs(mrb, subscribers.iterator());
        addSubscribers(eid, subscribers);
    }

    @SuppressWarnings("unchecked")
    private Set<SubscriberRLOC> getSubscribers(Eid address) {
        Set<SubscriberRLOC> subscriberSet = (Set<SubscriberRLOC>) mapService
                .getData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS);

        if (subscriberSet == null) {
            final Eid parentPrefix = findParentPrefix(address);
            final MappingRecord mapping = (MappingRecord) mapService.getMapping(parentPrefix);
            
            // if mapping is negative, get subscribers for that mapping
            if (mapping.getLocatorRecord() == null || mapping.getLocatorRecord().isEmpty()) {
                subscriberSet = (Set<SubscriberRLOC>) mapService
                        .getData(MappingOrigin.Southbound, parentPrefix, SubKeys.SUBSCRIBERS);
            }
        }
        return subscriberSet;
    }

    private Eid findParentPrefix(Eid eid) {
       return mapService.getParentPrefix(eid);
    }

    private void removeSubscribers(Eid address) {
        mapService.removeData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS);
    }

    private void addSubscribers(Eid address, Set<SubscriberRLOC> subscribers) {
        mapService.addData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS, subscribers);
    }

    private static InetAddress getLocalAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                LOG.debug("Interface " + current.toString());
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
        private final Map<IpAddressBinary, Map<Eid, ScheduledFuture<?>>> subscriberFutureMap = Maps.newConcurrentMap();

        void scheduleSmrs(MapRequestBuilder mrb, Iterator<SubscriberRLOC> subscribers) {
            // Using Iterator ensures that we don't get a ConcurrentModificationException when removing a SubscriberRLOC
            // from a Set.
            while (subscribers.hasNext()) {
                SubscriberRLOC subscriber = subscribers.next();
                if (subscriber.timedOut()) {
                    LOG.trace("Lazy removing expired subscriber entry " + subscriber.toString());
                    subscribers.remove();
                } else {
                    final ScheduledFuture<?> future = executor.scheduleAtFixedRate(new CancellableRunnable(
                            mrb, subscriber), 0L, ConfigIni.getInstance().getSmrTimeout(), TimeUnit.MILLISECONDS);
                    final IpAddressBinary subscriberAddress = LispAddressUtil
                            .addressBinaryFromAddress(subscriber.getSrcRloc().getAddress());

                    if (subscriberFutureMap.containsKey(subscriberAddress)) {
                        subscriberFutureMap.get(subscriberAddress).put(mrb.getSourceEid().getEid(), future);
                    } else {
                        final Map<Eid, ScheduledFuture<?>> eidFutureMap = Maps.newConcurrentMap();
                        eidFutureMap.put(mrb.getSourceEid().getEid(), future);
                        subscriberFutureMap.put(subscriberAddress, eidFutureMap);
                    }
                }
            }
        }

        void smrReceived(SmrEvent event) {
            final List<IpAddressBinary> subscriberAddressList = event.getSubscriberAddressList();
            for (IpAddressBinary subscriberAddress : subscriberAddressList) {
                final Map<Eid, ScheduledFuture<?>> eidFutureMap = subscriberFutureMap.get(subscriberAddress);
                if (eidFutureMap != null) {
                    final ScheduledFuture future = eidFutureMap.get(event.getEid());
                    if (future != null && !future.isCancelled()) {
                        future.cancel(false);
                        LOG.trace("SMR-invoked MapRequest received, scheduled task for subscriber {} with nonce {} has "
                                + "been canceled", subscriberAddress.toString(), event.getNonce());
                        eidFutureMap.remove(event.getEid());
                    }
                    if (eidFutureMap.isEmpty()) {
                        subscriberFutureMap.remove(subscriberAddress);
                    }
                }
            }
        }

        private final class CancellableRunnable implements Runnable {
            private MapRequestBuilder mrb;
            private SubscriberRLOC subscriber;
            private int executionCount = 1;

            CancellableRunnable(MapRequestBuilder mrb, SubscriberRLOC subscriber) {
                this.mrb = mrb;
                this.subscriber = subscriber;
            }

            @SuppressWarnings("checkstyle:IllegalCatch")
            @Override
            public void run() {
                final IpAddressBinary subscriberAddress = LispAddressUtil
                        .addressBinaryFromAddress(subscriber.getSrcRloc().getAddress());
                try {
                    // The address stored in the SMR's EID record is used as Source EID in the SMR-invoked
                    // Map-Request. To ensure consistent behavior it is set to the value used to originally request
                    // a given mapping.
                    if (executionCount <= ConfigIni.getInstance().getSmrRetryCount()) {
                        mrb.setEidItem(new ArrayList<EidItem>());
                        mrb.getEidItem().add(new EidItemBuilder().setEid(subscriber.getSrcEid()).build());
                        notifyHandler.handleSMR(mrb.build(), subscriber.getSrcRloc());
                        LOG.trace("{}. attempt to send SMR for MapRequest " + subscriber.getSrcEid(), executionCount);
                    } else {
                        LOG.trace("Cancelling execution of a SMR Map-Request after {} failed attempts.",
                                executionCount - 1);
                        cancelAndRemove(subscriberAddress);
                    }
                } catch (Exception e) {
                    LOG.error("Errors encountered while handling SMR:", e);
                    cancelAndRemove(subscriberAddress);
                }
                executionCount++;
            }

            private void cancelAndRemove(IpAddressBinary subscriberAddress) {
                final Map<Eid, ScheduledFuture<?>> eidFutureMap = subscriberFutureMap.get(subscriberAddress);
                final Eid eid = mrb.getSourceEid().getEid();
                if (eidFutureMap.containsKey(eid)) {
                    eidFutureMap.get(eid).cancel(false);
                }
                eidFutureMap.remove(eid);
                if (eidFutureMap.isEmpty()) {
                    subscriberFutureMap.remove(subscriberAddress);
                }
            }
        }
    }
}
