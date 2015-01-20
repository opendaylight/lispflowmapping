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

import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispDistinguishedNameAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifynotification.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifynotification.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapreplynotification.MapReply;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapreplynotification.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.transportaddress.TransportAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.transportaddress.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;

public class LispNotificationHelper {

    public static MapRegister convertMapRegister(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister mapRegister) {
        return new MapRegisterBuilder().setAuthenticationData(mapRegister.getAuthenticationData())
                .setEidToLocatorRecord(mapRegister.getEidToLocatorRecord()).setKeyId(mapRegister.getKeyId()).setNonce(mapRegister.getNonce())
                .setProxyMapReply(mapRegister.isProxyMapReply()).setWantMapNotify(mapRegister.isWantMapNotify()).build();
    }
    
    public static MapNotify convertMapNotify(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify mapNotify) {
        return new MapNotifyBuilder().setAuthenticationData(mapNotify.getAuthenticationData())
                .setEidToLocatorRecord(mapNotify.getEidToLocatorRecord()).setKeyId(mapNotify.getKeyId()).setNonce(mapNotify.getNonce())
                .setProxyMapReply(mapNotify.isProxyMapReply()).setWantMapNotify(mapNotify.isWantMapNotify()).build();
    }

    public static MapRequest convertMapRequest(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest mapRequest) {
        return new MapRequestBuilder().setAuthoritative(mapRequest.isAuthoritative()).setEidRecord(mapRequest.getEidRecord())
                .setItrRloc(mapRequest.getItrRloc()).setMapDataPresent(mapRequest.isMapDataPresent()).setMapReply(mapRequest.getMapReply())
                .setNonce(mapRequest.getNonce()).setPitr(mapRequest.isPitr()).setProbe(mapRequest.isProbe()).setSmr(mapRequest.isSmr())
                .setSmrInvoked(mapRequest.isSmrInvoked()).setSourceEid(mapRequest.getSourceEid()).build();
    }

    public static MapReply convertMapReply(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply mapReply) {
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
        if (address instanceof Ipv4) {
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((Ipv4) address).getIpv4Address().getValue()));
            tab.setPort(new PortNumber(LispMessage.PORT_NUM));
        } else if (address instanceof LcafKeyValue) {
            PrimitiveAddress primitiveAddress = ((LcafKeyValue) address).getValue().getPrimitiveAddress();
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
            tab.setIpAddress(IpAddressBuilder.getDefaultInstance(((LispIpv4Address) appData.getAddress().getPrimitiveAddress()).getIpv4Address()
                    .getValue()));
            tab.setPort(new PortNumber(appData.getLocalPort()));
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
}
