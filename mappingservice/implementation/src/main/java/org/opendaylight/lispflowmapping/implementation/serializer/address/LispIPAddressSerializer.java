package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIPAddress;

public abstract class LispIPAddressSerializer extends LispAddressSerializer{


    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
    	LispIPAddress lispIpvAddress = (LispIPAddress)lispAddress;
        buffer.put(lispIpvAddress.getAddress().getAddress());
    }
}
