/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.northbound;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaflistaddress.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.DstAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.SrcAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaftrafficengineeringaddress.HopsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.as.ASBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv6.Ipv6AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcaflist.LcafListAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcaftrafficengineering.LcafTrafficEngineeringAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.mac.MacAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.no.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.reencaphop.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReplyAction;
import org.opendaylight.lispflowmapping.type.lisp.address.LispASAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispApplicationDataLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispDistinguishedNameAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispMACAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSourceDestLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispTrafficEngineeringLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.ReencapHop;

public class YangTransformerNB {

    public static LispAFIAddress toAFI(LispAddressContainer container) {
        return LispAFIConvertor.toAFI(container);
    }

    public static PrimitiveAddress toPrimitive(LispAFIAddress address) {
        return LispAFIConvertor.toPrimitive(address);
    }

    public static LispAFIAddress toAFIfromPrimitive(PrimitiveAddress primitive) {
        return LispAFIConvertor.toAFIfromPrimitive(primitive);
    }

    public static LispAddressContainer transformLispAddress(LispAddress lispAddress) {

        if (lispAddress == null) {
            return null;
        }

        LispAFIAddress lispAFIAddress = null;
        short ianaCode = lispAddress.getAfi().getIanaCode();

        switch (lispAddress.getAfi()) {

        case IP:
            LispIpv4Address lispIpv4Address = (LispIpv4Address) lispAddress;
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address ipv4Address =
                    new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address(
                            lispIpv4Address.getAddress().getHostAddress());
            lispAFIAddress = new Ipv4AddressBuilder().setIpv4Address(ipv4Address).setAfi(ianaCode).build();
            break;
        case NO_ADDRESS:
            lispAFIAddress = new NoAddressBuilder().setAfi(ianaCode).build();
        case AS:
            LispASAddress lispASAddress = (LispASAddress) lispAddress;
            lispAFIAddress = new ASBuilder().setAS(lispASAddress.getAS()).setAfi(ianaCode).build();
            break;
        case DISTINGUISHED_NAME:
            LispDistinguishedNameAddress lispDNAddress = (LispDistinguishedNameAddress) lispAddress;
            lispAFIAddress = new DistinguishedNameBuilder().setDistinguishedName(lispDNAddress.getDistinguishedName()).setAfi(ianaCode).build();
            break;
        case IP6:
            LispIpv6Address lispIpv6Address = (LispIpv6Address) lispAddress;
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address ipv6Address =
                    new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address(
                            lispIpv6Address.getAddress().getHostAddress());
            lispAFIAddress = new Ipv6AddressBuilder().setIpv6Address(ipv6Address).setAfi(ianaCode).build();
            break;

        case MAC:
            lispAFIAddress = transformLispMACAddress(lispAddress);
            break;

        case LCAF:

            LispLCAFAddress lispLcafAddress = (LispLCAFAddress) lispAddress;
            LispCanonicalAddressFormatEnum lcafEnum = lispLcafAddress.getType();

            switch (lcafEnum) {
            case APPLICATION_DATA:
                lispAFIAddress = transformLispApplicationDataLCAFAddress(lispLcafAddress);
                break;
            case LIST:
                lispAFIAddress = transformLispListLCAFAddress(lispLcafAddress);
                break;
            case SEGMENT:
                lispAFIAddress = transformLispSegmentLCAFAddress(lispLcafAddress);
                break;
            case SOURCE_DEST:
                lispAFIAddress = transformLispSourceDestLCAFAddress(lispLcafAddress);
                break;
            case TRAFFIC_ENGINEERING:
                lispAFIAddress = transformLispTrafficEngineeringLCAFAddress(lispLcafAddress);
                break;
            case UNKNOWN:
            default:
                throw new IllegalArgumentException("LCAF type " + lcafEnum + " not supported by this transoformer");
            }
            break;

        case UNKNOWN:
        default:
            throw new IllegalArgumentException("AFI " + lispAddress.getAfi() + " not supported by this transformer");
        }

        return LispAFIConvertor.toContainer(lispAFIAddress);

    }

