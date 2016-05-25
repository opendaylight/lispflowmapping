/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NioDatagramChannel.class)
public class LispSouthboundPluginTest {

    private static NioDatagramChannel channel = PowerMockito.mock(NioDatagramChannel.class);
    private static NioDatagramChannel xtrChannel = PowerMockito.mock(NioDatagramChannel.class);
    private static LispSouthboundPlugin lispSouthboundPlugin;

    private static final String LISP_MAP_REQUEST_PACKET_STRING =
            "10 00 00 01 3d 8d 2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 00 02 00 20 00 01 7f 00 00 01";
    private static final ByteBuffer PACKET = parseHexString(LISP_MAP_REQUEST_PACKET_STRING);
    private static final int PORT = 9999;
    private static final IpAddressBinary IPV4_BINARY =
            new IpAddressBinary(new Ipv4AddressBinary(new byte[]{1, 2, 3, 4}));
    private static final TransportAddress TRANSPORT_ADDRESS = new TransportAddressBuilder()
            .setIpAddress(IPV4_BINARY)
            .setPort(new PortNumber(PORT)).build();

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        lispSouthboundPlugin = new LispSouthboundPlugin();
        injectChannel();
        injectXtrChannel();
    }

    @Test
    public void handleSerializedLispBufferTest() throws
            NoSuchFieldException, IllegalAccessException, UnknownHostException {
        final ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        final InetAddress address = InetAddress.getByAddress(IPV4_BINARY.getIpv4AddressBinary().getValue());
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, PORT);

        // Ensures that NPE is not thrown.
        Mockito.when(channel.write(Mockito.any())).thenReturn(Mockito.mock(ChannelFuture.class));

        lispSouthboundPlugin.handleSerializedLispBuffer(TRANSPORT_ADDRESS, PACKET, MessageType.MapRequest);
        Mockito.verify(channel).write(captor.capture());

        final DatagramPacket result = captor.getValue();
        assertArrayEquals(PACKET.array(), result.content().array());
        assertEquals(inetSocketAddress, result.recipient());
    }

    private static void injectChannel() throws NoSuchFieldException, IllegalAccessException {
        Field channelField = LispSouthboundPlugin.class.getDeclaredField("channel");
        channelField.setAccessible(true);
        channelField.set(lispSouthboundPlugin, channel);
    }

    private static void injectXtrChannel() throws NoSuchFieldException, IllegalAccessException {
        Field xtrChannelField = LispSouthboundPlugin.class.getDeclaredField("xtrChannel");
        xtrChannelField.setAccessible(true);
        xtrChannelField.set(lispSouthboundPlugin, xtrChannel);
    }

    private static ByteBuffer parseHexString(String packet) {
        final String[] tokens = packet.split("\\s+");
        final ByteBuffer buffer = ByteBuffer.allocate(tokens.length);
        for (String token : tokens) {
             buffer.put((byte) Integer.parseInt(token, 16));
        }

        return buffer;
    }
}
