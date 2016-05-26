/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.mapcache.SimpleMapCache;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundStats;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address
        .address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.RequestMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container
        .MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container
        .MapRegisterCacheKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification
        .MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;

@RunWith(MockitoJUnitRunner.class)
public class LispSouthboundHandlerTest {

    @Mock(name = "lispSbPlugin") private static LispSouthboundPlugin lispSouthboundPluginMock;
    @Mock(name = "lispSbStats") private static LispSouthboundStats lispSouthboundStatsMock;
    @Mock(name = "mapRegisterCache") private static MapRegisterCache mapRegisterCacheMock;
    @Mock(name = "notificationPublishService") private static NotificationPublishService notificationPublishServiceMock;
    @Mock(name = "authenticationKeyDataListener") private static AuthenticationKeyDataListener
            authenticationKeyDataListenerMock;
    @Mock(name = "smc") private static SimpleMapCache smcMock;
    @InjectMocks private static LispSouthboundHandler lispSouthboundHandler =
            new LispSouthboundHandler(lispSouthboundPluginMock);

    private static final String IPV4_STRING_1 =      "1.2.3.4";
    private static final String IPV4_STRING_2 =      "127.0.0.1";
    private static final String IPV4_STRING_PREFIX = "/32";
    private static final byte[] IPV4_BYTE = new byte[]{127, 0, 0, 2};

    private static final long NONCE = 4435248268955932168L;
    private static final int PORT_1 = 8888;
    private static final int PORT_2 = 9999;
    private static final int PORT_3 = 56756;
    private static final int MAP_REQUEST_HEADER_LENGTH = 74;
    private static final int MAP_REGISTER_HEADER_LENGTH = 42;
    private static final int ENCAPSULATED_CONTROL_MESSAGE_HEADER_LENGTH = 42;
    private static final int MAP_REQUEST_PACKET_LENGTH = 32;
    private static final int MAP_REGISTER_PACKET_LENGTH = 64;
    private static final int ENCAPSULATED_CONTROL_MESSAGE_IPV4_PACKET_LENGTH = 64;
    private static final int ENCAPSULATED_CONTROL_MESSAGE_IPV6_PACKET_LENGTH = 84;

    /**
     * SRC: 127.0.0.1:58560 to 127.0.0.1:4342
     * LISP(Type = 8 - Encapsulated)
     * IP: 192.168.136.10 -> 153.16.254.1
     * UDP: 56756
     * LISP Type = Map-Request (1)
     * ITR-RLOC count: 0
     * Record Count: 1
     * Nonce: 0x3d8d2acd39c8d608
     * Source EID AFI: 1
     * Source EID 1.2.3.4
     * ITR-RLOC AFI=1 Address=192.168.136.10
     * Record 1: 127.0.0.1/32
     */
    private static final String MAP_REQUEST_PACKET_STRING_IPV4 =
            "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " +
            "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 " +
            "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 " +
            "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 " +
            "0040   fe 01 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d " +
            "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 " +
            "0060   00 02 00 20 00 01 7f 00 00 01 ac 4a 06 7d";

    /**
     * SRC: 127.0.0.1:58560 to 127.0.0.1:4342
     * LISP(Type = 8 - Encapsulated)
     * IP: 1111:1111:1111:1111:1111:1111:1111:1111 -> 2222:2222:2222:2222:2222:2222:2222:2222
     * UDP: 56756
     * LISP Type = Map-Request (1)
     * ITR-RLOC count: 0
     * Record Count: 1
     * Nonce: 0x3d8d2acd39c8d608
     * Source EID AFI: 1
     * Source EID 1.2.3.4
     * ITR-RLOC AFI=1 Address=192.168.136.10
     * Record 1: 127.0.0.1/32
     */
    private static final String ENCAPSULATED_CONTROL_MESSAGE_STRING_IPV6 =
            "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " +
            "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 " +
            "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 60 00 " +
            "0030   00 00 00 00 00 00 11 11 11 11 11 11 11 11 11 11 " +
            "0040	11 11 11 11 11 11 22 22 22 22 22 22 22 22 22 22 " +
            "0050	22 22 22 22 22 22 dd b4 10 f6 00 24 ef 3a 10 00 " +
            "0060	00 01 3d 8d 2a cd 39 c8 d6 08 00 01 01 02 03 04 " +
            "0070	00 01 7f 00 00 02 00 20 00 01 7f 00 00 01 2e c3 " +
            "0080	77 a9";

