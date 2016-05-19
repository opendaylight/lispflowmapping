/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address
        .address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrRequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

@RunWith(MockitoJUnitRunner.class)
public class LispXtrSouthboundHandlerTest {

    @Mock(name = "notificationPublishService") private static NotificationPublishService notificationPublishServiceMock;
    @InjectMocks private static LispXtrSouthboundHandler handler;

    private static final int MAP_REQUEST_HEADER_LENGTH = 74;
    private static final int LISP_PACKET_LENGTH = 32;
    private static final int PORT = 9999;

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
    private static final String MAP_REQUEST_PACKET_STRING =
            "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " +
            "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 " +
            "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 " +
            "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 " +
            "0040   fe 01 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d " +
            "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 " +
            "0060   00 02 00 20 00 01 7f 00 00 01 ac 4a 06 7d";

    private static final DatagramPacket MAP_REQUEST_DATAGRAM_PACKET =
            extractLispMapRequestPacket(MAP_REQUEST_PACKET_STRING);

    /**
     * 
     * @throws InterruptedException
     */
    @Test
    public void handlePacketTest() throws InterruptedException {
        final ArgumentCaptor<XtrRequestMapping> captor = ArgumentCaptor.forClass(XtrRequestMapping.class);

        // expected result
        MapRequest expectedRequest = getMapRequestBuilder().build();

        handler.handlePacket(MAP_REQUEST_DATAGRAM_PACKET);
        Mockito.verify(notificationPublishServiceMock).putNotification(captor.capture());

        assertEquals(expectedRequest.getEidItem().get(0), captor.getValue().getMapRequest().getEidItem().get(0));
        assertEquals(expectedRequest.getItrRloc().get(0), captor.getValue().getMapRequest().getItrRloc().get(0));
    }

    private static DatagramPacket extractLispMapRequestPacket(String wiresharkHex) {
        final String[] tokens = wiresharkHex.split("\\s+");
        ByteBuffer buffer = ByteBuffer.allocate(tokens.length);

        for (String token : tokens) {
            if (token.length() == 2) {
                buffer.put((byte) Integer.parseInt(token, 16));
            }
        }

        byte[] result = Arrays.copyOfRange(buffer.array(),
                MAP_REQUEST_HEADER_LENGTH, MAP_REQUEST_HEADER_LENGTH + LISP_PACKET_LENGTH);
        final InetSocketAddress address = new InetSocketAddress(PORT);
        return new DatagramPacket(Unpooled.copiedBuffer(result), address);
    }

    private static MapRequestBuilder getMapRequestBuilder() {
        final ItrRloc itrRloc = new ItrRlocBuilder()
                .setRloc(new RlocBuilder()
                        .setAddressType(Ipv4BinaryAfi.class)
                        .setAddress(new Ipv4BinaryBuilder()
                                .setIpv4Binary(new Ipv4AddressBinary(new byte[]{127, 0, 0, 2})).build())
                        .build())
                .build();

        final EidItem eidItem = new EidItemBuilder()
                .setEid(LispAddressUtil.toEid(new Ipv4Prefix("127.0.0.1/32"), null)).build();

        return new MapRequestBuilder()
                .setItrRloc(Lists.newArrayList(itrRloc))
                .setEidItem(Lists.newArrayList(eidItem));
    }
}
