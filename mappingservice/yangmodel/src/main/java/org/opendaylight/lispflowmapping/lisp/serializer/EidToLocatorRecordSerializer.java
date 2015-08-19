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
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;

public class EidToLocatorRecordSerializer {

    private static final EidToLocatorRecordSerializer INSTANCE = new EidToLocatorRecordSerializer();

    // Private constructor prevents instantiation from other classes
    private EidToLocatorRecordSerializer() {
    }

    public static EidToLocatorRecordSerializer getInstance() {
        return INSTANCE;
    }

    public EidToLocatorRecord deserialize(ByteBuffer buffer) {
        EidToLocatorRecordBuilder builder = new EidToLocatorRecordBuilder();
        builder.setRecordTtl(buffer.getInt());
        byte locatorCount = (byte) ByteUtil.getUnsignedByte(buffer);
        builder.setMaskLength((short) ByteUtil.getUnsignedByte(buffer));
        byte actionAndAuthoritative = buffer.get();
        Action act = Action.forValue(actionAndAuthoritative >> 5);
        if (act == null) {
            act = Action.NoAction;
        }
        builder.setAction(act);
        builder.setAuthoritative(ByteUtil.extractBit(actionAndAuthoritative, Flags.AUTHORITATIVE));
        buffer.position(buffer.position() + Length.RESERVED);
        builder.setMapVersion(buffer.getShort());

        LispAFIAddress afiAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LispAddressContainer container = LispAFIConvertor.toContainer(afiAddress);
        builder.setLispAddressContainer(container);

        builder.setLocatorRecord(new ArrayList<LocatorRecord>());
        for (int i = 0; i < locatorCount; i++) {
            builder.getLocatorRecord().add(LocatorRecordSerializer.getInstance().deserialize(buffer));
        }

        return builder.build();
    }

    public void serialize(ByteBuffer replyBuffer, EidToLocatorRecord record) {
        replyBuffer.putInt(NumberUtil.asInt(record.getRecordTtl()));
        if (record.getLocatorRecord() != null) {
            replyBuffer.put((byte) record.getLocatorRecord().size());
        } else {
            replyBuffer.put((byte) 0);
        }
        replyBuffer.put((byte) NumberUtil.asShort(record.getMaskLength()));
        Action act = Action.NoAction;
        if (record.getAction() != null) {
            act = record.getAction();
        }
        replyBuffer.put((byte) ((act.getIntValue() << 5) | //
                ByteUtil.boolToBit(BooleanUtils.isTrue(record.isAuthoritative()), Flags.AUTHORITATIVE)));
        replyBuffer.position(replyBuffer.position() + Length.RESERVED);
        replyBuffer.putShort(NumberUtil.asShort(record.getMapVersion()));
        if (record.getLispAddressContainer() != null && record.getLispAddressContainer().getAddress() != null) {
            LispAddressSerializer.getInstance().serialize(replyBuffer, LispAFIConvertor.toAFI(record.getLispAddressContainer()));
        }

        if (record.getLocatorRecord() != null) {
            for (LocatorRecord locatorRecord : record.getLocatorRecord()) {
                LocatorRecordSerializer.getInstance().serialize(replyBuffer, locatorRecord);
            }
        }
    }

    public int getSerializationSize(EidToLocatorRecord record) {
        int size = Length.HEADER_SIZE;
        if (record.getLispAddressContainer() != null) {
            size += LispAddressSerializer.getInstance().getAddressSize((LispAFIConvertor.toAFI(record.getLispAddressContainer())));
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
