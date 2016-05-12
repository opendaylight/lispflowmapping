/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.util;

import java.net.InetAddress;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifynotification.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifynotification.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplynotification.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplynotification.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;

public final class LispNotificationHelper {
    // Utility class, should not be instantiated
    private LispNotificationHelper() {
    }

    public static MapRegister convertMapRegister(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister mapRegister) {
        return new MapRegisterBuilder(mapRegister).build();
    }

    public static MapNotify convertMapNotify(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify mapNotify) {
        return new MapNotifyBuilder(mapNotify).build();
    }

    public static MapRequest convertMapRequest(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest mapRequest) {
        return new MapRequestBuilder().setAuthoritative(mapRequest.isAuthoritative())
                .setEidItem(mapRequest.getEidItem()).setItrRloc(mapRequest.getItrRloc())
                .setMapDataPresent(mapRequest.isMapDataPresent()).setMapReply(mapRequest.getMapReply())
                .setNonce(mapRequest.getNonce()).setPitr(mapRequest.isPitr()).setProbe(mapRequest.isProbe())
                .setSmr(mapRequest.isSmr()).setSmrInvoked(mapRequest.isSmrInvoked())
                .setSourceEid(mapRequest.getSourceEid()).build();
    }

    public static MapReply convertMapReply(
            org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply mapReply) {
        return new MapReplyBuilder().setEchoNonceEnabled(mapReply.isEchoNonceEnabled())
                .setMappingRecordItem(mapReply.getMappingRecordItem()).setNonce(mapReply.getNonce())
                .setProbe(mapReply.isProbe()).setSecurityEnabled(mapReply.isSecurityEnabled()).build();
    }

    public static IpAddressBinary getIpAddressBinaryFromInetAddress(InetAddress address) {
        InetAddress inetAddress = address;
        if (inetAddress == null) {
            inetAddress = InetAddress.getLoopbackAddress();
        }

        return IpAddressBinaryBuilder.getDefaultInstance(address.getAddress());
    }
}
