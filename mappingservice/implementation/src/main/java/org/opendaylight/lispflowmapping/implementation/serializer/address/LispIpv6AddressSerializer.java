package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;


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
	
	@Override
	public LispIpv6Address deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[16];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new LispIpv6Address(address);
    }

}
