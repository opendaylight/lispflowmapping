/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.mdsal.it.base.AbstractMdsalTestBase;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AfiListLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ApplicationDataLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.KeyValueAddressLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AfiList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiListBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.application.data.ApplicationDataBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop.LrsBits;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.GotMapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MappingKeepAlive;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.OdlLispProtoListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.RequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrReplyMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrRequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_A;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_A_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_B;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_B_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_C;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_C_RLOC_10;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_C_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_C_WP_100_1_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_C_WP_50_2_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D4;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D5;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D_DELETE_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D_WP_100_1_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D_WP_50_2_SB;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_E_SB;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class MappingServiceIntegrationTest extends AbstractMdsalTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(MappingServiceIntegrationTest.class);

    /**
     * Defines how many attempt to create instance of DatagramSocket will be done before giving up.
     */
    private static final int NUM_OF_ATTEMPTS_TO_CREATE_SOCKET = 2;

    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacketWithNotify;
    private byte[] mapRegisterPacketWithoutNotify;
    String lispBindAddress = "127.0.0.1";
    static final String ourAddress = "127.0.0.2";
    private Rloc locatorEid;
    private DatagramSocket socket;
    private byte[] mapRegisterPacketWithAuthenticationAndMapNotify;

    public static final String ODL = "org.opendaylight.controller";
    public static final String YANG = "org.opendaylight.yangtools";
    private static final int MAX_NOTIFICATION_RETRYS = 20;
    private static final MappingAuthkey NULL_AUTH_KEY = new MappingAuthkeyBuilder().setKeyType(0).build();

    // This is temporary, since the properties in the pom file are not picked up
    @Override
    public String getKarafDistro() {
        return maven()
                .groupId("org.opendaylight.lispflowmapping")
                .artifactId("distribution-karaf")
                .versionAsInProject()
                .type("zip")
                .getURL();
    }

    @Override
    public MavenUrlReference getFeatureRepo() {
        return maven()
                .groupId("org.opendaylight.lispflowmapping")
                .artifactId("features-lispflowmapping")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
    }

    @Override
    public String getFeatureName() {
        return "odl-lispflowmapping-msmr";
    }

    @Override
    public Option getLoggingOption() {
        Option option = editConfigurationFilePut(ORG_OPS4J_PAX_LOGGING_CFG,
                "log4j.logger.org.opendaylight.lispflowmapping",
                LogLevel.DEBUG.name());
        option = composite(option, super.getLoggingOption());
        return option;
    }

    @Test
    public void testLispFlowMappingFeatureLoad() {
        Assert.assertTrue(true);
    }

    @After
    public void after() {
        if (socket != null) {
            socket.close();
        }
//        if (connection != null) {
//            connection.disconnect();
//        }
    }

    @Before
    public void before() throws Exception {
        areWeReady();
        mapService.setLookupPolicy(IMappingService.LookupPolicy.NB_FIRST);
        mapService.setMappingOverwrite(true);

        locatorEid = LispAddressUtil.asIpv4Rloc("4.3.2.1");
        socket = initSocket(socket, LispMessage.PORT_NUM);

        // SRC: 127.0.0.1:58560 to 127.0.0.1:4342
        // LISP(Type = 8 - Encapsulated)
        // IP: 192.168.136.10 -> 153.16.254.1
        // UDP: 56756
        // LISP(Type = 1 Map-Request
        // Record Count: 1
        // ITR-RLOC count: 0
        // Source EID AFI: 1
        // Source EID 1.2.3.4
        // Nonce: 0x3d8d2acd39c8d608
        // ITR-RLOC AFI=1 Address=192.168.136.10
        // Record 1: 153.16.254.1/32
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " //
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 3c d4 31 00 00 ff 11 56 f3 7f 00 00 02 99 10 "
                + "0040   fe 01 dd b4 10 f6 00 28 ef 3a 10 00 00 01 3d 8d "
                + "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 00 02 00 20 " //
                + "0060   00 01 99 10 fe 01"));

        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: 0
        // Key ID: 0x0001
        // AuthDataLength: 20 Data:
        // e8:f5:0b:c5:c5:f2:b0:21:27:a8:21:41:04:f3:46:5a:a5:68:89:ec
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight:
        // 255/0
        //

        mapRegisterPacketWithAuthenticationAndMapNotify = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));

        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: 7
        // Key ID: 0x0000 NO AUTHENTICATION!!
        // AuthDataLength: 00 Data:
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight:
        // 255/0
        //

        mapRegisterPacketWithNotify = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 07 00 00 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));

        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: 7
        // Key ID: 0x0000 NO AUTHENTICATION!!
        // AuthDataLength: 00 Data:
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight:
        // 255/0
        //

        mapRegisterPacketWithoutNotify = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 00 01 00 00 "
                + "0030   00 00 00 00 00 07 00 00 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
    }

    @Inject
    private BundleContext bc;
    //private HttpURLConnection connection;
    protected static boolean notificationCalled;

    @Inject @Filter(timeout=60000)
    private IFlowMapping lms;

    @Inject @Filter(timeout=60000)
    private IMappingService mapService;

    @Inject @Filter(timeout=10000)
    private IConfigLispSouthboundPlugin configLispPlugin;

    @Test
    public void testSimpleUsage() throws Exception {
        mapRequestSimple();
        mapRegisterWithMapNotify();
        mapRegisterWithMapNotifyAndMapRequest();
        registerAndQuery__MAC();
        mapRequestMapRegisterAndMapRequest();
        mapRegisterWithAuthenticationWithoutConfiguringAKey();
        mapRegisterWithoutMapNotify();
    }

    @Test
    public void testLCAFs() throws Exception {
        registerAndQuery__SrcDestLCAF();
        registerAndQuery__SrcDestLCAFOverlap();
        registerAndQuery__KeyValueLCAF();
        //registerAndQuery__ListLCAF();
        //registerAndQuery__ApplicationData();
        //registerAndQuery__TrafficEngineering();
        //registerAndQuery__SegmentLCAF();
    }

    @Test
    public void testMask() throws Exception {
        //testPasswordExactMatch();                     TODO commented because it needs NB
        //testPasswordMaskMatch();                      TODO commented because it needs NB
        eidPrefixLookupIPv4();
        eidPrefixLookupIPv6();
    }
/*
    @Test
    public void testNorthbound() throws Exception {
        northboundAddKey();
        northboundAddMapping();
        northboundDeleteMapping();
        northboundRetrieveKey();
        northboundRetrieveMapping();
        northboundRetrieveSourceDestKey();
        northboundRetrieveSourceDestMapping();
    }
*/
    @Test
    public void testOverWriting() throws Exception {
        //testMapRegisterDosntOverwritesOtherSubKeys(); TODO weird failure, needs debug

        // TODO: remove, we don't support overwrite flag any longer and RLOCs are not saved as independent RLOC groups
        // testMapRegisterOverwritesSameSubkey();
        // testMapRegisterOverwritesNoSubkey();
        // testMapRegisterDoesntOverwritesNoSubkey();
    }

    @Test
    public void testTimeOuts() throws Exception {
        mapRequestMapRegisterAndMapRequestTestTimeout();
        //mapRequestMapRegisterAndMapRequestTestNativelyForwardTimeoutResponse();   TODO commented because it needs NB
    }

