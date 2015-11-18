/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lisp.address.grouping.LispAddressContainerBuilder;

import com.google.common.net.InetAddresses;

/**
 * @author Lorand Jakab
 *
 */
public class Ipv4Serializer extends LispAddressSerializer {

    private static final Ipv4Serializer INSTANCE = new Ipv4Serializer();

    // Private constructor prevents instantiation from other classes
    private Ipv4Serializer() {
    }

    public static Ipv4Serializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.IPV4;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Ipv4 address = (Ipv4) lispAddress.getAddress();
        buffer.put((InetAddresses.forString(address.getIpv4().getValue())).getAddress());
    }

    @Override
    protected LispAddress deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        byte[] ipBuffer = new byte[4];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
        }
        LispAddressContainerBuilder lab = new LispAddressContainerBuilder();
        lab.setAddressType(Ipv4Afi.class);
        lab.setVirtualNetworkId(ctx.getVni());
        lab.setAddress(new Ipv4Builder().setIpv4(new Ipv4Address(address.getHostAddress())).build());
        return lab.build();
    }

    protected interface Length {
        int IPV4 = 4;
    }

}
