package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispAFIConvertor;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafSegmentAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafsegmentaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafSegmentBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;

public class LispSegmentLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispSegmentLCAFAddressSerializer INSTANCE = new LispSegmentLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispSegmentLCAFAddressSerializer() {
    }

    public static LispSegmentLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        return (short) (Length.INSTANCE + LispAddressSerializer.getInstance().getAddressSize(
                (LispAFIAddress) ((LcafSegmentAddress) lispAddress).getAddress().getPrimitiveAddress()));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        buffer.putInt(((LcafSegmentAddress) lispAddress).getInstanceId().intValue());
        LispAddressSerializer.getInstance().serialize(buffer, (LispAFIAddress) ((LcafSegmentAddress) lispAddress).getAddress().getPrimitiveAddress());
    }

    @Override
    protected LcafSegmentAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        int instanceId = buffer.getInt();
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
        LcafSegmentBuilder builder = new LcafSegmentBuilder();
        builder.setInstanceId((long) instanceId);
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.SEGMENT.getLispCode())
                .setAddress(new AddressBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(address)).build());

        return builder.build();
    }

    private interface Length {
        int INSTANCE = 4;
    }
}
