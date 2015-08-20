/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.dao;

import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingKey;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;

public class MappingKeyUtil {

    public static IMappingKey generateMappingKey(LispAddressContainer lispAddressContainer, int mask) {
        LispAFIAddress address = LispAFIConvertor.toAFI(lispAddressContainer);
        if (MaskUtil.isMaskable(address)) {
            LispAddressContainer normalizedAddress = LispAFIConvertor.toContainer(MaskUtil.normalize(address, mask));
            return new MappingKey(normalizedAddress, mask);
        } else {
            return new MappingNoMaskKey(lispAddressContainer);
        }
    }

    public static IMappingKey generateMappingKey(LispAFIAddress address, int mask) {
        if (MaskUtil.isMaskable(address)) {
            LispAddressContainer normalizedAddress = LispAFIConvertor.toContainer(MaskUtil.normalize(address, mask));
            return new MappingKey(normalizedAddress, mask);
        } else {
            return new MappingNoMaskKey(LispAFIConvertor.toContainer(address));
        }
    }

    public static IMappingKey generateMappingKey(LispAddressContainer lispAddressContainer) {
        LispAFIAddress address = LispAFIConvertor.toAFI(lispAddressContainer);
        if (MaskUtil.isMaskable(address)) {
            return generateMappingKey(lispAddressContainer, MaskUtil.getMaxMask(address));
        } else
            return generateMappingKey(lispAddressContainer, 0);
    }
}
