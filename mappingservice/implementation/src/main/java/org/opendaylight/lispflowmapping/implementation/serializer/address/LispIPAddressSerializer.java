package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIPAddress;

public abstract class LispIPAddressSerializer extends LispAddressSerializer{


    @Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
    	LispIPAddress lispIpvAddress = (LispIPAddress)lispAddress;
        super.internalSerialize(buffer, lispAddress);
        buffer.put(lispIpvAddress.getAddress().getAddress());
    }
}
