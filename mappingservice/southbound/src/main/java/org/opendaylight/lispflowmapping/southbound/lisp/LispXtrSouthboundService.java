/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.serializer.LispMessage;
import org.opendaylight.lispflowmapping.serializer.LispMessageEnum;
import org.opendaylight.lispflowmapping.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.XtrReplyMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.XtrRequestMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispXtrSouthboundService implements ILispSouthboundService {
    private NotificationProviderService notificationProvider;
    protected static final Logger LOG = LoggerFactory.getLogger(LispXtrSouthboundService.class);

    public void setNotificationProvider(NotificationProviderService nps) {
        this.notificationProvider = nps;
    }

    public void handlePacket(DatagramPacket packet) {
        ByteBuffer inBuffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        Object lispType = LispMessageEnum.valueOf((byte) (ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4));
        if (lispType == LispMessageEnum.MapRequest) {
            LOG.trace("Received packet of type MapRequest for xTR");
            handleMapRequest(inBuffer);
        } else if (lispType ==  LispMessageEnum.MapReply){
            LOG.trace("Received packet of type MapReply for xTR");
            handleMapReply(inBuffer);
        } else {
            LOG.warn("Received unknown packet type");
        }
    }

    private void handleMapRequest(ByteBuffer inBuffer) {
        try {
            MapRequest request = MapRequestSerializer.getInstance().deserialize(inBuffer);
            InetAddress finalSourceAddress = MapRequestUtil.selectItrRloc(request);
            if (finalSourceAddress == null) {
                throw new LispMalformedPacketException("Couldn't deserialize Map-Request, no ITR Rloc found!");
            }

            XtrRequestMappingBuilder requestMappingBuilder = new XtrRequestMappingBuilder();
            requestMappingBuilder.setMapRequest(LispNotificationHelper.convertMapRequest(request));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressFromInetAddress(finalSourceAddress));
            transportAddressBuilder.setPort(new PortNumber(LispMessage.PORT_NUM));
            requestMappingBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationProvider != null) {
                notificationProvider.publish(requestMappingBuilder.build());
                LOG.trace("MapRequest was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapReply(ByteBuffer buffer) {
        try {
            MapReply reply = MapReplySerializer.getInstance().deserialize(buffer);

            XtrReplyMappingBuilder replyMappingBuilder = new XtrReplyMappingBuilder();
            replyMappingBuilder.setMapReply(LispNotificationHelper.convertMapReply(reply));

            if (notificationProvider != null) {
                notificationProvider.publish(replyMappingBuilder.build());
                LOG.trace("MapReply was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Reply (len=" + buffer.capacity() + ")", re);
        }

    }
}
