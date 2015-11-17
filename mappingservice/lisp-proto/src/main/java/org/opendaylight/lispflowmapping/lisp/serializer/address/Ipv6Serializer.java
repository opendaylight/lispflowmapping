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

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lisp.address.grouping.LispAddressContainerBuilder;

import com.google.common.net.InetAddresses;

/**
 * @author Lorand Jakab
 *
 */
public class Ipv6Serializer extends LispAddressSerializer {

    private static final Ipv6Serializer INSTANCE = new Ipv6Serializer();

    // Private constructor prevents instantiation from other classes
    private Ipv6Serializer() {
    }

    public static Ipv6Serializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.IPV6;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Ipv6 address = (Ipv6) lispAddress.getAddress();
        buffer.put((InetAddresses.forString(address.getIpv6().getValue())).getAddress());
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
        lab.setAddressType(Ipv6Afi.class);
        lab.setVirtualNetworkId(ctx.getVni());
        lab.setAddress(new Ipv6Builder().setIpv6(new Ipv6Address(address.getHostAddress())).build());
        return lab.build();
    }

    protected interface Length {
        int IPV6 = 16;
    }

}
