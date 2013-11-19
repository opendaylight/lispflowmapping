package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispDistinguishedNameAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.DistinguishedNameBuilder;

public class LispDistinguishedNameAddressSerializer extends LispAddressSerializer {

    private static final LispDistinguishedNameAddressSerializer INSTANCE = new LispDistinguishedNameAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispDistinguishedNameAddressSerializer() {
    }

    public static LispDistinguishedNameAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
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
        return new DistinguishedNameBuilder().setAfi(AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode()).setDistinguishedName((sb.toString()))
                .build();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) lispAddress;
        buffer.put(distinguishedNameAddress.getDistinguishedName().getBytes());
        buffer.put((byte) 0);
    }

}
