/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to (de)serialize IPv6 addresses from/to byte[] representation.
 *
 * @author Lorand Jakab
 *
 */
public final class Ipv6BinarySerializer extends LispAddressSerializer {

    protected static final Logger LOG = LoggerFactory.getLogger(Ipv6BinarySerializer.class);

    private static final Ipv6BinarySerializer INSTANCE = new Ipv6BinarySerializer();

    // Private constructor prevents instantiation from other classes
    private Ipv6BinarySerializer() {
    }

    public static Ipv6BinarySerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.IPV6;
    }

    @Override
    public int getAddressSize(SimpleAddress simpleAddress) {
        return Length.IPV6;
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.IpV6.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Ipv6Binary address = (Ipv6Binary) lispAddress.getAddress();
        buffer.put(address.getIpv6Binary().getValue());
    }

    @Override
    protected void serializeData(ByteBuffer buffer, SimpleAddress address) {
        buffer.put(InetAddresses.forString(address.getIpAddress().getIpv6Address().getValue()).getAddress());
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(Ipv6BinaryAfi.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(new Ipv6BinaryBuilder().setIpv6Binary(deserializeData(buffer)).build());
        return eb.build();
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(Ipv6BinaryAfi.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new Ipv6BinaryBuilder().setIpv6Binary(deserializeData(buffer)).build());
        return rb.build();
    }

    @Override
    protected SimpleAddress deserializeSimpleAddressData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        return new SimpleAddress(new IpAddress(deserializeDataNonBinary(buffer)));
    }

    private static Ipv6AddressBinary deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[16];
        buffer.get(ipBuffer);
        return new Ipv6AddressBinary(ipBuffer);
    }

    private static Ipv6Address deserializeDataNonBinary(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[16];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", ipBuffer, e);
        }
        return new Ipv6Address(address.getHostAddress());
    }

    protected interface Length {
        int IPV6 = 16;
    }

}
