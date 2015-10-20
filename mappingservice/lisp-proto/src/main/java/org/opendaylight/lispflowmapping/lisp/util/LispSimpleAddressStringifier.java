/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier.Destination;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.NoAddress;

import com.google.common.base.Preconditions;

/**
 * Utility class with static methods returning string representations of
 * supported LISP simple address types
 *
 * @author Lorand Jakab
 *
 */
public class LispSimpleAddressStringifier {

    public static String getString(SimpleAddress addr) {
        return getString(Destination.USER, addr);
    }

    public static String getString(Destination dst, SimpleAddress addr) {
        Preconditions.checkNotNull(addr, "address should not be null");

        if (addr instanceof Ipv4) {
            return ((Ipv4) addr).getIpv4().getValue();
        } else if (addr instanceof Ipv6) {
            return ((Ipv6) addr).getIpv6().getValue();
        } else if (addr instanceof Mac) {
            return ((Mac) addr).getMac().getValue();
        } else if (addr instanceof DistinguishedName) {
            return ((DistinguishedName) addr).getDistinguishedName().getValue();
        } else if (addr instanceof AsNumber) {
            return "AS" + ((AsNumber) addr).getAsNumber().getValue();
        } else if (addr instanceof NoAddress) {
            if (dst == Destination.USER) {
                return "No Address Present";
            } else {
                return "" + LispAddressStringifier.noAddrSeq++;
            }
        }

        return null;
    }

    protected static String getURLPrefix(SimpleAddress addr) {
        Preconditions.checkNotNull(addr, "address should not be null");

        if (addr instanceof Ipv4) {
            return "ipv4";
        } else if (addr instanceof Ipv6) {
            return "ipv6";
        } else if (addr instanceof Mac) {
            return "mac";
        } else if (addr instanceof DistinguishedName) {
            return "dn";
        } else if (addr instanceof AsNumber) {
            return "as";
        } else if (addr instanceof NoAddress) {
            return "no";
        }

        return null;
    }

}
