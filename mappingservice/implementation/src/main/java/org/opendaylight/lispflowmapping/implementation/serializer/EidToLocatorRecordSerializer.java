package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
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
        builder.setRecordTtl(buffer.getInt());
        byte locatorCount = buffer.get();
        builder.setMaskLength((short) (buffer.get() & 0xff));
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
        if (record.getRecordTtl() != null) {
            replyBuffer.putInt(record.getRecordTtl());
        } else {
            replyBuffer.putInt(0);
        }
        if (record.getLocatorRecord() != null) {
            replyBuffer.put((byte) record.getLocatorRecord().size());
        } else {
            replyBuffer.put((byte) 0);

        }
        if (record.getMaskLength() != null) {
            replyBuffer.put(record.getMaskLength().byteValue());
        } else {
            replyBuffer.put((byte) 0);

        }
        Action act = Action.NoAction;
        if (record.getAction() != null) {
            act = record.getAction();
        }
        replyBuffer.put((byte) ((act.getIntValue() << 5) | //
                ByteUtil.boolToBit(BooleanUtils.isTrue(record.isAuthoritative()), Flags.AUTHORITATIVE)));
        replyBuffer.position(replyBuffer.position() + Length.RESERVED);
        if (record.getMapVersion() != null) {
            replyBuffer.putShort(record.getMapVersion());
        } else {
            replyBuffer.putShort((short) 0);
        }
        if (record.getLispAddressContainer() != null && record.getLispAddressContainer().getAddress() != null) {
            LispAddressSerializer.getInstance().serialize(replyBuffer, (LispAFIAddress) record.getLispAddressContainer().getAddress());
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
            size += LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) record.getLispAddressContainer().getAddress());
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
