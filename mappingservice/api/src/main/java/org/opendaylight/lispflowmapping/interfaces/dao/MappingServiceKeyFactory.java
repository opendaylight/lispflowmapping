package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingServiceKeyFactory {

    protected static final Logger logger = LoggerFactory.getLogger(MappingServiceKeyFactory.class);

    private static final MappingServiceKeyFactory INSTANCE = new MappingServiceKeyFactory();

    private MappingServiceKeyFactory() {
    }

    public static MappingServiceKeyFactory getInstance() {
        return INSTANCE;
    }

    public IMappingServiceKey generateMappingServiceKey(LispAddress prefix, int mask) {
        if (shouldNormalize(prefix, mask)) {
            ((IMaskable) prefix).normalize(mask);
            return new MappingServiceKey(prefix, (byte) mask);
        } else {
            return new MappingServiceNoMaskKey(prefix);
        }
    }

    private boolean shouldNormalize(LispAddress prefix, int mask) {
        if (!(prefix instanceof IMaskable)) {
            return false;
        }
        IMaskable maskablePrefix = (IMaskable) prefix;
        if (mask > 0 && mask < maskablePrefix.getMaxMask()) {
            return true;
        } else {
            return false;
        }
    }

    public IMappingServiceKey generateMappingServiceKey(LispAddress prefix) {
        return generateMappingServiceKey(prefix, 0);
    }
}
