/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AfiList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.NoAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop.LrsBits;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;

import com.google.common.base.Preconditions;

/**
 * Utility class with static methods returning string representations of
 * supported LISP address types, both for use in URLs and for user friendly
 * output.
 *
 * @author Lorand Jakab
 *
 */
public class LispAddressStringifier {

    private static final String PREFIX_SEPARATOR = ":";
    /*
     * In the unlikely event that a AFI 0 address is used a key, we use a
     * sequence number to ensure uniqueness
     */
    protected static int noAddrSeq = 0;
    /*
     * There are three possible destinations for rendering an address as a
     * string:
     *
     *   * General user interaction, like log and CLI output, GUI
     *     representation, etc.
     *   * As a URI string that is the entry key in the MD-SAL datastore
     *   * As a URL string that is the same as the URI string, except for
     *     characters not accepted in URLs are percent encoded (e.g. '/' is
     *     encoded as '%2f'
     */
    protected enum Destination {
        USER,
        URI,
        URL;
    }

    public static String getString(LispAddress lispAddress) {
        return getAddrString(Destination.USER, lispAddress);
    }

    public static String getURIString(LispAddress lispAddress) {
        return getAddrString(Destination.URI, lispAddress);
    }

    public static String getURLString(LispAddress lispAddress) {
        return getAddrString(Destination.URL, lispAddress);
    }

    private static String getAddrString(Destination dst, LispAddress lispAddress) {
        Preconditions.checkNotNull(lispAddress, "lispAddress should not be null");
        Address addr = lispAddress.getAddress();
        String prefix = null;
        String address = null;

        if (addr instanceof Ipv4) {
            prefix = "ipv4" + PREFIX_SEPARATOR;
            address = getStringFromIpv4(dst, (Ipv4) addr);
        } else if (addr instanceof Ipv4Prefix) {
            prefix = "ipv4" + PREFIX_SEPARATOR;
            address = getStringFromIpv4Prefix(dst, (Ipv4Prefix) addr);
        } else if (addr instanceof Ipv6) {
            prefix = "ipv6" + PREFIX_SEPARATOR;
            address = getStringFromIpv6(dst, (Ipv6) addr);
        } else if (addr instanceof Ipv6Prefix) {
            prefix = "ipv6" + PREFIX_SEPARATOR;
            address = getStringFromIpv6Prefix(dst, (Ipv6Prefix) addr);
        } else if (addr instanceof Mac) {
            prefix = "mac" + PREFIX_SEPARATOR;
            address = getStringFromMac(dst, (Mac) addr);
        } else if (addr instanceof InstanceId) {
            SimpleAddress pa = ((InstanceId)addr).getInstanceId().getAddress();
            // Instance ID is a separate parent hierarchy, so we use the simple address prefix
            prefix = LispSimpleAddressStringifier.getURLPrefix(pa) + PREFIX_SEPARATOR;
            address = getStringFromInstanceId(dst, (InstanceId) addr);
        } else if (addr instanceof NoAddress) {
            prefix = "no" + PREFIX_SEPARATOR;
            address = getStringFromNoAddress(dst, (NoAddress) addr);
        } else if (addr instanceof DistinguishedName) {
            prefix = "dn" + PREFIX_SEPARATOR;
            address = getStringFromDistinguishedName(dst, (DistinguishedName) addr);
        } else if (addr instanceof AsNumber) {
            prefix = "as" + PREFIX_SEPARATOR;
            address = getStringFromAsNumber(dst, (AsNumber) addr);
        } else if (addr instanceof AfiList) {
            prefix = "list" + PREFIX_SEPARATOR;
            address = getStringFromAfiList(dst, (AfiList) addr);
        } else if (addr instanceof ApplicationData) {
            prefix = "appdata" + PREFIX_SEPARATOR;
            address = getStringFromApplicationData(dst, (ApplicationData) addr);
        } else if (addr instanceof ExplicitLocatorPath) {
            prefix = "elp" + PREFIX_SEPARATOR;
            address = getStringFromExplicitLocatorPath(dst, (ExplicitLocatorPath) addr);
        } else if (addr instanceof SourceDestKey) {
            prefix = "srcdst" + PREFIX_SEPARATOR;
            address = getStringFromSourceDestKey(dst, (SourceDestKey) addr);
        } else if (addr instanceof KeyValueAddress) {
            prefix = "kv" + PREFIX_SEPARATOR;
            address = getStringFromKeyValueAddress(dst, (KeyValueAddress) addr);
        } else if (addr instanceof ServicePath) {
            prefix = "sp" + PREFIX_SEPARATOR;
            address = getStringFromServicePath(dst, (ServicePath) addr);
        }

        if (dst == Destination.USER) {
            return address;
        } else {
            return prefix + address;
        }

    }

    private static String getPrefixString(Destination dst, String prefix) {
        if (dst == Destination.URL) {
            return prefix.replace("/", "%2f");
        } else {
            return prefix;
        }
    }

    protected static String getStringFromNoAddress(Destination dst, NoAddress addr) {
        // AFI = 0
        if (dst == Destination.USER) {
            return "No Address Present";
        } else {
            return "" + noAddrSeq++;
        }
    }

    protected static String getStringFromIpv4(Destination dst, Ipv4 addr) {
        // AFI = 1; IPv4
        return addr.getIpv4().getValue();
    }

