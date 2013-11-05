package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.serializer.address.factory.LispAFIAddressSerializerFactory;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaftrafficengineeringaddress.HopsBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafTrafficEngineeringBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.reencaphop.HopBuilder;

public class LispTrafficEngineeringLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispTrafficEngineeringLCAFAddressSerializer INSTANCE = new LispTrafficEngineeringLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispTrafficEngineeringLCAFAddressSerializer() {
    }

    public static LispTrafficEngineeringLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        short totalSize = 0;
        for (Hops hop : ((LcafTrafficEngineeringAddress) lispAddress).getHops()) {
            totalSize += LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) hop.getHop().getPrimitiveAddress()) + 2;
        }
        return totalSize;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        for (Hops hop : ((LcafTrafficEngineeringAddress) lispAddress).getHops()) {
            serializeAFIAddressHeader(buffer, (LispAFIAddress) hop.getHop().getPrimitiveAddress());
            buffer.put((byte) 0);
            buffer.put((byte) (ByteUtil.boolToBit(hop.isLookup(), Flags.LOOKUP) | //
                    ByteUtil.boolToBit(hop.isRLOCProbe(), Flags.RLOC_PROBE) | //
            ByteUtil.boolToBit(hop.isStrict(), Flags.STRICT)));
            LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(AddressFamilyNumberEnum.valueOf(((LispAFIAddress) hop
                    .getHop().getPrimitiveAddress()).getAfi()));
            serializer.serializeData(buffer, (LispAFIAddress) hop.getHop());
        }
    }

    @Override
    protected LcafTrafficEngineeringAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        List<Hops> hops = new ArrayList<Hops>();
        while (length > 0) {
            short afi = buffer.getShort();
            LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(AddressFamilyNumberEnum.valueOf(afi));
            byte flags = (byte) buffer.getShort();
            boolean lookup = ByteUtil.extractBit(flags, Flags.LOOKUP);
            boolean RLOCProbe = ByteUtil.extractBit(flags, Flags.RLOC_PROBE);
            boolean strict = ByteUtil.extractBit(flags, Flags.STRICT);
            LispAFIAddress address = serializer.deserializeData(buffer);
            HopsBuilder builder = new HopsBuilder();
            builder.setLookup(lookup);
            builder.setRLOCProbe(RLOCProbe);
            builder.setStrict(strict);
            builder.setHop(new HopBuilder().setPrimitiveAddress((PrimitiveAddress) address).build());
            length -= LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) builder.getHop()) + 2;
            hops.add(builder.build());
        }
        return new LcafTrafficEngineeringBuilder().setHops(hops).build();
    }

    private interface Flags {
        int LOOKUP = 0x04;
        int RLOC_PROBE = 0x02;
        int STRICT = 0x01;
    }

}
