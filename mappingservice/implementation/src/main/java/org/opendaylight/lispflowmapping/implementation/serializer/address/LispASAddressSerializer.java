package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispASAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public class LispASAddressSerializer extends LispAddressSerializer {

	private static final LispASAddressSerializer INSTANCE = new LispASAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispASAddressSerializer() {
	}

	public static LispASAddressSerializer getInstance() {
		return INSTANCE;
	}
	
	public static LispASAddress valueOf(ByteBuffer buffer) {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public int getAddressSize(LispAddress lispAddress) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
		throw new RuntimeException("Not implemented");
	}


}
