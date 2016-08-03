/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class MapResolverUtil {

    // Util class can not be instantiated.
    private MapResolverUtil() {
    }

    public static boolean isEqualIpVersion(IpAddressBinary srcRloc, Rloc rloc) {
        if (srcRloc.getIpv4AddressBinary() != null) {
            if (rloc.getAddressType() == Ipv4Afi.class ||
                    rloc.getAddressType() == Ipv4BinaryAfi.class ||
                    rloc.getAddressType() == Ipv4PrefixAfi.class ||
                    rloc.getAddressType() == Ipv4PrefixBinaryAfi.class) {

                return true;
            }
        } else if (rloc.getAddressType() == Ipv6Afi.class ||
                rloc.getAddressType() == Ipv6BinaryAfi.class ||
                rloc.getAddressType() == Ipv6PrefixAfi.class ||
                rloc.getAddressType() == Ipv6PrefixBinaryAfi.class) {

            return true;
        }
        return false;
    }
}
