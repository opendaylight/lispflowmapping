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
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;

public final class LispNotificationHelper {
    // Utility class, should not be instantiated
    private LispNotificationHelper() {
    }

    public static TransportAddress getTransportAddressFromRloc(Rloc rloc) {
        TransportAddressBuilder tab = new TransportAddressBuilder();
        Address address = rloc.getAddress();
        if (address instanceof Ipv4) {
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((Ipv4) address).getIpv4().getValue()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof Ipv6) {
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((Ipv6) address).getIpv6().getValue()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof KeyValueAddress) {
            SimpleAddress sa = ((KeyValueAddress) address).getKeyValueAddress().getValue();
            if (sa.getDistinguishedNameType() != null) {
                String value = sa.getDistinguishedNameType().getValue();
                String ip = value.split(":")[0];
                int port = Integer.valueOf(value.split(":")[1]);
                tab.setIpAddress(IpAddressBuilder.getDefaultInstance(ip));
                tab.setPort(new PortNumber(port));
            }
        } else if (address instanceof DistinguishedName) {
            DistinguishedName dname = (DistinguishedName) address;
            String value = dname.getDistinguishedName().getValue();
            String ip = value.split(":")[0];
            int port = Integer.valueOf(value.split(":")[1]);

            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(ip));
            tab.setPort(new PortNumber(port));
        } else if (address instanceof ApplicationData) {
            ApplicationData appData = (ApplicationData) address;
            tab.setIpAddress(appData.getApplicationData().getAddress().getIpAddress());
            tab.setPort(new PortNumber(appData.getApplicationData().getLocalPortLow()));
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
        for (int i=0; i<mapRegisterNotification.getMapRegister().getMappingRecordItem().size(); i++) {
            MappingRecord record = mapRegisterNotification.getMapRegister().getMappingRecordItem().get(i).getMappingRecord();
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
