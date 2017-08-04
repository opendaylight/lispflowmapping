/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrReplyMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrRequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

@RunWith(MockitoJUnitRunner.class)
public class LispXtrSouthboundHandlerTest {

    @Mock(name = "lispSbPluginMock") private static LispSouthboundPlugin lispSbPluginMock;
    @Mock(name = "notificationPublishService") private static NotificationPublishService notificationPublishServiceMock;
    @InjectMocks private static LispXtrSouthboundHandler handler;

    private static final String IPV4_STRING_1 =      "1.2.3.4";
    private static final String IPV4_STRING_2 =      "127.0.0.1";
    private static final String IPV4_STRING_PREFIX = "/32";

    private static final long NONCE = 4435248268955932168L;
    private static final int HEADER_LENGTH = 74;
    private static final int LISP_MAP_REQUEST_PACKET_LENGTH = 32;
    private static final int LISP_MAP_REPLY_PACKET_LENGTH = 40;
    private static final int PORT = 9999;

    /*
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
    private static final String MAP_REQUEST_PACKET_STRING =
              "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 "
            + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
            + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
            + "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 "
            + "0040   fe 01 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "
            + "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 "
            + "0060   00 02 00 20 00 01 7f 00 00 01 ac 4a 06 7d";

    private static final String MAP_REQUEST_PACKET_STRING_MALFORMED =
              "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 "
            + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
            + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
            + "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 "
            + "0040   fe 01 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "
            + "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 00 00 00 "
            + "0060   00 00 00 20 00 01 7f 00 00 01 ac 4a 06 7d";

    /*
     * SRC: 127.0.0.1:58560 to 127.0.0.1:4342
     * LISP(Type = 8 - Encapsulated)
     * IP: 192.168.136.10 -> 153.16.254.1
     * UDP: 56756
     * LISP Type = Map-Reply (2)
     * Record Count: 1
     * Nonce: 0x3d8d2acd39c8d608
     * Source EID AFI: 1
     * Source EID Mask Length: 32
     * Source EID 1.2.3.4
     * Record TTL: 32-bit Max value
     * Locator Count: 1
     * Locator Record 1: 127.0.0.1/32
     */
    private static final String MAP_REPLY_PACKET_STRING =
              "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 "
            + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
            + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
            + "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 "
            + "0040   fe 01 dd b4 10 f6 00 24 ef 3a 28 00 00 01 3d 8d "
            + "0050   2a cd 39 c8 d6 08 ff ff ff ff 01 20 10 00 00 00 "
            + "0060   00 01 01 02 03 04 00 00 00 00 00 00 00 01 fe fe "
            + "0070   fe fe 0d e3 70 40";

    /**
     * Tests {@link LispXtrSouthboundHandler#handlePacket} method with Map-Request.
     */
    @Test
    public void handlePacketTest_withMapRequest() throws InterruptedException {
        final ArgumentCaptor<XtrRequestMapping> captor = ArgumentCaptor.forClass(XtrRequestMapping.class);

        // expected result
        final MapRequest expectedRequest = getDefaultMapRequestBuilder().build();

        handler.handlePacket(extractLispPacket(MAP_REQUEST_PACKET_STRING, HEADER_LENGTH,
                LISP_MAP_REQUEST_PACKET_LENGTH));
        Mockito.verify(lispSbPluginMock).getNotificationPublishService().putNotification(captor.capture());

        assertEquals(expectedRequest, captor.getValue().getMapRequest());
    }

