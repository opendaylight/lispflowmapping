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
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.mapcache.AuthKeyDb;
import org.opendaylight.lispflowmapping.southbound.lisp.AuthenticationKeyDataListener;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundHandler;
import org.opendaylight.lispflowmapping.southbound.lisp.LispXtrSouthboundHandler;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterCache;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundPlugin implements IConfigLispSouthboundPlugin, AutoCloseable, ClusterSingletonService {
    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundPlugin.class);
    public static final String LISPFLOWMAPPING_ENTITY_NAME = "lispflowmapping";
    public static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create(
            LISPFLOWMAPPING_ENTITY_NAME);

    private volatile boolean isMaster = false;
    private volatile String bindingAddress;
    private AuthKeyDb akdb;
    private MapRegisterCache mapRegisterCache = new MapRegisterCache();
    private boolean mapRegisterCacheEnabled;
    private long mapRegisterCacheTimeout;

    private static Object startLock = new Object();
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private LispSouthboundHandler lispSouthboundHandler;
    private LispXtrSouthboundHandler lispXtrSouthboundHandler;
    private NotificationPublishService notificationPublishService;
    private int numChannels = 1;
    private Channel[] channel;
    private Channel xtrChannel;
    private Class channelType;
    private volatile int xtrPort = LispMessage.XTR_PORT_NUM;
    private volatile boolean listenOnXtrPort = false;
    private ConcurrentLispSouthboundStats statistics = new ConcurrentLispSouthboundStats();
    private Bootstrap bootstrap = new Bootstrap();
    private Bootstrap xtrBootstrap = new Bootstrap();
    private ThreadFactory threadFactory = new DefaultThreadFactory("lisp-sb");
    private EventLoopGroup eventLoopGroup;
    private DataBroker dataBroker;
    private AuthenticationKeyDataListener authenticationKeyDataListener;
    private DataStoreBackEnd dsbe;

    public LispSouthboundPlugin(final DataBroker dataBroker,
            final NotificationPublishService notificationPublishService,
            final ClusterSingletonServiceProvider clusterSingletonService) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.clusterSingletonService = clusterSingletonService;
        if (Epoll.isAvailable()) {
            // When lispflowmapping is under heavy load, there are usually two threads nearing 100% CPU core
            // utilization. In order to have some headroom, we reserve 3 cores for "other" tasks, and allow the
            // rest to be used for southbound packet processing, which is the most CPU intensive work done in lfm
            numChannels = Math.max(1, Runtime.getRuntime().availableProcessors() - 3);
        }
        channel = new Channel[numChannels];
    }

    public void init() {
        LOG.info("LISP (RFC6830) Southbound Plugin is initializing...");
        synchronized (startLock) {
            this.akdb = new AuthKeyDb(new HashMapDb());
            this.authenticationKeyDataListener = new AuthenticationKeyDataListener(dataBroker, akdb);
            this.dsbe = new DataStoreBackEnd(dataBroker);
            restoreDaoFromDatastore();

            LispSouthboundHandler lispSouthboundHandler = new LispSouthboundHandler(this);
            this.lispSouthboundHandler = lispSouthboundHandler;

            LispXtrSouthboundHandler lispXtrSouthboundHandler = new LispXtrSouthboundHandler(this);
            this.lispXtrSouthboundHandler = lispXtrSouthboundHandler;

            if (Epoll.isAvailable()) {
                eventLoopGroup = new EpollEventLoopGroup(numChannels, threadFactory);
                channelType = EpollDatagramChannel.class;
                bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
                LOG.debug("Using Netty Epoll for UDP sockets");
            } else {
                eventLoopGroup = new NioEventLoopGroup(0, threadFactory);
                channelType = NioDatagramChannel.class;
                LOG.debug("Using Netty I/O (non-Epoll) for UDP sockets");
            }

            bootstrap.group(eventLoopGroup);
            bootstrap.channel(channelType);
            bootstrap.handler(lispSouthboundHandler);

            xtrBootstrap.group(eventLoopGroup);
            xtrBootstrap.channel(channelType);
            xtrBootstrap.handler(lispXtrSouthboundHandler);

            start();
            startXtr();

            clusterSingletonService.registerClusterSingletonService(this);
            LOG.info("LISP (RFC6830) Southbound Plugin is up!");
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void start() {
        try {
            for (int i = 0; i < numChannels; ++i) {
                channel[i] = bootstrap.bind(bindingAddress, LispMessage.PORT_NUM).sync().channel();
            }
            LOG.debug("Binding LISP UDP listening socket to {}:{}", bindingAddress, LispMessage.PORT_NUM);
        } catch (Exception e) {
            LOG.error("Failed to open main socket ", e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void startXtr() {
        if (listenOnXtrPort) {
            try {
                xtrChannel = xtrBootstrap.bind(bindingAddress, xtrPort).sync().channel();
                LOG.debug("Binding LISP xTR UDP listening socket to {}:{}", bindingAddress, xtrPort);
            } catch (Exception e) {
                LOG.error("Failed to open xTR socket ", e);
            }
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void stop() {
        try {
            for (int i = 0; i < numChannels; ++i) {
                channel[i].close().sync();
                channel[i] = null;
            }
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

    /**
     * Restore all keys from MDSAL datastore.
     */
    public void restoreDaoFromDatastore() {
        final List<AuthenticationKey> authKeys = dsbe.getAllAuthenticationKeys();
        LOG.info("Restoring {} keys from datastore into southbound DAO", authKeys.size());

        for (AuthenticationKey authKey : authKeys) {
            final Eid key = authKey.getEid();
            final MappingAuthkey mappingAuthkey = authKey.getMappingAuthkey();
            LOG.debug("Adding authentication key '{}' with key-ID {} for {}", mappingAuthkey.getKeyString(),
                    mappingAuthkey.getKeyType(),
                    LispAddressStringifier.getString(key));
            akdb.addAuthenticationKey(key, mappingAuthkey);
        }
    }

    public void handleSerializedLispBuffer(TransportAddress address, ByteBuffer outBuffer,
                                           final MessageType packetType) {
        InetAddress ip = getInetAddress(address);
        handleSerializedLispBuffer(ip, outBuffer, packetType, address.getPort().getValue(), null);
    }

    public void handleSerializedLispBuffer(InetAddress address, ByteBuffer outBuffer,
            final MessageType packetType, final int portNumber, Channel senderChannel) {
        if (senderChannel == null) {
            senderChannel = this.channel[0];
        }
        InetSocketAddress recipient = new InetSocketAddress(address, portNumber);
        outBuffer.position(0);
        ByteBuf data = wrappedBuffer(outBuffer);
        DatagramPacket packet = new DatagramPacket(data, recipient);
        LOG.debug("Sending {} on port {} to address: {}", packetType, portNumber, address);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Buffer:\n{}", ByteBufUtil.prettyHexDump(data));
        }
        senderChannel.write(packet).addListener(new ChannelFutureListener() {
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
        senderChannel.flush();
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
        dsbe.closeTransactionChain();
    }

    @Override
    public void instantiateServiceInstance() {
        this.isMaster = true;
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        this.isMaster = false;
        return Futures.<Void>immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SERVICE_GROUP_IDENTIFIER;
    }

    public synchronized AuthKeyDb getAkdb() {
        return akdb;
    }

    public synchronized NotificationPublishService getNotificationPublishService() {
        if (isMaster) {
            return notificationPublishService;
        }
        return null;
    }

    public synchronized ConcurrentLispSouthboundStats getStats() {
        return statistics;
    }

    public synchronized DataBroker getDataBroker() {
        return dataBroker;
    }

    public synchronized AuthenticationKeyDataListener getAuthenticationKeyDataListener() {
        return authenticationKeyDataListener;
    }

    public synchronized MapRegisterCache getMapRegisterCache() {
        return mapRegisterCache;
    }

    public synchronized boolean isMapRegisterCacheEnabled() {
        return mapRegisterCacheEnabled;
    }

    public synchronized long getMapRegisterCacheTimeout() {
        return mapRegisterCacheTimeout;
    }
}
