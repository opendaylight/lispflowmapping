/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.KeyValueAddressLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.NoAddressAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.NoAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class LispAddressUtilTest {


    private static final char[] DUMMY_CHARACTER_ARRAY_TEST = new char[]{'a', 'b', 'c', 'd'};
    private static final Long NUMBER_TEST = 5L;

    private static final String MAC_ADDRESS_VALUE_TEST = "aa:bb:cc:dd:ee:ff";
    private static final MacAddress MAC_ADDRESS_TEST = new MacAddress("aa:bb:cc:dd:ee:ff");

    private static final byte[] IPV4_ADDRESS_BYTES_A_TEST = new byte[]{(byte) 192, (byte) 168, 1, 1};
    private static final byte[] IPV4_ADDRESS_BYTES_B_TEST = new byte[]{(byte) 192, (byte) 168, 1, 2};

    private static final short MASK_OK_TEST = 30;
    private static final short MASK_OK_DEFAULT_IPV4_TEST = 32;
    private static final short MASK_OK_DEFAULT_IPV6_TEST = 128;

    private static final String IPV4_ADDRESS_VALUE_TEST = "192.168.1.1";
    private static final String IPV4_ADDRESS_PREFIX_VALUE_TEST = IPV4_ADDRESS_VALUE_TEST + "/" + MASK_OK_TEST;
    private static final Ipv4Address IPV4_ADDRESS_TEST = new Ipv4Address(IPV4_ADDRESS_VALUE_TEST);
    private static final IpAddress IP_ADDRESS_OBJECT_WITH_IPV4_TEST = new IpAddress(IPV4_ADDRESS_TEST);
    private static final Ipv4Prefix IPV4_ADDRESS_PREFIX_TEST = new Ipv4Prefix(IPV4_ADDRESS_PREFIX_VALUE_TEST);
    private static final IpPrefix IP_ADDRESS_PREFIX_WITH_IPV4_TEST = new IpPrefix(IPV4_ADDRESS_PREFIX_TEST);


    private static final byte[] IPV6_ADDRESS_BYTES_A_TEST = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 16};
    private static final byte[] IPV6_ADDRESS_BYTES_B_TEST = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 17};
    private static final String IPV6_ADDRESS_VALUE_TEST = "102:304:506:708:90a:b0c:d0e:f10";
    private static final String IPV6_ADDRESS_PREFIX_VALUE_TEST = IPV6_ADDRESS_VALUE_TEST + "/" + MASK_OK_TEST;
    private static final Ipv6Prefix IPV6_ADDRESS_PREFIX_TEST = new Ipv6Prefix(IPV6_ADDRESS_PREFIX_VALUE_TEST);
    private static final Ipv6Address IPV6_ADDRESS_TEST = new Ipv6Address(IPV6_ADDRESS_VALUE_TEST);
    private static final IpPrefix IP_ADDRESS_PREFIX_WITH_IPV6_TEST = new IpPrefix(IPV6_ADDRESS_PREFIX_TEST);
    private static final IpAddress IP_ADDRESS_OBJECT_WITH_IPV6_TEST = new IpAddress(IPV6_ADDRESS_TEST);

    private static final Short SERVICE_INDEX_TEST = 45;
    private static final Long DUMMY_SERVICE_PATH_ID_TYPE = 46L;
    private static final String DISTINGUISHED_NAME_TYPE_VALUE_TEST = "dummy distinguished name type";
    private static final String DISTINGUISHED_NAME_TYPE_VALUE_WITH_MAC_TEST = MAC_ADDRESS_VALUE_TEST;
    private static final DistinguishedNameType DISTINGUISHED_NAME_TYPE_TEST = new DistinguishedNameType
            (DISTINGUISHED_NAME_TYPE_VALUE_TEST);

    private static final Long AS_NUMBER_TEST = 100L;
    private static final Long INSTANCE_ID_TYPE_VALUE_TEST = 121L;
    private static final Short INSTANCE_ID_TYPE_VALUE_SHORT_TEST = 122;
    private static final InstanceIdType INSTANCE_ID_TYPE_TEST = new InstanceIdType(INSTANCE_ID_TYPE_VALUE_TEST);
    private static final String INCORRECT_IP_ADDRESS_TEST = "incorrect ip address";

    private static final SimpleAddress SIMPLE_ADDRESS_A_TEST = new SimpleAddress(IP_ADDRESS_OBJECT_WITH_IPV4_TEST);
    private static final SimpleAddress SIMPLE_ADDRESS_B_TEST = new SimpleAddress(MAC_ADDRESS_TEST);
    private static final String SIMPLE_ADDRESS_DISTINGUISHED_VALUE_TEST = DISTINGUISHED_NAME_TYPE_VALUE_TEST;
    private static final SimpleAddress SIMPLE_ADDRESS_DISTINGUISHED_TEST = new SimpleAddress(new DistinguishedNameType
            (SIMPLE_ADDRESS_DISTINGUISHED_VALUE_TEST));
    private static final SimpleAddress SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV4_TEST = new SimpleAddress
            (IP_ADDRESS_PREFIX_WITH_IPV4_TEST);
    private static final SimpleAddress SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV6_TEST = new SimpleAddress
            (IP_ADDRESS_PREFIX_WITH_IPV6_TEST);
    private static final Long SERVICE_PATH_ID_TEST = 2121L;

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link
     * LispAddressUtil#addressFromSimpleAddress} methods
     * with ipAddress
     */
    @Test
    public void addressFromSimpleAddressTest_asAnyIpAddress() {
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress
                (SIMPLE_ADDRESS_A_TEST);
        assertEquals(Ipv4Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(SIMPLE_ADDRESS_A_TEST);
        assertTrue(address instanceof Ipv4);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link
     * LispAddressUtil#addressFromSimpleAddress} methods
     * with ipPrefix
     */
    @Test
    public void addressFromSimpleAddressTest_asIpPrefix() {
        final SimpleAddress simpleAddress = new SimpleAddress(new IpPrefix(new Ipv4Prefix(IPV4_ADDRESS_PREFIX_VALUE_TEST)));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress
                (simpleAddress);
        assertEquals(Ipv4PrefixAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
                .types.rev151105.lisp.address.address.Ipv4Prefix);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link
     * LispAddressUtil#addressFromSimpleAddress} methods
     * with mac address
     */
    @Test
    public void addressFromSimpleAddressTest_asMacAddress() {
        final SimpleAddress simpleAddress = new SimpleAddress(new MacAddress(MAC_ADDRESS_VALUE_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress
                (simpleAddress);
        assertEquals(MacAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof Mac);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link
     * LispAddressUtil#addressFromSimpleAddress} methods
     * with general address
     */
    @Test
    public void addressFromSimpleAddressTest_asAddress() {
        final SimpleAddress simpleAddress = new SimpleAddress(DUMMY_CHARACTER_ARRAY_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress
                (simpleAddress);
        assertEquals(DistinguishedNameAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof DistinguishedName);
    }

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link
     * LispAddressUtil#addressFromSimpleAddress} methods
     * with address as number
     */
    @Test
    public void addressFromSimpleAddressTest_asNumber() {
        final SimpleAddress simpleAddress = new SimpleAddress(new AsNumber(NUMBER_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress
                (simpleAddress);
        assertEquals(AsNumberAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(simpleAddress);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
                .types.rev151105.lisp.address.address.AsNumber);
    }

    /**
     * Tests {@link LispAddressUtil#addressFromInet(InetAddress)} and {@link LispAddressUtil#addressTypeFromInet
     * (InetAddress)}
     * methods with ipv4 address
     *
     * @throws UnknownHostException
     */
    @Test
    public void addressFromInet_ipv4() throws UnknownHostException {
        final InetAddress ipv4InetAddress = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_A_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromInet(ipv4InetAddress);
        assertEquals(Ipv4Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromInet(ipv4InetAddress);
        assertTrue(address instanceof Ipv4);
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4) address).getIpv4().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromInet(InetAddress)} and {@link LispAddressUtil#addressTypeFromInet
     * (InetAddress)}
     * methods with ipv6 address
     *
     * @throws UnknownHostException
     */
    @Test
    public void addressFromInet_ipv6() throws UnknownHostException {
        final InetAddress ipv6InetAddress = Inet6Address.getByAddress(IPV6_ADDRESS_BYTES_A_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromInet(ipv6InetAddress);
        assertEquals(Ipv6Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromInet(ipv6InetAddress);
        assertTrue(address instanceof Ipv6);
        assertEquals(IPV6_ADDRESS_VALUE_TEST, ((Ipv6) address).getIpv6().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpAddress(IpAddress)} and {@link
     * LispAddressUtil#addressTypeFromIpAddress(IpAddress)}
     * methods with ipv4 address
     */
    @Test
    public void addressFromIpAddress_ipv4() {
        final IpAddress ipv4Address = new IpAddress(IPV4_ADDRESS_TEST);
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpAddress(ipv4Address);
        assertEquals(Ipv4Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpAddress(ipv4Address);
        assertTrue(address instanceof Ipv4);
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4) address).getIpv4().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpAddress(IpAddress)} and {@link
     * LispAddressUtil#addressTypeFromIpAddress(IpAddress)}
     * methods with ipv6 address
     */
    @Test
    public void addressFromIpAddress_ipv6() {
        final IpAddress ipv6Address = new IpAddress(new Ipv6Address(IPV6_ADDRESS_VALUE_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpAddress(ipv6Address);
        assertEquals(Ipv6Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpAddress(ipv6Address);
        assertTrue(address instanceof Ipv6);
        assertEquals(IPV6_ADDRESS_VALUE_TEST, ((Ipv6) address).getIpv6().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpAddress(IpAddress)} and {@link
     * LispAddressUtil#addressTypeFromIpAddress(IpAddress)}
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
     * Tests {@link LispAddressUtil#addressFromIpPrefix(IpPrefix)} and {@link
     * LispAddressUtil#addressTypeFromIpPrefix(IpPrefix)}
     * methods with ipv4 address
     */
    @Test
    public void addressFromIpPrefix_ipv4() {
        IpPrefix ipv4Prefix = new IpPrefix(new Ipv4Prefix(IPV4_ADDRESS_PREFIX_VALUE_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpPrefix(ipv4Prefix);
        assertEquals(Ipv4PrefixAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpPrefix(ipv4Prefix);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.
                types.rev151105.lisp.address.address.Ipv4Prefix);
        assertEquals(IPV4_ADDRESS_PREFIX_VALUE_TEST, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.
                address.types.rev151105.lisp.address.address.Ipv4Prefix) address).getIpv4Prefix().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpPrefix(IpPrefix)} and {@link
     * LispAddressUtil#addressTypeFromIpPrefix(IpPrefix)}
     * methods with ipv6 address
     */
    @Test
    public void addressFromIpPrefix_ipv6() {
        IpPrefix ipv6Address = new IpPrefix(new Ipv6Prefix(IPV6_ADDRESS_PREFIX_VALUE_TEST));
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromIpPrefix(ipv6Address);
        assertEquals(Ipv6PrefixAfi.class, addressClass);

        final Address address = LispAddressUtil.addressFromIpPrefix(ipv6Address);
        assertTrue(address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
                .types.rev151105.lisp.address.address.Ipv6Prefix);
        assertEquals(IPV6_ADDRESS_PREFIX_VALUE_TEST,
                ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp
                        .address.address.Ipv6Prefix) address).getIpv6Prefix().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromIpPrefix(IpPrefix)} and {@link LispAddressUtil#addressTypeFromIpPrefix
     * (IpPrefix)}
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
        final Address address = LispAddressUtil.addressFromMacAddress(new MacAddress(MAC_ADDRESS_VALUE_TEST));
        assertTrue(address instanceof Mac);
        assertEquals(MAC_ADDRESS_VALUE_TEST, ((Mac) address).getMac().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#addressFromMacAddress(MacAddress)} methods with null instead of address.
     */
    @Test
    public void addressFromMacAddress_null() {
        final Address address = LispAddressUtil.addressFromMacAddress(null);
        assertNull(address);
    }


    /**
     * Test {@link LispAddressUtil#addressFromServicePath(ServicePath)} method with concrete servicePath
     */
    @Test
    public void addressFromServicePathTest_withServicePath() {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.service.path.ServicePathBuilder servicePathBuilder = new org.opendaylight.yang.gen.v1.urn.ietf
                .params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.service.path.ServicePathBuilder();
        servicePathBuilder.setServiceIndex(SERVICE_INDEX_TEST);
        servicePathBuilder.setServicePathId(new ServicePathIdType(DUMMY_SERVICE_PATH_ID_TYPE));

        ServicePath expectedAddress = servicePathBuilder.build();
        final Address testedAddress = LispAddressUtil.addressFromServicePath(expectedAddress);
        assertTrue(testedAddress instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
                .types.rev151105.lisp.address.address.ServicePath);
        assertEquals(expectedAddress, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
                .types.rev151105.lisp.address.address.ServicePath) testedAddress).getServicePath());
    }

    /**
     * Test {@link LispAddressUtil#addressFromServicePath(ServicePath)} method with null value
     */
    @Test
    public void addressFromServicePathTest_withNull() {
        final Address testedAddress = LispAddressUtil.addressFromServicePath(null);
        assertNull(testedAddress);
    }

    /**
     * Test {@link LispAddressUtil#addressFromDistinguishedName(DistinguishedNameType)} method with distinguished name }
     */
    @Test
    public void addressFromDistinguishedNameTest_withDistinguishedName() {
        final DistinguishedNameType distinguishedNameType = new DistinguishedNameType(DISTINGUISHED_NAME_TYPE_VALUE_TEST);
        final Address testedAddress = LispAddressUtil.addressFromDistinguishedName(distinguishedNameType);

        assertTrue(testedAddress instanceof DistinguishedName);
        assertEquals(distinguishedNameType, ((DistinguishedName) testedAddress).getDistinguishedName());
    }

    /**
     * Test {@link LispAddressUtil#addressFromDistinguishedName(DistinguishedNameType)} method with null value }
     */
    @Test
    public void addressFromDistinguishedNameTest_withNull() {
        final Address testedAddress = LispAddressUtil.addressFromDistinguishedName(null);
        assertNull(testedAddress);
    }

    /**
     * Test {@link LispAddressUtil#addressFromAsNumber(AsNumber)} method with as number value }
     */
    @Test
    public void addressFromAsNumberTest_withAdNumber() {
        final AsNumber expectedAddress = new AsNumber(AS_NUMBER_TEST);
        final Address testedAddress = LispAddressUtil.addressFromAsNumber(expectedAddress);
        assertTrue(testedAddress instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.AsNumber);
        assertEquals(expectedAddress, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.AsNumber) testedAddress).getAsNumber());
    }

    /**
     * Test {@link LispAddressUtil#addressFromAsNumber(AsNumber)} method with null instead of value }
     */
    @Test
    public void addressFromAsNumberTest_withNull() {
        final Address testedAddress = LispAddressUtil.addressFromAsNumber(null);
        assertNull(testedAddress);
    }


    /**
     * Tests:
     * - {@link LispAddressUtil#toRloc(InetAddress)}
     * - {@link LispAddressUtil#toRloc(Ipv4Address)}
     * - {@link LispAddressUtil#toRloc(Ipv6Address)}
     * - {@link LispAddressUtil#toRloc(SimpleAddress)}
     * methods.
     */
    @Test
    public void toRloc() throws UnknownHostException {
        InetAddress ipv4AddressInet = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_A_TEST);
        final Rloc rlocFromInetAddress = LispAddressUtil.toRloc(ipv4AddressInet);
        assertEquals(Ipv4Afi.class, rlocFromInetAddress.getAddressType());
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4)rlocFromInetAddress.getAddress()).getIpv4().getValue());

        final Rloc rlocFromIpv4Address = LispAddressUtil.toRloc(IPV4_ADDRESS_TEST);
        assertEquals(Ipv4Afi.class, rlocFromIpv4Address.getAddressType());
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4) rlocFromIpv4Address.getAddress()).getIpv4().getValue());


        final Rloc rlocFromIpv6Address = LispAddressUtil.toRloc(IPV6_ADDRESS_TEST);
        assertEquals(Ipv6Afi.class, rlocFromIpv6Address.getAddressType());
        assertEquals(IPV6_ADDRESS_VALUE_TEST, ((Ipv6) rlocFromIpv6Address.getAddress()).getIpv6().getValue());

        final Rloc rlocFromSimpleAddress = LispAddressUtil.toRloc(SIMPLE_ADDRESS_A_TEST);
        assertEquals(Ipv4Afi.class, rlocFromSimpleAddress.getAddressType());
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4) rlocFromSimpleAddress.getAddress()).getIpv4().getValue());
    }

    /**
     * Test {@link LispAddressUtil#asIpv4Rloc(String)} method with ipv4 or ipv6.
     */
    @Test
    public void asIpv4Rloc() {
        final Rloc rlocFromIpV4 = LispAddressUtil.asIpv4Rloc(IPV4_ADDRESS_VALUE_TEST);
        assertEquals(Ipv4Afi.class, rlocFromIpV4.getAddressType());
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4) rlocFromIpV4.getAddress()).getIpv4().getValue());

        final Rloc rlocFromIpV6 = LispAddressUtil.asIpv6Rloc(IPV6_ADDRESS_VALUE_TEST);
        assertEquals(Ipv6Afi.class, rlocFromIpV6.getAddressType());
        assertEquals(IPV6_ADDRESS_VALUE_TEST, ((Ipv6) rlocFromIpV6.getAddress()).getIpv6().getValue());
    }

    /**
     * Test
     * - {@link LispAddressUtil#toEid(Ipv6Address, InstanceIdType)}
     * - {@link LispAddressUtil#asIpv6PrefixEid(String)}
     * - {@link LispAddressUtil#asIpv6PrefixEid(Eid, Inet6Address, short)}
     * - {@link LispAddressUtil#asIpv6PrefixEid(Ipv6Address, InstanceIdType)}
     * methods.
     */
    @Test
    public void toEid_ipv6Prefix() throws UnknownHostException {
        Eid eidFromIpv6Prefix = LispAddressUtil.toEid(IPV6_ADDRESS_PREFIX_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv6Prefix(eidFromIpv6Prefix, true, MASK_OK_TEST, Ipv6PrefixAfi.class);

        eidFromIpv6Prefix = LispAddressUtil.asIpv6PrefixEid(IPV6_ADDRESS_PREFIX_VALUE_TEST);
        verifyToEidWithIpv6Prefix(eidFromIpv6Prefix, false, MASK_OK_TEST, Ipv6PrefixAfi.class);

        eidFromIpv6Prefix = LispAddressUtil.asIpv6PrefixEid(IPV6_ADDRESS_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv6Prefix(eidFromIpv6Prefix, true, MASK_OK_DEFAULT_IPV6_TEST, Ipv6PrefixAfi.class);

        /**
         * Following code is testing method {@link LispAddressUtil#asIpv4PrefixEid(Eid, Inet6Address, short)} .
         * Method expected EID + inet6address with mask. It isn't specified whether EID should be also of type
         * inet4address. Therefore this test is created with EID which has address type MacAfi.
         */

        final InetAddress inet6Address = Inet6Address.getByAddress(IPV6_ADDRESS_BYTES_A_TEST);
        eidFromIpv6Prefix = LispAddressUtil.asIpv6PrefixEid(provideDummyMacEid(), (Inet6Address) inet6Address,
                MASK_OK_TEST);
        verifyToEidWithIpv6Prefix(eidFromIpv6Prefix, false, MASK_OK_TEST, MacAfi.class);

    }

    /**
     * Test
     * - {@link LispAddressUtil#toEid(MacAddress, InstanceIdType)}
     * - {@link LispAddressUtil#asMacEid(String)}
     * - {@link LispAddressUtil#asMacEid(String, long)}
     * methods.
     */
    @Test
    public void toEid_mac() {
        Eid eidFromMac = LispAddressUtil.toEid(MAC_ADDRESS_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithMacAddress(eidFromMac, true);

        eidFromMac = LispAddressUtil.asMacEid(MAC_ADDRESS_VALUE_TEST);
        verifyToEidWithMacAddress(eidFromMac, false);

        eidFromMac = LispAddressUtil.asMacEid(MAC_ADDRESS_VALUE_TEST, INSTANCE_ID_TYPE_VALUE_TEST);
        verifyToEidWithMacAddress(eidFromMac, true);
    }

    /**
     * Test
     * - {@link LispAddressUtil#toEid(Ipv6Address, InstanceIdType)}
     * - {@link LispAddressUtil#asIpv6Eid(String)}
     * - {@link LispAddressUtil#asIpv6Eid(String, long)}
     * methods.
     */
    @Test
    public void toEid_ipv6() {
        Eid eidFromIpv6 = LispAddressUtil.toEid(IPV6_ADDRESS_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv6(eidFromIpv6, true);

        eidFromIpv6 = LispAddressUtil.asIpv6Eid(IPV6_ADDRESS_VALUE_TEST);
        verifyToEidWithIpv6(eidFromIpv6, false);

        eidFromIpv6 = LispAddressUtil.asIpv6Eid(IPV6_ADDRESS_VALUE_TEST, INSTANCE_ID_TYPE_VALUE_TEST);
        verifyToEidWithIpv6(eidFromIpv6, true);
    }

    /**
     * Test
     * - {@link LispAddressUtil#toEid(Ipv4Prefix, InstanceIdType)}
     * - {@link LispAddressUtil#asIpv4PrefixEid(String)}
     * - {@link LispAddressUtil#asIpv4PrefixEid(Eid, Inet4Address, short)}
     * - {@link LispAddressUtil#asIpv4PrefixEid(Ipv4Address, InstanceIdType)}  }
     * methods.
     */
    @Test
    public void toEid_ipv4Prefix() throws UnknownHostException {
        Eid eidFromIpv4Prefix = LispAddressUtil.toEid(IPV4_ADDRESS_PREFIX_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpv4Prefix, true, MASK_OK_TEST, Ipv4PrefixAfi.class);

        eidFromIpv4Prefix  = LispAddressUtil.asIpv4PrefixEid(IPV4_ADDRESS_PREFIX_VALUE_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpv4Prefix, false, MASK_OK_TEST, Ipv4PrefixAfi.class);

        eidFromIpv4Prefix  = LispAddressUtil.asIpv4PrefixEid(IPV4_ADDRESS_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpv4Prefix, false, (short) 32, Ipv4PrefixAfi.class);


        /**
         * Following code is testing method {@link LispAddressUtil#asIpv4PrefixEid(Eid, Inet4Address, short)} .
         * Method expected EID + inet4address with mask. It isn't specified whether EID should be also of type
         * inet4address. Therefore this test is created with EID which has address type MacAfi.
         */

        final InetAddress inet4Address = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_A_TEST);
        eidFromIpv4Prefix = LispAddressUtil.asIpv4PrefixEid(provideDummyMacEid(), (Inet4Address) inet4Address,
                MASK_OK_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpv4Prefix, false, MASK_OK_TEST, MacAfi.class);
    }

    public Eid provideDummyMacEid() {
        final EidBuilder eidBuilder = new EidBuilder();
        eidBuilder.setAddressType(MacAfi.class);
        eidBuilder.setVirtualNetworkId(INSTANCE_ID_TYPE_TEST);
        return eidBuilder.build();
    }

    /**
     * Test
     * - {@link LispAddressUtil#toEid(Ipv4Address, InstanceIdType)}
     * - {@link LispAddressUtil#asIpv4Eid(String)}
     * - {@link LispAddressUtil#asIpv4Eid(String, long)}
     * methods.
     */
    @Test
    public void toEid_ipv4() {
        Eid eidFromIpv4 = LispAddressUtil.toEid(IPV4_ADDRESS_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv4(eidFromIpv4, true);

        eidFromIpv4 = LispAddressUtil.asIpv4Eid(IPV4_ADDRESS_VALUE_TEST);
        verifyToEidWithIpv4(eidFromIpv4, false);

        eidFromIpv4 = LispAddressUtil.asIpv4Eid(IPV4_ADDRESS_VALUE_TEST, INSTANCE_ID_TYPE_VALUE_TEST);
        verifyToEidWithIpv4(eidFromIpv4, true);
    }

    /**
     * Test
     * - {@link LispAddressUtil#toEid(IpPrefix, InstanceIdType)}
     * method.
     */
    @Test
    public void toEid_ipPrefix() {
        final Eid eidFromIpPrefix = LispAddressUtil.toEid(IP_ADDRESS_PREFIX_WITH_IPV4_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpPrefix, true, MASK_OK_TEST, Ipv4PrefixAfi.class);
    }

    /**
     * Test
     * - {@link LispAddressUtil#toEid(DistinguishedNameType, InstanceIdType)}
     * - {@link LispAddressUtil#asDistinguishedNameEid(String)}
     * - {@link LispAddressUtil#asDistinguishedNameEid(String, long)}
     * methods with various input.
     */
    @Test
    public void toEid_distinguishedName() {
        Eid eidFromDistinguishedName = LispAddressUtil.toEid(DISTINGUISHED_NAME_TYPE_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithDistinguishedName(eidFromDistinguishedName, true);

        eidFromDistinguishedName = LispAddressUtil.asDistinguishedNameEid(DISTINGUISHED_NAME_TYPE_VALUE_TEST);
        verifyToEidWithDistinguishedName(eidFromDistinguishedName, false);

        eidFromDistinguishedName = LispAddressUtil.asDistinguishedNameEid(DISTINGUISHED_NAME_TYPE_VALUE_WITH_MAC_TEST, INSTANCE_ID_TYPE_VALUE_TEST);
        verifyToEidWithMacAddress(eidFromDistinguishedName, true);
    }

    private void verifyToEidWithIpv6Prefix(final Eid eidFromIpv6Prefix, final boolean isVniChecked, short
            expectedMask, final Class<? extends org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
            .address.types.rev151105.LispAddressFamily> addressType) {
        assertEquals(addressType, eidFromIpv6Prefix.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv6Prefix.getVirtualNetworkId());
        }
        assertEquals(IPV6_ADDRESS_VALUE_TEST + "/" + expectedMask, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml
                .ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) eidFromIpv6Prefix
                .getAddress()).getIpv6Prefix().getValue());
    }

    private void verifyToEidWithIpv4(final Eid eidFromIpv4, final boolean isVniChecked) {
        assertEquals(Ipv4Afi.class, eidFromIpv4.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv4.getVirtualNetworkId());
        }
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4) eidFromIpv4.getAddress()).getIpv4().getValue());
    }

    private void verifyToEidWithIpv6(final Eid eidFromIpv6, final boolean isVniChecked) {
        assertEquals(Ipv6Afi.class, eidFromIpv6.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv6.getVirtualNetworkId());
        }
        assertEquals(IPV6_ADDRESS_VALUE_TEST, ((Ipv6) eidFromIpv6.getAddress()).getIpv6().getValue());
    }

    private void verifyToEidWithIpv4Prefix(final Eid eidFromIpv4Prefix, final boolean isVniChecked, short
            expectedMask, Class<? extends org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
            .types.rev151105.LispAddressFamily>  addressType) {
        assertEquals(addressType, eidFromIpv4Prefix.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv4Prefix.getVirtualNetworkId());
        }
        assertEquals(IPV4_ADDRESS_VALUE_TEST +"/"+expectedMask, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns
                .yang.ietf
                .lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix)eidFromIpv4Prefix.getAddress())
                .getIpv4Prefix().getValue());
    }

    private void verifyToEidWithDistinguishedName(final Eid eidFromDistinguishedName, final boolean isVniChecked) {
        assertEquals(DistinguishedNameAfi.class, eidFromDistinguishedName.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromDistinguishedName.getVirtualNetworkId());
        }
        assertEquals(DISTINGUISHED_NAME_TYPE_TEST, ((DistinguishedName)eidFromDistinguishedName.getAddress())
                .getDistinguishedName());
    }

    private void verifyToEidWithMacAddress(final Eid eidFromMac, final boolean isVniChecked) {
        assertEquals(MacAfi.class, eidFromMac.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromMac.getVirtualNetworkId());
        }
        assertEquals(MAC_ADDRESS_VALUE_TEST, ((Mac)eidFromMac.getAddress()).getMac().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#asIpPrefix(String, int)} with ipv4 address and correct mask
     */
    @Test
    public void asIpPrefix_ipv4() {
        final IpPrefix ipPrefix = LispAddressUtil.asIpPrefix(IPV4_ADDRESS_VALUE_TEST, MASK_OK_TEST);
        assertNotNull(ipPrefix);
        final Ipv4Prefix ipv4Prefix = ipPrefix.getIpv4Prefix();
        assertNotNull(ipv4Prefix);
        assertEquals(IPV4_ADDRESS_PREFIX_TEST, ipv4Prefix);
    }

    /**
     * Tests {@link LispAddressUtil#asIpPrefix(String, int)} with ipv6 address and correct mask
     */
    @Test
    public void asIpPrefix_ipv6() {
        final IpPrefix ipPrefix = LispAddressUtil.asIpPrefix(IPV6_ADDRESS_VALUE_TEST, MASK_OK_TEST);
        assertNotNull(ipPrefix);
        final Ipv6Prefix ipv6Prefix = ipPrefix.getIpv6Prefix();
        assertNotNull(ipv6Prefix);
        assertEquals(IPV6_ADDRESS_PREFIX_TEST, ipv6Prefix);
    }

    /**
     * Tests {@link LispAddressUtil#asIpPrefix(String, int)} with incorrect ip address and correct mask
     */
    @Test
    public void asIpPrefix_other() {
        final IpPrefix ipPrefix = LispAddressUtil.asIpPrefix(INCORRECT_IP_ADDRESS_TEST, MASK_OK_TEST);
        assertNull(ipPrefix);
    }

    /**
     * Tests {@link LispAddressUtil#ipVersionFromString(String)} with ipv4
     */
    @Test
    public void ipVersionFromString_ipv4() {
        final int addressType = LispAddressUtil.ipVersionFromString(IPV4_ADDRESS_VALUE_TEST);
        assertEquals(4, addressType);
    }

    /**
     * Tests {@link LispAddressUtil#ipVersionFromString(String)} with ipv6
     */
    @Test
    public void ipVersionFromString_ipv6() {
        final int addressType = LispAddressUtil.ipVersionFromString(IPV6_ADDRESS_VALUE_TEST);
        assertEquals(6, addressType);
    }

    /**
     * Tests {@link LispAddressUtil#ipVersionFromString(String)} with incorrect ip address format
     */
    @Test
    public void ipVersionFromString_other() {
        final int addressType = LispAddressUtil.ipVersionFromString(INCORRECT_IP_ADDRESS_TEST);
        assertEquals(0, addressType);

    }

    /**
     * Tests {@link LispAddressUtil#asKeyValueAddressEid(SimpleAddress, SimpleAddress)} method.
     */
    @Test
    public void asKeyValueAddressEid() {
        final Eid eid = LispAddressUtil.asKeyValueAddressEid(SIMPLE_ADDRESS_A_TEST, SIMPLE_ADDRESS_B_TEST);
        verifyKeyValueAddress(eid, SIMPLE_ADDRESS_A_TEST);
    }

    /**
     * Tests {@link LispAddressUtil#asKeyValueAddressRloc(SimpleAddress, SimpleAddress)} method.
     */
    @Test
    public void asKeyValueAddressRloc() {
        final Rloc rloc = LispAddressUtil.asKeyValueAddressRloc(SIMPLE_ADDRESS_A_TEST, SIMPLE_ADDRESS_B_TEST);
        verifyKeyValueAddress(rloc, SIMPLE_ADDRESS_A_TEST);
    }

    /**
     * Tests {@link LispAddressUtil#asKeyValueAddress(String, SimpleAddress)} method.
     */
    @Test
    public void asKeyValueAddress() {
        final Rloc rloc = LispAddressUtil.asKeyValueAddress(SIMPLE_ADDRESS_DISTINGUISHED_VALUE_TEST, SIMPLE_ADDRESS_B_TEST);
        verifyKeyValueAddress(rloc, SIMPLE_ADDRESS_DISTINGUISHED_TEST);
    }

    private void verifyKeyValueAddress(final LispAddress lispAddress, final SimpleAddress keyValue) {
        assertEquals(KeyValueAddressLcaf.class, lispAddress.getAddressType());
        assertNull(lispAddress.getVirtualNetworkId());
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.key.value.address.KeyValueAddress keyValueAddress = ((KeyValueAddress) lispAddress.getAddress()).
                getKeyValueAddress();
        assertNotNull(keyValueAddress);
        assertEquals(keyValue, keyValueAddress.getKey());
        assertEquals(SIMPLE_ADDRESS_B_TEST, keyValueAddress.getValue());
    }

    /**
     * Tests {@link LispAddressUtil#asSrcDst(String, String, int, int)} method.
     */
    @Test
    public void asSrcDst() {
        final SourceDestKey sourceDestKey = LispAddressUtil.asSrcDst(IPV4_ADDRESS_VALUE_TEST,
                IPV6_ADDRESS_VALUE_TEST, MASK_OK_TEST, MASK_OK_TEST);
        assertNotNull(sourceDestKey);
        assertEquals(SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV4_TEST, sourceDestKey.getSource());
        assertEquals(SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV6_TEST, sourceDestKey.getDest());
    }

    /**
     * Tests {@link LispAddressUtil#asSrcDstEid(String, String, int, int, int)} method.
     */
    @Test
    public void asSrcDstEid_addressesAsString() {
        final Eid srcDstEid = LispAddressUtil.asSrcDstEid(IPV4_ADDRESS_VALUE_TEST,
                IPV6_ADDRESS_VALUE_TEST, MASK_OK_TEST, MASK_OK_TEST, INSTANCE_ID_TYPE_VALUE_SHORT_TEST);
        assertNotNull(srcDstEid);
        assertEquals(SourceDestKeyLcaf.class, srcDstEid.getAddressType());
        final SourceDestKey sourceDestKey = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns
                .yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey) srcDstEid.getAddress())
                .getSourceDestKey();
        assertNotNull(sourceDestKey);
        assertEquals(SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV4_TEST, sourceDestKey.getSource());
        assertEquals(SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV6_TEST, sourceDestKey.getDest());
    }

    /**
     * Tests {@link LispAddressUtil#asSrcDstEid(SourceDestKey, InstanceIdType)}  method.
     */
    @Test
    public void asSrcDstEid_addressesAsSrcDstKey() {
        final SourceDestKey expectedSourceDestKey = new SourceDestKeyBuilder().setSource
                (SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV4_TEST).setDest(SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV6_TEST).build();
        final Eid srcDstEid = LispAddressUtil.asSrcDstEid(expectedSourceDestKey, INSTANCE_ID_TYPE_TEST);
        assertNotNull(srcDstEid);
        assertEquals(SourceDestKeyLcaf.class, srcDstEid.getAddressType());
        final SourceDestKey testedSourceDestKey = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns
                .yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey) srcDstEid.getAddress())
                .getSourceDestKey();
        assertNotNull(testedSourceDestKey);
        assertEquals(SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV4_TEST, testedSourceDestKey.getSource());
        assertEquals(SIMPLE_ADDRESS_WITH_IP_PREFIX_IPV6_TEST, testedSourceDestKey.getDest());
    }

    /**
     * Tests {@link LispAddressUtil#asSrcDstEid(SourceDestKey, InstanceIdType)}  method.
     */
    @Test
    public void asTeLcafRloc() {
        final List<IpAddress> ipAddresses = Arrays.asList(IP_ADDRESS_OBJECT_WITH_IPV4_TEST, IP_ADDRESS_OBJECT_WITH_IPV6_TEST);
        final Rloc rloc = LispAddressUtil.asTeLcafRloc(ipAddresses);
        assertNotNull(rloc);
        assertEquals(ExplicitLocatorPathLcaf.class, rloc.getAddressType());
        final List<Hop> hops = ((ExplicitLocatorPath) rloc.getAddress()).getExplicitLocatorPath().getHop();
        assertEquals(2, hops.size());
        assertEquals(hops.size(), ipAddresses.size());
        for(Hop hop : hops) {
            final Hop.LrsBits lrsBits = hop.getLrsBits();
            assertFalse(lrsBits.isLookup());
            assertFalse(lrsBits.isRlocProbe());
            assertFalse(lrsBits.isStrict());

            final IpAddress ipAddressFromHop = hop.getAddress().getIpAddress();
            assertNotNull(ipAddressFromHop);
            assertTrue("ExplicitLocatorPath contains hop " + ipAddressFromHop + " which wasn't specified at " +
                    "input.", ipAddresses.contains(ipAddressFromHop));
        }
    }

    /**
     * Tests {@link LispAddressUtil#asLocatorRecords(List)}  method.
     */
    @Test
    public void asLocatorRecords() {
        final List<Rloc> expectedRlocs = Arrays.asList(LispAddressUtil.toRloc(IPV4_ADDRESS_TEST), LispAddressUtil.toRloc
                (IPV6_ADDRESS_TEST));
        final List<LocatorRecord> locatorRecords = LispAddressUtil.asLocatorRecords(expectedRlocs);

        assertEquals(expectedRlocs.size(), locatorRecords.size());
        for (LocatorRecord locatorRecord : locatorRecords) {
            assertFalse(locatorRecord.isLocalLocator());
            assertFalse(locatorRecord.isRlocProbed());
            assertTrue(locatorRecord.isRouted());
            assertTrue(1 == locatorRecord.getWeight());
            assertTrue(1 == locatorRecord.getPriority());
            assertTrue(1 == locatorRecord.getMulticastWeight());
            assertTrue(1 == locatorRecord.getMulticastPriority());
            assertEquals("SFC_LISP", locatorRecord.getLocatorId());
            assertTrue("Rloc " + locatorRecord.getRloc() + "wasn't specified at expected rloc list.", expectedRlocs.contains
                    (locatorRecord.getRloc()));
        }
    }

    /**
     * Tests {@link LispAddressUtil#getNoAddressEid()}  method.
     */
    @Test
    public void getNoAddressEid() {
        final Eid noAddressEid = LispAddressUtil.getNoAddressEid();
        assertEquals(NoAddressAfi.class, noAddressEid.getAddressType());
        assertNull(noAddressEid.getVirtualNetworkId());
        assertTrue(((NoAddress) noAddressEid.getAddress()).isNoAddress());
    }

    /**
     * Tests {@link LispAddressUtil#ipAddressToInet(Address)} method with ipv4 value.
     */
    @Test
    public void ipAddressToInet_ipv4() {
        final Ipv4 expectedIpv4 = new Ipv4Builder().setIpv4(IPV4_ADDRESS_TEST).build();
        final InetAddress testedAddress = LispAddressUtil.ipAddressToInet(expectedIpv4);
        assertEquals(IPV4_ADDRESS_TEST.getValue(), testedAddress.getHostAddress());
    }

    /**
     * Tests {@link LispAddressUtil#ipAddressToInet(Address)} method with ipv6 value.
     */
    @Test
    public void ipAddressToInet_ipv6() {
        final Ipv6 expectedIpv6 = new Ipv6Builder().setIpv6(IPV6_ADDRESS_TEST).build();
        final InetAddress testedAddress = LispAddressUtil.ipAddressToInet(expectedIpv6);
        assertEquals(IPV6_ADDRESS_TEST.getValue(), testedAddress.getHostAddress());
    }

    /**
     * Tests {@link LispAddressUtil#ipAddressToInet(Address)} method with mac value.
     */
    @Test
    public void ipAddressToInet_other() {
        final InetAddress testedAddress = LispAddressUtil.ipAddressToInet(new MacBuilder().build());
        assertNull(testedAddress);
    }

    /**
     * Tests {@link LispAddressUtil#compareInetAddresses(InetAddress, InetAddress)} with all possible combination
     * of input parameters to reach 100 % coverage.
     */
    @Test
    public void compareInetAddresses() throws UnknownHostException {
        final InetAddress inetIpv4A = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_A_TEST);
        final InetAddress inetIpv6A = Inet6Address.getByAddress(IPV6_ADDRESS_BYTES_A_TEST);
        final InetAddress inetIpv4B = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_B_TEST);
        final InetAddress inetIpv6B = Inet6Address.getByAddress(IPV6_ADDRESS_BYTES_B_TEST);

        int comparationResult = LispAddressUtil.compareInetAddresses(inetIpv4A, inetIpv6A);
        assertEquals(-1, comparationResult);

        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv6A, inetIpv4A);
        assertEquals(1, comparationResult);

        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv4A, inetIpv4B);
        assertEquals(-1, comparationResult);

        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv4B, inetIpv4A);
        assertEquals(1, comparationResult);

        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv4B, inetIpv4B);
        assertEquals(0, comparationResult);

        // remove this ignore once https://git.opendaylight.org/gerrit/#/c/35682/1 will be merged */
