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
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv6Serializer.Length;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to (de)serialize IPv6 prefixes from/to byte[] representation.
 *
 * @author Lorand Jakab
 *
 */
public final class Ipv6PrefixBinarySerializer extends LispAddressSerializer {

    protected static final Logger LOG = LoggerFactory.getLogger(Ipv6PrefixBinarySerializer.class);

    private static final Ipv6PrefixBinarySerializer INSTANCE = new Ipv6PrefixBinarySerializer();

    // Private constructor prevents instantiation from other classes
    private Ipv6PrefixBinarySerializer() {
    }

    public static Ipv6PrefixBinarySerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        // XXX does this need to worry about the mask too?
        return Length.IPV6;
    }

    @Override
    public int getAddressSize(SimpleAddress simpleAddress) {
        // XXX does this need to worry about the mask too?
        return Length.IPV6;
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.IpV6.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Ipv6PrefixBinary prefix = (Ipv6PrefixBinary) lispAddress.getAddress();
        buffer.put(prefix.getIpv6AddressBinary().getValue());
    }

    @Override
    protected void serializeData(ByteBuffer buffer, IpPrefix prefix) {
        buffer.put(InetAddresses.forString(prefix.getIpv6Prefix().getValue()).getAddress());
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        byte[] ipBuffer = new byte[16];
        buffer.get(ipBuffer);
        Ipv6PrefixBinary prefix = new Ipv6PrefixBinaryBuilder()
                .setIpv6AddressBinary(new Ipv6AddressBinary(ipBuffer)).setIpv6MaskLength(ctx.getMaskLen()).build();
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(Ipv6PrefixBinaryAfi.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(prefix);
        return eb.build();
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        throw new LispSerializationException("RLOCs should not be deserialized into prefixes!");
    }

    @Override
    protected SimpleAddress deserializeSimpleAddressData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        byte[] ipBuffer = new byte[16];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", ipBuffer, e);
        }
        return new SimpleAddress(new IpPrefix(new Ipv6Prefix(address.getHostAddress() + "/" + ctx.getMaskLen())));
    }
}
