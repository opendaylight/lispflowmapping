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
import static org.junit.Assert.assertNull;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundHandler;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NioDatagramChannel.class)
public class LispSouthboundPluginTest {

    private static NioDatagramChannel channelUdp;
    private static NioServerSocketChannel channelTcp;
    private static NioDatagramChannel xtrChannel;
    private static LispSouthboundPlugin lispSouthboundPlugin;
    private static final Bootstrap BOOTSTRAP_MOCK = Mockito.mock(Bootstrap.class);
    private static final ServerBootstrap BOOTSTRAP_TCP_MOCK = Mockito.mock(ServerBootstrap.class);

    private static final String LISP_MAP_REQUEST_PACKET_STRING =
            "10 00 00 01 3d 8d 2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 00 02 00 20 00 01 7f 00 00 01";
    private static final String ADDRESS_1 = "0.0.0.0";
    private static final String ADDRESS_2 = "1.1.1.1";
    private static final ByteBuffer PACKET = parseHexString(LISP_MAP_REQUEST_PACKET_STRING);
    private static final int PORT = 9999;
    private static final byte[] IPV4_BYTES = new byte[]{1, 2, 3, 4};
    private static final byte[] IPV6_BYTES = new byte[]{11, 11, 22, 22, 33, 33, 44, 44, 55, 55, 66, 66, 77, 77, 88, 88};
    private static final IpAddressBinary IPV4_BINARY = new IpAddressBinary(new Ipv4AddressBinary(IPV4_BYTES));
    private static final IpAddressBinary IPV6_BINARY = new IpAddressBinary(new Ipv6AddressBinary(IPV6_BYTES));
    private static final TransportAddress TRANSPORT_ADDRESS_IPV4 = new TransportAddressBuilder()
            .setIpAddress(IPV4_BINARY)
            .setPort(new PortNumber(PORT)).build();
    private static final TransportAddress TRANSPORT_ADDRESS_IPV6 = new TransportAddressBuilder()
            .setIpAddress(IPV6_BINARY)
            .setPort(new PortNumber(PORT)).build();

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        lispSouthboundPlugin = new LispSouthboundPlugin(
                Mockito.mock(DataBroker.class),
                Mockito.mock(NotificationPublishService.class),
                Mockito.mock(ClusterSingletonServiceProvider.class));
        lispSouthboundPlugin.setBindingAddress(ADDRESS_1);
        lispSouthboundPlugin.setMapRegisterCacheEnabled(false);

