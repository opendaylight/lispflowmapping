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

import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4Serializer.Length;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

import com.google.common.net.InetAddresses;

/**
 * @author Lorand Jakab
 *
 */
public class Ipv4PrefixSerializer extends LispAddressSerializer {

    private static final Ipv4PrefixSerializer INSTANCE = new Ipv4PrefixSerializer();

    // Private constructor prevents instantiation from other classes
    private Ipv4PrefixSerializer() {
    }

    public static Ipv4PrefixSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.IPV4;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Ipv4Prefix address = (Ipv4Prefix) lispAddress.getAddress();
        buffer.put((InetAddresses.forString(address.getIpv4Prefix().getValue())).getAddress());
    }

    @Override
    protected void serializeData(ByteBuffer buffer, IpPrefix prefix) {
        buffer.put((InetAddresses.forString(prefix.getIpv4Prefix().getValue())).getAddress());
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        byte[] ipBuffer = new byte[4];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
        }
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(Ipv4Afi.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(new Ipv4PrefixBuilder().setIpv4Prefix
                (new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix(
                        address.getHostAddress() + "/" + ctx.getMaskLen())).build());
        return eb.build();
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        throw new LispSerializationException("RLOCs should not be deserialized into prefixes!");
    }
}
