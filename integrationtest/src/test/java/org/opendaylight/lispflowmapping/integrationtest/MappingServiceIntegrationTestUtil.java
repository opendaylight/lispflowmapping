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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregistermessage.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MappingServiceIntegrationTestUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MappingServiceIntegrationTestUtil.class);

    // Socket related method constants
    static final String SEND_ADDRESS = "127.0.0.1";
    static final String RECEIVE_ADDRESS = "127.0.0.2";
    static final int NUM_OF_ATTEMPTS_TO_CREATE_SOCKET = 2;
    static final int DEFAULT_SOCKET_TIMEOUT = 6000;

    // Packet creation method constants
    static final String DEFAULT_IPV4_EID_STRING = "192.0.2.1";
    static final Eid DEFAULT_IPV4_EID = LispAddressUtil.asIpv4Eid(DEFAULT_IPV4_EID_STRING);
    static final String DEFAULT_IPV4_RLOC_STRING = "172.16.0.1";
    static final Rloc DEFAULT_IPV4_RLOC = LispAddressUtil.asIpv4Rloc(DEFAULT_IPV4_RLOC_STRING);
    static final Rloc DEFAULT_IPV4_ITR_RLOC = LispAddressUtil.asIpv4Rloc(RECEIVE_ADDRESS);
    static final byte[] DEFAULT_SITE_ID_BYTES = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
    static final SiteId DEFAULT_SITE_ID = new SiteId(DEFAULT_SITE_ID_BYTES);
    static final byte[] DEFAULT_XTR_ID_BYTES = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    static final XtrId DEFAULT_XTR_ID = new XtrId(DEFAULT_XTR_ID_BYTES);

    // Utility class, should not be instantiated
    private MappingServiceIntegrationTestUtil() {
    }

    /*
     *   SOCKET RELATED METHODS
     */

    /**
     * Create and return a UDP socket listening on the given port.
     *
     * @param port listening port
     * @return the created socket
     */
    static DatagramSocket initSocket(int port) {
        for (int i=0; i < NUM_OF_ATTEMPTS_TO_CREATE_SOCKET; i++) {
            try {
                LOG.debug("Binding socket on {}:{}", RECEIVE_ADDRESS, port);
                return new DatagramSocket(new InetSocketAddress(RECEIVE_ADDRESS, port));
            } catch (SocketException e) {
                LOG.error("Can't initialize socket for {}:{}", RECEIVE_ADDRESS, port, e);
            }
        }
        fail();
        return null;
    }

    /**
     * Set the destination address and port of a UDP packet
     * @param packet the packet to be set up
     * @param port destination port
     * @throws UnknownHostException when SEND_ADDRESS cannot be converted to InetAddress
     */
    static void initPacketAddress(DatagramPacket packet, int port) throws UnknownHostException {
        packet.setAddress(InetAddress.getByName(SEND_ADDRESS));
        packet.setPort(port);
    }

    /**
     * Send a packet.
     * @param datagramSocket use this socket for sending the packet
     * @param bytesToSend the packet contents
     * @param port destination port
     */
    static void sendPacket(DatagramSocket datagramSocket, byte[] bytesToSend, int port) {
        try {
            DatagramPacket packet = new DatagramPacket(bytesToSend, bytesToSend.length);
            initPacketAddress(packet, port);
            LOG.trace("Sending packet to {}:{}", packet.getAddress(), port);
            datagramSocket.send(packet);
        } catch (Throwable t) {
            fail();
        }
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
     * Read packets on a UDP socket with a set timeout until the given type is received and return it.
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

    /**
     * Read packets on a UDP socket with a set timeout until a Map-Request is received and return it.
     *
     * @param datagramSocket the listening socket where we expect the packet
     * @return the Map-Request
     * @throws SocketTimeoutException
     */
    static MapRequest receiveMapRequest(DatagramSocket datagramSocket) throws SocketTimeoutException {
        ByteBuffer packet = receiveSpecificPacketType(datagramSocket, DEFAULT_SOCKET_TIMEOUT, MessageType.MapRequest);
        return MapRequestSerializer.getInstance().deserialize(packet, null);
    }

    /**
     * Read packets on a UDP socket with a set timeout until a Map-Reply is received and return it.
     *
     * @param datagramSocket the listening socket where we expect the packet
     * @return the Map-Reply
     * @throws SocketTimeoutException
     */
    static MapReply receiveMapReply(DatagramSocket datagramSocket) throws SocketTimeoutException {
        ByteBuffer packet = receiveSpecificPacketType(datagramSocket, DEFAULT_SOCKET_TIMEOUT, MessageType.MapReply);
        return MapReplySerializer.getInstance().deserialize(packet);
    }

    /**
     * Read packets on a UDP socket with a set timeout until a Map-Notify is received and return it.
     *
     * @param datagramSocket the listening socket where we expect the packet
     * @return the Map-Notify
     * @throws SocketTimeoutException
     */
    static MapNotify receiveMapNotify(DatagramSocket datagramSocket) throws SocketTimeoutException {
        ByteBuffer packet = receiveSpecificPacketType(datagramSocket, DEFAULT_SOCKET_TIMEOUT, MessageType.MapNotify);
        return MapNotifySerializer.getInstance().deserialize(packet);
    }

    /**
     * Check if a buffer assumed to be a LISP control packet is of the given type.
     *
     * @param packet buffer containing the packet data
     * @param type LISP control packet type
     * @return true if the packet is of the given type
     */
    static boolean checkType(ByteBuffer packet, MessageType type) {
        final int receivedType = ByteUtil.getUnsignedByte(packet, LispMessage.Pos.TYPE) >> 4;
        MessageType messageType = MessageType.forValue(receivedType);
        LOG.trace("Packet type: {}", messageType);
        return messageType == type;
    }

    /*
     * PACKETS CREATION METHODS
     *
     * In general we return "Builders" so that the caller can customize the fields, but that means it also needs to
     * call .build() on the received objects.
     */

    /* Map-Request */

    /**
     * Create a default MapRequestBuilder object.
     *
     * @param eid the requested EID
     * @return the MapRequestBuilder object
     */
    static MapRequestBuilder getDefaultMapRequestBuilder(Eid eid) {
        MapRequestBuilder mrBuilder = new MapRequestBuilder()
                .setAuthoritative(false)
                .setEidItem(new ArrayList<>())
                .setItrRloc(new ArrayList<>())
                .setMapDataPresent(true)
                .setNonce((long) 4)
                .setPitr(false)
                .setProbe(false)
                .setSmr(false)
                .setSmrInvoked(false)
                .setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.getNoAddressEid()).build())
                .setItrRloc(getDefaultItrRlocList(DEFAULT_IPV4_ITR_RLOC));

        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(eid).build());

        return mrBuilder;
    }

    /**
     * Create a default ItrRloc List.
     *
     * @param rloc the single Rloc to be added to the list
     * @return the ItrRloc List object
     */
    static List<ItrRloc> getDefaultItrRlocList(Rloc rloc) {
        if (rloc == null) {
            rloc = DEFAULT_IPV4_ITR_RLOC;
        }

        final List<ItrRloc> itrRlocList = new ArrayList<>();
        final ItrRloc itrRloc = new ItrRlocBuilder()
                .setKey(new ItrRlocKey(LispAddressStringifier.getString(rloc)))
                .setItrRlocId(LispAddressStringifier.getString(rloc))
                .setRloc(rloc).build();
        itrRlocList.add(itrRloc);

        return itrRlocList;
    }

    /* Map-Register */

    /**
     * Create a default MapRegisterBuilder object with a non-empty default LocatorRecord.
     *
     * @param eid EID for the single mapping record, if null, a default will be added
     * @return the MapRegisterBuilder object
     */
    static MapRegisterBuilder getDefaultMapRegisterBuilder(Eid eid) {
        MapRegisterBuilder mapRegisterBuilder = getDefaultMapRegisterBuilder(eid, null);
        mapRegisterBuilder.setMappingRecordItem(new ArrayList<>());
        mapRegisterBuilder.getMappingRecordItem().add(getDefaultMappingRecordItemBuilder(eid,
                DEFAULT_IPV4_RLOC).build());

        return mapRegisterBuilder;
    }

    /**
     * Create a default MapRegisterBuilder object.
     *
     * @param eid EID for the single mapping record, if null, a default will be added
     * @param rloc RLOC for the single mapping record, if null, no locator record will be added
     * @return the MapRegisterBuilder object
     */
    static MapRegisterBuilder getDefaultMapRegisterBuilder(Eid eid, Rloc rloc) {
        final MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder()
                .setProxyMapReply(true)
                .setWantMapNotify(true)
                .setKeyId((short) 0)
                .setMappingRecordItem(new ArrayList<>())
                .setMergeEnabled(true)
                .setNonce(8L)
                .setSiteId(new SiteId(DEFAULT_SITE_ID_BYTES))
                .setXtrId(new XtrId(DEFAULT_XTR_ID_BYTES))
                .setXtrSiteIdPresent(true);
        mapRegisterBuilder.getMappingRecordItem().add(getDefaultMappingRecordItemBuilder(eid, rloc).build());

        return mapRegisterBuilder;
    }

    /**
     * Create a default MappingRecordItemBuilder object.
     *
     * @param eid EID for the mapping record, if null, a default will be added
     * @param rloc RLOC for the mapping record, if null, no locator record will be added
     * @return the MappingRecordItemBuilder object
     */
    static MappingRecordItemBuilder getDefaultMappingRecordItemBuilder(Eid eid, Rloc rloc) {
        return new MappingRecordItemBuilder()
                .setMappingRecordItemId("mapping-record-item-id")
                .setKey(new MappingRecordItemKey("mapping-record-item-key"))
                .setMappingRecord(getDefaultMappingRecordBuilder(eid, rloc).build());
    }

    /**
     * Create a default MappingRecordBuilder object with a single default locator record.
     *
     * @param eid EID for the mapping record, if null, a default will be added
     * @return the MappingRecordBuilder object
     */
    static MappingRecordBuilder getDefaultMappingRecordBuilder(Eid eid) {
        return getDefaultMappingRecordBuilder(eid, DEFAULT_IPV4_RLOC);
    }

    /**
     * Create a default MappingRecordBuilder object.
     *
     * @param eid EID for the mapping record, if null, a default will be added
     * @param rloc RLOC for the mapping record, if null, no locator record will be added
     * @return the MappingRecordBuilder object
     */
    static MappingRecordBuilder getDefaultMappingRecordBuilder(Eid eid, Rloc rloc) {
        if (eid == null) {
            eid = DEFAULT_IPV4_EID;
            LOG.warn("getDefaultMappingRecordBuilder(): null EID received, using the default {}",
                    DEFAULT_IPV4_EID_STRING);
        }

        MappingRecordBuilder mrb = new MappingRecordBuilder()
                .setEid(eid)
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(false)
                .setLocatorRecord(new ArrayList<>())
                .setMapVersion((short) 0)
                .setRecordTtl(60)
                .setTimestamp(System.currentTimeMillis());

        // We want to allow for empty locator records, so we only add one if rloc is not null
        if (rloc != null) {
            mrb.getLocatorRecord().add(getDefaultLocatorBuilder(rloc).build());
        }

        return mrb;
    }

    /**
     * Create a default LocatorRecordBuilder object.
     *
     * @param rloc RLOC for the mapping record, if null, a default will be added
     * @return the LocatorRecordBuilder object
     */
    static LocatorRecordBuilder getDefaultLocatorBuilder(Rloc rloc) {
        if (rloc == null) {
            rloc = DEFAULT_IPV4_RLOC;
            LOG.warn("getDefaultLocatorBuilder(): null RLOC received, using the default {}", DEFAULT_IPV4_RLOC_STRING);
        }

        return new LocatorRecordBuilder()
                .setLocalLocator(true)
                .setMulticastPriority((short) 255)
                .setMulticastWeight((short) 0)
                .setPriority((short) 1)
                .setRlocProbed(false)
                .setRouted(true)
                .setWeight((short) 1)
                .setKey(new LocatorRecordKey(LispAddressStringifier.getString(rloc)))
                .setRloc(rloc);
    }
}
