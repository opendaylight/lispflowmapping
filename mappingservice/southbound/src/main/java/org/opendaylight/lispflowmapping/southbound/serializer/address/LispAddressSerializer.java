package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public class LispAddressSerializer {
	
	private static final LispAddressSerializer INSTANCE = new LispAddressSerializer();

	// Private constructor prevents instantiation from other classes
	protected LispAddressSerializer() {
	}

	public static LispAddressSerializer getInstance() {
		return INSTANCE;
	}
	
	
	public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
		throw new RuntimeException("Not implemented");
	}
	
    protected void internalSerialize(ByteBuffer buffer, LispAddress lispAddress) {
        buffer.putShort(lispAddress.getAfi().getIanaCode());
    }
    
    public int getAddressSize(LispAddress lispAddress) {
        return Length.AFI;
    }

    public LispAddress deserialize(ByteBuffer buffer) {
        short afi = buffer.getShort();
        AddressFamilyNumberEnum afiType = AddressFamilyNumberEnum.valueOf(afi);
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(afiType);
        if (serializer == null) {
            throw new LispMalformedPacketException("Unknown AFI type=" + afiType);
        }
        try {
        	return serializer.deserialize(buffer);
        } catch (Exception e) {
        	 throw new LispMalformedPacketException(e.getMessage());
        }
    }
    
    private interface Length {
        int AFI = 2;
    }
}
