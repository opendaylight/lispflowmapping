package org.opendaylight.lispflowmapping.implementation.serializer.convertors;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;

public interface IAFIToContainerConvertor {

    public LispAFIAddress ToAFI(LispAddressContainer container);

    public LispAddressContainer ToContainer(LispAFIAddress address);
}
