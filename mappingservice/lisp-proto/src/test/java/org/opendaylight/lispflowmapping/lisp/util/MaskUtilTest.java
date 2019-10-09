/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.common.UintConversions.fromJava;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceIdBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaskUtilTest {

    protected static final Logger LOG = LoggerFactory.getLogger(MaskUtilTest.class);
    private static final String IPV4_STRING = "255.255.255.255";
    private static final String IPV6_STRING = "1111:2222:3333:4444:5555:6666:7777:8888";
    private static byte[] ipv4ByteAddress;
    private static byte[] ipv4ExpectedByteAddress;
    private static byte[] ipv6ByteAddress;
    private static byte[] ipv6ExpectedByteAddress;

    static {
        try {
            ipv4ByteAddress = Inet4Address.getByName(IPV4_STRING).getAddress();
            ipv4ExpectedByteAddress = Inet4Address.getByName("255.255.240.0").getAddress();
            ipv6ByteAddress = Inet6Address.getByName(IPV6_STRING).getAddress();
            ipv6ExpectedByteAddress = Inet6Address.getByName("1111:2222:3333:4444:4000::").getAddress();
        } catch (UnknownHostException e) {
            LOG.error("Unknow host", e);
        }
    }

    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.3.4.0";
    private static final String MASK_1 =        "/32";
    private static final String MASK_2 =        "/20";
    private static final String MASK_3 =        "/66";
    private static final String MASK_4 =        "/128";
    private static final String MAC_STRING =    "aa:bb:cc:dd:ee:ff";

    private static final Ipv4Address IPV4_ADDRESS = new Ipv4Address(IPV4_STRING_1);
    private static final Ipv6Address IPV6_ADDRESS = new Ipv6Address(IPV6_STRING);
    private static final Ipv4Prefix IPV4_PREFIX = new Ipv4Prefix(IPV4_STRING_1 + MASK_1);
    private static final Ipv6Prefix IPV6_PREFIX = new Ipv6Prefix(IPV6_STRING + MASK_4);
    private static final Ipv4AddressBinary IPV4_ADDRESS_BINARY = new Ipv4AddressBinary(ipv4ByteAddress);
    private static final Ipv6AddressBinary IPV6_ADDRESS_BINARY = new Ipv6AddressBinary(ipv6ByteAddress);
    private static final Ipv4PrefixBinary IPV4_PREFIX_BINARY = new Ipv4PrefixBinaryBuilder()
            .setIpv4AddressBinary(IPV4_ADDRESS_BINARY).setIpv4MaskLength(fromJava((short) 32)).build();
    private static final Ipv6PrefixBinary IPV6_PREFIX_BINARY = new Ipv6PrefixBinaryBuilder()
            .setIpv6AddressBinary(IPV6_ADDRESS_BINARY).setIpv6MaskLength(fromJava((short) 128)).build();

    private static final Address ADDRESS_IPV4 = new Ipv4Builder().setIpv4(IPV4_ADDRESS).build();
    private static final Address ADDRESS_IPV6 = new Ipv6Builder().setIpv6(new Ipv6Address(IPV6_STRING)).build();
    private static final Address ADDRESS_IPV4_BINARY = new Ipv4BinaryBuilder()
            .setIpv4Binary(IPV4_ADDRESS_BINARY).build();
    private static final Address ADDRESS_IPV6_BINARY = new Ipv6BinaryBuilder()
            .setIpv6Binary(IPV6_ADDRESS_BINARY).build();
    private static final Address ADDRESS_IPV4_PREFIX = new Ipv4PrefixBuilder()
            .setIpv4Prefix(IPV4_PREFIX).build();
    private static final Address ADDRESS_IPV6_PREFIX = new Ipv6PrefixBuilder()
            .setIpv6Prefix(IPV6_PREFIX).build();
    private static final Address ADDRESS_SOURCE_DEST_KEY = getDefaultSourceDestKeyAddressBuilder().build();
    private static final Address ADDRESS_INSTANCE_ID_IP_PREFIX = getDefaultIpPrefixInstanceId();
    private static final Address ADDRESS_INSTANCE_ID_IP = getDefaultIpInstanceId();
    private static final Address ADDRESS_MAC = new MacBuilder().setMac(new MacAddress(MAC_STRING)).build();
    private static final Address ADDRESS_SERVICE_PATH = getDefaultServicePath();

    private static final SimpleAddress SIMPLE_ADDRESS_IPV4 = new SimpleAddress(new IpAddress(IPV4_ADDRESS));
    private static final SimpleAddress SIMPLE_ADDRESS_IPV4_PREFIX = new SimpleAddress(new IpPrefix(IPV4_PREFIX));
    private static final SimpleAddress SIMPLE_ADDRESS_IPV6_PREFIX = new SimpleAddress(new IpPrefix(IPV6_PREFIX));

    private static final Eid EID_IPV4 = LispAddressUtil.toEid(IPV4_ADDRESS, null);
    private static final Eid EID_IPV6 = LispAddressUtil.toEid(IPV6_ADDRESS, null);
    private static final Eid EID_IPV4_PREFIX = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING + MASK_2);
    private static final Eid EID_IPV6_PREFIX = LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + MASK_3);
    private static final Eid EID_IPV4_PREFIX_BINARY = LispAddressUtil.asIpv4PrefixBinaryEid(IPV4_STRING + MASK_2);
    private static final Eid EID_IPV6_PREFIX_BINARY = LispAddressUtil.asIpv6PrefixBinaryEid(IPV6_STRING + MASK_3);
    private static final Eid EID_INSTANCE_ID = new EidBuilder().setAddress(ADDRESS_INSTANCE_ID_IP).build();
    private static final Eid EID_SOURCE_DEST_KEY = new EidBuilder().setAddress(ADDRESS_SOURCE_DEST_KEY).build();
    private static final Eid EID_SERVICE_PATH = new EidBuilder().setAddress(ADDRESS_SERVICE_PATH).build();
    private static final Eid EID_MAC = new EidBuilder().setAddress(ADDRESS_MAC).build();

    /**
     * Tests {@link MaskUtil#isMaskable} method.
     */
    @Test
    public void isMaskableTest() {
        assertTrue(MaskUtil.isMaskable(ADDRESS_IPV4_PREFIX));
        assertTrue(MaskUtil.isMaskable(ADDRESS_IPV6_PREFIX));
        assertTrue(MaskUtil.isMaskable(ADDRESS_SOURCE_DEST_KEY));
        assertTrue(MaskUtil.isMaskable(ADDRESS_INSTANCE_ID_IP_PREFIX));
        assertFalse(MaskUtil.isMaskable(ADDRESS_INSTANCE_ID_IP));
        assertFalse(MaskUtil.isMaskable(ADDRESS_MAC));
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid, short mask)} method with Ipv4PrefixBinary.
     */
    @Test
    public void normalizeTest_withIpv4PrefixBinary_withMask() throws UnknownHostException {
        // input ip: 255.255.255.255, mask: 20
        final Eid result = MaskUtil.normalize(EID_IPV4_PREFIX_BINARY, (short) 20);
        final Ipv4PrefixBinary resultPrefix = (Ipv4PrefixBinary) result.getAddress();

        // expected result
        assertArrayEquals(ipv4ExpectedByteAddress, resultPrefix.getIpv4AddressBinary().getValue());
        assertEquals((short) 20, (short) resultPrefix.getIpv4MaskLength().toJava());
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid, short mask)} method with Ipv6PrefixBinary.
     */
    @Test
    public void normalizeTest_withIpv6PrefixBinary_withMask() throws UnknownHostException {
        // input ip: 1111:2222:3333:4444:5555:6666:7777:8888, mask: 66
        final Eid result = MaskUtil.normalize(EID_IPV6_PREFIX_BINARY, (short) 66);
        final Ipv6PrefixBinary resultPrefix = (Ipv6PrefixBinary) result.getAddress();

        // expected result
        assertArrayEquals(ipv6ExpectedByteAddress, resultPrefix.getIpv6AddressBinary().getValue());
        assertEquals((short) 66, (short) resultPrefix.getIpv6MaskLength().toJava());
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid, short mask)} method with Ipv4Prefix.
     */
    @Test
    public void normalizeTest_withIpv4Prefix_withMask() {
        // input ip: 255.255.255.255, mask: 20
        final Eid result = MaskUtil.normalize(EID_IPV4_PREFIX, (short) 20);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.Ipv4Prefix resultPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv4Prefix) result.getAddress();

        // expected result
        final String expectedResult = "255.255.240.0" + MASK_2;
        assertEquals(expectedResult, resultPrefix.getIpv4Prefix().getValue());
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid, short mask)} method with Ipv6Prefix.
     */
    @Test
    public void normalizeTest_withIpv6Prefix_withMask() {
        // input ip: 1111:2222:3333:4444:5555:6666:7777:8888, mask: 66
        final Eid result = MaskUtil.normalize(EID_IPV6_PREFIX, (short) 66);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.Ipv6Prefix resultPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv6Prefix) result.getAddress();

        // expected result
        final String expectedResult = "1111:2222:3333:4444:4000::" + MASK_3;
        assertEquals(expectedResult, resultPrefix.getIpv6Prefix().getValue());
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid, short mask)} method with InstanceId.
     */
    @Test
    public void normalizeTest_withInstanceId_withMask() {
        //TODO finish this test when implementation is done
        assertEquals(EID_INSTANCE_ID, MaskUtil.normalize(EID_INSTANCE_ID, (short) 0));
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid, short mask)} method with Mac.
     */
    @Test
    public void normalizeTest_withMac_withMask() {
        assertEquals(EID_MAC, MaskUtil.normalize(EID_MAC, (short) 0));
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid)} method with Ipv4PrefixBinary.
     */
    @Test
    public void normalizeTest_withIpv4PrefixBinary() throws UnknownHostException {
        // input ip: 255.255.255.255, mask: 20
        final Eid result = MaskUtil.normalize(EID_IPV4_PREFIX_BINARY);
        final Ipv4PrefixBinary resultPrefix = (Ipv4PrefixBinary) result.getAddress();

        // expected result
        final byte[] expectedResult = Inet4Address.getByName("255.255.240.0").getAddress();
        assertArrayEquals(expectedResult, resultPrefix.getIpv4AddressBinary().getValue());
        assertEquals((short) 20, (short) resultPrefix.getIpv4MaskLength().toJava());
    }

    /**
     * Tests {@link MaskUtil#normalize(Eid eid)} method with Ipv6PrefixBinary.
     */
    @Test
    public void normalizeTest_withIpv6PrefixBinary() throws UnknownHostException {
        // input ip: 1111:2222:3333:4444:5555:6666:7777:8888, mask: 66
        final Eid result = MaskUtil.normalize(EID_IPV6_PREFIX_BINARY);
        final Ipv6PrefixBinary resultPrefix = (Ipv6PrefixBinary) result.getAddress();

        // expected result
        final byte[] expectedResult = Inet6Address.getByName("1111:2222:3333:4444:4000::").getAddress();
        assertArrayEquals(expectedResult, resultPrefix.getIpv6AddressBinary().getValue());
        assertEquals((short) 66, (short) resultPrefix.getIpv6MaskLength().toJava());
    }

    /**
     * Tests {@link MaskUtil#normalize} method with Ipv4Prefix.
     */
    @Test
    public void normalizeTest_withIpv4Prefix() {
        // input ip: 255.255.255.255, mask: 20
        final Eid result = MaskUtil.normalize(EID_IPV4_PREFIX);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.Ipv4Prefix resultPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv4Prefix) result.getAddress();

        // expected result
        final String expectedResult = "255.255.240.0" + MASK_2;
        assertEquals(expectedResult, resultPrefix.getIpv4Prefix().getValue());
    }

    /**
     * Tests {@link MaskUtil#normalize} method with Ipv6Prefix.
     */
    @Test
    public void normalizeTest_withIpv6Prefix() {
        // input ip: 1111:2222:3333:4444:5555:6666:7777:8888, mask: 66
        final Eid result = MaskUtil.normalize(EID_IPV6_PREFIX);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.Ipv6Prefix resultPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv6Prefix) result.getAddress();

        // expected result
        final String expectedResult = "1111:2222:3333:4444:4000::" + MASK_3;
        assertEquals(expectedResult, resultPrefix.getIpv6Prefix().getValue());
    }

    /**
     * Tests {@link MaskUtil#normalize} method with Ipv4.
     */
    @Test
    public void normalizeTest_withIpv4() {
        // input ip: 1.2.3.0
        final Eid result = MaskUtil.normalize(EID_IPV4);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.Ipv4Prefix resultPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv4Prefix) result.getAddress();

        // expected result
        final String expectedResult = "1.2.3.0" + MASK_1;
        assertEquals(expectedResult, resultPrefix.getIpv4Prefix().getValue());
    }

    /**
     * Tests {@link MaskUtil#normalize} method with Ipv6.
     */
    @Test
    public void normalizeTest_withIpv6() {
        // input ip: 1111:2222:3333:4444:5555:6666:7777:8888
        final Eid result = MaskUtil.normalize(EID_IPV6);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.Ipv6Prefix resultPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv6Prefix) result.getAddress();

        // expected result
        final String expectedResult = "1111:2222:3333:4444:5555:6666:7777:8888" + MASK_4;
        assertEquals(expectedResult, resultPrefix.getIpv6Prefix().getValue());
    }

    /**
     * Tests {@link MaskUtil#normalize} method with InstanceId.
     */
    @Test
    public void normalizeTest_withInstanceId() {
        //TODO finish this test when implementation is done
        assertEquals(EID_INSTANCE_ID, MaskUtil.normalize(EID_INSTANCE_ID));
    }

    /**
     * Tests {@link MaskUtil#normalize} method with SourceDestKey.
     */
    @Test
    public void normalizeTest_withSourceDestKey() {
        //expected result
        final String expectedSourceAddress = "1.2.0.0" + MASK_2;
        final String expectedDestAddress = "1.3.4.0";

        final Eid resultEid = MaskUtil.normalize(EID_SOURCE_DEST_KEY);
        final SourceDestKey resultAddr = (SourceDestKey) resultEid.getAddress();
        final String resultSource = resultAddr.getSourceDestKey().getSource().getIpPrefix().getIpv4Prefix().getValue();
        final String resultDest = resultAddr.getSourceDestKey().getDest().getIpAddress().getIpv4Address().getValue();

        assertEquals(expectedSourceAddress, resultSource);
        assertEquals(expectedDestAddress, resultDest);
    }

    /**
     * Tests {@link MaskUtil#normalize} method with ServicePath.
     */
    @Test
    public void normalizeTest_withServicePath() {
        // expected result
        final Eid expectedResult = LispAddressUtil.asServicePathEid(-1, 1L, (short)0);

        // result
        final Eid result = MaskUtil.normalize(EID_SERVICE_PATH);
        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link MaskUtil#normalize} method with non-maskable address type.
     */
    @Test
    public void normalizeTest_withNonMaskableAddress() {
        assertEquals(EID_MAC, MaskUtil.normalize(EID_MAC));
    }

    /**
     * Tests {@link MaskUtil#getMaxMask} method.
     */
    @Test
    public void getMaxMaskTest() {
        assertEquals(32, MaskUtil.getMaxMask(ADDRESS_IPV4));
        assertEquals(32, MaskUtil.getMaxMask(ADDRESS_IPV4_PREFIX));
        assertEquals(128, MaskUtil.getMaxMask(ADDRESS_IPV6));
        assertEquals(128, MaskUtil.getMaxMask(ADDRESS_IPV6_PREFIX));
        assertEquals(-1, MaskUtil.getMaxMask(ADDRESS_MAC));
    }

    /**
     * Tests {@link MaskUtil#getMaskForAddress} method.
     */
    @Test
    public void getMaskForAddressTest() {
        // SimpleAddress
        assertEquals(-1, MaskUtil.getMaskForAddress(SIMPLE_ADDRESS_IPV4));
        assertEquals((short) 32, MaskUtil.getMaskForAddress(SIMPLE_ADDRESS_IPV4_PREFIX));
        assertEquals((short) 128, MaskUtil.getMaskForAddress(SIMPLE_ADDRESS_IPV6_PREFIX));

        // Address
        assertEquals((short) 32, MaskUtil.getMaskForAddress(ADDRESS_IPV4));
        assertEquals((short) 128, MaskUtil.getMaskForAddress(ADDRESS_IPV6));
        assertEquals((short) 32, MaskUtil.getMaskForAddress(ADDRESS_IPV4_BINARY));
        assertEquals((short) 128, MaskUtil.getMaskForAddress(ADDRESS_IPV6_BINARY));
        assertEquals((short) 32, MaskUtil.getMaskForAddress(ADDRESS_IPV4_PREFIX));
        assertEquals((short) 128, MaskUtil.getMaskForAddress(ADDRESS_IPV6_PREFIX));
        assertEquals((short) 32, MaskUtil.getMaskForAddress(ADDRESS_INSTANCE_ID_IP_PREFIX));
        assertEquals((short) 32, MaskUtil.getMaskForAddress(IPV4_PREFIX_BINARY));
        assertEquals((short) 128, MaskUtil.getMaskForAddress(IPV6_PREFIX_BINARY));
        assertEquals((short) -1, MaskUtil.getMaskForAddress(ADDRESS_MAC));
    }

    /**
     * Tests {@link MaskUtil#getAddressStringForIpPrefix} method.
     */
    @Test
    public void getAddressStringForIpPrefixTest() {
        assertEquals(IPV4_STRING_1, MaskUtil.getAddressStringForIpPrefix(new IpPrefix(IPV4_PREFIX)));
    }

    /**
     * Tests {@link MaskUtil#getAddressStringForIpv4Prefix} method.
     */
    @Test
    public void getAddressStringForIpv4PrefixTest() {
        assertEquals(IPV4_STRING_1, MaskUtil
                .getAddressStringForIpv4Prefix(new Ipv4PrefixBuilder().setIpv4Prefix(IPV4_PREFIX).build()));
    }

    /**
     * Tests {@link MaskUtil#getAddressStringForIpv6Prefix} method.
     */
    @Test
    public void getAddressStringForIpv6PrefixTest() {
        assertEquals(IPV6_STRING, MaskUtil
                .getAddressStringForIpv6Prefix(new Ipv6PrefixBuilder().setIpv6Prefix(IPV6_PREFIX).build()));
    }

    private static SourceDestKeyBuilder getDefaultSourceDestKeyAddressBuilder() {
        return new SourceDestKeyBuilder()
                .setSourceDestKey(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                        .address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder()
                        .setSource(new SimpleAddress(new IpPrefix(new Ipv4Prefix(IPV4_STRING_1 + MASK_2))))
                        .setDest(new SimpleAddress(new IpAddress(new Ipv4Address(IPV4_STRING_2)))).build());
    }

    private static InstanceId getDefaultIpPrefixInstanceId() {
        return new InstanceIdBuilder()
                .setInstanceId(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                        .rev151105.lisp.address.address.instance.id.InstanceIdBuilder()
                        .setIid(new InstanceIdType(fromJava(1L)))
                        .setAddress(new SimpleAddress(new IpPrefix(IPV4_PREFIX)))
                        .setMaskLength(fromJava((short) 32)).build())
                .build();
    }

    private static InstanceId getDefaultIpInstanceId() {
        return new InstanceIdBuilder()
                .setInstanceId(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                        .rev151105.lisp.address.address.instance.id.InstanceIdBuilder()
                        .setIid(new InstanceIdType(fromJava(1L)))
                        .setAddress(new SimpleAddress(new IpAddress(new Ipv4Address(IPV4_STRING_1)))).build())
                .build();
    }

    private static ServicePath getDefaultServicePath() {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
                .service.path.ServicePathBuilder servicePathBuilder = new org.opendaylight.yang.gen.v1.urn.ietf.params
                .xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePathBuilder();

        return new ServicePathBuilder().setServicePath(servicePathBuilder
                        .setServiceIndex(fromJava((short) 1))
                        .setServicePathId(new ServicePathIdType(fromJava(1L))).build())
                .build();
    }
}
