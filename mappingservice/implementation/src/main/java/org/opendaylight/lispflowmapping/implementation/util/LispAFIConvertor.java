/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafkeyvalueaddress.KeyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafkeyvalueaddress.ValueBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafKeyValueBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Mac;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

public class LispAFIConvertor {

    public static LispAddressContainer toContainer(LispAFIAddress address) {
        if (address instanceof Address) {
            return new LispAddressContainerBuilder().setAddress((Address) address).build();
        } else {
            return null;
        }
    }

    public static LispAFIAddress toAFI(LispAddressContainer container) {
        return (LispAFIAddress) container.getAddress();
    }

    public static PrimitiveAddress toPrimitive(LispAFIAddress address) {
        if (address instanceof Ipv4) {
            return new Ipv4Builder().setIpv4Address(((Ipv4) address).getIpv4Address()).setAfi(address.getAfi()).build();
        }
        if (address instanceof Ipv6) {
            return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv6Builder()
                    .setIpv6Address(((Ipv6) address).getIpv6Address()).setAfi(address.getAfi()).build();
        }
        if (address instanceof Mac) {
            return new MacBuilder().setAfi(address.getAfi()).setMacAddress(((Mac) address).getMacAddress()).build();
        }
        if (address instanceof DistinguishedName) {
            return new DistinguishedNameBuilder().setAfi(address.getAfi()).setDistinguishedName(((DistinguishedName) address).getDistinguishedName())
                    .build();
        }
        return null;
    }

    public static LispAFIAddress toAFIfromPrimitive(PrimitiveAddress primitive) {

        if (primitive instanceof org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4) {
            return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder()
                    .setAfi(((LispAFIAddress) primitive).getAfi())
                    .setIpv4Address(
                            ((org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4) primitive)
                                    .getIpv4Address()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv6) {
            return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6Builder()
                    .setAfi(((LispAFIAddress) primitive).getAfi())
                    .setIpv6Address(
                            ((org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv6) primitive)
                                    .getIpv6Address()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Mac) {
            return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.MacBuilder()
                    .setAfi(((LispAFIAddress) primitive).getAfi())
                    .setMacAddress(
                            ((org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Mac) primitive)
                                    .getMacAddress()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.DistinguishedName) {
            return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.DistinguishedNameBuilder()
                    .setAfi(((LispAFIAddress) primitive).getAfi())
                    .setDistinguishedName(
                            ((org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.DistinguishedName) primitive)
                                    .getDistinguishedName()).build();
        }
        if (primitive instanceof org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.AS) {
            return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.ASBuilder()
                    .setAfi(((LispAFIAddress) primitive).getAfi())
                    .setAS(((org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.AS) primitive).getAS())
                    .build();
        }
        return null;
    }

    public static LispAddressContainer getIPContainer(String ip) {
        return new LispAddressContainerBuilder().setAddress(asIPAfiAddress(ip)).build();
    }

    public static Ipv4 asIPAfiAddress(String ip) {
        return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder()
                .setIpv4Address(new Ipv4Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode()).build();
    }

    public static LcafKeyValue asKeyValue(String key, PrimitiveAddress value) {
        return new LcafKeyValueBuilder()
                .setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode())
                .setKey(new KeyBuilder().setPrimitiveAddress(
                        new DistinguishedNameBuilder().setAfi(AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode()).setDistinguishedName(key)
                                .build()).build()).setValue(new ValueBuilder().setPrimitiveAddress(value).build()).build();
    }

    public static org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4 asPrimitiveIPAfiAddress(String ip) {
        return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4Builder()
                .setIpv4Address(new Ipv4Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode()).build();
    }

    public static Mac asMacAfiAddress(String mac) {
        return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.MacBuilder()
                .setMacAddress(new MacAddress(mac)).setAfi((short) AddressFamilyNumberEnum.MAC.getIanaCode()).build();
    }

    public static org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Mac asPrimitiveMacAfiAddress(String mac) {
        return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.MacBuilder()
                .setMacAddress(new MacAddress(mac)).setAfi((short) AddressFamilyNumberEnum.MAC.getIanaCode()).build();
    }

    public static Ipv6 asIPv6AfiAddress(String ip) {
        return new Ipv6Builder().setIpv6Address(new Ipv6Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP6.getIanaCode()).build();
    }

}
