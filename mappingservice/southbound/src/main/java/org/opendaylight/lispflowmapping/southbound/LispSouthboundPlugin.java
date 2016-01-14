/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
//import java.net.InetSocketAddress;
//import java.net.SocketException;
//import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;
//import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
//import org.opendaylight.lispflowmapping.southbound.lisp.ILispSouthboundService;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundService;
import org.opendaylight.lispflowmapping.southbound.lisp.LispXtrSouthboundService;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
//import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.OdlLispSbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

public class LispSouthboundPlugin implements IConfigLispSouthboundPlugin, AutoCloseable, BindingAwareProvider {
    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundPlugin.class);

    private static Object startLock = new Object();
//    private LispIoThread lispThread;
//    private LispIoThread xtrThread;
    private LispSouthboundService lispSouthboundService;
    private LispXtrSouthboundService lispXtrSouthboundService;
    private NotificationPublishService notificationPublishService;
    private RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker broker;
//    private volatile DatagramSocket socket = null;
    private NioDatagramChannel channel;
    private volatile String bindingAddress = null;
    private volatile int xtrPort = LispMessage.XTR_PORT_NUM;
    private volatile boolean listenOnXtrPort = false;
    private BindingAwareBroker.RpcRegistration<OdlLispSbService> sbRpcRegistration;
//    private DatagramSocket xtrSocket;
    private LispSouthboundStats statistics = new LispSouthboundStats();
    private IFlowMapping lispMappingService;

    public void init() {
        LOG.info("LISP (RFC6830) southbound plugin is initializing...");
        final LispSouthboundRPC sbRpcHandler = new LispSouthboundRPC(this);

        sbRpcRegistration = rpcRegistry.addRpcImplementation(OdlLispSbService.class, sbRpcHandler);
        broker.registerProvider(this);

        synchronized (startLock) {
            lispSouthboundService = new LispSouthboundService(this);
            lispXtrSouthboundService = new LispXtrSouthboundService();
            lispSouthboundService.setNotificationProvider(this.notificationPublishService);
            lispXtrSouthboundService.setNotificationProvider(this.notificationPublishService);
            try {
                start();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
            LOG.info("LISP (RFC6830) southbound plugin is up!");
        }
    }

    private void start() throws IOException, InterruptedException {
        ThreadFactory factory = new DefaultThreadFactory("lisp-sb");
        NioEventLoopGroup group = new NioEventLoopGroup(0, factory);

        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioDatagramChannel.class);
        b.handler(lispSouthboundService);
        channel = (NioDatagramChannel) b.bind(LispMessage.PORT_NUM).sync().channel();
        LOG.info("Listening on port {} using a Netty channel created with {} executors", LispMessage.PORT_NUM,
                group.executorCount());
    }

    private void stop() throws IOException, InterruptedException {
        EventLoop loop = channel.eventLoop();
        channel.close().sync();
        channel = null;
        loop.shutdownGracefully();
    }

    public void setNotificationPublishService(NotificationPublishService notificationService) {
        this.notificationPublishService = notificationService;
    }

    public void setRpcRegistryDependency(RpcProviderRegistry rpcRegistry) {
        this.rpcRegistry = rpcRegistry;
    }

    public void setBindingAwareBroker(BindingAwareBroker broker) {
        this.broker = broker;
    }

    public void setLispMappingService(IFlowMapping lispMappingService) {
        this.lispMappingService = lispMappingService;
    }

    public IFlowMapping getLispMappingService() {
        return this.lispMappingService;
    }

    private void unloadActions() {
//        if (lispThread != null) {
//            lispThread.stopRunning();
//        }
        lispSouthboundService = null;
        lispXtrSouthboundService = null;
//        lispThread = null;
//        xtrThread = null;
        bindingAddress = null;

        try {
            stop();
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }

        LOG.info("LISP (RFC6830) southbound plugin is down!");
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
        }
    }
