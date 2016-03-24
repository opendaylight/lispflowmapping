/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier.Destination;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;

import com.google.common.base.Preconditions;

/**
 * Utility class with static methods returning string representations of
 * supported LISP simple address types
 *
 * @author Lorand Jakab
 *
 */
public final class LispSimpleAddressStringifier {
    // Utility class, should not be instantiated
    private LispSimpleAddressStringifier() {
    }

    public static String getString(SimpleAddress addr) {
        return getString(Destination.USER, addr);
    }

    public static String getString(Destination dst, SimpleAddress addr) {
        Preconditions.checkNotNull(addr, "address should not be null");

        if (addr.getIpAddress() != null) {
            if (addr.getIpAddress().getIpv4Address() != null) {
                return addr.getIpAddress().getIpv4Address().getValue();
            } else if (addr.getIpAddress().getIpv6Address() != null) {
                return addr.getIpAddress().getIpv6Address().getValue();
            }
        } else if (addr.getIpPrefix() != null) {
            if (addr.getIpPrefix().getIpv4Prefix() != null) {
                return addr.getIpPrefix().getIpv4Prefix().getValue();
            } else if (addr.getIpPrefix().getIpv6Prefix() != null) {
                return addr.getIpPrefix().getIpv6Prefix().getValue();
            }
        } else if (addr.getMacAddress() != null) {
            return addr.getMacAddress().getValue();
        } else if (addr.getDistinguishedNameType() != null) {
            return (addr.getDistinguishedNameType().getValue());
        } else if (addr.getAsNumber() != null) {
            return "AS" + addr.getAsNumber().getValue();
        }

        return null;
    }

    protected static String getURLPrefix(SimpleAddress addr) {
        Preconditions.checkNotNull(addr, "address should not be null");

        if (addr.getIpAddress() != null) {
            if (addr.getIpAddress().getIpv4Address() != null) {
                return "ipv4";
            } else if (addr.getIpAddress().getIpv6Address() != null) {
                return "ipv6";
            }
        } else if (addr.getIpPrefix() != null) {
            if (addr.getIpPrefix().getIpv4Prefix() != null) {
                return "ipv4";
            } else if (addr.getIpPrefix().getIpv6Prefix() != null) {
                return "ipv6";
            }
        } else if (addr.getMacAddress() != null) {
            return "mac";
        } else if (addr.getDistinguishedNameType() != null) {
            return "dn";
        } else if (addr.getAsNumber() != null) {
            return "as";
        }

        return null;
    }

}