    /**
     * SRC: 127.0.0.1:58560 to 127.0.0.1:4342
     * LISP(Type = 8 - Encapsulated)
     * IP: 192.168.136.10 -> 153.16.254.1
     * UDP: 56756
     * LISP Type = Map-Request (1)
     * ITR-RLOC count: 0
     * Record Count: 1
     * Nonce: 0x3d8d2acd39c8d608
     * Source EID AFI: 1
     * Source EID 1.2.3.4
     * ITR-RLOC AFI=1 Address=192.168.136.10
     * Record 1: 127.0.0.1/32
     */
    private static final String ENCAPSULATED_CONTROL_MESSAGE_STRING_IPV4 =
            "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " +
            "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 " +
            "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 " +
            "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 " +
            "0040   fe 01 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d " +
            "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 " +
            "0060   00 02 00 20 00 01 7f 00 00 01 ac 4a 06 7d";

    /* IP: 192.168.136.10 -> 128.223.156.35
     UDP: 49289 -> 4342
     LISP(Type = 3 Map-Register, P=1, M=1
     Record Count: 1
     Nonce: 0
     Key ID: 0x0001
     AuthDataLength: 20 Data:
     e8:f5:0b:c5:c5:f2:b0:21:27:a8:21:41:04:f3:46:5a:a5:68:89:ec
     EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
     No-Action
     Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
     Priority/Weight: 1/100, Multicast Priority/Weight:
     255/0
    */
    private static final String MAP_REGISTER_STRING_IPV4_MERGE_ENABLED =
            "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " +
            "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df " +
            "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 05 01 00 00 " +
            "0030   00 00 00 00 00 00 00 01 00 14 0e a4 c6 d8 a4 06 " +
            "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 " +
            "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " +
            "0060   ff 00 00 05 00 01 c0 a8 88 0a";

