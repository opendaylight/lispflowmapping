/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lorand Jakab
 *
 */
public final class Ipv4Serializer extends LispAddressSerializer {

    protected static final Logger LOG = LoggerFactory.getLogger(Ipv4Serializer.class);

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
    public int getAddressSize(SimpleAddress simpleAddress) {
        return Length.IPV4;
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.IpV4.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Ipv4 address = (Ipv4) lispAddress.getAddress();
        try {
            buffer.put(Inet4Address.getByName(address.getIpv4().getValue()).getAddress());
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", address.getIpv4().getValue(), e);
        }
    }

    @Override
    protected void serializeData(ByteBuffer buffer, SimpleAddress address) {
        try {
            buffer.put(Inet4Address.getByName(address.getIpAddress().getIpv4Address().getValue()).getAddress());
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", address.getIpAddress().getIpv4Address().getValue(), e);
        }
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(Ipv4Afi.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(new Ipv4Builder().setIpv4(deserializeData(buffer)).build());
        return eb.build();
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(Ipv4Afi.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new Ipv4Builder().setIpv4(deserializeData(buffer)).build());
        return rb.build();
    }

    @Override
    protected SimpleAddress deserializeSimpleAddressData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        return new SimpleAddress(new IpAddress(deserializeData(buffer)));
    }

    private Ipv4Address deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[4];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", ipBuffer, e);
        }
        return new Ipv4Address(address.getHostAddress());
    }

    protected interface Length {
        int IPV4 = 4;
    }

}
