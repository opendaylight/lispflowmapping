/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(immediate = true, property = "type=default", configurationPid = "org.opendaylight.lispflowmapping",
           service = { IConfigLispSouthboundPlugin.class, LispSouthboundPlugin.class })
@Designate(ocd = LispSouthboundPlugin.Configuration.class)
public class LispSouthboundPlugin implements IConfigLispSouthboundPlugin, AutoCloseable, ClusterSingletonService {
    @ObjectClassDefinition
    public @interface Configuration {
        @AttributeDefinition()
        String bindingAddress() default DEFAULT_BINDING_ADDRESS;

        @AttributeDefinition()
        boolean mapRegisterCacheEnabled() default true;

        @AttributeDefinition()
        long mapRegisterCacheTimeout() default DEFAULT_MAP_REGISTER_CACHE_TIMEOUT;
    }

    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundPlugin.class);
    public static final String LISPFLOWMAPPING_ENTITY_NAME = "lispflowmapping";
    public static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER =
        new ServiceGroupIdentifier(LISPFLOWMAPPING_ENTITY_NAME);

    private static final String DEFAULT_BINDING_ADDRESS = "0.0.0.0";
    private static final long DEFAULT_MAP_REGISTER_CACHE_TIMEOUT = 90000;

    private volatile boolean isMaster = false;
    private volatile String bindingAddress;
    private AuthKeyDb akdb;
    private final MapRegisterCache mapRegisterCache = new MapRegisterCache();
    private final boolean mapRegisterCacheEnabled;
    private final long mapRegisterCacheTimeout;

    private static Object startLock = new Object();

    private final DataBroker dataBroker;
    private final NotificationPublishService notificationPublishService;
    private final ClusterSingletonServiceProvider clusterSingletonService;

    private LispSouthboundHandler lispSouthboundHandler;
    private int numChannels = 1;
    private final Channel[] channel;
    private Channel xtrChannel;
    private volatile int xtrPort = LispMessage.XTR_PORT_NUM;
    private volatile boolean listenOnXtrPort = false;
    private final ConcurrentLispSouthboundStats statistics = new ConcurrentLispSouthboundStats();
    private final Bootstrap bootstrap = new Bootstrap();
    private final Bootstrap xtrBootstrap = new Bootstrap();
    private final ThreadFactory threadFactory = new DefaultThreadFactory("lisp-sb");
    private EventLoopGroup eventLoopGroup;
    private AuthenticationKeyDataListener authenticationKeyDataListener;
    private DataStoreBackEnd dsbe;
    private Registration cssReg;

    @Inject
    public LispSouthboundPlugin(final DataBroker dataBroker,
            final NotificationPublishService notificationPublishService,
            final ClusterSingletonServiceProvider clusterSingletonService) {
        this(dataBroker, notificationPublishService, clusterSingletonService, DEFAULT_BINDING_ADDRESS, true,
            DEFAULT_MAP_REGISTER_CACHE_TIMEOUT);
    }

    @Activate
    public LispSouthboundPlugin(@Reference final DataBroker dataBroker,
            @Reference final NotificationPublishService notificationPublishService,
            @Reference final ClusterSingletonServiceProvider clusterSingletonService,
            final Configuration configuration) {
        this(dataBroker, notificationPublishService, clusterSingletonService, configuration.bindingAddress(),
            configuration.mapRegisterCacheEnabled(), configuration.mapRegisterCacheTimeout());
        init();
    }

    public LispSouthboundPlugin(final DataBroker dataBroker,
            final NotificationPublishService notificationPublishService,
            final ClusterSingletonServiceProvider clusterSingletonService,
            final String bindingAddress, final boolean mapRegisterCacheEnabled, final long mapRegisterCacheTimeout) {
        LOG.info("LISP (RFC6830) Southbound Plugin is initializing...");
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.clusterSingletonService = clusterSingletonService;
        this.bindingAddress = bindingAddress;
        this.mapRegisterCacheEnabled = mapRegisterCacheEnabled;
        this.mapRegisterCacheTimeout = mapRegisterCacheTimeout;

        if (Epoll.isAvailable()) {
            // When lispflowmapping is under heavy load, there are usually two threads nearing 100% CPU core
            // utilization. In order to have some headroom, we reserve 3 cores for "other" tasks, and allow the
            // rest to be used for southbound packet processing, which is the most CPU intensive work done in lfm
            numChannels = Math.max(1, Runtime.getRuntime().availableProcessors() - 3);
        }
        channel = new Channel[numChannels];
    }

    @PostConstruct
    public void init() {
        synchronized (startLock) {
            akdb = new AuthKeyDb(new HashMapDb());
            authenticationKeyDataListener = new AuthenticationKeyDataListener(dataBroker, akdb);
            dsbe = new DataStoreBackEnd(dataBroker);
            restoreDaoFromDatastore();

            final Class<? extends DatagramChannel> channelType;
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
            lispSouthboundHandler = new LispSouthboundHandler(this);
            bootstrap.handler(lispSouthboundHandler);

            xtrBootstrap.group(eventLoopGroup);
            xtrBootstrap.channel(channelType);
            xtrBootstrap.handler(new LispXtrSouthboundHandler(this));

            start();
            startXtr();

            cssReg = clusterSingletonService.registerClusterSingletonService(this);
        }

        LOG.info("LISP (RFC6830) Southbound Plugin is up!");
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

    public void handleSerializedLispBuffer(final TransportAddress address, final ByteBuffer outBuffer,
                                           final MessageType packetType) {
        InetAddress ip = getInetAddress(address);
        handleSerializedLispBuffer(ip, outBuffer, packetType, address.getPort().getValue().toJava(), null);
    }

    public void handleSerializedLispBuffer(final InetAddress address, final ByteBuffer outBuffer,
            final MessageType packetType, final int portNumber, Channel senderChannel) {
        if (senderChannel == null) {
            senderChannel = channel[0];
        }
        InetSocketAddress recipient = new InetSocketAddress(address, portNumber);
        outBuffer.position(0);
        ByteBuf data = wrappedBuffer(outBuffer);
        DatagramPacket packet = new DatagramPacket(data, recipient);
        LOG.debug("Sending {} on port {} to address: {}", packetType, portNumber, address);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Buffer:\n{}", ByteBufUtil.prettyHexDump(data));
        }
        senderChannel.write(packet).addListener(future -> {
            if (future.isSuccess()) {
                LOG.trace("Success");
                statistics.incrementTx(packetType.getIntValue());
            } else {
                LOG.warn("Failed to send packet");
                statistics.incrementTxErrors();
            }
        });
        senderChannel.flush();
    }

    private static InetAddress getInetAddress(final TransportAddress address) {
        requireNonNull(address, "TransportAddress must not be null");
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
    public void setLispAddress(final String address) {
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
    public void shouldListenOnXtrPort(final boolean shouldListenOnXtrPort) {
        listenOnXtrPort = shouldListenOnXtrPort;
        if (listenOnXtrPort) {
            restartXtr();
        } else {
            LOG.info("Shutting down xTR");
            stopXtr();
        }
    }

    @Override
    public void setXtrPort(final int port) {
        xtrPort = port;
        if (listenOnXtrPort) {
            restartXtr();
        }
    }

    @Deactivate
    @PreDestroy
    @Override
    public void close() throws Exception {
        eventLoopGroup.shutdownGracefully();
        lispSouthboundHandler.close();
        unloadActions();
        if (cssReg != null) {
            cssReg.close();
        }
        dsbe.closeTransactionChain();
    }

    @Override
    public void instantiateServiceInstance() {
        isMaster = true;
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        isMaster = false;
        return Futures.<Void>immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SERVICE_GROUP_IDENTIFIER;
    }

    public synchronized void sendNotificationIfPossible(final Notification notification) throws InterruptedException {
        if (isMaster && notificationPublishService != null) {
            notificationPublishService.putNotification(notification);
            LOG.trace("Publishing notification: {}", notification);
        } else if (notificationPublishService == null) {
            LOG.warn("Can't publish notification because no reference to publication service exists!");
        }
    }

    public AuthKeyDb getAkdb() {
        return akdb;
    }

    public ConcurrentLispSouthboundStats getStats() {
        return statistics;
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public AuthenticationKeyDataListener getAuthenticationKeyDataListener() {
        return authenticationKeyDataListener;
    }

    public MapRegisterCache getMapRegisterCache() {
        return mapRegisterCache;
    }

    public boolean isMapRegisterCacheEnabled() {
        return mapRegisterCacheEnabled;
    }

    public long getMapRegisterCacheTimeout() {
        return mapRegisterCacheTimeout;
    }
}