    private static final String MAP_REGISTER_STRING_IPV4_MERGE_DISABLED =
            "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " +
            "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df " +
            "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 05 01 00 00 " +
            "0030   00 00 00 00 00 00 00 01 00 14 0e a4 c6 d8 a4 06 " +
            "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 " +
            "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " +
            "0060   ff 00 00 05 00 01 c0 a8 88 0a";

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        injectLispSouthboundPlugin();
        Mockito.when(lispSouthboundPluginMock.getStats()).thenReturn(lispSouthboundStatsMock);
    }

    /**
     * Tests {@link LispSouthboundHandler#handleEncapsulatedControlMessage} method.
     */
    @Test
    public void handleEncapsulatedControlMessageTest_ipv4() throws InterruptedException {
        final DatagramPacket packet = extractLispPacket(ENCAPSULATED_CONTROL_MESSAGE_STRING_IPV4,
                ENCAPSULATED_CONTROL_MESSAGE_HEADER_LENGTH, ENCAPSULATED_CONTROL_MESSAGE_IPV4_PACKET_LENGTH);
        final RequestMappingBuilder requestMappingBuilder = new RequestMappingBuilder()
                .setMapRequest(getDefaultMapRequestBuilder().build())
                .setTransportAddress(getDefaultTransportAddressBuilder(PORT_3));

        lispSouthboundHandler.handlePacket(packet);
        Mockito.verify(notificationPublishServiceMock).putNotification(requestMappingBuilder.build());
    }

    /**
     * Tests {@link LispSouthboundHandler#handleEncapsulatedControlMessage} method.
     */
    @Test
    public void handleEncapsulatedControlMessageTest_ipv6() throws InterruptedException {
        final DatagramPacket packet = extractLispPacket(ENCAPSULATED_CONTROL_MESSAGE_STRING_IPV6,
                ENCAPSULATED_CONTROL_MESSAGE_HEADER_LENGTH, ENCAPSULATED_CONTROL_MESSAGE_IPV6_PACKET_LENGTH);
        final RequestMappingBuilder requestMappingBuilder = new RequestMappingBuilder()
                .setMapRequest(getDefaultMapRequestBuilder().build())
                .setTransportAddress(getDefaultTransportAddressBuilder(PORT_3));

        lispSouthboundHandler.handlePacket(packet);
        Mockito.verify(notificationPublishServiceMock).putNotification(requestMappingBuilder.build());
    }

    /**
     * Tests {@link LispSouthboundHandler#handleMapRegister} method.
     */
    @Test
    public void handleMapRegisterTest_mergeEnabled() throws InterruptedException {
        final DatagramPacket packet = extractLispPacket(MAP_REGISTER_STRING_IPV4_MERGE_ENABLED,
                MAP_REGISTER_HEADER_LENGTH, MAP_REGISTER_PACKET_LENGTH);

        final AddMappingBuilder addMappingBuilder = new AddMappingBuilder()
                .setMapRegister(getDefaultMapRegisterBuilder().build())
                .setTransportAddress(getDefaultTransportAddressBuilder(PORT_3));
        lispSouthboundHandler.handlePacket(packet);
        Mockito.verifyZeroInteractions(mapRegisterCacheMock);
        //TODO Mockito.verify(notificationPublishServiceMock).putNotification(addMappingBuilder.build());
    }

    /**
     * Tests {@link LispSouthboundHandler#handleMapRequest} method.
     */
    @Test
    public void handleMapRequestTest() throws InterruptedException {
        final DatagramPacket packet = extractLispPacket(MAP_REQUEST_PACKET_STRING_IPV4, MAP_REQUEST_HEADER_LENGTH,
                MAP_REQUEST_PACKET_LENGTH);

        final RequestMappingBuilder requestMappingBuilder = new RequestMappingBuilder()
                .setMapRequest(getDefaultMapRequestBuilder().build())
                .setTransportAddress(getDefaultTransportAddressBuilder(PORT_2));

        lispSouthboundHandler.handlePacket(packet);
        Mockito.verify(notificationPublishServiceMock).putNotification(requestMappingBuilder.build());
    }

    private static DatagramPacket extractLispPacket(String packetString, int headerLength, int lispPacketLength) {
        final String[] tokens = packetString.split("\\s+");
        final ByteBuffer buffer = ByteBuffer.allocate(tokens.length);

        for (String token : tokens) {
            if (token.length() == 2) {
                buffer.put((byte) Integer.parseInt(token, 16));
            }
        }

        byte[] result = Arrays.copyOfRange(buffer.array(), headerLength, headerLength + lispPacketLength);
        final InetSocketAddress recipient = new InetSocketAddress(PORT_1);
        final InetSocketAddress sender = new InetSocketAddress(PORT_2);
        return new DatagramPacket(Unpooled.copiedBuffer(result), recipient, sender);
    }


    private static void injectLispSouthboundPlugin() throws NoSuchFieldException, IllegalAccessException {
        Field lispSBPluginField = LispSouthboundHandler.class.getDeclaredField("lispSbPlugin");
        lispSBPluginField.setAccessible(true);
        lispSBPluginField.set(lispSouthboundHandler, lispSouthboundPluginMock);
    }

    private static MapRequestBuilder getDefaultMapRequestBuilder() {
        final ItrRloc itrRloc = new ItrRlocBuilder()
                .setRloc(new RlocBuilder()
                        .setAddressType(Ipv4BinaryAfi.class)
                        .setAddress(new Ipv4BinaryBuilder()
                                .setIpv4Binary(new Ipv4AddressBinary(IPV4_BYTE)).build())
                        .build())
                .build();

        final EidItem eidItem = new EidItemBuilder()
                .setEid(LispAddressUtil.asIpv4PrefixBinaryEid(IPV4_STRING_2 + IPV4_STRING_PREFIX)).build();

        return new MapRequestBuilder()
                .setItrRloc(Lists.newArrayList(itrRloc))
                .setEidItem(Lists.newArrayList(eidItem))
                .setNonce(NONCE)
                .setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(IPV4_STRING_1)).build())
                .setAuthoritative(false)
                .setMapDataPresent(false)
                .setPitr(false)
                .setProbe(false)
                .setSmr(false)
                .setSmrInvoked(false);
    }

    private static MapRegisterBuilder getDefaultMapRegisterBuilder() {
        return new MapRegisterBuilder()
                .setProxyMapReply(true)
                .setXtrSiteIdPresent(false)
                .setMergeEnabled(true)
                .setWantMapNotify(true)
                .setNonce(0L);
    }

    private static TransportAddress getDefaultTransportAddressBuilder(int port) {
        return new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(new Ipv4AddressBinary(IPV4_BYTE)))
                .setPort(new PortNumber(port)).build();
    }

    private static MapRegisterCacheKey getDefaultMapRegisterCacheKey() {
        return new MapRegisterCacheKeyBuilder().build();
    }
}