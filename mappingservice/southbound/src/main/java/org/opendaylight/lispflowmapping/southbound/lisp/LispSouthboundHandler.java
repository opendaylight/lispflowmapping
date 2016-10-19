/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.lisp.authentication.ILispAuthentication;
import org.opendaylight.lispflowmapping.lisp.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.mapcache.SimpleMapCache;
import org.opendaylight.lispflowmapping.southbound.ConcurrentLispSouthboundStats;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterCache;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterPartialDeserializer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.lisp.network.PacketHeader;
import org.opendaylight.lispflowmapping.southbound.util.LispNotificationHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MappingKeepAlive;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MappingKeepAliveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.RequestMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.MapRegisterCacheMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.MapRegisterCacheMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.map.register.cache.metadata.EidLispAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.map.register.cache.metadata.EidLispAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class LispSouthboundHandler extends ChannelInboundHandlerAdapter
        implements ILispSouthboundService, AutoCloseable {
    private MapRegisterCache mapRegisterCache;
    private long mapRegisterCacheTimeout;

    private DataBroker dataBroker;
    private NotificationPublishService notificationPublishService;

    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundHandler.class);

    //TODO: think whether this field can be accessed through mappingservice or some other configuration parameter
    private boolean authenticationEnabled = true;
    private final LispSouthboundPlugin lispSbPlugin;
    private ConcurrentLispSouthboundStats lispSbStats = null;
    private SimpleMapCache smc;
    private AuthenticationKeyDataListener authenticationKeyDataListener;
    private DataStoreBackEnd dsbe;
    private boolean isReadFromChannelEnabled = true;

    public LispSouthboundHandler(LispSouthboundPlugin lispSbPlugin) {
        this.lispSbPlugin = lispSbPlugin;
    }

    @Override
    public void handlePacket(DatagramPacket packet) {
        handlePacket(packet.sender(),packet.content().nioBuffer());
    }

    public void handlePacket(final InetSocketAddress sender, final ByteBuffer inBuffer) {
        int type = ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4;
        handleStats(type);
        Object lispType = MessageType.forValue(type);
        if (lispType == MessageType.EncapsulatedControlMessage) {
            LOG.trace("Received packet of type Encapsulated Control Message");
            handleEncapsulatedControlMessage(inBuffer, sender.getAddress());
        } else if (lispType == MessageType.MapRequest) {
            LOG.trace("Received packet of type Map-Request");
            handleMapRequest(inBuffer, sender.getAddress(), sender.getPort());
        } else if (lispType == MessageType.MapRegister) {
            LOG.trace("Received packet of type Map-Register");
            handleMapRegister(inBuffer, sender.getAddress(), sender.getPort());
        } else if (lispType == MessageType.MapNotify) {
            LOG.trace("Received packet of type Map-Notify");
            handleMapNotify(inBuffer, sender.getAddress(), sender.getPort());
        } else if (lispType == MessageType.MapReply) {
            LOG.trace("Received packet of type Map-Reply");
            handleMapReply(inBuffer, sender.getAddress(), sender.getPort());
        } else {
            LOG.warn("Received unknown LISP control packet (type " + ((lispType != null) ? lispType : type) + ")");
        }
    }

    public void handleUdpPacket(DatagramPacket msg) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Received UDP packet from {}:{} with content:\n{}", msg.sender().getHostString(),
                    msg.sender().getPort(), ByteBufUtil.prettyHexDump(msg.content()));
        }
        handlePacket(msg.sender() ,msg.content().nioBuffer());
    }


    public void handleTcpPacket(final InetSocketAddress sender, final ByteBuf msg) {
        LOG.debug("Processing of packet which was received via TCP");
        handlePacket(sender,msg.nioBuffer());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleEncapsulatedControlMessage(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            handleMapRequest(inBuffer, sourceAddress, extractEncapsulatedSourcePort(inBuffer));
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len="
                    + inBuffer.capacity() + ")", re);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleMapRequest(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            MapRequest request = MapRequestSerializer.getInstance().deserialize(inBuffer, sourceAddress);
            InetAddress finalSourceAddress = MapRequestUtil.selectItrRloc(request);
            if (finalSourceAddress == null) {
                throw new LispMalformedPacketException("Couldn't deserialize Map-Request, no ITR Rloc found!");
            }

            RequestMappingBuilder requestMappingBuilder = new RequestMappingBuilder();
            requestMappingBuilder.setMapRequest(LispNotificationHelper.convertMapRequest(request));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(
                    LispNotificationHelper.getIpAddressBinaryFromInetAddress(finalSourceAddress));
            transportAddressBuilder.setPort(new PortNumber(port));
            requestMappingBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationPublishService != null) {
                notificationPublishService.putNotification(requestMappingBuilder.build());
                LOG.trace("MapRequest was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len="
                    + inBuffer.capacity() + ")", re);
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private int extractEncapsulatedSourcePort(ByteBuffer inBuffer) {
        try {
            inBuffer.position(PacketHeader.Length.LISP_ENCAPSULATION);
            int ipType = (inBuffer.get() >> 4);
            if (ipType == 4) {
                inBuffer.position(inBuffer.position() + PacketHeader.Length.IPV4 - 1);
            } else if (ipType == 6) {
                inBuffer.position(inBuffer.position() + PacketHeader.Length.IPV6_NO_EXT - 1);
            } else {
                throw new LispMalformedPacketException(
                        "Couldn't deserialize Map-Request: inner packet has unknown IP version: " + ipType);
            }

            int encapsulatedSourcePort = inBuffer.getShort() & 0xFFFF;
            inBuffer.position(inBuffer.position() + PacketHeader.Length.UDP - 2);
            return encapsulatedSourcePort;
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len="
                    + inBuffer.capacity() + ")", re);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleMapRegister(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            Map.Entry<MapRegisterCacheKey, byte[]> artificialEntry = null;
            MapRegisterCacheKey cacheKey = null;
            MapRegisterCacheValue cacheValue = null;
            if (lispSbPlugin.isMapRegisterCacheEnabled()) {
                artificialEntry = MapRegisterPartialDeserializer.deserializePartially(inBuffer, sourceAddress);
                cacheKey = artificialEntry == null ? null : artificialEntry.getKey();
                cacheValue = resolveCacheValue(artificialEntry);
            }
            if (cacheValue != null) {
                MapRegisterCacheMetadata mapRegisterMeta = cacheValue.getMapRegisterCacheMetadata();
                LOG.debug("Map register message site-ID: {} xTR-ID: {} from cache.", mapRegisterMeta.getSiteId(),
                        mapRegisterMeta.getXtrId());
                cacheValue = refreshEntry(cacheKey);
                if (cacheValue != null) {
                    sendNotificationIfPossible(createMappingKeepAlive(cacheValue));
                    if (cacheValue.getMapRegisterCacheMetadata().isWantMapNotify()) {
                        sendMapNotifyMsg(inBuffer, sourceAddress, port, cacheValue);
                    }
                }
                lispSbStats.incrementCacheHits();
            } else {
                MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(inBuffer, sourceAddress);
                final MappingAuthkey mappingAuthkey = tryToAuthenticateMessage(mapRegister, inBuffer);
                if (mappingAuthkey != null) {
                    AddMappingBuilder addMappingBuilder = new AddMappingBuilder();
                    addMappingBuilder.setMapRegister(LispNotificationHelper.convertMapRegister(mapRegister));
                    TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
                    transportAddressBuilder.setIpAddress(LispNotificationHelper.getIpAddressBinaryFromInetAddress(
                            sourceAddress));
                    transportAddressBuilder.setPort(new PortNumber(port));
                    addMappingBuilder.setTransportAddress(transportAddressBuilder.build());
                    sendNotificationIfPossible(addMappingBuilder.build());
                    if (artificialEntry != null) {
                        final MapRegisterCacheMetadataBuilder cacheMetadataBldNew = new
                                MapRegisterCacheMetadataBuilder();
                        cacheMetadataBldNew.setEidLispAddress(provideEidPrefixesFromMessage(mapRegister));
                        cacheMetadataBldNew.setXtrId(mapRegister.getXtrId());
                        cacheMetadataBldNew.setSiteId(mapRegister.getSiteId());
                        cacheMetadataBldNew.setWantMapNotify(mapRegister.isWantMapNotify());
                        cacheMetadataBldNew.setMergeEnabled(mapRegister.isMergeEnabled());
                        cacheMetadataBldNew.setTimestamp(System.currentTimeMillis());

                        final MapRegisterCacheValueBuilder cacheValueBldNew = new MapRegisterCacheValueBuilder();
                        cacheValueBldNew.setPacketData(artificialEntry.getValue());
                        cacheValueBldNew.setMappingAuthkey(mappingAuthkey);
                        cacheValueBldNew.setMapRegisterCacheMetadata(cacheMetadataBldNew.build());

                        mapRegisterCache.addEntry(cacheKey, cacheValueBldNew.build());
                    }
                }
                lispSbStats.incrementCacheMisses();
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Register (len="
                    + inBuffer.capacity() + ")", re);
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    private MapRegisterCacheValue refreshEntry(final MapRegisterCacheKey cacheKey) {
        MapRegisterCacheValue mapRegisterCacheValue = mapRegisterCache.refreshEntry(cacheKey);
        if (mapRegisterCacheValue != null) {
            mapRegisterCacheValue = refreshAuthKeyIfNecessary(mapRegisterCacheValue);
            mapRegisterCache.addEntry(cacheKey, mapRegisterCacheValue);
            return mapRegisterCacheValue;
        }
        return null;
    }

    private MapRegisterCacheValue refreshAuthKeyIfNecessary(MapRegisterCacheValue mapRegisterCacheValue) {
        if (authenticationKeyDataListener.isAuthKeyRefreshing()) {
            final boolean shouldAuthKeyRefreshingStop = System.currentTimeMillis() - authenticationKeyDataListener
                    .getAuthKeyRefreshingDate() > mapRegisterCacheTimeout;
            if (shouldAuthKeyRefreshingStop) {
                authenticationKeyDataListener.setAuthKeyRefreshing(false);
            } else {
                final MappingAuthkey mappingAuthkey = provideAuthenticateKey(mapRegisterCacheValue
                        .getMapRegisterCacheMetadata().getEidLispAddress());

                final MapRegisterCacheValueBuilder newMapRegisterCacheValueBuilder = new MapRegisterCacheValueBuilder(
                        mapRegisterCacheValue);
                final MapRegisterCacheMetadataBuilder newMapRegisterCacheMetadataBuilder =
                        new MapRegisterCacheMetadataBuilder(mapRegisterCacheValue.getMapRegisterCacheMetadata());

                newMapRegisterCacheValueBuilder.setMappingAuthkey(mappingAuthkey);
                newMapRegisterCacheValueBuilder.setMapRegisterCacheMetadata(newMapRegisterCacheMetadataBuilder.build());
                return newMapRegisterCacheValueBuilder.build();
            }
        }

        return mapRegisterCacheValue;

    }

    private MapRegisterCacheValue resolveCacheValue(Map.Entry<MapRegisterCacheKey, byte[]> entry) {
        if (entry != null) {
            final MapRegisterCacheValue mapRegisterCacheValue = mapRegisterCache.getEntry(entry.getKey());
            if (mapRegisterCacheValue != null) {
                final long creationTime = mapRegisterCacheValue.getMapRegisterCacheMetadata().getTimestamp();
                final long currentTime = System.currentTimeMillis();
                if (currentTime - creationTime > mapRegisterCacheTimeout) {
                    mapRegisterCache.removeEntry(entry.getKey());
                    return null;
                } else if (Arrays.equals(mapRegisterCacheValue.getPacketData(), entry.getValue())) {
                    return mapRegisterCacheValue;
                }
            }
        }
        return null;
    }

    private void sendNotificationIfPossible(final Notification notification) throws InterruptedException {
        if (notificationPublishService != null) {
            notificationPublishService.putNotification(notification);
            LOG.trace("{} was published.", notification.getClass());
        } else {
            LOG.warn("Notification Provider is null!");
        }
    }

    private MappingKeepAlive createMappingKeepAlive(final MapRegisterCacheValue value) {
        MappingKeepAliveBuilder mappingKeepAliveBuilder = new MappingKeepAliveBuilder();
        mappingKeepAliveBuilder.setMapRegisterCacheMetadata(value.getMapRegisterCacheMetadata());
        return mappingKeepAliveBuilder.build();
    }

    /**
     * Returns null if not all of eids have the same value of authentication key.
     */
    private MappingAuthkey provideAuthenticateKey(final List<EidLispAddress> eidLispAddresses) {
        MappingAuthkey firstAuthKey = null;
        for (int i = 0; i < eidLispAddresses.size(); i++) {
            final Eid eid = eidLispAddresses.get(i).getEid();
            if (i == 0) {
                firstAuthKey = smc.getAuthenticationKey(eid);
            } else {
                final MappingAuthkey authKey = smc.getAuthenticationKey(eid);
                if (!Objects.equals(firstAuthKey, authKey)) {
                    return null;
                }
            }
        }
        return firstAuthKey;

    }

    private void sendMapNotifyMsg(final ByteBuffer inBuffer, final InetAddress inetAddress, int portNumber,
                                  MapRegisterCacheValue mapRegisterValue) {
        if (mapRegisterValue.getMappingAuthkey().getKeyType() != null) {
            ByteBuffer outBuffer = transformMapRegisterToMapNotify(inBuffer);
            if (mapRegisterValue.getMappingAuthkey().getKeyType() != 0) {
                outBuffer = calculateAndSetNewMAC(outBuffer, mapRegisterValue.getMappingAuthkey().getKeyString());
            }
            outBuffer.position(0);
            lispSbPlugin.handleSerializedLispBuffer(inetAddress, outBuffer, MessageType.MapNotify, portNumber);
        } else {
            LOG.error("Map-Register Cache: authentication succeeded, but can't find auth key for sending Map-Notify");
        }
    }

    /**
     * Calculates new message authentication code (MAC) for notify message.
     */
    private ByteBuffer calculateAndSetNewMAC(final ByteBuffer buffer, final String authKey) {
        final byte[] authenticationData = LispAuthenticationUtil.createAuthenticationData(buffer, authKey);
        buffer.position(ILispAuthentication.MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        buffer.put(authenticationData);
        return buffer;
    }

    private ByteBuffer transformMapRegisterToMapNotify(final ByteBuffer buffer) {
        buffer.position(0);
        byte typeAndFlags = buffer.get(0);
        // Shift the xTR-ID present and built for an RTR bits to their correct position
        byte flags = (byte) ((typeAndFlags << 2) & 0x0F);
        // Set control message type to 4 (Map-Notify)
        byte type = 0x40;
        // Combine the nibbles
        typeAndFlags = (byte) (type | flags);
        byte[] byteReplacement = new byte[] {typeAndFlags, 0x00, 0x00};
        buffer.put(byteReplacement);

        return buffer;
    }

    private List<EidLispAddress> provideEidPrefixesFromMessage(final MapRegister mapRegister) {
        List<EidLispAddress> eidsResult = new ArrayList<>();
        for (MappingRecordItem mappingRecordItem : mapRegister.getMappingRecordItem()) {
            final EidLispAddressBuilder eidLispAddressBuilder = new EidLispAddressBuilder();
            final Eid eid = mappingRecordItem.getMappingRecord().getEid();
            eidLispAddressBuilder.setEidLispAddressId(LispAddressStringifier.getString(eid));
            eidLispAddressBuilder.setEid(eid);
            eidsResult.add(eidLispAddressBuilder.build());
        }
        return eidsResult;
    }

    /**
     * Checks whether authentication data is valid.
     *
     * <p>Methods pass through all records from map register message. For the EID of the first record it gets
     * authentication key and does validation of authentication data again this authentication key. If it pass
     * it just checks for remaining records (and its EID) whether they have the same authenticatin key stored in
     * simple map cache (smc).
     *
     * @return Returns authentication key if all of EIDs have the same authentication key or null otherwise
     */
    private MappingAuthkey tryToAuthenticateMessage(final MapRegister mapRegister, final ByteBuffer byteBuffer) {
        if (!authenticationEnabled) {
            return null;
        }

        if (smc == null) {
            LOG.debug("Simple map cache wasn't instantieted and set.");
            return null;
        }

        MappingAuthkey firstAuthKey = null;
        final List<MappingRecordItem> mappingRecords = mapRegister.getMappingRecordItem();
        for (int i = 0; i < mappingRecords.size(); i++) {
            final MappingRecordItem recordItem = mappingRecords.get(i);
            final MappingRecord mappingRecord = recordItem.getMappingRecord();
            if (i == 0) {
                firstAuthKey = smc.getAuthenticationKey(mappingRecord.getEid());
                if (!LispAuthenticationUtil.validate(mapRegister, byteBuffer, mappingRecord.getEid(), firstAuthKey)) {
                    return null;
                }
            } else {
                final Eid eid = mappingRecord.getEid();
                final MappingAuthkey authKey = smc.getAuthenticationKey(eid);
                if (!firstAuthKey.equals(authKey)) {
                    LOG.debug("Map register packet contained several eids. Authentication keys for first one and for "
                            + "{} are different.",LispAddressStringifier.getString(eid));
                    return null;
                }
            }
        }
        return firstAuthKey;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleMapNotify(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            MapNotify mapNotify = MapNotifySerializer.getInstance().deserialize(inBuffer);
            GotMapNotifyBuilder gotMapNotifyBuilder = new GotMapNotifyBuilder();
            gotMapNotifyBuilder.setMapNotify(LispNotificationHelper.convertMapNotify(mapNotify));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper
                    .getIpAddressBinaryFromInetAddress(sourceAddress));
            transportAddressBuilder.setPort(new PortNumber(port));
            gotMapNotifyBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationPublishService != null) {
                notificationPublishService.putNotification(gotMapNotifyBuilder.build());
                LOG.trace("MapNotify was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Notify (len="
                    + inBuffer.capacity() + ")", re);
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleMapReply(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            MapReply mapReply = MapReplySerializer.getInstance().deserialize(inBuffer);
            GotMapReplyBuilder gotMapReplyBuilder = new GotMapReplyBuilder();
            gotMapReplyBuilder.setMapReply(LispNotificationHelper.convertMapReply(mapReply));
            TransportAddressBuilder transportAddressBuilder = new TransportAddressBuilder();
            transportAddressBuilder.setIpAddress(LispNotificationHelper
                    .getIpAddressBinaryFromInetAddress(sourceAddress));
            transportAddressBuilder.setPort(new PortNumber(port));
            gotMapReplyBuilder.setTransportAddress(transportAddressBuilder.build());
            if (notificationPublishService != null) {
                notificationPublishService.putNotification(gotMapReplyBuilder.build());
                LOG.trace("MapReply was published!");
            } else {
                LOG.warn("Notification Provider is null!");
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Reply (len="
                    + inBuffer.capacity() + ")", re);
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    private void handleStats(int type) {
        if (lispSbStats != null) {
            if (type <= ConcurrentLispSouthboundStats.MAX_LISP_TYPES) {
                lispSbStats.incrementRx(type);
            } else {
                lispSbStats.incrementRxUnknown();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isReadFromChannelEnabled) {
            if (msg instanceof DatagramPacket) {
                DatagramPacket castedMsg = (DatagramPacket) msg;
                handleUdpPacket(castedMsg);
            } else if (msg instanceof ByteBuf) {
                final SocketAddress socketAddress = ctx.channel().remoteAddress();
                if (socketAddress instanceof InetSocketAddress) {
                    handleTcpPacket((InetSocketAddress) socketAddress, (ByteBuf) msg);
                } else {
                    LOG.debug("{} isn't instance of InetSocketAddres but {}",socketAddress,socketAddress.getClass());
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("Error on channel: " + cause, cause);
    }

    @Override
    public void close() throws Exception {
    }

    public void setSimpleMapCache(final SimpleMapCache smc) {
        this.smc = smc;
    }

    public void setDataBroker(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setNotificationProvider(NotificationPublishService nps) {
        this.notificationPublishService = nps;
    }

    public void setMapRegisterCache(final MapRegisterCache mapRegisterCache) {
        this.mapRegisterCache = mapRegisterCache;
    }

    public void setAuthenticationKeyDataListener(AuthenticationKeyDataListener authenticationKeyDataListener) {
        this.authenticationKeyDataListener = authenticationKeyDataListener;
    }

    public void setDataStoreBackEnd(DataStoreBackEnd dsbe) {
        this.dsbe = dsbe;
    }

    public void setStats(ConcurrentLispSouthboundStats lispSbStats) {
        this.lispSbStats = lispSbStats;
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
            smc.addAuthenticationKey(key, mappingAuthkey);
        }
    }

    public void setIsMaster(boolean isReadFromChannelEnabled) {
        this.isReadFromChannelEnabled = isReadFromChannelEnabled;
    }

    public void setMapRegisterCacheTimeout(long mapRegisterCacheTimeout) {
        this.mapRegisterCacheTimeout = mapRegisterCacheTimeout;
    }
}
