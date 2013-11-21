/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.util.NumberUtil;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecordBuilder;

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
        LispAFIAddress afiAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        builder.setLispAddressContainer(LispAFIConvertor.toContainer(afiAddress));
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
        LispAddressSerializer.getInstance().serialize(replyBuffer, (LispAFIAddress) record.getLispAddressContainer().getAddress());
    }

    public int getSerializationSize(LocatorRecord record) {
        return Length.HEADER_SIZE
                + LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) record.getLispAddressContainer().getAddress());
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
