package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingServiceKeyUtil {

    protected static final Logger logger = LoggerFactory.getLogger(MappingServiceKeyUtil.class);

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer lispAddressContainer, int mask) {
        if (shouldNormalize(lispAddressContainer, mask)) {
            ((IMaskable) lispAddressContainer).normalize(mask);
            return new MappingServiceKey(lispAddressContainer, (byte) mask);
        } else {
            return new MappingServiceNoMaskKey(lispAddressContainer);
        }
    }

    private static boolean shouldNormalize(LispAddressContainer lispAddressContainer, int mask) {
        if (!(lispAddressContainer instanceof IMaskable)) {
            return false;
        }
        IMaskable maskablePrefix = (IMaskable) lispAddressContainer;
        if (mask >= 0 && mask < maskablePrefix.getMaxMask()) {
            return true;
        } else {
            return false;
        }
    }

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer prefix) {
        if (prefix instanceof IMaskable) {
            return generateMappingServiceKey(prefix, ((IMaskable) prefix).getMaxMask());
        } else
            return generateMappingServiceKey(prefix, 0);
    }
}
