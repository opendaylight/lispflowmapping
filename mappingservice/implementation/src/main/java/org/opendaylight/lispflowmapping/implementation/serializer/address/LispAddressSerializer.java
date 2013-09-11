package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;

public class LispAddressSerializer {

    private static final LispAddressSerializer INSTANCE = new LispAddressSerializer();

    // Private constructor prevents instantiation from other classes
    protected LispAddressSerializer() {
    }

    public static LispAddressSerializer getInstance() {
        return INSTANCE;
    }
    
    public void innerSerialize(ByteBuffer buffer, LispAddress lispAddress) {
        throw new RuntimeException("UnImplemented");
    }
    
    public LispAddress innerDeserialize(ByteBuffer buffer) {
        throw new RuntimeException("UnImplemented");
    }

    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(lispAddress.getAfi());
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI type=" + lispAddress.getAfi());
        }
        addAFIAddressHeader(buffer, lispAddress);
        serializer.innerSerialize(buffer, lispAddress);
    }

    protected static void addAFIAddressHeader(ByteBuffer buffer, LispAddress lispAddress) {
        buffer.putShort(lispAddress.getAfi().getIanaCode());
    }

    public int getAddressSize(LispAddress lispAddress) {
        return Length.AFI;
    }

    public LispAddress deserialize(ByteBuffer buffer) {
        short afi = buffer.getShort();
        AddressFamilyNumberEnum afiType = AddressFamilyNumberEnum.valueOf(afi);
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(afiType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI type=" + afiType);
        }
        try {
            return serializer.innerDeserialize(buffer);
        } catch (Exception e) {
            throw new LispSerializationException(e.getMessage());
        }
    }

    private interface Length {
        int AFI = 2;
    }
}
