/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.mdsal.AuthenticationKeyDataListener;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.mdsal.MappingDataListener;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.RPCInputConvertorUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMappingShell;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.update.key.input.EidBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;

/**
 * Dispatcher of API calls that implements the RPC and Java APIs in mappingservice.yang and IMappingService
 * respectively. It also coordinates and acts as container for all objects in charge of
 *  - saving and updating md-sal datastore stored mappings
 *  - monitoring md-sal datastore mapping updates and pushing them to the in memory mapping-system
 *  - in memory mapping-system
 *
 * @author Lorand Jakab
 * @author Florin Coras
 *
 */
public class MappingService implements MappingserviceService, IMappingService, IFlowMappingShell, BindingAwareProvider,
        AutoCloseable {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingService.class);
    private static final String NOT_FOUND_TAG = "data-missing";
    private static final String DATA_EXISTS_TAG = "data-exists";

    private MappingSystem mappingSystem;
    private DataStoreBackEnd dsbe;
    private RpcRegistration<MappingserviceService> mappingServiceRpc;
    private AuthenticationKeyDataListener keyListener;
    private MappingDataListener mappingListener;
    private ILispDAO dao;

    private DataBroker dataBroker;
    private RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker bindingAwareBroker;
    private NotificationProviderService notificationService;

    private static final ConfigIni configIni = new ConfigIni();
    private boolean overwritePolicy = configIni.mappingOverwriteIsSet();
    private boolean notificationPolicy = configIni.smrIsSet();
    private boolean iterateMask = true;

    public MappingService() {
        LOG.debug("MappingService created!");
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setRpcProviderRegistry(RpcProviderRegistry rpc) {
        this.rpcRegistry = rpc;
    }

    public void setBindingAwareBroker(BindingAwareBroker broker) {
        this.bindingAwareBroker = broker;
    }

    public void setNotificationService(NotificationProviderService notification) {
        this.notificationService = notification;
    }

    public void setDaoService(ILispDAO dao) {
        this.dao = dao;
    }

    @Override
    public void setMappingOverwrite(boolean overwrite) {
        this.overwritePolicy = overwrite;
        if (mappingSystem != null) {
            mappingSystem.setOverwritePolicy(overwrite);
        }
    }

    public void initialize() {
        bindingAwareBroker.registerProvider(this);

        mappingServiceRpc = rpcRegistry.addRpcImplementation(MappingserviceService.class, this);
        dsbe = new DataStoreBackEnd(dataBroker);

        mappingSystem = new MappingSystem(dao, iterateMask, notificationPolicy, overwritePolicy);
        mappingSystem.setDataStoreBackEnd(dsbe);
        mappingSystem.initialize();

        keyListener = new AuthenticationKeyDataListener(dataBroker, mappingSystem);
        mappingListener = new MappingDataListener(dataBroker, mappingSystem, notificationService);
    }

    @Override
    public Future<RpcResult<Void>> addKey(AddKeyInput input) {
        Preconditions.checkNotNull(input, "add-key RPC input must be not null!");
        LOG.trace("RPC received to add the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new AddKeyInputBuilder(input).setLispAddressContainer(
                MaskUtil.fixMask(input.getLispAddressContainer(), input.getMaskLength())).build();

        String key = mappingSystem.getAuthenticationKey(input.getLispAddressContainer());

        if (key != null) {
            String message = "Key already exists! Please use update-key if you want to change it.";
            rpcResultBuilder = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.PROTOCOL, DATA_EXISTS_TAG, message);
            return Futures.immediateFuture(rpcResultBuilder.build());
        }

        dsbe.addAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));
        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> addMapping(AddMappingInput input) {
        Preconditions.checkNotNull(input, "add-mapping RPC input must be not null!");
        LOG.trace("RPC received to add the following mapping: " + input.toString());

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new AddMappingInputBuilder(input).setLispAddressContainer(
                MaskUtil.fixMask(input.getLispAddressContainer(), input.getMaskLength())).build();

        dsbe.addMapping(RPCInputConvertorUtil.toMapping(input));

        RpcResultBuilder<Void> rpcResultBuilder;

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<GetKeyOutput>> getKey(GetKeyInput input) {
        Preconditions.checkNotNull(input, "get-key RPC input must be not null!");
        LOG.trace("RPC received to get the following key: " + input.toString());

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new GetKeyInputBuilder(input).setLispAddressContainer(
                MaskUtil.fixMask(input.getLispAddressContainer(), input.getMaskLength())).build();

        RpcResultBuilder<GetKeyOutput> rpcResultBuilder;

        String key = mappingSystem.getAuthenticationKey(input.getLispAddressContainer());

        if (key == null) {
            String message = "Key was not found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetKeyOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, NOT_FOUND_TAG, message);
        } else {
            rpcResultBuilder = RpcResultBuilder.success(new GetKeyOutputBuilder().setAuthkey(key));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<GetMappingOutput>> getMapping(GetMappingInput input) {
        Preconditions.checkNotNull(input, "get-mapping RPC input must be not null!");
        LOG.trace("RPC received to get the following mapping: " + input.toString());

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new GetMappingInputBuilder(input).setLispAddressContainer(
                MaskUtil.fixMask(input.getLispAddressContainer(), input.getMaskLength())).build();

        RpcResultBuilder<GetMappingOutput> rpcResultBuilder;

        EidToLocatorRecord reply = (EidToLocatorRecord) mappingSystem.getMapping(input.getLispAddressContainer());

        if (reply == null) {
            String message = "No mapping was found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetMappingOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, NOT_FOUND_TAG, message);
        } else {
            rpcResultBuilder = RpcResultBuilder.success(new GetMappingOutputBuilder().setEidToLocatorRecord(Arrays
                    .asList(reply)));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeKey(RemoveKeyInput input) {
        Preconditions.checkNotNull(input, "remove-key RPC input must be not null!");
        LOG.trace("RPC received to remove the following key: " + input.toString());

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new RemoveKeyInputBuilder(input).setLispAddressContainer(
                MaskUtil.fixMask(input.getLispAddressContainer(), input.getMaskLength())).build();

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.removeAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeMapping(RemoveMappingInput input) {
        Preconditions.checkNotNull(input, "remove-mapping RPC input must be not null!");
        LOG.trace("RPC received to remove the following mapping: " + input.toString());

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new RemoveMappingInputBuilder(input).setLispAddressContainer(
                MaskUtil.fixMask(input.getLispAddressContainer(), input.getMaskLength())).build();

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.removeMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> updateKey(UpdateKeyInput input) {
        Preconditions.checkNotNull(input, "update-key RPC input must be not null!");
        LOG.trace("RPC received to update the following key: " + input.toString());

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new UpdateKeyInputBuilder(input).setEid(
                new EidBuilder(input.getEid()).setLispAddressContainer(
                        MaskUtil.fixMask(input.getEid().getLispAddressContainer(), input.getEid()
                                .getMaskLength())).build()).build();

        RpcResultBuilder<Void> rpcResultBuilder;

        String key = mappingSystem.getAuthenticationKey(input.getEid().getLispAddressContainer());

        if (key == null) {
            String message = "Key doesn't exist! Please use add-key if you want to create a new authentication key.";
            rpcResultBuilder = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.PROTOCOL, NOT_FOUND_TAG, message);
            return Futures.immediateFuture(rpcResultBuilder.build());
        }

        dsbe.updateAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));
        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> updateMapping(UpdateMappingInput input) {
        LOG.trace("RPC received to update the following mapping: " + input.toString());
        Preconditions.checkNotNull(input, "update-mapping RPC input must be not null!");

        // XXX: to remove once RPC API has been updated to remove explicit mask
        input = new UpdateMappingInputBuilder(input).setLispAddressContainer(
                MaskUtil.fixMask(input.getLispAddressContainer(), input.getMaskLength())).build();

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.updateMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public void addMapping(MappingOrigin origin, LispAddressContainer key, SiteId siteId, Object data) {
        dsbe.addMapping(DSBEInputUtil.toMapping(origin, key, siteId, (EidToLocatorRecord) data));
        mappingSystem.updateMappingRegistration(origin, key);
    }

    @Override
    public Object getMapping(MappingOrigin origin, LispAddressContainer key) {
        return mappingSystem.getMapping(origin, key);
    }

    @Override
    public Object getMapping(LispAddressContainer key) {
        return mappingSystem.getMapping(key);
    }

    @Override
    public Object getMapping(LispAddressContainer srcKey, LispAddressContainer dstKey) {
        return mappingSystem.getMapping(srcKey, dstKey);
    }

    @Override
    public void removeMapping(MappingOrigin origin, LispAddressContainer key) {
        dsbe.removeMapping(DSBEInputUtil.toMapping(origin, key));
    }

    @Override
    public void addAuthenticationKey(LispAddressContainer key, String authKey) {
        dsbe.addAuthenticationKey(DSBEInputUtil.toAuthenticationKey(key, authKey));
    }

    @Override
    public String getAuthenticationKey(LispAddressContainer key) {
        return mappingSystem.getAuthenticationKey(key);
    }

    @Override
    public void removeAuthenticationKey(LispAddressContainer key) {
        dsbe.removeAuthenticationKey(DSBEInputUtil.toAuthenticationKey(key, null));
    }

    @Override
    public void addData(MappingOrigin origin, LispAddressContainer key, String subKey, Object data) {
        mappingSystem.addData(origin, key, subKey, data);
    }

    @Override
    public Object getData(MappingOrigin origin, LispAddressContainer key, String subKey) {
        return mappingSystem.getData(origin, key, subKey);
    }

    @Override
    public void removeData(MappingOrigin origin, LispAddressContainer key, String subKey) {
        mappingSystem.removeData(origin, key, subKey);
    }

    @Override
    public String printMappings() {
        return mappingSystem.printMappings();
    }

    @Override
    public void addDefaultKeyIPv4() {
        LispAddressContainer address = LispAFIConvertor.toContainer(
                new Ipv4AddressBuilder().setIpv4Address(new Ipv4Address("0.0.0.0")).setMask((short)0).build());
        addAuthenticationKey(address, "password");
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("Mapping Service provider session initializing!");
    }

    @Override
    public void close() throws Exception {
        LOG.info("Mapping Service is being destroyed!");
        mappingServiceRpc.close();
        keyListener.closeDataChangeListener();
        mappingListener.closeDataChangeListener();
    }

    @Override
    public void cleanCachedMappings() {
        mappingSystem.cleanCaches();
        dsbe.removeAllMappings();
    }
}
