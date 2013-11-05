package org.opendaylight.lispflowmapping.implementation.serializer.convertors;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;

public class LispAFIToContainerConvertorFactory {

    public static LispAddressContainer toContainer(LispAFIAddress address) {
        if (address instanceof Address) {
            return new LispAddressContainerBuilder().setAddress((Address) address).build();
        } else {
            return null;
        }
    }

    public static LispAFIAddress toAFI(LispAddressContainer container) {
        return (LispAFIAddress) container.getAddress();
    }
}
