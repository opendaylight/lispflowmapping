package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispASCILCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispBaseOneLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;

public class LispBaseOneLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispBaseOneLCAFAddressSerializer INSTANCE = new LispBaseOneLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    protected LispBaseOneLCAFAddressSerializer() {
    }

    public static LispBaseOneLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        if (lispAddress instanceof LispASCILCAFAddress) {
            return LispASCILCAFAddressSerializer.getInstance().getLcafLength(lispAddress);
        } else {
            return LispListLCAFAddressSerializer.getInstance().getLcafLength(lispAddress);

        }
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        if (lispAddress instanceof LispASCILCAFAddress) {
            LispASCILCAFAddressSerializer.getInstance().serializeData(buffer, lispAddress);
        } else {
            LispListLCAFAddressSerializer.getInstance().serializeData(buffer, lispAddress);
        }
    }

    @Override
    public LispBaseOneLCAFAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        if (length > 0) {
            int pos = buffer.position();
            short afi = buffer.getShort();
            buffer.position(pos);
            if (afi == AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode()) {
                return LispASCILCAFAddressSerializer.getInstance().deserializeData(buffer, res2, length);
            } else {
                return LispListLCAFAddressSerializer.getInstance().deserializeData(buffer, res2, length);

            }
        }
        List<LispAddress> addresses = new ArrayList<LispAddress>();
        while (length > 0) {
            LispAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
            length -= LispAddressSerializer.getInstance().getAddressSize(address);
            addresses.add(address);
        }
        return new LispListLCAFAddress(res2, addresses);
    }
}
