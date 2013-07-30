/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.lisp.network.PacketHeader;
import org.opendaylight.lispflowmapping.southbound.serializer.LispMessage;
import org.opendaylight.lispflowmapping.southbound.serializer.LispMessageEnum;
import org.opendaylight.lispflowmapping.southbound.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.southbound.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.southbound.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.southbound.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.southbound.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;

public class LispSouthboundService implements ILispSouthboundService{
    private IMapResolver mapResolver;
    private IMapServer mapServer;

    public LispSouthboundService(IMapResolver mapResolver, IMapServer mapServer) {
        this.mapResolver = mapResolver;
        this.mapServer = mapServer;
    }

    public DatagramPacket handlePacket(DatagramPacket packet) {
        ByteBuffer inBuffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        Object lispType = LispMessageEnum.valueOf((byte) (ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4));
        if (lispType == LispMessageEnum.EncapsulatedControlMessage) {
            return handleMapRequest(inBuffer);
        } else {
            if (lispType == LispMessageEnum.MapRegister) {
                return handleMapRegister(inBuffer);
            } else {
                return null;
            }
        }
    }

    private DatagramPacket handleMapRequest(ByteBuffer inBuffer) {
        int encapsulatedSourcePort = extractEncapsulatedSourcePort(inBuffer);
        MapRequest request = MapRequestSerializer.getInstance().deserialize(inBuffer);
        MapReply mapReply = mapResolver.handleMapRequest(request);
        ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReply);

        DatagramPacket replyPacket = new DatagramPacket(outBuffer.array(), outBuffer.capacity());
        replyPacket.setPort(encapsulatedSourcePort);
        return replyPacket;
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

    private DatagramPacket handleMapRegister(ByteBuffer inBuffer) {
        MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(inBuffer);
        MapNotify mapNotify = mapServer.handleMapRegister(mapRegister);

        if (mapNotify != null) {
            ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotify);
            DatagramPacket notify = new DatagramPacket(outBuffer.array(), outBuffer.limit());
            notify.setPort(LispMessage.PORT_NUM);
            return notify;
        } else {
            return null;
        }
    }
}
