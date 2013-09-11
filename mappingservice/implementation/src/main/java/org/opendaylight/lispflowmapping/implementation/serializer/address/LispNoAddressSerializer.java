package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;

public class LispNoAddressSerializer extends LispAddressSerializer{

	private static final LispNoAddressSerializer INSTANCE = new LispNoAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispNoAddressSerializer() {
	}

	public static LispNoAddressSerializer getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected LispNoAddress deserializeData(ByteBuffer buffer) {
        return new LispNoAddress();
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return 0;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
    }

}
