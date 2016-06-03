/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.util;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lorand Jakab
 *
 */

public final class SourceDestKeyHelper {
    // Utility class, should not be instantiated
    private SourceDestKeyHelper() {
    }

    private final static Logger LOG = LoggerFactory.getLogger(SourceDestKeyHelper.class);
    public static Eid getSrc(Eid eid) {
        Address addr = eid.getAddress();
        if (addr instanceof SourceDestKey) {
            return LispAddressUtil.asEid(((SourceDestKey) addr).getSourceDestKey().getSource(),
                    eid.getVirtualNetworkId());
        } else {
            return eid;
        }
    }

    public static Eid getDst(Eid eid) {
        Address addr = eid.getAddress();
        if (addr instanceof SourceDestKey) {
            return LispAddressUtil.asEid(((SourceDestKey) addr).getSourceDestKey().getDest(),
                    eid.getVirtualNetworkId());
        } else {
            return eid;
        }
    }

    public static Eid getSrcBinary(Eid eid) {
        if (eid.getAddress() instanceof SourceDestKey) {
            return LispAddressUtil.asBinaryEid(((SourceDestKey) eid.getAddress()).getSourceDestKey().getSource(),
                    eid.getVirtualNetworkId());
        }
        return eid;
    }

    public static Eid getDstBinary(Eid eid) {
        if (eid.getAddress() instanceof SourceDestKey) {
            return LispAddressUtil.asBinaryEid(((SourceDestKey) eid.getAddress()).getSourceDestKey().getDest(),
                    eid.getVirtualNetworkId());
        }
        return eid;
    }

    public static short getSrcMask(Eid eid) {
        Address addr = eid.getAddress();
        if (!isSrcDst(addr)) {
            return 0;
        }
        return MaskUtil.getMaskForAddress(((SourceDestKey)addr).getSourceDestKey().getSource());
    }

    public static short getDstMask(Eid eid) {
        Address addr = eid.getAddress();
        if (!isSrcDst(addr)) {
            return 0;
        }
        return MaskUtil.getMaskForAddress(((SourceDestKey)addr).getSourceDestKey().getDest());
    }

    private static boolean isSrcDst(Address addr) {
        if (!(addr instanceof SourceDestKey)) {
            LOG.warn("Address {} is not a valid SourceDest LCAF", addr);
            return false;
        }
        return true;
    }
}
