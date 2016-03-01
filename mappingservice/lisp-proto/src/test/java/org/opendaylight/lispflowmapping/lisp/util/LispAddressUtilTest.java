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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class LispAddressUtilTest {


    private static final char[] DUMMY_CHARACTER_ARRAY_TEST = new char[]{'a', 'b', 'c', 'd'};
    private static final Long NUMBER_TEST = 5L;

    private static final String MAC_ADDRESS_VALUE_TEST = "aa:bb:cc:dd:ee:ff";
    private static final MacAddress MAC_ADDRESS_TEST = new MacAddress("aa:bb:cc:dd:ee:ff");

    private static final String IPV4_ADDRESS_PREFIX_VALUE_TEST = "192.168.1.2/30";
    private static final Ipv4Prefix IPV4_ADDRESS_PREFIX_TEST = new Ipv4Prefix(IPV4_ADDRESS_PREFIX_VALUE_TEST);

    private static final IpPrefix IP_ADDRESS_PREFIX = new IpPrefix(IPV4_ADDRESS_PREFIX_TEST);

    private static final String IPV6_ADDRESS_PREFIX_VALUE_TEST = "102:304:506:708:90a:b0c:d0e:f11/30";
    private static final Ipv6Prefix IPV6_ADDRESS_PREFIX_TEST = new Ipv6Prefix(IPV6_ADDRESS_PREFIX_VALUE_TEST);

    private static final byte[] IPV4_ADDRESS_BYTES_TEST = new byte[]{(byte) 192, (byte) 168, 1, 1};
    private static final String IPV4_ADDRESS_VALUE_TEST = "192.168.1.1";
    private static final Ipv4Address IPV4_ADDRESS_TEST = new Ipv4Address(IPV4_ADDRESS_VALUE_TEST);
    private static final IpAddress IP_ADDRESS_OBJECT_GENERAL_TEST = new IpAddress(IPV4_ADDRESS_TEST);
    private static final SimpleAddress SIMPLE_ADDRESS_TEST = new SimpleAddress(IP_ADDRESS_OBJECT_GENERAL_TEST);

    private static final byte[] IPV6_ADDRESS_BYTES_TEST = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 16};
    private static final String IPV6_ADDRESS_VALUE_TEST = "102:304:506:708:90a:b0c:d0e:f10";
    private static final Ipv6Address IPV6_ADDRESS_TEST = new Ipv6Address(IPV6_ADDRESS_VALUE_TEST);

    private static final Short DUMMY_SERVICE_INDEX = 45;
    private static final Long DUMMY_SERVICE_PATH_ID_TYPE = 46L;
    private static final String DISTINGUISHED_NAME_TYPE_VALUE_TEST = "dummy distinguished name type";
    private static final String DISTINGUISHED_NAME_TYPE_VALUE_WITH_MAC_TEST = MAC_ADDRESS_VALUE_TEST;
    private static final DistinguishedNameType DISTINGUISHED_NAME_TYPE_TEST = new DistinguishedNameType
            (DISTINGUISHED_NAME_TYPE_VALUE_TEST);
    private static final Long AS_NUMBER_TEST = 100L;

    private static final Long INSTANCE_ID_TYPE_VALUE_TEST = 121L;
    private static final Short INSTANCE_ID_TYPE_VALUE_SHORT_TEST = 122;
    private static final InstanceIdType INSTANCE_ID_TYPE_TEST = new InstanceIdType(INSTANCE_ID_TYPE_VALUE_TEST);

    /**
     * Tests {@link LispAddressUtil#addressTypeFromSimpleAddress} and {@link
     * LispAddressUtil#addressFromSimpleAddress} methods
     * with ipAddress
     */
    @Test
    public void addressFromSimpleAddressTest_asAnyIpAddress() {
        final Class<? extends LispAddressFamily> addressClass = LispAddressUtil.addressTypeFromSimpleAddress
                (SIMPLE_ADDRESS_TEST);
        assertEquals(Ipv4Afi.class, addressClass);

        final Address address = LispAddressUtil.addressFromSimpleAddress(SIMPLE_ADDRESS_TEST);
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
        final InetAddress ipv4InetAddress = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_TEST);
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
        final InetAddress ipv6InetAddress = Inet6Address.getByAddress(IPV6_ADDRESS_BYTES_TEST);
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
        servicePathBuilder.setServiceIndex(DUMMY_SERVICE_INDEX);
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
        InetAddress ipv4AddressInet = Inet4Address.getByAddress(IPV4_ADDRESS_BYTES_TEST);
        final Rloc rlocFromInetAddress = LispAddressUtil.toRloc(ipv4AddressInet);
        assertEquals(Ipv4Afi.class, rlocFromInetAddress.getAddressType());
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4)rlocFromInetAddress.getAddress()).getIpv4().getValue());

        final Rloc rlocFromIpv4Address = LispAddressUtil.toRloc(IPV4_ADDRESS_TEST);
        assertEquals(Ipv4Afi.class, rlocFromIpv4Address.getAddressType());
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4)rlocFromIpv4Address.getAddress()).getIpv4().getValue());


        final Rloc rlocFromIpv6Address = LispAddressUtil.toRloc(IPV6_ADDRESS_TEST);
        assertEquals(Ipv6Afi.class, rlocFromIpv6Address.getAddressType());
        assertEquals(IPV6_ADDRESS_VALUE_TEST, ((Ipv6)rlocFromIpv6Address.getAddress()).getIpv6().getValue());

        final Rloc rlocFromSimpleAddress = LispAddressUtil.toRloc(SIMPLE_ADDRESS_TEST);
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
     * methods.
     */
    @Test
    public void toEid_ipv6Prefix() {
        Eid eidFromIpv6Prefix = LispAddressUtil.toEid(IPV6_ADDRESS_PREFIX_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv6Prefix(eidFromIpv6Prefix, true);

        eidFromIpv6Prefix = LispAddressUtil.asIpv6PrefixEid(IPV6_ADDRESS_PREFIX_VALUE_TEST);
        verifyToEidWithIpv6Prefix(eidFromIpv6Prefix, false);
        //TODO missing one method with as ....
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
     * methods.
     */
    @Test
    public void toEid_ipv4Prefix() {
        Eid eidFromIpv4Prefix = LispAddressUtil.toEid(IPV4_ADDRESS_PREFIX_TEST, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpv4Prefix, true);

        eidFromIpv4Prefix  = LispAddressUtil.asIpv4PrefixEid(IPV4_ADDRESS_PREFIX_VALUE_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpv4Prefix, false);
        //TODO missing one method with as...

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
        final Eid eidFromIpPrefix = LispAddressUtil.toEid(IP_ADDRESS_PREFIX, INSTANCE_ID_TYPE_TEST);
        verifyToEidWithIpv4Prefix(eidFromIpPrefix, true);
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

    private void verifyToEidWithIpv6Prefix(final Eid eidFromIpv6Prefix, final boolean isVniChecked) {
        assertEquals(Ipv6PrefixAfi.class, eidFromIpv6Prefix.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv6Prefix.getVirtualNetworkId());
        }
        assertEquals(IPV6_ADDRESS_PREFIX_VALUE_TEST, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) eidFromIpv6Prefix.getAddress())
                .getIpv6Prefix().getValue());
    }

    private void verifyToEidWithIpv4(final Eid eidFromIpv4, final boolean isVniChecked) {
        assertEquals(Ipv4Afi.class, eidFromIpv4.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv4.getVirtualNetworkId());
        }
        assertEquals(IPV4_ADDRESS_VALUE_TEST, ((Ipv4)eidFromIpv4.getAddress()).getIpv4().getValue());
    }

    private void verifyToEidWithIpv6(final Eid eidFromIpv6, final boolean isVniChecked) {
        assertEquals(Ipv6Afi.class, eidFromIpv6.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv6.getVirtualNetworkId());
        }
        assertEquals(IPV6_ADDRESS_VALUE_TEST, ((Ipv6)eidFromIpv6.getAddress()).getIpv6().getValue());
    }

    private void verifyToEidWithIpv4Prefix(final Eid eidFromIpv4Prefix, final boolean isVniChecked) {
        assertEquals(Ipv4PrefixAfi.class, eidFromIpv4Prefix.getAddressType());
        if (isVniChecked) {
            assertEquals(INSTANCE_ID_TYPE_TEST, eidFromIpv4Prefix.getVirtualNetworkId());
        }
        assertEquals(IPV4_ADDRESS_PREFIX_VALUE_TEST, ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
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
}

