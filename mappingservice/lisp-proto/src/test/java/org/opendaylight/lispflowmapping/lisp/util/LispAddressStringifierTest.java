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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .InstanceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .InstanceIdBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Ipv6PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class LispAddressStringifierTest {

    private static final long VNI = 100L;
    private static final long IID = 200L;

    // Ipv4
    private static final String IPV4_STRING = "192.168.0.1";
    private static final Ipv4Address IPV4_ADDRESS = new Ipv4Address(IPV4_STRING);
    private static final Ipv4 IPV4 = new Ipv4Builder().setIpv4(IPV4_ADDRESS).build();
    private static final LispAddress LISP_IPV4 = new EidBuilder().setAddress(IPV4)
            .setVirtualNetworkId(new InstanceIdType(VNI)).build();

    // Ipv6
    private static final String IPV6_STRING = "1111:2222:3333:4444:5555:6666:7777:8888";
    private static final Ipv6Address IPV6_ADDRESS = new Ipv6Address(IPV6_STRING);
    private static final Ipv6 IPV6 = new Ipv6Builder().setIpv6(new Ipv6Address(IPV6_STRING)).build();
    private static final LispAddress LISP_IPV6 = new EidBuilder().setAddress(IPV6).build();

    // Ipv4Prefix
    private static final String PREFIX = "/24";
    private static final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.Ipv4Prefix IPV4_PREFIX = new Ipv4PrefixBuilder()
            .setIpv4Prefix(new Ipv4Prefix(IPV4_STRING + PREFIX)).build();
    private static final LispAddress LISP_IPV4_PREFIX = new EidBuilder().setAddress(IPV4_PREFIX).build();

    // Ipv6Prefix
    private static final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.Ipv6Prefix IPV6_PREFIX = new Ipv6PrefixBuilder()
            .setIpv6Prefix(new Ipv6Prefix(IPV6_STRING + PREFIX)).build();
    private static final LispAddress LISP_IPV6_PREFIX = new EidBuilder().setAddress(IPV6_PREFIX).build();

    //Mac
    private static final Mac MAC = new MacBuilder().setMac(new MacAddress("01:23:45:67:89:ab")).build();
    private static final LispAddress LISP_MAC = new EidBuilder().setAddress(MAC).build();

    // InstanceId with Ipv4
    private static final InstanceIdType INSTANCE_ID_TYPE = new InstanceIdType(IID);
    private static final InstanceId INSTANCE_ID_IPV4 = new InstanceIdBuilder().setInstanceId(new org.opendaylight.yang
            .gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.instance.id
            .InstanceIdBuilder().setAddress(new SimpleAddress(new IpAddress(IPV4_ADDRESS)))
            .setIid(INSTANCE_ID_TYPE).build()).build();
    private static final LispAddress LISP_IID_IPV4 = new EidBuilder().setAddress(INSTANCE_ID_IPV4).build();

    // InstanceId with Ipv6
    private static final InstanceId INSTANCE_ID_IPV6 = new InstanceIdBuilder().setInstanceId(new org.opendaylight.yang
            .gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.instance.id
            .InstanceIdBuilder().setAddress(new SimpleAddress(new IpAddress(IPV6_ADDRESS)))
            .setIid(INSTANCE_ID_TYPE).build()).build();
    private static final LispAddress LISP_IID_IPV6 = new EidBuilder().setAddress(INSTANCE_ID_IPV6).build();



    /**
     * Tests {@link LispAddressStringifier#getString} with Ipv4 address type.
     */
    @Test
    public void getStringTest_withIpv4() {
        assertEquals("[" + VNI + "] " + IPV4_STRING, LispAddressStringifier.getString(LISP_IPV4));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with Ipv4Prefix address type.
     */
    @Test
    public void getStringTest_withIpv4Prefix() {
        assertEquals(IPV4_STRING + PREFIX, LispAddressStringifier.getString(LISP_IPV4_PREFIX));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with Ipv6 address type.
     */
    @Test
    public void getStringTest_withIpv6() {
        assertEquals(IPV6_STRING, LispAddressStringifier.getString(LISP_IPV6));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with Ipv6Prefix address type.
     */
    @Test
    public void getStringTest_withIpv6Prefix() {
        assertEquals(IPV6_STRING + PREFIX, LispAddressStringifier.getString(LISP_IPV6_PREFIX));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with Mac address type.
     */
    @Test
    public void getStringTest_withMac() {
        assertEquals(MAC.getMac().getValue(), LispAddressStringifier.getString(LISP_MAC));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with InstanceId address type.
     */
    @Test
    public void getStringTest_withInstanceId() {
        // with Ipv4
        assertEquals("[" + IID + "] " + IPV4_STRING, LispAddressStringifier.getString(LISP_IID_IPV4));

        // with Ipv6
        assertEquals("[" + IID + "] " + IPV6_STRING, LispAddressStringifier.getString(LISP_IID_IPV6));
    }
}
