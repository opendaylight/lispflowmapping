package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public class MappingServiceKeyFactory {

    public static IMappingServiceKey generateMappingServiceKey(LispAddress prefix, int mask) {
        if (prefix instanceof IMaskable && mask > 0 && mask < ((IMaskable)prefix).getMaxMask()) {
            ((IMaskable)prefix).normalize(mask);
            return new MappingServiceKey(prefix,(byte)mask);
        } else {
            return new MappingServiceNoMaskKey(prefix);
        }
    }
    
    public static IMappingServiceKey generateMappingServiceKey(LispAddress prefix) {
        return generateMappingServiceKey(prefix, 0);
    }
}
