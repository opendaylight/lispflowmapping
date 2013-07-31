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
        serializer.serialize(buffer, lispAddress);
    }

	private interface Length {
        int INSTANCE = 4;
    }
}