    protected static String getStringFromIpv4Prefix(Destination dst, Ipv4Prefix addr) {
        // AFI = 1; IPv4
        String prefix = addr.getIpv4Prefix().getValue();
        return getPrefixString(dst, prefix);
    }

    protected static String getStringFromIpv6(Destination dst, Ipv6 addr) {
        // AFI = 2; IPv6
        return addr.getIpv6().getValue();
    }

    protected static String getStringFromIpv6Prefix(Destination dst, Ipv6Prefix addr) {
        // AFI = 2; IPv6
        return addr.getIpv6Prefix().getValue();
    }

    protected static String getStringFromDistinguishedName(Destination dst, DistinguishedName addr) {
        // AFI = 17; Distinguished Name
        return addr.getDistinguishedName().getValue();
    }

    protected static String getStringFromAsNumber(Destination dst, AsNumber addr) {
        // AFI = 18; Autonomous System Number
        return "AS" + addr.getAsNumber().getValue();
    }
    protected static String getStringFromAfiList(Destination dst, AfiList addr) {
        // AFI 16387, LCAF Type 1; Address List
        // Example rendering:
        //    {192.0.2.1,192.0.2.2,2001:db8::1}
        List<SimpleAddress> addresses = addr.getAfiList().getAddressList();
        StringBuilder sb = new StringBuilder("{");
        boolean needComma = false;
        for (SimpleAddress a : addresses) {
            if (needComma) {
                sb.append(",");
            }
            sb.append(a.getValue());
            needComma = true;
        }
        sb.append("}");
        return sb.toString();
    }

    protected static String getStringFromInstanceId(Destination dst, InstanceId addr) {
        // AFI = 16387, LCAF Type 2; Instance ID
        // Example rendering:
        //    [223] 192.0.2.0/24
        SimpleAddress pa = addr.getInstanceId().getAddress();
        if (dst == Destination.USER) {
            return "[" + addr.getInstanceId().getIid().getValue() + "] "
                    + LispSimpleAddressStringifier.getString(dst, pa);
        } else {
            return LispSimpleAddressStringifier.getString(dst, pa);
        }
    }

    protected static String getStringFromApplicationData(Destination dst, ApplicationData addr) {
        // AFI = 16387, LCAF Type 4; Application Data
        // Example rendering:
        //    192.0.2.1!128!17!80-81!6667-7000
        return LispSimpleAddressStringifier.getString(dst, addr.getApplicationData().getAddress())
                + "!" + addr.getApplicationData().getIpTos()
                + "!" + addr.getApplicationData().getProtocol()
                + "!" + addr.getApplicationData().getLocalPortLow()
                + "-" + addr.getApplicationData().getLocalPortHigh()
                + "!" + addr.getApplicationData().getRemotePortLow()
                + "-" + addr.getApplicationData().getRemotePortHigh();
    }

    protected static String getStringFromExplicitLocatorPath(Destination dst, ExplicitLocatorPath addr) {
        // AFI = 16387, LCAF Type 10, Explicit Locator Path
        // Example rendering:
        //    {192.0.2.1->192.0.2.2|lps->192.0.2.3}
        List<Hop> hops = addr.getExplicitLocatorPath().getHop();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean needArrow = false;
        for (Hop hop : hops) {
            if (needArrow) {
                sb.append("->");
            }
            sb.append(LispSimpleAddressStringifier.getString(dst, hop.getAddress()));
            LrsBits lrs = hop.getLrsBits();
            if (lrs.isLookup() || lrs.isRlocProbe() || lrs.isStrict()) {
                sb.append("|");
            }
            if (lrs.isLookup()) {
                sb.append("l");
            }
            if (lrs.isRlocProbe()) {
                sb.append("p");
            }
            if (lrs.isStrict()) {
                sb.append("s");
            }
            needArrow = true;
        }
        sb.append("}");
        return sb.toString();
    }

    protected static String getStringFromSourceDestKey(Destination dst, SourceDestKey addr) {
        // AFI = 16387, LCAF Type 12, Source/Destination Key
        // Example rendering:
        //    192.0.2.1/32|192.0.2.2/32
        return getPrefixString(dst, (new String(addr.getSourceDestKey().getSource().getValue()))
                + "|" + getPrefixString(dst, new String(addr.getSourceDestKey().getDest().getValue())));
    }

    protected static String getStringFromKeyValueAddress(Destination dst, KeyValueAddress addr) {
        // AFI = 16387, LCAF Type 15, Key/Value Address Pair
        // Example rendering:
        //    192.0.2.1=>192.0.2.2
        return getPrefixString(dst, new String(addr.getKeyValueAddress().getKey().getValue())
                + "=>" + getPrefixString(dst, new String(addr.getKeyValueAddress().getValue().getValue())));
    }

    protected static String getStringFromMac(Destination dst, Mac addr) {
        // AFI = 16389; MAC
        return addr.getMac().getValue();
    }

    protected static String getStringFromServicePath(Destination dst, ServicePath addr) {
        // AFI = 16387; LCAF http://tools.ietf.org/html/draft-ermagan-lisp-nsh-00
        // Example rendering:
        //     42(3)
        return getPrefixString(dst, new String(addr.getServicePath().getServicePathId().getValue() + "("
                + addr.getServicePath().getServiceIndex() + ")"));
    }

}
