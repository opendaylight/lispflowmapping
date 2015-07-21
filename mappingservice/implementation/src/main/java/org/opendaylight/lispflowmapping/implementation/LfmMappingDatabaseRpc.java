/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.util.MapServerMapResolverUtil;
import org.opendaylight.lispflowmapping.implementation.util.RPCInputConvertorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.LfmMappingDatabaseService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.UpdateMappingInput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;

/**
 * Implementation of the RPCs defined in lfm-mapping-database.yang
 *
 * @author Lorand Jakab
 * @author Florin Coras
 *
 */
public class LfmMappingDatabaseRpc implements LfmMappingDatabaseService {
    protected static final Logger LOG = LoggerFactory.getLogger(LfmMappingDatabaseRpc.class);
    private static final String NOT_FOUND_TAG = "data-missing";
    private static final String DATA_EXISTS_TAG = "data-exists";

    private LispMappingService lispMappingService;
    private DataBroker dataBroker;
    private DataStoreBackEnd dsbe;

    public LfmMappingDatabaseRpc(DataBroker dataBroker) {
        this.lispMappingService = LispMappingService.getLispMappingService();
        this.dataBroker = dataBroker;
        this.dsbe = new DataStoreBackEnd(this.dataBroker);
        LOG.debug("LfmMappingDatabaseProviderRpc created!");
    }

    @Override
    public Future<RpcResult<Void>> addKey(AddKeyInput input) {
        Preconditions.checkNotNull(input, "add-key RPC input must be not null!");
        LOG.debug("RPC received to add the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        String key = lispMappingService.getAuthenticationKey(input.getLispAddressContainer(),
                input.getMaskLength());

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
        LOG.debug("RPC received to add the following mapping: " + input.toString());

        dsbe.addMapping(RPCInputConvertorUtil.toMapping(input));

        RpcResultBuilder<Void> rpcResultBuilder;

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<GetKeyOutput>> getKey(GetKeyInput input) {
        Preconditions.checkNotNull(input, "get-key RPC input must be not null!");
        LOG.debug("RPC received to get the following key: " + input.toString());

        RpcResultBuilder<GetKeyOutput> rpcResultBuilder;

        String key = lispMappingService.getAuthenticationKey(input.getLispAddressContainer(), input.getMaskLength());

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
        LOG.debug("RPC received to get the following mapping: " + input.toString());

        RpcResultBuilder<GetMappingOutput> rpcResultBuilder;

        MapRequest request = MapServerMapResolverUtil.getMapRequest(input.getLispAddressContainer(),
                input.getMaskLength());
        MapReply reply = lispMappingService.handleMapRequest(request, false);

        if (reply == null) {
            String message = "No mapping was found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetMappingOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, NOT_FOUND_TAG, message);
        } else {
            rpcResultBuilder = RpcResultBuilder.success(new GetMappingOutputBuilder()
            .setEidToLocatorRecord(reply.getEidToLocatorRecord()));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeKey(RemoveKeyInput input) {
        Preconditions.checkNotNull(input, "remove-key RPC input must be not null!");
        LOG.debug("RPC received to remove the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.removeAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeMapping(RemoveMappingInput input) {
        Preconditions.checkNotNull(input, "remove-mapping RPC input must be not null!");
        LOG.debug("RPC received to remove the following mapping: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.removeMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> updateKey(UpdateKeyInput input) {
        Preconditions.checkNotNull(input, "update-key RPC input must be not null!");
        LOG.debug("RPC received to update the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        String key = lispMappingService.getAuthenticationKey(input.getEid().getLispAddressContainer(),
                input.getEid().getMaskLength());

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
        LOG.debug("RPC received to update the following mapping: " + input.toString());
        Preconditions.checkNotNull(input, "update-mapping RPC input must be not null!");

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.updateMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    public void setLispMappingService(LispMappingService lispMappingService) {
        this.lispMappingService = lispMappingService;
    }
}
