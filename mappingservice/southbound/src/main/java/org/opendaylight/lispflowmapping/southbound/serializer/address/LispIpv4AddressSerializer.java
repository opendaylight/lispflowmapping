package org.opendaylight.lispflowmapping.southbound.serializer.address;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;


public class LispIpv4AddressSerializer extends LispIPAddressSerializer{
	
	private static final LispIpv4AddressSerializer INSTANCE = new LispIpv4AddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispIpv4AddressSerializer() {
	}

	public static LispIpv4AddressSerializer getInstance() {
		return INSTANCE;
	}
	
	
	@Override
    public int getAddressSize(LispAddress lispAddress) {
        return super.getAddressSize(lispAddress) + 4;
    }



}