    public static LispAFIAddress transformLispMACAddress(LispAddress lispAddress) {
        LispMACAddress lispMacAddress = (LispMACAddress) lispAddress;
        StringBuilder sb = new StringBuilder(17);
        for (byte b : lispMacAddress.getMAC()) {
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }

        return new MacAddressBuilder().setAfi(lispMacAddress.getAfi().getIanaCode()).setMacAddress(
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress(sb.toString())).build();
    }

    public static LispAFIAddress transformLispApplicationDataLCAFAddress(LispLCAFAddress lispLcafAddress) {
        LispApplicationDataLCAFAddress lispApplicationDataLCAFAddress = (LispApplicationDataLCAFAddress) lispLcafAddress;
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafapplicationdataaddress.Address address;
        address = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafapplicationdataaddress.AddressBuilder().setPrimitiveAddress(
                toPrimitive(toAFI(transformLispAddress(lispApplicationDataLCAFAddress.getAddress())))).build();
        return new LcafApplicationDataAddrBuilder().setAfi(lispLcafAddress.getAfi().getIanaCode())
                .setLcafType((short) lispLcafAddress.getType().getLispCode()).setIpTos(lispApplicationDataLCAFAddress.getIPTos())
                .setLocalPortLow(new PortNumber((int) lispApplicationDataLCAFAddress.getLocalPortLow()))
                .setLocalPortHigh(new PortNumber((int) lispApplicationDataLCAFAddress.getLocalPortHigh()))
                .setRemotePortLow(new PortNumber((int) lispApplicationDataLCAFAddress.getRemotePortLow()))
                .setRemotePortHigh(new PortNumber((int) lispApplicationDataLCAFAddress.getRemotePortHigh()))
                .setAddress(address).build();
    }

    public static LispAFIAddress transformLispListLCAFAddress(LispLCAFAddress lispLcafAddress) {

        LispListLCAFAddress lispListLCAFAddress = (LispListLCAFAddress) lispLcafAddress;

        List<Addresses> listAddresses = new ArrayList<Addresses>();

        for (int i = 0; i < lispListLCAFAddress.getAddresses().size(); i++) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaflistaddress.Addresses addresses;
            addresses = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaflistaddress.AddressesBuilder().setPrimitiveAddress(
                    toPrimitive(toAFI(transformLispAddress(lispListLCAFAddress.getAddresses().get(i))))).build();
            listAddresses.add(addresses);
        }

