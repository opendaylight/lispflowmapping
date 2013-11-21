/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.lispflowmapping.implementation.dao.InMemoryDAO;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKey;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceNoMaskKey;
import org.opendaylight.lispflowmapping.implementation.lisp.MapResolver;
import org.opendaylight.lispflowmapping.implementation.lisp.MapServer;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.interfaces.dao.IQueryAll;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapReplyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.type.sbplugin.ILispSouthboundPlugin;
import org.opendaylight.lispflowmapping.type.sbplugin.LispNotification;
import org.opendaylight.lispflowmapping.type.sbplugin.MapRegisterNotification;
import org.opendaylight.lispflowmapping.type.sbplugin.MapRequestNotification;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMappingService implements CommandProvider, IFlowMapping, BindingAwareConsumer, //
        NotificationListener<LispNotification>, IMapReplyHandler, IMapNotifyHandler {
    protected static final Logger logger = LoggerFactory.getLogger(LispMappingService.class);

    private ILispDAO lispDao = null;
    private IMapResolverAsync mapResolver;
    private IMapServerAsync mapServer;
    private volatile boolean shouldIterateMask;
    private volatile boolean shouldAuthenticate;
    private ThreadLocal<MapReply> tlsMapReply = new ThreadLocal<MapReply>();
    private ThreadLocal<MapNotify> tlsMapNotify = new ThreadLocal<MapNotify>();

    private ILispSouthboundPlugin lispSB = null;

    private ConsumerContext session;

    public static void main(String[] args) throws Exception {
        LispMappingService serv = new LispMappingService();
        serv.setLispDao(new InMemoryDAO());
        serv.init();
    }

    class LispIpv4AddressInMemoryConverter implements ILispTypeConverter<Ipv4Address, Integer> {
    }

    class LispIpv6AddressInMemoryConverter implements ILispTypeConverter<Ipv6Address, Integer> {
    }

    class MappingServiceKeyConvertor implements ILispTypeConverter<MappingServiceKey, Integer> {
    }

    class MappingServiceNoMaskKeyConvertor implements ILispTypeConverter<MappingServiceNoMaskKey, Integer> {
    }

    void setBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {
        logger.info("BindingAwareBroker set!");
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bindingAwareBroker.registerConsumer(this, bundleContext);
    }

    void unsetBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {
        logger.info("BindingAwareBroker was unset in LispMappingService");
    }

    public void basicInit(ILispDAO dao) {
        lispDao = dao;
        mapResolver = new MapResolver(dao);
        mapServer = new MapServer(dao);
    }

    void setLispDao(ILispDAO dao) {
        logger.info("LispDAO set in LispMappingService");
        basicInit(dao);
        logger.debug("Registering LispIpv4Address");
        lispDao.register(LispIpv4AddressInMemoryConverter.class);
        logger.debug("Registering LispIpv6Address");
        lispDao.register(LispIpv6AddressInMemoryConverter.class);
        logger.debug("Registering MappingServiceKey");
        lispDao.register(MappingServiceKeyConvertor.class);
        logger.debug("Registering MappingServiceNoMaskKey");
        lispDao.register(MappingServiceNoMaskKeyConvertor.class);
    }

    void unsetLispDao(ILispDAO dao) {
        logger.info("LispDAO was unset in LispMappingService");
        mapServer = null;
        mapResolver = null;
        lispDao = null;
    }

    public void init() {
        try {
            registerWithOSGIConsole();
            logger.info("LISP (RFC6830) Mapping Service init finished");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }

    public void destroy() {
        logger.debug("LISP (RFC6830) Mapping Service is destroyed!");
        mapResolver = null;
        mapServer = null;
    }

    public void _removeEid(final CommandInterpreter ci) {
        lispDao.remove(LispAFIConvertor.asIPAfiAddress(ci.nextArgument()));
    }

    public void _dumpAll(final CommandInterpreter ci) {
        ci.println("EID\tRLOCs");
        if (lispDao instanceof IQueryAll) {
            ((IQueryAll) lispDao).getAll(new IRowVisitor() {
                String lastKey = "";

                public void visitRow(Class<?> keyType, Object keyId, String valueKey, Object value) {
                    String key = keyType.getSimpleName() + "#" + keyId;
                    if (!lastKey.equals(key)) {
                        ci.println();
                        ci.print(key + "\t");
                    }
                    ci.print(valueKey + "=" + value + "\t");
                    lastKey = key;
                }
            });
            ci.println();
        } else {
            ci.println("Not implemented by this DAO");
        }
        return;
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
        return help.toString();
    }

    public MapReply handleMapRequest(MapRequest request) {
        tlsMapReply.set(null);
        mapResolver.handleMapRequest(request, this);
        // After this invocation we assume that the thread local is filled with
        // the reply
        return tlsMapReply.get();

    }

    public MapNotify handleMapRegister(MapRegister mapRegister) {
        tlsMapNotify.set(null);
        mapServer.handleMapRegister(mapRegister, this);
        // After this invocation we assume that the thread local is filled with
        // the reply
        return tlsMapNotify.get();
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        return mapServer.getAuthenticationKey(address, maskLen);
    }

    public boolean removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        return mapServer.removeAuthenticationKey(address, maskLen);
    }

    public boolean addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        return mapServer.addAuthenticationKey(address, maskLen, key);
    }

    public boolean shouldIterateMask() {
        return this.shouldIterateMask;
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
        logger.debug("Lisp Consumer session initialized!");
        NotificationService notificationService = session.getSALService(NotificationService.class);
        notificationService.registerNotificationListener(LispNotification.class, this);
        this.session = session;
    }

    public void onNotification(LispNotification notification) {
        try {
            if (notification instanceof MapRegisterNotification) {
                logger.trace("MapRegister notification recieved!");
                MapRegisterNotification mapRegisterNotification = (MapRegisterNotification) notification;
                MapNotify mapNotify = handleMapRegister(mapRegisterNotification.getMapRegister());
                getLispSB().handleMapNotify(mapNotify, mapRegisterNotification.getSourceAddress());
            } else if (notification instanceof MapRequestNotification) {
                logger.trace("MapRequest notification recieved!");
                MapRequestNotification mapRequestNotification = (MapRequestNotification) notification;
                MapReply mapReply = handleMapRequest(mapRequestNotification.getMapRequest());
                getLispSB().handleMapReply(mapReply, mapRequestNotification.getSourceAddress());
            } else {
                logger.error("Unknown notification: " + notification);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private ILispSouthboundPlugin getLispSB() {
        if (lispSB == null) {
            lispSB = session.getRpcService(ILispSouthboundPlugin.class);
        }
        return lispSB;
    }

    public void handleMapReply(MapReply reply) {
        tlsMapReply.set(reply);
    }

    public void handleMapNotify(MapNotify notify) {
        tlsMapNotify.set(notify);
    }
}
