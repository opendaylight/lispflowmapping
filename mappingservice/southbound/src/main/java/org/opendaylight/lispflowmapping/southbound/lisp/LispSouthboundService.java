/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
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
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessageEnum;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.lisp.network.PacketHeader;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIPAddress;
import org.opendaylight.lispflowmapping.type.sbplugin.MapRegisterNotification;
import org.opendaylight.lispflowmapping.type.sbplugin.MapRequestNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundService implements ILispSouthboundService {
    private IMapResolver mapResolver;
    private IMapServer mapServer;
    private NotificationProviderService notificationProvider;
    protected static final Logger logger = LoggerFactory.getLogger(LispSouthboundService.class);

    public LispSouthboundService(IMapResolver mapResolver, IMapServer mapServer) {
        this.mapResolver = mapResolver;
        this.mapServer = mapServer;
    }

    public void setNotificationProvider(NotificationProviderService nps) {
        this.notificationProvider = nps;
    }

    public DatagramPacket handlePacket(DatagramPacket packet) {
        ByteBuffer inBuffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        Object lispType = LispMessageEnum.valueOf((byte) (ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4));
        if (lispType == LispMessageEnum.EncapsulatedControlMessage) {
            logger.debug("Recieved packet of type EncapsulatedControlMessage");
            return handleEncapsulatedControlMessage(inBuffer, packet.getAddress());
        } else if (lispType == LispMessageEnum.MapRequest) {
            logger.debug("Recieved packet of type MapRequest");
            return handleMapRequest(inBuffer, packet.getAddress());
        } else if (lispType == LispMessageEnum.MapRegister) {
            logger.debug("Recieved packet of type MapRegister");
            return handleMapRegister(inBuffer, packet.getAddress());
        }
        logger.debug("Recieved unknown packet type");
        return null;
    }

    private DatagramPacket handleEncapsulatedControlMessage(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            extractEncapsulatedSourcePort(inBuffer);
            DatagramPacket replyPacket = handleMapRequest(inBuffer, sourceAddress);
            return replyPacket;
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private DatagramPacket handleMapRequest(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            MapRequest request = MapRequestSerializer.getInstance().deserialize(inBuffer);
            InetAddress finalSourceAddress = sourceAddress;
            for (LispAddress address : request.getItrRlocs()) {
                if (address instanceof LispIPAddress) {
                    finalSourceAddress = (((LispIPAddress) address).getAddress());
                }
            }

            MapRequestNotification requestNotification = new MapRequestNotification(request, finalSourceAddress);
            if (notificationProvider != null) {
                notificationProvider.publish(requestNotification);
                logger.info("MapRequest was published!");
            } else {
                logger.error("Notification Provider is null!");
            }
            //MapReply mapReply = mapResolver.handleMapRequest(request);
            //ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReply);
            return null;

            /*DatagramPacket replyPacket = new DatagramPacket(outBuffer.array(), outBuffer.capacity());
            replyPacket.setPort(LispMessage.PORT_NUM);
            for (LispAddress address : request.getItrRlocs()) {
                if (address instanceof LispIPAddress) {
                    replyPacket.setAddress(((LispIPAddress) address).getAddress());
                    return replyPacket;
                }
            }*/
            //  throw new LispMalformedPacketException("No IP address in the ITR's RLOC's");
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private int extractEncapsulatedSourcePort(ByteBuffer inBuffer) {
        try {
            inBuffer.position(PacketHeader.Length.LISP_ENCAPSULATION);
            int ipType = (inBuffer.get() >> 4);
            if (ipType == 4) {
                inBuffer.position(inBuffer.position() + PacketHeader.Length.IPV4 - 1);
            } else {
                inBuffer.position(inBuffer.position() + PacketHeader.Length.IPV6_NO_EXT - 1);
            }

            int encapsulatedSourcePort = inBuffer.getShort() & 0xFFFF;
            inBuffer.position(inBuffer.position() + PacketHeader.Length.UDP - 2);
            return encapsulatedSourcePort;
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private DatagramPacket handleMapRegister(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(inBuffer);
            MapRegisterNotification registerNotification = new MapRegisterNotification(mapRegister, sourceAddress);
            if (notificationProvider != null) {
                notificationProvider.publish(registerNotification);
                logger.info("MapRegister was published!");
            } else {
                logger.error("Notification Provider is null!");
            }
            /*MapNotify mapNotify = mapServer.handleMapRegister(mapRegister);

            if (mapNotify != null) {
                ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotify);
                DatagramPacket notify = new DatagramPacket(outBuffer.array(), outBuffer.limit());
                notify.setPort(LispMessage.PORT_NUM);
                return notify;
            } else {
                logger.debug("MapNotify was null");
                return null;
            }*/
            return null;
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Register (len=" + inBuffer.capacity() + ")", re);
        }
    }
}
