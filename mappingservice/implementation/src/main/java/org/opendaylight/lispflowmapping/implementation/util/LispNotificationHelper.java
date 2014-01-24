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

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;

public class LispNotificationHelper {

    public static MapRegister convertMapRegister(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister mapRegister) {
        return new MapRegisterBuilder().setAuthenticationData(mapRegister.getAuthenticationData())
                .setEidToLocatorRecord(mapRegister.getEidToLocatorRecord()).setKeyId(mapRegister.getKeyId()).setNonce(mapRegister.getNonce())
                .setProxyMapReply(mapRegister.isProxyMapReply()).setWantMapNotify(mapRegister.isWantMapNotify()).build();
    }

    public static MapRequest convertMapRequest(org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest mapRequest) {
        return new MapRequestBuilder().setAuthoritative(mapRequest.isAuthoritative()).setEidRecord(mapRequest.getEidRecord())
                .setItrRloc(mapRequest.getItrRloc()).setMapDataPresent(mapRequest.isMapDataPresent()).setMapReply(mapRequest.getMapReply())
                .setNonce(mapRequest.getNonce()).setPitr(mapRequest.isPitr()).setProbe(mapRequest.isProbe()).setSmr(mapRequest.isSmr())
                .setSmrInvoked(mapRequest.isSmrInvoked()).setSourceEid(mapRequest.getSourceEid()).build();
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

    public static InetAddress getInetAddressFromContainer(LispAddressContainer container) {
        Ipv4Address ipAddress = ((Ipv4) LispAFIConvertor.toAFI(container)).getIpv4Address();
        return getAddressByName(ipAddress.getValue());
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
