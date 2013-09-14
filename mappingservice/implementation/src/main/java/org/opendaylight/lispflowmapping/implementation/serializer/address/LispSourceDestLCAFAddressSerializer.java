package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSourceDestLCAFAddress;

public class LispSourceDestLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispSourceDestLCAFAddressSerializer INSTANCE = new LispSourceDestLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispSourceDestLCAFAddressSerializer() {
    }

    public static LispSourceDestLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        return (short) (Length.ALL_FIELDS + LispAddressSerializer.getInstance().getAddressSize(((LispSourceDestLCAFAddress) lispAddress).getSrcAddress()) + LispAddressSerializer.getInstance()
                .getAddressSize(((LispSourceDestLCAFAddress) lispAddress).getDstAddress()));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        LispSourceDestLCAFAddress lispSourceDestLCAFAddress = ((LispSourceDestLCAFAddress) lispAddress);
        buffer.putShort(lispSourceDestLCAFAddress.getReserved());
        buffer.put(lispSourceDestLCAFAddress.getSrcMaskLength());
        buffer.put(lispSourceDestLCAFAddress.getDstMaskLength());
        LispAddressSerializer.getInstance().serialize(buffer, lispSourceDestLCAFAddress.getSrcAddress());
        LispAddressSerializer.getInstance().serialize(buffer, lispSourceDestLCAFAddress.getDstAddress());
    }

    @Override
    protected LispSourceDestLCAFAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        short res = buffer.getShort();
        byte srcMaskLength = buffer.get();
        byte dstMaskLength = buffer.get();
        LispAddress srcAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LispAddress dstAddress = LispAddressSerializer.getInstance().deserialize(buffer);

        return new LispSourceDestLCAFAddress(res2, res, srcMaskLength, dstMaskLength, srcAddress, dstAddress);
    }

    private interface Length {
        int SOURCE_MASK_LENGTH = 2;
        int DEST_MASK_LENGTH = 2;
        int ALL_FIELDS = SOURCE_MASK_LENGTH + DEST_MASK_LENGTH;
    }
}
