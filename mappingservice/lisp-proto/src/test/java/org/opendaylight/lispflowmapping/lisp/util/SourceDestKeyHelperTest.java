/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address
        .address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address
        .address.Ipv4PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class SourceDestKeyHelperTest {

    private static final String IPV4_STRING_SOURCE = "192.168.0.1";
    private static final String IPV4_STRING_DEST =   "192.168.0.2";
    private static final short MASK_1 = 20;
    private static final short MASK_2 = 24;

    private static final byte[] IPV4_BYTE_SOURCE = new byte[]{-64, -88, 0, 1};
    private static final byte[] IPV4_BYTE_DEST = new byte[]{-64, -88, 0, 2};

    private static final Ipv4Prefix IPV4_PREFIX_SOURCE = new Ipv4Prefix(IPV4_STRING_SOURCE + "/" + MASK_1);
    private static final Ipv4Prefix IPV4_PREFIX_DEST = new Ipv4Prefix(IPV4_STRING_DEST + "/" + MASK_2);
    private static final Ipv4AddressBinary IPV4_SOURCE_ADDRESS_BINARY = new Ipv4AddressBinary(IPV4_BYTE_SOURCE);
    private static final Ipv4AddressBinary IPV4_DEST_ADDRESS_BINARY = new Ipv4AddressBinary(IPV4_BYTE_DEST);
    private static final Ipv4PrefixBinary IPV4_SOURCE_PREFIX_BINARY =
            getIpv4PrefixBinary(IPV4_SOURCE_ADDRESS_BINARY, MASK_1);
    private static final Ipv4PrefixBinary IPV4_DEST_PREFIX_BINARY =
            getIpv4PrefixBinary(IPV4_DEST_ADDRESS_BINARY, MASK_2);

    private static final SimpleAddress SOURCE = new SimpleAddress(new IpPrefix(IPV4_PREFIX_SOURCE));
    private static final SimpleAddress DEST = new SimpleAddress(new IpPrefix(IPV4_PREFIX_DEST));
    private static final InstanceIdType INSTANCE_ID = new InstanceIdType(100L);
    private static final Address SOURCE_DEST_KEY = getSourceDestKeyAddress(SOURCE, DEST);
    private static final Address IPV4_PREFIX = new Ipv4PrefixBuilder().setIpv4Prefix(IPV4_PREFIX_SOURCE).build();
    private static final Eid EID_IPV4_PREFIX_ADDRESS = new EidBuilder().setAddress(IPV4_PREFIX).build();
    private static final Eid EID_SOURCE_DEST_KEY = getEidSourceDestKey();

    /**
     * Tests {@link SourceDestKeyHelper#getSrc} method with SourceDestKey address type.
     */
    @Test
    public void getSrcTest_sourceDestKey() {
        final Eid expectedResult = new EidBuilder()
                .setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(IPV4_PREFIX_SOURCE).build())
                .setVirtualNetworkId(INSTANCE_ID)
                .setAddressType(Ipv4PrefixAfi.class).build();
        assertEquals(expectedResult, SourceDestKeyHelper.getSrc(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrc} method with Ipv4Prefix address type.
     */
    @Test
    public void getSrcTest_ipv4Prefix() {
        assertEquals(EID_IPV4_PREFIX_ADDRESS, SourceDestKeyHelper.getSrc(EID_IPV4_PREFIX_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDst} method with SourceDestKey address type.
     */
    @Test
    public void getDstTest_sourceDestKey() {
        final Eid expectedResult = new EidBuilder()
                .setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(IPV4_PREFIX_DEST).build())
                .setVirtualNetworkId(INSTANCE_ID)
                .setAddressType(Ipv4PrefixAfi.class).build();
        assertEquals(expectedResult, SourceDestKeyHelper.getDst(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDst} method with Ipv4Prefix address type.
     */
    @Test
    public void getDstTest_ipv4Prefix() {
        assertEquals(EID_IPV4_PREFIX_ADDRESS, SourceDestKeyHelper.getDst(EID_IPV4_PREFIX_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrcBinary} method with SourceDestKey address type.
     */
    @Test
    public void getSrcBinaryTest_sourceDestKey() {
        final Eid expectedResult = new EidBuilder()
                .setAddress(IPV4_SOURCE_PREFIX_BINARY)
                .setVirtualNetworkId(INSTANCE_ID)
                .setAddressType(Ipv4PrefixBinaryAfi.class).build();
        assertEquals(expectedResult, SourceDestKeyHelper.getSrcBinary(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrcBinary} method with Ipv4Prefix address type.
     */
    @Test
    public void getSrcBinaryTest_ipv4Prefix() {
        assertEquals(EID_IPV4_PREFIX_ADDRESS, SourceDestKeyHelper.getSrcBinary(EID_IPV4_PREFIX_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDstBinary} method with SourceDestKey address type.
     */
    @Test
    public void getDstBinaryTest_sourceDestKey() {
        final Eid expectedResult = new EidBuilder()
                .setAddress(IPV4_DEST_PREFIX_BINARY)
                .setVirtualNetworkId(INSTANCE_ID)
                .setAddressType(Ipv4PrefixBinaryAfi.class).build();
        assertEquals(expectedResult, SourceDestKeyHelper.getDstBinary(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDstBinary} method with Ipv4Prefix address type.
     */
    @Test
    public void getDstBinaryTest_ipv4Prefix() {
        assertEquals(EID_IPV4_PREFIX_ADDRESS, SourceDestKeyHelper.getDstBinary(EID_IPV4_PREFIX_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrcMask} method with SourceDestKey address type.
     */
    @Test
    public void getSrcMaskTest_sourceDestKey() {
        assertEquals((short) 20, SourceDestKeyHelper.getSrcMask(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrcMask} method with Ipv4Prefix address type.
     */
    @Test
    public void getSrcMaskTest_ipv4Prefix() {
        assertEquals((short) 0, SourceDestKeyHelper.getSrcMask(EID_IPV4_PREFIX_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDstMask} method with SourceDestKey address type.
     */
    @Test
    public void getDstMaskTest_sourceDestKey() {
        assertEquals((short) 24, SourceDestKeyHelper.getDstMask(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDstMask} method with Ipv4Prefix address type.
     */
    @Test
    public void getDstMaskTest_ipv4Prefix() {
        assertEquals((short) 0, SourceDestKeyHelper.getDstMask(EID_IPV4_PREFIX_ADDRESS));
    }

    private static SourceDestKey getSourceDestKeyAddress(SimpleAddress sourceAddress, SimpleAddress destAddress) {
        return new SourceDestKeyBuilder()
                .setSourceDestKey(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                        .rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder()
                        .setSource(sourceAddress)
                        .setDest(destAddress).build())
                .build();
    }

    private static Eid getEidSourceDestKey() {
        return new EidBuilder().setAddress(SOURCE_DEST_KEY).setVirtualNetworkId(INSTANCE_ID).build();
    }

    private static Ipv4PrefixBinary getIpv4PrefixBinary(Ipv4AddressBinary ipv4AddressBinary, short mask) {
        return new Ipv4PrefixBinaryBuilder().setIpv4AddressBinary(ipv4AddressBinary).setIpv4MaskLength(mask).build();
    }
}
