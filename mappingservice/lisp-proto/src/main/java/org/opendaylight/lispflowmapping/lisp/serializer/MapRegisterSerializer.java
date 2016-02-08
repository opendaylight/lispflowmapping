/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;

/**
 * This class deals with deserializing map register from udp to the java object.
 */
public final class MapRegisterSerializer {

    private static final MapRegisterSerializer INSTANCE = new MapRegisterSerializer();

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
            registerBuffer.put(mapRegister.getXtrId());
            registerBuffer.put(mapRegister.getSiteId());
        }
        registerBuffer.clear();
        return registerBuffer;
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
                byte[] xtrId  = new byte[Length.XTRID_SIZE];
                registerBuffer.get(xtrId);
                byte[] siteId = new byte[Length.SITEID_SIZE];
                registerBuffer.get(siteId);
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
            throw new LispSerializationException("Couldn't deserialize Map-Register (len=" + registerBuffer.capacity() + ")", re);
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
