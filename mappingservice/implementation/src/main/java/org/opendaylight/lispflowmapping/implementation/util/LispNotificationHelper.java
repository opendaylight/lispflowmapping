/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispDistinguishedNameAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapreplynotification.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapreplynotification.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.transportaddress.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;

public class LispNotificationHelper {

    public static MapRegister convertMapRegister(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister mapRegister) {
        return new MapRegisterBuilder().setAuthenticationData(mapRegister.getAuthenticationData())
                .setEidToLocatorRecord(mapRegister.getEidToLocatorRecord()).setKeyId(mapRegister.getKeyId()).setNonce(mapRegister.getNonce())
                .setProxyMapReply(mapRegister.isProxyMapReply()).setWantMapNotify(mapRegister.isWantMapNotify())
                .setXtrSiteIdPresent(mapRegister.isXtrSiteIdPresent()).setXtrId(mapRegister.getXtrId()).setSiteId(mapRegister.getSiteId()).build();
    }

    public static MapRequest convertMapRequest(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest mapRequest) {
        return new MapRequestBuilder().setAuthoritative(mapRequest.isAuthoritative()).setEidRecord(mapRequest.getEidRecord())
                .setItrRloc(mapRequest.getItrRloc()).setMapDataPresent(mapRequest.isMapDataPresent()).setMapReply(mapRequest.getMapReply())
                .setNonce(mapRequest.getNonce()).setPitr(mapRequest.isPitr()).setProbe(mapRequest.isProbe()).setSmr(mapRequest.isSmr())
                .setSmrInvoked(mapRequest.isSmrInvoked()).setSourceEid(mapRequest.getSourceEid()).build();
    }

    public static MapReply convertMapReply(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply mapReply) {
        return new MapReplyBuilder().setEchoNonceEnabled(mapReply.isEchoNonceEnabled()).setEidToLocatorRecord(mapReply.getEidToLocatorRecord())
                .setNonce(mapReply.getNonce()).setProbe(mapReply.isProbe()).setSecurityEnabled(mapReply.isSecurityEnabled()).build();
    }

    public static IpAddress getIpAddressFromInetAddress(InetAddress inetAddress) {
        if (inetAddress == null) {
            inetAddress = InetAddress.getLoopbackAddress();
        }
        if (inetAddress instanceof Inet4Address) {
            return new IpAddress(new Ipv4Address(inetAddress.getHostAddress()));
        } else {
            return new IpAddress(new Ipv6Address(inetAddress.getHostAddress()));
        }
    }

    public static InetAddress getInetAddressFromIpAddress(IpAddress ipAddress) {
        InetAddress address = null;
        if (ipAddress != null) {
            if (ipAddress.getIpv4Address() != null) {
                address = getAddressByName(ipAddress.getIpv4Address().getValue());
            } else if (ipAddress.getIpv6Address() != null) {
                address = getAddressByName(ipAddress.getIpv6Address().getValue());
            }
        }
        if (address == null) {
            address = InetAddress.getLoopbackAddress();
        }
        return address;
    }

    public static TransportAddress getTransportAddressFromContainer(LispAddressContainer container) {
        TransportAddressBuilder tab = new TransportAddressBuilder();
        LispAFIAddress address = LispAFIConvertor.toAFI(container);
        if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address) {
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address) address).getIpv4Address().getValue()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address) {
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address) address).getIpv6Address().getValue()));
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
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4) appData.getAddress().getPrimitiveAddress()).getIpv4Address()
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
            EidToLocatorRecord record = mapRegisterNotification.getMapRegister().getEidToLocatorRecord().get(i);
            MappingBuilder mb = new MappingBuilder();
            mb.setEid(new EidUri(LispAddressStringifier.getURIString(
                    record.getLispAddressContainer(), record.getMaskLength())));
            mb.setOrigin(MappingOrigin.Southbound);
            mb.setRecordTtl(record.getRecordTtl());
            mb.setMaskLength(record.getMaskLength());
            mb.setMapVersion(record.getMapVersion());
            mb.setAction(record.getAction());
            mb.setAuthoritative(record.isAuthoritative());
            mb.setLispAddressContainer(record.getLispAddressContainer());
            mb.setLocatorRecord(record.getLocatorRecord());
            mappings.add(mb.build());
        }
        return mappings;
    }
}