    /**
     * Tests {@link LispXtrSouthboundHandler#handlePacket} method with Map-Request, null NotificationPublishService.
     */
    @Test
    public void handlePacketTest_withMapRequest_withNullNotifPublishService() throws InterruptedException {
        final LispXtrSouthboundHandler handler = new LispXtrSouthboundHandler(lispSbPluginMock);
        handler.handlePacket(extractLispPacket(MAP_REQUEST_PACKET_STRING, HEADER_LENGTH,
                LISP_MAP_REQUEST_PACKET_LENGTH));
        Mockito.verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link LispXtrSouthboundHandler#handlePacket} method with Map-Request, no Itr Rlocs.
     */
    @Test(expected = LispMalformedPacketException.class)
    public void handlePacketTest__withMapRequest_withNoItrRloc() throws InterruptedException {
        handler.handlePacket(extractLispPacket(MAP_REQUEST_PACKET_STRING_MALFORMED, HEADER_LENGTH,
                LISP_MAP_REQUEST_PACKET_LENGTH));
    }

    /**
     * Tests {@link LispXtrSouthboundHandler#handlePacket} method with Map-Reply.
     */
    @Test
    public void handlePacketTest_withMapReply() throws InterruptedException {
        Mockito.when(lispSbPluginMock.getNotificationPublishService()).thenReturn(notificationPublishServiceMock);
        ArgumentCaptor<XtrReplyMapping> captor = ArgumentCaptor.forClass(XtrReplyMapping.class);
        handler.handlePacket(extractLispPacket(MAP_REPLY_PACKET_STRING, HEADER_LENGTH,
                LISP_MAP_REPLY_PACKET_LENGTH));

        Mockito.verify(lispSbPluginMock).getNotificationPublishService().putNotification(captor.capture());
        assertNotNull(captor.getValue().getMapReply());
    }

    /**
     * Tests {@link LispXtrSouthboundHandler#handlePacket} method with Map-Reply over channelRead0 method.
     */
    @Test
    public void handlePacketTest_withMapReply_withNullNotifPublishService() throws Exception {
        final LispXtrSouthboundHandler handler = new LispXtrSouthboundHandler(lispSbPluginMock);
        handler.channelRead0(Mockito.mock(ChannelHandlerContext.class),
                extractLispPacket(MAP_REPLY_PACKET_STRING, HEADER_LENGTH, LISP_MAP_REPLY_PACKET_LENGTH));

        Mockito.verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link LispXtrSouthboundHandler#channelReadComplete} method.
     */
    @Test
    public void channelReadCompleteTest() throws Exception {
        ChannelHandlerContext ctxMock = Mockito.mock(ChannelHandlerContext.class);
        handler.channelReadComplete(ctxMock);

        Mockito.verify(ctxMock).flush();
    }

    /**
     * Following test is executed for coverage-increase purpose only.
     */
    @Test
    public void otherTest() throws Exception {

        // This map-notification packet is not valid! Don't use it anywhere else.
        String mapNotificationPacket =
                  "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 "
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 "
                + "0040   fe 01 dd b4 10 f6 00 24 ef 3a 40 00 00 01 3d 8d "
                + "0050   2a cd 39 c8 d6 08 ff ff ff ff 01 20 10 00 00 00 "
                + "0060   00 01 01 02 03 04 00 00 00 00 00 00 00 01 fe fe "
                + "0070   fe fe 0d e3 70 40";

        handler.exceptionCaught(Mockito.mock(ChannelHandlerContext.class), Mockito.mock(Throwable.class));
        handler.handlePacket(extractLispPacket(mapNotificationPacket, HEADER_LENGTH, LISP_MAP_REPLY_PACKET_LENGTH));
    }

    private static DatagramPacket extractLispPacket(String packetString, int headerLength, int lispPacketLength) {
        final String[] tokens = packetString.split("\\s+");
        ByteBuffer buffer = ByteBuffer.allocate(tokens.length);

        for (String token : tokens) {
            if (token.length() == 2) {
                buffer.put((byte) Integer.parseInt(token, 16));
            }
        }

        byte[] result = Arrays.copyOfRange(buffer.array(),
                headerLength, headerLength + lispPacketLength);
        final InetAddress inetAddress = null;
        final InetSocketAddress recipient = new InetSocketAddress(PORT);
        final InetSocketAddress sender = new InetSocketAddress(inetAddress, PORT);
        return new DatagramPacket(Unpooled.copiedBuffer(result), recipient, sender);
    }

    private static MapRequestBuilder getDefaultMapRequestBuilder() {
        final ItrRloc itrRloc = new ItrRlocBuilder()
                .setRloc(new RlocBuilder()
                        .setAddressType(Ipv4BinaryAfi.class)
                        .setAddress(new Ipv4BinaryBuilder()
                                .setIpv4Binary(new Ipv4AddressBinary(new byte[]{127, 0, 0, 2})).build())
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
}
