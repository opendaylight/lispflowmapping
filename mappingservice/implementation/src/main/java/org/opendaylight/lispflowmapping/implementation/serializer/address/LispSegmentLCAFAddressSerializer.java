package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.serializer.factory.LispAFIAddressSerializerFactory;
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
		LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(((LispSegmentLCAFAddress)lispAddress).getAddress().getAfi());
        return (short) (Length.INSTANCE + serializer.getAddressSize(((LispSegmentLCAFAddress)lispAddress).getAddress()));
    }

	@Override
    public void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        buffer.putInt(((LispSegmentLCAFAddress)lispAddress).getInstanceId());
        LispAddressSerializer.getInstance().serialize(buffer, ((LispSegmentLCAFAddress)lispAddress).getAddress());
    }
	
	@Override
	public LispSegmentLCAFAddress innerDeserialize(ByteBuffer buffer, byte res2, short length) {
        int instanceId = buffer.getInt();
        LispAddress address = LispAddressSerializer.getInstance().deserialize(buffer);

        return new LispSegmentLCAFAddress(res2, instanceId, address);
    }

	private interface Length {
        int INSTANCE = 4;
    }
}
