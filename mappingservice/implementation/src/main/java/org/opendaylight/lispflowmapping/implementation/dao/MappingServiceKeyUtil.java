/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.dao;

import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;

public class MappingServiceKeyUtil {

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
