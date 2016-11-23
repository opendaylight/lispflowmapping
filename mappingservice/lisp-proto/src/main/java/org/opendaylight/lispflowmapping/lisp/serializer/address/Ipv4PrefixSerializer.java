/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
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

import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4Serializer.Length;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to (de)serialize IPv4 prefixes from/to String representation.
 *
 * @author Lorand Jakab
 *
 */
public final class Ipv4PrefixSerializer extends LispAddressSerializer {

    protected static final Logger LOG = LoggerFactory.getLogger(Ipv4PrefixSerializer.class);

    private static final Ipv4PrefixSerializer INSTANCE = new Ipv4PrefixSerializer();

    // Private constructor prevents instantiation from other classes
    private Ipv4PrefixSerializer() {
    }

    public static Ipv4PrefixSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        // XXX does this need to worry about the mask too?
        return Length.IPV4;
    }

    @Override
    public int getAddressSize(SimpleAddress simpleAddress) {
        // XXX does this need to worry about the mask too?
        return Length.IPV4;
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.IpV4.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Ipv4Prefix prefix = (Ipv4Prefix) lispAddress.getAddress();
        String address = MaskUtil.getAddressStringForIpv4Prefix(prefix);
        buffer.put(InetAddresses.forString(address).getAddress());
    }

    @Override
    protected void serializeData(ByteBuffer buffer, SimpleAddress address) {
        buffer.put(InetAddresses.forString(MaskUtil.getAddressStringForIpPrefix(address.getIpPrefix())).getAddress());
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(Ipv4PrefixAfi.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(deserializeData(buffer, ctx)).build());
        return eb.build();
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        throw new LispSerializationException("RLOCs should not be deserialized into prefixes!");
    }

    @Override
    protected SimpleAddress deserializeSimpleAddressData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        return new SimpleAddress(new IpPrefix(deserializeData(buffer, ctx)));
    }

    private static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix
            deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        byte[] ipBuffer = new byte[4];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", ipBuffer, e);
        }
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix(
                address.getHostAddress() + "/" + ctx.getMaskLen());
    }
}
