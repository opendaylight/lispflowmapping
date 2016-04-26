/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer;

import com.google.common.base.Optional;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.mapregister.cache.rev160425.MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.mapregister.cache.rev160425.MapRegisterCacheKeyBuilder;
import org.opendaylight.yang.gen.v1.mapregister.cache.rev160425.MapRegisterCacheValue;
import org.opendaylight.yang.gen.v1.mapregister.cache.rev160425.MapRegisterCacheValueBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;

/**
 * This class deals with deserializing map register from udp to the java object.
 */
public final class MapRegisterSerializer {

    private static final MapRegisterSerializer INSTANCE = new MapRegisterSerializer();
    private static final int NUM_FO_BYTES_FROM_START_TO_AUTH_DATA_LENGTH_POS = 13;
    private static final int NUM_OF_BYTES_FROM_AUTH_DATA_TO_EID_PREFIX_AFI = 10;
    private static final int NUM_OF_BYTES_EID_PREFIX_AFI = 2;
    private static final int PAYLOAD1_LEN = 4;
    private static final int SKIP1_LEN = 8;
    private static final int PAYLOAD2_LEN = 2;
    private static final int NUM_OF_BYTES_AUTHENTICATION_DATA_LENGTH = 2;
    private static final int IPV4 = 1;
    private static final int IPV6 = 2;
    private static final int NUM_OF_BYTES_IPV4 = 4;
    private static final int NUM_OF_BYTES_IPV6 = 16;

    // Private constructor prevents instantiation from other classes
    private MapRegisterSerializer() {
    }

    public static MapRegisterSerializer getInstance() {
        return INSTANCE;
    }

    public ByteBuffer serialize(MapRegister mapRegister) {
        int size = Length.HEADER_SIZE;
        if (mapRegister.getAuthenticationData() != null) {
            size += mapRegister.getAuthenticationData().length;
        }
        if (mapRegister.isXtrSiteIdPresent() != null && mapRegister.isXtrSiteIdPresent()) {
            size += Length.XTRID_SIZE + Length.SITEID_SIZE;
        }
        for (MappingRecordItem eidToLocatorRecord : mapRegister.getMappingRecordItem()) {
            size += MappingRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord.getMappingRecord());
        }

        ByteBuffer registerBuffer = ByteBuffer.allocate(size);
        registerBuffer.put((byte) ((byte) (MessageType.MapRegister.getIntValue() << 4) |
                ByteUtil.boolToBit(BooleanUtils.isTrue(mapRegister.isProxyMapReply()), Flags.PROXY) |
                ByteUtil.boolToBit(BooleanUtils.isTrue(mapRegister.isXtrSiteIdPresent()), Flags.XTRSITEID)));
        registerBuffer.position(registerBuffer.position() + Length.RES);
        registerBuffer.put((byte)
                (ByteUtil.boolToBit(BooleanUtils.isTrue(mapRegister.isMergeEnabled()), Flags.MERGE_ENABLED) |
                ByteUtil.boolToBit(BooleanUtils.isTrue(mapRegister.isWantMapNotify()), Flags.WANT_MAP_NOTIFY)));
        registerBuffer.put((byte) mapRegister.getMappingRecordItem().size());
        registerBuffer.putLong(NumberUtil.asLong(mapRegister.getNonce()));
        registerBuffer.putShort(NumberUtil.asShort(mapRegister.getKeyId()));

        if (mapRegister.getAuthenticationData() != null) {
            registerBuffer.putShort((short) mapRegister.getAuthenticationData().length);
            registerBuffer.put(mapRegister.getAuthenticationData());
        } else {
            registerBuffer.putShort((short) 0);
        }
        for (MappingRecordItem eidToLocatorRecord : mapRegister.getMappingRecordItem()) {
            MappingRecordSerializer.getInstance().serialize(registerBuffer, eidToLocatorRecord.getMappingRecord());
        }

