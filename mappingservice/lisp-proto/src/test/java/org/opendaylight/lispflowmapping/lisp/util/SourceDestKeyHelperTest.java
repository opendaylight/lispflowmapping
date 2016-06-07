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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address
        .address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class SourceDestKeyHelperTest {

    private static final String IPV4_STRING_SOURCE = "192.168.0.1";
    private static final String IPV4_STRING_DEST = "192.168.0.2";
    private static final byte[] IPV4_BYTE_SOURCE = new byte[]{-64, -88, 0, 1};
    private static final byte[] IPV4_BYTE_DEST = new byte[]{-64, -88, 0, 2};

    private static final Ipv4Address IPV4_SOURCE_ADDRESS = new Ipv4Address(IPV4_STRING_SOURCE);
    private static final Ipv4Address IPV4_DEST_ADDRESS = new Ipv4Address(IPV4_STRING_DEST);
    private static final Ipv4AddressBinary IPV4_SOURCE_ADDRESS_BINARY = new Ipv4AddressBinary(IPV4_BYTE_SOURCE);
    private static final Ipv4AddressBinary IPV4_DEST_ADDRESS_BINARY = new Ipv4AddressBinary(IPV4_BYTE_DEST);
    private static final SimpleAddress SOURCE = new SimpleAddress(new IpAddress(IPV4_SOURCE_ADDRESS));
    private static final SimpleAddress DEST = new SimpleAddress(new IpAddress(IPV4_DEST_ADDRESS));
    private static final InstanceIdType INSTANCE_ID = new InstanceIdType(100L);
    private static final Address SOURCE_DEST_KEY = getSourceDestKeyAddress(SOURCE, DEST);
    private static final Address IPV4_ADDRESS = new Ipv4Builder().setIpv4(new Ipv4Address(IPV4_STRING_SOURCE)).build();
    private static final Eid EID_IPV4_ADDRESS = new EidBuilder().setAddress(IPV4_ADDRESS).build();
    private static final Eid EID_SOURCE_DEST_KEY = getEidSourceDestKey();

    /**
     * Tests {@link SourceDestKeyHelper#getSrc} method with SourceDestKey address type.
     */
    @Test
    public void getSrcTest_sourceDestKey() {
        final Eid expectedResult = new EidBuilder()
                .setAddress(new Ipv4BinaryBuilder().setIpv4Binary(IPV4_SOURCE_ADDRESS_BINARY).build())
                .setVirtualNetworkId(INSTANCE_ID)
                .setAddressType(Ipv4BinaryAfi.class).build();
        assertEquals(expectedResult, SourceDestKeyHelper.getSrc(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrc} method with Ipv4 address type.
     */
    @Test
    public void getSrcTest_ipv4() {
        assertEquals(EID_IPV4_ADDRESS, SourceDestKeyHelper.getSrc(EID_IPV4_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDst} method with SourceDestKey address type.
     */
    @Test
    public void getDstTest_sourceDestKey() {
        final Eid expectedResult = new EidBuilder()
                .setAddress(new Ipv4BinaryBuilder().setIpv4Binary(IPV4_DEST_ADDRESS_BINARY).build())
                .setVirtualNetworkId(INSTANCE_ID)
                .setAddressType(Ipv4BinaryAfi.class).build();
        assertEquals(expectedResult, SourceDestKeyHelper.getDst(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDst} method with Ipv4 address type.
     */
    @Test
    public void getDstTest_ipv4() {
        assertEquals(EID_IPV4_ADDRESS, SourceDestKeyHelper.getDst(EID_IPV4_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrcMask} method with SourceDestKey address type.
     */
    @Test
    public void getSrcMaskTest_sourceDestKey() {
        assertEquals((short) -1, SourceDestKeyHelper.getSrcMask(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getSrcMask} method with Ipv4 address type.
     */
    @Test
    public void getSrcMaskTest_ipv4() {
        assertEquals((short) 0, SourceDestKeyHelper.getSrcMask(EID_IPV4_ADDRESS));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDstMask} method with SourceDestKey address type.
     */
    @Test
    public void getDstMaskTest_sourceDestKey() {
        assertEquals((short) -1, SourceDestKeyHelper.getDstMask(EID_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link SourceDestKeyHelper#getDstMask} method with Ipv4 address type.
     */
    @Test
    public void getDstMaskTest_ipv4() {
        assertEquals((short) 0, SourceDestKeyHelper.getDstMask(EID_IPV4_ADDRESS));
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
}
