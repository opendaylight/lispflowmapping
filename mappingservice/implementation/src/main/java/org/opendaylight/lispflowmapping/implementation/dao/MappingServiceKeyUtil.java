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
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;

public class MappingServiceKeyUtil {

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer lispAddressContainer, int mask) {
        LispAFIAddress address = LispAFIConvertor.toAFI(lispAddressContainer);
        if (MaskUtil.isMaskable(address)) {
            LispAddressContainer normalizedAddress = LispAFIConvertor.toContainer(MaskUtil.normalize(address, mask));
            return new MappingServiceKey(normalizedAddress, mask);
        } else {
            return new MappingServiceNoMaskKey(lispAddressContainer);
        }
    }

    public static IMappingServiceKey generateMappingServiceKey(LispAFIAddress address, int mask) {
        if (MaskUtil.isMaskable(address)) {
            LispAddressContainer normalizedAddress = LispAFIConvertor.toContainer(MaskUtil.normalize(address, mask));
            return new MappingServiceKey(normalizedAddress, mask);
        } else {
            return new MappingServiceNoMaskKey(LispAFIConvertor.toContainer(address));
        }
    }

    public static IMappingServiceKey generateMappingServiceKey(LispAddressContainer lispAddressContainer) {
        LispAFIAddress address = LispAFIConvertor.toAFI(lispAddressContainer);
        if (MaskUtil.isMaskable(address)) {
            return generateMappingServiceKey(lispAddressContainer, MaskUtil.getMaxMask(address));
        } else
            return generateMappingServiceKey(lispAddressContainer, 0);
    }
}
