/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.northbound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.AS;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.ASBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.NoBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReplyAction;
import org.opendaylight.lispflowmapping.type.lisp.address.LispASAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangTransformerNB {

    protected static final Logger logger = LoggerFactory.getLogger(YangTransformerNB.class);

    public static LispAddressContainer toContainer(LispAFIAddress address) {
        if (address instanceof Address) {
            return new LispAddressContainerBuilder().setAddress((Address) address).build();
        } else {
            return null;
        }
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
            Ipv4Address ipv4Address = new Ipv4Address(lispIpv4Address.getAddress().getHostAddress());
            lispAFIAddress = new Ipv4Builder().setIpv4Address(ipv4Address).setAfi(ianaCode).build();
            break;
        case NO_ADDRESS:
            lispAFIAddress = new NoBuilder().setAfi(ianaCode).build();
        case AS:
            LispASAddress lispASAddress = (LispASAddress) lispAddress;
            lispAFIAddress = new ASBuilder().setAS(lispASAddress.getAS()).setAfi(ianaCode).build();
            break;
        case DISTINGUISHED_NAME:
            break;
        case IP6:
            LispIpv6Address lispIpv6Address = (LispIpv6Address) lispAddress;
            Ipv6Address ipv6Address = new Ipv6Address(lispIpv6Address.getAddress().getHostAddress());
            lispAFIAddress = new Ipv6Builder().setIpv6Address(ipv6Address).setAfi(ianaCode).build();
            break;
        case LCAF:
            break;
        case MAC:
            break;
        case UNKNOWN:
            break;
        default:

            break;
        }

        return toContainer(lispAFIAddress);

    }

    public static LispAddress transformToLispAddress(LispAddressContainer lispAddress) {

        LispAddress legacyAddres = null;
        LispAFIAddress address = (LispAFIAddress) lispAddress.getAddress();
        short ianaCode = address.getAfi();

        // TODO: add all other cases.

        switch (ianaCode) {

        case 1:
            org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address lispIpv4Address = (org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address) address;
            legacyAddres = new LispIpv4Address(lispIpv4Address.getIpv4Address().getValue());
            break;
        case 2:
            org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv6Address lispIpv6Address = (org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv6Address) address;
            legacyAddres = new LispIpv6Address(lispIpv6Address.getIpv6Address().getValue());
            break;
        default:

            break;
        }

        return legacyAddres;

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

            LocatorRecord locatorRecord = eidToLocatorRecord.getLocatorRecord().get(j);

            org.opendaylight.lispflowmapping.type.lisp.LocatorRecord legacyLocatorRecord = new org.opendaylight.lispflowmapping.type.lisp.LocatorRecord();

            LispAddressContainer rloc = locatorRecord.getLispAddressContainer();

            legacyLocatorRecord.setLocator(YangTransformerNB.transformToLispAddress(rloc));

            legacyLocatorRecord.setLocalLocator(legacyLocatorRecord.isLocalLocator()).setPriority((byte) legacyLocatorRecord.getPriority())
                    .setWeight((byte) legacyLocatorRecord.getWeight()).setMulticastPriority((byte) legacyLocatorRecord.getMulticastPriority())
                    .setMulticastWeight((byte) legacyLocatorRecord.getMulticastWeight()).setRlocProbed(legacyLocatorRecord.isRlocProbed())
                    .setRouted(legacyLocatorRecord.isRouted());

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

        builder.setEidRecord(new ArrayList<org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord>());
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
