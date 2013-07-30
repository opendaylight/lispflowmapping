package org.opendaylight.lispflowmapping.southbound.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.southbound.serializer.address.LispAddressSerializerFactory;
import org.opendaylight.lispflowmapping.southbound.serializer.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReplyAction;

public class EidToLocatorRecordSerializer {
	
	private static final EidToLocatorRecordSerializer INSTANCE = new EidToLocatorRecordSerializer();

	// Private constructor prevents instantiation from other classes
	private EidToLocatorRecordSerializer() {
	}

	public static EidToLocatorRecordSerializer getInstance() {
		return INSTANCE;
	}
	
	public EidToLocatorRecord deserialize(ByteBuffer buffer) {
        EidToLocatorRecord eidToLocatorRecord = new EidToLocatorRecord();
        eidToLocatorRecord.setRecordTtl(buffer.getInt());
        byte locatorCount = buffer.get();
        eidToLocatorRecord.setMaskLength(buffer.get());
        byte actionAndAuthoritative = buffer.get();
        eidToLocatorRecord.setAction(MapReplyAction.valueOf(actionAndAuthoritative >> 5));
        eidToLocatorRecord.setAuthoritative(ByteUtil.extractBit(actionAndAuthoritative, Flags.AUTHORITATIVE));
        buffer.position(buffer.position() + Length.RESERVED);
        eidToLocatorRecord.setMapVersion(buffer.getShort());

        eidToLocatorRecord.setPrefix(LispAddressSerializer.getInstance().deserialize(buffer));
        for (int i = 0; i < locatorCount; i++) {
            eidToLocatorRecord.addLocator(LocatorRecordSerializer.getInstance().deserialize(buffer));
        }

        return eidToLocatorRecord;
    }

    public void serialize(ByteBuffer replyBuffer, EidToLocatorRecord record) {
        replyBuffer.putInt(record.getRecordTtl());
        replyBuffer.put((byte) record.getLocators().size());
        replyBuffer.put((byte) record.getMaskLength());
        replyBuffer.put((byte) ((record.getAction().getCode() << 5) | //
                ByteUtil.boolToBit(record.isAuthoritative(), Flags.AUTHORITATIVE)));
        replyBuffer.position(replyBuffer.position() + Length.RESERVED);
        replyBuffer.putShort(record.getMapVersion());
        LispAddressSerializerFactory.getSerializer(record.getPrefix().getAfi()).serialize(replyBuffer, record.getPrefix());

        for (LocatorRecord locatorRecord : record.getLocators()) {
            LocatorRecordSerializer.getInstance().serialize(replyBuffer, locatorRecord);
        }
    }

    public int getSerializationSize(EidToLocatorRecord record) {
        int size = Length.HEADER_SIZE + LispAddressSerializerFactory.getSerializer(record.getPrefix().getAfi()).getAddressSize(record.getPrefix());
        for (LocatorRecord locatorRecord : record.getLocators()) {
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
