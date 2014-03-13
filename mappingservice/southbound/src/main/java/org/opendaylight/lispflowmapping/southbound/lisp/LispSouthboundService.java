/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessageEnum;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.implementation.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.lisp.network.PacketHeader;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.AddMappingBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv6Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.RequestMappingBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundService implements ILispSouthboundService {
    private NotificationProviderService notificationProvider;
    protected static final Logger logger = LoggerFactory.getLogger(LispSouthboundService.class);

    public void setNotificationProvider(NotificationProviderService nps) {
        this.notificationProvider = nps;
    }

    public void handlePacket(DatagramPacket packet) {
        ByteBuffer inBuffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        Object lispType = LispMessageEnum.valueOf((byte) (ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4));
        if (lispType == LispMessageEnum.EncapsulatedControlMessage) {
            logger.trace("Received packet of type EncapsulatedControlMessage");
            handleEncapsulatedControlMessage(inBuffer, packet.getAddress());
        } else if (lispType == LispMessageEnum.MapRequest) {
            logger.trace("Received packet of type MapRequest");
            handleMapRequest(inBuffer);
        } else if (lispType == LispMessageEnum.MapRegister) {
            logger.trace("Received packet of type MapRegister");
            handleMapRegister(inBuffer, packet.getAddress());
        } else {
            logger.warn("Received unknown packet type");
        }
    }

    private void handleEncapsulatedControlMessage(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            extractEncapsulatedSourcePort(inBuffer);
            handleMapRequest(inBuffer);
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapRequest(ByteBuffer inBuffer) {
        try {
            MapRequest request = MapRequestSerializer.getInstance().deserialize(inBuffer);
            InetAddress finalSourceAddress = MapRequestUtil.selectItrRloc(request);
            if (finalSourceAddress == null) {
                throw new LispMalformedPacketException("Couldn't deserialize Map-Request, no ITR Rloc found!");
            }

            RequestMappingBuilder requestMappingBuilder = new RequestMappingBuilder();
            requestMappingBuilder.setMapRequest(LispNotificationHelper.convertMapRequest(request));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressFromInetAddress(finalSourceAddress));
            requestMappingBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationProvider != null) {
                notificationProvider.publish(requestMappingBuilder.build());
                logger.trace("MapRequest was published!");
            } else {
                logger.warn("Notification Provider is null!");
            }
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

    private void handleMapRegister(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(inBuffer);
            AddMappingBuilder addMappingBuilder = new AddMappingBuilder();
            addMappingBuilder.setMapRegister(LispNotificationHelper.convertMapRegister(mapRegister));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressFromInetAddress(sourceAddress));
            addMappingBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationProvider != null) {
                notificationProvider.publish(addMappingBuilder.build());
                logger.trace("MapRegister was published!");
            } else {
                logger.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Register (len=" + inBuffer.capacity() + ")", re);
        }
    }
}
