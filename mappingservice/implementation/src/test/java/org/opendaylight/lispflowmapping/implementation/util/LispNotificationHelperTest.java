/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.ApplicationDataBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.KeyValueAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list
        .MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification
        .MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;

public class LispNotificationHelperTest {

    private static final String IPV4_STRING = "1.2.3.0";
    private static final String IPV6_STRING = "1111:2222:3333:4444:5555:6666:7777:8888";

    private static final Ipv4Address ADDRESS_IPV4 = new Ipv4Address(IPV4_STRING);
    private static final Ipv6Address ADDRESS_IPV6 = new Ipv6Address(IPV6_STRING);
    private static final Ipv4AddressBinary ADDRESS_IPV4_BINARY = new Ipv4AddressBinary(InetAddresses.
            forString(IPV4_STRING).getAddress());
    private static final Ipv6AddressBinary ADDRESS_IPV6_BINARY = new Ipv6AddressBinary(InetAddresses.
            forString(IPV6_STRING).getAddress());

    private static final int PORT = 9999;
    private static final DistinguishedNameType DISTINGUISHED_NAME_TYPE =
            new DistinguishedNameType(IPV4_STRING + ":" + PORT);

    private static final Eid EID_IPV4 = LispAddressUtil.asIpv4Eid(IPV4_STRING);
    private static final Eid EID_IPV6 = LispAddressUtil.asIpv6Eid(IPV6_STRING);

