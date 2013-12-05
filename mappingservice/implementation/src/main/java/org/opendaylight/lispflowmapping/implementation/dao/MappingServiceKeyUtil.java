package org.opendaylight.lispflowmapping.implementation.dao;

import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingServiceKeyUtil {

    protected static final Logger logger = LoggerFactory.getLogger(MappingServiceKeyUtil.class);

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer lispAddressContainer, int mask) {
        if (MaskUtil.isMaskable(lispAddressContainer.getAddress())) {
            LispAddressContainerBuilder normalizedBuilder = new LispAddressContainerBuilder();
            normalizedBuilder.setAddress(MaskUtil.normalize(lispAddressContainer.getAddress(), mask));
            return new MappingServiceKey(normalizedBuilder.build(), mask);
        } else {
            return new MappingServiceNoMaskKey(lispAddressContainer);
        }
    }

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer lispAddressContainer) {
        if (MaskUtil.isMaskable(lispAddressContainer.getAddress())) {
            return generateMappingServiceKey(lispAddressContainer, MaskUtil.getMaxMask(lispAddressContainer.getAddress()));
        } else
            return generateMappingServiceKey(lispAddressContainer, 0);
    }
}
