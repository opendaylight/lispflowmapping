package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
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
		LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(((LispSegmentLCAFAddress)lispAddress).getAddress().getAfi());
        return (short) (Length.INSTANCE + serializer.getAddressSize(lispAddress));
    }

	@Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        buffer.putInt(((LispSegmentLCAFAddress)lispAddress).getInstanceId());
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(((LispSegmentLCAFAddress)lispAddress).getAddress().getAfi());
        if (serializer == null) {
            throw new LispMalformedPacketException("Unknown AFI type=" + ((LispSegmentLCAFAddress)lispAddress).getAddress().getAfi());
        }
        serializer.serialize(buffer, ((LispSegmentLCAFAddress)lispAddress).getAddress());
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
