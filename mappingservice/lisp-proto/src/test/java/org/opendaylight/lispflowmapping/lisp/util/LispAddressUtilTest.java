/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AsNumberAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

public class LispAddressUtilTest {


    private static final char[] DUMMY_CHARACTER_ARRAY_TEST = new char[]{'a', 'b', 'c', 'd'};
    private static final String MAC_ADDRESS_TEST = "aa:bb:cc:dd:ee:ff";
    private static final String IPV4_ADDRESS_PREFIX_TEST = "192.168.1.2/30";
    private static final String IPV6_ADDRESS_PREFIX_TEST = "102:304:506:708:90a:b0c:d0e:f11/30";
    private static final Long NUMBER_TEST = 5L;

    private static final byte[] IPV4_ADDRESS_BYTES_TEST = new byte[]{(byte) 192, (byte) 168, 1, 1};
    private static final String IPV4_ADDRESS_TEST = "192.168.1.1";

    private static final byte[] IPV6_ADDRESS_BYTES_TEST = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    private static final String IPV6_ADDRESS_TEST = "102:304:506:708:90a:b0c:d0e:f10";


    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link LispAddressUtil#addressFromSimpleAddress} methods
     * with ipAddress
     */
    @Test
    public void addressFromSimpleAddressTest_asAnyIpAddress() {
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
    public void addressFromSimpleAddressTest_asIpPrefix() {
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
    public void addressFromSimpleAddressTest_asMacAddress() {
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
    public void addressFromSimpleAddressTest_asAddress() {
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
    public void addressFromSimpleAddressTest_asNumber() {
        final SimpleAddress simpleAddress = new SimpleAddress(new AsNumber(NUMBER_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress(simpleAddress);
        assertEquals(AsNumberAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AsNumber);
    }

    /**
     * Tests {@link LispAddressUtil#addressFromInet(InetAddress)} and {@link LispAddressUtil#addressTypeFromInet(InetAddress)}
     * methods with ipv4 address
     *
     * @throws UnknownHostException
     */
    @Test
    public void addressFromInet_ipv4() throws UnknownHostException {
        final InetAddress ipv4InetAddress = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromInet(ipv4InetAddress);
        assertEquals(Ipv4Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromInet(ipv4InetAddress);
        assertTrue(address instanceof Ipv4);
        assertEquals(IPV4_ADDRESS_TEST, ((Ipv4) address).getIpv4().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromInet(InetAddress)} and {@link LispAddressUtil#addressTypeFromInet(InetAddress)}
     * methods with ipv6 address
     *
     * @throws UnknownHostException
     */
    @Test
    public void addressFromInet_ipv6() throws UnknownHostException {
        final InetAddress ipv6InetAddress = Inet6Address.getByAddress(IPV6_ADDRESS_BYTES_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromInet(ipv6InetAddress);
        assertEquals(Ipv6Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromInet(ipv6InetAddress);
        assertTrue(address instanceof Ipv6);
        assertEquals(IPV6_ADDRESS_TEST, ((Ipv6) address).getIpv6().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpAddress(IpAddress)} and {@link LispAddressUtil#addressTypeFromIpAddress(IpAddress)}
     * methods with ipv4 address
     */
    @Test
    public void addressFromIpAddress_ipv4() {
        final IpAddress ipv4Address = new IpAddress(new Ipv4Address(IPV4_ADDRESS_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpAddress(ipv4Address);
        assertEquals(Ipv4Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpAddress(ipv4Address);
        assertTrue(address instanceof Ipv4);
        assertEquals(IPV4_ADDRESS_TEST, ((Ipv4) address).getIpv4().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpAddress(IpAddress)} and {@link LispAddressUtil#addressTypeFromIpAddress(IpAddress)}
     * methods with ipv6 address
     */
    @Test
    public void addressFromIpAddress_ipv6() {
        final IpAddress ipv6Address = new IpAddress(new Ipv6Address(IPV6_ADDRESS_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpAddress(ipv6Address);
        assertEquals(Ipv6Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpAddress(ipv6Address);
        assertTrue(address instanceof Ipv6);
        assertEquals(IPV6_ADDRESS_TEST, ((Ipv6) address).getIpv6().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpAddress(IpAddress)} and {@link LispAddressUtil#addressTypeFromIpAddress(IpAddress)}
     * methods with null value instead off address.
     */
    @Test
    public void addressFromIpAddress_null() {
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpAddress(null);
        assertNull(addressClass);

        final Address address = LispAddressUtil.addressFromIpAddress(null);
        assertNull(address);
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpPrefix(IpPrefix)} and {@link LispAddressUtil#addressTypeFromIpPrefix(IpPrefix)}
     * methods with ipv4 address
     */
    @Test
    public void addressFromIpPrefix_ipv4() {
        IpPrefix ipv4Prefix = new IpPrefix(new Ipv4Prefix(IPV4_ADDRESS_PREFIX_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpPrefix(ipv4Prefix);
        assertEquals(Ipv4PrefixAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpPrefix(ipv4Prefix);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix);
        assertEquals(IPV4_ADDRESS_PREFIX_TEST, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix) address).getIpv4Prefix().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpPrefix(IpPrefix)} and {@link LispAddressUtil#addressTypeFromIpPrefix(IpPrefix)}
     * methods with ipv6 address
     */
    @Test
    public void addressFromIpPrefix_ipv6() {
        IpPrefix ipv6Address = new IpPrefix(new Ipv6Prefix(IPV6_ADDRESS_PREFIX_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpPrefix(ipv6Address);
        assertEquals(Ipv6PrefixAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpPrefix(ipv6Address);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix);
        assertEquals(IPV6_ADDRESS_PREFIX_TEST,
                ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) address).getIpv6Prefix().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpPrefix(IpPrefix)} and {@link LispAddressUtil#addressTypeFromIpPrefix(IpPrefix)}
     * methods with null value instead off address.
     */
    @Test
    public void addressFromIpPrefix_null() {
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpPrefix(null);
        assertNull(addressClass);

        final Address address = LispAddressUtil.addressFromIpPrefix(null);
        assertNull(address);
    }

    /**
     * Tests {@link LispAddressUtil#addressFromMacAddress(MacAddress)} methods with mac address.
     */
    @Test
    public void addressFromMacAddress_mac() {
        final Address address = LispAddressUtil.addressFromMacAddress(new MacAddress(MAC_ADDRESS_TEST));
        assertTrue(address instanceof Mac);
        assertEquals(MAC_ADDRESS_TEST, ((Mac) address).getMac().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromMacAddress(MacAddress)} methods with null instead of address.
     */
    @Test
    public void addressFromMacAddress_null() {
        final Address address = LispAddressUtil.addressFromMacAddress(null);
        assertNull(address);
    }


}