    private static final Rloc RLOC_IPV4 = LispAddressUtil.toRloc(ADDRESS_IPV4);
    private static final Rloc RLOC_IPV6 = LispAddressUtil.toRloc(ADDRESS_IPV6);
    private static final Rloc RLOC_IPV4_BINARY = LispAddressUtil.toRloc(ADDRESS_IPV4_BINARY);
    private static final Rloc RLOC_IPV6_BINARY = LispAddressUtil.toRloc(ADDRESS_IPV6_BINARY);
    private static final Rloc RLOC_KEYVALUE_ADDRESS = new RlocBuilder().setAddress(getDefaultKeyValueAddress()).build();
    private static final Rloc RLOC_DISTINGUISHED_NAME_ADDRESS = new RlocBuilder()
            .setAddress(new DistinguishedNameBuilder().setDistinguishedName(DISTINGUISHED_NAME_TYPE).build()).build();
    private static final Rloc RLOC_APPLICATION_DATA_IPV4 = new RlocBuilder()
            .setAddress(getDefaultApplicationDataForIpv4()).build();
    private static final Rloc RLOC_APPLICATION_DATA_IPV6 = new RlocBuilder()
            .setAddress(getDefaultApplicationDataForIpv6()).build();
    private static final Rloc RLOC_APPLICATION_DATA_IPV6_PREFIX = new RlocBuilder()
            .setAddress(getDefaultApplicationDataForIpv4Prefix()).build();
    private static final Rloc RLOC_IPV4_PREFIX = new RlocBuilder()
            .setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(new Ipv4Prefix(IPV4_STRING + "/20")).build()).build();

    private static final SiteId SITE_ID = new SiteId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});


    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with Ipv4 type address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withIpv4Address() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV4_BINARY))
                .setPort(new PortNumber(LispMessage.PORT_NUM)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_IPV4));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with Ipv6 type address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withIpv6Address() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV6_BINARY))
                .setPort(new PortNumber(LispMessage.PORT_NUM)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_IPV6));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with Ipv4AddressBinary type address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withIpv4AddressBinary() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV4_BINARY))
                .setPort(new PortNumber(LispMessage.PORT_NUM)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_IPV4_BINARY));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with Ipv6AddressBinary type address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withIpv6AddressBinary() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV6_BINARY))
                .setPort(new PortNumber(LispMessage.PORT_NUM)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_IPV6_BINARY));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with KeyValueAddress type address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withKeyValueAddress() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV4_BINARY))
                .setPort(new PortNumber(PORT)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_KEYVALUE_ADDRESS));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with DistinguishedName type address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withDistinguishedNameAddress() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV4_BINARY))
                .setPort(new PortNumber(PORT)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_DISTINGUISHED_NAME_ADDRESS));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with ApplicationData type Ipv4 address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withApplicationDataIpv4Address() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV4_BINARY))
                .setPort(new PortNumber(PORT)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_APPLICATION_DATA_IPV4));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with ApplicationData type Ipv6 address.
     */
    @Test
    public void getTransportAddressFromRlocTest_withApplicationDataIpv6Address() {
        final TransportAddress result = new TransportAddressBuilder()
                .setIpAddress(new IpAddressBinary(ADDRESS_IPV6_BINARY))
                .setPort(new PortNumber(PORT)).build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_APPLICATION_DATA_IPV6));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with ApplicationData type address.
     */
    @Test(expected = NullPointerException.class)
    public void getTransportAddressFromRlocTest_withApplicationDataIpv4PrefixAddress() {
        final TransportAddress result = new TransportAddressBuilder().build();

        assertEquals(result, LispNotificationHelper.getTransportAddressFromRloc(RLOC_APPLICATION_DATA_IPV6_PREFIX));
    }

    /**
     * Tests {@link LispNotificationHelper#getTransportAddressFromRloc} method with Ipv4Prefix type address.
     * Null address expected.
     */
    @Test
    public void getTransportAddressFromRlocTest_withIpv4PrefixAddress() {
        assertNull(LispNotificationHelper.getTransportAddressFromRloc(RLOC_IPV4_PREFIX).getIpAddress());
    }

    /**
     * Tests {@link LispNotificationHelper#getAddressByName} method with correct address.
     */
    @Test
    public void getAddressByNameTest() throws UnknownHostException {
        final InetAddress result = InetAddress.getByName(IPV4_STRING);
        assertEquals(result, LispNotificationHelper.getAddressByName(IPV4_STRING));
    }

    /**
     * Tests {@link LispNotificationHelper#getAddressByName} method with invalid address.
     */
    @Test
    public void getAddressByNameTest_withInvalidAddress() {
        assertNull(LispNotificationHelper.getAddressByName("[" + IPV4_STRING));
    }

    /**
     * Tests {@link LispNotificationHelper#getMapping} method with invalid address.
     */
    @Test
    public void getMappingTest() {
        final MappingBuilder mappingBuilder_1 = new MappingBuilder()
                .setMappingRecord(new MappingRecordBuilder().setEid(EID_IPV4).build())
                .setEidUri(new EidUri("ipv4:" + IPV4_STRING))
                .setOrigin(MappingOrigin.Southbound)
                .setSiteId(Lists.newArrayList(SITE_ID));

        final MappingBuilder mappingBuilder_2 = new MappingBuilder()
                .setMappingRecord(new MappingRecordBuilder().setEid(EID_IPV6).build())
                .setEidUri(new EidUri("ipv6:" + IPV6_STRING))
                .setOrigin(MappingOrigin.Southbound)
                .setSiteId(Lists.newArrayList(SITE_ID));

        assertEquals(Lists.newArrayList(mappingBuilder_1.build(), mappingBuilder_2.build()),
                LispNotificationHelper.getMapping(getDefaultAddMapping(true)));
    }

    /**
     * Tests {@link LispNotificationHelper#getMapping} method with invalid address, isXtrSiteIdPresent == false.
     */
    @Test
    public void getMappingTest_SiteIdNotPresent() {
        final MappingBuilder mappingBuilder_1 = new MappingBuilder()
                .setMappingRecord(new MappingRecordBuilder().setEid(EID_IPV4).build())
                .setEidUri(new EidUri("ipv4:" + IPV4_STRING))
                .setOrigin(MappingOrigin.Southbound);

        final MappingBuilder mappingBuilder_2 = new MappingBuilder()
                .setMappingRecord(new MappingRecordBuilder().setEid(EID_IPV6).build())
                .setEidUri(new EidUri("ipv6:" + IPV6_STRING))
                .setOrigin(MappingOrigin.Southbound);

        assertEquals(Lists.newArrayList(mappingBuilder_1.build(), mappingBuilder_2.build()),
                LispNotificationHelper.getMapping(getDefaultAddMapping(false)));
    }

    private static KeyValueAddress getDefaultKeyValueAddress() {
        return new KeyValueAddressBuilder()
                .setKeyValueAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                        .rev151105.lisp.address.address.key.value.address.KeyValueAddressBuilder()
                        .setKey(new SimpleAddress(new DistinguishedNameType("key")))
                        .setValue(new SimpleAddress(DISTINGUISHED_NAME_TYPE)).build())
                .build();
    }

    private static ApplicationData getDefaultApplicationDataForIpv4() {
        return new ApplicationDataBuilder()
                .setApplicationData(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                        .rev151105.lisp.address.address.application.data.ApplicationDataBuilder()
                        .setAddress(new SimpleAddress(new IpAddress(ADDRESS_IPV4)))
                        .setLocalPortLow(new PortNumber(PORT)).build())
                .build();
    }

    private static ApplicationData getDefaultApplicationDataForIpv6() {
        return new ApplicationDataBuilder()
                .setApplicationData(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                        .rev151105.lisp.address.address.application.data.ApplicationDataBuilder()
                        .setAddress(new SimpleAddress(new IpAddress(ADDRESS_IPV6)))
                        .setLocalPortLow(new PortNumber(PORT)).build())
                .build();
    }

    private static ApplicationData getDefaultApplicationDataForIpv4Prefix() {
        return new ApplicationDataBuilder()
                .setApplicationData(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                        .rev151105.lisp.address.address.application.data.ApplicationDataBuilder()
                        .setAddress(new SimpleAddress(new IpPrefix(new Ipv4Prefix(IPV4_STRING + "/20"))))
                        .setLocalPortLow(new PortNumber(PORT)).build())
                .build();
    }

    private static AddMapping getDefaultAddMapping(boolean isXtrSiteIdPresent) {
        final MappingRecordItem mappingRecordItem_1 = new MappingRecordItemBuilder()
                .setMappingRecord(new MappingRecordBuilder().setEid(EID_IPV4).build()).build();
        final MappingRecordItem mappingRecordItem_2 = new MappingRecordItemBuilder()
                .setMappingRecord(new MappingRecordBuilder().setEid(EID_IPV6).build()).build();

        final MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder()
                .setMappingRecordItem(Lists.newArrayList(mappingRecordItem_1, mappingRecordItem_2))
                .setXtrSiteIdPresent(isXtrSiteIdPresent)
                .setSiteId(SITE_ID);

        return new AddMappingBuilder().setMapRegister(mapRegisterBuilder.build()).build();
    }
}
