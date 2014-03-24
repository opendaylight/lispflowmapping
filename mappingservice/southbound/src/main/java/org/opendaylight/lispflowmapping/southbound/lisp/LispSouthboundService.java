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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessageEnum;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.lisp.network.PacketHeader;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.AddMappingBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv6Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.RequestMappingBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
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
        int type = ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4;
        Object lispType = LispMessageEnum.valueOf((byte) (type));
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
            logger.warn("Received unknown LISP control packet (type " + ((lispType != null) ? lispType : type) + ")");
            logger.trace("Buffer: " + ByteUtil.bytesToHex(packet.getData(), packet.getLength()));
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
            InetAddress finalSourceAddress = null;
            for (ItrRloc itr : request.getItrRloc()) {
                Address addr = itr.getLispAddressContainer().getAddress();
                if (addr instanceof LispIpv4Address) {
                    try {
                        finalSourceAddress = InetAddress.getByName(((LispIpv4Address) addr).getIpv4Address().getValue());
                    } catch (UnknownHostException e) {
                    }
                    break;
                }
                if (addr instanceof LispIpv6Address) {
                    try {
                        finalSourceAddress = InetAddress.getByName((((LispIpv6Address) addr).getIpv6Address().getValue()));
                    } catch (UnknownHostException e) {
                    }
                    break;
                }
            }
            if (finalSourceAddress == null) {
                throw new LispMalformedPacketException("Couldn't deserialize Map-Request, no ITR Rloc found!");
            }

            RequestMappingBuilder requestMappingBuilder = new RequestMappingBuilder();
            requestMappingBuilder.setMapRequest(LispNotificationHelper.convertMapRequest(request));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressFromInetAddress(finalSourceAddress));
            transportAddressBuilder.setPort(new PortNumber(LispMessage.PORT_NUM));
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
            transportAddressBuilder.setPort(new PortNumber(LispMessage.PORT_NUM));
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
