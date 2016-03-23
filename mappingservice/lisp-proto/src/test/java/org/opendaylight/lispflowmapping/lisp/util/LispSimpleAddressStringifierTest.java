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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

import org.mockito.Mockito;

public class LispSimpleAddressStringifierTest {

    private static final String IPV4_ADDRESS_STRING = "127.0.0.1";
    private static final Ipv4Address IPV4_ADDRESS = new Ipv4Address(IPV4_ADDRESS_STRING);
    private static final IpAddress IP_ADDRESS_USING_IPV4 = new IpAddress(IPV4_ADDRESS);

    private static final String IPV6_ADDRESS_STRING = "1111:2222:3333:4444:5555:6666:7777:8888";
    private static final Ipv6Address IPV6_ADDRESS = new Ipv6Address(IPV6_ADDRESS_STRING);
    private static final IpAddress IP_ADDRESS_USING_IPV6 = new IpAddress(IPV6_ADDRESS);

    private static final String MAC_ADDRESS_STRING = "aa:bb:cc:dd:ee:ff";
    private static final MacAddress MAC_ADDRESS = new MacAddress(MAC_ADDRESS_STRING);

    private static final IpPrefix IPV4_PREFIX = new IpPrefix(new Ipv4Prefix(IPV4_ADDRESS_STRING + "/24"));
    private static final IpPrefix IPV6_PREFIX = new IpPrefix(new Ipv6Prefix(IPV6_ADDRESS_STRING + "/48"));

    private static final DistinguishedNameType DISTINGUISHED_NAME = new DistinguishedNameType("distinguished_name_type");

    private static final AsNumber AS_NUMBER = new AsNumber(1111L);

    /**
     * Tests {@link LispSimpleAddressStringifier#getString} with Ipv4Address.
     */
    @Test
    public void getStringTest_asIpv4() {
        SimpleAddress simpleAddress = new SimpleAddress(IP_ADDRESS_USING_IPV4);
        String result = LispSimpleAddressStringifier.getString(simpleAddress);

        assertEquals(IPV4_ADDRESS_STRING, result);
    }

    /**
     * Tests {@link LispSimpleAddressStringifier#getString} with Ipv6Address.
     */
    @Test
    public void getStringTest_asIpv6() {
        SimpleAddress simpleAddress = new SimpleAddress(IP_ADDRESS_USING_IPV6);
        String result = LispSimpleAddressStringifier.getString(simpleAddress);

        assertEquals(IPV6_ADDRESS_STRING, result);
    }

    /**
     * Tests {@link LispSimpleAddressStringifier#getString} with Ipv4Prefix.
     */
    @Test
    public void getStringTest_asIpv4Prefix() {
        SimpleAddress simpleAddress = new SimpleAddress(IPV4_PREFIX);
        String result = LispSimpleAddressStringifier.getString(simpleAddress);

        assertEquals(IPV4_PREFIX.getIpv4Prefix().getValue(), result);
    }

    /**
     * Tests {@link LispSimpleAddressStringifier#getString} with Ipv6Prefix.
     */
    @Test
    public void getStringTest_asIpv6Prefix() {
        SimpleAddress simpleAddress = new SimpleAddress(IPV6_PREFIX);
        String result = LispSimpleAddressStringifier.getString(simpleAddress);

        assertEquals(IPV6_PREFIX.getIpv6Prefix().getValue(), result);
    }

    /**
     * Tests {@link LispSimpleAddressStringifier#getString} with MacAddress.
     */
    @Test
    public void getStringTest_asMac() {
        SimpleAddress simpleAddress = new SimpleAddress(MAC_ADDRESS);
        String result = LispSimpleAddressStringifier.getString(simpleAddress);

        assertEquals(MAC_ADDRESS_STRING, result);
    }

    /**
     * Tests {@link LispSimpleAddressStringifier#getString} with DistinguishedName.
     */
    @Test
    public void getStringTest_asDistinguishedName() {
        DistinguishedName distinguishedNameMock = Mockito.mock(DistinguishedName.class);
        Mockito.when(distinguishedNameMock.getDistinguishedName()).thenReturn(DISTINGUISHED_NAME);
        SimpleAddress simpleAddress = new SimpleAddress(distinguishedNameMock.getDistinguishedName());

        assertEquals(DISTINGUISHED_NAME.getValue(), LispSimpleAddressStringifier.getString(simpleAddress));
    }

    /**
     * Tests {@link LispSimpleAddressStringifier#getString} with AsNumber.
     */
    @Test
    public void getStringTest_asAsNumber() {
        SimpleAddress simpleAddress = new SimpleAddress(AS_NUMBER);
        String result = LispSimpleAddressStringifier.getString(simpleAddress);

        assertEquals("AS" + AS_NUMBER.getValue(), result);
    }
}
