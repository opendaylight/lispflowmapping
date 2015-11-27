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

public class SourceDestKeyHelper {
    private final static Logger LOG = LoggerFactory.getLogger(SourceDestKeyHelper.class);
    public static Eid getSrc(Eid eid) {
        Address addr = eid.getAddress();
        if (addr instanceof SourceDestKey) {
            return LispAddressUtil.toEid(eid, ((SourceDestKey)addr).getSourceDestKey().getSource());
        } else {
            return eid;
        }
    }

    public static Eid getDst(Eid eid) {
        Address addr = eid.getAddress();
        if (addr instanceof SourceDestKey) {
            return LispAddressUtil.toEid(eid, ((SourceDestKey)addr).getSourceDestKey().getDest());
        } else {
            return eid;
        }
    }

    public static short getSrcMask(Eid eid) {
        Address addr = eid.getAddress();
        if (!isSrcDst(addr)) {
            return 0;
        }
        return MaskUtil.getMaskForIpPrefix(((SourceDestKey)addr).getSourceDestKey().getSource());
    }

    public static short getDstMask(Eid eid) {
        Address addr = eid.getAddress();
        if (!isSrcDst(addr)) {
            return 0;
        }
        return MaskUtil.getMaskForIpPrefix(((SourceDestKey)addr).getSourceDestKey().getDest());
    }

    private static boolean isSrcDst(Address addr) {
        if (!(addr instanceof SourceDestKey)) {
            LOG.warn("Address {} is not a valid SourceDest LCAF", addr);
            return false;
        }
        return true;
    }
}
