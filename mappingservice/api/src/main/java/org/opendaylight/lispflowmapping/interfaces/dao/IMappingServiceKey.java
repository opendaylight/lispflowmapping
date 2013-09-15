package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public interface IMappingServiceKey {
    
    LispAddress getEID();
    void setEID(LispAddress eID);
    byte getMask();
    void setMask(byte mask);

}
