package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.lisp.address.ApplicationData;
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
		LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(((LispApplicationDataLCAFAddress)lispAddress).getApplicationData().getAddress().getAfi());
        return (short) (Length.INSTANCE + serializer.getAddressSize(lispAddress));
    }

	@Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        LispApplicationDataLCAFAddress applicationDataAddress = ((LispApplicationDataLCAFAddress)lispAddress);
        buffer.put(applicationDataAddress.getApplicationData().getIPTos());
        buffer.put(applicationDataAddress.getApplicationData().getProtocol());
        buffer.putShort(applicationDataAddress.getApplicationData().getLocalPort());
        buffer.putShort(applicationDataAddress.getApplicationData().getRemotePort());
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(applicationDataAddress.getApplicationData().getAddress().getAfi());
        if (serializer == null) {
            throw new LispMalformedPacketException("Unknown AFI type=" + ((LispSegmentLCAFAddress)lispAddress).getAddress().getAfi());
        }
        serializer.serialize(buffer, ((LispApplicationDataLCAFAddress)lispAddress).getApplicationData().getAddress());
    }
	
	@Override
	public LispApplicationDataLCAFAddress innerDeserialize(ByteBuffer buffer, byte res2, short length) {
		
		ApplicationData appData = new ApplicationData();
		buffer.get(appData.getIPTos());
		appData.setProtocol(buffer.get());
		appData.setLocalPort(buffer.getShort());
		appData.setRemotePort(buffer.getShort());
        LispAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
        appData.setAddress(address);

        return new LispApplicationDataLCAFAddress(res2, appData);
    }
	
	private interface Length {
        int INSTANCE = 8;
    }
	
}
