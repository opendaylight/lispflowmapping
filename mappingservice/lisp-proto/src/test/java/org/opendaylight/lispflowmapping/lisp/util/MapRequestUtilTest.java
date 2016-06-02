/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address
        .address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address
        .address.Ipv6BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class MapRequestUtilTest {

    private static final String IPV4_STRING = "192.168.0.1";
    private static final String IPV6_STRING = "1111:2222:3333:4444:5555:6666:7777:8888";
    private static final String MASK = "/24";
    private static final byte[] IPV4_BYTES = new byte[]{-64, -88, 0, 1};
    private static final byte[] IPV6_BYTES =
            new byte[]{17, 17, 34, 34, 51, 51, 68, 68, 85, 85, 102, 102, 119, 119, -120, -120};

    private static final Address IPV4_ADDRESS = new Ipv4Builder().setIpv4(new Ipv4Address(IPV4_STRING)).build();
    private static final Address IPV6_ADDRESS = new Ipv6Builder().setIpv6(new Ipv6Address(IPV6_STRING)).build();
    private static final Address IPV4_ADDRESS_BINARY = new Ipv4BinaryBuilder()
            .setIpv4Binary(new Ipv4AddressBinary(IPV4_BYTES)).build();
    private static final Address IPV6_ADDRESS_BINARY = new Ipv6BinaryBuilder()
            .setIpv6Binary(new Ipv6AddressBinary(IPV6_BYTES)).build();
    private static final Address IPV4_ADDRESS_PREFIX = new Ipv4PrefixBuilder()
            .setIpv4Prefix(new Ipv4Prefix(IPV4_STRING + MASK)).build();

    /**
     * Tests {@link MapRequestUtil#selectItrRloc} method with Ipv4.
     */
    @Test
    public void selectItrRlocTest_Ipv4() {
        final ItrRlocBuilder itrRloc = new ItrRlocBuilder().setRloc(new RlocBuilder().setAddress(IPV4_ADDRESS).build());
        final MapRequest request = new MapRequestBuilder().setItrRloc(Lists.newArrayList(itrRloc.build())).build();

        // expected result
        InetAddress expectedResult = InetAddresses.forString(IPV4_STRING);

        // result
        InetAddress result = MapRequestUtil.selectItrRloc(request);
        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link MapRequestUtil#selectItrRloc} method with Ipv6.
     */
    @Test
    public void selectItrRlocTest_Ipv6() {
        final ItrRlocBuilder itrRloc = new ItrRlocBuilder().setRloc(new RlocBuilder().setAddress(IPV6_ADDRESS).build());
        final MapRequest request = new MapRequestBuilder().setItrRloc(Lists.newArrayList(itrRloc.build())).build();

        // expected result
        InetAddress expectedResult = InetAddresses.forString(IPV6_STRING);

        // result
        InetAddress result = MapRequestUtil.selectItrRloc(request);
        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link MapRequestUtil#selectItrRloc} method with Ipv4Binary.
     */
    @Test
    public void selectItrRlocTest_Ipv4Binary() {
        final ItrRlocBuilder itrRloc = new ItrRlocBuilder()
                .setRloc(new RlocBuilder().setAddress(IPV4_ADDRESS_BINARY).build());
        final MapRequest request = new MapRequestBuilder().setItrRloc(Lists.newArrayList(itrRloc.build())).build();

        // expected result
        InetAddress expectedResult = InetAddresses.forString(IPV4_STRING);

        // result
        InetAddress result = MapRequestUtil.selectItrRloc(request);
        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link MapRequestUtil#selectItrRloc} method with Ipv6Binary.
     */
    @Test
    public void selectItrRlocTest_Ipv6Binary() {
        final ItrRlocBuilder itrRloc = new ItrRlocBuilder()
                .setRloc(new RlocBuilder().setAddress(IPV6_ADDRESS_BINARY).build());
        final MapRequest request = new MapRequestBuilder().setItrRloc(Lists.newArrayList(itrRloc.build())).build();

        // expected result
        InetAddress expectedResult = InetAddresses.forString(IPV6_STRING);

        // result
        InetAddress result = MapRequestUtil.selectItrRloc(request);
        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link MapRequestUtil#selectItrRloc} method with Ipv4Prefix.
     */
    @Test
    public void selectItrRlocTest_Ipv4Prefix() {
        final ItrRlocBuilder itrRloc = new ItrRlocBuilder()
                .setRloc(new RlocBuilder().setAddress(IPV4_ADDRESS_PREFIX).build());
        final MapRequest request = new MapRequestBuilder().setItrRloc(Lists.newArrayList(itrRloc.build())).build();

        assertNull(MapRequestUtil.selectItrRloc(request));
    }

    /**
     * Tests {@link MapRequestUtil#selectItrRloc} method with no ItrRlocs.
     */
    @Test
    public void selectItrRlocTest_noItrRlocs() {
        final MapRequest request = new MapRequestBuilder().build();
        assertNull(MapRequestUtil.selectItrRloc(request));
    }
}
