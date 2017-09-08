/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.integrationtest;

import static org.junit.Assert.fail;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MappingServiceIntegrationTestUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MappingServiceIntegrationTestUtil.class);
    private static final int DEFAULT_SOCKET_TIMEOUT = 6000;

    // Utility class, should not be instantiated
    private MappingServiceIntegrationTestUtil() {
    }

    /**
     * Receive a packet on a UDP socket with a set timeout and return it.
     *
     * @param datagramSocket the listening socket where we expect the packet
     * @param timeout timeout to wait for the packet to be received in milliseconds
     * @return the packet
     * @throws SocketTimeoutException when timout expires without receiving a packet on the socket
     */
    static ByteBuffer receivePacket(DatagramSocket datagramSocket, int timeout) throws SocketTimeoutException {
        try {
            byte[] buffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            LOG.trace("Waiting for packet from socket...");
            datagramSocket.setSoTimeout(timeout);
            datagramSocket.receive(receivePacket);
            LOG.trace("Received packet from socket!");
            return ByteBuffer.wrap(receivePacket.getData());
        } catch (SocketTimeoutException ste) {
            throw ste;
        } catch (Throwable t) {
            fail();
            return null;
        }
    }

    /**
     * Receive a packet on a UDP socket with a set timeout and return it.
     *
     * @param datagramSocket the listening socket where we expect the packet
     * @return the packet
     * @throws SocketTimeoutException when timout expires without receiving a packet on the socket
     */
    static ByteBuffer receivePacket(DatagramSocket datagramSocket) throws SocketTimeoutException {
        return receivePacket(datagramSocket, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Read packets on a UDP socket with a set timeout until the give type is received and return it.
     *
     * @param datagramSocket the listening socket where we expect the packet
     * @param timeout timeout to wait for the packet to be received in milliseconds
     * @param type the expected packet type
     * @return the packet
     * @throws SocketTimeoutException when timout expires without receiving a packet on the socket
     */
    static ByteBuffer receiveSpecificPacketType(DatagramSocket datagramSocket, int timeout, MessageType type)
            throws SocketTimeoutException {
        while (true) {
            ByteBuffer packet = receivePacket(datagramSocket, timeout);
            if (checkType(packet, type)) {
                return packet;
            }
        }
    }

    static MapRequest receiveMapRequest(DatagramSocket datagramSocket) throws SocketTimeoutException {
        ByteBuffer packet = receiveSpecificPacketType(datagramSocket, DEFAULT_SOCKET_TIMEOUT, MessageType.MapRequest);
        return MapRequestSerializer.getInstance().deserialize(packet, null);
    }

    static MapReply receiveMapReply(DatagramSocket datagramSocket) throws SocketTimeoutException {
        ByteBuffer packet = receiveSpecificPacketType(datagramSocket, DEFAULT_SOCKET_TIMEOUT, MessageType.MapReply);
        return MapReplySerializer.getInstance().deserialize(packet);
    }

    static MapNotify receiveMapNotify(DatagramSocket datagramSocket) throws SocketTimeoutException {
        ByteBuffer packet = receiveSpecificPacketType(datagramSocket, DEFAULT_SOCKET_TIMEOUT, MessageType.MapNotify);
        return MapNotifySerializer.getInstance().deserialize(packet);
    }

    static boolean checkType(ByteBuffer packet, MessageType type) {
        final int receivedType = ByteUtil.getUnsignedByte(packet, LispMessage.Pos.TYPE) >> 4;
        MessageType messageType = MessageType.forValue(receivedType);
        LOG.trace("Packet type: {}", messageType);
        return messageType == type;
    }
}
