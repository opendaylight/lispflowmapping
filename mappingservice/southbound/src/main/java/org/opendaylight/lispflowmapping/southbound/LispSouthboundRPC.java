/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.GetStatsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.GetStatsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.GetStatsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.OdlLispSbService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.ResetStatsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.ResetStatsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.ResetStatsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapNotifyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapNotifyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapNotifyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRegisterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRegisterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRegisterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapReplyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapReplyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.ctrl.msg.stats.ControlMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.ctrl.msg.stats.ControlMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.get.stats.output.ControlMessageStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.get.stats.output.MapRegisterCacheStatsBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds all RPCs methods for LispSouthbound Plugin.
 *
 * <p>
 * @author Florin Coras (fcoras@cisco.com)
 * @author Lorand Jakab (lojakab@cisco.com)
 */

public class LispSouthboundRPC implements OdlLispSbService {

    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundRPC.class);

    private final LispSouthboundPlugin lispSbPlugin;

    public LispSouthboundRPC(LispSouthboundPlugin lispSbPlugin) {
        this.lispSbPlugin = lispSbPlugin;
    }


    @Override
    public ListenableFuture<RpcResult<SendMapNotifyOutput>> sendMapNotify(SendMapNotifyInput mapNotifyInput) {
        LOG.trace("sendMapNotify called!!");
        if (mapNotifyInput != null) {
            ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotifyInput.getMapNotify());
            lispSbPlugin.handleSerializedLispBuffer(mapNotifyInput.getTransportAddress(), outBuffer,
                    MessageType.MapNotify);
        } else {
            LOG.warn("MapNotify was null");
            return Futures.immediateFuture(RpcResultBuilder.<SendMapNotifyOutput>failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<SendMapNotifyOutput>success(
                new SendMapNotifyOutputBuilder().build()).build());
    }

    @Override
    public ListenableFuture<RpcResult<SendMapReplyOutput>> sendMapReply(SendMapReplyInput mapReplyInput) {
        LOG.trace("sendMapReply called!!");
        if (mapReplyInput != null) {
            ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReplyInput.getMapReply());
            lispSbPlugin.handleSerializedLispBuffer(mapReplyInput.getTransportAddress(), outBuffer,
                    MessageType.MapReply);
        } else {
            LOG.warn("MapReply was null");
            return Futures.immediateFuture(RpcResultBuilder.<SendMapReplyOutput>failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<SendMapReplyOutput>success(
                new SendMapReplyOutputBuilder().build()).build());
    }

    @Override
    public ListenableFuture<RpcResult<SendMapRequestOutput>> sendMapRequest(SendMapRequestInput mapRequestInput) {
        LOG.trace("sendMapRequest called!!");
        if (mapRequestInput != null) {
            ByteBuffer outBuffer = MapRequestSerializer.getInstance().serialize(mapRequestInput.getMapRequest());
            lispSbPlugin.handleSerializedLispBuffer(mapRequestInput.getTransportAddress(), outBuffer,
                    MessageType.MapRequest);
        } else {
            LOG.debug("MapRequest was null");
            return Futures.immediateFuture(RpcResultBuilder.<SendMapRequestOutput>failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<SendMapRequestOutput>success(
                new SendMapRequestOutputBuilder().build()).build());
    }

    @Override
    public ListenableFuture<RpcResult<SendMapRegisterOutput>> sendMapRegister(SendMapRegisterInput mapRegisterInput) {
        LOG.trace("sendMapRegister called!!");
        if (mapRegisterInput != null) {
            ByteBuffer outBuffer = MapRegisterSerializer.getInstance().serialize(mapRegisterInput.getMapRegister());
            lispSbPlugin.handleSerializedLispBuffer(mapRegisterInput.getTransportAddress(), outBuffer,
                    MessageType.MapRegister);
        } else {
            LOG.debug("MapRegister was null");
            return Futures.immediateFuture(RpcResultBuilder.<SendMapRegisterOutput>failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<SendMapRegisterOutput>success(
                new SendMapRegisterOutputBuilder().build()).build());
    }

    @Override
    public ListenableFuture<RpcResult<GetStatsOutput>> getStats(GetStatsInput input) {
        LOG.trace("getStats called!!");

        RpcResultBuilder<GetStatsOutput> rpcResultBuilder;

        ConcurrentLispSouthboundStats stats = lispSbPlugin.getStats();

        if (stats == null) {
            rpcResultBuilder = RpcResultBuilder.<GetStatsOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "data-missing", "No stats found");
        } else {
            rpcResultBuilder = RpcResultBuilder.success(createGetStatsOutput(stats));
        }
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public ListenableFuture<RpcResult<ResetStatsOutput>> resetStats(ResetStatsInput input) {
        LOG.trace("resetStats called!!");

        ConcurrentLispSouthboundStats stats = lispSbPlugin.getStats();

        if (stats == null) {
            return Futures.immediateFuture(RpcResultBuilder.<ResetStatsOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "data-missing", "No stats found")
                    .build());
        } else {
            stats.resetStats();
            return Futures.immediateFuture(RpcResultBuilder.<ResetStatsOutput>success(
                    new ResetStatsOutputBuilder().build()).build());
        }
    }

    private static GetStatsOutput createGetStatsOutput(ConcurrentLispSouthboundStats stats) {
        long[] rxStats = stats.getRx();
        long[] txStats = stats.getTx();

        ControlMessageStatsBuilder cmsb = new ControlMessageStatsBuilder();
        cmsb.setRxUnknown(stats.getRxUnknown());
        cmsb.setTxErrors(stats.getTxErrors());

        List<ControlMessage> messages = new ArrayList<ControlMessage>();
        for (int i = 0; i <= ConcurrentLispSouthboundStats.MAX_LISP_TYPES; i++) {
            if (MessageType.forValue(i) == null) {
                continue;
            }
            ControlMessageBuilder cmb = new ControlMessageBuilder();
            cmb.setMsgType(MessageType.forValue(i));
            cmb.setRxCount(rxStats[i]);
            cmb.setTxCount(txStats[i]);
            messages.add(cmb.build());
        }

        cmsb.setControlMessage(messages);

        MapRegisterCacheStatsBuilder mrcsb = new MapRegisterCacheStatsBuilder();
        mrcsb.setHits(stats.getCacheHits());
        mrcsb.setMisses(stats.getCacheMisses());

        return new GetStatsOutputBuilder().setControlMessageStats(cmsb.build())
                .setMapRegisterCacheStats(mrcsb.build()).build();
    }
}
