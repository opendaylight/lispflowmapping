package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispApplicationDataLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;

public class LispApplicationDataLCAFAddressSerializer extends LispLCAFAddressSerializer{
	
	private static final LispApplicationDataLCAFAddressSerializer INSTANCE = new LispApplicationDataLCAFAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispApplicationDataLCAFAddressSerializer() {
	}

	public static LispApplicationDataLCAFAddressSerializer getInstance() {
		return INSTANCE;
	}


	@Override
    public short getLcafLength(LispAddress lispAddress) {
		LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(((LispApplicationDataLCAFAddress)lispAddress).getAddress().getAfi());
        return (short) (Length.INSTANCE + serializer.getAddressSize(lispAddress));
    }

	@Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        LispApplicationDataLCAFAddress applicationDataAddress = ((LispApplicationDataLCAFAddress)lispAddress);
        buffer.put(applicationDataAddress.getIPTos());
        buffer.put(applicationDataAddress.getProtocol());
        buffer.putShort(applicationDataAddress.getLocalPort());
        buffer.putShort(applicationDataAddress.getRemotePort());
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(applicationDataAddress.getAddress().getAfi());
        if (serializer == null) {
            throw new LispMalformedPacketException("Unknown AFI type=" + ((LispSegmentLCAFAddress)lispAddress).getAddress().getAfi());
        }
        serializer.serialize(buffer, ((LispApplicationDataLCAFAddress)lispAddress).getAddress());
    }
	
	@Override
	public LispApplicationDataLCAFAddress innerDeserialize(ByteBuffer buffer, byte res2, short length) {
		
		LispApplicationDataLCAFAddress applicationDataAddress = new LispApplicationDataLCAFAddress(res2);
		buffer.get(applicationDataAddress.getIPTos());
		applicationDataAddress.setProtocol(buffer.get());
		applicationDataAddress.setLocalPort(buffer.getShort());
		applicationDataAddress.setRemotePort(buffer.getShort());
        LispAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
        applicationDataAddress.setAddress(address);

        return applicationDataAddress;
    }
	
	private interface Length {
        int INSTANCE = 8;
    }
	
}
