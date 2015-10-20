/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispDistinguishedNameAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.transportaddress.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;

public class LispNotificationHelper {
    public static TransportAddress getTransportAddressFromContainer(LispAddressContainer container) {
        TransportAddressBuilder tab = new TransportAddressBuilder();
        LispAFIAddress address = LispAFIConvertor.toAFI(container);
        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address) {
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address) address).getIpv4Address().getValue()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address) {
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address) address).getIpv6Address().getValue()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof LcafKeyValueAddressAddr) {
            PrimitiveAddress primitiveAddress = ((LcafKeyValueAddressAddr) address).getValue().getPrimitiveAddress();
            if (primitiveAddress instanceof LispDistinguishedNameAddress) {
                String value = ((LispDistinguishedNameAddress) primitiveAddress).getDistinguishedName();
                String ip = value.split(":")[0];
                int port = Integer.valueOf(value.split(":")[1]);
                tab.setIpAddress(IpAddressBuilder.getDefaultInstance(ip));
                tab.setPort(new PortNumber(port));
            }
        } else if (address instanceof DistinguishedName) {
            DistinguishedName dname = (DistinguishedName) address;
            String value = dname.getDistinguishedName();
            String ip = value.split(":")[0];
            int port = Integer.valueOf(value.split(":")[1]);

            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(ip));
            tab.setPort(new PortNumber(port));
        } else if (address instanceof LcafApplicationDataAddress) {
            LcafApplicationDataAddress appData = (LcafApplicationDataAddress) address;
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.primitiveaddress.Ipv4) appData.getAddress().getPrimitiveAddress()).getIpv4Address()
                    .getIpv4Address().getValue()));
            tab.setPort(new PortNumber(appData.getLocalPortLow()));
        }
        return tab.build();
    }

    public static InetAddress getAddressByName(String IPAddress) {
        try {
            InetAddress address = InetAddress.getByName(IPAddress);
            return address;
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static List<Mapping> getMapping(AddMapping mapRegisterNotification) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        for (int i=0; i<mapRegisterNotification.getMapRegister().getEidToLocatorRecord().size(); i++) {
            MappingRecord record = mapRegisterNotification.getMapRegister().getEidToLocatorRecord().get(i).getMappingRecord();
            MappingBuilder mb = new MappingBuilder();
            mb.setEidUri(new EidUri(LispAddressStringifier.getURIString(
                    record.getEid())));
            mb.setOrigin(MappingOrigin.Southbound);
            mb.setSiteId(getSiteId(mapRegisterNotification.getMapRegister()));
            mb.setMappingRecord(record);
            mappings.add(mb.build());
        }
        return mappings;
    }

    public static List<SiteId> getSiteId(MapRegister mapRegister) {
        if (mapRegister.isXtrSiteIdPresent()) {
            List<SiteId> siteIds = new ArrayList<SiteId>();
            SiteId siteId = new SiteId(mapRegister.getSiteId());
            siteIds.add(siteId);
            return siteIds;
        } else {
            return null;
        }
    }
}
