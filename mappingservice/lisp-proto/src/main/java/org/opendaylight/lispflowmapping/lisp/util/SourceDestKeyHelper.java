/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.util;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafSourceDestAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.DstAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.SrcAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lorand Jakab
 *
 */

public class SourceDestKeyHelper {
    private final static Logger LOG = LoggerFactory.getLogger(SourceDestKeyHelper.class);
    public static LispAFIAddress getSrcAfi(LispAddressContainer addr) {
        if (!isSrcDst(addr)) {
            return LispAFIConvertor.toAFI(addr);
        }
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDest) addr.getAddress()).getLcafSourceDestAddr()
                .getSrcAddress().getPrimitiveAddress());
    }

    public static LispAFIAddress getSrcAfi(LispAFIAddress addr) {
        if (!isSrcDst(addr)) {
            return addr;
        }
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddress) addr)
                .getSrcAddress().getPrimitiveAddress());
    }

    public static LispAFIAddress getDstAfi(LispAddressContainer addr) {
        if (!isSrcDst(addr)) {
            return LispAFIConvertor.toAFI(addr);
        }
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDest) addr.getAddress()).getLcafSourceDestAddr()
                .getDstAddress().getPrimitiveAddress());
    }

    public static LispAFIAddress getDstAfi(LispAFIAddress addr) {
        if (!isSrcDst(addr)) {
            return addr;
        }
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddress) addr)
                .getDstAddress().getPrimitiveAddress());
    }

    public static Eid getSrc(Eid eid) {
        Address addr = eid.getAddress();
        if (addr instanceof SourceDestKey) {
            return LispAFIConvertor.toEid(eid, ((SourceDestKey)addr).getSourceDestKey().getSource());
        } else {
            return eid;
        }
    }

    public static Eid getDst(Eid eid) {
        Address addr = eid.getAddress();
        if (addr instanceof SourceDestKey) {
            return LispAFIConvertor.toEid(eid, ((SourceDestKey)addr).getSourceDestKey().getDest());
        } else {
            return eid;
        }
    }

    public static LispAddressContainer getDst(LispAddressContainer addr) {
        if (!isSrcDst(addr)) {
            return addr;
        }
        return LispAFIConvertor.toContainer(LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDest) addr
                .getAddress()).getLcafSourceDestAddr().getDstAddress().getPrimitiveAddress()));
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

    public static short getSrcMask(LispAFIAddress addr) {
        if (!isSrcDst(addr)) {
            return 0;
        }
        return ((LcafSourceDestAddress) addr).getSrcMaskLength();
    }

    public static short getDstMask(LispAFIAddress addr) {
        if (!isSrcDst(addr)) {
            return 0;
        }
        return ((LcafSourceDestAddress) addr).getDstMaskLength();
    }

    public static DstAddress getDstAddress(LispAFIAddress addr) {
        if (!isSrcDst(addr)) {
            return null;
        }
        return ((LcafSourceDestAddress)addr).getDstAddress();
    }

    public static SrcAddress getSrcAddress(LispAFIAddress addr) {
        if (!isSrcDst(addr)) {
            return null;
        }
        return ((LcafSourceDestAddress)addr).getSrcAddress();
    }

    public static DstAddress getDstAddress(LispAddressContainer addr) {
        if (!isSrcDst(addr)) {
            return null;
        }
        return ((LcafSourceDest)addr.getAddress()).getLcafSourceDestAddr().getDstAddress();
    }

    public static SrcAddress getSrcAddress(LispAddressContainer addr) {
        if (!isSrcDst(addr)) {
            return null;
        }
        return ((LcafSourceDest)addr.getAddress()).getLcafSourceDestAddr().getSrcAddress();
    }


    private static boolean isSrcDst(Address addr) {
        if (!(addr instanceof SourceDestKey)) {
            LOG.warn("Address {} is not a valid SourceDest LCAF", addr);
            return false;
        }
        return true;
    }

    private static boolean isSrcDst(LispAFIAddress addr) {
        if (!(addr instanceof LcafSourceDestAddress)) {
            LOG.warn("Address {} is not a valid SourceDest LCAF", addr);
            return false;
        }
        return true;
    }

    private static boolean isSrcDst(LispAddressContainer addr) {
        if (!(addr.getAddress() instanceof LcafSourceDest)) {
            LOG.warn("Address {} is not a valid SourceDest LCAF", addr);
            return false;
        }
        return true;
    }
}
