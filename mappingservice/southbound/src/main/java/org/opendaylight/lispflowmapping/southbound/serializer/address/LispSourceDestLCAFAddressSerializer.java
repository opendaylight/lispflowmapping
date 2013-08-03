package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSourceDestLCAFAddress;

public class LispSourceDestLCAFAddressSerializer extends LispLCAFAddressSerializer{
	
	private static final LispSourceDestLCAFAddressSerializer INSTANCE = new LispSourceDestLCAFAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispSourceDestLCAFAddressSerializer() {
	}

	public static LispSourceDestLCAFAddressSerializer getInstance() {
		return INSTANCE;
	}


	@Override
    public short getLcafLength(LispAddress lispAddress) {
		LispAddressSerializer srcSerializer = LispAddressSerializerFactory.getSerializer(((LispSourceDestLCAFAddress)lispAddress).getSrcAddress().getAfi());
		LispAddressSerializer dstSerializer = LispAddressSerializerFactory.getSerializer(((LispSourceDestLCAFAddress)lispAddress).getDstAddress().getAfi());
        return (short) (Length.INSTANCE + srcSerializer.getAddressSize(((LispSourceDestLCAFAddress)lispAddress).getSrcAddress()) + dstSerializer.getAddressSize(((LispSourceDestLCAFAddress)lispAddress).getDstAddress()));
    }

	@Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        LispSourceDestLCAFAddress lispSourceDestLCAFAddress = ((LispSourceDestLCAFAddress)lispAddress);
        buffer.putShort(lispSourceDestLCAFAddress.getReserved());
        buffer.put(lispSourceDestLCAFAddress.getSrcMaskLength());
        buffer.put(lispSourceDestLCAFAddress.getDstMaskLength());
        LispAddressSerializer srcSerializer = LispAddressSerializerFactory.getSerializer(lispSourceDestLCAFAddress.getSrcAddress().getAfi());
        if (srcSerializer == null) {
            throw new LispMalformedPacketException("Unknown AFI type=" + lispSourceDestLCAFAddress.getSrcAddress().getAfi());
        }
        srcSerializer.serialize(buffer, lispSourceDestLCAFAddress.getSrcAddress());
        LispAddressSerializer dstSerializer = LispAddressSerializerFactory.getSerializer(lispSourceDestLCAFAddress.getDstAddress().getAfi());
        if (dstSerializer == null) {
        	throw new LispMalformedPacketException("Unknown AFI type=" + lispSourceDestLCAFAddress.getDstAddress().getAfi());
        }
        dstSerializer.serialize(buffer, lispSourceDestLCAFAddress.getDstAddress());
    }
	
	@Override
	public LispSourceDestLCAFAddress innerDeserialize(ByteBuffer buffer, byte res2, short length) {
		short res = buffer.getShort();
		byte srcMaskLength = buffer.get();
		byte dstMaskLength = buffer.get();
        LispAddress srcAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LispAddress dstAddress = LispAddressSerializer.getInstance().deserialize(buffer);

        return new LispSourceDestLCAFAddress(res2, res, srcMaskLength, dstMaskLength, srcAddress, dstAddress);
    }

	private interface Length {
        int INSTANCE = 4;
    }
}