//    @Test
//    public void testNonProxy() throws Throwable {
//        testSimpleNonProxy();
//        testNonProxyOtherPort();
//        testRecievingNonProxyOnXtrPort();
//    }

    @Test
    public void testSmr() throws Exception {
        registerQueryRegisterWithSmr();
        testRepeatedSmr();
    }

    @Ignore
    @Test
    public void testMultiSite() throws Exception {
        testMultiSiteScenarioA();
        testMultiSiteScenarioB();
    }

    @Test
    public void testNegativePrefix() throws UnknownHostException {
        insertMappings();
        testGapIntersection();
        testMultipleMappings();
    }

    private void testRepeatedSmr() throws SocketTimeoutException, UnknownHostException {
        cleanUP();
        long timeout = ConfigIni.getInstance().getSmrTimeout();

        final InstanceIdType iid = new InstanceIdType(1L);
        final Eid eid1 = LispAddressUtil.asIpv4Eid("1.1.1.1", 1L);
        final Eid eid2 = LispAddressUtil.asIpv4Eid("2.2.2.2", 1L);

        /* set auth */
        final Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("0.0.0.0/0", iid);
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);

        /* add subscribers */
        final String subscriberSrcRloc1 = "127.0.0.3";
        final String subscriberSrcRloc2 = "127.0.0.4";
        final Set<SubscriberRLOC> subscriberSet = Sets.newHashSet(
                newSubscriber(eid1, subscriberSrcRloc1), newSubscriber(eid2, subscriberSrcRloc2));
        mapService.addData(MappingOrigin.Southbound, eid1, SubKeys.SUBSCRIBERS, subscriberSet);

        final int expectedSmrs1 = 2;
        final int expectedSmrs2 = 3;

        final SocketReader reader1 = startSocketReader(subscriberSrcRloc1, 15000);
        final SocketReader reader2 = startSocketReader(subscriberSrcRloc2, 15000);
        sleepForSeconds(1);

        /* add mapping */
        final MappingRecord mapping1 = new MappingRecordBuilder()
                .setEid(eid1).setTimestamp(System.currentTimeMillis()).setRecordTtl(1440).build();
        mapService.addMapping(MappingOrigin.Northbound, mapping1.getEid(), null, mapping1, false);

        sleepForMilliseconds((timeout * expectedSmrs1) - 1500);
        final List<MapRequest> requests1 = processBuffers(reader1, subscriberSrcRloc1, expectedSmrs1);
        final MapReply mapReply1 = lms.handleMapRequest(
                new MapRequestBuilder(requests1.get(0))
                        .setItrRloc(Lists.newArrayList(new ItrRlocBuilder()
                                .setRloc(LispAddressUtil.asIpv4Rloc(subscriberSrcRloc1)).build()))
                        .setSmrInvoked(true)
                        .setSmr(false).build());

        // sleep to get 1 extra smr request
        sleepForMilliseconds(timeout * 1);
        final List<MapRequest> requests2 = processBuffers(reader2, subscriberSrcRloc2, expectedSmrs2);
        final MapReply mapReply2 = lms.handleMapRequest(
                new MapRequestBuilder(requests2.get(0))
                        .setItrRloc(Lists.newArrayList(new ItrRlocBuilder()
                                .setRloc(LispAddressUtil.asIpv4Rloc(subscriberSrcRloc2)).build()))
                        .setSmrInvoked(true)
                        .setSmr(false).build());

        sleepForSeconds(3);
        assertEquals(expectedSmrs1, requests1.size());
        assertEquals(expectedSmrs2, requests2.size());
        assertEquals((long) mapReply1.getNonce(), (long) requests1.get(0).getNonce());
        assertEquals((long) mapReply2.getNonce(), (long) requests1.get(0).getNonce());
        assertNextBufferEmpty(reader1);
        assertNextBufferEmpty(reader2);

        reader1.stopReading();
        reader2.stopReading();
    }

    private SocketReader startSocketReader(String address, int timeout) {
        DatagramSocket receivingSocket = null;

        try {
            receivingSocket = new DatagramSocket(new InetSocketAddress(address, LispMessage.PORT_NUM));
        } catch (SocketException e) {
            LOG.error("Can't initialize socket for {}", address, e);
        }
        return SocketReader.startReadingInStandaloneThread(receivingSocket, timeout);
    }

    private List<MapRequest> processBuffers(SocketReader reader, String address, int expectedSmrs) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            LOG.error("Unknown address {}.", address, e);
        }

        final List<MapRequest> requests = Lists.newArrayList();
        byte[][] buffers = reader.getBuffers(expectedSmrs);
        for (byte[] buf : buffers) {
            MapRequest request = MapRequestSerializer.getInstance().deserialize(ByteBuffer.wrap(buf), inetAddress);
            requests.add(request);
        }
        return requests;
    }

    private void assertNextBufferEmpty(SocketReader socketReader) {
        assertTrue(isArrayEmpty(socketReader.getBuffers(1)[0]));
    }

    private boolean isArrayEmpty(byte[] byteArray) {
        for (byte b : byteArray) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private static SubscriberRLOC newSubscriber(Eid srcEid, String srcRlocIp) {
        final byte[] addressBinary = InetAddresses.forString(srcRlocIp).getAddress();
        final int timeout = 5;
        final Rloc srcRloc = new RlocBuilder().setAddress(new Ipv4BinaryBuilder()
                .setIpv4Binary(new Ipv4AddressBinary(addressBinary)).build()).build();

        return new SubscriberRLOC(srcRloc, srcEid, timeout);
    }

    private void testMultipleMappings() throws UnknownHostException {
        final InstanceIdType iid = new InstanceIdType(1L);
        final String prefix1 = "1.1.127.10/32"; // prefix from the intersection of NB and SB gaps
        final String prefix2 = "1.1.200.255/32"; // prefix with existing mapping in NB
        final String prefix3 = "1.3.255.255/32";

        final MapRequest mapRequest = new MapRequestBuilder().setSmrInvoked(false).setEidItem(Lists.newArrayList(
                new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixBinaryEid(prefix1, iid))
                        .build(),
                new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixBinaryEid(prefix2, iid))
                        .build(),
                new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixBinaryEid(prefix3, iid))
                        .build()))
                .build();
        final MapReply mapReply = lms.handleMapRequest(mapRequest);

        // expected result
        final String resultPrefix1 = "1.1.64.0";
        final Address resultNegMapping1 = new Ipv4PrefixBinaryBuilder()
                .setIpv4AddressBinary(new Ipv4AddressBinary(InetAddress.getByName(resultPrefix1).getAddress()))
                .setIpv4MaskLength((short) 18).build();

        final String resultPrefix2 = "1.1.192.0";
        final Address resultMapping2 = new Ipv4PrefixBinaryBuilder()
                .setIpv4AddressBinary(new Ipv4AddressBinary(InetAddress.getByName(resultPrefix2).getAddress()))
                .setIpv4MaskLength((short) 18).build();

        final String resultPrefix3 = "1.3.0.0";
        final Address resultNegMapping3 = new Ipv4PrefixBinaryBuilder()
                .setIpv4AddressBinary(new Ipv4AddressBinary(InetAddress.getByName(resultPrefix3).getAddress()))
                .setIpv4MaskLength((short) 16).build();

        assertEquals(resultNegMapping1, mapReply.getMappingRecordItem().get(0).getMappingRecord().getEid()
                .getAddress());
        assertEquals(resultMapping2, mapReply.getMappingRecordItem().get(1).getMappingRecord().getEid()
                .getAddress());
        assertEquals(resultNegMapping3, mapReply.getMappingRecordItem().get(2).getMappingRecord().getEid()
                .getAddress());
    }

    /**
     * Tests a negative mapping from an intersection of gaps in northbound and southbound.
     */
    private void testGapIntersection() throws UnknownHostException {
        final InstanceIdType iid = new InstanceIdType(1L);

        // request an Eid from a gap between mappings
        final MapRequest mapRequest = new MapRequestBuilder().setSmrInvoked(false).setEidItem(Lists.newArrayList(
                new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixBinaryEid("1.1.127.10/32", iid))
                        .build()))
                .build();
        final MapReply mapReply = lms.handleMapRequest(mapRequest);

        // expected negative mapping
        final Address resultNegMapping = new Ipv4PrefixBinaryBuilder()
                .setIpv4AddressBinary(new Ipv4AddressBinary(InetAddress.getByName("1.1.64.0").getAddress()))
                .setIpv4MaskLength((short) 18).build();
        assertEquals(resultNegMapping, mapReply.getMappingRecordItem().get(0).getMappingRecord().getEid()
                .getAddress());
    }

    private void insertMappings() {
        cleanUP();
        mapService.setLookupPolicy(IMappingService.LookupPolicy.NB_AND_SB);

        final InstanceIdType iid = new InstanceIdType(1L);
        final String prefixNbLeft = "1.2.0.0/16";
        final String prefixNbRight = "1.1.128.0/17";
        final String prefixSbLeft = "1.1.32.0/19";
        final String prefixSbRight = "1.0.0.0/8";

        final MappingRecord mapRecordNbLeft = newMappingRecord(prefixNbLeft, iid);
        final MappingRecord mapRecordNbRight = newMappingRecord(prefixNbRight, iid);
        final MappingRecord mapRecordSbLeft = newMappingRecord(prefixSbLeft, iid);
        final MappingRecord mapRecordSbRight = newMappingRecord(prefixSbRight, iid);

        /* set auth */
        final Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("0.0.0.0/0", iid);
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);

        mapService.addMapping(MappingOrigin.Northbound, mapRecordNbLeft.getEid(), null, mapRecordNbLeft, false);
        mapService.addMapping(MappingOrigin.Northbound, mapRecordNbRight.getEid(), null, mapRecordNbRight, false);
        mapService.addMapping(MappingOrigin.Southbound, mapRecordSbLeft.getEid(), null, mapRecordSbLeft, false);
        mapService.addMapping(MappingOrigin.Southbound, mapRecordSbRight.getEid(), null, mapRecordSbRight, false);

        restartSocket();
        sleepForSeconds(2);
    }

    /**
     * Creates a new MappingRecord object.
     *
     * @param prefix The Eid prefix
     * @param iid VNI
     * @return new MappingRecord object
     */
    private MappingRecord newMappingRecord(String prefix, InstanceIdType iid) {
        final Eid prefixBinary = LispAddressUtil.asIpv4PrefixBinaryEid(prefix, iid);
        return new MappingRecordBuilder()
                .setEid(prefixBinary)
                .setLocatorRecord(Lists.newArrayList(new LocatorRecordBuilder()
                        .setRloc(LispAddressUtil.asIpv4Rloc("2.2.2.2")).setLocatorId("loc_id").build()))
                .setTimestamp(System.currentTimeMillis()).setRecordTtl(1440).build();
    }

    /**
     * TEST SCENARIO A
     */
    public void testMultiSiteScenarioA() throws IOException {
        cleanUP();

        final MultiSiteScenario multiSiteScenario = new MultiSiteScenario(mapService, lms);
        multiSiteScenario.setCommonAuthentication();

        restartSocket();
        final SocketReader socketReader = SocketReader.startReadingInStandaloneThread(socket);

        //TEST CASE 1
        multiSiteScenario.storeSouthboundMappings(false, SITE_A, SITE_B, SITE_C, SITE_D4, SITE_D5);
        multiSiteScenario.storeNorthMappingSrcDst(SITE_B, SITE_C);
        multiSiteScenario.storeNorthMappingNegative(SITE_C, Action.Drop);
        sleepForSeconds(2);
        multiSiteScenario.assertPingWorks(SITE_A, 5, SITE_B, 4);
        multiSiteScenario.assertPingWorks(SITE_B, 5, SITE_C, 4);
        multiSiteScenario.assertPingFails(SITE_A, 1, SITE_C, 4);

        //TEST CASE 2
        //following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.1/32
        multiSiteScenario.storeNorthMappingSrcDst(SITE_A, SITE_C);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B.getHost(5), SITE_A.getHost(1));
        multiSiteScenario.assertPingWorks(SITE_A, 5, SITE_C, 4);
        multiSiteScenario.assertPingWorks(SITE_B, 5, SITE_C, 4);
        multiSiteScenario.assertPingFails(SITE_D4, 5, SITE_C, 4);

        //TEST CASE 3
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.1/32
        // 3) 192.0.1.5/32
        // 4) 192.0.4.5/32
        multiSiteScenario.deleteNorthMappingNegative(SITE_C);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B.getHost(5), SITE_A.getHost(1), SITE_A
                        .getHost(5),
                SITE_D4.getHost(5));
        multiSiteScenario.assertPingWorks(SITE_D4, 5, SITE_C, 4);

        //TEST CASE 4
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.4.5/32
        multiSiteScenario.storeNorthMappingSrcDst(SITE_B, SITE_C_RLOC_10);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_D4.getHost(5));
        //way of testing ping - get RLOC for mapping src-dst and compare it with awaited value doesn't test
        //that ping won't be successfull
        multiSiteScenario.assertPingFails(SITE_B, 5, SITE_C, 4);

        //TEST CASE 5
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.4.5/32
        // 2) 192.0.2.5/32
        multiSiteScenario.storeNorthMappingNegative(SITE_C, Action.Drop);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_D4.getHost(5), SITE_B.getHost(5));
        multiSiteScenario.assertPingFails(SITE_D4, 5, SITE_C, 4);

        //TEST CASE 6
        multiSiteScenario.assertPingFails(SITE_D5, 5, SITE_C, 3);

        //TEST CASE 7
        multiSiteScenario.deleteNorthMapingSrcDst(SITE_A, SITE_C);
        sleepForSeconds(2);
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.4.5/32
        // 2) 192.0.2.5/32
        // 3) 192.0.5.5/32
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_D5.getHost(5), SITE_D4.getHost(5),
                SITE_B.getHost(5));

        // following action should trigger generatting of SMR messages:
        // 1) 192.0.4.5/32
        // 2) 192.0.2.5/32
        // 3) 192.0.5.5/32
        multiSiteScenario.storeNorthMappingSrcDst(SITE_B, SITE_C);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_D5.getHost(5), SITE_D4.getHost(5),
                SITE_B.getHost(5));

        multiSiteScenario.assertPingWorks(SITE_A, 5, SITE_B, 4);
        multiSiteScenario.assertPingWorks(SITE_B, 5, SITE_C, 4);
        multiSiteScenario.assertPingFails(SITE_A, 1, SITE_C, 4);

        //TEST CASE 8
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.4.5/32
        // 2) 192.0.2.5/32
        // 3) 192.0.5.5/32
        // 4) 192.0.1.1/32
        multiSiteScenario.deleteNorthMapingSrcDst(SITE_B, SITE_C);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_D5.getHost(5), SITE_D4.getHost(5),
                SITE_B.getHost(5),
                SITE_A.getHost(1));
        multiSiteScenario.assertPingWorks(SITE_A, 5, SITE_B, 4);
        multiSiteScenario.assertPingFails(SITE_B, 5, SITE_C, 4);
        multiSiteScenario.assertPingFails(SITE_A, 1, SITE_C, 4);

        //TEST CASE 9
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.4.5/32
        // 2) 192.0.2.5/32
        // 3) 192.0.5.5/32
        // 4) 192.0.1.1/32
        multiSiteScenario.deleteNorthMappingNegative(SITE_C);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_D5.getHost(5), SITE_D4.getHost(5),
                SITE_B.getHost(5),
                SITE_A.getHost(1));
        multiSiteScenario.assertPingWorks(SITE_A, 5, SITE_B, 4);
        multiSiteScenario.assertPingWorks(SITE_B, 5, SITE_C, 4);
        multiSiteScenario.assertPingWorks(SITE_A, 5, SITE_C, 4);

        socketReader.stopReading();

    }

    /**
     * TEST SCENARIO B
     */
    public void testMultiSiteScenarioB() throws IOException {
        cleanUP();

        final MultiSiteScenario multiSiteScenario = new MultiSiteScenario(mapService, lms);
        multiSiteScenario.setCommonAuthentication();

        restartSocket();
        final SocketReader socketReader = SocketReader.startReadingInStandaloneThread(socket);

        mapService.setLookupPolicy(IMappingService.LookupPolicy.NB_AND_SB);
        mapService.setMappingOverwrite(false);

        //TEST CASE 1
        multiSiteScenario.storeSouthboundMappings(true, SITE_A_SB, SITE_B_SB, SITE_C_WP_100_1_SB, SITE_D_WP_100_1_SB,
                SITE_E_SB);
        multiSiteScenario.storeNorthMappingIpPrefix(SITE_A_SB);
        multiSiteScenario.storeNorthMappingIpPrefix(SITE_B_SB);
        multiSiteScenario.storeNorthMappingIpPrefix(SITE_C_WP_50_2_SB, SITE_D_WP_50_2_SB);
        sleepForSeconds(2);
        multiSiteScenario.assertPingWorks(SITE_A_SB, 5, SITE_C_WP_50_2_SB, 4, SITE_D_WP_50_2_SB);
        multiSiteScenario.assertPingWorks(SITE_B_SB, 5, SITE_C_WP_50_2_SB, 4, SITE_D_WP_50_2_SB);

        //TEST CASE 2
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.5/32
        multiSiteScenario.storeNorthMappingSrcDst(SITE_A_SB, SITE_C_WP_50_2_SB, SITE_D_WP_50_2_SB);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B_SB.getHost(5), SITE_A_SB.getHost(5));

        // following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.5/32
        multiSiteScenario.storeNorthMappingNegative(SITE_C_SB, Action.Drop);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B_SB.getHost(5), SITE_A_SB.getHost(5));

        multiSiteScenario.assertPingWorks(SITE_A_SB, 5, SITE_C_WP_50_2_SB, 4, SITE_D_WP_50_2_SB);
        multiSiteScenario.assertPingFails(SITE_B_SB, 5, SITE_C_SB, 4);


        //TEST CASE 3
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.5/32
        multiSiteScenario.storeNorthMappingSrcDst(SITE_A_SB, SITE_C_WP_50_2_SB);
        sleepForSeconds(2);
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B_SB.getHost(5), SITE_A_SB.getHost(5));
        multiSiteScenario.assertPingWorks(SITE_A_SB, 5, SITE_C_WP_50_2_SB, 4);

        //TEST CASE 4
        multiSiteScenario.storeNorthMappingSrcDst(SITE_B_SB, SITE_C_WP_50_2_SB, SITE_D_WP_50_2_SB);
        sleepForSeconds(2);
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.5/32
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B_SB.getHost(5), SITE_A_SB.getHost(5));
        multiSiteScenario.assertPingWorks(SITE_B_SB, 5, SITE_C_WP_50_2_SB, 4, SITE_D_WP_50_2_SB);

        //TEST CASE 5
        multiSiteScenario.deleteSouthboundMappings(SITE_D_DELETE_SB);
        sleepForSeconds(2);
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.5/32
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B_SB.getHost(5), SITE_A_SB.getHost(5));

        multiSiteScenario.assertPingWorks(SITE_B_SB, 5, SITE_C_WP_50_2_SB, 4);

        //TEST CASE 6
        multiSiteScenario.deleteNorthMapingSrcDst(SITE_A_SB, SITE_C_WP_50_2_SB);
        sleepForSeconds(2);
        // following action should trigger generatting of SMR messages:
        // 1) 192.0.2.5/32
        // 2) 192.0.1.5/32
        multiSiteScenario.checkSMR(socketReader, SITE_C.getEidPrefix(), SITE_B_SB.getHost(5), SITE_A_SB.getHost(5));

        multiSiteScenario.deleteNorthMapingSrcDst(SITE_B_SB, SITE_C_WP_50_2_SB);
        sleepForSeconds(2);
        multiSiteScenario.assertPingFails(SITE_B_SB, 5, SITE_C_WP_50_2_SB, 4);

        socketReader.stopReading();

    }

    // ------------------------------- Simple Tests ---------------------------

    public void mapRequestSimple() throws SocketTimeoutException {
        cleanUP();

        // This Map-Request is sent from a source port different from 4342
        // We close and bind the socket on the correct port
        if (socket != null) {
            socket.close();
        }
        socket = initSocket(socket, 56756);

        sendPacket(mapRequestPacket);
        ByteBuffer readBuf = ByteBuffer.wrap(receivePacket().getData());
        MapReply reply = MapReplySerializer.getInstance().deserialize(readBuf);
        assertEquals(4435248268955932168L, reply.getNonce().longValue());

    }

    public void mapRegisterWithMapNotify() throws SocketTimeoutException {
        cleanUP();
        mapService.addAuthenticationKey(LispAddressUtil.asIpv4PrefixBinaryEid("153.16.254.1/32"), NULL_AUTH_KEY);

        sleepForSeconds(2);
        sendPacket(mapRegisterPacketWithNotify);
        MapNotify reply = receiveMapNotify();
        assertEquals(7, reply.getNonce().longValue());
    }

    public void mapRegisterWithMapNotifyAndMapRequest() throws SocketTimeoutException {
        cleanUP();
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");

        MapReply mapReply = registerAddressAndQuery(eid);

        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(locatorEid, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getRloc());

    }

    public void registerAndQuery__MAC() throws SocketTimeoutException {
        cleanUP();
        String macAddress = "01:02:03:04:05:06";

        MapReply reply = registerAddressAndQuery(LispAddressUtil.asMacEid(macAddress));

        assertTrue(true);
        Eid addressFromNetwork = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();
        assertEquals(MacAfi.class, addressFromNetwork.getAddressType());
        String macAddressFromReply = ((Mac) addressFromNetwork.getAddress()).getMac().getValue();

        assertEquals(macAddress, macAddressFromReply);
    }

    public void mapRequestMapRegisterAndMapRequest() throws SocketTimeoutException {
        cleanUP();
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);

        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.getNoAddressEid()).build());
        mapRequestBuilder.setEidItem(new ArrayList<EidItem>());
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(eid).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(ourAddress)).build());
        sendMapRequest(mapRequestBuilder.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(0, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());
        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);
        MappingRecordBuilder etlrBuilder = new MappingRecordBuilder();
        etlrBuilder.setEid(eid);
        etlrBuilder.setRecordTtl(254);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegisterbuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(etlrBuilder.build()).build());
        sendMapRegister(mapRegisterbuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        sleepForSeconds(1);
        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(recordBuilder.getRloc(), mapReply.getMappingRecordItem().get(0).getMappingRecord()
                .getLocatorRecord().get(0).getRloc());

    }

    public void testMapRegisterDosntOverwritesOtherSubKeys() throws SocketTimeoutException {
        cleanUP();
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");
        SimpleAddress rloc1Value = new SimpleAddress(new IpAddress(new Ipv4Address("4.3.2.1")));
        Rloc rloc1 = LispAddressUtil.asKeyValueAddress("subkey1", rloc1Value);
        SimpleAddress rloc2Value = new SimpleAddress(new IpAddress(new Ipv4Address("4.3.2.2")));
        Rloc rloc2 = LispAddressUtil.asKeyValueAddress("subkey2", rloc2Value);
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1, rloc2);
        assertEquals(2, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());
        assertEquals(rloc2, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getRloc());
        assertEquals(rloc1, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1)
                .getRloc());
    }

    public void testMapRegisterOverwritesSameSubkey() throws SocketTimeoutException {
        cleanUP();
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");
        SimpleAddress rloc1Value = new SimpleAddress(new IpAddress(new Ipv4Address("4.3.2.1")));
        Rloc rloc1 = LispAddressUtil.asKeyValueAddress("subkey1", rloc1Value);
        SimpleAddress rloc2Value = new SimpleAddress(new IpAddress(new Ipv4Address("4.3.2.2")));
        Rloc rloc2 = LispAddressUtil.asKeyValueAddress("subkey2", rloc2Value);
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1, rloc2);
        assertEquals(1, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());
        assertEquals(rloc2, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getRloc());
    }

    public void testMapRegisterOverwritesNoSubkey() throws SocketTimeoutException {
        cleanUP();
        mapService.setMappingOverwrite(true);
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");
        Rloc rloc1Value = LispAddressUtil.asIpv4Rloc("4.3.2.1");
        Rloc rloc2Value = LispAddressUtil.asIpv4Rloc("4.3.2.2");
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1Value, rloc2Value);
        assertEquals(1, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());
        assertEquals(rloc2Value, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getRloc());
    }

    public void testMapRegisterDoesntOverwritesNoSubkey() throws SocketTimeoutException {
        cleanUP();
        mapService.setMappingOverwrite(false);
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");
        Rloc rloc1Value = LispAddressUtil.asIpv4Rloc("4.3.2.1");
        Rloc rloc2Value = LispAddressUtil.asIpv4Rloc("4.3.2.2");
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1Value, rloc2Value);
        assertEquals(1, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());
        Rloc rloc1ReturnValueContainer = mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord()
                .get(0).getRloc();
        Rloc rloc2ReturnValueContainer = mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord()
                .get(1).getRloc();
        assertTrue((rloc1Value.equals(rloc1ReturnValueContainer) && rloc2Value.equals(rloc2ReturnValueContainer))
                || (rloc1Value.equals(rloc2ReturnValueContainer) && rloc2Value.equals(rloc1ReturnValueContainer)));
    }

    private MapReply sendMapRegisterTwiceWithDiffrentValues(Eid eid, Rloc rloc1, Rloc rloc2)
            throws SocketTimeoutException {
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);
        MapRegister mb = createMapRegister(eid, rloc1);
        MapNotify mapNotify = lms.handleMapRegister(mb).getLeft();
        MapRequest mr = createMapRequest(eid);
        MapReply mapReply = lms.handleMapRequest(mr);
        assertEquals(mb.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0).getRloc(),
                mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0).getRloc());
        mb = createMapRegister(eid, rloc2);
        mapNotify = lms.handleMapRegister(mb).getLeft();
        assertEquals(8, mapNotify.getNonce().longValue());
        mr = createMapRequest(eid);
        sendMapRequest(mr);
        mapReply = lms.handleMapRequest(mr);
        return mapReply;
    }

    public void mapRegisterWithAuthenticationWithoutConfiguringAKey() throws SocketTimeoutException {
        cleanUP();
        sendPacket(mapRegisterPacketWithAuthenticationAndMapNotify);
        try {
            receivePacket(3000);
            // If didn't timeout then fail:
            fail();
        } catch (SocketTimeoutException ste) {
        }
    }

    public void mapRegisterWithoutMapNotify() {
        cleanUP();
        sendPacket(mapRegisterPacketWithoutNotify);
        try {
            receivePacket(3000);
            // If didn't timeout then fail:
            fail();
        } catch (SocketTimeoutException ste) {
        }
    }

    public void registerQueryRegisterWithSmr() throws SocketTimeoutException {
        cleanUP();
        lms.setShouldUseSmr(true);
        mapService.addAuthenticationKey(LispAddressUtil.asIpv4PrefixBinaryEid("153.16.254.1/32"), NULL_AUTH_KEY);
        sleepForSeconds(1);

        sendPacket(mapRegisterPacketWithNotify);
        receiveMapNotify();

        sleepForSeconds(1);
        sendPacket(mapRequestPacket);
        sleepForSeconds(1);

        mapRegisterPacketWithoutNotify[mapRegisterPacketWithoutNotify.length - 1] += 1;
        sendPacket(mapRegisterPacketWithoutNotify);

        ByteBuffer readBuf = ByteBuffer.wrap(receivePacket().getData());
        MapRequest smr = MapRequestSerializer.getInstance().deserialize(readBuf, null);
        assertTrue(smr.isSmr());
        Eid sourceEid = smr.getSourceEid().getEid();
        assertTrue(LispAddressUtil.asIpv4Eid("153.16.254.1").equals(sourceEid));
        Eid smrEid = smr.getEidItem().get(0).getEid();
        assertTrue(LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32").equals(smrEid));
    }

    // --------------------- Northbound Tests ---------------------------
/*
    private void northboundAddKey() throws Exception {
        cleanUP();
        LispIpv4Address address = LispAddressUtil.asIPAfiAddress("1.2.3.4");
        int mask = 32;
        String pass = "asdf";

        URL url = createPutURL("key");
        String authKeyJSON = createAuthKeyJSON(pass, address, mask);
        callURL("PUT", "application/json", "text/plain", authKeyJSON, url);

        String retrievedKey = lms.getAuthenticationKey(LispAddressUtil.toContainer(address), mask);

        // Check stored password matches the one sent
        assertEquals(pass, retrievedKey);

    }

    private void northboundRetrieveSourceDestKey() throws Exception {
        cleanUP();
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispsimpleaddress.primitiveaddress.Ipv4
                address1 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress
                .primitiveaddress.Ipv4) LispAddressUtil
                .toPrimitive(LispAddressUtil.asIPAfiAddress("10.0.0.1"));
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispsimpleaddress.primitiveaddress.Ipv4
                address2 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress
                .primitiveaddress.Ipv4) LispAddressUtil
                .toPrimitive(LispAddressUtil.asIPAfiAddress("10.0.0.2"));
        int mask1 = 32;
        int mask2 = 32;
        LcafSourceDestAddr sourceDestAddress = new LcafSourceDestAddrBuilder().setAfi(
                AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode())
                .setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(address1).build())
                .setSrcMaskLength((short) mask1)
                .setDstAddress(new DstAddressBuilder().setPrimitiveAddress(address2).build())
                .setDstMaskLength((short) mask2).build();
        String pass = "asdf";

        lms.addAuthenticationKey(LispAddressUtil.toContainer(sourceDestAddress), mask1, pass);

        // URL url = createGetKeyIPv4URL(address1, mask1);
        URL url = createGetKeySourceDestURL(address1.getIpv4Address().getAfi(),
                ((LispIpv4Address) LispAddressUtil.toAFIfromPrimitive(sourceDestAddress.getSrcAddress()
                .getPrimitiveAddress())).getIpv4Address().getValue(), sourceDestAddress.getSrcMaskLength(),
                ((LispIpv4Address) LispAddressUtil.toAFIfromPrimitive(sourceDestAddress.getDstAddress()
                .getPrimitiveAddress())).getIpv4Address().getValue(), sourceDestAddress.getDstMaskLength());
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // test that the password matches what was we expected.
        assertEquals(pass, json.get("key"));

    }

    private void northboundRetrieveKey() throws Exception {
        cleanUP();
        LispIpv4Address address = LispAddressUtil.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        String pass = "asdf";

        lms.addAuthenticationKey(LispAddressUtil.toContainer(address), mask, pass);

        URL url = createGetKeyIPv4URL(address, mask);
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // test that the password matches what was we expected.
        assertEquals(pass, json.get("key"));

    }

    private String createAuthKeyJSON(String key, LispIpv4Address address, int mask) {
        return "{\"key\" : \"" + key + "\",\"maskLength\" : " + mask + ",\"address\" : " + "{\"ipAddress\" : \""
                + address.getIpv4Address().getValue() + "\",\"afi\" : " + address.getAfi().shortValue() + "}}";
    }

    private void northboundAddMapping() throws Exception {
        cleanUP();
        String pass = "asdf";
        LispIpv4Address eid = LispAddressUtil.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAddressUtil.asIPAfiAddress("20.0.0.2");

        // NB add mapping always checks the key
        lms.addAuthenticationKey(LispAddressUtil.toContainer(eid), mask, pass);

        URL url = createPutURL("mapping");
        String mapRegisterJSON = createMapRegisterJSON(pass, eid, mask, rloc);
        callURL("PUT", "application/json", "text/plain", mapRegisterJSON, url);

        // Retrieve the RLOC from the database
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setPitr(false);
        mapRequestBuilder.setEidItem(new ArrayList<EidItem>());
        mapRequestBuilder.getEidItem().add(
                new EidRecordBuilder().setMask((short) mask).setLispAddressContainer(
                LispAddressUtil.toContainer(eid)).build());
        MapReply mapReply = lms.handleMapRequest(mapRequestBuilder.build());

        LispIpv4Address retrievedRloc = (LispIpv4Address) LispAddressUtil.toAFI(
                mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getLispAddressContainer());

        assertEquals(rloc.getIpv4Address().getValue(), retrievedRloc.getIpv4Address().getValue());

    }

    private String createMapRegisterJSON(String key, LispIpv4Address eid, int mask, LispIpv4Address rloc) {
        String jsonString = "{ " + "\"key\" : \"" + key + "\"," + "\"mapregister\" : " + "{ "
                + "\"proxyMapReply\" : false, "
                + "\"eidToLocatorRecords\" : " + "[ " + "{ " + "\"authoritative\" : true," + "\"prefixGeneric\" : "
                + "{ " + "\"ipAddress\" : \""
                + eid.getIpv4Address().getValue() + "\"," + "\"afi\" : " + eid.getAfi().shortValue() + "},"
                + "\"mapVersion\" : 0,"
                + "\"maskLength\" : " + mask + ", " + "\"action\" : \"NoAction\"," + "\"locators\" : " + "[ " + "{ "
                + "\"multicastPriority\" : 1,"
                + "\"locatorGeneric\" : " + "{ " + "\"ipAddress\" : \"" + rloc.getIpv4Address().getValue() + "\","
                + "\"afi\" : "
                + rloc.getAfi().shortValue() + "}, " + "\"routed\" : true," + "\"multicastWeight\" : 50,"
                + "\"rlocProbed\" : false, "
                + "\"localLocator\" : false, " + "\"priority\" : 1, " + "\"weight\" : 50 " + "} " + "], "
                + "\"recordTtl\" : 100" + "} " + "], "
                + "\"nonce\" : 3," + "\"keyId\" : 0 " + "} " + "}";

        return jsonString;
    }

    private void northboundRetrieveMapping() throws Exception {
        cleanUP();
        LispIpv4Address eid = LispAddressUtil.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAddressUtil.asIPAfiAddress("20.0.0.2");
        // Insert mapping in the database
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAddressUtil.toContainer(eid));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        etlr.setAuthoritative(false);
        etlr.setAction(Action.NoAction);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAddressUtil.toContainer(rloc));
        record.setRouted(true);
        record.setRlocProbed(false);
        record.setLocalLocator(false);
        record.setPriority((short) 1);
        record.setWeight((short) 50);
        record.setMulticastPriority((short) 1);
        record.setMulticastWeight((short) 1);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegister.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlr.build()).build());
        lms.handleMapRegister(mapRegister.build());

        // Get mapping using NB interface. No IID used
        URL url = createGetMappingIPv4URL(0, eid, mask);
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // With just one locator, locators is not a JSONArray
        String rlocRetrieved = json.getJSONArray("locators").getJSONObject(0).getJSONObject("locatorGeneric")
                .getString("ipAddress");

        assertEquals(rloc.getIpv4Address().getValue(), rlocRetrieved);

    }

    private void northboundDeleteMapping() throws Exception {
        cleanUP();
        LispIpv4Address eid = LispAddressUtil.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAddressUtil.asIPAfiAddress("20.0.0.2");
        // Insert mapping in the database
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAddressUtil.toContainer(eid));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        etlr.setAuthoritative(false);
        etlr.setAction(Action.NoAction);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAddressUtil.toContainer(rloc));
        record.setRouted(true);
        record.setRlocProbed(false);
        record.setLocalLocator(false);
        record.setPriority((short) 1);
        record.setWeight((short) 50);
        record.setMulticastPriority((short) 1);
        record.setMulticastWeight((short) 1);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegister.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlr.build()).build());
        lms.handleMapRegister(mapRegister.build());

        // Delete mapping using NB interface. No IID used
        URL url = createDeleteMappingIPv4URL(0, eid, mask);
        String reply = callURL("DELETE", null, "application/json", null, url);

        // Get mapping using NB interface. No IID used
        url = createGetMappingIPv4URL(0, eid, mask);
        reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // With just one locator, locators is not a JSONArray
        assertEquals(json.getJSONArray("locators").length(), 0);
    }

    private void northboundRetrieveSourceDestMapping() throws Exception {
        cleanUP();
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispsimpleaddress.primitiveaddress.Ipv4
                address1 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress
                .primitiveaddress.Ipv4) LispAddressUtil
                .toPrimitive(LispAddressUtil.asIPAfiAddress("10.0.0.1"));
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispsimpleaddress.primitiveaddress.Ipv4
                address2 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress
                .primitiveaddress.Ipv4) LispAddressUtil
                .toPrimitive(LispAddressUtil.asIPAfiAddress("10.0.0.2"));
        int mask1 = 32;
        int mask2 = 32;
        LcafSourceDestAddr sourceDestAddress = new LcafSourceDestAddrBuilder().setAfi(
                AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode())
                .setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(
                        address1).build()).setSrcMaskLength((short) mask1)
                .setDstAddress(new DstAddressBuilder().setPrimitiveAddress(
                        address2).build()).setDstMaskLength((short) mask2).build();
        LispIpv4Address rloc = LispAddressUtil.asIPAfiAddress("20.0.0.2");

        // Insert mapping in the database
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAddressUtil.toContainer(sourceDestAddress));
        etlr.setMaskLength((short) mask1);
        etlr.setRecordTtl(254);
        etlr.setAuthoritative(false);
        etlr.setAction(Action.NoAction);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAddressUtil.toContainer(rloc));
        record.setRouted(true);
        record.setRlocProbed(false);
        record.setLocalLocator(false);
        record.setPriority((short) 1);
        record.setWeight((short) 50);
        record.setMulticastPriority((short) 1);
        record.setMulticastWeight((short) 1);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegister.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlr.build()).build());
        lms.handleMapRegister(mapRegister.build());

        // Get mapping using NB interface. No IID used
        URL url = createGetMappingSourceDestURL(address1.getIpv4Address().getAfi(),
                address1.getIpv4Address().getIpv4Address().getValue(),
                mask1,
                address2.getIpv4Address().getIpv4Address().getValue(),
                mask2);
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // With just one locator, locators is not a JSONArray
        String rlocRetrieved = json.getJSONArray("locators").getJSONObject(0).getJSONObject("locatorGeneric")
        .getString("ipAddress");

        assertEquals(rloc.getIpv4Address().getValue(), rlocRetrieved);

    }

    private URL createGetKeyIPv4URL(LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/0/%d/%s/%d", "key",
                address.getAfi().shortValue(),
                address.getIpv4Address().getValue(), mask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createGetKeySourceDestURL(int afi, String srcAddress, int srcMask, String dstAddress, int dstMask)
            throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/0/%d/%s/%d/%s/%d",
                "key", afi, srcAddress, srcMask,
                dstAddress, dstMask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createGetMappingSourceDestURL(int afi, String srcAddress, int srcMask, String dstAddress, int dstMask)
            throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/0/%d/%s/%d/%s/%d",
                "mapping", afi, srcAddress,
                srcMask, dstAddress, dstMask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createGetMappingIPv4URL(int iid, LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/%d/%d/%s/%d", "mapping",
                iid, address.getAfi()
                .shortValue(), address.getIpv4Address().getValue(), mask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createDeleteMappingIPv4URL(int iid, LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/%d/%d/%s/%d", "mapping",
                iid, address.getAfi()
                .shortValue(), address.getIpv4Address().getValue(), mask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createPutURL(String resource) throws MalformedURLException {

        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s", resource);

        URL url = new URL(restUrl);
        return url;
    }

    private String createAuthenticationString() {
        String authString = "admin:admin";
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        return authStringEnc;
    }

    private String callURL(String method, String content, String accept, String body, URL url) throws IOException,
            JSONException {
        String authStringEnc = createAuthenticationString();
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        if (content != null) {
            connection.setRequestProperty("Content-Type", content);
        }
        if (accept != null) {
            connection.setRequestProperty("Accept", accept);
        }
        if (body != null) {
            // now add the request body
            connection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(body);
            wr.flush();
        }
        connection.connect();

        // getting the result, first check response code
        Integer httpResponseCode = connection.getResponseCode();

        if (httpResponseCode > 299) {
            LOG.trace("HTTP Address: " + url);
            LOG.trace("HTTP Response Code: " + httpResponseCode);
            fail();
        }

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        is.close();
        connection.disconnect();
        return (sb.toString());
    }

    // timePeriod - in ms
    public void assertNoPacketReceived(int timePeriod) {
        try {
            receivePacket(timePeriod);
            // If didn't timeout then fail:
            fail();
        } catch (SocketTimeoutException ste) {
        }
    }
*/
    // ------------------------------- Mask Tests ---------------------------

    public void eidPrefixLookupIPv4() throws SocketTimeoutException {
        cleanUP();
        runPrefixTest(LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/16"),
                LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.2/32"),
                LispAddressUtil.asIpv4PrefixBinaryEid("1.1.1.1/32"));
    }

    public void eidPrefixLookupIPv6() throws SocketTimeoutException {
        cleanUP();
        runPrefixTest(LispAddressUtil.asIpv6PrefixBinaryEid("1:2:3:4:5:6:7:8/64"),
                LispAddressUtil.asIpv6PrefixBinaryEid("1:2:3:4:5:1:2:3/128"),
                LispAddressUtil.asIpv6PrefixBinaryEid("1:2:3:1:2:3:1:2/128"));
    }

    private void runPrefixTest(Eid registerEID, Eid matchedAddress, Eid unMatchedAddress)
            throws SocketTimeoutException {
        mapService.addAuthenticationKey(registerEID, NULL_AUTH_KEY);
        sleepForSeconds(1);

        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        mapRegister.setWantMapNotify(true);
        mapRegister.setKeyId((short) 0);
        mapRegister.setAuthenticationData(new byte[0]);
        mapRegister.setNonce((long) 8);
        mapRegister.setProxyMapReply(false);
        MappingRecordBuilder etlr = new MappingRecordBuilder();
        etlr.setRecordTtl(254);
        etlr.setAction(Action.NoAction);
        etlr.setAuthoritative(false);
        etlr.setMapVersion((short) 0);
        etlr.setEid(registerEID);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record.setLocalLocator(false);
        record.setRlocProbed(false);
        record.setRouted(true);
        record.setMulticastPriority((short) 0);
        record.setMulticastWeight((short) 0);
        record.setPriority((short) 0);
        record.setWeight((short) 0);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegister.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlr.build()).build());
        sendMapRegister(mapRegister.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        sleepForSeconds(1);
        MapRequestBuilder mapRequest = new MapRequestBuilder();
        mapRequest.setNonce((long) 4);
        mapRequest.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(ourAddress)).build());
        mapRequest.setEidItem(new ArrayList<EidItem>());
        mapRequest.setAuthoritative(false);
        mapRequest.setMapDataPresent(false);
        mapRequest.setPitr(false);
        mapRequest.setProbe(false);
        mapRequest.setSmr(false);
        mapRequest.setSmrInvoked(false);
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(matchedAddress).build());
        mapRequest.setItrRloc(new ArrayList<ItrRloc>());
        mapRequest.getItrRloc().add(
                new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(ourAddress)).build());
        sendMapRequest(mapRequest.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(record.getRloc(), mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord()
                .get(0).getRloc());
        mapRequest.setEidItem(new ArrayList<EidItem>());
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(unMatchedAddress).build());
        sendMapRequest(mapRequest.build());
        mapReply = receiveMapReply();
        assertEquals(0, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());
    }
