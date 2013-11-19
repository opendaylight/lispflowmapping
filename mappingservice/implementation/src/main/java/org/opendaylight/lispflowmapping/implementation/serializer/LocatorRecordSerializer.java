package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.LispAFIConvertor;
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
        builder.setPriority((short) buffer.get());
        builder.setWeight((short) buffer.get());
        builder.setMulticastPriority((short) buffer.get());
        builder.setMulticastWeight((short) buffer.get());
        byte flags = (byte) buffer.getShort();
        builder.setLocalLocator(ByteUtil.extractBit(flags, Flags.LOCAL_LOCATOR));
        builder.setRlocProbed(ByteUtil.extractBit(flags, Flags.RLOC_PROBED));
        builder.setRouted(ByteUtil.extractBit(flags, Flags.ROUTED));
        LispAFIAddress afiAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        builder.setLispAddressContainer(LispAFIConvertor.toContainer(afiAddress));
        return builder.build();
    }

    public void serialize(ByteBuffer replyBuffer, LocatorRecord record) {
        if (record.getPriority() != null) {
            replyBuffer.put(record.getPriority().byteValue());
        } else {
            replyBuffer.put((byte) 0);
        }
        if (record.getWeight() != null) {
            replyBuffer.put(record.getWeight().byteValue());
        } else {
            replyBuffer.put((byte) 0);
        }
        if (record.getMulticastPriority() != null) {
            replyBuffer.put(record.getMulticastPriority().byteValue());
        } else {
            replyBuffer.put((byte) 0);
        }
        if (record.getMulticastWeight() != null) {
            replyBuffer.put(record.getMulticastWeight().byteValue());
        } else {
            replyBuffer.put((byte) 0);
        }
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
