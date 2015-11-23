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

import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

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
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        throw new LispSerializationException("EIDs should be deserialized into prefixes!");
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(Ipv6Afi.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new Ipv6Builder().setIpv6(deserializeData(buffer)).build());
        return rb.build();
    }

    @Override
    protected SimpleAddress deserializeSimpleAddressData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        return new SimpleAddress(new IpAddress(deserializeData(buffer)));
    }

    private Ipv6Address deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[16];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
        }
        return new Ipv6Address(address.getHostAddress());
    }

    protected interface Length {
        int IPV6 = 16;
    }

}
