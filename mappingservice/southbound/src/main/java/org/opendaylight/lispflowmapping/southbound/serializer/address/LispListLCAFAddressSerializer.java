package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;

public class LispListLCAFAddressSerializer extends LispLCAFAddressSerializer{
	
	private static final LispListLCAFAddressSerializer INSTANCE = new LispListLCAFAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispListLCAFAddressSerializer() {
	}

	public static LispListLCAFAddressSerializer getInstance() {
		return INSTANCE;
	}


	@Override
    public short getLcafLength(LispAddress lispAddress) {
        short totalSize = 0;
        for (LispAddress address : ((LispListLCAFAddress)lispAddress).getAddresses()) {
            totalSize += LispAddressSerializerFactory.getSerializer(address).getAddressSize(lispAddress);
        }
        return totalSize;
    }

	@Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        for (LispAddress address : ((LispListLCAFAddress)lispAddress).getAddresses()) {
        	LispAddressSerializerFactory.getSerializer(address).serialize(buffer, address);
        }
    }
	
	public static LispListLCAFAddress valueOf(byte res2, short length, ByteBuffer buffer) {
        List<LispAddress> addresses = new ArrayList<LispAddress>();
        while (length > 0) {
            LispAddress address = LispAddressSerializer.valueOf(buffer);
            length -= LispAddressSerializerFactory.getSerializer(address).getAddressSize(address);
            addresses.add(address);
        }
        return new LispListLCAFAddress(res2, addresses);
    }
	
}
