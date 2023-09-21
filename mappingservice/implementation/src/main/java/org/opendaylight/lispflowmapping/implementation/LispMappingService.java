/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.lisp.MapResolver;
import org.opendaylight.lispflowmapping.implementation.lisp.MapServer;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.ISmrNotificationListener;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MappingKeepAlive;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.RequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrReplyMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrRequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.MapRegisterCacheMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.map.register.cache.metadata.EidLispAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.OdlLispSbService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapNotifyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRequestInputBuilder;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = {IFlowMapping.class, IMapRequestResultHandler.class, IMapNotifyHandler.class},
        immediate = true, property = "type=default")
public class LispMappingService implements IFlowMapping, IMapRequestResultHandler,
        IMapNotifyHandler, AutoCloseable, ClusterSingletonService {
    private static final String LISPFLOWMAPPING_ENTITY_NAME = "lispflowmapping";
    private static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create(
            LISPFLOWMAPPING_ENTITY_NAME);

    private static final Logger LOG = LoggerFactory.getLogger(LispMappingService.class);

    private volatile boolean smr = ConfigIni.getInstance().smrIsSet();
    private volatile String elpPolicy = ConfigIni.getInstance().getElpPolicy();

    private ThreadLocal<MapReply> tlsMapReply = new ThreadLocal<>();
    private ThreadLocal<Pair<MapNotify, List<TransportAddress>>> tlsMapNotify = new ThreadLocal<>();
    private ThreadLocal<Pair<MapRequest, TransportAddress>> tlsMapRequest = new ThreadLocal<>();

    private IMapResolverAsync mapResolver;
    private IMapServerAsync mapServer;

    private final IMappingService mapService;
    private final OdlLispSbService lispSB;
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final NotificationService notificationService;
    private final Registration rpcRegistration;
    private final Registration listenerRegistration;

    @Inject
    @Activate
    public LispMappingService(@Reference final IMappingService mappingService,
            @Reference final OdlLispSbService odlLispService,
            @Reference final ClusterSingletonServiceProvider clusterSingletonService,
            @Reference final RpcProviderService rpcProviderService,
            @Reference final NotificationService notificationService) {
        this.mapService = mappingService;
        this.lispSB = odlLispService;
        this.clusterSingletonService = clusterSingletonService;
        this.notificationService = notificationService;

        // initialize
        listenerRegistration = notificationService.registerCompositeListener(new CompositeListener(Set.of(
                new CompositeListener.Component<>(AddMapping.class, this::onAddMapping),
                new CompositeListener.Component<>(GotMapNotify.class, this::onGotMapNotify),
                new CompositeListener.Component<>(RequestMapping.class, this::onRequestMapping),
                new CompositeListener.Component<>(GotMapReply.class, this::onGotMapReply),
                new CompositeListener.Component<>(XtrRequestMapping.class, this::onXtrRequestMapping),
                new CompositeListener.Component<>(XtrReplyMapping.class, this::onXtrReplyMapping),
                new CompositeListener.Component<>(MappingKeepAlive.class, this::onMappingKeepAlive))));
        rpcRegistration = rpcProviderService.registerRpcImplementation(OdlLispSbService.class, lispSB);

        mapResolver = new MapResolver(mapService, smr, elpPolicy, this);
        mapServer = new MapServer(mapService, smr, this, notificationService);
        clusterSingletonService.registerClusterSingletonService(this);
        mapResolver.setSmrNotificationListener((ISmrNotificationListener) mapServer);
        LOG.info("LISP (RFC6830) Mapping Service initialized");
    }

    public boolean shouldUseSmr() {
        return this.smr;
    }

    @Override
    public void setShouldUseSmr(boolean shouldUseSmr) {
        this.smr = shouldUseSmr;
        if (mapServer != null) {
            mapServer.setSubscriptionService(shouldUseSmr);
        }
        if (mapResolver != null) {
            mapResolver.setSubscriptionService(shouldUseSmr);
        }
        ConfigIni.getInstance().setSmr(shouldUseSmr);
    }

    public NotificationService getNotificationService() {
        return this.notificationService;
    }

    @Override
    public MapReply handleMapRequest(MapRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LISP: Retrieving mapping for {}",
                    LispAddressStringifier.getString(request.getEidItem().get(0).getEid()));
        }

        tlsMapReply.set(null);
        tlsMapRequest.set(null);
        mapResolver.handleMapRequest(request);
        // After this invocation we assume that the thread local is filled with the reply
        if (tlsMapRequest.get() != null) {
            SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder();
            smrib.setMapRequest(new MapRequestBuilder(tlsMapRequest.get().getLeft()).build());
            smrib.setTransportAddress(tlsMapRequest.get().getRight());
            getLispSB().sendMapRequest(smrib.build());
            return null;
        } else {
            return tlsMapReply.get();
        }
    }

    @Override
    public Pair<MapNotify, List<TransportAddress>> handleMapRegister(MapRegister mapRegister) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LISP: Adding mapping for {}",
                    LispAddressStringifier.getString(mapRegister.getMappingRecordItem().get(0)
                            .getMappingRecord().getEid()));
        }

        tlsMapNotify.set(null);
        mapServer.handleMapRegister(mapRegister);
        // After this invocation we assume that the thread local is filled with the reply
        return tlsMapNotify.get();
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.mapResolver.setShouldAuthenticate(shouldAuthenticate);
    }

    private void sendMapNotify(MapNotify mapNotify, TransportAddress address) {
        SendMapNotifyInputBuilder smnib = new SendMapNotifyInputBuilder();
        smnib.setMapNotify(new MapNotifyBuilder(mapNotify).build());
        smnib.setTransportAddress(address);
        getLispSB().sendMapNotify(smnib.build());
    }

    @VisibleForTesting
    void onAddMapping(AddMapping mapRegisterNotification) {
        Pair<MapNotify, List<TransportAddress>> result = handleMapRegister(mapRegisterNotification.getMapRegister());
        if (result != null && result.getLeft() != null) {
            MapNotify mapNotify = result.getLeft();
            List<TransportAddress> rlocs = result.getRight();
            if (rlocs == null) {
                TransportAddressBuilder tab = new TransportAddressBuilder();
                tab.setIpAddress(mapRegisterNotification.getTransportAddress().getIpAddress());
                tab.setPort(new PortNumber(LispMessage.PORT_NUMBER));
                sendMapNotify(mapNotify, tab.build());
            } else {
                for (TransportAddress ta : rlocs) {
                    sendMapNotify(mapNotify, ta);
                }
            }
        } else {
            LOG.debug("Not sending Map-Notify");
        }
    }

    @VisibleForTesting
    void onRequestMapping(RequestMapping mapRequestNotification) {
        MapReply mapReply = handleMapRequest(mapRequestNotification.getMapRequest());
        if (mapReply != null) {
            SendMapReplyInputBuilder smrib = new SendMapReplyInputBuilder();
            smrib.setMapReply(new MapReplyBuilder(mapReply).build());
            smrib.setTransportAddress(mapRequestNotification.getTransportAddress());
            getLispSB().sendMapReply(smrib.build());
        } else {
            LOG.debug("handleMapRequest: Got null MapReply");
        }
    }

    @VisibleForTesting
    void onGotMapReply(GotMapReply notification) {
        LOG.debug("Received GotMapReply notification, ignoring");
    }

    @VisibleForTesting
    void onGotMapNotify(GotMapNotify notification) {
        LOG.debug("Received GotMapNotify notification, ignoring");
    }

    @VisibleForTesting
    void onXtrRequestMapping(XtrRequestMapping notification) {
        LOG.debug("Received XtrRequestMapping notification, ignoring");
    }

    @VisibleForTesting
    void onXtrReplyMapping(XtrReplyMapping notification) {
        LOG.debug("Received XtrReplyMapping notification, ignoring");
    }

    @VisibleForTesting
    void onMappingKeepAlive(MappingKeepAlive notification) {
        final MapRegisterCacheMetadata cacheMetadata = notification.getMapRegisterCacheMetadata();
        for (EidLispAddress eidLispAddress : cacheMetadata.nonnullEidLispAddress().values()) {
            final Eid eid = eidLispAddress.getEid();
            final XtrId xtrId = cacheMetadata.getXtrId();
            final Long timestamp = cacheMetadata.getTimestamp();
            LOG.debug("Update map registration for eid {} with timestamp {}", LispAddressStringifier.getString(eid),
                    timestamp);
            mapService.refreshMappingRegistration(eid, xtrId, timestamp);
        }
    }

    private OdlLispSbService getLispSB() {
        return lispSB;
    }

    @Override
    public void handleMapReply(MapReply reply) {
        tlsMapReply.set(reply);
    }

    @Override
    public void handleMapNotify(MapNotify notify, List<TransportAddress> rlocs) {
        tlsMapNotify.set(new MutablePair<>(notify, rlocs));
    }

    @Override
    public void handleSMR(MapRequest smrMapRequest, Rloc subscriber) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending SMR Map-Request to {} with Source-EID {} and EID Record {} (reversed)",
                    LispAddressStringifier.getString(subscriber),
                    LispAddressStringifier.getString(smrMapRequest.getSourceEid().getEid()),
                    LispAddressStringifier.getString(smrMapRequest.getEidItem().get(0).getEid()));
        }
        SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder();
        smrib.setMapRequest(new MapRequestBuilder(smrMapRequest).build());
        smrib.setTransportAddress(LispNotificationHelper.getTransportAddressFromRloc(subscriber));
        getLispSB().sendMapRequest(smrib.build());

    }

    @Override
    public void handleNonProxyMapRequest(MapRequest mapRequest, TransportAddress transportAddress) {
        tlsMapRequest.set(new MutablePair<>(mapRequest, transportAddress));
    }

    private void destroy() {
        LOG.info("LISP (RFC6830) Mapping Service is destroyed!");
        mapResolver = null;
        mapServer = null;
    }

    @Deactivate
    @PreDestroy
    @Override
    public void close() throws Exception {
        destroy();
        clusterSingletonService.close();
        rpcRegistration.close();
        listenerRegistration.close();
    }

    @Override
    public void instantiateServiceInstance() {
        mapService.setIsMaster(true);
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        if (mapService != null) {
            mapService.setIsMaster(false);
        }
        return Futures.<Void>immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SERVICE_GROUP_IDENTIFIER;
    }
}