        return new LcafListAddrBuilder().setAfi(lispLcafAddress.getAfi().getIanaCode()).setLcafType((short) lispLcafAddress.getType().getLispCode())
                .setAddresses(listAddresses).build();
    }

    public static LispAFIAddress transformLispSegmentLCAFAddress(LispLCAFAddress lispLcafAddress) {
        LispSegmentLCAFAddress lispSegmentLCAFAddress = (LispSegmentLCAFAddress) lispLcafAddress;
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsegmentaddress.Address address;
        address = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsegmentaddress.AddressBuilder().setPrimitiveAddress(
                toPrimitive(toAFI(transformLispAddress(lispSegmentLCAFAddress.getAddress())))).build();

        return new LcafSegmentAddrBuilder().setAfi(lispLcafAddress.getAfi().getIanaCode()).setLcafType((short) lispLcafAddress.getType().getLispCode())
                .setInstanceId((long) lispSegmentLCAFAddress.getInstanceId()).setAddress(address)
		.setIidMaskLength((short)lispSegmentLCAFAddress.getIdMaskLen()).build();
    }

    public static LispAFIAddress transformLispSourceDestLCAFAddress(LispLCAFAddress lispLcafAddress) {
        LispSourceDestLCAFAddress lispSourceDestLCAFAddress = (LispSourceDestLCAFAddress) lispLcafAddress;

        SrcAddress srcAddress = new SrcAddressBuilder().setPrimitiveAddress(
                toPrimitive(toAFI(transformLispAddress(lispSourceDestLCAFAddress.getSrcAddress())))).build();

        DstAddress dstAddress = new DstAddressBuilder().setPrimitiveAddress(
                toPrimitive(toAFI(transformLispAddress(lispSourceDestLCAFAddress.getDstAddress())))).build();

        return new LcafSourceDestAddrBuilder().setAfi(lispLcafAddress.getAfi().getIanaCode())
                .setLcafType((short) lispLcafAddress.getType().getLispCode()).setSrcMaskLength((short) lispSourceDestLCAFAddress.getSrcMaskLength())
                .setDstMaskLength((short) lispSourceDestLCAFAddress.getDstMaskLength()).setSrcAddress(srcAddress).setDstAddress(dstAddress).build();
    }

    public static LispAFIAddress transformLispTrafficEngineeringLCAFAddress(LispLCAFAddress lispLcafAddress) {
        LispTrafficEngineeringLCAFAddress lispTrafficEngineeringLCAFAddress = (LispTrafficEngineeringLCAFAddress) lispLcafAddress;

        List<Hops> listHops = new ArrayList<Hops>();

        for (int i = 0; i < lispTrafficEngineeringLCAFAddress.getHops().size(); i++) {
            ReencapHop reencapHop = lispTrafficEngineeringLCAFAddress.getHops().get(i);

            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.reencaphop.Hop hop;
            hop = new HopBuilder().setPrimitiveAddress(toPrimitive(toAFI(transformLispAddress(reencapHop.getHop())))).build();

            Hops hops = new HopsBuilder().setLookup(reencapHop.isLookup()).setRLOCProbe(reencapHop.isRLOCProbe()).setStrict(reencapHop.isStrict())
                    .setHop(hop).build();

            listHops.add(hops);

        }

        return new LcafTrafficEngineeringAddrBuilder().setAfi(lispLcafAddress.getAfi().getIanaCode())
                .setLcafType((short) lispLcafAddress.getType().getLispCode()).setHops(listHops).build();
    }

    public static LispAddress transformToLispAddress(LispAddressContainer lispAddress) {

        LispAddress legacyAddress = null;
        LispAFIAddress address = LispAFIConvertor.toAFI(lispAddress);
        AddressFamilyNumberEnum afi = AddressFamilyNumberEnum.valueOf(address.getAfi());

        switch (afi) {

        case IP:
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv4Address lispIpv4Address;
            lispIpv4Address = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv4Address) address;
            legacyAddress = new LispIpv4Address(lispIpv4Address.getIpv4Address().getValue());
            break;
        case IP6:
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv6Address lispIpv6Address;
            lispIpv6Address = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv6Address) address;
            legacyAddress = new LispIpv6Address(lispIpv6Address.getIpv6Address().getValue());
            break;
        case AS:
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispASAddress lispASAddress;
            lispASAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispASAddress) address;
            legacyAddress = new LispASAddress(lispASAddress.getAS());
            break;
        case DISTINGUISHED_NAME:
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispDistinguishedNameAddress lispDistinguishedNameAddress;
            lispDistinguishedNameAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispDistinguishedNameAddress) address;
            legacyAddress = new LispDistinguishedNameAddress(lispDistinguishedNameAddress.getDistinguishedName());
            break;
        case MAC:
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispMacAddress lispMacAddress;
            lispMacAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispMacAddress) address;
            String macString = lispMacAddress.getMacAddress().getValue();
            macString = macString.replaceAll(":", "");
            legacyAddress = new LispMACAddress(DatatypeConverter.parseHexBinary(macString));
            break;
        case NO_ADDRESS:
            legacyAddress = new LispNoAddress();
            break;
        case LCAF:
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress lispLcafAddress;
            lispLcafAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress) address;

            LispCanonicalAddressFormatEnum lcafEnum = LispCanonicalAddressFormatEnum.valueOf(lispLcafAddress.getLcafType());

            switch (lcafEnum) {
            case APPLICATION_DATA:
                legacyAddress = transformToLispApplicationDataLCAFAddress(lispLcafAddress);
                break;
            case LIST:
                legacyAddress = transformToLispListLCAFAddress(lispLcafAddress);
                break;
            case SEGMENT:
                legacyAddress = transformToLispSegmentLCAFAddress(lispLcafAddress);
                break;
            case SOURCE_DEST:
                legacyAddress = transformToLispSourceDestLCAFAddress(lispLcafAddress);
                break;
            case TRAFFIC_ENGINEERING:
                legacyAddress = transformToLispTrafficEngineeringLCAFAddress(lispLcafAddress);
                break;
            case UNKNOWN:
            default:
                throw new IllegalArgumentException("LCAF type " + lcafEnum + " not supported by this transoformer");
            }
            break;
        case UNKNOWN:
        default:
            throw new IllegalArgumentException("AFI " + afi + " not supported by this transformer");
        }

        return legacyAddress;
    }

    public static LispAddress transformToLispApplicationDataLCAFAddress(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress lispLcafAddress) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafApplicationDataAddress lcafApplicationDataAddress;
        lcafApplicationDataAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafApplicationDataAddress) lispLcafAddress;

        return new LispApplicationDataLCAFAddress((byte) 0, lcafApplicationDataAddress.getProtocol().byteValue(), lcafApplicationDataAddress
                .getIpTos().intValue(), lcafApplicationDataAddress.getLocalPortLow().getValue().shortValue(), lcafApplicationDataAddress.getLocalPortHigh()
                .getValue().shortValue(), lcafApplicationDataAddress.getRemotePortLow().getValue().shortValue(), lcafApplicationDataAddress.getRemotePortHigh()
                .getValue().shortValue(), transformToLispAddress(LispAFIConvertor.toContainer(toAFIfromPrimitive(lcafApplicationDataAddress.getAddress()
                .getPrimitiveAddress()))));
    }

    public static LispAddress transformToLispListLCAFAddress(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress lispLcafAddress) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafListAddress lcafListAddress;
        lcafListAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafListAddress) lispLcafAddress;

        List<LispAddress> listLegacyAddress = new ArrayList<LispAddress>();

        for (int i = 0; i < lcafListAddress.getAddresses().size(); i++) {
            listLegacyAddress
                    .add(transformToLispAddress(LispAFIConvertor.toContainer(toAFIfromPrimitive(lcafListAddress.getAddresses().get(i).getPrimitiveAddress()))));
        }
        return new LispListLCAFAddress((byte) 0, listLegacyAddress);
    }

    public static LispAddress transformToLispSegmentLCAFAddress(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress lispLcafAddress) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSegmentAddress lcafSegmentAddress;
        lcafSegmentAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSegmentAddress) lispLcafAddress;

        byte iidMaskLen = 0;

        return new LispSegmentLCAFAddress(iidMaskLen, lcafSegmentAddress.getInstanceId().intValue(),
                transformToLispAddress(LispAFIConvertor.toContainer(toAFIfromPrimitive(lcafSegmentAddress.getAddress().getPrimitiveAddress()))));

    }

    public static LispAddress transformToLispSourceDestLCAFAddress(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress lispLcafAddress) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSourceDestAddress lcafSourceDestAddress;
        lcafSourceDestAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSourceDestAddress) lispLcafAddress;

        return new LispSourceDestLCAFAddress((byte) 0, (short) 0, lcafSourceDestAddress.getSrcMaskLength().byteValue(), lcafSourceDestAddress
                .getDstMaskLength().byteValue(), transformToLispAddress(LispAFIConvertor.toContainer(toAFIfromPrimitive(lcafSourceDestAddress.getSrcAddress()
                .getPrimitiveAddress()))), transformToLispAddress(LispAFIConvertor.toContainer(toAFIfromPrimitive(lcafSourceDestAddress.getDstAddress()
                .getPrimitiveAddress()))));

    }

    public static LispAddress transformToLispTrafficEngineeringLCAFAddress(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress lispLcafAddress) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafTrafficEngineeringAddress lcafTrafficEngineeringAddress;
        lcafTrafficEngineeringAddress = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafTrafficEngineeringAddress) lispLcafAddress;

        List<ReencapHop> listLegacyHops = new ArrayList<ReencapHop>();

        ReencapHop legacyHop;
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.ReencapHop hop;

        // for (Addresses addresses : lcafListAddress.getAddresses()){
        for (int i = 0; i < lcafTrafficEngineeringAddress.getHops().size(); i++) {
            hop = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.ReencapHop) lcafTrafficEngineeringAddress.getHops().get(i);

            legacyHop = new ReencapHop(transformToLispAddress(LispAFIConvertor.toContainer(toAFIfromPrimitive(hop.getHop().getPrimitiveAddress()))), (short) 0,
                    hop.isLookup(), hop.isRLOCProbe(), hop.isStrict());

            listLegacyHops.add(legacyHop);
        }
        return new LispTrafficEngineeringLCAFAddress((byte) 0, listLegacyHops);
    }

    public static MapRegister transformMapRegister(org.opendaylight.lispflowmapping.type.lisp.MapRegister legacyMapRegister) {

        List<EidToLocatorRecord> listEidToLocatorRecord = new ArrayList<EidToLocatorRecord>();

        // for (EidToLocatorRecord legacyRecord:
        // legacyMapRegister.getEidToLocatorRecords()){
        for (int i = 0; i < legacyMapRegister.getEidToLocatorRecords().size(); i++) {

            listEidToLocatorRecord.add(transformEidToLocatorRecord(legacyMapRegister.getEidToLocatorRecords().get(i)));

        }

        MapRegisterBuilder builder = new MapRegisterBuilder();

        MapRegister yangMapRegister = builder.setKeyId(legacyMapRegister.getKeyId()).setProxyMapReply(legacyMapRegister.isProxyMapReply())
                .setEidToLocatorRecord(listEidToLocatorRecord).setWantMapNotify(true).build();

        return yangMapRegister;

    }

    public static EidToLocatorRecord transformEidToLocatorRecord(
            org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord legacyEidToLocatorRecord) {

        List<LocatorRecord> listLocatorRecord = new ArrayList<LocatorRecord>();

        for (int j = 0; j < legacyEidToLocatorRecord.getLocators().size(); j++) {

            org.opendaylight.lispflowmapping.type.lisp.LocatorRecord legacyLocatorRecord = legacyEidToLocatorRecord.getLocators().get(j);

            LispAddressContainer rloc = transformLispAddress(legacyLocatorRecord.getLocator());

            LocatorRecordBuilder locatorRecordBuilder = new LocatorRecordBuilder();

            LocatorRecord locatorRecord = locatorRecordBuilder.setLispAddressContainer((LispAddressContainer) rloc)
                    .setLocalLocator(legacyLocatorRecord.isLocalLocator()).setPriority((short) legacyLocatorRecord.getPriority())
                    .setWeight((short) legacyLocatorRecord.getWeight()).setMulticastPriority((short) legacyLocatorRecord.getMulticastPriority())
                    .setMulticastWeight((short) legacyLocatorRecord.getMulticastWeight()).setRlocProbed(legacyLocatorRecord.isRlocProbed())
                    .setRouted(legacyLocatorRecord.isRouted()).build();

            listLocatorRecord.add(locatorRecord);
        }

        LispAddressContainer eid = transformLispAddress(legacyEidToLocatorRecord.getPrefix());
        EidToLocatorRecordBuilder eidToLocatorRecordBuilder = new EidToLocatorRecordBuilder();
        EidToLocatorRecord eidToLocatorRecord = eidToLocatorRecordBuilder.setLispAddressContainer((LispAddressContainer) eid)
                .setAction(Action.forValue(legacyEidToLocatorRecord.getAction().getCode()))
                .setAuthoritative(legacyEidToLocatorRecord.isAuthoritative()).setMaskLength((short) legacyEidToLocatorRecord.getMaskLength())
                .setRecordTtl(legacyEidToLocatorRecord.getRecordTtl()).setLocatorRecord(listLocatorRecord).build();
        return eidToLocatorRecord;
    }

    public static org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord reTransformEidToLocatorRecord(EidToLocatorRecord eidToLocatorRecord) {

        org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord legacyRecord = new org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord();

        for (int j = 0; j < eidToLocatorRecord.getLocatorRecord().size(); j++) {

            LocatorRecord locatorRecord = (LocatorRecord) eidToLocatorRecord.getLocatorRecord().get(j);

            org.opendaylight.lispflowmapping.type.lisp.LocatorRecord legacyLocatorRecord = new org.opendaylight.lispflowmapping.type.lisp.LocatorRecord();

            LispAddressContainer rloc = locatorRecord.getLispAddressContainer();

            legacyLocatorRecord.setLocator(YangTransformerNB.transformToLispAddress(rloc));

            legacyLocatorRecord.setLocalLocator(locatorRecord.isLocalLocator()).setPriority(locatorRecord.getPriority())
                    .setWeight(locatorRecord.getWeight()).setMulticastPriority(locatorRecord.getMulticastPriority())
                    .setMulticastWeight(locatorRecord.getMulticastWeight()).setRlocProbed(locatorRecord.isRlocProbed())
                    .setRouted(locatorRecord.isRouted());

            legacyRecord.addLocator(legacyLocatorRecord);
        }

        LispAddress eid = transformToLispAddress(eidToLocatorRecord.getLispAddressContainer());
        legacyRecord.setPrefix(eid).setAction(MapReplyAction.valueOf(eidToLocatorRecord.getAction().getIntValue()))
                .setAuthoritative(eidToLocatorRecord.isAuthoritative()).setMaskLength((short) eidToLocatorRecord.getMaskLength())
                .setRecordTtl(eidToLocatorRecord.getRecordTtl());
        return legacyRecord;
    }

    public static MapRequest transformMapRequest(org.opendaylight.lispflowmapping.type.lisp.MapRequest legacyMapRequest) {

        MapRequestBuilder builder = new MapRequestBuilder();
        builder.setAuthoritative(legacyMapRequest.isAuthoritative());
        builder.setMapDataPresent(legacyMapRequest.isMapDataPresent());
        builder.setPitr(legacyMapRequest.isPitr());
        builder.setProbe(legacyMapRequest.isProbe());
        builder.setSmr(legacyMapRequest.isSmr());
        builder.setSmrInvoked(legacyMapRequest.isSmrInvoked());

        builder.setEidRecord(new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord>());
        for (EidRecord record : legacyMapRequest.getEids()) {
            LispAddressContainer container = transformLispAddress(record.getPrefix());
            builder.getEidRecord().add(

            new EidRecordBuilder().setMask((short) record.getMaskLength()).setLispAddressContainer(container).build());
        }

        builder.setItrRloc(new ArrayList<ItrRloc>());
        for (LispAddress itr : legacyMapRequest.getItrRlocs()) {
            builder.getItrRloc().add(new ItrRlocBuilder().setLispAddressContainer(transformLispAddress(itr)).build());
        }

        builder.setMapReply(null);
        builder.setNonce(legacyMapRequest.getNonce());
        if (legacyMapRequest.getSourceEid() != null) {
            builder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(transformLispAddress(legacyMapRequest.getSourceEid())).build());
        }
        return builder.build();

    }

}
