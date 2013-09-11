package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispLCAFAddress;

public class LispLCAFAddressSerializer extends LispAddressSerializer{
	
	private static final LispLCAFAddressSerializer INSTANCE = new LispLCAFAddressSerializer();

	// Private constructor prevents instantiation from other classes
	protected LispLCAFAddressSerializer() {
	}

	public static LispLCAFAddressSerializer getInstance() {
		return INSTANCE;
	}

	@Override
	public LispLCAFAddress innerDeserialize(ByteBuffer buffer) {
        buffer.position(buffer.position() + Length.RES + Length.FLAGS);
        byte lispCode = buffer.get();
        LispCanonicalAddressFormatEnum lcafType = LispCanonicalAddressFormatEnum.valueOf(lispCode);
        byte res2 = buffer.get();
        short length = buffer.getShort();

        LispLCAFAddressSerializer serializer = LispAddressSerializerFactory.getLCAFSerializer(lcafType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown LispLCAFAddress type=" + lispCode);
        }
        return serializer.innerDeserialize(buffer, res2, length);
    }
	
	public LispLCAFAddress innerDeserialize(ByteBuffer buffer, byte res2, short length) {
		throw new RuntimeException("Not implemented");
	}

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return super.getAddressSize(lispAddress) + Length.LCAF_HEADER + LispAddressSerializerFactory.getLCAFSerializer(((LispLCAFAddress)lispAddress).getType()).getLcafLength(lispAddress);
    }
    
    public short getLcafLength(LispAddress lispAddress) {
    	throw new RuntimeException("Not implemented");
    }
    
    @Override
    public void innerSerialize(ByteBuffer buffer, LispAddress lispAddress) {
    	LispLCAFAddressSerializer lcafSerializer = LispAddressSerializerFactory.getLCAFSerializer(((LispLCAFAddress)lispAddress).getType());
    	addLCAFAddressHeader(buffer, lispAddress);
        lcafSerializer.innerSerialize(buffer, lispAddress);
    }

    public static void addLCAFAddressHeader(ByteBuffer buffer, LispAddress lispAddress) {
        LispLCAFAddress lispLCAFAddress = (LispLCAFAddress)lispAddress;
        buffer.putShort((short) 0); // RES + Flags.
        buffer.put(lispLCAFAddress.getType().getLispCode());
        buffer.put(lispLCAFAddress.getRes2());
        LispLCAFAddressSerializer lcafSerializer = LispAddressSerializerFactory.getLCAFSerializer(lispLCAFAddress.getType());
        buffer.putShort(lcafSerializer.getLcafLength(lispAddress));
    }
    private interface Length {
        int RES = 1;
        int FLAGS = 1;

        int LCAF_HEADER = 6;
    }
}