        if (mapRegister.isXtrSiteIdPresent() != null && mapRegister.isXtrSiteIdPresent()) {
            registerBuffer.put(mapRegister.getXtrId().getValue());
            registerBuffer.put(mapRegister.getSiteId().getValue());
        }
        registerBuffer.clear();
        return registerBuffer;
    }




    public Map.Entry<MapRegisterCacheKey, MapRegisterCacheValue> deserializePartially(final ByteBuffer buffer,
                                                                                      final InetAddress sourceRloc) {
        final int authDataLength = ByteUtil.getUnsignedShort(buffer, NUM_FO_BYTES_FROM_START_TO_AUTH_DATA_LENGTH_POS);

        MapRegisterCacheKey mapRegisterCacheKey = createKeyFromBytes(buffer, authDataLength);
        if (mapRegisterCacheKey == null) {
            return null;
        }
        MapRegisterCacheValue mapRegisterCacheValue = createValueFromBytes(buffer, authDataLength);
        if (mapRegisterCacheValue == null) {
            return null;
        }
        return new HashMap.SimpleEntry<>(mapRegisterCacheKey, mapRegisterCacheValue);
    }

    /**
     * Extract from buffer the data which doesn't change.
     *
     * It means that nonce and authentication data are extracted (skipped)
     *
     * payload1 - <Type;RecordCount>
     * skip1    - Nonce
     * payload2 - KeyId
     * skip2    - <Authentication Data Length; Authentication Data>
     * payload3 - <Record TTL; end of message>
     *
     * @param buffer
     * @return
     */
    private MapRegisterCacheValue createValueFromBytes(ByteBuffer buffer, final int authDataLength) {
        final MapRegisterCacheValueBuilder mapRegisterCacheValueBuilder = new MapRegisterCacheValueBuilder();
        buffer.position(0);
        final int bufferLength = buffer.remaining();
        final int skip2Length = authDataLength + NUM_OF_BYTES_AUTHENTICATION_DATA_LENGTH;
        final int totalNewPayloadLength = bufferLength - SKIP1_LEN - skip2Length;
        final byte[] newPayload = new byte[totalNewPayloadLength];

        buffer.get(newPayload, 0, PAYLOAD1_LEN);
        buffer.position(PAYLOAD1_LEN + SKIP1_LEN);
        buffer.get(newPayload, PAYLOAD1_LEN, PAYLOAD2_LEN);
        buffer.position(PAYLOAD1_LEN + SKIP1_LEN + PAYLOAD2_LEN + skip2Length);
        int payload3Len = totalNewPayloadLength - PAYLOAD1_LEN - PAYLOAD2_LEN;
        buffer.get(newPayload, PAYLOAD1_LEN + PAYLOAD2_LEN, payload3Len);

        return mapRegisterCacheValueBuilder.setVal(newPayload).build();
    }

    private MapRegisterCacheKey createKeyFromBytes(final ByteBuffer buffer, int authDataLength) {
        buffer.mark();
        byte typeAndFlags = buffer.get();
        buffer.position(0);
        boolean xtrSiteIdPresent = ByteUtil.extractBit(typeAndFlags, Flags.XTRSITEID);
        if (!xtrSiteIdPresent) {
            return null;
        }
        final MapRegisterCacheKeyBuilder mapRegisterCacheKeyBuilder = new MapRegisterCacheKeyBuilder();
        final byte[] eidPrefix = extractEidPrefix(buffer, authDataLength);
        if (eidPrefix == null) {
            return null;
        }
        mapRegisterCacheKeyBuilder.setEidPrefix(eidPrefix);
        if (xtrSiteIdPresent) {
            buffer.position(0);
            final int bufferLength = buffer.remaining();
            buffer.position(bufferLength - Length.XTRID_SIZE - Length.SITEID_SIZE);

            byte[] xtrId  = new byte[Length.XTRID_SIZE];
            buffer.get(xtrId);
            mapRegisterCacheKeyBuilder.setXTRId(xtrId);

            byte[] siteId = new byte[Length.SITEID_SIZE];
            buffer.get(siteId);
            mapRegisterCacheKeyBuilder.setSiteId(siteId);
        }
        return mapRegisterCacheKeyBuilder.build();
    }

    //TODO: simplification - only one record in map register message is supposed
    private byte[] extractEidPrefix(final ByteBuffer buffer, int authDataLength) {
        final int startPositionOfEidPrefixAFI = NUM_FO_BYTES_FROM_START_TO_AUTH_DATA_LENGTH_POS + authDataLength +
                NUM_OF_BYTES_FROM_AUTH_DATA_TO_EID_PREFIX_AFI;
        final int eidPrefixAfi = ByteUtil.getUnsignedShort(buffer, startPositionOfEidPrefixAFI);
        Optional<Integer> eidPrefixLengthOpt = resolveEidPrefixLength(eidPrefixAfi);
        if (eidPrefixLengthOpt.isPresent()) {
            byte[] result = new byte[eidPrefixLengthOpt.get()];
            final int startPositionOfEidPrefix = startPositionOfEidPrefixAFI + NUM_OF_BYTES_EID_PREFIX_AFI;
            buffer.position(startPositionOfEidPrefix);
            buffer.get(result);
            return result;
        }
        return null;
    }

    private Optional<Integer> resolveEidPrefixLength(final int eidPrefixAfi) {
        switch (eidPrefixAfi) {
            case IPV4:
                return Optional.of(NUM_OF_BYTES_IPV4);
            case IPV6:
                return Optional.of(NUM_OF_BYTES_IPV6);
            default:
                return Optional.absent();
        }
    }

    public MapRegister deserialize(ByteBuffer registerBuffer, InetAddress sourceRloc) {
        try {
            MapRegisterBuilder builder = new MapRegisterBuilder();
            builder.setMappingRecordItem(new ArrayList<MappingRecordItem>());

            byte typeAndFlags = registerBuffer.get();
            boolean xtrSiteIdPresent = ByteUtil.extractBit(typeAndFlags, Flags.XTRSITEID);
            builder.setProxyMapReply(ByteUtil.extractBit(typeAndFlags, Flags.PROXY));
            builder.setXtrSiteIdPresent(xtrSiteIdPresent);

            registerBuffer.position(registerBuffer.position() + Length.RES);
            byte mergeAndMapReply = registerBuffer.get();
            builder.setWantMapNotify(ByteUtil.extractBit(mergeAndMapReply, Flags.WANT_MAP_NOTIFY));
            builder.setMergeEnabled(ByteUtil.extractBit(mergeAndMapReply, Flags.MERGE_ENABLED));
            byte recordCount = (byte) ByteUtil.getUnsignedByte(registerBuffer);
            builder.setNonce(registerBuffer.getLong());
            builder.setKeyId(registerBuffer.getShort());
            short authenticationLength = registerBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            registerBuffer.get(authenticationData);
            builder.setAuthenticationData(authenticationData);

            if (xtrSiteIdPresent) {
                List<MappingRecordBuilder> mrbs = new ArrayList<MappingRecordBuilder>();
                for (int i = 0; i < recordCount; i++) {
                    mrbs.add(MappingRecordSerializer.getInstance().deserializeToBuilder(registerBuffer));
                }
                byte[] xtrIdBuf  = new byte[Length.XTRID_SIZE];
                registerBuffer.get(xtrIdBuf);
                XtrId xtrId = new XtrId(xtrIdBuf);
                byte[] siteIdBuf = new byte[Length.SITEID_SIZE];
                registerBuffer.get(siteIdBuf);
                SiteId siteId = new SiteId(siteIdBuf);
                builder.setXtrId(xtrId);
                builder.setSiteId(siteId);
                for (MappingRecordBuilder mrb : mrbs) {
                    mrb.setXtrId(xtrId);
                    mrb.setSiteId(siteId);
                    mrb.setSourceRloc(getSourceRloc(sourceRloc));
                    builder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(
                            mrb.build()).build());
                }
            } else {
                for (int i = 0; i < recordCount; i++) {
                    builder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(
                            MappingRecordSerializer.getInstance().deserialize(registerBuffer)).build());
                }
            }

            registerBuffer.limit(registerBuffer.position());
            byte[] mapRegisterBytes = new byte[registerBuffer.position()];
            registerBuffer.position(0);
            registerBuffer.get(mapRegisterBytes);
            return builder.build();
        } catch (RuntimeException re) {
            throw new LispSerializationException("Couldn't deserialize Map-Register (len="
                    + registerBuffer.capacity() + ")", re);
        }

    }

    private static IpAddress getSourceRloc(InetAddress sourceRloc) {
        if (sourceRloc == null) {
            sourceRloc = InetAddress.getLoopbackAddress();
        }

        if (sourceRloc instanceof Inet4Address) {
            return new IpAddress(new Ipv4Address(sourceRloc.getHostAddress()));
        } else {
            return new IpAddress(new Ipv6Address(sourceRloc.getHostAddress()));
        }
    }

    private interface Flags {
        byte PROXY = 0x08;
        byte XTRSITEID = 0x02;
        byte MERGE_ENABLED = 0x04;
        byte WANT_MAP_NOTIFY = 0x01;
    }

    public interface Length {
        int HEADER_SIZE = 16;
        int XTRID_SIZE = 16;
        int SITEID_SIZE = 8;
        int RES = 1;
    }
}
