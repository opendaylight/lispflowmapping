/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.net.InetAddress;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKey;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceNoMaskKey;
import org.opendaylight.lispflowmapping.implementation.inventory.IAdSalLispInventoryService;
import org.opendaylight.lispflowmapping.implementation.lisp.MapResolver;
import org.opendaylight.lispflowmapping.implementation.lisp.MapServer;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.AddMapping;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispflowmappingService;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.RequestMapping;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.SendMapNotifyInputBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.SendMapReplyInputBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.SendMapRequestInputBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.transportaddress.TransportAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMappingService implements CommandProvider, IFlowMapping, BindingAwareConsumer, //
        IMapRequestResultHandler, IMapNotifyHandler {
    protected static final Logger logger = LoggerFactory.getLogger(LispMappingService.class);

    private static final ConfigIni configIni = new ConfigIni();
    private ILispDAO lispDao = null;
    private IMapResolverAsync mapResolver;
    private IMapServerAsync mapServer;
    private volatile boolean shouldIterateMask;
    private volatile boolean shouldAuthenticate;
    private volatile boolean smr = configIni.smrIsSet();
    private ThreadLocal<MapReply> tlsMapReply = new ThreadLocal<MapReply>();
    private ThreadLocal<MapNotify> tlsMapNotify = new ThreadLocal<MapNotify>();
    private ThreadLocal<Pair<MapRequest, TransportAddress>> tlsMapRequest = new ThreadLocal<Pair<MapRequest, TransportAddress>>();

    private LispflowmappingService lispSB = null;

    private ConsumerContext session;

    private NotificationService notificationService;
    private IAdSalLispInventoryService inventoryService;

    class LispIpv4AddressInMemoryConverter implements ILispTypeConverter<Ipv4Address, Integer> {
    }

    class LispIpv6AddressInMemoryConverter implements ILispTypeConverter<Ipv6Address, Integer> {
    }

    class MappingServiceKeyConvertor implements ILispTypeConverter<MappingServiceKey, Integer> {
    }

    class MappingServiceNoMaskKeyConvertor implements ILispTypeConverter<MappingServiceNoMaskKey, Integer> {
    }

    public IAdSalLispInventoryService getInventoryService() {
        return inventoryService;
    }

    public void setInventoryService(IAdSalLispInventoryService inventoryService) {
        logger.debug("Setting inventoryService");
        this.inventoryService = inventoryService;
    }

    public void unsetInventoryService(IAdSalLispInventoryService inventoryService) {
        logger.debug("Unsetting inventoryService");
        if (this.inventoryService == inventoryService) {
            this.inventoryService = null;
        }
    }

    void setBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {
        logger.debug("BindingAwareBroker set!");
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bindingAwareBroker.registerConsumer(this, bundleContext);
    }

    void unsetBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {
        logger.debug("BindingAwareBroker was unset in LispMappingService");
    }

    public void basicInit(ILispDAO dao) {
        lispDao = dao;
        mapResolver = new MapResolver(dao);
        mapServer = new MapServer(dao);
    }

    void setLispDao(ILispDAO dao) {
        logger.debug("LispDAO set in LispMappingService");
        basicInit(dao);
    }

    void unsetLispDao(ILispDAO dao) {
        logger.debug("LispDAO was unset in LispMappingService");
        mapServer = null;
        mapResolver = null;
        lispDao = null;
    }

    public void init() {
        try {
            registerWithOSGIConsole();
            logger.info("LISP (RFC6830) Mapping Service init finished");
        } catch (Throwable t) {
            logger.error(t.getStackTrace().toString());
        }
    }

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }

    public void destroy() {
        logger.info("LISP (RFC6830) Mapping Service is destroyed!");
        mapResolver = null;
        mapServer = null;
    }

    public void _removeEid(final CommandInterpreter ci) {
        lispDao.remove(LispAFIConvertor.asIPAfiAddress(ci.nextArgument()));
    }

    public void _dumpAll(final CommandInterpreter ci) {
        ci.println("EID\tRLOCs");
        lispDao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    ci.println();
                    ci.print(key + "\t");
                }
                ci.print(valueKey + "=" + value + "\t");
                lastKey = key;
            }
        });
        ci.println();
        return;
    }

    public void _setShouldOverwriteRlocs(final CommandInterpreter ci) {
        try {
            boolean shouldOverwriteRloc = Boolean.parseBoolean(ci.nextArgument());
            setOverwrite(shouldOverwriteRloc);
        } catch (Exception e) {
            ci.println("Bad Usage!!");
        }

    }

    public void _addDefaultPassword(final CommandInterpreter ci) {
        LispAddressContainerBuilder builder = new LispAddressContainerBuilder();
        builder.setAddress((Address) (new Ipv4Builder().setIpv4Address(new Ipv4Address("0.0.0.0")).build()));
        addAuthenticationKey(builder.build(), 0, "password");
    }

    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---LISP Mapping Service---\n");
        help.append("\t dumpAll        - Dump all current EID -> RLOC mapping\n");
        help.append("\t removeEid      - Remove a single LispIPv4Address Eid\n");
        help.append("\t setShouldOverwritingRloc(true/false)      - set the map server's behaivior regarding existing RLOCs\n");
        return help.toString();
    }

    public MapReply handleMapRequest(MapRequest request) {
        return handleMapRequest(request, smr);
    }

    public MapReply handleMapRequest(MapRequest request, boolean smr) {
        tlsMapReply.set(null);
        tlsMapRequest.set(null);
        mapResolver.handleMapRequest(request, smr, this);
        // After this invocation we assume that the thread local is filled with
        // the reply
        if (tlsMapRequest.get() != null) {
            SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder();
            new MapRequestBuilder(tlsMapRequest.get().getLeft());
            smrib.setMapRequest(new MapRequestBuilder(tlsMapRequest.get().getLeft()).build());
            smrib.setTransportAddress(tlsMapRequest.get().getRight());
            getLispSB().sendMapRequest(smrib.build());
            return null;
        } else {
            return tlsMapReply.get();
        }

    }

    public MapNotify handleMapRegister(MapRegister mapRegister) {
        return handleMapRegister(mapRegister, smr);
    }

    public MapNotify handleMapRegister(MapRegister mapRegister, boolean smr) {
        tlsMapNotify.set(null);
        mapServer.handleMapRegister(mapRegister, smr, this);
        // After this invocation we assume that the thread local is filled with
        // the reply
        return tlsMapNotify.get();
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        return mapServer.getAuthenticationKey(address, maskLen);
    }

    public void removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        mapServer.removeAuthenticationKey(address, maskLen);
    }

    public void addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        mapServer.addAuthenticationKey(address, maskLen, key);
    }

    public boolean shouldIterateMask() {
        return this.shouldIterateMask;
    }

    public boolean shouldUseSmr() {
        return this.smr;
    }

    public void setShouldUseSmr(boolean smr) {
        this.smr = smr;
    }

    public void setShouldIterateMask(boolean shouldIterateMask) {
        this.shouldIterateMask = shouldIterateMask;
        this.mapResolver.setShouldIterateMask(shouldIterateMask);
        this.mapServer.setShouldIterateMask(shouldIterateMask);
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.shouldAuthenticate = shouldAuthenticate;
        this.mapResolver.setShouldAuthenticate(shouldAuthenticate);
        this.mapServer.setShouldAuthenticate(shouldAuthenticate);
    }

    public boolean shouldAuthenticate() {
        return shouldAuthenticate;
    }

    public void onSessionInitialized(ConsumerContext session) {
        logger.info("Lisp Consumer session initialized!");
        notificationService = session.getSALService(NotificationService.class);
        registerNotificationListener(AddMapping.class, new MapRegisterNotificationHandler());
        registerNotificationListener(RequestMapping.class, new MapRequestNotificationHandler());
        this.session = session;
    }

    public <T extends Notification> void registerNotificationListener(Class<T> notificationType, NotificationListener<T> listener) {
        notificationService.registerNotificationListener(notificationType, listener);
    }

    private class MapRegisterNotificationHandler implements NotificationListener<AddMapping> {

        @Override
        public void onNotification(AddMapping mapRegisterNotification) {
            MapRegister mapRegister = mapRegisterNotification.getMapRegister();
            InetAddress address = LispNotificationHelper.getInetAddressFromIpAddress(mapRegisterNotification.getTransportAddress().getIpAddress());
            if (mapRegister.isProxyMapReply()) {
                byte[] xtrId = mapRegister.getXtrId();
                inventoryService.addNode(address, xtrId);
            } else {
                inventoryService.addNode(address, null);
            }

            MapNotify mapNotify = handleMapRegister(mapRegister, smr);
            if (mapNotify != null) {
                TransportAddressBuilder tab = new TransportAddressBuilder();
                tab.setIpAddress(mapRegisterNotification.getTransportAddress().getIpAddress());
                tab.setPort(new PortNumber(LispMessage.PORT_NUM));
                SendMapNotifyInputBuilder smnib = new SendMapNotifyInputBuilder();
                smnib.setMapNotify(new MapNotifyBuilder(mapNotify).build());
                smnib.setTransportAddress(tab.build());
                getLispSB().sendMapNotify(smnib.build());
            } else {
                logger.warn("got null map notify");
            }

        }
    }

    private class MapRequestNotificationHandler implements NotificationListener<RequestMapping> {

        @Override
        public void onNotification(RequestMapping mapRequestNotification) {
            MapReply mapReply = handleMapRequest(mapRequestNotification.getMapRequest());
            if (mapReply != null) {
                SendMapReplyInputBuilder smrib = new SendMapReplyInputBuilder();
                smrib.setMapReply((new MapReplyBuilder(mapReply).build()));
                smrib.setTransportAddress(mapRequestNotification.getTransportAddress());
                getLispSB().sendMapReply(smrib.build());
            } else {
                logger.warn("got null map reply");
            }
        }
    }

    private LispflowmappingService getLispSB() {
        if (lispSB == null) {
            lispSB = session.getRpcService(LispflowmappingService.class);
        }
        return lispSB;
    }

    public void handleMapReply(MapReply reply) {
        tlsMapReply.set(reply);
    }

    public void handleMapNotify(MapNotify notify) {
        tlsMapNotify.set(notify);
    }

    public void handleSMR(MapRequest smr, LispAddressContainer subscriber) {
        logger.debug("Sending SMR to " + subscriber.toString());
        SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder();
        smrib.setMapRequest(new MapRequestBuilder(smr).build());
        smrib.setTransportAddress(LispNotificationHelper.getTransportAddressFromContainer(subscriber));
        getLispSB().sendMapRequest(smrib.build());

    }

    @Override
    public void handleNonProxyMapRequest(MapRequest mapRequest, TransportAddress transportAddress) {
        tlsMapRequest.set(new MutablePair<MapRequest, TransportAddress>(mapRequest, transportAddress));
    }

    @Override
    public void clean() {
        lispDao.removeAll();
    }

    @Override
    public boolean shouldOverwrite() {
        return mapServer.shouldOverwrite();
    }

    @Override
    public void setOverwrite(boolean overwrite) {
        mapServer.setOverwrite(overwrite);
    }

}
