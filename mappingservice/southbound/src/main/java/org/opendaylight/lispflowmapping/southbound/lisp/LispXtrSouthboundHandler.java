/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.util.LispNotificationHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrReplyMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrRequestMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispXtrSouthboundHandler extends SimpleChannelInboundHandler<DatagramPacket>
        implements ILispSouthboundService {
    private final LispSouthboundPlugin lispSbPlugin;
    protected static final Logger LOG = LoggerFactory.getLogger(LispXtrSouthboundHandler.class);

    public LispXtrSouthboundHandler(LispSouthboundPlugin lispSbPlugin) {
        this.lispSbPlugin = lispSbPlugin;
    }

    public void handlePacket(DatagramPacket packet) {
        ByteBuffer inBuffer = packet.content().nioBuffer();
        Object lispType = MessageType.forValue((int) (ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4));
        if (lispType == MessageType.MapRequest) {
            LOG.trace("Received packet of type MapRequest for xTR");
            handleMapRequest(inBuffer, packet.sender().getAddress());
        } else if (lispType ==  MessageType.MapReply) {
            LOG.trace("Received packet of type MapReply for xTR");
            handleMapReply(inBuffer);
        } else {
            LOG.warn("Received unknown packet type");
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleMapRequest(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            MapRequest request = MapRequestSerializer.getInstance().deserialize(inBuffer, sourceAddress);
            InetAddress finalSourceAddress = MapRequestUtil.selectItrRloc(request);
            if (finalSourceAddress == null) {
                throw new LispMalformedPacketException("Couldn't deserialize Map-Request, no ITR Rloc found!");
            }

            XtrRequestMappingBuilder requestMappingBuilder = new XtrRequestMappingBuilder();
            requestMappingBuilder.setMapRequest(LispNotificationHelper.convertMapRequest(request));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(
                    LispNotificationHelper.getIpAddressBinaryFromInetAddress(finalSourceAddress));
            transportAddressBuilder.setPort(new PortNumber(LispMessage.PORT_NUM));
            requestMappingBuilder.setTransportAddress(transportAddressBuilder.build());
            if (lispSbPlugin.getNotificationPublishService() != null) {
                lispSbPlugin.getNotificationPublishService().putNotification(requestMappingBuilder.build());
                LOG.trace("MapRequest was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException(
                    "Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleMapReply(ByteBuffer buffer) {
        try {
            MapReply reply = MapReplySerializer.getInstance().deserialize(buffer);

            XtrReplyMappingBuilder replyMappingBuilder = new XtrReplyMappingBuilder();
            replyMappingBuilder.setMapReply(LispNotificationHelper.convertMapReply(reply));

            if (lispSbPlugin.getNotificationPublishService() != null) {
                lispSbPlugin.getNotificationPublishService().putNotification(replyMappingBuilder.build());
                LOG.trace("MapReply was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException(
                    "Couldn't deserialize Map-Reply (len=" + buffer.capacity() + ")", re);
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Received xTR UDP packet from {}:{} with content:\n{}", msg.sender().getHostString(),
                    msg.sender().getPort(), ByteBufUtil.prettyHexDump(msg.content()));
        }
        handlePacket(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("Error on channel: " + cause, cause);
    }
}