/*
        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv6A, inetIpv6B);
        assertEquals(-1, comparationResult);

        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv6B, inetIpv6A);
        assertEquals(1, comparationResult);

        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv6B, inetIpv6B);
        assertEquals(0, comparationResult);
*/

        final InetAddress inetAddress = mock(InetAddress.class);
        comparationResult = LispAddressUtil.compareInetAddresses(inetIpv6B, inetAddress);
        assertEquals(0, comparationResult);
    }

    /**
     * Tests {@link LispAddressUtil#asServicePathEid(long, long, short)} method.
     */
    @Test
    public void asServicePathEid() {
        final Eid eid = LispAddressUtil.asServicePathEid(INSTANCE_ID_TYPE_VALUE_TEST, SERVICE_PATH_ID_TEST,
                SERVICE_INDEX_TEST);

        assertNotNull(eid);
        assertEquals(ServicePathLcaf.class, eid.getAddressType());
        assertEquals(INSTANCE_ID_TYPE_TEST, eid.getVirtualNetworkId());
        ServicePath servicePath = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.
                address.types.rev151105.lisp.address.address.ServicePath) eid.getAddress()).getServicePath();
        assertNotNull(servicePath);
        assertEquals(SERVICE_INDEX_TEST, servicePath.getServiceIndex());
        assertEquals(SERVICE_PATH_ID_TEST, servicePath.getServicePathId().getValue());
    }

    /**
     * Tests {@link LispAddressUtil#toIpPrefixEid(IpAddress, int)} method.
     */
    @Ignore /* remove when https://git.opendaylight.org/gerrit/#/c/35681/2 will be merged */
    @Test
    public void toIpPrefixEid() throws UnknownHostException {
        final Eid eid = LispAddressUtil.toIpPrefixEid(IP_ADDRESS_OBJECT_WITH_IPV4_TEST, INSTANCE_ID_TYPE_VALUE_SHORT_TEST);
        verifyEidContainsIpPrefix(eid, MASK_OK_DEFAULT_IPV4_TEST);
    }

    /**
     * Tests {@link LispAddressUtil#toEidNoVni(IpPrefix)} method.
     */
    @Test
    public void toEidNoVni() {
        final Eid eid = LispAddressUtil.toEidNoVni(IP_ADDRESS_PREFIX_WITH_IPV4_TEST);
        verifyEidContainsIpPrefix(eid, MASK_OK_TEST);
    }

    public void verifyEidContainsIpPrefix(final Eid eid, final short mask) {
        final Ipv4Prefix expectedPrefix = new Ipv4Prefix(IPV4_ADDRESS_VALUE_TEST + "/" + mask);
        assertNotNull(eid);
        assertTrue(eid.getAddress() instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv4Prefix);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.Ipv4Prefix address = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                .address.types.rev151105.lisp.address.address.Ipv4Prefix) eid.getAddress();
        assertEquals(expectedPrefix, address.getIpv4Prefix());
    }

    /**
     * Tests {@link LispAddressUtil#asEid(SimpleAddress, InstanceIdType)} method.
     */
    @Test
    public void asEid() {
        final Eid eid = LispAddressUtil.asEid(SIMPLE_ADDRESS_A_TEST, INSTANCE_ID_TYPE_TEST);
        assertNotNull(eid);
        assertEquals(Ipv4Afi.class, eid.getAddressType());
        final Ipv4 address = (Ipv4) eid.getAddress();
        assertEquals(SIMPLE_ADDRESS_A_TEST.getIpAddress().getIpv4Address(), address.getIpv4());
        assertEquals(INSTANCE_ID_TYPE_TEST, eid.getVirtualNetworkId());
    }

}

