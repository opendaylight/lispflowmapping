/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafkeyvalueaddress.KeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafkeyvalueaddress.ValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.distinguishedname.DistinguishedNameAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.mac.MacAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6PrefixBuilder;

public class LispAFIConvertor {

    public static Eid toEid(Eid eid, IpPrefix prefix) {
        EidBuilder builder = new EidBuilder();
        builder.setAfi(eid.getAfi());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        // XXX Not sure if the below actually works as expected... also, what happens to AFI?
        builder.setAddress((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.Address)prefix);
        return builder.build();
    }

    public static Eid asIpv4Prefix(Eid eid, Inet4Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAfi(eid.getAfi());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(new Ipv4Prefix(getStringPrefix(address, mask))).build());
        return builder.build();
    }

    public static Eid asIpv6Prefix(Eid eid, Inet6Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAfi(eid.getAfi());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv6PrefixBuilder().setIpv6Prefix(new Ipv6Prefix(getStringPrefix(address, mask))).build());
        return builder.build();
    }

    private static String getStringPrefix(InetAddress address, short mask) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getHostAddress());
        sb.append("/");
        sb.append(mask);
        return sb.toString();
    }

    public static LispAddressContainer toContainer(LispAFIAddress address) {
        Address addr = null;

        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv4Builder()
                    .setIpv4Address(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv6Builder()
                    .setIpv6Address(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcaftrafficengineering.LcafTrafficEngineeringAddr) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafTrafficEngineeringBuilder()
                    .setLcafTrafficEngineeringAddr(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcaftrafficengineering.LcafTrafficEngineeringAddr) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.as.AS) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ASBuilder()
                    .setAS((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.as.AS) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.DistinguishedNameBuilder()
                    .setDistinguishedName(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddr) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafApplicationDataBuilder()
                    .setLcafApplicationDataAddr(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddr) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddr) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafKeyValueBuilder()
                    .setLcafKeyValueAddressAddr(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddr) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcaflist.LcafListAddr) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafListBuilder()
                    .setLcafListAddr(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcaflist.LcafListAddr) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddr) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSegmentBuilder()
                    .setLcafSegmentAddr(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddr) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddr) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDestBuilder()
                    .setLcafSourceDestAddr(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddr) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.mac.MacAddress) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.MacBuilder()
                    .setMacAddress(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.mac.MacAddress) address)
                    .build();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.no.NoAddress) {
            addr = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.NoBuilder()
                    .setNoAddress(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.no.NoAddress) address)
                    .build();
        }
        return new LispAddressContainerBuilder().setAddress(addr).build();
    }

    public static LispAddressContainer toContainer(InetAddress address) {
        if (address instanceof Inet4Address) {
            return toContainer(asIPAfiAddress(address.getHostAddress()));
        }

        if (address instanceof Inet6Address) {
            return toContainer(asIPv6AfiAddress(address.getHostAddress()));
        }

        return null;
    }

    public static LispAFIAddress toAFI(LispAddressContainer container) {
        Address address = container.getAddress();
        LispAFIAddress addr = null;

        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv4) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv4) address)
                    .getIpv4Address();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv6) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv6) address)
                    .getIpv6Address();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafTrafficEngineering) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafTrafficEngineering) address)
                    .getLcafTrafficEngineeringAddr();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.AS) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.AS) address)
                    .getAS();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.DistinguishedName) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.DistinguishedName) address)
                    .getDistinguishedName();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafApplicationData) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafApplicationData) address)
                    .getLcafApplicationDataAddr();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafKeyValue) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafKeyValue) address)
                    .getLcafKeyValueAddressAddr();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafList) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafList) address)
                    .getLcafListAddr();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSegment) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSegment) address)
                    .getLcafSegmentAddr();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest) address)
                    .getLcafSourceDestAddr();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Mac) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Mac) address)
                    .getMacAddress();
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.No) {
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.No) address)
                    .getNoAddress();
        }

        return addr;
    }

    public static PrimitiveAddress toPrimitive(LispAFIAddress address) {
        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address) {
            return new Ipv4Builder()
                    .setIpv4Address(
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.ipv4.Ipv4AddressBuilder(
                                    (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispIpv4Address) address)
                                    .build()).build();
        }
        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address) {
            return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv6Builder()
                    .setIpv6Address(
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.ipv6.Ipv6AddressBuilder(
                                    (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispIpv6Address) address)
                                    .build()).build();
        }
        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.mac.MacAddress) {
            return new MacBuilder()
                    .setMacAddress(
                            new MacAddressBuilder()
                                    .setMacAddress(
                                            ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.mac.MacAddress) address)
                                                    .getMacAddress())
                                    .setAfi(address.getAfi()).build()).build();
        }
        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName) {
            return new DistinguishedNameBuilder()
                    .setDistinguishedNameAddress(
                            new DistinguishedNameAddressBuilder()
                                    .setDistinguishedName(
                                            ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName) address)
                                                    .getDistinguishedName())
                                    .setAfi(address.getAfi()).build()).build();
        }
        return null;
    }

    public static LispAFIAddress toAFIfromPrimitive(PrimitiveAddress primitive) {

        if (primitive instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4) {
            return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4) primitive)
                    .getIpv4Address()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv6) {
            return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6AddressBuilder(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv6) primitive)
                    .getIpv6Address()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Mac) {
            return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.mac.MacAddressBuilder()
                    .setAfi(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Mac) primitive)
                            .getMacAddress().getAfi())
                    .setMacAddress(
                            ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Mac) primitive)
                                    .getMacAddress().getMacAddress()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.DistinguishedName) {
            return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedNameBuilder()
                    .setAfi(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.DistinguishedName) primitive)
                            .getDistinguishedNameAddress().getAfi())
                    .setDistinguishedName(
                            ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.DistinguishedName) primitive)
                                    .getDistinguishedNameAddress()
                                    .getDistinguishedName()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.AS) {
            return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.as.ASBuilder()
                    .setAfi(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.AS) primitive)
                            .getASAddress().getAfi())
                    .setAS(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.AS) primitive)
                            .getASAddress().getAS()).build();
        }
        return null;
    }

    public static LispAddressContainer getIPContainer(String ip) {
        return toContainer(asIPAfiAddress(ip));
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName asDistinguishedNameAddress(
            String distName) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedNameBuilder()
            .setAfi(AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode())
            .setDistinguishedName(distName).build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address asIPAfiAddress(
            String ip) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder()
                .setIpv4Address(new Ipv4Address(ip))
                .setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode())
                .build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address asIPv6AfiAddress(
            String ip) {
        return new Ipv6AddressBuilder().setIpv6Address(new Ipv6Address(ip))
                .setAfi((short) AddressFamilyNumberEnum.IP6.getIanaCode())
                .build();
    }

    public static LispAddressContainer asIPv4Address(String ip) {
        return new LispAddressContainerBuilder().setAddress(new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv4Builder()
        .setIpv4Address(new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder()
        .setIpv4Address(new Ipv4Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode()).setMask((short)32).build()).build()).build();
    }

    public static LcafKeyValueAddressAddr asKeyValue(String key,
            PrimitiveAddress value) {
        return new LcafKeyValueAddressAddrBuilder()
                .setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType(
                        (short) LispCanonicalAddressFormatEnum.KEY_VALUE
                                .getLispCode())
                .setKey(new KeyBuilder()
                        .setPrimitiveAddress(
                                new DistinguishedNameBuilder()
                                        .setDistinguishedNameAddress(
                                                new DistinguishedNameAddressBuilder()
                                                        .setAfi(AddressFamilyNumberEnum.DISTINGUISHED_NAME
                                                                .getIanaCode())
                                                        .setDistinguishedName(
                                                                key).build())
                                        .build()).build())
                .setValue(new ValueBuilder().setPrimitiveAddress(value).build())
                .build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4 asPrimitiveIPAfiAddress(
            String ip) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4Builder()
                .setIpv4Address(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.ipv4.Ipv4AddressBuilder()
                                .setIpv4Address(new Ipv4Address(ip))
                                .setAfi((short) AddressFamilyNumberEnum.IP
                                        .getIanaCode()).build()).build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4 asPrimitiveIPv4AfiPrefix(
            String ip, int mask) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4Builder()
                .setIpv4Address(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.ipv4.Ipv4AddressBuilder()
                                .setIpv4Address(new Ipv4Address(ip))
                                .setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode()).setMask((short) mask).build())
                .build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv6 asPrimitiveIPv6AfiPrefix(
            String ip, int mask) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv6Builder()
                .setIpv6Address(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.ipv6.Ipv6AddressBuilder()
                                .setIpv6Address(new Ipv6Address(ip))
                                .setAfi((short) AddressFamilyNumberEnum.IP6.getIanaCode()).setMask((short) mask).build())
                .build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.mac.MacAddress asMacAfiAddress(
            String mac) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.mac.MacAddressBuilder()
                .setMacAddress(new MacAddress(mac))
                .setAfi((short) AddressFamilyNumberEnum.MAC.getIanaCode())
                .build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Mac asPrimitiveMacAfiAddress(
            String mac) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.MacBuilder()
                .setMacAddress(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.mac.MacAddressBuilder()
                                .setMacAddress(new MacAddress(mac))
                                .setAfi((short) AddressFamilyNumberEnum.MAC
                                        .getIanaCode()).build()).build();
    }
}
