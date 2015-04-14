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

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingOutputBuilder;
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
 * Actual implementation of the RPCs defined in lfm-mapping-database.yang and
 * referenced by {@link LispMappingService}
 *
 * @author Lorand Jakab
 *
 */
class LfmMappingDatabaseRPCs {
    protected static final Logger LOG = LoggerFactory.getLogger(LfmMappingDatabaseRPCs.class);
    protected static final String TAG_NOT_FOUND = "data-missing";

    private LispMappingService lispMappingService;

    Future<RpcResult<Void>> addKey(AddKeyInput input) {
        Preconditions.checkNotNull(input, "add-key RPC input must be not null!");
        LOG.debug("RPC received to add the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        String key = lispMappingService.getAuthenticationKey(input.getLispAddressContainer(),
                input.getMaskLength());

        if (key != null) {
            String message = "Key already exists! Please use update-key if you want to change it.";
            rpcResultBuilder = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.PROTOCOL, message);
            return Futures.immediateFuture(rpcResultBuilder.build());
        }

        lispMappingService.addAuthenticationKey(input.getLispAddressContainer(),
                input.getMaskLength(), input.getAuthkey());
        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<Void>> addMapping(AddMappingInput input) {
        Preconditions.checkNotNull(input, "add-mapping RPC input must be not null!");
        LOG.debug("RPC received to add the following mapping: " + input.toString());

        return addMapping((EidToLocatorRecord)input);
    }

    Future<RpcResult<GetKeyOutput>> getKey(GetKeyInput input) {
        Preconditions.checkNotNull(input, "get-key RPC input must be not null!");
        LOG.debug("RPC received to get the following key: " + input.toString());

        RpcResultBuilder<GetKeyOutput> rpcResultBuilder;

        String key = lispMappingService.getAuthenticationKey(input.getLispAddressContainer(), input.getMaskLength());

        if (key == null) {
            String message = "The MapServer returned a null authentication key";
            rpcResultBuilder = RpcResultBuilder.<GetKeyOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, TAG_NOT_FOUND, message);
        } else {
            rpcResultBuilder = RpcResultBuilder.success(new GetKeyOutputBuilder().setAuthkey(key));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<GetMappingOutput>> getMapping(GetMappingInput input) {
        Preconditions.checkNotNull(input, "get-mapping RPC input must be not null!");
        LOG.debug("RPC received to add get following mapping: " + input.toString());

        RpcResultBuilder<GetMappingOutput> rpcResultBuilder;

        MapRequest request = getMapRequest(input.getLispAddressContainer(), input.getMaskLength());
        MapReply reply = lispMappingService.handleMapRequest(request, false);

        if (reply == null) {
            String message = "The MapServer returned a null mapping";
            rpcResultBuilder = RpcResultBuilder.<GetMappingOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, TAG_NOT_FOUND, message);
        } else {
            rpcResultBuilder = RpcResultBuilder.success(new GetMappingOutputBuilder()
            .setEidToLocatorRecord(reply.getEidToLocatorRecord()));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<Void>> removeKey(RemoveKeyInput input) {
        Preconditions.checkNotNull(input, "remove-key RPC input must be not null!");
        LOG.debug("RPC received to remove the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        lispMappingService.removeAuthenticationKey(input.getLispAddressContainer(), input.getMaskLength());

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<Void>> removeMapping(RemoveMappingInput input) {
        Preconditions.checkNotNull(input, "remove-mapping RPC input must be not null!");
        LOG.debug("RPC received to remove the following mapping: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        lispMappingService.removeMapping(input.getLispAddressContainer(), input.getMaskLength());

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<Void>> updateKey(UpdateKeyInput input) {
        Preconditions.checkNotNull(input, "update-key RPC input must be not null!");
        LOG.debug("RPC received to update the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        String key = lispMappingService.getAuthenticationKey(input.getEid().getLispAddressContainer(),
                input.getEid().getMaskLength());

        if (key == null) {
            String message = "Key doesn't exist! Please use add-key if you want to create a new authentication key.";
            rpcResultBuilder = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.PROTOCOL, message);
            return Futures.immediateFuture(rpcResultBuilder.build());
        }

        lispMappingService.addAuthenticationKey(input.getEid().getLispAddressContainer(),
                input.getEid().getMaskLength(), input.getKey().getAuthkey());
        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    Future<RpcResult<Void>> updateMapping(UpdateMappingInput input) {
        LOG.debug("RPC received to update the following mapping: " + input.toString());
        Preconditions.checkNotNull(input, "update-mapping RPC input must be not null!");

        return addMapping((EidToLocatorRecord)input);
    }

    Future<RpcResult<Void>> addMapping(EidToLocatorRecord mapping) {
        RpcResultBuilder<Void> rpcResultBuilder = null;

        MapRegister register = getMapRegister(mapping);
        // SMR is false, since we don't want to subscribe for SMRs (that's for SB Map-Registers)
        lispMappingService.handleMapRegister(register, false);

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    MapRegister getMapRegister(EidToLocatorRecord mapping) {
        MapRegisterBuilder mrb = new MapRegisterBuilder();
        mrb.setEidToLocatorRecord(getEidToLocatorRecord(mapping));
        return mrb.build();
    }

    MapRequest getMapRequest(LispAddressContainer address, short mask) {
        MapRequestBuilder mrb = new MapRequestBuilder();
        mrb.setPitr(false);
        mrb.setEidRecord(getEidRecord(address, mask));
        return mrb.build();
    }

    List<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord> getEidToLocatorRecord(EidToLocatorRecord mapping) {
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

    List<EidRecord> getEidRecord(LispAddressContainer address, short mask) {
        EidRecordBuilder erb = new EidRecordBuilder();
        erb.setLispAddressContainer(address);
        erb.setMask(mask);

        List<EidRecord> records = new ArrayList<EidRecord>();
        records.add(erb.build());
        return records;
    }

    public void setLispMappingService(LispMappingService lispMappingService) {
        this.lispMappingService = lispMappingService;
    }
}
