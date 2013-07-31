package org.opendaylight.lispflowmapping.southbound.serializer.address;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;


public class LispIpv6AddressSerializer extends LispIPAddressSerializer{

	
	private static final LispIpv6AddressSerializer INSTANCE = new LispIpv6AddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispIpv6AddressSerializer() {
	}

	public static LispIpv6AddressSerializer getInstance() {
		return INSTANCE;
	}
	
	@Override
    public int getAddressSize(LispAddress lispAddress) {
        return super.getAddressSize(lispAddress) + 16;
    }

}
