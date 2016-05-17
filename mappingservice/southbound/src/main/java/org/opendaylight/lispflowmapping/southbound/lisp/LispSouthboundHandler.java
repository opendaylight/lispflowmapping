/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.mapcache.SimpleMapCache;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundStats;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterCache;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterPartialDeserializer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.lisp.network.PacketHeader;
import org.opendaylight.lispflowmapping.southbound.util.LispNotificationHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class LispSouthboundHandler extends SimpleChannelInboundHandler<DatagramPacket>
        implements ILispSouthboundService, AutoCloseable {
    private final MapRegisterCache mapRegisterCache;

    /**
     * How long is record supposed to be relevant. After this time record isn't valid.
     *
     * If you modify this value, please update the LispSouthboundServiceTest class too.
     */
    private static final long CACHE_RECORD_TIMEOUT = 90000;

    private NotificationPublishService notificationPublishService;
    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundHandler.class);

    //TODO: think whether this field can be accessed through mappingservice or some other configuration parameter
    private boolean authenticationEnabled = true;

    private final LispSouthboundPlugin lispSbPlugin;
    private LispSouthboundStats lispSbStats = null;
    private SimpleMapCache smc;
    private final AuthenticationKeyDataListener authenticationKeyDataListener;

    public LispSouthboundHandler(LispSouthboundPlugin lispSbPlugin, final MapRegisterCache mapRegisterCache, final
    DataBroker dataBroker) {
        this.lispSbPlugin = lispSbPlugin;
        if (lispSbPlugin != null) {
            this.lispSbStats = lispSbPlugin.getStats();
        }
        this.mapRegisterCache = mapRegisterCache;

        authenticationKeyDataListener = new AuthenticationKeyDataListener(dataBroker, smc);
    }

    public void setNotificationProvider(NotificationPublishService nps) {
        this.notificationPublishService = nps;
    }

    public void handlePacket(DatagramPacket msg) {
        ByteBuffer inBuffer = msg.content().nioBuffer();
        int type = ByteUtil.getUnsignedByte(inBuffer, LispMessage.Pos.TYPE) >> 4;
        handleStats(type);
        Object lispType = MessageType.forValue(type);
        if (lispType == MessageType.EncapsulatedControlMessage) {
            LOG.trace("Received packet of type Encapsulated Control Message");
            handleEncapsulatedControlMessage(inBuffer, msg.sender().getAddress());
        } else if (lispType == MessageType.MapRequest) {
            LOG.trace("Received packet of type Map-Request");
            handleMapRequest(inBuffer, msg.sender().getPort());
        } else if (lispType == MessageType.MapRegister) {
            LOG.trace("Received packet of type Map-Register");
            handleMapRegister(inBuffer, msg.sender().getAddress(), msg.sender().getPort());
        } else if (lispType == MessageType.MapNotify) {
            LOG.trace("Received packet of type Map-Notify");
            handleMapNotify(inBuffer, msg.sender().getAddress(), msg.sender().getPort());
        } else if (lispType == MessageType.MapReply) {
            LOG.trace("Received packet of type Map-Reply");
            handleMapReply(inBuffer, msg.sender().getAddress(), msg.sender().getPort());
        } else {
            LOG.warn("Received unknown LISP control packet (type " + ((lispType != null) ? lispType : type) + ")");
        }
    }

    private void handleEncapsulatedControlMessage(ByteBuffer inBuffer, InetAddress sourceAddress) {
        try {
            handleMapRequest(inBuffer, extractEncapsulatedSourcePort(inBuffer));
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len="
                    + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapRequest(ByteBuffer inBuffer, int port) {
        try {
            MapRequest request = MapRequestSerializer.getInstance().deserialize(inBuffer);
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
            throw new LispMalformedPacketException("Couldn't deserialize Map-RequestLispAuthenticationUtil (len="
                    + inBuffer.capacity() + ")", re);
        }
    }

    private void handleMapRegister(ByteBuffer inBuffer, InetAddress sourceAddress, int port) {
        try {
            final Map.Entry<MapRegisterCacheKey, byte[]> artificialEntry = MapRegisterPartialDeserializer
                    .deserializePartially(inBuffer, sourceAddress);
            final MapRegisterCacheKey cacheKey = artificialEntry == null ? null : artificialEntry.getKey();

            final MapRegisterCacheValue cacheValue = resolveCacheValue(artificialEntry);
            if (cacheValue != null) {
                final MapRegisterCacheMetadata mapRegisterValue = cacheValue.getMapRegisterCacheMetadata();
                LOG.debug("Map register message site-ID: {} xTR-ID: {} from cache.", mapRegisterValue.getSiteId(),
                        mapRegisterValue.getXtrId());
                mapRegisterCache.refreshEntry(cacheKey);
                sendNotificationIfPossible(createMappingKeepAlive(cacheValue));
                if (mapRegisterValue.isWantMapNotify()) {
                    sendMapNotifyMsg(inBuffer, sourceAddress, port);
                }
            } else {
                MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(inBuffer, sourceAddress);
                if (isAuthenticationSuccessful(mapRegister, inBuffer)) {
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
                        cacheValueBldNew.setMapRegisterCacheMetadata(cacheMetadataBldNew.build());

                        mapRegisterCache.addEntry(cacheKey, cacheValueBldNew.build());
                    }
                }
            }
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Register (len="
                    + inBuffer.capacity() + ")", re);
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    private MapRegisterCacheValue resolveCacheValue(Map.Entry<MapRegisterCacheKey, byte[]> entry) {
        if (entry != null) {
            final MapRegisterCacheValue mapRegisterCacheValue = mapRegisterCache.getEntry(entry.getKey());
            if (mapRegisterCacheValue != null) {
                final long creationTime = mapRegisterCacheValue.getMapRegisterCacheMetadata().getTimestamp();
                final long currentTime = System.currentTimeMillis();
                if (currentTime - creationTime > CACHE_RECORD_TIMEOUT) {
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

    private void sendMapNotifyMsg(final ByteBuffer inBuffer, final InetAddress inetAddress, int portNumber) {
        ByteBuffer outBuffer = transformMapRegisterToMapNotify(inBuffer);
        outBuffer.position(0);
        lispSbPlugin.handleSerializedLispBuffer(inetAddress, outBuffer, MessageType.MapNotify, portNumber);
    }

    private ByteBuffer transformMapRegisterToMapNotify(final ByteBuffer buffer) {
        buffer.position(0);
        //TODO: also reset of authentication data is required. other trello card is opened for this task.
        byte[] byteReplacement = new byte[] {0x04, 0x00, 0x00};
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
     * Methods pass through all records from map register message. For the EID of the first record it gets
     * authentication key and does validation of authentication data again this authentication key. If it pass
     * it just checks for remaining records (and its EID) whether they have the same authenticatin key stored in
     * simple map cache (smc).
     *
     * @param mapRegister
     * @param byteBuffer
     * @return
     */
    private boolean isAuthenticationSuccessful(final MapRegister mapRegister, final ByteBuffer byteBuffer) {
        if (!authenticationEnabled) {
            return true;
        }

        if (smc == null) {
            LOG.debug("Simple map cache wasn't instantieted and set.");
            return false;
        }

        MappingAuthkey firstAuthKey = null;
        final List<MappingRecordItem> mappingRecords = mapRegister.getMappingRecordItem();
        for (int i = 0; i < mappingRecords.size(); i++) {
            final MappingRecordItem recordItem = mappingRecords.get(i);
            final MappingRecord mappingRecord = recordItem.getMappingRecord();
            if (i == 0) {
                firstAuthKey = smc.getAuthenticationKey(mappingRecord.getEid());
                if (!LispAuthenticationUtil.validate(mapRegister, byteBuffer, mappingRecord.getEid(), firstAuthKey)) {
                    return false;
                }
            } else {
                final Eid eid = mappingRecord.getEid();
                final MappingAuthkey authKey = smc.getAuthenticationKey(eid);
                if (!firstAuthKey.equals(authKey)) {
                    LOG.debug("Map register packet contained several eids. Authentication keys for first one and for " +
                            "{} are different.",LispAddressStringifier.getString(eid));
                    return false;
                }
            }
        }
        return true;
    }

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
            if (type <= LispSouthboundStats.MAX_LISP_TYPES) {
                lispSbStats.incrementRx(type);
            } else {
                lispSbStats.incrementRxUnknown();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Received UDP packet from {}:{} with content:\n{}", msg.sender().getHostString(),
                    msg.sender().getPort(), ByteBufUtil.prettyHexDump(msg.content()));
        }
        handlePacket(msg);
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
        authenticationKeyDataListener.closeDataChangeListener();
    }

    public void setSimpleMapCache(final SimpleMapCache smc) {
        this.smc = smc;
    }
}