/*
    // This registers an IP with a MapRegister, then adds a password via the
    // northbound REST API
    // and checks that the password works
    public void testPasswordExactMatch() throws Exception {
        cleanUP();
        String ipString = "10.0.0.1";
        LispIpv4Address address = LispAddressUtil.asIPAfiAddress(ipString);
        int mask = 32;
        String pass = "pass";

        URL url = createPutURL("key");

        String jsonAuthData = createAuthKeyJSON(pass, address, mask);

        LOG.trace("Sending this JSON to LISP server: \n" + jsonAuthData);
        LOG.trace("Address: " + address);

        byte[] expectedSha = new byte[] { (byte) 146, (byte) 234, (byte) 52, (byte) 247, (byte) 186, (byte) 232,
                (byte) 31, (byte) 249, (byte) 87,
                (byte) 73, (byte) 234, (byte) 54, (byte) 225, (byte) 160, (byte) 129, (byte) 251, (byte) 73, (byte) 53,
                (byte) 196, (byte) 62 };

        byte[] zeros = new byte[20];

        callURL("PUT", "application/json", "text/plain", jsonAuthData, url);

        // build a MapRegister
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAddressUtil.toContainer(address));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAddressUtil.toContainer(locatorEid));
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegister.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlr.build()).build());

        mapRegister.setKeyId((short) 1); // LispKeyIDEnum.SHA1.getKeyID()
        mapRegister.setAuthenticationData(zeros);

        sendMapRegister(mapRegister.build());
        assertNoPacketReceived(3000);

        mapRegister.setAuthenticationData(expectedSha);

        sendMapRegister(mapRegister.build());

        assertMapNotifyReceived();
    }

    public void testPasswordMaskMatch() throws Exception {
        cleanUP();
        LispIpv4Address addressInRange = LispAddressUtil.asIPAfiAddress("10.20.30.40");
        LispIpv4Address addressOutOfRange = LispAddressUtil.asIPAfiAddress("20.40.30.40");
        LispIpv4Address range = LispAddressUtil.asIPAfiAddress("10.20.30.0");

        int mask = 32;
        String pass = "pass";

        URL url = createPutURL("key");
        String jsonAuthData = createAuthKeyJSON(pass, range, 8);

        callURL("PUT", "application/json", "text/plain", jsonAuthData, url);
        // build a MapRegister
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();

        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAddressUtil.toContainer(addressInRange));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAddressUtil.toContainer(locatorEid));
        record.setLispAddressContainer(LispAddressUtil.toContainer(locatorEid));
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegister.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlr.build()).build());

        mapRegister.setKeyId((short) 1); // LispKeyIDEnum.SHA1.getKeyID()
        mapRegister
                .setAuthenticationData(new byte[] { -15, -52, 38, -94, 125, -111, -68, -79, 68, 6, 101, 45, -1, 47, -4,
                -67, -113, 104, -110, -71 });

        sendMapRegister(mapRegister.build());

        assertMapNotifyReceived();

        etlr.setLispAddressContainer(LispAddressUtil.toContainer(addressOutOfRange));
        mapRegister
                .setAuthenticationData(new byte[] { -54, 68, -58, -91, -23, 22, -88, -31, 113, 39, 115, 78, -68, -123,
                -71, -14, -99, 67, -23, -73 });

        sendMapRegister(mapRegister.build());
        assertNoPacketReceived(3000);
    }
*/
    // takes an address, packs it in a MapRegister and sends it
    private void registerAddress(Eid eid) throws SocketTimeoutException {
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);
        MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setWantMapNotify(true);
        mapRegisterBuilder.setKeyId((short) 0);
        mapRegisterBuilder.setAuthenticationData(new byte[0]);
        mapRegisterBuilder.setNonce((long) 8);
        mapRegisterBuilder.setProxyMapReply(false);
        MappingRecordBuilder etlrBuilder = new MappingRecordBuilder();
        etlrBuilder.setEid(eid);
        etlrBuilder.setRecordTtl(254);
        etlrBuilder.setAction(Action.NoAction);
        etlrBuilder.setAuthoritative(false);
        etlrBuilder.setMapVersion((short) 0);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLocalLocator(false);
        recordBuilder.setRlocProbed(false);
        recordBuilder.setRouted(true);
        recordBuilder.setMulticastPriority((short) 0);
        recordBuilder.setMulticastWeight((short) 0);
        recordBuilder.setPriority((short) 0);
        recordBuilder.setWeight((short) 0);
        recordBuilder.setRloc(locatorEid);
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegisterBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(etlrBuilder.build()).build());
        sendMapRegister(mapRegisterBuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
    }

    private MapReply queryForAddress(Eid eid, String srcEid) throws SocketTimeoutException {
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setEidItem(new ArrayList<EidItem>());
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(eid).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        if (srcEid != null) {
            mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(srcEid)).build());
        } else {
            mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(ourAddress))
                    .build());
        }
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(ourAddress)).build());
        mapRequestBuilder.setAuthoritative(false);
        mapRequestBuilder.setMapDataPresent(false);
        mapRequestBuilder.setPitr(false);
        mapRequestBuilder.setProbe(false);
        mapRequestBuilder.setSmr(false);
        mapRequestBuilder.setSmrInvoked(false);
        sendMapRequest(mapRequestBuilder.build());
        return receiveMapReply();
    }

    // takes an address, packs it in a MapRegister, sends it, returns the
    // MapReply
    private MapReply registerAddressAndQuery(Eid eid) throws SocketTimeoutException {
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);
        MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setWantMapNotify(true);
        mapRegisterBuilder.setKeyId((short) 0);
        mapRegisterBuilder.setAuthenticationData(new byte[0]);
        mapRegisterBuilder.setNonce((long) 8);
        mapRegisterBuilder.setProxyMapReply(false);
        MappingRecordBuilder etlrBuilder = new MappingRecordBuilder();
        etlrBuilder.setEid(eid);
        etlrBuilder.setRecordTtl(254);
        etlrBuilder.setAction(Action.NoAction);
        etlrBuilder.setAuthoritative(false);
        etlrBuilder.setMapVersion((short) 0);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLocalLocator(false);
        recordBuilder.setRlocProbed(false);
        recordBuilder.setRouted(true);
        recordBuilder.setMulticastPriority((short) 0);
        recordBuilder.setMulticastWeight((short) 0);
        recordBuilder.setPriority((short) 0);
        recordBuilder.setWeight((short) 0);
        recordBuilder.setRloc(locatorEid);
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(
                etlrBuilder.build()).build());
        sendMapRegister(mapRegisterBuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        // wait for the notifications to propagate
        sleepForSeconds(1);
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setEidItem(new ArrayList<EidItem>());
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(eid).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(ourAddress)).build());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(ourAddress)).build());
        mapRequestBuilder.setAuthoritative(false);
        mapRequestBuilder.setMapDataPresent(false);
        mapRequestBuilder.setPitr(false);
        mapRequestBuilder.setProbe(false);
        mapRequestBuilder.setSmr(false);
        mapRequestBuilder.setSmrInvoked(false);
        sendMapRequest(mapRequestBuilder.build());
        return receiveMapReply();
    }

    // ------------------------------- LCAF Tests ---------------------------

    public void registerAndQuery__SrcDestLCAF() throws SocketTimeoutException {
        cleanUP();
        String ipPrefix = "10.20.30.200/32";
        String macString = "01:02:03:04:05:06";

        SourceDestKeyBuilder builder = new SourceDestKeyBuilder();
        builder.setSource(new SimpleAddress(new IpPrefix(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang
                .ietf.inet.types.rev130715.Ipv4Prefix(ipPrefix))));
        builder.setDest(new SimpleAddress(new MacAddress(macString)));

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(SourceDestKeyLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.SourceDestKeyBuilder().setSourceDestKey(builder.build()).build());

        MapReply reply = registerAddressAndQuery(eb.build());

        Eid fromNetwork = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();
        assertEquals(SourceDestKeyLcaf.class, fromNetwork.getAddressType());
        SourceDestKey sourceDestFromNetwork = (SourceDestKey) fromNetwork.getAddress();

        SimpleAddress receivedAddr1 = sourceDestFromNetwork.getSourceDestKey().getSource();
        SimpleAddress receivedAddr2 = sourceDestFromNetwork.getSourceDestKey().getDest();

        assertNotNull(receivedAddr1.getIpPrefix().getIpv4Prefix());
        assertNotNull(receivedAddr2.getMacAddress());

        IpPrefix receivedIP = receivedAddr1.getIpPrefix();
        MacAddress receivedMAC = receivedAddr2.getMacAddress();

        assertEquals(ipPrefix, receivedIP.getIpv4Prefix().getValue());
        assertEquals(macString, receivedMAC.getValue());
    }

    public void registerAndQuery__SrcDestLCAFOverlap() throws SocketTimeoutException {
        cleanUP();
        String ipString1 = "10.10.10.0";
        String ipString2 = "20.20.20.0";
        String ipPrefix1 = ipString1 + "/24";
        String ipPrefix2 = ipString2 + "/24";

        Eid srcDst = LispAddressUtil.asSrcDstEid(ipString1, ipString2, 24, 24, 0);
        registerAddress(LispAddressUtil.asIpv4PrefixBinaryEid(ipPrefix2));
        registerAddress(srcDst);

        // exact match
        MapReply reply = queryForAddress(srcDst, null);

        Eid fromNetwork = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();
        assertEquals(SourceDestKeyLcaf.class, fromNetwork.getAddressType());
        SourceDestKey sourceDestFromNetwork = (SourceDestKey) fromNetwork.getAddress();

        IpPrefix receivedAddr1 = sourceDestFromNetwork.getSourceDestKey().getSource().getIpPrefix();
        IpPrefix receivedAddr2 = sourceDestFromNetwork.getSourceDestKey().getDest().getIpPrefix();

        assertNotNull(receivedAddr1.getIpv4Prefix());
        assertNotNull(receivedAddr2.getIpv4Prefix());

        assertEquals(ipPrefix1, receivedAddr1.getIpv4Prefix().getValue());
        assertEquals(ipPrefix2, receivedAddr2.getIpv4Prefix().getValue());

        // srcEid/dstEid match
        reply = queryForAddress(LispAddressUtil.asIpv4PrefixBinaryEid("20.20.20.1/32"), "10.10.10.1");
        fromNetwork = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();
        assertEquals(Ipv4PrefixBinaryAfi.class, fromNetwork.getAddressType());

        assertEquals(LispAddressUtil.asIpv4PrefixBinaryEid(ipPrefix2), fromNetwork);

        // dstEid match only
        reply = queryForAddress(LispAddressUtil.asIpv4PrefixBinaryEid("20.20.20.1/32"), "1.2.3.4");
        fromNetwork = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();
        assertEquals(Ipv4PrefixBinaryAfi.class, fromNetwork.getAddressType());

        assertEquals(LispAddressUtil.asIpv4PrefixBinaryEid(ipPrefix2), fromNetwork);
    }

    public void registerAndQuery__KeyValueLCAF() throws SocketTimeoutException {
        cleanUP();
        String ipString = "10.20.30.200";
        String macString = "01:02:03:04:05:06";
        SimpleAddress addrToSend1 = new SimpleAddress(new IpAddress(new Ipv4Address(ipString)));
        SimpleAddress addrToSend2 = new SimpleAddress(new MacAddress(macString));
        Eid kv = LispAddressUtil.asKeyValueAddressEid(addrToSend1, addrToSend2);

        MapReply reply = registerAddressAndQuery(kv);

        Eid fromNetwork = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();
        assertEquals(KeyValueAddressLcaf.class, fromNetwork.getAddressType());
        KeyValueAddress keyValueFromNetwork = (KeyValueAddress) fromNetwork.getAddress();

        SimpleAddress receivedAddr1 = keyValueFromNetwork.getKeyValueAddress().getKey();
        SimpleAddress receivedAddr2 = keyValueFromNetwork.getKeyValueAddress().getValue();

        assertNotNull(receivedAddr1.getIpAddress().getIpv4Address());
        assertNotNull(receivedAddr2.getMacAddress());

        Ipv4Address receivedIP = receivedAddr1.getIpAddress().getIpv4Address();
        MacAddress receivedMAC = receivedAddr2.getMacAddress();

        assertEquals(ipString, receivedIP.getValue());
        assertEquals(macString, receivedMAC.getValue());
    }

    public void registerAndQuery__ListLCAF() throws SocketTimeoutException {
        cleanUP();
        String macString = "01:02:03:04:05:06";
        String ipString = "10.20.255.30";
        List<SimpleAddress> addresses = new ArrayList<SimpleAddress>();
        addresses.add(new SimpleAddress(new IpAddress(new Ipv4Address(ipString))));
        addresses.add(new SimpleAddress(new MacAddress(macString)));
        AfiListBuilder listbuilder = new AfiListBuilder();
        listbuilder.setAddressList(addresses);

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(AfiListLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.AfiListBuilder().setAfiList(listbuilder.build()).build());

        MapReply reply = registerAddressAndQuery(eb.build());

        Eid receivedAddress = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();

        assertEquals(AfiListLcaf.class, receivedAddress.getAddressType());

        AfiList listAddrFromNetwork = (AfiList) receivedAddress.getAddress();
        SimpleAddress receivedAddr1 = (SimpleAddress) listAddrFromNetwork.getAfiList().getAddressList().get(0);
        SimpleAddress receivedAddr2 = (SimpleAddress) listAddrFromNetwork.getAfiList().getAddressList().get(1);

        assertNotNull(receivedAddr1.getIpAddress().getIpv4Address());
        assertNotNull(receivedAddr2.getMacAddress());

        assertEquals(macString, receivedAddr2.getMacAddress().getValue());
        assertEquals(ipString, receivedAddr1.getIpAddress().getIpv4Address().getValue());
    }

    public void registerAndQuery__SegmentLCAF() throws SocketTimeoutException {
        cleanUP();
        String ipString = "10.20.255.30";
        int instanceId = 6;

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(Ipv4PrefixAfi.class);
        eb.setVirtualNetworkId(new InstanceIdType((long) instanceId));
        eb.setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns
                .yang.ietf.inet.types.rev130715.Ipv4Prefix(ipString)).build());

        MapReply reply = registerAddressAndQuery(eb.build());

        Eid receivedAddress = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();
        assertEquals(Ipv4PrefixAfi.class, receivedAddress.getAddressType());

        assertEquals(ipString, ((Ipv4Prefix) receivedAddress.getAddress()).getIpv4Prefix().getValue());

        assertEquals(instanceId, receivedAddress.getVirtualNetworkId().getValue().intValue());
    }

    public void registerAndQuery__TrafficEngineering() throws SocketTimeoutException {
        cleanUP();
        String macString = "01:02:03:04:05:06";
        String ipString = "10.20.255.30";
        HopBuilder hopBuilder = new HopBuilder();
        hopBuilder.setAddress(new SimpleAddress(new IpAddress(new Ipv4Address(ipString))));
        hopBuilder.setLrsBits(new LrsBits(true, false, true));
        Hop hop1 = hopBuilder.build();
        hopBuilder.setAddress(new SimpleAddress(new MacAddress(macString)));
        hopBuilder.setLrsBits(new LrsBits(false, true, false));
        Hop hop2 = hopBuilder.build();
        ExplicitLocatorPathBuilder elpBuilder = new ExplicitLocatorPathBuilder();
        elpBuilder.setHop(new ArrayList<Hop>());
        elpBuilder.getHop().add(hop1);
        elpBuilder.getHop().add(hop2);

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ExplicitLocatorPathLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ExplicitLocatorPathBuilder().setExplicitLocatorPath(elpBuilder.build()).build());

        MapReply reply = registerAddressAndQuery(eb.build());

        assertEquals(ExplicitLocatorPathLcaf.class, reply.getMappingRecordItem().get(0).getMappingRecord().getEid()
                .getAddressType());

        ExplicitLocatorPath receivedAddress = (ExplicitLocatorPath) reply.getMappingRecordItem().get(0)
                .getMappingRecord().getEid().getAddress();

        Hop receivedHop1 = (Hop) receivedAddress.getExplicitLocatorPath().getHop().get(0);
        Hop receivedHop2 = (Hop) receivedAddress.getExplicitLocatorPath().getHop().get(1);

        assertEquals(true, receivedHop1.getLrsBits().isLookup());
        assertEquals(false, receivedHop1.getLrsBits().isRlocProbe());
        assertEquals(true, receivedHop1.getLrsBits().isStrict());

        assertEquals(false, receivedHop2.getLrsBits().isLookup());
        assertEquals(true, receivedHop2.getLrsBits().isRlocProbe());
        assertEquals(false, receivedHop2.getLrsBits().isStrict());

        assertNotNull(receivedHop1.getAddress().getIpAddress().getIpv4Address());
        assertNotNull(receivedHop2.getAddress().getMacAddress());

        assertEquals(ipString, receivedHop1.getAddress().getIpAddress().getIpv4Address().getValue());
        assertEquals(macString, receivedHop2.getAddress().getMacAddress().getValue());
    }

    public void registerAndQuery__ApplicationData() throws SocketTimeoutException {
        cleanUP();
        String ipString = "1.2.3.4";
        short protocol = 1;
        int ipTOs = 2;
        int localPortLow = 3;
        int localPortHigh = 4;
        int remotePortLow = 4;
        int remotePortHigh = 5;

        ApplicationDataBuilder builder = new ApplicationDataBuilder();
        builder.setIpTos(ipTOs);
        builder.setProtocol(protocol);
        builder.setLocalPortLow(new PortNumber(localPortLow));
        builder.setLocalPortHigh(new PortNumber(localPortHigh));
        builder.setRemotePortLow(new PortNumber(remotePortLow));
        builder.setRemotePortHigh(new PortNumber(remotePortHigh));
        builder.setAddress(new SimpleAddress(new IpAddress(new Ipv4Address(ipString))));

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ApplicationDataLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ApplicationDataBuilder().setApplicationData(builder.build()).build());
        Eid addressToSend = eb.build();

        MapReply reply = registerAddressAndQuery(addressToSend);

        Eid receivedAddress = reply.getMappingRecordItem().get(0).getMappingRecord().getEid();

        assertEquals(ApplicationDataLcaf.class, receivedAddress.getAddressType());

        ApplicationData receivedApplicationDataAddress = (ApplicationData) receivedAddress.getAddress();
        assertEquals(protocol, receivedApplicationDataAddress.getApplicationData().getProtocol().intValue());
        assertEquals(ipTOs, receivedApplicationDataAddress.getApplicationData().getIpTos().intValue());
        assertEquals(localPortLow, receivedApplicationDataAddress.getApplicationData().getLocalPortLow().getValue()
                .intValue());
        assertEquals(localPortHigh, receivedApplicationDataAddress.getApplicationData().getLocalPortHigh().getValue()
                .intValue());
        assertEquals(remotePortLow, receivedApplicationDataAddress.getApplicationData().getRemotePortLow().getValue()
                .intValue());
        assertEquals(remotePortHigh, receivedApplicationDataAddress.getApplicationData().getRemotePortHigh().getValue()
                .intValue());

        SimpleAddress ipAddressReceived = receivedApplicationDataAddress.getApplicationData().getAddress();
        assertEquals(ipString, ipAddressReceived.getIpAddress().getIpv4Address().getValue());
    }

    // ------------------- TimeOut Tests -----------

    public void mapRequestMapRegisterAndMapRequestTestTimeout() throws SocketTimeoutException {
        cleanUP();
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.getNoAddressEid()).build());
        mapRequestBuilder.setEidItem(new ArrayList<EidItem>());
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(eid).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(ourAddress)).build());

        sendMapRequest(mapRequestBuilder.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(0, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());

        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);

        MappingRecordBuilder etlrBuilder = new MappingRecordBuilder();
        etlrBuilder.setEid(eid);
        etlrBuilder.setRecordTtl(254);

        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegisterbuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(etlrBuilder.build()).build());

        sendMapRegister(mapRegisterbuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        sleepForSeconds(1);

        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(recordBuilder.getRloc(), mapReply.getMappingRecordItem().get(0).getMappingRecord()
                .getLocatorRecord().get(0).getRloc());

        causeEntryToBeCleaned();
        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(0, mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().size());
    }

    public void mapRequestMapRegisterAndMapRequestTestNativelyForwardTimeoutResponse() throws Exception {
        cleanUP();
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32");
        MapRequest mapRequest = createMapRequest(eid);

        testTTLBeforeRegister(mapRequest);

        registerForTTL(eid);

        testTTLAfterRegister(mapRequest);

        causeEntryToBeCleaned();
        testTTLAfterClean(mapRequest);

        //northboundAddKey();
        //testTTLAfterAutherize(mapRequest);

    }

    private void testTTLAfterClean(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertCorrectMapReplyTTLAndAction(mapReply, 15, Action.NativelyForward);
    }

    private void causeEntryToBeCleaned() {
        // TODO XXX for the time being, to keep master and stable/lithium in sync, we need to remove the forceful
        // expiration of DAO entries. Once we're past this, we'll have to expose methods to setTimeUnit(TimeUnit)
        // and cleanOld() (expired) entries in IFlowMapping (and perhaps ILispDAO) and use them here.
        mapService.cleanCachedMappings();
    }

    private void testTTLAfterRegister(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertEquals(LispAddressUtil.asIpv4Rloc("4.3.2.1"), mapReply.getMappingRecordItem().get(0).getMappingRecord()
                .getLocatorRecord().get(0).getRloc());
        assertCorrectMapReplyTTLAndAction(mapReply, 254, Action.NoAction);
    }

    private void registerForTTL(Eid eid) throws SocketTimeoutException {
        MapRegister mapRegister = createMapRegister(eid);
        sendMapRegister(mapRegister);
        assertMapNotifyReceived();
    }

    private void testTTLBeforeRegister(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertCorrectMapReplyTTLAndAction(mapReply, 15, Action.NativelyForward);
    }
