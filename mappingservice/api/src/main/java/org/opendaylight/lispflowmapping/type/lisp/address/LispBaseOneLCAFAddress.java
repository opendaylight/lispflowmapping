package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public abstract class LispBaseOneLCAFAddress extends LispLCAFAddress {

    public LispBaseOneLCAFAddress(byte res2) {
        super(LispCanonicalAddressFormatEnum.BASEONE, res2);
    }

}
