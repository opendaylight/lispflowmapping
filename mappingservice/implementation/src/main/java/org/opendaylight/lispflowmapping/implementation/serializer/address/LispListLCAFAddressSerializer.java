package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafListAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaflistaddress.Addresses;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaflistaddress.AddressesBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafListBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;

public class LispListLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispListLCAFAddressSerializer INSTANCE = new LispListLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispListLCAFAddressSerializer() {
    }

    public static LispListLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        short totalSize = 0;
        for (Addresses address : ((LcafListAddress) lispAddress).getAddresses()) {
            totalSize += LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) address.getPrimitiveAddress());
        }
        return totalSize;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        for (Addresses address : ((LcafListAddress) lispAddress).getAddresses()) {
            LispAddressSerializer.getInstance().serialize(buffer, (LispAFIAddress) address.getPrimitiveAddress());
        }
    }

    @Override
    public LcafListAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        List<Addresses> addresses = new ArrayList<Addresses>();
        while (length > 0) {
            PrimitiveAddress address = LispAFIConvertor.toPrimitive(LispAddressSerializer.getInstance().deserialize(buffer));
            length -= LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) address);
            addresses.add(new AddressesBuilder().setPrimitiveAddress((PrimitiveAddress) address).build());
        }
        return new LcafListBuilder().setAddresses(addresses).setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.LIST.getLispCode()).build();
    }
}
