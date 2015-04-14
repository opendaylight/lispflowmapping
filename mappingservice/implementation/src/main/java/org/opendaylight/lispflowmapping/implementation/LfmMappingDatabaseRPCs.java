/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import java.util.concurrent.Future;

import org.opendaylight.lispflowmapping.implementation.util.InstanceIdentifierUtil;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.db.instance.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyOutput;
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
import org.opendaylight.yangtools.yang.common.RpcError;
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
    protected static final Logger LOG = LoggerFactory.getLogger(LispMappingService.class);
    //by default we add keys/mappings to the DAO, this will be configurable
    private static final boolean DAO = true;
    private LispMappingService lispMappingService;

    Future<RpcResult<AddKeyOutput>> addKey(AddKeyInput input) {
        Preconditions.checkNotNull(input, "add-key RPC input must be not null!");
        MappingKey key = (MappingKey)input;

        LOG.debug("RPC received to add the following key: " + key.toString());

        RpcResultBuilder<AddKeyOutput> rpcResultBuilder;

        if (DAO) {
            lispMappingService.addAuthenticationKey(key.getLispAddressContainer(), 0, key.getKey());
        }

        InstanceIdentifier<Eid> path = InstanceIdentifierUtil.createKey(key);
        rpcResultBuilder = RpcResultBuilder.success(new AddKeyOutputBuilder()
                .setKeyRef(new KeyRef(path)));

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<AddMappingOutput>> addMapping(AddMappingInput input) {
        Preconditions.checkNotNull(input, "add-mapping RPC input must be not null!");
        EidToLocatorRecord mapping = (EidToLocatorRecord)input;

        LOG.debug("RPC received to add the following mapping: " + mapping.toString());

        RpcResultBuilder<AddMappingOutput> rpcResultBuilder = null;

        // TODO is this really necessary?
//        if (!(input instanceof EidToLocatorRecord)) {
//            String message = "input is not an instance of EidToLocatorRecord!";
//            rpcResultBuilder = RpcResultBuilder.<AddMappingOutput>failed()
//                    .withError(RpcError.ErrorType.PROTOCOL, message);
//            return Futures.immediateFuture(rpcResultBuilder.build());
//        }

        if (DAO) {
            // keyCheck(); ???
            // mappingCheck();  // this is an add operation, it should fail if entry exists (or not?)
            // mapRegister = getMapRegister(mapping);
            // lispMappingService.handleMapRegister(mapRegister, false) //SMR is false, since we ADD and not UPDATE a mapping
        }

        InstanceIdentifier<Eid> path = InstanceIdentifierUtil.createEid(mapping);
        rpcResultBuilder = RpcResultBuilder.success(new AddMappingOutputBuilder()
                .setMapRef(new MappingRef(path)));

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<GetKeyOutput>> getKey(GetKeyInput input) {
        LOG.debug("RPC received to get the following key: " + input.toString());
        return null;
    }

    Future<RpcResult<GetKeyWithRefOutput>> getKeyWithRef(
            GetKeyWithRefInput input) {
        LOG.debug("RPC received to get the following key (reference based): " + input.toString());
        return null;
    }

    Future<RpcResult<GetMappingOutput>> getMapping(GetMappingInput input) {
        LOG.debug("RPC received to add get following mapping: " + input.toString());
        return null;
    }

    Future<RpcResult<GetMappingWithRefOutput>> getMappingWithRef(
            GetMappingWithRefInput input) {
        LOG.debug("RPC received to add the following mapping (reference based): " + input.toString());
        return null;
    }

    Future<RpcResult<Void>> removeKey(RemoveKeyInput input) {
        return null;
    }

    Future<RpcResult<Void>> removeKeyWithRef(RemoveKeyWithRefInput input) {
        return null;
    }

    Future<RpcResult<Void>> removeMapping(RemoveMappingInput input) {
        return null;
    }

    Future<RpcResult<Void>> removeMappingWithRef(
            RemoveMappingWithRefInput input) {
        return null;
    }

    Future<RpcResult<Void>> updateKey(UpdateKeyInput input) {
        return null;
    }

    Future<RpcResult<Void>> updateKeyWithRef(UpdateKeyWithRefInput input) {
        return null;
    }

    Future<RpcResult<Void>> updateMapping(UpdateMappingInput input) {
        return null;
    }

    Future<RpcResult<Void>> updateMappingWithRef(
            UpdateMappingWithRefInput input) {
        return null;
    }

    public void setLispMappingService(LispMappingService lispMappingService) {
        this.lispMappingService = lispMappingService;
    }

}
