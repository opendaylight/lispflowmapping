package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;

public class LispSegmentLCAFAddressSerializer extends LispLCAFAddressSerializer{
	
	private static final LispSegmentLCAFAddressSerializer INSTANCE = new LispSegmentLCAFAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispSegmentLCAFAddressSerializer() {
	}

	public static LispSegmentLCAFAddressSerializer getInstance() {
		return INSTANCE;
	}


	@Override
    public short getLcafLength(LispAddress lispAddress) {
		LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(((LispSegmentLCAFAddress)lispAddress).getAddress());
        return (short) (Length.INSTANCE + serializer.getAddressSize(lispAddress));
    }

	@Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        buffer.putInt(((LispSegmentLCAFAddress)lispAddress).getInstanceId());
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(((LispSegmentLCAFAddress)lispAddress).getAddress());
        serializer.serialize(buffer, ((LispSegmentLCAFAddress)lispAddress).getAddress());
    }
	
	public static LispSegmentLCAFAddress valueOf(byte res2, short length, ByteBuffer buffer) {
        int instanceId = buffer.getInt();
        LispAddress address = LispAddressSerializer.valueOf(buffer);

        return new LispSegmentLCAFAddress(res2, instanceId, address);
    }

	private interface Length {
        int INSTANCE = 4;
    }
}
