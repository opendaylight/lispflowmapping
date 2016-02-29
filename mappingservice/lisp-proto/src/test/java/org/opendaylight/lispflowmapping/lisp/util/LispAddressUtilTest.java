/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AsNumberAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

public class LispAddressUtilTest {


    private static final String IPV4_ADDRESS_TEST = "192.168.1.1";
    private static final char[] DUMMY_CHARACTER_ARRAY_TEST = new char[]{'a', 'b', 'c', 'd'};
    private static final String MAC_ADDRESS_TEST = "aa:bb:cc:dd:ee:ff";
    private static final String IPV4_ADDRESS_PREFIX_TEST = "192.168.1.2/30";
    private static final Long NUMBER_TEST = 5L;
    private static final byte[] IPV4_ADDRESS_BYTES_TEST = new byte[] {(byte) 192, (byte) 168, 1, 2};
    private static final byte[] IPV6_ADDRESS_BYTES_TEST = new byte[] {1, 2, 3, 4, 5, 6 ,7, 8, 9, 10, 11, 12, 13, 14, 15, 16};


    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link LispAddressUtil#addressFromSimpleAddress} methods
     * with ipAddress
     */
    @Test
    public void addressTypeFromSimpleAddressTest_asAnyIpAddress() {
        final SimpleAddress simpleAddress = new SimpleAddress(new IpAddress(new Ipv4Address(IPV4_ADDRESS_TEST)));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress(simpleAddress);
        assertEquals(Ipv4Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof Ipv4);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link LispAddressUtil#addressFromSimpleAddress} methods
     * with ipPrefix
     */
    @Test
    public void addressTypeFromSimpleAddressTest_asIpPrefix() {
        final SimpleAddress simpleAddress = new SimpleAddress(new IpPrefix(new Ipv4Prefix(IPV4_ADDRESS_PREFIX_TEST)));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress(simpleAddress);
        assertEquals(Ipv4PrefixAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link LispAddressUtil#addressFromSimpleAddress} methods
     * with mac address
     */
    @Test
    public void addressTypeFromSimpleAddressTest_asMacAddress() {
        final SimpleAddress simpleAddress = new SimpleAddress(new MacAddress(MAC_ADDRESS_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress(simpleAddress);
        assertEquals(MacAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof Mac);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link LispAddressUtil#addressFromSimpleAddress} methods
     * with general address
     */
    @Test
    public void addressTypeFromSimpleAddressTest_asAddress() {
        final SimpleAddress simpleAddress = new SimpleAddress(DUMMY_CHARACTER_ARRAY_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress(simpleAddress);
        assertEquals(DistinguishedNameAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof DistinguishedName);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link LispAddressUtil#addressFromSimpleAddress} methods
     * with address as number
     */
    @Test
    public void addressTypeFromSimpleAddressTest_asNumber() {
        final SimpleAddress simpleAddress = new SimpleAddress(new AsNumber(NUMBER_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress(simpleAddress);
        assertEquals(AsNumberAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AsNumber);
    }

    @Test
    public void addressTypeFromInet_ipv4() throws UnknownHostException {
        final InetAddress address = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromInet(address);
        assertEquals(Ipv4Afi.class, addressClass);
    }

    @Test
    public void addressTypeFromInet_ipv6() throws UnknownHostException {
        final InetAddress address = Inet6Address.getByAddress(IPV6_ADDRESS_BYTES_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromInet(address);
        assertEquals(Ipv6Afi.class, addressClass);

    }
}
