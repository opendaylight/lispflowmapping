/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaflistaddress.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.AS;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafApplicationData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafTrafficEngineering;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Mac;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.No;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.PrimitiveAddress;

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

    public static String getString(LispAddressContainer container) {
        return getAddrString(Destination.USER, container);
    }

    public static String getURIString(LispAddressContainer container) {
        return getString(Destination.URI, container);
    }

    public static String getURLString(LispAddressContainer container) {
        return getString(Destination.URL, container);
    }

    private static String getAddrString(Destination dst, LispAddressContainer container) {
        Preconditions.checkNotNull(container, "address should not be null");
        Address addr = container.getAddress();
        String prefix = null;
        String address = null;

        if (addr instanceof Ipv4) {
            prefix = "ipv4" + PREFIX_SEPARATOR;
            address = getStringFromIpv4(dst, (Ipv4) addr);
        } else if (addr instanceof Ipv6) {
            prefix = "ipv6" + PREFIX_SEPARATOR;
            address = getStringFromIpv6(dst, (Ipv6) addr);
        } else if (addr instanceof Mac) {
            prefix = "mac" + PREFIX_SEPARATOR;
            address = getStringFromMac(dst, (Mac) addr);
        } else if (addr instanceof LcafSegment) {
            PrimitiveAddress pa = ((LcafSegment) addr).getLcafSegmentAddr().getAddress().getPrimitiveAddress();
            // Instance ID is a separate parent hierarchy, so we use the simple address prefix
            prefix = LispPrimitiveAddressStringifier.getURLPrefix(pa) + PREFIX_SEPARATOR;
            address = getStringFromLcafSegment(dst, (LcafSegment) addr);
        } else if (addr instanceof No) {
            prefix = "no" + PREFIX_SEPARATOR;
            address = getStringFromNo(dst, (No) addr);
        } else if (addr instanceof DistinguishedName) {
            prefix = "dn" + PREFIX_SEPARATOR;
            address = getStringFromDistinguishedName(dst, (DistinguishedName) addr);
        } else if (addr instanceof AS) {
            prefix = "as" + PREFIX_SEPARATOR;
            address = getStringFromAS(dst, (AS) addr);
        } else if (addr instanceof LcafList) {
            prefix = "list" + PREFIX_SEPARATOR;
            address = getStringFromLcafList(dst, (LcafList) addr);
        } else if (addr instanceof LcafApplicationData) {
            prefix = "appdata" + PREFIX_SEPARATOR;
            address = getStringFromLcafApplicationData(dst, (LcafApplicationData) addr);
        } else if (addr instanceof LcafTrafficEngineering) {
            prefix = "elp" + PREFIX_SEPARATOR;
            address = getStringFromLcafTrafficEngineering(dst, (LcafTrafficEngineering) addr);
        } else if (addr instanceof LcafSourceDest) {
            prefix = "srcdst" + PREFIX_SEPARATOR;
            address = getStringFromLcafSourceDest(dst, (LcafSourceDest) addr);
        } else if (addr instanceof LcafKeyValue) {
            prefix = "kv" + PREFIX_SEPARATOR;
            address = getStringFromLcafKeyValue(dst, (LcafKeyValue) addr);
        }

        if (dst == Destination.USER) {
            return address;
        } else {
            return prefix + address;
        }

    }

    private static String getString(Destination dst, LispAddressContainer container) {
        if (MaskUtil.isMaskable(container)) {
            return (getAddrString(dst, container) + getMaskSeparator(dst) + MaskUtil.getMaskForAddress(container));
        } else {
            return getAddrString(dst, container);
        }
    }

    private static String getMaskSeparator(Destination dst) {
        if (dst == Destination.URL) {
            return "%2f";
        } else {
            return "/";
        }
    }

    protected static String getStringFromNo(Destination dst, No addr) {
        // AFI = 0
        if (dst == Destination.USER) {
            return "No Address Present";
        } else {
            return "" + noAddrSeq++;
        }
    }

    protected static String getStringFromIpv4(Destination dst, Ipv4 addr) {
        // AFI = 1; IPv4
        return addr.getIpv4Address().getIpv4Address().getValue();
    }

    protected static String getStringFromIpv6(Destination dst, Ipv6 addr) {
        // AFI = 2; IPv6
        return addr.getIpv6Address().getIpv6Address().getValue();
    }

    protected static String getStringFromDistinguishedName(Destination dst, DistinguishedName addr) {
        // AFI = 17; Distinguished Name
        return addr.getDistinguishedName().getDistinguishedName();
    }

    protected static String getStringFromAS(Destination dst, AS addr) {
        // AFI = 18; Autonomous System Number
        return "AS" + addr.getAS().getAS();
    }
    protected static String getStringFromLcafList(Destination dst, LcafList addr) {
        // AFI 16387, LCAF Type 1; Address List
        // Example rendering:
        //    {192.0.2.1,192.0.2.2,2001:db8::1}
        List<Addresses> addresses = addr.getLcafListAddr().getAddresses();
        StringBuilder sb = new StringBuilder("{");
        boolean needComma = false;
        for (Addresses a : addresses) {
            if (needComma) {
                sb.append(",");
            }
            sb.append(LispPrimitiveAddressStringifier.getString(dst, a.getPrimitiveAddress()));
            needComma = true;
        }
        sb.append("}");
        return sb.toString();
    }

    protected static String getStringFromLcafSegment(Destination dst, LcafSegment addr) {
        // AFI = 16387, LCAF Type 2; Instance ID
        // Example rendering:
        //    [223] 192.0.2.0/24
        PrimitiveAddress pa = addr.getLcafSegmentAddr().getAddress().getPrimitiveAddress();
        if (dst == Destination.USER) {
            return "[" + addr.getLcafSegmentAddr().getInstanceId() + "] "
                    + LispPrimitiveAddressStringifier.getString(dst, pa);
        } else {
            return LispPrimitiveAddressStringifier.getString(dst, pa);
        }
    }

    protected static String getStringFromLcafApplicationData(Destination dst, LcafApplicationData addr) {
        // AFI = 16387, LCAF Type 4; Application Data
        // Example rendering:
        //    192.0.2.1!128!17!80-81!6667-7000
        LcafApplicationDataAddr a = addr.getLcafApplicationDataAddr();
        return LispPrimitiveAddressStringifier.getString(dst, a.getAddress().getPrimitiveAddress())
                + "!" + a.getIpTos() + "!" + a.getProtocol()
                + "!" + a.getLocalPortLow() + "-" + a.getLocalPortHigh()
                + "!" + a.getRemotePortLow() + "-" + a.getRemotePortHigh();

    }
    protected static String getStringFromLcafTrafficEngineering(Destination dst, LcafTrafficEngineering addr) {
        // AFI = 16387, LCAF Type 10, Explicit Locator Path
        // Example rendering:
        //    {192.0.2.1->192.0.2.2|lps->192.0.2.3}
        List<Hops> hops = addr.getLcafTrafficEngineeringAddr().getHops();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean needArrow = false;
        for (Hops hop : hops) {
            if (needArrow) {
                sb.append("->");
            }
            sb.append(LispPrimitiveAddressStringifier.getString(dst, hop.getHop().getPrimitiveAddress()));
            if (hop.isLookup() || hop.isRLOCProbe() || hop.isStrict()) {
                sb.append("|");
            }
            if (hop.isLookup()) {
                sb.append("l");
            }
            if (hop.isRLOCProbe()) {
                sb.append("p");
            }
            if (hop.isStrict()) {
                sb.append("s");
            }
            needArrow = true;
        }
        sb.append("}");
        return sb.toString();
    }

    protected static String getStringFromLcafSourceDest(Destination dst, LcafSourceDest addr) {
        // AFI = 16387, LCAF Type 12, Source/Destination Key
        // Example rendering:
        //    192.0.2.1/32|192.0.2.2/32
        LcafSourceDestAddr a = ((LcafSourceDest) addr).getLcafSourceDestAddr();
        return LispPrimitiveAddressStringifier.getString(dst, a.getSrcAddress().getPrimitiveAddress())
                + getMaskSeparator(dst) + a.getSrcMaskLength() + "|"
                + LispPrimitiveAddressStringifier.getString(dst, a.getDstAddress().getPrimitiveAddress())
                + getMaskSeparator(dst) + a.getDstMaskLength();
    }

    protected static String getStringFromLcafKeyValue(Destination dst, LcafKeyValue addr) {
        // AFI = 16387, LCAF Type 15, Key/Value Address Pair
        // Example rendering:
        //    192.0.2.1=>192.0.2.2
        LcafKeyValueAddressAddr a = addr.getLcafKeyValueAddressAddr();
        return LispPrimitiveAddressStringifier.getString(dst, a.getKey().getPrimitiveAddress()) + "=>"
                + LispPrimitiveAddressStringifier.getString(dst, a.getValue().getPrimitiveAddress());
    }

    protected static String getStringFromMac(Destination dst, Mac addr) {
        // AFI = 16389; MAC
        return addr.getMacAddress().getMacAddress().getValue();
    }

}
