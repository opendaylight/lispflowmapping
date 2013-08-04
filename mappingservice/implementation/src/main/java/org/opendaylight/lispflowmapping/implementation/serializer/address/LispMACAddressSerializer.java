package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispMACAddress;

public class LispMACAddressSerializer extends LispAddressSerializer{
	
	private static final LispMACAddressSerializer INSTANCE = new LispMACAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispMACAddressSerializer() {
	}

	public static LispMACAddressSerializer getInstance() {
		return INSTANCE;
	}


	@Override
    public int getAddressSize(LispAddress lispAddress) {
		return super.getAddressSize(lispAddress) + 6;

    }
	
	@Override
	public LispMACAddress deserialize(ByteBuffer buffer) {
		byte[] macBuffer = new byte[6];
		buffer.get(macBuffer);
        return new LispMACAddress(macBuffer);
    }

    @Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
    	LispMACAddress lispMACAddress = (LispMACAddress)lispAddress;
        super.internalSerialize(buffer, lispMACAddress);
        buffer.put(lispMACAddress.getMAC());
    }

}
