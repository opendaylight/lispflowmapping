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
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializerContext;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;

public final class MappingRecordSerializer {

    private static final MappingRecordSerializer INSTANCE = new MappingRecordSerializer();

    // Private constructor prevents instantiation from other classes
    private MappingRecordSerializer() {
    }

    public static MappingRecordSerializer getInstance() {
        return INSTANCE;
    }

    public MappingRecord deserialize(ByteBuffer buffer) {
        return deserializeToBuilder(buffer).build();
    }

    public MappingRecordBuilder deserializeToBuilder(ByteBuffer buffer) {
        MappingRecordBuilder builder = new MappingRecordBuilder();
        builder.setRecordTtl(buffer.getInt());
        final byte locatorCount = (byte) ByteUtil.getUnsignedByte(buffer);
        final short maskLength = ((short) ByteUtil.getUnsignedByte(buffer));
        final byte actionAndAuthoritative = buffer.get();
        Action act = Action.forValue(actionAndAuthoritative >> 5);
        if (act == null) {
            act = Action.NoAction;
        }
        builder.setAction(act);
        builder.setAuthoritative(ByteUtil.extractBit(actionAndAuthoritative, Flags.AUTHORITATIVE));
        buffer.position(buffer.position() + Length.RESERVED);
        builder.setMapVersion(buffer.getShort());

        LispAddressSerializerContext ctx = new LispAddressSerializerContext(maskLength);
        builder.setEid(LispAddressSerializer.getInstance().deserializeEid(buffer, ctx));

        builder.setLocatorRecord(new ArrayList<LocatorRecord>());
        for (int i = 0; i < locatorCount; i++) {
            builder.getLocatorRecord().add(LocatorRecordSerializer.getInstance().deserialize(buffer));
        }

        return builder;
    }

    public void serialize(ByteBuffer replyBuffer, MappingRecord record) {
        replyBuffer.putInt(NumberUtil.asInt(record.getRecordTtl()));
        if (record.getLocatorRecord() != null) {
            replyBuffer.put((byte) record.getLocatorRecord().size());
        } else {
            replyBuffer.put((byte) 0);
        }
        if (record.getEid() != null && MaskUtil.getMaskForAddress(record.getEid().getAddress()) != -1) {
            replyBuffer.put((byte) NumberUtil.asShort(MaskUtil.getMaskForAddress(record.getEid().getAddress())));
        } else {
            replyBuffer.put((byte) 0);
        }
        Action act = Action.NoAction;
        if (record.getAction() != null) {
            act = record.getAction();
        }
        replyBuffer.put((byte) ((act.getIntValue() << 5) |
                ByteUtil.boolToBit(BooleanUtils.isTrue(record.isAuthoritative()), Flags.AUTHORITATIVE)));
        replyBuffer.position(replyBuffer.position() + Length.RESERVED);
        replyBuffer.putShort(NumberUtil.asShort(record.getMapVersion()));
        if (record.getEid() != null && record.getEid().getAddress() != null) {
            LispAddressSerializer.getInstance().serialize(replyBuffer, record.getEid());
        }

        if (record.getLocatorRecord() != null) {
            for (LocatorRecord locatorRecord : record.getLocatorRecord()) {
                LocatorRecordSerializer.getInstance().serialize(replyBuffer, locatorRecord);
            }
        }
    }

    public int getSerializationSize(MappingRecord record) {
        int size = Length.HEADER_SIZE;
        if (record.getEid() != null) {
            size += LispAddressSerializer.getInstance().getAddressSize(record.getEid());
        }
        if (record.getLocatorRecord() != null) {
            for (LocatorRecord locatorRecord : record.getLocatorRecord()) {
                size += LocatorRecordSerializer.getInstance().getSerializationSize(locatorRecord);
            }
        }
        return size;
    }

    private interface Flags {
        int AUTHORITATIVE = 0x10;
    }

    private interface Length {
        int HEADER_SIZE = 10;
        int RESERVED = 1;
    }
}
