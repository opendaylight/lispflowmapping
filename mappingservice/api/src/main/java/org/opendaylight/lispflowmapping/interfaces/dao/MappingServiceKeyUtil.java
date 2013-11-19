package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.MaskUtils;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingServiceKeyUtil {

    protected static final Logger logger = LoggerFactory.getLogger(MappingServiceKeyUtil.class);

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer lispAddressContainer, int mask) {
        if (MaskUtils.isMaskable(lispAddressContainer.getAddress(), mask)) {
            LispAddressContainerBuilder normalizedBuilder = new LispAddressContainerBuilder();
            normalizedBuilder.setAddress(MaskUtils.normalize(lispAddressContainer.getAddress(), mask));
            return new MappingServiceKey(normalizedBuilder.build(), mask);
        } else {
            return new MappingServiceNoMaskKey(lispAddressContainer);
        }
    }

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer prefix) {
        if (MaskUtils.isMaskable(prefix.getAddress(), 0)) {
            return generateMappingServiceKey(prefix, MaskUtils.getMaxMask(prefix.getAddress()));
        } else
            return generateMappingServiceKey(prefix, 0);
    }
}