        channelUdp = PowerMockito.mock(NioDatagramChannel.class);
        channelTcp = PowerMockito.mock(NioServerSocketChannel.class);
        xtrChannel = PowerMockito.mock(NioDatagramChannel.class);
        injectChannelUdp();
        injectChannelTcp();
        injectXtrChannel();
    }

    /**
     * Tests {@link LispSouthboundPlugin#handleSerializedLispBuffer} method with ipv4.
     */
    @Test
    public void handleSerializedLispBufferTest_withIpv4() throws
            NoSuchFieldException, IllegalAccessException, UnknownHostException {
        final ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        final InetAddress address = InetAddress.getByAddress(IPV4_BINARY.getIpv4AddressBinary().getValue());
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, PORT);

        // Ensures that NPE is not thrown.
        Mockito.when(channelUdp.write(Mockito.any())).thenReturn(Mockito.mock(ChannelFuture.class));

        lispSouthboundPlugin.handleSerializedLispBuffer(TRANSPORT_ADDRESS_IPV4, PACKET, MessageType.MapRequest);
        Mockito.verify(channelUdp).write(captor.capture());
        Mockito.verify(channelUdp).flush();

        final DatagramPacket result = captor.getValue();
        assertArrayEquals(PACKET.array(), result.content().array());
        assertEquals(inetSocketAddress, result.recipient());
    }

    /**
     * Tests {@link LispSouthboundPlugin#handleSerializedLispBuffer} method with ipv6.
     */
    @Test
    public void handleSerializedLispBufferTest_withIpv6() throws
            NoSuchFieldException, IllegalAccessException, UnknownHostException {
        final ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        final InetAddress address = InetAddress.getByAddress(IPV6_BINARY.getIpv6AddressBinary().getValue());
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, PORT);

        // Ensures that NPE is not thrown.
        Mockito.when(channelUdp.write(Mockito.any())).thenReturn(Mockito.mock(ChannelFuture.class));

        lispSouthboundPlugin.handleSerializedLispBuffer(TRANSPORT_ADDRESS_IPV6, PACKET, MessageType.MapRequest);
        Mockito.verify(channelUdp).write(captor.capture());
        Mockito.verify(channelUdp).flush();

        final DatagramPacket result = captor.getValue();
        assertArrayEquals(PACKET.array(), result.content().array());
        assertEquals(inetSocketAddress, result.recipient());
    }

    /**
     * Tests {@link LispSouthboundPlugin#setLispAddress} method - binding address has changed.
     */
    @Test
    public void setLispAddressTest_withEqualAddress() throws NoSuchFieldException, IllegalAccessException {
        injectField("bootstrap", BOOTSTRAP_MOCK);
        injectField("bootstrapTcp", BOOTSTRAP_TCP_MOCK);
        Mockito.when(channelUdp.close()).thenReturn(Mockito.mock(ChannelFuture.class));
        Mockito.when(channelTcp.close()).thenReturn(Mockito.mock(ChannelFuture.class));

        lispSouthboundPlugin.setLispAddress(ADDRESS_2);

        Mockito.verify(BOOTSTRAP_MOCK).bind(ADDRESS_2, LispMessage.PORT_NUM);
        Mockito.verify(channelUdp).close();
    }

    /**
     * Tests {@link LispSouthboundPlugin#setLispAddress} method - binding address has not changed.
     */
    @Test
    public void setLispAddressTest_withChangedAddress() throws NoSuchFieldException, IllegalAccessException {
        injectField("bootstrap", BOOTSTRAP_MOCK);
        lispSouthboundPlugin.setLispAddress(ADDRESS_1);

        Mockito.verifyZeroInteractions(BOOTSTRAP_MOCK);
        Mockito.verifyZeroInteractions(channelUdp);
    }

    /**
     * Tests {@link LispSouthboundPlugin#shouldListenOnXtrPort} method, shouldListenOnXtrPort == true.
     */
    @Test
    public void shouldListenOnXtrPortTest_true() throws NoSuchFieldException, IllegalAccessException {
        lispSouthboundPlugin.shouldListenOnXtrPort(true);

        Mockito.verify(xtrChannel).close();
    }

    /**
     * Tests {@link LispSouthboundPlugin#shouldListenOnXtrPort} method, shouldListenOnXtrPort == false.
     */
    @Test
    public void shouldListenOnXtrPortTest_false() throws NoSuchFieldException, IllegalAccessException {
        lispSouthboundPlugin.shouldListenOnXtrPort(false);

        Mockito.verifyZeroInteractions(xtrChannel);
    }

    /**
     * Tests {@link LispSouthboundPlugin#setXtrPort} method.
     */
    @Test
    public void setXtrPortTest() throws NoSuchFieldException, IllegalAccessException {
        lispSouthboundPlugin.shouldListenOnXtrPort(true);
        lispSouthboundPlugin.setXtrPort(PORT);

        Mockito.verify(xtrChannel, Mockito.times(2)).close();
        assertEquals(PORT, (int) LispSouthboundPluginTest.<Integer>getField("xtrPort"));
    }

    /**
     * Tests {@link LispSouthboundPlugin#close} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void closeTest() throws Exception {
        EventLoopGroup elgMock = Mockito.mock(EventLoopGroup.class);
        LispSouthboundPluginTest.injectField("eventLoopGroupUdp", elgMock);
        LispSouthboundPluginTest.injectField("eventLoopGroupTcp", Mockito.mock(EventLoopGroup.class));
        LispSouthboundPluginTest.injectField("workerGroupTcp", Mockito.mock(EventLoopGroup.class));

        LispSouthboundHandler handlerMock = Mockito.mock(LispSouthboundHandler.class);
        LispSouthboundPluginTest.injectField("lispSouthboundHandler", handlerMock);
        Mockito.when(channelUdp.close()).thenReturn(Mockito.mock(ChannelFuture.class));
        Mockito.when(channelTcp.close()).thenReturn(Mockito.mock(ChannelFuture.class));

        lispSouthboundPlugin.close();

        Mockito.verify(channelUdp).close();
        Mockito.verify(elgMock).shutdownGracefully();
        Mockito.verify(handlerMock).close();
        assertNull(getField("lispSouthboundHandler"));
        assertNull(getField("lispXtrSouthboundHandler"));
        assertNull(getField("channelUdp"));
    }

    private static void injectChannelUdp() throws NoSuchFieldException, IllegalAccessException {
        final Field channelField = LispSouthboundPlugin.class.getDeclaredField("channelUdp");
        channelField.setAccessible(true);
        channelField.set(lispSouthboundPlugin, channelUdp);
    }

    private static void injectChannelTcp() throws NoSuchFieldException, IllegalAccessException {
        final Field channelField = LispSouthboundPlugin.class.getDeclaredField("channelTcp");
        channelField.setAccessible(true);
        channelField.set(lispSouthboundPlugin, channelTcp);
    }

    private static void injectXtrChannel() throws NoSuchFieldException, IllegalAccessException {
        final Field xtrChannelField = LispSouthboundPlugin.class.getDeclaredField("xtrChannel");
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

    private static <T> void injectField(String fieldName, T obj) throws NoSuchFieldException, IllegalAccessException {
        Field field = LispSouthboundPlugin.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(lispSouthboundPlugin, obj);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = LispSouthboundPlugin.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        return (T) field.get(lispSouthboundPlugin);
    }
}
