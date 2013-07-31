package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;

public abstract class LispIPAddressSerializer extends LispAddressSerializer{


    @Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
    	LispIpv4Address lispIpv4Address = (LispIpv4Address)lispAddress;
        super.internalSerialize(buffer, lispAddress);
        buffer.put(lispIpv4Address.getAddress().getAddress());
    }
}
