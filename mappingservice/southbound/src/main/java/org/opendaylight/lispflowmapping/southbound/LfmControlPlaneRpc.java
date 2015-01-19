/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LfmControlPlaneService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapNotifyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapRegisterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapRequestInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;

/**
 * This class holds all RPCs methods for LispSouthbound Plugin.
 *
 * <p>
 * @author Florin Coras (fcoras@cisco.com)
 * @author Lorand Jakab (lojakab@cisco.com)
 */

public class LfmControlPlaneRpc implements LfmControlPlaneService {

    protected static final Logger LOG = LoggerFactory.getLogger(LfmControlPlaneRpc.class);

    private final String MAP_NOTIFY = "MapNotify";
    private final String MAP_REPlY = "MapReply";
    private final String MAP_REQUEST = "MapRequest";
    private final String MAP_REGISTER = "MapRegister";
    private final LispSouthboundPlugin lispSbPlugin;

    public LfmControlPlaneRpc(LispSouthboundPlugin lispSbPlugin) {
        this.lispSbPlugin = lispSbPlugin;
    }


    @Override
    public Future<RpcResult<Void>> sendMapNotify(SendMapNotifyInput mapNotifyInput) {
        LOG.trace("sendMapNotify called!!");
        if (mapNotifyInput != null) {
            ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotifyInput.getMapNotify());
            lispSbPlugin.handleSerializedLispBuffer(mapNotifyInput.getTransportAddress(), outBuffer, MAP_NOTIFY);
        } else {
            LOG.warn("MapNotify was null");
            return Futures.immediateFuture(RpcResultBuilder.<Void> failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
    }

    @Override
    public Future<RpcResult<Void>> sendMapReply(SendMapReplyInput mapReplyInput) {
        LOG.trace("sendMapReply called!!");
        if (mapReplyInput != null) {
            ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReplyInput.getMapReply());
            lispSbPlugin.handleSerializedLispBuffer(mapReplyInput.getTransportAddress(), outBuffer, MAP_REPlY);
        } else {
            LOG.warn("MapReply was null");
            return Futures.immediateFuture(RpcResultBuilder.<Void> failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
    }

    @Override
    public Future<RpcResult<Void>> sendMapRequest(SendMapRequestInput mapRequestInput) {
        LOG.trace("sendMapRequest called!!");
        if (mapRequestInput != null) {
            ByteBuffer outBuffer = MapRequestSerializer.getInstance().serialize(mapRequestInput.getMapRequest());
            lispSbPlugin.handleSerializedLispBuffer(mapRequestInput.getTransportAddress(), outBuffer, MAP_REQUEST);
        } else {
            LOG.debug("MapRequest was null");
            return Futures.immediateFuture(RpcResultBuilder.<Void> failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
    }

    @Override
    public Future<RpcResult<Void>> sendMapRegister(SendMapRegisterInput mapRegisterInput) {
        LOG.trace("sendMapRegister called!!");
        if (mapRegisterInput != null) {
            ByteBuffer outBuffer = MapRegisterSerializer.getInstance().serialize(mapRegisterInput.getMapRegister());
            lispSbPlugin.handleSerializedLispBuffer(mapRegisterInput.getTransportAddress(), outBuffer, MAP_REGISTER);
        } else {
            LOG.debug("MapRegister was null");
            return Futures.immediateFuture(RpcResultBuilder.<Void> failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
    }
}
