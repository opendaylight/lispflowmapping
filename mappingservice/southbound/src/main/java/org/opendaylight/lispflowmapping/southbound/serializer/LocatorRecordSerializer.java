package org.opendaylight.lispflowmapping.southbound.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.southbound.serializer.address.LispAddressSerializerFactory;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.util.ByteUtil;

public class LocatorRecordSerializer {
	
	private static final LocatorRecordSerializer INSTANCE = new LocatorRecordSerializer();

	// Private constructor prevents instantiation from other classes
	private LocatorRecordSerializer() {
	}

	public static LocatorRecordSerializer getInstance() {
		return INSTANCE;
	}
	
	protected LocatorRecord deserialize(ByteBuffer buffer) {
        LocatorRecord record = new LocatorRecord();
        record.setPriority(buffer.get());
        record.setWeight(buffer.get());
        record.setMulticastPriority(buffer.get());
        record.setMulticastWeight(buffer.get());
        byte flags = (byte) buffer.getShort();
        record.setLocalLocator(ByteUtil.extractBit(flags, Flags.LOCAL_LOCATOR));
        record.setRlocProbed(ByteUtil.extractBit(flags, Flags.RLOC_PROBED));
        record.setRouted(ByteUtil.extractBit(flags, Flags.ROUTED));
        record.setLocator(LispAddressSerializer.valueOf(buffer));
        return record;
    }

    public void serialize(ByteBuffer replyBuffer, LocatorRecord record) {
        replyBuffer.put(record.getPriority());
        replyBuffer.put(record.getWeight());
        replyBuffer.put(record.getMulticastPriority());
        replyBuffer.put(record.getMulticastWeight());
        replyBuffer.position(replyBuffer.position() + Length.UNUSED_FLAGS);
        replyBuffer.put((byte) (ByteUtil.boolToBit(record.isLocalLocator(), Flags.LOCAL_LOCATOR) | //
                ByteUtil.boolToBit(record.isRlocProbed(), Flags.RLOC_PROBED) | //
                ByteUtil.boolToBit(record.isRouted(), Flags.ROUTED)));
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(record.getLocator());
        serializer.serialize(replyBuffer, record.getLocator());
    }

    public int getSerializationSize(LocatorRecord record) {
    	LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(record.getLocator());
        return Length.HEADER_SIZE + serializer.getAddressSize(record.getLocator());
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
