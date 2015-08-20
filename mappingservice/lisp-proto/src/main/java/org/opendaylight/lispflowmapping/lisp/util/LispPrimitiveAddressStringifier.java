/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier.Destination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.AS;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Mac;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.No;

import com.google.common.base.Preconditions;

/**
 * Utility class with static methods returning string representations of
 * supported LISP simple address types
 *
 * @author Lorand Jakab
 *
 */
public class LispPrimitiveAddressStringifier {

    public static String getString(PrimitiveAddress addr) {
        return getString(Destination.USER, addr);
    }

    public static String getString(Destination dst, PrimitiveAddress addr) {
        Preconditions.checkNotNull(addr, "address should not be null");

        if (addr instanceof Ipv4) {
            return ((Ipv4) addr).getIpv4Address().getIpv4Address().getValue();
        } else if (addr instanceof Ipv6) {
            return ((Ipv6) addr).getIpv6Address().getIpv6Address().getValue();
        } else if (addr instanceof Mac) {
            return ((Mac) addr).getMacAddress().getMacAddress().getValue();
        } else if (addr instanceof DistinguishedName) {
            return ((DistinguishedName) addr).getDistinguishedNameAddress().getDistinguishedName();
        } else if (addr instanceof AS) {
            return "AS" + ((AS) addr).getASAddress().getAS();
        } else if (addr instanceof No) {
            if (dst == Destination.USER) {
                return "No Address Present";
            } else {
                return "" + LispAddressStringifier.noAddrSeq++;
            }
        }

        return null;
    }

    protected static String getURLPrefix(PrimitiveAddress addr) {
        Preconditions.checkNotNull(addr, "address should not be null");

        if (addr instanceof Ipv4) {
            return "ipv4";
        } else if (addr instanceof Ipv6) {
            return "ipv6";
        } else if (addr instanceof Mac) {
            return "mac";
        } else if (addr instanceof DistinguishedName) {
            return "dn";
        } else if (addr instanceof AS) {
            return "as";
        } else if (addr instanceof No) {
            return "no";
        }

        return null;
    }

}
