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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKey;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceNoMaskKey;
import org.opendaylight.lispflowmapping.implementation.lisp.AbstractLispComponent;
import org.opendaylight.lispflowmapping.implementation.lisp.MapResolver;
import org.opendaylight.lispflowmapping.implementation.lisp.MapServer;
import org.opendaylight.lispflowmapping.implementation.mdsal.AuthenticationKeyDataListener;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.mdsal.MappingDataListener;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.implementation.util.MapServerMapResolverUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMappingShell;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LfmControlPlaneService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.RequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapNotifyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.transportaddress.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.LfmMappingDatabaseService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMappingService implements IFlowMapping, IFlowMappingShell, BindingAwareProvider,
        IMapRequestResultHandler, IMapNotifyHandler, AutoCloseable {
    protected static final Logger LOG = LoggerFactory.getLogger(LispMappingService.class);

    private static final ConfigIni configIni = new ConfigIni();
    private AuthenticationKeyDataListener keyListener;
    private MappingDataListener mappingListener;
    private ILispDAO lispDao = null;
    private IMapResolverAsync mapResolver;
    private IMapServerAsync mapServer;
    private volatile boolean shouldIterateMask;
    private volatile boolean shouldAuthenticate;
    private volatile boolean smr = configIni.smrIsSet();
    private ThreadLocal<MapReply> tlsMapReply = new ThreadLocal<MapReply>();
    private ThreadLocal<MapNotify> tlsMapNotify = new ThreadLocal<MapNotify>();
    private ThreadLocal<Pair<MapRequest, TransportAddress>> tlsMapRequest = new ThreadLocal<Pair<MapRequest, TransportAddress>>();

    private LfmControlPlaneService lispSB = null;
    private ProviderContext session;

    private DataStoreBackEnd dsbe;
    private NotificationService notificationService;
    private static LispMappingService lfmService = null;
    private BindingAwareBroker.RpcRegistration<LfmMappingDatabaseService> lfmDbRpc;
    private DataBroker dataBrokerService;
    private RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker broker;

    public LispMappingService() {
        LOG.debug("LispMappingService Module starting!");
        lfmService = this;
    }

    public void setDataBrokerService(DataBroker dataBrokerService) {
        this.dataBrokerService = dataBrokerService;
    }

    public void setRpcProviderRegistry(RpcProviderRegistry rpcRegistry) {
        this.rpcRegistry = rpcRegistry;
    }

    public void setBindingAwareBroker(BindingAwareBroker broker) {
        this.broker = broker;
    }

    public void initialize() {
        broker.registerProvider(this);

        LfmMappingDatabaseRpc mappingDbProviderRpc = new LfmMappingDatabaseRpc(dataBrokerService);
        lfmDbRpc = rpcRegistry.addRpcImplementation(LfmMappingDatabaseService.class, mappingDbProviderRpc);
        dsbe = new DataStoreBackEnd(dataBrokerService);
        restoreDaoFromDatastore();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("Lisp Consumer session initialized!");
        notificationService = session.getSALService(NotificationService.class);
        registerNotificationListener(AddMapping.class, new MapRegisterNotificationHandler());
        registerNotificationListener(RequestMapping.class, new MapRequestNotificationHandler());
        registerDataListeners(session.getSALService(DataBroker.class));
        this.session = session;
        LOG.info("LISP (RFC6830) Mapping Service init finished");
    }

    class LispIpv4AddressInMemoryConverter implements ILispTypeConverter<Ipv4Address, Integer> {
    }

    class LispIpv6AddressInMemoryConverter implements ILispTypeConverter<Ipv6Address, Integer> {
    }

    class MappingServiceKeyConvertor implements ILispTypeConverter<MappingServiceKey, Integer> {
    }

    class MappingServiceNoMaskKeyConvertor implements ILispTypeConverter<MappingServiceNoMaskKey, Integer> {
    }

    public static LispMappingService getLispMappingService() {
        return lfmService;
    }

    public void basicInit(ILispDAO dao) {
        lispDao = dao;
        mapResolver = new MapResolver(dao);
        mapServer = new MapServer(dao);
    }

    public void setLispDao(ILispDAO dao) {
        LOG.trace("LispDAO set in LispMappingService");
        basicInit(dao);
    }

    void unsetLispDao(ILispDAO dao) {
        LOG.trace("LispDAO was unset in LispMappingService");
        mapServer = null;
        mapResolver = null;
        lispDao = null;
    }

    private void restoreDaoFromDatastore() {
        List<Mapping> mappings = dsbe.getAllMappings();
        List<AuthenticationKey> authKeys = dsbe.getAllAuthenticationKeys();

        LOG.info("Restoring {} mappings and {} keys from datastore into DAO", mappings.size(), authKeys.size());

        // restore southbound registered entries first ...
        for (Mapping mapping : mappings) {
            if (mapping.getOrigin() == MappingOrigin.Southbound) {
                MapRegister register = MapServerMapResolverUtil.getMapRegister(mapping);
                handleMapRegister(register, false);
            }
        }

        // because northbound registrations have priority
        for (Mapping mapping : mappings) {
            if (mapping.getOrigin() == MappingOrigin.Northbound) {
                MapRegister register = MapServerMapResolverUtil.getMapRegister(mapping);
                handleMapRegister(register, false);
            }
        }

        for (AuthenticationKey authKey : authKeys) {
            addAuthenticationKey(authKey.getLispAddressContainer(), authKey.getMaskLength(), authKey.getAuthkey());
        }
    }

    public void destroy() {
        LOG.info("LISP (RFC6830) Mapping Service is destroyed!");
        mapResolver = null;
        mapServer = null;
        closeDataListeners();
    }

    public String printMappings() {
        final StringBuffer sb = new StringBuffer();
        sb.append("EID\tRLOCs\n");
        final IRowVisitor innerVisitor = (new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append(key + "\t");
                }
                if (!(valueKey.equals(AbstractLispComponent.LCAF_SRCDST_SUBKEY))) {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        lispDao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                if (valueKey.equals(AbstractLispComponent.LCAF_SRCDST_SUBKEY)) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(innerVisitor);
                    sb.append("}\t");
                } else {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    public void addDefaultKeyIPv4() {
        LispAddressContainer address = LispAFIConvertor.toContainer(
                new Ipv4AddressBuilder().setIpv4Address(new Ipv4Address("0.0.0.0")).build());
        addAuthenticationKey(address, 0, "password");
    }

    public MapReply handleMapRequest(MapRequest request) {
        return handleMapRequest(request, smr);
    }

    public MapReply handleMapRequest(MapRequest request, boolean smr) {
        LOG.debug("DAO: Retrieving mapping for {}",
                LispAddressStringifier.getString(request.getEidRecord().get(0).getLispAddressContainer(),
                request.getEidRecord().get(0).getMask()));

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
        LOG.debug("DAO: Adding mapping for {}",
                LispAddressStringifier.getString(mapRegister.getEidToLocatorRecord().get(0).getLispAddressContainer(),
                mapRegister.getEidToLocatorRecord().get(0).getMaskLength()));

        tlsMapNotify.set(null);
        mapServer.handleMapRegister(mapRegister, smr, this);
        // After this invocation we assume that the thread local is filled with
        // the reply
        return tlsMapNotify.get();
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        LOG.debug("DAO: Retrieving authentication key for {}", LispAddressStringifier.getString(address, maskLen));
        return mapServer.getAuthenticationKey(address, maskLen);
    }

    public void removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        LOG.debug("DAO: Removing authentication key for {}", LispAddressStringifier.getString(address, maskLen));
        mapServer.removeAuthenticationKey(address, maskLen);
    }

    public void addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        LOG.debug("DAO: Adding authentication key '{}' for {}", key,
                LispAddressStringifier.getString(address, maskLen));
        mapServer.addAuthenticationKey(address, maskLen, key);
    }

    public void removeMapping(LispAddressContainer address, int maskLen) {
        LOG.debug("DAO: Removing mapping for {}", LispAddressStringifier.getString(address, maskLen));
        mapServer.removeMapping(address, maskLen, smr, this);
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

    private void registerDataListeners(DataBroker broker) {
        keyListener = new AuthenticationKeyDataListener(broker, this);
        mappingListener = new MappingDataListener(broker, this);
    }

    private void closeDataListeners() {
        keyListener.closeDataChangeListener();
        mappingListener.closeDataChangeListener();
    }

    public <T extends Notification> void registerNotificationListener(Class<T> notificationType, NotificationListener<T> listener) {
        notificationService.registerNotificationListener(notificationType, listener);
    }

    private class MapRegisterNotificationHandler implements NotificationListener<AddMapping> {

        @Override
        public void onNotification(AddMapping mapRegisterNotification) {
            MapNotify mapNotify = handleMapRegister(mapRegisterNotification.getMapRegister(), smr);
            if (mapNotify != null) {
                // store mappings in md-sal datastore only if we have a MapNotify
                // XXX: this assumes that null MapNotifys are equivalent to authentication/registration errors
                //      however notifies may be disabled with a flag (by the registering router). This should
                //      be solved by moving southbound authentication of registrations out of handleMapRegister().
                List<Mapping> mappings = LispNotificationHelper.getMapping(mapRegisterNotification);
                for (Mapping mapping : mappings) {
                    dsbe.updateMapping(mapping);
                }

                TransportAddressBuilder tab = new TransportAddressBuilder();
                tab.setIpAddress(mapRegisterNotification.getTransportAddress().getIpAddress());
                tab.setPort(new PortNumber(LispMessage.PORT_NUM));
                SendMapNotifyInputBuilder smnib = new SendMapNotifyInputBuilder();
                smnib.setMapNotify(new MapNotifyBuilder(mapNotify).build());
                smnib.setTransportAddress(tab.build());
                getLispSB().sendMapNotify(smnib.build());
            } else {
                LOG.warn("got null map notify");
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
                LOG.warn("got null map reply");
            }
        }
    }

    private LfmControlPlaneService getLispSB() {
        if (lispSB == null) {
            lispSB = session.getRpcService(LfmControlPlaneService.class);
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
        LOG.debug("Sending SMR to " + subscriber.toString());
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

    @Override
    public void close() throws Exception {
        lfmDbRpc.close();
        destroy();
    }
}
