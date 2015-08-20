/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispDistinguishedNameAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifynotification.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifynotification.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplynotification.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplynotification.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.transportaddress.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150820.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150820.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150820.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150820.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150820.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;

public class LispNotificationHelper {

    public static MapRegister convertMapRegister(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister mapRegister) {
        return new MapRegisterBuilder().setAuthenticationData(mapRegister.getAuthenticationData())
                .setEidToLocatorRecord(mapRegister.getEidToLocatorRecord()).setKeyId(mapRegister.getKeyId()).setNonce(mapRegister.getNonce())
                .setProxyMapReply(mapRegister.isProxyMapReply()).setWantMapNotify(mapRegister.isWantMapNotify())
                .setXtrSiteIdPresent(mapRegister.isXtrSiteIdPresent()).setXtrId(mapRegister.getXtrId()).setSiteId(mapRegister.getSiteId()).build();
    }

    public static MapNotify convertMapNotify(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify mapNotify) {
        return new MapNotifyBuilder().setAuthenticationData(mapNotify.getAuthenticationData())
                .setEidToLocatorRecord(mapNotify.getEidToLocatorRecord()).setKeyId(mapNotify.getKeyId()).setNonce(mapNotify.getNonce())
                .setXtrSiteIdPresent(mapNotify.isXtrSiteIdPresent()).setXtrId(mapNotify.getXtrId()).setSiteId(mapNotify.getSiteId()).build();
    }

    public static MapRequest convertMapRequest(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest mapRequest) {
        return new MapRequestBuilder().setAuthoritative(mapRequest.isAuthoritative()).setEidRecord(mapRequest.getEidRecord())
                .setItrRloc(mapRequest.getItrRloc()).setMapDataPresent(mapRequest.isMapDataPresent()).setMapReply(mapRequest.getMapReply())
                .setNonce(mapRequest.getNonce()).setPitr(mapRequest.isPitr()).setProbe(mapRequest.isProbe()).setSmr(mapRequest.isSmr())
                .setSmrInvoked(mapRequest.isSmrInvoked()).setSourceEid(mapRequest.getSourceEid()).build();
    }

    public static MapReply convertMapReply(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapReply mapReply) {
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
            EidToLocatorRecord record = mapRegisterNotification.getMapRegister().getEidToLocatorRecord().get(i);
            MappingBuilder mb = new MappingBuilder();
            mb.setEid(new EidUri(LispAddressStringifier.getURIString(
                    record.getLispAddressContainer(), record.getMaskLength())));
            mb.setOrigin(MappingOrigin.Southbound);
            mb.setSiteId(getSiteId(mapRegisterNotification.getMapRegister()));
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
