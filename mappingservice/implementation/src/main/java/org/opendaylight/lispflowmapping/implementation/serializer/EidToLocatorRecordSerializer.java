package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.convertors.LispAFIToContainerConvertorFactory;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;

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
        builder.setLocatorRecord(new ArrayList<LocatorRecord>());
        builder.setRecordTtl(buffer.getInt());
        byte locatorCount = buffer.get();
        builder.setMaskLength(buffer.get());
        byte actionAndAuthoritative = buffer.get();
        // builder.setAction(MapReplyAction.valueOf(actionAndAuthoritative >>
        // 5));
        builder.setAction(Action.NoAction);
        builder.setAuthoritative(ByteUtil.extractBit(actionAndAuthoritative, Flags.AUTHORITATIVE));
        buffer.position(buffer.position() + Length.RESERVED);
        builder.setMapVersion(buffer.getShort());

        LispAFIAddress afiAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        builder.setLispAddressContainer(LispAFIToContainerConvertorFactory.toContainer(afiAddress));
        for (int i = 0; i < locatorCount; i++) {
            builder.getLocatorRecord().add(LocatorRecordSerializer.getInstance().deserialize(buffer));
        }

        return builder.build();
    }

    public void serialize(ByteBuffer replyBuffer, EidToLocatorRecord record) {
        replyBuffer.putInt(record.getRecordTtl());
        replyBuffer.put((byte) record.getLocatorRecord().size());
        replyBuffer.put((byte) record.getMaskLength());
        replyBuffer.put((byte) ((record.getAction().getIntValue() << 5) | //
                ByteUtil.boolToBit(record.isAuthoritative(), Flags.AUTHORITATIVE)));
        // replyBuffer.put((byte) ((record.getAction().getCode() << 5) | //
        // ByteUtil.boolToBit(record.isAuthoritative(), Flags.AUTHORITATIVE)));
        replyBuffer.position(replyBuffer.position() + Length.RESERVED);
        replyBuffer.putShort(record.getMapVersion());
        LispAddressSerializer.getInstance().serialize(replyBuffer, (LispAFIAddress) record.getLispAddressContainer().getAddress());

        for (LocatorRecord locatorRecord : record.getLocatorRecord()) {
            LocatorRecordSerializer.getInstance().serialize(replyBuffer, locatorRecord);
        }
    }

    public int getSerializationSize(EidToLocatorRecord record) {
        int size = Length.HEADER_SIZE
                + LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) record.getLispAddressContainer().getAddress());
        for (LocatorRecord locatorRecord : record.getLocatorRecord()) {
            size += LocatorRecordSerializer.getInstance().getSerializationSize(locatorRecord);
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
