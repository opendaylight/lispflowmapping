package org.opendaylight.lispflowmapping.implementation.serializer.convertors;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4;

public class IPV4Convertor implements IAFIToContainerConvertor {

    private static final IPV4Convertor INSTANCE = new IPV4Convertor();

    // Private constructor prevents instantiation from other classes
    private IPV4Convertor() {
    }

    public static IPV4Convertor getInstance() {
        return INSTANCE;
    }

    @Override
    public LispAFIAddress ToAFI(LispAddressContainer container) {
        return (Ipv4) container.getAddress();
    }

    @Override
    public LispAddressContainer ToContainer(LispAFIAddress address) {
        // TODO Auto-generated method stub
        return null;
    }

}
