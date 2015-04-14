/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.lispflowmapping.implementation.backends.mdsal.InstanceIdentifierUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.db.instance.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyWithRefInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyWithRefOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingWithRefInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingWithRefOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.KeyRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveKeyWithRefInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveMappingWithRefInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.UpdateKeyWithRefInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.UpdateMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.UpdateMappingWithRefInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;

/**
 * Actual implementation of the RPCs defined in lfm-mapping-database.yang and
 * referenced by {@link LispMappingService}
 *
 * @author Lorand Jakab
 *
 */
class LfmMappingDatabaseRPCs {
    protected static final Logger LOG = LoggerFactory.getLogger(LfmMappingDatabaseRPCs.class);

    private LispMappingService lispMappingService;

    Future<RpcResult<AddKeyOutput>> addKey(AddKeyInput input) {
        Preconditions.checkNotNull(input, "add-key RPC input must be not null!");
        LOG.debug("RPC received to add the following key: " + input.toString());

        RpcResultBuilder<AddKeyOutput> rpcResultBuilder;

        MappingKey key = (MappingKey)input;

        lispMappingService.addAuthenticationKey(key.getLispAddressContainer(), key.getMaskLength(), key.getKey());

        // TODO
        // For now we generate the InstanceIdentifier to be returned by the RPC
        // using the same code that is in the back-end.  However, for better
        // separation, the MS/MR API should return it.  It should also return
        // helpful error messages in case adding the key fails

        // TODO change the InstanceIdentifier type, once we have YANG model support
        InstanceIdentifier<Eid> path = InstanceIdentifierUtil.createKey(key);
        rpcResultBuilder = RpcResultBuilder.success(new AddKeyOutputBuilder()
                .setKeyRef(new KeyRef(path)));

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<AddMappingOutput>> addMapping(AddMappingInput input) {
        Preconditions.checkNotNull(input, "add-mapping RPC input must be not null!");
        LOG.debug("RPC received to add the following mapping: " + input.toString());

        RpcResultBuilder<AddMappingOutput> rpcResultBuilder = null;

        // TODO is this really necessary?
//        if (!(input instanceof EidToLocatorRecord)) {
//            String message = "input is not an instance of EidToLocatorRecord!";
//            rpcResultBuilder = RpcResultBuilder.<AddMappingOutput>failed()
//                    .withError(RpcError.ErrorType.PROTOCOL, message);
//            return Futures.immediateFuture(rpcResultBuilder.build());
//        }

        EidToLocatorRecord mapping = (EidToLocatorRecord)input;

        MapRegister mapRegister = getMapRegister(mapping);
        lispMappingService.handleMapRegister(mapRegister, false); //SMR is false, since we ADD and not UPDATE a mapping

        // TODO
        // For now we generate the InstanceIdentifier to be returned by the RPC
        // using the same code that is in the back-end.  However, for better
        // separation, the MS/MR API should return it.  It should also return
        // helpful error messages in case adding the mapping fails
        InstanceIdentifier<Eid> index = InstanceIdentifierUtil.createEid(mapping);
        rpcResultBuilder = RpcResultBuilder.success(new AddMappingOutputBuilder()
                .setMapRef(new MappingRef(index)));

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<GetKeyOutput>> getKey(GetKeyInput input) {
        Preconditions.checkNotNull(input, "get-key RPC input must be not null!");
        LOG.debug("RPC received to get the following key: " + input.toString());

        RpcResultBuilder<GetKeyOutput> rpcResultBuilder;

        LispAddressContainer address = input.getLispAddressContainer();

        String key = lispMappingService.getAuthenticationKey(address, 0);

        InstanceIdentifier<Eid> path = InstanceIdentifierUtil.createKey(address);
        rpcResultBuilder = RpcResultBuilder.success(new GetKeyOutputBuilder()
                .setKeyRef(new KeyRef(path)).setKey(key));

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<GetKeyWithRefOutput>> getKeyWithRef(GetKeyWithRefInput input) {
        Preconditions.checkNotNull(input, "get-key-with-ref RPC input must be not null!");
        LOG.debug("RPC received to get the following key (reference based): " + input.toString());
        return null;
    }

    Future<RpcResult<GetMappingOutput>> getMapping(GetMappingInput input) {
        Preconditions.checkNotNull(input, "get-mapping RPC input must be not null!");
        LOG.debug("RPC received to add get following mapping: " + input.toString());
        return null;
    }

    Future<RpcResult<GetMappingWithRefOutput>> getMappingWithRef(GetMappingWithRefInput input) {
        Preconditions.checkNotNull(input, "get-mapping-with-ref RPC input must be not null!");
        LOG.debug("RPC received to add the following mapping (reference based): " + input.toString());
        return null;
    }

    Future<RpcResult<Void>> removeKey(RemoveKeyInput input) {
        Preconditions.checkNotNull(input, "remove-key RPC input must be not null!");
        LOG.debug("RPC received to remove the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        LispAddressContainer address = input.getLispAddressContainer();

        lispMappingService.removeAuthenticationKey(address, 0);

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<Void>> removeKeyWithRef(RemoveKeyWithRefInput input) {
        Preconditions.checkNotNull(input, "remove-key-with-ref RPC input must be not null!");
        LOG.debug("RPC received to remove the following key (reference based): " + input.toString());
        return null;
    }

    Future<RpcResult<Void>> removeMapping(RemoveMappingInput input) {
        Preconditions.checkNotNull(input, "remove-mapping RPC input must be not null!");
        LOG.debug("RPC received to remove the following mapping: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        LispAddressContainer address = input.getLispAddressContainer();

        lispMappingService.removeMapping(address, 0);

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<Void>> removeMappingWithRef(RemoveMappingWithRefInput input) {
        Preconditions.checkNotNull(input, "remove-mapping-with-ref RPC input must be not null!");
        LOG.debug("RPC received to remove the following mapping (reference based): " + input.toString());
        return null;
    }

    Future<RpcResult<Void>> updateKey(UpdateKeyInput input) {
        Preconditions.checkNotNull(input, "update-key RPC input must be not null!");
        LOG.debug("RPC received to update the following key: " + input.toString());
        return null;
    }

    Future<RpcResult<Void>> updateKeyWithRef(UpdateKeyWithRefInput input) {
        Preconditions.checkNotNull(input, "update-key-with-ref RPC input must be not null!");
        LOG.debug("RPC received to update the following key (reference based): " + input.toString());
        return null;
    }

    Future<RpcResult<Void>> updateMapping(UpdateMappingInput input) {
        LOG.debug("RPC received to update the following mapping: " + input.toString());
        Preconditions.checkNotNull(input, "update-mapping RPC input must be not null!");
        return null;
    }

    Future<RpcResult<Void>> updateMappingWithRef(UpdateMappingWithRefInput input) {
        Preconditions.checkNotNull(input, "update-mapping-with-ref RPC input must be not null!");
        LOG.debug("RPC received to update the following mapping (reference based): " + input.toString());
        return null;
    }

    MapRegister getMapRegister(EidToLocatorRecord mapping) {
        MapRegisterBuilder mrb = new MapRegisterBuilder();
        mrb.setEidToLocatorRecord(convertMapping(mapping));
        return mrb.build();
    }

    List<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord> convertMapping(EidToLocatorRecord mapping) {
        EidToLocatorRecordBuilder etlrb = new EidToLocatorRecordBuilder();
        etlrb.setRecordTtl(mapping.getRecordTtl());
        etlrb.setMaskLength(mapping.getMaskLength());
        etlrb.setMapVersion(mapping.getMapVersion());
        etlrb.setAction(mapping.getAction());
        etlrb.setAuthoritative(mapping.isAuthoritative());
        etlrb.setLispAddressContainer(mapping.getLispAddressContainer());
        etlrb.setLocatorRecord(mapping.getLocatorRecord());

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord> mappings =
                new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord>();
        mappings.add(etlrb.build());
        return mappings;
    }

    public void setLispMappingService(LispMappingService lispMappingService) {
        this.lispMappingService = lispMappingService;
    }
}
