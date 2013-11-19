package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.serializer.LispAFIConvertor;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafSourceDestAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafSourceDestBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;

public class LispSourceDestLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispSourceDestLCAFAddressSerializer INSTANCE = new LispSourceDestLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispSourceDestLCAFAddressSerializer() {
    }

    public static LispSourceDestLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        return (short) (Length.ALL_FIELDS
                + LispAddressSerializer.getInstance().getAddressSize(
                        (LispAFIAddress) ((LcafSourceDestAddress) lispAddress).getSrcAddress().getPrimitiveAddress()) + LispAddressSerializer
                .getInstance().getAddressSize((LispAFIAddress) ((LcafSourceDestAddress) lispAddress).getDstAddress().getPrimitiveAddress()));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LcafSourceDestAddress lispSourceDestLCAFAddress = ((LcafSourceDestAddress) lispAddress);
        buffer.putShort((short) 0);
        buffer.put(lispSourceDestLCAFAddress.getSrcMaskLength().byteValue());
        buffer.put(lispSourceDestLCAFAddress.getDstMaskLength().byteValue());
        LispAddressSerializer.getInstance().serialize(buffer, (LispAFIAddress) lispSourceDestLCAFAddress.getSrcAddress().getPrimitiveAddress());
        LispAddressSerializer.getInstance().serialize(buffer, (LispAFIAddress) lispSourceDestLCAFAddress.getDstAddress().getPrimitiveAddress());
    }

    @Override
    protected LcafSourceDestAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        short res = buffer.getShort();
        byte srcMaskLength = buffer.get();
        byte dstMaskLength = buffer.get();
        LispAFIAddress srcAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LispAFIAddress dstAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LcafSourceDestBuilder builder = new LcafSourceDestBuilder();
        builder.setDstMaskLength((short) dstMaskLength).setSrcMaskLength((short) srcMaskLength);
        builder.setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(srcAddress)).build());
        builder.setDstAddress(new DstAddressBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(dstAddress)).build());
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());

        return builder.build();
    }

    private interface Length {
        int SOURCE_MASK_LENGTH = 1;
        int DEST_MASK_LENGTH = 1;
        int RESERVED = 2;
        int ALL_FIELDS = SOURCE_MASK_LENGTH + DEST_MASK_LENGTH + RESERVED;
    }
}
