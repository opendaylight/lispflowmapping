/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import com.google.common.base.Splitter;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LispNotificationHelper {
    protected static final Logger LOG = LoggerFactory.getLogger(LispNotificationHelper.class);
    private static final Splitter COLON_SPLITTER = Splitter.on(':');

    // Utility class, should not be instantiated
    private LispNotificationHelper() {
    }

    public static TransportAddress getTransportAddressFromRloc(Rloc rloc) {
        TransportAddressBuilder tab = new TransportAddressBuilder();
        Address address = rloc.getAddress();
        // once we switch to using Ipv4Binary and Ipv6Binary by default for RLOCs, we will only need to keep the below
        // for backwards compatibility reasons, but the default will be to just return the reference for the binary
        // object, yey!
        if (address instanceof Ipv4) {
            String ipv4 = ((Ipv4) address).getIpv4().getValue();
            tab.setIpAddress(IpAddressBinaryBuilder.getDefaultInstance(InetAddresses.forString(ipv4).getAddress()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof Ipv6) {
            String ipv6 = ((Ipv6) address).getIpv6().getValue();
            tab.setIpAddress(IpAddressBinaryBuilder.getDefaultInstance(InetAddresses.forString(ipv6).getAddress()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof Ipv4Binary) {
            Ipv4AddressBinary ipv6 = ((Ipv4Binary) address).getIpv4Binary();
            tab.setIpAddress(new IpAddressBinary(ipv6));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof Ipv6Binary) {
            Ipv6AddressBinary ipv6 = ((Ipv6Binary) address).getIpv6Binary();
            tab.setIpAddress(new IpAddressBinary(ipv6));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof KeyValueAddress) {
            SimpleAddress sa = ((KeyValueAddress) address).getKeyValueAddress().getValue();
            if (sa.getDistinguishedNameType() != null) {
                final Iterator<String> it = COLON_SPLITTER.split(sa.getDistinguishedNameType().getValue()).iterator();
                String ip = it.next();
                int port = Integer.valueOf(it.next());

                tab.setIpAddress(IpAddressBinaryBuilder.getDefaultInstance(InetAddresses.forString(ip).getAddress()));
                tab.setPort(new PortNumber(port));
            }
        } else if (address instanceof DistinguishedName) {
            DistinguishedName dname = (DistinguishedName) address;
            final Iterator<String> it = COLON_SPLITTER.split(dname.getDistinguishedName().getValue()).iterator();
            String ip = it.next();
            int port = Integer.valueOf(it.next());

            tab.setIpAddress(IpAddressBinaryBuilder.getDefaultInstance(InetAddresses.forString(ip).getAddress()));
            tab.setPort(new PortNumber(port));
        } else if (address instanceof ApplicationData) {
            ApplicationData appData = (ApplicationData) address;
            tab.setIpAddress(getIpAddressBinary(appData.getApplicationData().getAddress().getIpAddress()));
            tab.setPort(new PortNumber(appData.getApplicationData().getLocalPortLow()));
        }
        return tab.build();
    }

    public static InetAddress getAddressByName(String IPAddress) {
        try {
            InetAddress address = InetAddress.getByName(IPAddress);
            return address;
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", IPAddress, e);
            return null;
        }
    }

    public static List<Mapping> getMapping(AddMapping mapRegisterNotification) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        for (int i=0; i<mapRegisterNotification.getMapRegister().getMappingRecordItem().size(); i++) {
            MappingRecord record = mapRegisterNotification.getMapRegister().getMappingRecordItem().get(i)
                    .getMappingRecord();
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

    // We will see if we need to keep this method post full binary-migration, and if yes, will probably move to
    // LispAddressUtil (and add tests!)
    private static IpAddressBinary getIpAddressBinary(IpAddress address) {
        if (address.getIpv4Address() != null) {
            String ipv4 = address.getIpv4Address().getValue();
            return IpAddressBinaryBuilder.getDefaultInstance(InetAddresses.forString(ipv4).getAddress());
        } else if (address.getIpv6Address() != null) {
            String ipv6 = address.getIpv6Address().getValue();
            return IpAddressBinaryBuilder.getDefaultInstance(InetAddresses.forString(ipv6).getAddress());
        }
        return null;
    }
}
