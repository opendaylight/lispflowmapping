/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import com.google.common.base.Preconditions;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundHandler;
import org.opendaylight.lispflowmapping.southbound.lisp.LispXtrSouthboundHandler;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundPlugin implements IConfigLispSouthboundPlugin, AutoCloseable, ClusterSingletonService {
    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundPlugin.class);
    public static final String LISPFLOWMAPPING_ENTITY_NAME = "lispflowmapping";
    public static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create(
            LISPFLOWMAPPING_ENTITY_NAME);

    private volatile String bindingAddress;
    private boolean mapRegisterCacheEnabled;
    private long mapRegisterCacheTimeout;

    private static Object startLock = new Object();
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private LispSouthboundHandler lispSouthboundHandler;
    private LispXtrSouthboundHandler lispXtrSouthboundHandler;
    private NotificationPublishService notificationPublishService;
    private NioDatagramChannel channel;
    private volatile int xtrPort = LispMessage.XTR_PORT_NUM;
    private volatile boolean listenOnXtrPort = false;
    private NioDatagramChannel xtrChannel;
    private LispSouthboundStats statistics = new LispSouthboundStats();
    private Bootstrap bootstrap = new Bootstrap();
    private Bootstrap xtrBootstrap = new Bootstrap();
    private ThreadFactory threadFactory = new DefaultThreadFactory("lisp-sb");
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(0, threadFactory);
    private DataBroker dataBroker;

    public LispSouthboundPlugin(final DataBroker dataBroker,
            final NotificationPublishService notificationPublishService,
            final ClusterSingletonServiceProvider clusterSingletonService) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.clusterSingletonService = clusterSingletonService;
        this.clusterSingletonService.registerClusterSingletonService(this);
    }

    public void init() {
        LOG.info("LISP (RFC6830) Southbound Plugin is initializing...");
        synchronized (startLock) {
            lispSouthboundHandler = new LispSouthboundHandler(this);
            lispSouthboundHandler.setDataBroker(dataBroker);
            lispSouthboundHandler.setNotificationProvider(this.notificationPublishService);
            lispSouthboundHandler.setMapRegisterCacheEnabled(mapRegisterCacheEnabled);
            lispSouthboundHandler.setMapRegisterCacheTimeout(mapRegisterCacheTimeout);
            lispSouthboundHandler.init();
            lispSouthboundHandler.restoreDaoFromDatastore();

            lispXtrSouthboundHandler = new LispXtrSouthboundHandler();
            lispXtrSouthboundHandler.setNotificationProvider(this.notificationPublishService);

            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioDatagramChannel.class);
            bootstrap.handler(lispSouthboundHandler);

            xtrBootstrap.group(eventLoopGroup);
            xtrBootstrap.channel(NioDatagramChannel.class);
            xtrBootstrap.handler(lispXtrSouthboundHandler);

            start();
            startXtr();

            LOG.info("LISP (RFC6830) Southbound Plugin is up!");
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void start() {
        try {
            channel = (NioDatagramChannel) bootstrap.bind(bindingAddress, LispMessage.PORT_NUM).sync().channel();
            LOG.debug("Binding LISP UDP listening socket to {}:{}", bindingAddress, LispMessage.PORT_NUM);
        } catch (Exception e) {
            LOG.error("Failed to open main socket ", e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void startXtr() {
        if (listenOnXtrPort) {
            try {
                xtrChannel = (NioDatagramChannel) xtrBootstrap.bind(bindingAddress, xtrPort).sync().channel();
                LOG.debug("Binding LISP xTR UDP listening socket to {}:{}", bindingAddress, xtrPort);
            } catch (Exception e) {
                LOG.error("Failed to open xTR socket ", e);
            }
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void stop() {
        try {
            channel.close().sync();
            channel = null;
        } catch (Exception e) {
            LOG.error("Failed to close main socket ", e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void stopXtr() {
        if (listenOnXtrPort) {
            try {
                xtrChannel.close().sync();
                xtrChannel = null;
            } catch (Exception e) {
                LOG.error("Failed to close xTR socket ", e);
            }
        }
    }

    private void restart() {
        LOG.info("Reloading");
        stop();
        start();
    }

    private void restartXtr() {
        LOG.info("Reloading xTR");
        stopXtr();
        startXtr();
    }

    private void unloadActions() {
        lispSouthboundHandler = null;
        lispXtrSouthboundHandler = null;

        stop();
        stopXtr();

        LOG.info("LISP (RFC6830) Southbound Plugin is down!");
    }

    public void handleSerializedLispBuffer(TransportAddress address, ByteBuffer outBuffer,
                                           final MessageType packetType) {
        InetAddress ip = getInetAddress(address);
        handleSerializedLispBuffer(ip, outBuffer, packetType, address.getPort().getValue());
    }

    public void handleSerializedLispBuffer(InetAddress address, ByteBuffer outBuffer,
            final MessageType packetType, final int portNumber) {
        InetSocketAddress recipient = new InetSocketAddress(address, portNumber);
        outBuffer.position(0);
        ByteBuf data = wrappedBuffer(outBuffer);
        DatagramPacket packet = new DatagramPacket(data, recipient);
        LOG.debug("Sending {} on port {} to address: {}", packetType, portNumber, address);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Buffer:\n{}", ByteBufUtil.prettyHexDump(data));
        }
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

    private InetAddress getInetAddress(TransportAddress address) {
        Preconditions.checkNotNull(address, "TransportAddress must not be null");
        IpAddressBinary ip = address.getIpAddress();
        try {
            if (ip.getIpv4AddressBinary() != null) {
                return InetAddress.getByAddress(ip.getIpv4AddressBinary().getValue());
            } else if (ip.getIpv6AddressBinary() != null) {
                return InetAddress.getByAddress(ip.getIpv6AddressBinary().getValue());
            }
        } catch (UnknownHostException e) {
            LOG.debug("Could not convert TransportAddress {} to InetAddress", address, e);
        }
        return null;
    }

    public LispSouthboundStats getStats() {
        return statistics;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void setLispAddress(String address) {
        synchronized (startLock) {
            if (bindingAddress.equals(address)) {
                LOG.debug("Configured LISP binding address didn't change.");
            } else {
                LOG.debug("Setting LISP binding address to {}", address);
                bindingAddress = address;
                if (channel != null) {
                    try {
                        restart();
                        restartXtr();
                    } catch (Exception e) {
                        LOG.error("Failed to set LISP binding address: ", e);
                    }
                }
            }
        }
    }

    @Override
    public void shouldListenOnXtrPort(boolean shouldListenOnXtrPort) {
        listenOnXtrPort = shouldListenOnXtrPort;
        if (listenOnXtrPort) {
            restartXtr();
        } else {
            LOG.info("Shutting down xTR");
            stopXtr();
        }
    }

    @Override
    public void setXtrPort(int port) {
        this.xtrPort = port;
        if (listenOnXtrPort) {
            restartXtr();
        }
    }

    public void setMapRegisterCacheEnabled(final boolean mapRegisterCacheEnabled) {
        this.mapRegisterCacheEnabled = mapRegisterCacheEnabled;
        if (mapRegisterCacheEnabled) {
            LOG.info("Enabling Map-Register cache");
        } else {
            LOG.info("Disabling Map-Register cache");
        }
    }

    public void setMapRegisterCacheTimeout(long mapRegisterCacheTimeout) {
        this.mapRegisterCacheTimeout = mapRegisterCacheTimeout;
    }

    public void setBindingAddress(String bindingAddress) {
        this.bindingAddress = bindingAddress;
    }

    @Override
    public void close() throws Exception {
        eventLoopGroup.shutdownGracefully();
        lispSouthboundHandler.close();
        unloadActions();
        clusterSingletonService.close();
    }

    @Override
    public void instantiateServiceInstance() {
        if (lispSouthboundHandler != null) {
            lispSouthboundHandler.setNotificationProvider(notificationPublishService);
            lispSouthboundHandler.restoreDaoFromDatastore();
            lispSouthboundHandler.setIsMaster(true);
        }
        if (lispXtrSouthboundHandler != null) {
            lispXtrSouthboundHandler.setNotificationProvider(notificationPublishService);
        }
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        if (lispSouthboundHandler != null) {
            lispSouthboundHandler.setNotificationProvider(null);
            lispSouthboundHandler.setIsMaster(false);
        }
        if (lispXtrSouthboundHandler != null) {
            lispXtrSouthboundHandler.setNotificationProvider(null);
        }
        return Futures.<Void>immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SERVICE_GROUP_IDENTIFIER;
    }

}
