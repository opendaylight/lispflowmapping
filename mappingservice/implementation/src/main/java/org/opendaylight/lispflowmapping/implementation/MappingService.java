/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.mdsal.AuthenticationKeyDataListener;
import org.opendaylight.lispflowmapping.implementation.mdsal.MappingDataListener;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.RPCInputConvertorUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllMappingsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingWithXtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingWithXtrIdInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingWithXtrIdOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingWithXtrIdOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllMappingsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllOperationalContent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllOperationalContentInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllOperationalContentOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveAllOperationalContentOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingsOutput;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Component(service = IMappingService.class, immediate = true, property = "type=default")
@Singleton
public class MappingService implements IMappingService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(MappingService.class);

    private MappingSystem mappingSystem;
    private DataStoreBackEnd dsbe;
    private AuthenticationKeyDataListener keyListener;
    private MappingDataListener mappingListener;

    @Reference
    private volatile ILispDAO dao;
    @Reference
    private volatile DataBroker dataBroker;
    @Reference
    private volatile RpcProviderService rpcProviderService;
    @Reference
    private volatile NotificationPublishService notificationPublishService;
    private Registration rpcRegistration;

    private boolean mappingMergePolicy = ConfigIni.getInstance().mappingMergeIsSet();
    private final boolean notificationPolicy = ConfigIni.getInstance().smrIsSet();
    private final boolean iterateMask = true;
    private boolean isMaster = false;

    @Override
    public void setMappingMerge(boolean mergeMapping) {
        this.mappingMergePolicy = mergeMapping;
        if (mappingSystem != null) {
            mappingSystem.setMappingMerge(mergeMapping);
            ConfigIni.getInstance().setMappingMerge(mappingMergePolicy);
        }
    }

    @Override
    public void setLookupPolicy(IMappingService.LookupPolicy policy) {
        ConfigIni.getInstance().setLookupPolicy(policy);
    }

    @Activate
    public void initialize() {
        LOG.info("Mapping Service initializing...");
        dsbe = new DataStoreBackEnd(dataBroker);

        rpcRegistration = rpcProviderService.registerRpcImplementations(
            ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
                .put(AddKey.class, this::addKey)
                .put(GetKey.class, this::getKey)
                .put(UpdateKey.class, this::updateKey)
                .put(RemoveKey.class, this::removeKey)
                .put(AddMapping.class, this::addMapping)
                .put(GetMapping.class, this::getMapping)
                .put(GetMappingWithXtrId.class, this::getMappingWithXtrId)
                .put(UpdateMapping.class, this::updateMapping)
                .put(RemoveMapping.class, this::removeMapping)
                .put(AddKeys.class, this::addKeys)
                .put(GetKeys.class, this::getKeys)
                .put(UpdateKeys.class, this::updateKeys)
                .put(RemoveKeys.class, this::removeKeys)
                .put(AddMappings.class, this::addMappings)
                .put(GetMappings.class, this::getMappings)
                .put(UpdateMappings.class, this::updateMappings)
                .put(RemoveMappings.class, this::removeMappings)
                .put(GetAllKeys.class, this::getAllKeys)
                .put(GetAllMappings.class, this::getAllMappings)
                .put(RemoveAllKeys.class, this::removeAllKeys)
                .put(RemoveAllMappings.class, this::removeAllMappings)
                .put(RemoveAllOperationalContent.class, this::removeAllOperationalContent)
                .build()
        );

        mappingSystem = new MappingSystem(dao, iterateMask, notificationPublishService, mappingMergePolicy);
        mappingSystem.setDataStoreBackEnd(dsbe);
        mappingSystem.initialize();

        keyListener = new AuthenticationKeyDataListener(dataBroker, mappingSystem);
        mappingListener = new MappingDataListener(dataBroker, mappingSystem, notificationPublishService);
        LOG.info("Mapping Service loaded.");
    }

    public ListenableFuture<RpcResult<AddKeyOutput>> addKey(AddKeyInput input) {
        requireNonNull(input, "add-key RPC input must be not null!");
        LOG.trace("RPC received to add the following key: {}", input);

        RpcResultBuilder<AddKeyOutput> rpcResultBuilder;

        MappingAuthkey key = mappingSystem.getAuthenticationKey(convertToBinaryIfNecessary(input.getEid()));

        if (key != null) {
            String message = "Key already exists! Please use update-key if you want to change it.";
            rpcResultBuilder = RpcResultBuilder.<AddKeyOutput>failed()
                    .withError(ErrorType.PROTOCOL, ErrorTag.DATA_EXISTS, message);
            return rpcResultBuilder.buildFuture();
        }

        dsbe.addAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));
        rpcResultBuilder = RpcResultBuilder.success(new AddKeyOutputBuilder().build());

        return rpcResultBuilder.buildFuture();
    }

    public ListenableFuture<RpcResult<AddMappingOutput>> addMapping(AddMappingInput input) {
        requireNonNull(input, "add-mapping RPC input must be not null!");
        LOG.trace("RPC received to add the following mapping: {}", input);

        dsbe.addMapping(RPCInputConvertorUtil.toMapping(input));

        RpcResultBuilder<AddMappingOutput> rpcResultBuilder;

        rpcResultBuilder = RpcResultBuilder.success(new AddMappingOutputBuilder().build());

        return rpcResultBuilder.buildFuture();
    }

    @Override
    public void addMapping(MappingOrigin origin, Eid key, SiteId siteId, MappingData mappingData) {
        // SB registrations are first written to the MappingSystem and only afterwards are persisted to the datastore
        if (origin.equals(MappingOrigin.Southbound)) {
            // Store data first in MapCache and only afterwards persist to datastore. This should be used only for SB
            // registrations
            mappingSystem.addMapping(origin, key, mappingData);
            dsbe.addMapping(DSBEInputUtil.toMapping(origin, key, siteId, mappingData));
            if (mappingData.getXtrId() != null) {
                dsbe.addXtrIdMapping(DSBEInputUtil.toXtrIdMapping(mappingData));
            }
        } else {
            dsbe.addMapping(DSBEInputUtil.toMapping(origin, key, siteId, mappingData));
        }
    }

    @Override
    public MappingData addNegativeMapping(Eid key) {
        return mappingSystem.addNegativeMapping(key);
    }

    @Override
    public void refreshMappingRegistration(Eid key, XtrId xtrId, Long timestamp) {
        mappingSystem.refreshMappingRegistration(key, xtrId, timestamp);
    }

    public ListenableFuture<RpcResult<GetKeyOutput>> getKey(GetKeyInput input) {
        requireNonNull(input, "get-key RPC input must be not null!");
        LOG.trace("RPC received to get the following key: {}", input);

        RpcResultBuilder<GetKeyOutput> rpcResultBuilder;

        MappingAuthkey key = mappingSystem.getAuthenticationKey(convertToBinaryIfNecessary(input.getEid()));

        if (key == null) {
            String message = "Key was not found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetKeyOutput>failed()
                    .withError(ErrorType.APPLICATION, ErrorTag.DATA_MISSING, message);
        } else {
            rpcResultBuilder = RpcResultBuilder.success(new GetKeyOutputBuilder().setMappingAuthkey(key).build());
        }

        return rpcResultBuilder.buildFuture();
    }

    public ListenableFuture<RpcResult<GetMappingOutput>> getMapping(GetMappingInput input) {
        requireNonNull(input, "get-mapping RPC input must be not null!");
        LOG.trace("RPC received to get the following mapping: {}", input);

        RpcResultBuilder<GetMappingOutput> rpcResultBuilder;

        MappingData reply = mappingSystem.getMapping(convertToBinaryIfNecessary(input.getEid()));

        if (reply == null) {
            String message = "No mapping was found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetMappingOutput>failed()
                    .withError(ErrorType.APPLICATION, ErrorTag.DATA_MISSING, message);
        } else {
            final MappingRecord convertedReply = convertFromBinaryIfNecessary(reply.getRecord());
            rpcResultBuilder = RpcResultBuilder.success(
                new GetMappingOutputBuilder().setMappingRecord(convertedReply).build());
        }

        return rpcResultBuilder.buildFuture();
    }

    @Override
    public MappingData getMapping(MappingOrigin origin, Eid key) {
        return mappingSystem.getMapping(origin, key);
    }

    @Override
    public MappingData getMapping(Eid key) {
        return mappingSystem.getMapping(key);
    }

    @Override
    public MappingData getMapping(Eid srcKey, Eid dstKey) {
        return mappingSystem.getMapping(srcKey, dstKey);
    }

    @Override
    public void subscribe(Subscriber subscriber, Eid subscribedEid) {
        mappingSystem.subscribe(subscriber, subscribedEid);
    }

    @Override
    public Set<Subscriber> getSubscribers(Eid eid) {
        return mappingSystem.getSubscribers(eid);
    }

    private ListenableFuture<RpcResult<GetMappingWithXtrIdOutput>> getMappingWithXtrId(GetMappingWithXtrIdInput input) {
        requireNonNull(input, "get-mapping RPC input must be not null!");
        LOG.trace("RPC received to get the following mapping: {}", input);

        RpcResultBuilder<GetMappingWithXtrIdOutput> rpcResultBuilder;

        MappingData reply = mappingSystem.getMapping(null, convertToBinaryIfNecessary(input.getEid()),
                input.getXtrId());

        if (reply == null) {
            String message = "No mapping was found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetMappingWithXtrIdOutput>failed()
                    .withError(ErrorType.APPLICATION, ErrorTag.DATA_MISSING, message);
        } else {
            final MappingRecord convertedReply = convertFromBinaryIfNecessary(reply.getRecord());
            rpcResultBuilder = RpcResultBuilder.success(new GetMappingWithXtrIdOutputBuilder()
                    .setMappingRecord(convertedReply).build());
        }

        return rpcResultBuilder.buildFuture();
    }

    public ListenableFuture<RpcResult<RemoveKeyOutput>> removeKey(RemoveKeyInput input) {
        requireNonNull(input, "remove-key RPC input must be not null!");
        LOG.trace("RPC received to remove the following key: {}", input);

        RpcResultBuilder<RemoveKeyOutput> rpcResultBuilder;

        dsbe.removeAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));

        rpcResultBuilder = RpcResultBuilder.success(new RemoveKeyOutputBuilder().build());

        return rpcResultBuilder.buildFuture();
    }

    public ListenableFuture<RpcResult<RemoveMappingOutput>> removeMapping(RemoveMappingInput input) {
        requireNonNull(input, "remove-mapping RPC input must be not null!");
        LOG.trace("RPC received to remove the following mapping: {}", input);

        RpcResultBuilder<RemoveMappingOutput> rpcResultBuilder;

        dsbe.removeMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success(new RemoveMappingOutputBuilder().build());

        return rpcResultBuilder.buildFuture();
    }

    @Override
    public void removeMapping(MappingOrigin origin, Eid key) {
        if (origin.equals(MappingOrigin.Southbound)) {
            mappingSystem.removeMapping(origin, key);
        }
        dsbe.removeMapping(DSBEInputUtil.toMapping(origin, key));
    }

    public ListenableFuture<RpcResult<UpdateKeyOutput>> updateKey(UpdateKeyInput input) {
        requireNonNull(input, "update-key RPC input must be not null!");
        LOG.trace("RPC received to update the following key: {}", input);

        RpcResultBuilder<UpdateKeyOutput> rpcResultBuilder;

        MappingAuthkey key = mappingSystem.getAuthenticationKey(convertToBinaryIfNecessary(input.getEid()));

        if (key == null) {
            String message = "Key doesn't exist! Please use add-key if you want to create a new authentication key.";
            rpcResultBuilder = RpcResultBuilder.<UpdateKeyOutput>failed()
                    .withError(ErrorType.PROTOCOL, ErrorTag.DATA_MISSING, message);
            return rpcResultBuilder.buildFuture();
        }

        dsbe.updateAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));
        rpcResultBuilder = RpcResultBuilder.success(new UpdateKeyOutputBuilder().build());

        return rpcResultBuilder.buildFuture();
    }

    public ListenableFuture<RpcResult<UpdateMappingOutput>> updateMapping(UpdateMappingInput input) {
        LOG.trace("RPC received to update the following mapping: {}", input);
        requireNonNull(input, "update-mapping RPC input must be not null!");

        RpcResultBuilder<UpdateMappingOutput> rpcResultBuilder;

        dsbe.updateMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success(new UpdateMappingOutputBuilder().build());

        return rpcResultBuilder.buildFuture();
    }

    public ListenableFuture<RpcResult<RemoveKeysOutput>> removeKeys(RemoveKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<RemoveMappingsOutput>> removeMappings(RemoveMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<GetKeysOutput>> getKeys(GetKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<AddMappingsOutput>> addMappings(AddMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<UpdateKeysOutput>> updateKeys(UpdateKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<RemoveAllMappingsOutput>> removeAllMappings(RemoveAllMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<RemoveAllKeysOutput>> removeAllKeys(RemoveAllKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<GetAllKeysOutput>> getAllKeys(GetAllKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<UpdateMappingsOutput>> updateMappings(UpdateMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<AddKeysOutput>> addKeys(AddKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<GetAllMappingsOutput>> getAllMappings(GetAllMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenableFuture<RpcResult<GetMappingsOutput>> getMappings(
            GetMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    private ListenableFuture<RpcResult<RemoveAllOperationalContentOutput>> removeAllOperationalContent(
            RemoveAllOperationalContentInput input) {
        RpcResultBuilder<RemoveAllOperationalContentOutput> rpcResultBuilder;

        /*
         * Since master nodes ignore datastore changes for southbound originated mappings, they need to be removed
         * explicitly.
         */
        if (isMaster) {
            mappingSystem.cleanSBMappings();
        }
        dsbe.removeAllOperationalDatastoreContent();

        rpcResultBuilder = RpcResultBuilder.success(new RemoveAllOperationalContentOutputBuilder().build());

        return rpcResultBuilder.buildFuture();
    }

    @Override
    public Eid getWidestNegativePrefix(Eid key) {
        return mappingSystem.getWidestNegativePrefix(key);
    }

    @Override
    public Set<Eid> getSubtree(MappingOrigin origin, Eid key) {
        return mappingSystem.getSubtree(origin, key);
    }

    @Override
    public void addAuthenticationKey(Eid key, MappingAuthkey authKey) {
        dsbe.addAuthenticationKey(DSBEInputUtil.toAuthenticationKey(key, authKey));
    }

    @Override
    public MappingAuthkey getAuthenticationKey(Eid key) {
        return mappingSystem.getAuthenticationKey(key);
    }

    @Override
    public void removeAuthenticationKey(Eid key) {
        dsbe.removeAuthenticationKey(DSBEInputUtil.toAuthenticationKey(key, null));
    }

    @Override
    public void addData(MappingOrigin origin, Eid key, String subKey, Object data) {
        mappingSystem.addData(origin, key, subKey, data);
    }

    @Override
    public Object getData(MappingOrigin origin, Eid key, String subKey) {
        return mappingSystem.getData(origin, key, subKey);
    }

    @Override
    public void removeData(MappingOrigin origin, Eid key, String subKey) {
        mappingSystem.removeData(origin, key, subKey);
    }

    @Override
    public Eid getParentPrefix(Eid key) {
        return mappingSystem.getParentPrefix(key);
    }

    @Override
    public String printMappings() {
        return mappingSystem.printMappings();
    }

    @Override
    public String prettyPrintMappings() {
        return mappingSystem.prettyPrintMappings();
    }

    @Override
    public String printKeys() {
        return mappingSystem.printKeys();
    }

    @Override
    public String prettyPrintKeys() {
        return mappingSystem.prettyPrintKeys();
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() throws Exception {
        LOG.info("Mapping Service is being destroyed!");
        rpcRegistration.close();
        keyListener.closeDataChangeListener();
        mappingListener.closeDataChangeListener();
        mappingSystem.destroy();
    }

    @Override
    public void cleanCachedMappings() {
        mappingSystem.cleanCaches();
        dsbe.removeAllDatastoreContent();
    }

    private static Eid convertToBinaryIfNecessary(Eid eid) {
        if (LispAddressUtil.addressNeedsConversionToBinary(eid.getAddress())) {
            return LispAddressUtil.convertToBinary(eid);
        }
        return eid;
    }

    private static MappingRecord convertFromBinaryIfNecessary(MappingRecord originalRecord) {
        List<LocatorRecord> originalLocators = originalRecord.getLocatorRecord();

        List<LocatorRecord> convertedLocators = null;
        if (originalLocators != null) {
            // If convertedLocators is non-null, while originalLocators is also non-null, conversion has been made
            convertedLocators = convertFromBinaryIfNecessary(originalLocators);
        }

        if (LispAddressUtil.addressNeedsConversionFromBinary(originalRecord.getEid().getAddress())
                || originalLocators != null && convertedLocators != null) {
            MappingRecordBuilder mrb = new MappingRecordBuilder(originalRecord);
            mrb.setEid(LispAddressUtil.convertFromBinary(originalRecord.getEid()));
            if (convertedLocators != null) {
                mrb.setLocatorRecord(convertedLocators);
            }
            return mrb.build();
        }
        return originalRecord;
    }

    private static List<LocatorRecord> convertFromBinaryIfNecessary(List<LocatorRecord> originalLocators) {
        List<LocatorRecord> convertedLocators = null;
        for (LocatorRecord record : originalLocators) {
            if (LispAddressUtil.addressNeedsConversionFromBinary(record.getRloc().getAddress())) {
                LocatorRecordBuilder lrb = new LocatorRecordBuilder(record);
                lrb.setRloc(LispAddressUtil.convertFromBinary(record.getRloc()));
                if (convertedLocators == null) {
                    convertedLocators = new ArrayList<>();
                }
                convertedLocators.add(lrb.build());
            }
        }
        if (convertedLocators != null) {
            return convertedLocators;
        }
        return originalLocators;
    }

    @Override
    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
        mappingSystem.setIsMaster(isMaster);
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }
}
