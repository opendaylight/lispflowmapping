/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.LispMessageEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegisterBuilder;

/**
 * This class deals with deserializing map register from udp to the java object.
 */
public class MapRegisterSerializer {

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
        for (EidToLocatorRecord eidToLocatorRecord : mapRegister.getEidToLocatorRecord()) {
            size += EidToLocatorRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord);
        }

        ByteBuffer registerBuffer = ByteBuffer.allocate(size);
        registerBuffer.put((byte) ((byte) (LispMessageEnum.MapRegister.getValue() << 4) |
                ByteUtil.boolToBit(BooleanUtils.isTrue(mapRegister.isProxyMapReply()), Flags.PROXY) |
                ByteUtil.boolToBit(BooleanUtils.isTrue(mapRegister.isXtrSiteIdPresent()), Flags.XTRSITEID)));
        registerBuffer.position(registerBuffer.position() + Length.RES);
        registerBuffer.put(ByteUtil.boolToBit(BooleanUtils.isTrue(mapRegister.isWantMapNotify()), Flags.WANT_MAP_REPLY));
        registerBuffer.put((byte) mapRegister.getEidToLocatorRecord().size());
        registerBuffer.putLong(NumberUtil.asLong(mapRegister.getNonce()));
        registerBuffer.putShort(NumberUtil.asShort(mapRegister.getKeyId()));

        if (mapRegister.getAuthenticationData() != null) {
            registerBuffer.putShort((short) mapRegister.getAuthenticationData().length);
            registerBuffer.put(mapRegister.getAuthenticationData());
        } else {
            registerBuffer.putShort((short) 0);
        }
        for (EidToLocatorRecord eidToLocatorRecord : mapRegister.getEidToLocatorRecord()) {
            EidToLocatorRecordSerializer.getInstance().serialize(registerBuffer, eidToLocatorRecord);
        }

        if (mapRegister.isXtrSiteIdPresent() != null && mapRegister.isXtrSiteIdPresent()) {
            registerBuffer.put(mapRegister.getXtrId());
            registerBuffer.put(mapRegister.getSiteId());
        }
        registerBuffer.clear();
        return registerBuffer;
    }

    public MapRegister deserialize(ByteBuffer registerBuffer) {
        try {
            MapRegisterBuilder builder = new MapRegisterBuilder();
            builder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());

            byte typeAndFlags = registerBuffer.get();
            boolean xtrSiteIdPresent = ByteUtil.extractBit(typeAndFlags, Flags.XTRSITEID);
            builder.setProxyMapReply(ByteUtil.extractBit(typeAndFlags, Flags.PROXY));
            builder.setXtrSiteIdPresent(xtrSiteIdPresent);

            registerBuffer.position(registerBuffer.position() + Length.RES);
            builder.setWantMapNotify(ByteUtil.extractBit(registerBuffer.get(), Flags.WANT_MAP_REPLY));
            byte recordCount = (byte) ByteUtil.getUnsignedByte(registerBuffer);
            builder.setNonce(registerBuffer.getLong());
            builder.setKeyId(registerBuffer.getShort());
            short authenticationLength = registerBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            registerBuffer.get(authenticationData);
            builder.setAuthenticationData(authenticationData);

            for (int i = 0; i < recordCount; i++) {
                builder.getEidToLocatorRecord().add(
                        new EidToLocatorRecordBuilder(EidToLocatorRecordSerializer.getInstance().deserialize(registerBuffer)).build());
            }

            if (xtrSiteIdPresent) {
                byte[] xtrId  = new byte[Length.XTRID_SIZE];
                registerBuffer.get(xtrId);
                byte[] siteId = new byte[Length.SITEID_SIZE];
                registerBuffer.get(siteId);
                builder.setXtrId(xtrId);
                builder.setSiteId(siteId);
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

    private interface Flags {
        byte PROXY = 0x08;
        byte XTRSITEID = 0x02;
        byte WANT_MAP_REPLY = 0x01;
    }

    public interface Length {
        int HEADER_SIZE = 16;
        int XTRID_SIZE = 16;
        int SITEID_SIZE = 8;
        int RES = 1;
    }
}
