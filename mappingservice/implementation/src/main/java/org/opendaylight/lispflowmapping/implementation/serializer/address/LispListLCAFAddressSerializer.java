package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;

public class LispListLCAFAddressSerializer extends LispBaseOneLCAFAddressSerializer {

    private static final LispListLCAFAddressSerializer INSTANCE = new LispListLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispListLCAFAddressSerializer() {
    }

    public static LispListLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        short totalSize = 0;
        for (LispAddress address : ((LispListLCAFAddress) lispAddress).getAddresses()) {
            totalSize += LispAddressSerializer.getInstance().getAddressSize(address);
        }
        return totalSize;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        for (LispAddress address : ((LispListLCAFAddress) lispAddress).getAddresses()) {
            LispAddressSerializer.getInstance().serialize(buffer, address);
        }
    }

    @Override
    public LispListLCAFAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        List<LispAddress> addresses = new ArrayList<LispAddress>();
        while (length > 0) {
            LispAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
            length -= LispAddressSerializer.getInstance().getAddressSize(address);
            addresses.add(address);
        }
        return new LispListLCAFAddress(res2, addresses);
    }

}
