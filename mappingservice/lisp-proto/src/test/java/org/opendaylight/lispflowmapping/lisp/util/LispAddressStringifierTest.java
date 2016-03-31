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

import com.google.common.io.BaseEncoding;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .AfiList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .AfiListBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .ApplicationDataBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .AsNumberBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .ExplicitLocatorPathBuilder;
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
        .KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .KeyValueAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .NoAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .ServicePathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .explicit.locator.path.explicit.locator.path.HopKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class LispAddressStringifierTest {

    private static final long VNI = 100L;
    private static final long IID = 200L;

    // Ipv4
    private static final String IPV4_STRING = "192.168.0.1";
    private static final Ipv4Address IPV4_ADDRESS = new Ipv4Address(IPV4_STRING);
    private static final Ipv4 IPV4 = new Ipv4Builder().setIpv4(IPV4_ADDRESS).build();
    private static final LispAddress LISP_IPV4 = new EidBuilder()
            .setAddress(IPV4)
            .setVirtualNetworkId(new InstanceIdType(VNI)).build();

    // Ipv6
    private static final String IPV6_STRING = "1111:2222:3333:4444:5555:6666:7777:8888";
    private static final Ipv6Address IPV6_ADDRESS = new Ipv6Address(IPV6_STRING);
    private static final Ipv6 IPV6 = new Ipv6Builder().setIpv6(new Ipv6Address(IPV6_STRING)).build();
    private static final LispAddress LISP_IPV6 = new EidBuilder().setAddress(IPV6).build();

    // Ipv4Prefix
    private static final String PREFIX = "/24";
    private static final String PREFIX_URL = "%2f24";
    private static final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.Ipv4Prefix IPV4_PREFIX = new Ipv4PrefixBuilder()
            .setIpv4Prefix(new Ipv4Prefix(IPV4_STRING + PREFIX)).build();
    private static final LispAddress LISP_IPV4_PREFIX = new EidBuilder().setAddress(IPV4_PREFIX).build();

    // Ipv6Prefix
    private static final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.Ipv6Prefix IPV6_PREFIX = new Ipv6PrefixBuilder()
            .setIpv6Prefix(new Ipv6Prefix(IPV6_STRING + PREFIX)).build();
    private static final LispAddress LISP_IPV6_PREFIX = new EidBuilder().setAddress(IPV6_PREFIX).build();

    // Mac
    private static final MacAddress MAC_ADDRESS = new MacAddress("01:23:45:67:89:ab");
    private static final Mac MAC = new MacBuilder().setMac(MAC_ADDRESS).build();
    private static final LispAddress LISP_MAC = new EidBuilder().setAddress(MAC).build();

    // DistinguishedNameType
    private static final DistinguishedNameType DISTINGUISHED_NAME_TYPE =
            new DistinguishedNameType("distinguished_name");
    private static final DistinguishedName DISTINGUISHED_NAME = new DistinguishedNameBuilder()
            .setDistinguishedName(DISTINGUISHED_NAME_TYPE).build();
    private static final LispAddress LISP_DISTINGUISHED_NAME = new EidBuilder().setAddress(DISTINGUISHED_NAME).build();

    // AsNumber
    private static final AsNumber AS_NUMBER = new AsNumber(300L);
    private static final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp
            .address.address.AsNumber AS_NUMBER_LISP_TYPE = new AsNumberBuilder().setAsNumber(AS_NUMBER).build();
    private static final LispAddress LISP_AS_NUMBER = new EidBuilder().setAddress(AS_NUMBER_LISP_TYPE).build();

    // NoAddress
    private static final NoAddress NO_ADDRESS = new NoAddressBuilder().setNoAddress(true).build();
    private static final LispAddress LISP_NO_ADDRESS = new EidBuilder().setAddress(NO_ADDRESS).build();

    // AfiList
    private static final SimpleAddress SIMPLE_ADDRESS_1 = new SimpleAddress(new IpAddress(IPV4_ADDRESS));
    private static final SimpleAddress SIMPLE_ADDRESS_2 = new SimpleAddress(new IpAddress(IPV6_ADDRESS));
    private static final List<SimpleAddress> ADDRESS_LIST = new ArrayList<>();
    private static final AfiList AFI_LIST = new AfiListBuilder().setAfiList(new org.opendaylight.yang.gen.v1.urn.ietf
            .params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiListBuilder()
            .setAddressList(ADDRESS_LIST).build()).build();

    // ApplicationData
    private static final LispAddress LISP_AFI_LIST = new EidBuilder().setAddress(AFI_LIST).build();
    private static final ApplicationData APPLICATION_DATA = new ApplicationDataBuilder().setApplicationData(new org
            .opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
            .application.data.ApplicationDataBuilder()
            .setAddress(SIMPLE_ADDRESS_1)
            .setIpTos(0)
            .setLocalPortHigh(new PortNumber(1234))
            .setLocalPortLow(new PortNumber(1111))
            .setProtocol((short)1)
            .setRemotePortHigh(new PortNumber(9999))
            .setRemotePortLow(new PortNumber(1000)).build()).build();
    private static final LispAddress LISP_APPLICATION_DATA = new EidBuilder().setAddress(APPLICATION_DATA).build();

    // ExplicitLocatorPath
    private static final Hop HOP_1 = new HopBuilder()
            .setHopId("hop_1")
            .setAddress(SIMPLE_ADDRESS_1)
            .setKey(new HopKey("hop_1"))
            .setLrsBits(new Hop.LrsBits(true, true, true)).build();
    private static final Hop HOP_2 = new HopBuilder()
            .setHopId("hop_2")
            .setAddress(SIMPLE_ADDRESS_2)
            .setKey(new HopKey("hop_2"))
            .setLrsBits(new Hop.LrsBits(true, true, true)).build();
    private static final List<Hop> HOP_LIST = new ArrayList<>();
    private static final ExplicitLocatorPath EXPLICIT_LOCATOR_PATH = new ExplicitLocatorPathBuilder()
            .setExplicitLocatorPath(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.explicit.locator.path.ExplicitLocatorPathBuilder().setHop(HOP_LIST)
                    .build()).build();
    private static final LispAddress LISP_EXPLICIT_LOCATOR_PATH = new EidBuilder().setAddress(EXPLICIT_LOCATOR_PATH)
            .build();

    // SourceDestKey
    private static final SourceDestKey SOURCE_DEST_KEY = new SourceDestKeyBuilder().setSourceDestKey(new org
            .opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
            .source.dest.key.SourceDestKeyBuilder().setSource(SIMPLE_ADDRESS_1)
            .setDest(SIMPLE_ADDRESS_2).build()).build();
    private static final LispAddress LISP_SOURCE_DEST_KEY = new EidBuilder().setAddress(SOURCE_DEST_KEY).build();

    // KeyValueAddress
    private static final KeyValueAddress KEY_VALUE_ADDRESS = new KeyValueAddressBuilder()
            .setKeyValueAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.key.value.address.KeyValueAddressBuilder().setKey(SIMPLE_ADDRESS_1)
                    .setValue(SIMPLE_ADDRESS_2).build()).build();
    private static final LispAddress LISP_KEY_VALUE_ADDRESS = new EidBuilder().setAddress(KEY_VALUE_ADDRESS).build();

    // ServicePath
    private static final ServicePathIdType SERVICE_PATH_ID_TYPE = new ServicePathIdType(123L);
    private static final ServicePath SERVICE_PATH = new ServicePathBuilder().setServicePath(new org.opendaylight.yang
            .gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path
            .ServicePathBuilder().setServiceIndex((short) 1)
            .setServicePathId(SERVICE_PATH_ID_TYPE).build()).build();
    private static final LispAddress LISP_SERVICE_PATH = new EidBuilder().setAddress(SERVICE_PATH).build();

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

    // InstanceId with Ipv4Prefix
    private static final InstanceId INSTANCE_ID_IPV4_PREFIX = new InstanceIdBuilder().setInstanceId(new org.opendaylight
            .yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.instance.id
            .InstanceIdBuilder().setAddress(new SimpleAddress(new IpPrefix(new Ipv4Prefix(IPV4_STRING + PREFIX))))
            .setIid(INSTANCE_ID_TYPE).build()).build();
    private static final LispAddress LISP_IID_IPV4_PREFIX = new EidBuilder().setAddress(INSTANCE_ID_IPV4_PREFIX)
            .build();

    // InstanceId with Ipv6Prefix
    private static final InstanceId INSTANCE_ID_IPV6_PREFIX = new InstanceIdBuilder().setInstanceId(new org.opendaylight
            .yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.instance.id
            .InstanceIdBuilder().setAddress(new SimpleAddress(new IpPrefix(new Ipv6Prefix(IPV6_STRING + PREFIX))))
            .setIid(INSTANCE_ID_TYPE).build()).build();
    private static final LispAddress LISP_IID_IPV6_PREFIX = new EidBuilder().setAddress(INSTANCE_ID_IPV6_PREFIX)
            .build();

    // InstanceId with Mac
    private static final InstanceId INSTANCE_ID_MAC = new InstanceIdBuilder().setInstanceId(new org.opendaylight
            .yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.instance.id
            .InstanceIdBuilder().setAddress(new SimpleAddress(MAC_ADDRESS))
            .setIid(INSTANCE_ID_TYPE).build()).build();
    private static final LispAddress LISP_IID_MAC = new EidBuilder().setAddress(INSTANCE_ID_MAC).build();

    // InstanceId with DistinguishedNameType
    private static final InstanceId INSTANCE_ID_DISTINGUISHED_NAME_TYPE = new InstanceIdBuilder().setInstanceId(new org
            .opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
            .instance.id.InstanceIdBuilder().setAddress(new SimpleAddress(DISTINGUISHED_NAME_TYPE))
            .setIid(INSTANCE_ID_TYPE).build()).build();
    private static final LispAddress LISP_IID_DISTINGUISHED_NAME_TYPE = new EidBuilder()
            .setAddress(INSTANCE_ID_DISTINGUISHED_NAME_TYPE).build();

    // InstanceId with AsNumber
    private static final InstanceId INSTANCE_ID_AS_NUMBER = new InstanceIdBuilder().setInstanceId(new org.opendaylight
            .yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.instance.id
            .InstanceIdBuilder().setAddress(new SimpleAddress(AS_NUMBER))
            .setIid(INSTANCE_ID_TYPE).build()).build();
    private static final LispAddress LISP_IID_AS_NUMBER = new EidBuilder().setAddress(INSTANCE_ID_AS_NUMBER).build();

    private static final XtrId XTR_ID = new XtrId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

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

        // with Ipv4Prefix
        assertEquals("[" + IID + "] " + IPV4_STRING + PREFIX, LispAddressStringifier.getString(LISP_IID_IPV4_PREFIX));

        // with Ipv6Prefix
        assertEquals("[" + IID + "] " + IPV6_STRING + PREFIX, LispAddressStringifier.getString(LISP_IID_IPV6_PREFIX));

        // with Mac
        assertEquals("[" + IID + "] " + MAC.getMac().getValue(), LispAddressStringifier.getString(LISP_IID_MAC));

        // with DistinguishedNameType
        assertEquals("[" + IID + "] " + DISTINGUISHED_NAME_TYPE.getValue(), LispAddressStringifier
                .getString(LISP_IID_DISTINGUISHED_NAME_TYPE));

        // with AsNumber
        assertEquals("[" + IID + "] AS" + AS_NUMBER.getValue(), LispAddressStringifier.getString(LISP_IID_AS_NUMBER));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with NoAddress address type.
     */
    @Test
    public void getStringTest_withNoAddress() {
        assertEquals("No Address Present", LispAddressStringifier.getString(LISP_NO_ADDRESS));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with DistinguishedName address type.
     */
    @Test
    public void getStringTest_withDistinguishedName() {
        assertEquals(DISTINGUISHED_NAME_TYPE.getValue(), LispAddressStringifier.getString(LISP_DISTINGUISHED_NAME));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with AsNumber address type.
     */
    @Test
    public void getStringTest_withAsNumber() {
        assertEquals("AS" + AS_NUMBER.getValue(), LispAddressStringifier.getString(LISP_AS_NUMBER));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with AfiList address type.
     */
    @Test
    public void getStringTest_withAfiList() {
        ADDRESS_LIST.add(SIMPLE_ADDRESS_1);
        ADDRESS_LIST.add(SIMPLE_ADDRESS_2);

        assertEquals("{" + IPV4_STRING + "," + IPV6_STRING + "}", LispAddressStringifier.getString(LISP_AFI_LIST));
    }

    private String getStringFromAppData(final ApplicationData appData) {
        StringBuilder sb = new StringBuilder();
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.application.data.ApplicationData applicationData = appData.getApplicationData();

        sb.append(applicationData.getAddress().getIpAddress().getIpv4Address().getValue())
        .append("!").append(applicationData.getIpTos())
        .append("!").append(applicationData.getProtocol())
        .append("!").append(applicationData.getLocalPortLow())
        .append("-").append(applicationData.getLocalPortHigh())
        .append("!").append(applicationData.getRemotePortLow())
        .append("-").append(applicationData.getRemotePortHigh());

        return sb.toString();
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with ApplicationData address type.
     */
    @Test
    public void getStringTest_withApplicationData() {
        assertEquals(getStringFromAppData((ApplicationData) LISP_APPLICATION_DATA.getAddress()), LispAddressStringifier
                .getString(LISP_APPLICATION_DATA));
    }

    private static String getStringFromExplicLocPath() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
        .append(IPV4_STRING)
        .append("|")
        .append("l")
        .append("p")
        .append("s")

        .append("->")

        .append(IPV6_STRING)
        .append("|")
        .append("l")
        .append("p")
        .append("s")
        .append("}");

        return sb.toString();
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with ExplicitLocatorPath address type.
     */
    @Test
    public void getStringTest_withExplicitLocatorPath() {
        HOP_LIST.add(HOP_1);
        HOP_LIST.add(HOP_2);

        assertEquals(getStringFromExplicLocPath(), LispAddressStringifier.getString(LISP_EXPLICIT_LOCATOR_PATH));
    }

    private static String getStringFromSrcDstKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(IPV4_STRING)
        .append("|")
        .append(IPV6_STRING);

        return sb.toString();
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with SourceDestKey address type.
     */
    @Test
    public void getStringTest_withSourceDestKey() {
        assertEquals(getStringFromSrcDstKey(), LispAddressStringifier.getString(LISP_SOURCE_DEST_KEY));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with KeyValueAddress address type.
     */
    @Test
    public void getStringTest_withKeyValueAddress() {
        assertEquals(IPV4_STRING + "=>" + IPV6_STRING, LispAddressStringifier.getString(LISP_KEY_VALUE_ADDRESS));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with ServicePath address type.
     */
    @Test
    public void getStringTest_withServicePath() {
        assertEquals(SERVICE_PATH_ID_TYPE.getValue() + "(" + SERVICE_PATH.getServicePath().getServiceIndex() + ")",
                LispAddressStringifier.getString(LISP_SERVICE_PATH));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with no address type.
     */
    @Test
    public void getStringTest_withoutAddress() {
        assertNull(LispAddressStringifier.getString(new EidBuilder().build()));
    }

    /**
     * Tests {@link LispAddressStringifier#getURIString} with Ipv4Prefix address type.
     */
    @Test
    public void getURIStringTest_withIpv4Prefix() {
        assertEquals("ipv4:" + IPV4_STRING + PREFIX, LispAddressStringifier.getURIString(LISP_IPV4_PREFIX));
    }

    /**
     * Tests {@link LispAddressStringifier#getURLString} with Ipv4Prefix address type.
     */
    @Test
    public void getURLStringTest_withIpv4Prefix() {
        assertEquals("ipv4:" + IPV4_STRING + PREFIX_URL, LispAddressStringifier.getURLString(LISP_IPV4_PREFIX));
    }

    /**
     * Tests {@link LispAddressStringifier#getURLString} with IID_Ipv4 address type.
     */
    @Test
    public void getURLStringTest_withInstanceId() {
        assertEquals("ipv4:" + IPV4_STRING, LispAddressStringifier.getURLString(LISP_IID_IPV4));
    }

    /**
     * Tests {@link LispAddressStringifier#getString} with XtrId.
     */
    @Test
    public void getStringTest_withXtrId() {
        assertEquals(BaseEncoding.base16().encode(XTR_ID.getValue()), LispAddressStringifier.getString(XTR_ID));
    }

    /**
     * Tests {@link LispAddressStringifier#getURIString} with XtrId.
     */
    @Test
    public void getStringURITest_withXtrId() {
        assertEquals(BaseEncoding.base16().encode(XTR_ID.getValue()), LispAddressStringifier.getURIString(XTR_ID));
    }

    /**
     * Tests {@link LispAddressStringifier#getURLString} with XtrId.
     */
    @Test
    public void getStringURLTest_withXtrId() {
        assertEquals(BaseEncoding.base16().encode(XTR_ID.getValue()), LispAddressStringifier.getURLString(XTR_ID));
    }

}
