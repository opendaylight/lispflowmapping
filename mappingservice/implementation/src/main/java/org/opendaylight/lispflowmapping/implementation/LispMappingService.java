/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.lisp.MapResolver;
import org.opendaylight.lispflowmapping.implementation.lisp.MapServer;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.OdlLispProtoListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.RequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrReplyMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrRequestMapping;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMappingService implements IFlowMapping, BindingAwareProvider, IMapRequestResultHandler,
        IMapNotifyHandler, OdlLispProtoListener, AutoCloseable {
    protected static final Logger LOG = LoggerFactory.getLogger(LispMappingService.class);

    private volatile boolean shouldAuthenticate = true;

    private volatile boolean smr = ConfigIni.getInstance().smrIsSet();
    private volatile String elpPolicy = ConfigIni.getInstance().getElpPolicy();

    private ThreadLocal<MapReply> tlsMapReply = new ThreadLocal<MapReply>();
    private ThreadLocal<Pair<MapNotify, List<TransportAddress>>> tlsMapNotify =
            new ThreadLocal<Pair<MapNotify, List<TransportAddress>>>();
    private ThreadLocal<Pair<MapRequest, TransportAddress>> tlsMapRequest =
            new ThreadLocal<Pair<MapRequest, TransportAddress>>();

    private OdlLispSbService lispSB = null;
    private IMapResolverAsync mapResolver;
    private IMapServerAsync mapServer;

    private IMappingService mapService;
    private NotificationService notificationService;
    private BindingAwareBroker broker;
    private ProviderContext session;

    public LispMappingService() {
        LOG.debug("LispMappingService Module constructed!");
    }

    public void setBindingAwareBroker(BindingAwareBroker broker) {
        this.broker = broker;
    }

    public void setNotificationService(NotificationService ns) {
        this.notificationService = ns;
    }

    public void setMappingService(IMappingService ms) {
        LOG.trace("MappingService set in LispMappingService");
        this.mapService = ms;
    }

    public boolean shouldUseSmr() {
        return this.smr;
    }

    public void setShouldUseSmr(boolean smr) {
        this.smr = smr;
        if (mapServer != null) {
            mapServer.setSubscriptionService(smr);
        }
        if (mapResolver != null) {
            mapResolver.setSubscriptionService(smr);
        }
        ConfigIni.getInstance().setSmr(smr);
    }

    public NotificationService getNotificationService() {
        return this.notificationService;
    }

    public void initialize() {
        broker.registerProvider(this);
        notificationService.registerNotificationListener(this);
        mapResolver = new MapResolver(mapService, smr, elpPolicy, this);
        mapServer = new MapServer(mapService, shouldAuthenticate, smr, this, notificationService);
    }

    public void basicInit() {
        mapResolver = new MapResolver(mapService, smr, elpPolicy, this);
        mapServer = new MapServer(mapService, shouldAuthenticate, smr, this, notificationService);
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("Lisp Consumer session initialized!");
        this.session = session;
        LOG.info("LISP (RFC6830) Mapping Service init finished");
    }

    public MapReply handleMapRequest(MapRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("DAO: Retrieving mapping for {}",
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

    public Pair<MapNotify, List<TransportAddress>> handleMapRegister(MapRegister mapRegister) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("DAO: Adding mapping for {}",
                    LispAddressStringifier.getString(mapRegister.getMappingRecordItem().get(0)
                            .getMappingRecord().getEid()));
        }

        tlsMapNotify.set(null);
        mapServer.handleMapRegister(mapRegister);
        // After this invocation we assume that the thread local is filled with the reply
        return tlsMapNotify.get();
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.shouldAuthenticate = shouldAuthenticate;
        this.mapResolver.setShouldAuthenticate(shouldAuthenticate);
        this.mapServer.setShouldAuthenticate(shouldAuthenticate);
    }

    public boolean shouldAuthenticate() {
        return shouldAuthenticate;
    }

    private void sendMapNotify(MapNotify mapNotify, TransportAddress address) {
        SendMapNotifyInputBuilder smnib = new SendMapNotifyInputBuilder();
        smnib.setMapNotify(new MapNotifyBuilder(mapNotify).build());
        smnib.setTransportAddress(address);
        getLispSB().sendMapNotify(smnib.build());
    }

    @Override
    public void onAddMapping(AddMapping mapRegisterNotification) {
        Pair<MapNotify, List<TransportAddress>> result = handleMapRegister(mapRegisterNotification.getMapRegister());
        if (result != null && result.getLeft() != null) {
            MapNotify mapNotify = result.getLeft();
            List <TransportAddress> rlocs = result.getRight();
            if (rlocs == null) {
                TransportAddressBuilder tab = new TransportAddressBuilder();
                tab.setIpAddress(mapRegisterNotification.getTransportAddress().getIpAddress());
                tab.setPort(new PortNumber(LispMessage.PORT_NUM));
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

    @Override
    public void onRequestMapping(RequestMapping mapRequestNotification) {
        MapReply mapReply = handleMapRequest(mapRequestNotification.getMapRequest());
        if (mapReply != null) {
            SendMapReplyInputBuilder smrib = new SendMapReplyInputBuilder();
            smrib.setMapReply((new MapReplyBuilder(mapReply).build()));
            smrib.setTransportAddress(mapRequestNotification.getTransportAddress());
            getLispSB().sendMapReply(smrib.build());
        } else {
            LOG.warn("got null map reply");
        }
    }

    @Override
    public void onGotMapReply(GotMapReply notification) {
        LOG.debug("Received GotMapReply notification, ignoring");
    }

    @Override
    public void onGotMapNotify(GotMapNotify notification) {
        LOG.debug("Received GotMapNotify notification, ignoring");
    }

    @Override
    public void onXtrRequestMapping(XtrRequestMapping notification) {
        LOG.debug("Received XtrRequestMapping notification, ignoring");
    }

    @Override
    public void onXtrReplyMapping(XtrReplyMapping notification) {
        LOG.debug("Received XtrReplyMapping notification, ignoring");
    }

    private OdlLispSbService getLispSB() {
        if (lispSB == null) {
            lispSB = session.getRpcService(OdlLispSbService.class);
        }
        return lispSB;
    }

    @Override
    public void handleMapReply(MapReply reply) {
        tlsMapReply.set(reply);
    }

    @Override
    public void handleMapNotify(MapNotify notify, List<TransportAddress> rlocs) {
        tlsMapNotify.set(new MutablePair<MapNotify, List<TransportAddress>>(notify, rlocs));
    }

    @Override
    public void handleSMR(MapRequest smr, Rloc subscriber) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending SMR to {} with Source-EID {} and EID Record {}",
                    LispAddressStringifier.getString(subscriber),
                    LispAddressStringifier.getString(smr.getSourceEid().getEid()),
                    LispAddressStringifier.getString(smr.getEidItem().get(0).getEid()));
        }
        SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder();
        smrib.setMapRequest(new MapRequestBuilder(smr).build());
        smrib.setTransportAddress(LispNotificationHelper.getTransportAddressFromRloc(subscriber));
        getLispSB().sendMapRequest(smrib.build());

    }

    @Override
    public void handleNonProxyMapRequest(MapRequest mapRequest, TransportAddress transportAddress) {
        tlsMapRequest.set(new MutablePair<MapRequest, TransportAddress>(mapRequest, transportAddress));
    }

    public void destroy() {
        LOG.info("LISP (RFC6830) Mapping Service is destroyed!");
        mapResolver = null;
        mapServer = null;
    }

    @Override
    public void close() throws Exception {
        destroy();
    }
}
