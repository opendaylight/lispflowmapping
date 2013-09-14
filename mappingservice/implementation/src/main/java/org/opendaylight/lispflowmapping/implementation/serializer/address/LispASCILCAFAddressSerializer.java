package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispASCILCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispDistinguishedNameAddress;

public class LispASCILCAFAddressSerializer extends LispBaseOneLCAFAddressSerializer {

    private static final LispASCILCAFAddressSerializer INSTANCE = new LispASCILCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispASCILCAFAddressSerializer() {
    }

    public static LispASCILCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        return (short) (((LispASCILCAFAddress) lispAddress).getdistinguishedName().getDistinguishedName().length() + 3);
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        buffer.putShort(AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode());
        buffer.put(((LispASCILCAFAddress) lispAddress).getdistinguishedName().getDistinguishedName().getBytes());
        buffer.put((byte) 0);
    }

    @Override
    public LispASCILCAFAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        buffer.getShort();
        byte[] distinguishedNameString = new byte[length - 3];
        buffer.get(distinguishedNameString);
        LispDistinguishedNameAddress distinguishedName = new LispDistinguishedNameAddress(new String(distinguishedNameString));
        return new LispASCILCAFAddress(res2, distinguishedName);
    }
}