/*
    private class LispIoThread extends Thread {
        private volatile boolean shouldRun;
        private volatile DatagramSocket threadSocket = null;
        private volatile ILispSouthboundService service;
        private volatile boolean running;

        public LispIoThread(DatagramSocket socket, ILispSouthboundService service) {
            super("Lisp Thread");
            this.threadSocket = socket;
            this.service = service;
            shouldRun = true;
        }

        @Override
        public void run() {
            running = true;

            int lispReceiveTimeout = 1000;

            LOG.info("LISP (RFC6830) southbound plugin is running and listening on address: " + bindingAddress
                    + " port: " + threadSocket.getLocalPort());
            try {

                threadSocket.setSoTimeout(lispReceiveTimeout);
            } catch (SocketException e) {
                LOG.error("Cannot open socket on UDP port " + threadSocket.getLocalPort(), e);
                return;
            }

            while (shouldRun) {
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    threadSocket.receive(packet);
                    LOG.trace("Received a packet!");
                } catch (SocketTimeoutException ste) {
                    LOG.trace("Timed out waiting on socket", ste);
                    continue;
                } catch (IOException e) {
                    LOG.warn("IO Exception while trying to recieve packet", e);
                }
                LOG.trace(String.format("Handling packet from {%s}:{%d} (len={%d})", packet.getAddress()
                        .getHostAddress(), packet.getPort(), packet.getLength()));

                try {
                    this.service.handlePacket(packet);
                } catch (Exception e) {
                    LOG.warn("Error while handling packet", e);
                }
            }

            threadSocket.close();
            LOG.trace("Socket closed");
            running = false;
        }

        public void stopRunning() {
            shouldRun = false;
        }

        public boolean isRunning() {
            return running;
        }
    }

    public static String intToIpv4(int address) {
        return ((address >> 24) & 0xff) + "." + //
                ((address >> 16) & 0xff) + "." + //
                ((address >> 8) & 0xff) + "." + //
                ((address >> 0) & 0xff);
    }

    private void startIOThread() {
        if (socket != null) {
            while (!socket.isClosed()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
        try {
            socket = new DatagramSocket(new InetSocketAddress(bindingAddress, LispMessage.PORT_NUM));
            lispThread = new LispIoThread(socket, lispSouthboundService);
            lispThread.start();
            LOG.info("LISP (RFC6830) southbound plugin is listening for control packets!");
            if (listenOnXtrPort) {
                restartXtrThread();
            }
        } catch (SocketException e) {
            LOG.error("couldn't start socket: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private void restartXtrThread() {
        try {
            stopXtrThread();
            xtrSocket = new DatagramSocket(new InetSocketAddress(bindingAddress, xtrPort));
            xtrThread = new LispIoThread(xtrSocket, lispXtrSouthboundService);
            xtrThread.start();
            LOG.info("xTR southbound plugin is up!");
        } catch (SocketException e) {
            LOG.warn("failed to start xtr thread: {}", ExceptionUtils.getStackTrace(e));
        }
    }
*/

    public void handleSerializedLispBuffer(TransportAddress address, ByteBuffer outBuffer, final MessageType packetType) {
        InetAddress ip = InetAddresses.forString(new String(address.getIpAddress().getValue()));
        int port = address.getPort().getValue();
        handleSerializedLispBuffer(ip, port, outBuffer, packetType);
    }

    public void handleSerializedLispBuffer(InetAddress ip, int port, ByteBuffer outBuffer, final MessageType packetType) {
        InetSocketAddress recipient = new InetSocketAddress(ip, port);
        // the wrappedBuffer() method doesn't copy data, so this conversion shouldn't hurt performance
        ByteBuf data = wrappedBuffer(outBuffer.array());
        DatagramPacket packet = new DatagramPacket(data, recipient);
        LOG.trace("Sending {} on port {} to address: {}", packetType, port, ip);
        LOG.trace("Buffer:\n{}", ByteBufUtil.prettyHexDump(data));
        channel.write(packet).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    LOG.trace("Success");
                    statistics.incrementTx(packetType.getIntValue());
                } else {
                    LOG.warn("Failed to send packet");
                    statistics.incrementTxErrors();
                }
            }
        });
        channel.flush();
    }

    public LispSouthboundStats getStats() {
        return statistics;
    }

    public void setLispAddress(String address) {
        synchronized (startLock) {
            if (bindingAddress != null && bindingAddress.equals(address)) {
                LOG.trace("configured lisp binding address didn't change.");
            } else {
                String action = (bindingAddress == null ? "Setting" : "Resetting");
                LOG.trace(action + " lisp binding address to: " + address);
                bindingAddress = address;
//                if (lispThread != null) {
//                    lispThread.stopRunning();
//                    while (lispThread.isRunning()) {
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                        }
//                    }
//                }
//                stopXtrThread();
//                startIOThread();
            }
        }
    }
/*
    private void stopXtrThread() {
        if (xtrThread != null) {
            xtrThread.stopRunning();
            while (xtrThread.isRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }
*/
    @Override
    public void shouldListenOnXtrPort(boolean shouldListenOnXtrPort) {
        listenOnXtrPort = shouldListenOnXtrPort;
        if (listenOnXtrPort) {
            LOG.debug("restarting xtr thread");
            //restartXtrThread();
        } else {
            LOG.debug("terminating thread");
            //stopXtrThread();
        }
    }

    @Override
    public void setXtrPort(int port) {
        this.xtrPort = port;
        if (listenOnXtrPort) {
            //restartXtrThread();
        }
    }


    @Override
    public void close() throws Exception {
        unloadActions();
        sbRpcRegistration.close();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("LispSouthboundPlugin Provider Session Initiated");
    }
}
