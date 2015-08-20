/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.util;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifynotification.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifynotification.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplynotification.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplynotification.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;

public class LispNotificationHelper {
    public static MapRegister convertMapRegister(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister mapRegister) {
        return new MapRegisterBuilder().setAuthenticationData(mapRegister.getAuthenticationData())
                .setEidToLocatorRecord(mapRegister.getEidToLocatorRecord()).setKeyId(mapRegister.getKeyId())
                .setNonce(mapRegister.getNonce()).setProxyMapReply(mapRegister.isProxyMapReply())
                .setWantMapNotify(mapRegister.isWantMapNotify()).setXtrSiteIdPresent(mapRegister.isXtrSiteIdPresent())
                .setXtrId(mapRegister.getXtrId()).setSiteId(mapRegister.getSiteId()).build();
    }

    public static MapNotify convertMapNotify(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify mapNotify) {
        return new MapNotifyBuilder().setAuthenticationData(mapNotify.getAuthenticationData())
                .setEidToLocatorRecord(mapNotify.getEidToLocatorRecord()).setKeyId(mapNotify.getKeyId())
                .setNonce(mapNotify.getNonce()).setXtrSiteIdPresent(mapNotify.isXtrSiteIdPresent())
                .setXtrId(mapNotify.getXtrId()).setSiteId(mapNotify.getSiteId()).build();
    }

    public static MapRequest convertMapRequest(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest mapRequest) {
        return new MapRequestBuilder().setAuthoritative(mapRequest.isAuthoritative())
                .setEidRecord(mapRequest.getEidRecord()).setItrRloc(mapRequest.getItrRloc())
                .setMapDataPresent(mapRequest.isMapDataPresent()).setMapReply(mapRequest.getMapReply())
                .setNonce(mapRequest.getNonce()).setPitr(mapRequest.isPitr()).setProbe(mapRequest.isProbe())
                .setSmr(mapRequest.isSmr()).setSmrInvoked(mapRequest.isSmrInvoked())
                .setSourceEid(mapRequest.getSourceEid()).build();
    }

    public static MapReply convertMapReply(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapReply mapReply) {
        return new MapReplyBuilder().setEchoNonceEnabled(mapReply.isEchoNonceEnabled())
                .setEidToLocatorRecord(mapReply.getEidToLocatorRecord()).setNonce(mapReply.getNonce())
                .setProbe(mapReply.isProbe()).setSecurityEnabled(mapReply.isSecurityEnabled()).build();
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
}
