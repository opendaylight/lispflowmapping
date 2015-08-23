/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.serializer;

import org.opendaylight.lispflowmapping.lisp.util.LcafSourceDestHelper;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafSegmentAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafSourceDestAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispIpv6Address;

/**
 * @author Florin Coras
 *
 *         Helper class needed to fix masks of EIDs in southbound incoming LISP packets.
 *
 *         RFC6830 defines masks and EIDs as separate fields of larger, encompassing blocks of LISP messages. However
 *         within LispFlowMapping masks are internal fields of EIDs. The method fixMask should be used in deserializers
 *         to ensure that masks for deserialized EIDs reflect accordingly the mask field values carried in LISP
 *         messages.
 */

public class SerializerHelper {
    public static LispAFIAddress fixMask(LispAFIAddress addr, short mask) {
        if (addr instanceof LispIpv4Address || addr instanceof LispIpv6Address || addr instanceof LcafSegmentAddress) {
           return MaskUtil.setMask(addr, mask);
        } else if (addr instanceof LcafSourceDestAddress) {
           return MaskUtil.setMaskSourceDest(addr, LcafSourceDestHelper.getSrcMask(addr),
                    LcafSourceDestHelper.getDstMask(addr));
        }
        return addr;
    }
}
