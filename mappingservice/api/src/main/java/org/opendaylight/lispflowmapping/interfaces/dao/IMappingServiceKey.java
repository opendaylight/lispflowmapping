package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;

public interface IMappingServiceKey {

    LispAddressContainer getEID();

    void setEID(LispAddressContainer eID);

    int getMask();

    void setMask(int mask);

}
