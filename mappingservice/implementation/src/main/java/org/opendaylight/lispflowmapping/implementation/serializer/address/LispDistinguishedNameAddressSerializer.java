package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispDistinguishedNameAddress;

public class LispDistinguishedNameAddressSerializer extends LispAddressSerializer {

    private static final LispDistinguishedNameAddressSerializer INSTANCE = new LispDistinguishedNameAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispDistinguishedNameAddressSerializer() {
    }

    public static LispDistinguishedNameAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return ((LispDistinguishedNameAddress) lispAddress).getDistinguishedName().length() + 1;

    }

    @Override
    protected LispDistinguishedNameAddress deserializeData(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        byte b = buffer.get();
        while (b != 0) {
            sb.append((char) b);
            b = buffer.get();
        }
        return new LispDistinguishedNameAddress(sb.toString());
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) lispAddress;
        buffer.put(distinguishedNameAddress.getDistinguishedName().getBytes());
        buffer.put((byte) 0);
    }

}
