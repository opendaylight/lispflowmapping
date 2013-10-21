package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public interface IMappingServiceKey {

    LispAddress getEID();

    void setEID(LispAddress eID);

    int getMask();

    void setMask(byte mask);

}
