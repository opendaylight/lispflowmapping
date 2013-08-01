package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;


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
	
	public static LispIpv4Address valueOf(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[4];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new LispIpv4Address(address);
    }



}
