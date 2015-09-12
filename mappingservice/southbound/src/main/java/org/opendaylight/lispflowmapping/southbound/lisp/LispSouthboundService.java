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
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundStats;
import org.opendaylight.lispflowmapping.southbound.util.LispNotificationHelper;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.lisp.network.PacketHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.AddMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.GotMapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.GotMapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.RequestMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundService implements ILispSouthboundService {
    private NotificationProviderService notificationProvider;
    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundService.class);

    private final LispSouthboundPlugin lispSbPlugin;
    private LispSouthboundStats lispSbStats = null;

    public LispSouthboundService(LispSouthboundPlugin lispSbPlugin) {
        this.lispSbPlugin = lispSbPlugin;
        if (lispSbPlugin != null) {
            this.lispSbStats = lispSbPlugin.getStats();
        }
    }

    public void setNotificationProvider(NotificationProviderService nps) {
        this.notificationProvider = nps;
    }

    public void handlePacket(DatagramPacket packet) {
        ByteBuffer inBuffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        int type = ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4;
        handleStats(type);
        Object lispType = MessageType.forValue(type);
        if (lispType == MessageType.EncapsulatedControlMessage) {
            LOG.trace("Received packet of type Encapsulated Control Message");
            handleEncapsulatedControlMessage(inBuffer, packet.getAddress());
        } else if (lispType == MessageType.MapRequest) {
            LOG.trace("Received packet of type Map-Request");
            handleMapRequest(inBuffer, packet.getPort());
        } else if (lispType == MessageType.MapRegister) {
            LOG.trace("Received packet of type Map-Register");
            handleMapRegister(inBuffer, packet.getAddress(), packet.getPort());
        } else if (lispType == MessageType.MapNotify) {
            LOG.trace("Received packet of type Map-Notify");
            handleMapNotify(inBuffer, packet.getAddress(), packet.getPort());
        } else if (lispType == MessageType.MapReply) {
            LOG.trace("Received packet of type Map-Reply");
            handleMapReply(inBuffer, packet.getAddress(), packet.getPort());
        } else {
            LOG.warn("Received unknown LISP control packet (type " + ((lispType != null) ? lispType : type) + ")");
            LOG.trace("Buffer: " + ByteUtil.bytesToHex(packet.getData(), packet.getLength()));
        }
    }

    private void handleEncapsulatedControlMessage(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            handleMapRequest(inBuffer, extractEncapsulatedSourcePort(inBuffer));
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapRequest(ByteBuffer inBuffer, int port) {
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
            transportAddressBuilder.setPort(new PortNumber(port));
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

    private int extractEncapsulatedSourcePort(ByteBuffer inBuffer) {
        try {
            inBuffer.position(PacketHeader.Length.LISP_ENCAPSULATION);
            int ipType = (inBuffer.get() >> 4);
            if (ipType == 4) {
                inBuffer.position(inBuffer.position() + PacketHeader.Length.IPV4 - 1);
            } else if (ipType == 6) {
                inBuffer.position(inBuffer.position() + PacketHeader.Length.IPV6_NO_EXT - 1);
            } else {
                throw new LispMalformedPacketException("Couldn't deserialize Map-Request: inner packet has unknown IP version: " + ipType);
            }

            int encapsulatedSourcePort = inBuffer.getShort() & 0xFFFF;
            inBuffer.position(inBuffer.position() + PacketHeader.Length.UDP - 2);
            return encapsulatedSourcePort;
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapRegister(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(inBuffer);
            AddMappingBuilder addMappingBuilder = new AddMappingBuilder();
            addMappingBuilder.setMapRegister(LispNotificationHelper.convertMapRegister(mapRegister));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressFromInetAddress(sourceAddress));
            transportAddressBuilder.setPort(new PortNumber(port));
            addMappingBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationProvider != null) {
                notificationProvider.publish(addMappingBuilder.build());
                LOG.trace("MapRegister was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Register (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapNotify(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            MapNotify mapNotify = MapNotifySerializer.getInstance().deserialize(inBuffer);
            GotMapNotifyBuilder gotMapNotifyBuilder = new GotMapNotifyBuilder();
            gotMapNotifyBuilder.setMapNotify(LispNotificationHelper.convertMapNotify(mapNotify));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressFromInetAddress(sourceAddress));
            transportAddressBuilder.setPort(new PortNumber(port));
            gotMapNotifyBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationProvider != null) {
                notificationProvider.publish(gotMapNotifyBuilder.build());
                LOG.trace("MapNotify was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Notify (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapReply(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            MapReply mapReply = MapReplySerializer.getInstance().deserialize(inBuffer);
            GotMapReplyBuilder gotMapReplyBuilder = new GotMapReplyBuilder();
            gotMapReplyBuilder.setMapReply(LispNotificationHelper.convertMapReply(mapReply));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressFromInetAddress(sourceAddress));
            transportAddressBuilder.setPort(new PortNumber(port));
            gotMapReplyBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationProvider != null) {
                notificationProvider.publish(gotMapReplyBuilder.build());
                LOG.trace("MapReply was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Reply (len=" + inBuffer.capacity() + ")", re);
        }
    }

    private void handleStats(int type) {
        if (lispSbStats != null) {
            if (type <= LispSouthboundStats.MAX_LISP_TYPES) {
                lispSbStats.incrementRx(type);
            } else {
                lispSbStats.incrementRxUnknown();
            }
        }
    }
}
