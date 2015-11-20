/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class LocatorRecordSerializer {

    private static final LocatorRecordSerializer INSTANCE = new LocatorRecordSerializer();

    // Private constructor prevents instantiation from other classes
    private LocatorRecordSerializer() {
    }

    public static LocatorRecordSerializer getInstance() {
        return INSTANCE;
    }

    protected LocatorRecord deserialize(ByteBuffer buffer) {
        LocatorRecordBuilder builder = new LocatorRecordBuilder();
        builder.setPriority((short) ByteUtil.getUnsignedByte(buffer));
        builder.setWeight((short) ByteUtil.getUnsignedByte(buffer));
        builder.setMulticastPriority((short) ByteUtil.getUnsignedByte(buffer));
        builder.setMulticastWeight((short) ByteUtil.getUnsignedByte(buffer));
        byte flags = (byte) buffer.getShort();
        builder.setLocalLocator(ByteUtil.extractBit(flags, Flags.LOCAL_LOCATOR));
        builder.setRlocProbed(ByteUtil.extractBit(flags, Flags.RLOC_PROBED));
        builder.setRouted(ByteUtil.extractBit(flags, Flags.ROUTED));
        Rloc rloc = LispAddressSerializer.getInstance().deserializeRloc(buffer);
        builder.setRloc(rloc);
        builder.setLocatorId(LispAddressStringifier.getString(rloc));
        return builder.build();
    }

    public void serialize(ByteBuffer replyBuffer, LocatorRecord record) {
        replyBuffer.put((byte) NumberUtil.asShort(record.getPriority()));
        replyBuffer.put((byte) NumberUtil.asShort(record.getWeight()));
        replyBuffer.put((byte) NumberUtil.asShort(record.getMulticastPriority()));
        replyBuffer.put((byte) NumberUtil.asShort(record.getMulticastWeight()));
        replyBuffer.position(replyBuffer.position() + Length.UNUSED_FLAGS);
        replyBuffer.put((byte) (ByteUtil.boolToBit(BooleanUtils.isTrue(record.isLocalLocator()), Flags.LOCAL_LOCATOR) | //
                ByteUtil.boolToBit(BooleanUtils.isTrue(record.isRlocProbed()), Flags.RLOC_PROBED) | //
                ByteUtil.boolToBit(BooleanUtils.isTrue(record.isRouted()), Flags.ROUTED)));
        LispAddressSerializer.getInstance().serialize(replyBuffer, record.getRloc());
    }

    public int getSerializationSize(LocatorRecord record) {
        return Length.HEADER_SIZE
                + LispAddressSerializer.getInstance().getAddressSize(record.getRloc());
    }

    private interface Flags {
        int LOCAL_LOCATOR = 0x04;
        int RLOC_PROBED = 0x02;
        int ROUTED = 0x01;
    }

    private interface Length {
        int HEADER_SIZE = 6;
        int UNUSED_FLAGS = 1;
    }
}