/*
    private void testTTLAfterAutherize(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertCorrectMapReplyTTLAndAction(mapReply, 1, Action.NativelyForward);
    }
*/
    private void assertCorrectMapReplyTTLAndAction(MapReply mapReply, int expectedTTL, Action expectedAction) {
        assertEquals(expectedTTL, mapReply.getMappingRecordItem().get(0).getMappingRecord().getRecordTtl().intValue());
        assertEquals(expectedAction, mapReply.getMappingRecordItem().get(0).getMappingRecord().getAction());
    }

    private MapRegister createMapRegister(Eid eid, Rloc rloc) {
        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);
        mapRegisterbuilder.setKeyId((short) 0);
        MappingRecordBuilder etlrBuilder = new MappingRecordBuilder();
        etlrBuilder.setEid(eid);
        etlrBuilder.setRecordTtl(254);
        etlrBuilder.setAuthoritative(false);
        etlrBuilder.setAction(Action.NoAction);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setRloc(rloc);
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegisterbuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(etlrBuilder.build()).build());
        MapRegister mapRegister = mapRegisterbuilder.build();
        return mapRegister;
    }

    private MapRegister createMapRegister(Eid eid) {
        return createMapRegister(eid, LispAddressUtil.asIpv4Rloc("4.3.2.1"));
    }

    private MapRequest createMapRequest(Eid eid) {
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setPitr(false);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.getNoAddressEid()).build());
        mapRequestBuilder.setEidItem(new ArrayList<EidItem>());
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(eid).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(ourAddress)).build());
        MapRequest mr = mapRequestBuilder.build();
        return mr;
    }

    public void testSimpleNonProxy() throws SocketTimeoutException, SocketException {
        cleanUP();
        String rloc = "127.0.0.3";
        int port = LispMessage.PORT_NUM;
        Rloc ipRloc = LispAddressUtil.asIpv4Rloc(rloc);
        sendProxyMapRequest(rloc, port, ipRloc);

    }

    public void testNonProxyOtherPort() throws SocketTimeoutException, SocketException {
        cleanUP();
        String rloc = "127.0.0.3";
        int port = 4350;

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(ApplicationDataLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ApplicationDataBuilder()
                .setApplicationData(new ApplicationDataBuilder().setAddress(new SimpleAddress(new IpAddress(
                new Ipv4Address(rloc)))).setLocalPortLow(new PortNumber(port)).build()).build());
        Rloc adLcaf = rb.build();

        LOG.info("testNonProxyOtherPort:" + LispAddressStringifier.getString(adLcaf));
        sendProxyMapRequest(rloc, port, adLcaf);

    }

    private class XtrRequestMappingListener implements OdlLispProtoListener {

        @Override
        public void onGotMapReply(GotMapReply notification) {
        }

        @Override
        public void onAddMapping(AddMapping notification) {
        }

        @Override
        public void onXtrReplyMapping(XtrReplyMapping notification) {
        }

        @Override
        public void onRequestMapping(RequestMapping notification) {
        }

        @Override
        public void onGotMapNotify(GotMapNotify notification) {
        }

        @Override
        public void onXtrRequestMapping(XtrRequestMapping notification) {
        }

        @Override
        public void onMappingKeepAlive(MappingKeepAlive notification) {
        }

    }

    public void testRecievingNonProxyOnXtrPort() throws SocketTimeoutException, SocketException, Throwable {
        cleanUP();
        configLispPlugin.shouldListenOnXtrPort(true);
        notificationCalled = false;
        final String eid = "10.10.10.10/32";
        String rloc = "127.0.0.3";
        int port = LispMessage.XTR_PORT_NUM;

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(ApplicationDataLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ApplicationDataBuilder()
                .setApplicationData(new ApplicationDataBuilder().setAddress(new SimpleAddress(new IpAddress(
                new Ipv4Address(rloc)))).setLocalPortLow(new PortNumber(port)).build()).build());
        Rloc adLcaf = rb.build();

        final MapRequest mapRequest = createNonProxyMapRequest(eid, adLcaf);
        ((LispMappingService) lms).getNotificationService().registerNotificationListener(
                new XtrRequestMappingListener() {

            @Override
            public void onXtrRequestMapping(XtrRequestMapping notification) {
                assertEquals(((Ipv4Prefix) mapRequest.getEidItem().get(0).getEid().getAddress())
                        .getIpv4Prefix().getValue(), eid);
                notificationCalled = true;
                LOG.warn("notification arrived");
            }
        });
        sendMapRequest(mapRequest, port);
        for (int i = 0; i < MAX_NOTIFICATION_RETRYS; i++) {
            if (notificationCalled) {
                return;
            } else {
                LOG.warn("notification hasn't arrived, sleeping...");
                Thread.sleep(500);
            }
        }

        fail("Notification hasn't arrived");

    }

    private void sendProxyMapRequest(String rloc, int port, Rloc adLcaf) throws SocketTimeoutException,
            SocketException {
        String eid = "10.1.0.1/32";
        MapRequest mapRequest = createNonProxyMapRequest(eid, adLcaf);
        sendMapRequest(mapRequest);
        DatagramSocket nonProxySocket = new DatagramSocket(new InetSocketAddress(rloc, port));
        MapRequest receivedMapRequest = receiveMapRequest(nonProxySocket);
        assertEquals(mapRequest.getNonce(), receivedMapRequest.getNonce());
        assertEquals(mapRequest.getSourceEid(), receivedMapRequest.getSourceEid());
        assertEquals(mapRequest.getItrRloc(), receivedMapRequest.getItrRloc());
        assertEquals(mapRequest.getEidItem(), receivedMapRequest.getEidItem());
        nonProxySocket.close();
    }

    private MapRequest createNonProxyMapRequest(String eid, Rloc adLcaf) throws SocketTimeoutException {
        MapRegister mr = createMapRegister(LispAddressUtil.asIpv4PrefixBinaryEid(eid));
        LocatorRecord record = new LocatorRecordBuilder(mr.getMappingRecordItem().get(0).getMappingRecord()
                .getLocatorRecord().get(0)).setRloc(adLcaf).build();
        mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().set(0, record);
        sendMapRegister(mr);
        assertMapNotifyReceived();
        MapRequest mapRequest = createMapRequest(LispAddressUtil.asIpv4PrefixBinaryEid(eid));
        MapRequestBuilder builder = new MapRequestBuilder(mapRequest);
        builder.setPitr(true);
        mapRequest = builder.build();
        return mapRequest;
    }

    private void assertMapNotifyReceived() throws SocketTimeoutException {
        receiveMapNotify();
    }

    private MapReply receiveMapReply() throws SocketTimeoutException {
        return MapReplySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    }

    private MapRequest receiveMapRequest(DatagramSocket datagramSocket) throws SocketTimeoutException {
        return MapRequestSerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket(
                datagramSocket, 30000).getData()), null);
    }

    private MapNotify receiveMapNotify() throws SocketTimeoutException {
        return MapNotifySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    }

    private void sendMapRequest(MapRequest mapRequest) {
        sendMapRequest(mapRequest, LispMessage.PORT_NUM);
    }

    private void sendMapRequest(MapRequest mapRequest, int port) {
        sendPacket(MapRequestSerializer.getInstance().serialize(mapRequest).array(), port);
    }

    private void sendMapRegister(MapRegister mapRegister) {
        sendPacket(MapRegisterSerializer.getInstance().serialize(mapRegister).array());
    }

    private void sendPacket(byte[] bytesToSend) {
        sendPacket(bytesToSend, LispMessage.PORT_NUM);
    }

    private void sendPacket(byte[] bytesToSend, int port) {
        try {
            DatagramPacket packet = new DatagramPacket(bytesToSend, bytesToSend.length);
            initPacketAddress(packet, port);
            LOG.trace("Sending packet to LispPlugin on socket, port {}", port);
            socket.send(packet);
        } catch (Throwable t) {
            fail();
        }
    }

    private DatagramPacket receivePacket() throws SocketTimeoutException {
        return receivePacket(6000);
    }

    private DatagramPacket receivePacket(int timeout) throws SocketTimeoutException {
        return receivePacket(socket, timeout);
    }

    private DatagramPacket receivePacket(DatagramSocket receivedSocket, int timeout) throws SocketTimeoutException {
        try {
            byte[] buffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            LOG.trace("Waiting for packet from socket...");
            receivedSocket.setSoTimeout(timeout);
            receivedSocket.receive(receivePacket);
            LOG.trace("Received packet from socket!");
            return receivePacket;
        } catch (SocketTimeoutException ste) {
            throw ste;
        } catch (Throwable t) {
            fail();
            return null;
        }
    }

    private void sleepForSeconds(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while sleeping", e);
        }
    }

    private void sleepForMilliseconds(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while sleeping", e);
        }
    }

    private void initPacketAddress(DatagramPacket packet, int port) throws UnknownHostException {
        packet.setAddress(InetAddress.getByName(lispBindAddress));
        packet.setPort(port);
    }

    private DatagramSocket initSocket(DatagramSocket socket, int port) {
        for (int i=0; i < NUM_OF_ATTEMPTS_TO_CREATE_SOCKET; i++) {
            try {
                return new DatagramSocket(new InetSocketAddress(ourAddress, port));
            } catch (SocketException e) {
                LOG.error("Can't initialize socket for {}", ourAddress, e);
            }
        }
        fail();
        return null;
    }

    private byte[] extractWSUdpByteArray(String wiresharkHex) {
        final int HEADER_LEN = 42;
        byte[] res = new byte[1000];
        String[] split = wiresharkHex.split(" ");
        int counter = 0;
        for (String cur : split) {
            cur = cur.trim();
            if (cur.length() == 2) {
                ++counter;
                if (counter > HEADER_LEN) {
                    res[counter - HEADER_LEN - 1] = (byte) Integer.parseInt(cur, 16);
                }

            }
        }
        return Arrays.copyOf(res, counter - HEADER_LEN);
    }

    private String stateToString(int state) {
        switch (state) {
        case Bundle.ACTIVE:
            return "ACTIVE";
        case Bundle.INSTALLED:
            return "INSTALLED";
        case Bundle.RESOLVED:
            return "RESOLVED";
        case Bundle.UNINSTALLED:
            return "UNINSTALLED";
        default:
            return "Not CONVERTED";
        }
    }

    private void areWeReady() throws InvalidSyntaxException {
        sleepForSeconds(5);

        assertNotNull(bc);
        boolean debugit = false;
        Bundle b[] = bc.getBundles();
        for (Bundle element : b) {
            int state = element.getState();
            LOG.trace("Bundle[" + element.getBundleId() + "]:" + element.getSymbolicName() + ",v"
                    + element.getVersion() + ", state:" + stateToString(state));
            if (state != Bundle.ACTIVE && state != Bundle.RESOLVED) {
                LOG.debug("Bundle:" + element.getSymbolicName() + " state:" + stateToString(state));

                // try {
                // String host = element.getHeaders().get("FRAGMENT-HOST");
                // if (host != null) {
                // LOG.warn("Bundle " + element.getSymbolicName() +
                // " is a fragment which is part of: " + host);
                // LOG.warn("Required imports are: " +
                // element.getHeaders().get("IMPORT-PACKAGE"));
                // } else {
                // element.start();
                // }
                // } catch (BundleException e) {
                // LOG.error("BundleException:", e);
                // fail();
                // }

                debugit = true;

            }
        }
        if (debugit) {
            LOG.warn(("Do some debugging because some bundle is unresolved"));
        }
        // assertNotNull(broker);

        configLispPlugin.setLispAddress(lispBindAddress);

        // Uncomment this code to Know which services were actually loaded to
        // BundleContext

        /*
        for (ServiceReference sr : bc.getAllServiceReferences(null, null)) {
            LOG.info(sr.getBundle().getSymbolicName());
            LOG.info(sr.toString());
        }
        */

        sleepForSeconds(1);
    }

    private void cleanUP() {
        after();
        mapService.cleanCachedMappings();
        configLispPlugin.shouldListenOnXtrPort(false);
        socket = initSocket(socket, LispMessage.PORT_NUM);

    }

    private void restartSocket() {
        after();
        socket = initSocket(socket, LispMessage.PORT_NUM);
    }

}
