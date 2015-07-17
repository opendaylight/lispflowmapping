/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafListAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSegmentAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispMacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.ReencapHop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.XtrRequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafkeyvalueaddress.KeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafkeyvalueaddress.ValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaflistaddress.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaflistaddress.AddressesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsegmentaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcaftrafficengineeringaddress.HopsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcaflist.LcafListAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcaftrafficengineering.LcafTrafficEngineeringAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.no.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.reencaphop.Hop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.reencaphop.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
public class MappingServiceIntegrationTest {

    protected static final Logger LOG = LoggerFactory.getLogger(MappingServiceIntegrationTest.class);
    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacketWithNotify;
    private byte[] mapRegisterPacketWithoutNotify;
    String lispBindAddress = "127.0.0.1";
    String ourAddress = "127.0.0.2";
    private LispAFIAddress locatorEid;
    private DatagramSocket socket;
    private byte[] mapRegisterPacketWithAuthenticationAndMapNotify;

    public static final String ODL = "org.opendaylight.controller";
    public static final String YANG = "org.opendaylight.yangtools";
    public static final String JERSEY = "com.sun.jersey";
    private static final int MAX_SERVICE_LOAD_RETRIES = 45;
    private static final int MAX_NOTIFICATION_RETRYS = 20;

    @After
    public void after() {
        if (socket != null) {
            socket.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Before
    public void before() throws Exception {
        areWeReady();
        locatorEid = LispAFIConvertor.asIPAfiAddress("4.3.2.1");
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

        mapRegisterPacketWithAuthenticationAndMapNotify = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
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

        mapRegisterPacketWithNotify = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
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

        mapRegisterPacketWithoutNotify = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 00 01 00 00 "
                + "0030   00 00 00 00 00 07 00 00 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
    }

    @Inject
    private BundleContext bc;
    private HttpURLConnection connection;
    protected static boolean notificationCalled;

    @Inject @Filter(timeout=10000)
    private IFlowMapping lms;

    @Inject @Filter(timeout=10000)
    private IConfigLispSouthboundPlugin configLispPlugin;


    // Configure the OSGi container
    @Configuration
    public Option[] config() {
        return options(MappingServiceTestHelper.mappingServiceBundlesWithClusterDAO());
    }

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
        registerAndQuery__ListLCAF();
        registerAndQuery__ApplicationData();
        registerAndQuery__TrafficEngineering();
        registerAndQuery__SegmentLCAF();
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
        testMapRegisterOverwritesSameSubkey();
        testMapRegisterOverwritesNoSubkey();
        testMapRegisterDoesntOverwritesNoSubkey();
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
        sendPacket(mapRegisterPacketWithNotify);
        MapNotify reply = receiveMapNotify();
        assertEquals(7, reply.getNonce().longValue());
    }

    public void mapRegisterWithMapNotifyAndMapRequest() throws SocketTimeoutException {
        cleanUP();
        LispAFIAddress eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");

        MapReply mapReply = registerAddressAndQuery(eid, 32);

        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(LispAFIConvertor.toContainer(locatorEid), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());

    }

    public void registerAndQuery__MAC() throws SocketTimeoutException {
        cleanUP();
        String macAddress = "01:02:03:04:05:06";

        MapReply reply = registerAddressAndQuery(LispAFIConvertor.asMacAfiAddress(macAddress));

        assertTrue(true);
        LispAFIAddress addressFromNetwork = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());
        assertTrue(addressFromNetwork instanceof LispMacAddress);
        String macAddressFromReply = ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.mac.MacAddress) addressFromNetwork).getMacAddress().getValue();

        assertEquals(macAddress, macAddressFromReply);
    }

    public void mapRequestMapRegisterAndMapRequest() throws SocketTimeoutException {
        cleanUP();
        LispAFIAddress eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(new NoAddressBuilder().setAfi((short) 0).build())).build());
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
        sendMapRequest(mapRequestBuilder.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlrBuilder.setMaskLength((short) 32);
        etlrBuilder.setRecordTtl(254);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterbuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        sendMapRegister(mapRegisterbuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(recordBuilder.getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());

    }

    public void testMapRegisterDosntOverwritesOtherSubKeys() throws SocketTimeoutException {
        cleanUP();
        LispAFIAddress eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        LispAFIAddress rloc1Value = LispAFIConvertor.asIPAfiAddress("4.3.2.1");
        LispAFIAddress rloc1 = LispAFIConvertor.asKeyValue("subkey1", LispAFIConvertor.toPrimitive(rloc1Value));
        LispAFIAddress rloc2Value = LispAFIConvertor.asIPAfiAddress("4.3.2.2");
        LispAFIAddress rloc2 = LispAFIConvertor.asKeyValue("subkey2", LispAFIConvertor.toPrimitive(rloc2Value));
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1, rloc2);
        assertEquals(2, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        assertEquals(LispAFIConvertor.toContainer(rloc2), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(rloc1), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).getLispAddressContainer());
    }

    public void testMapRegisterOverwritesSameSubkey() throws SocketTimeoutException {
        cleanUP();
        LispAFIAddress eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        LispAFIAddress rloc1Value = LispAFIConvertor.asIPAfiAddress("4.3.2.1");
        LispAFIAddress rloc1 = LispAFIConvertor.asKeyValue("subkey", LispAFIConvertor.toPrimitive(rloc1Value));
        LispAFIAddress rloc2Value = LispAFIConvertor.asIPAfiAddress("4.3.2.2");
        LispAFIAddress rloc2 = LispAFIConvertor.asKeyValue("subkey", LispAFIConvertor.toPrimitive(rloc2Value));
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1, rloc2);
        assertEquals(1, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        assertEquals(LispAFIConvertor.toContainer(rloc2), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer());
    }

    public void testMapRegisterOverwritesNoSubkey() throws SocketTimeoutException {
        cleanUP();
        lms.setOverwrite(true);
        LispAFIAddress eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        LispAFIAddress rloc1Value = LispAFIConvertor.asIPAfiAddress("4.3.2.1");
        LispAFIAddress rloc2Value = LispAFIConvertor.asIPAfiAddress("4.3.2.2");
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1Value, rloc2Value);
        assertEquals(1, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        assertEquals(LispAFIConvertor.toContainer(rloc2Value), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());
    }

    public void testMapRegisterDoesntOverwritesNoSubkey() throws SocketTimeoutException {
        cleanUP();
        lms.setOverwrite(false);
        LispAFIAddress eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        LispAFIAddress rloc1Value = LispAFIConvertor.asIPAfiAddress("4.3.2.1");
        LispAFIAddress rloc2Value = LispAFIConvertor.asIPAfiAddress("4.3.2.2");
        MapReply mapReply = sendMapRegisterTwiceWithDiffrentValues(eid, rloc1Value, rloc2Value);
        assertEquals(2, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        LispAddressContainer rloc1ReturnValueContainer = mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer();
        LispAddressContainer rloc2ReturnValueContainer = mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).getLispAddressContainer();
        assertTrue((LispAFIConvertor.toContainer(rloc1Value).equals(rloc1ReturnValueContainer) && LispAFIConvertor.toContainer(rloc2Value).equals(
                rloc2ReturnValueContainer))
                || (LispAFIConvertor.toContainer(rloc1Value).equals(rloc2ReturnValueContainer) && LispAFIConvertor.toContainer(rloc2Value).equals(
                        rloc1ReturnValueContainer)));
    }

    private MapReply sendMapRegisterTwiceWithDiffrentValues(LispAFIAddress eid, LispAFIAddress rloc1, LispAFIAddress rloc2)
            throws SocketTimeoutException {
        MapRegister mb = createMapRegister(eid, rloc1);
        MapNotify mapNotify = lms.handleMapRegister(mb, false);
        MapRequest mr = createMapRequest(eid);
        MapReply mapReply = lms.handleMapRequest(mr);
        assertEquals(mb.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0)
                .getLocatorRecord().get(0).getLispAddressContainer());
        mb = createMapRegister(eid, rloc2);
        mapNotify = lms.handleMapRegister(mb, false);
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

        sendPacket(mapRegisterPacketWithNotify);
        receiveMapNotify();

        sendPacket(mapRequestPacket);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        mapRegisterPacketWithoutNotify[mapRegisterPacketWithoutNotify.length - 1] += 1;
        sendPacket(mapRegisterPacketWithoutNotify);

        ByteBuffer readBuf = ByteBuffer.wrap(receivePacket().getData());
        MapRequest smr = MapRequestSerializer.getInstance().deserialize(readBuf);
        assertTrue(smr.isSmr());
        LispAddressContainer sourceEid = smr.getSourceEid().getLispAddressContainer();
        assertTrue(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("153.16.254.1")).equals(sourceEid));
        LispAddressContainer smrEid = smr.getEidRecord().get(0).getLispAddressContainer();
        assertTrue(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4")).equals(smrEid));
    }

    // --------------------- Northbound Tests ---------------------------

    private void northboundAddKey() throws Exception {
        cleanUP();
        LispIpv4Address address = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        int mask = 32;
        String pass = "asdf";

        URL url = createPutURL("key");
        String authKeyJSON = createAuthKeyJSON(pass, address, mask);
        callURL("PUT", "application/json", "text/plain", authKeyJSON, url);

        String retrievedKey = lms.getAuthenticationKey(LispAFIConvertor.toContainer(address), mask);

        // Check stored password matches the one sent
        assertEquals(pass, retrievedKey);

    }

    private void northboundRetrieveSourceDestKey() throws Exception {
        cleanUP();
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 address1 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4) LispAFIConvertor
                .toPrimitive(LispAFIConvertor.asIPAfiAddress("10.0.0.1"));
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 address2 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4) LispAFIConvertor
                .toPrimitive(LispAFIConvertor.asIPAfiAddress("10.0.0.2"));
        int mask1 = 32;
        int mask2 = 32;
        LcafSourceDestAddr sourceDestAddress = new LcafSourceDestAddrBuilder().setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode())
                .setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(address1).build()).setSrcMaskLength((short) mask1)
                .setDstAddress(new DstAddressBuilder().setPrimitiveAddress(address2).build()).setDstMaskLength((short) mask2).build();
        String pass = "asdf";

        lms.addAuthenticationKey(LispAFIConvertor.toContainer(sourceDestAddress), mask1, pass);

        // URL url = createGetKeyIPv4URL(address1, mask1);
        URL url = createGetKeySourceDestURL(address1.getIpv4Address().getAfi(),
                ((LispIpv4Address) LispAFIConvertor.toAFIfromPrimitive(sourceDestAddress.getSrcAddress().getPrimitiveAddress())).getIpv4Address().getValue(), sourceDestAddress.getSrcMaskLength(),
                ((LispIpv4Address) LispAFIConvertor.toAFIfromPrimitive(sourceDestAddress.getDstAddress().getPrimitiveAddress())).getIpv4Address().getValue(), sourceDestAddress.getDstMaskLength());
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // test that the password matches what was we expected.
        assertEquals(pass, json.get("key"));

    }

    private void northboundRetrieveKey() throws Exception {
        cleanUP();
        LispIpv4Address address = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        String pass = "asdf";

        lms.addAuthenticationKey(LispAFIConvertor.toContainer(address), mask, pass);

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
        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAFIConvertor.asIPAfiAddress("20.0.0.2");

        // NB add mapping always checks the key
        lms.addAuthenticationKey(LispAFIConvertor.toContainer(eid), mask, pass);

        URL url = createPutURL("mapping");
        String mapRegisterJSON = createMapRegisterJSON(pass, eid, mask, rloc);
        callURL("PUT", "application/json", "text/plain", mapRegisterJSON, url);

        // Retrieve the RLOC from the database
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setPitr(false);
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) mask).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        MapReply mapReply = lms.handleMapRequest(mapRequestBuilder.build());

        LispIpv4Address retrievedRloc = (LispIpv4Address) LispAFIConvertor.toAFI(
                mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer());

        assertEquals(rloc.getIpv4Address().getValue(), retrievedRloc.getIpv4Address().getValue());

    }

    private String createMapRegisterJSON(String key, LispIpv4Address eid, int mask, LispIpv4Address rloc) {
        String jsonString = "{ " + "\"key\" : \"" + key + "\"," + "\"mapregister\" : " + "{ " + "\"proxyMapReply\" : false, "
                + "\"eidToLocatorRecords\" : " + "[ " + "{ " + "\"authoritative\" : true," + "\"prefixGeneric\" : " + "{ " + "\"ipAddress\" : \""
                + eid.getIpv4Address().getValue() + "\"," + "\"afi\" : " + eid.getAfi().shortValue() + "}," + "\"mapVersion\" : 0,"
                + "\"maskLength\" : " + mask + ", " + "\"action\" : \"NoAction\"," + "\"locators\" : " + "[ " + "{ " + "\"multicastPriority\" : 1,"
                + "\"locatorGeneric\" : " + "{ " + "\"ipAddress\" : \"" + rloc.getIpv4Address().getValue() + "\"," + "\"afi\" : "
                + rloc.getAfi().shortValue() + "}, " + "\"routed\" : true," + "\"multicastWeight\" : 50," + "\"rlocProbed\" : false, "
                + "\"localLocator\" : false, " + "\"priority\" : 1, " + "\"weight\" : 50 " + "} " + "], " + "\"recordTtl\" : 100" + "} " + "], "
                + "\"nonce\" : 3," + "\"keyId\" : 0 " + "} " + "}";

        return jsonString;
    }

    private void northboundRetrieveMapping() throws Exception {
        cleanUP();
        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAFIConvertor.asIPAfiAddress("20.0.0.2");
        // Insert mapping in the database
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        etlr.setAuthoritative(false);
        etlr.setAction(Action.NoAction);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        record.setRouted(true);
        record.setRlocProbed(false);
        record.setLocalLocator(false);
        record.setPriority((short) 1);
        record.setWeight((short) 50);
        record.setMulticastPriority((short) 1);
        record.setMulticastWeight((short) 1);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());
        lms.handleMapRegister(mapRegister.build(), false);

        // Get mapping using NB interface. No IID used
        URL url = createGetMappingIPv4URL(0, eid, mask);
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // With just one locator, locators is not a JSONArray
        String rlocRetrieved = json.getJSONArray("locators").getJSONObject(0).getJSONObject("locatorGeneric").getString("ipAddress");

        assertEquals(rloc.getIpv4Address().getValue(), rlocRetrieved);

    }

    private void northboundDeleteMapping() throws Exception {
        cleanUP();
        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAFIConvertor.asIPAfiAddress("20.0.0.2");
        // Insert mapping in the database
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        etlr.setAuthoritative(false);
        etlr.setAction(Action.NoAction);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        record.setRouted(true);
        record.setRlocProbed(false);
        record.setLocalLocator(false);
        record.setPriority((short) 1);
        record.setWeight((short) 50);
        record.setMulticastPriority((short) 1);
        record.setMulticastWeight((short) 1);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());
        lms.handleMapRegister(mapRegister.build(), false);

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
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 address1 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4) LispAFIConvertor
                .toPrimitive(LispAFIConvertor.asIPAfiAddress("10.0.0.1"));
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 address2 = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4) LispAFIConvertor
                .toPrimitive(LispAFIConvertor.asIPAfiAddress("10.0.0.2"));
        int mask1 = 32;
        int mask2 = 32;
        LcafSourceDestAddr sourceDestAddress = new LcafSourceDestAddrBuilder().setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode())
                .setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(address1).build()).setSrcMaskLength((short) mask1)
                .setDstAddress(new DstAddressBuilder().setPrimitiveAddress(address2).build()).setDstMaskLength((short) mask2).build();
        LispIpv4Address rloc = LispAFIConvertor.asIPAfiAddress("20.0.0.2");

        // Insert mapping in the database
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(sourceDestAddress));
        etlr.setMaskLength((short) mask1);
        etlr.setRecordTtl(254);
        etlr.setAuthoritative(false);
        etlr.setAction(Action.NoAction);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        record.setRouted(true);
        record.setRlocProbed(false);
        record.setLocalLocator(false);
        record.setPriority((short) 1);
        record.setWeight((short) 50);
        record.setMulticastPriority((short) 1);
        record.setMulticastWeight((short) 1);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());
        lms.handleMapRegister(mapRegister.build(), false);

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
        String rlocRetrieved = json.getJSONArray("locators").getJSONObject(0).getJSONObject("locatorGeneric").getString("ipAddress");

        assertEquals(rloc.getIpv4Address().getValue(), rlocRetrieved);

    }

    private URL createGetKeyIPv4URL(LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/0/%d/%s/%d", "key", address.getAfi().shortValue(),
                address.getIpv4Address().getValue(), mask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createGetKeySourceDestURL(int afi, String srcAddress, int srcMask, String dstAddress, int dstMask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/0/%d/%s/%d/%s/%d", "key", afi, srcAddress, srcMask,
                dstAddress, dstMask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createGetMappingSourceDestURL(int afi, String srcAddress, int srcMask, String dstAddress, int dstMask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/0/%d/%s/%d/%s/%d", "mapping", afi, srcAddress,
                srcMask, dstAddress, dstMask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createGetMappingIPv4URL(int iid, LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/%d/%d/%s/%d", "mapping", iid, address.getAfi()
                .shortValue(), address.getIpv4Address().getValue(), mask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createDeleteMappingIPv4URL(int iid, LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/nb/v2/default/%s/%d/%d/%s/%d", "mapping", iid, address.getAfi()
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

    private String callURL(String method, String content, String accept, String body, URL url) throws IOException, JSONException {
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

    // ------------------------------- Mask Tests ---------------------------

    public void eidPrefixLookupIPv4() throws SocketTimeoutException {
        cleanUP();
        runPrefixTest(LispAFIConvertor.asIPAfiAddress("1.2.3.4"), 16, LispAFIConvertor.asIPAfiAddress("1.2.3.2"),
                LispAFIConvertor.asIPAfiAddress("1.1.1.1"), (byte) 32);
    }

    public void eidPrefixLookupIPv6() throws SocketTimeoutException {
        cleanUP();
        runPrefixTest(LispAFIConvertor.asIPv6AfiAddress("1:2:3:4:5:6:7:8"), 64, LispAFIConvertor.asIPv6AfiAddress("1:2:3:4:5:1:2:3"),
                LispAFIConvertor.asIPv6AfiAddress("1:2:3:1:2:3:1:2"), 128);
    }

    private void runPrefixTest(LispAFIAddress registerEID, int registerdMask, LispAFIAddress matchedAddress, LispAFIAddress unMatchedAddress, int mask)
            throws SocketTimeoutException {

        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        mapRegister.setWantMapNotify(true);
        mapRegister.setKeyId((short) 0);
        mapRegister.setAuthenticationData(new byte[0]);
        mapRegister.setNonce((long) 8);
        mapRegister.setProxyMapReply(false);
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setRecordTtl(254);
        etlr.setAction(Action.NoAction);
        etlr.setAuthoritative(false);
        etlr.setMapVersion((short) 0);
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(registerEID));
        etlr.setMaskLength((short) registerdMask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")));
        record.setLocalLocator(false);
        record.setRlocProbed(false);
        record.setRouted(true);
        record.setMulticastPriority((short) 0);
        record.setMulticastWeight((short) 0);
        record.setPriority((short) 0);
        record.setWeight((short) 0);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());
        sendMapRegister(mapRegister.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        MapRequestBuilder mapRequest = new MapRequestBuilder();
        mapRequest.setNonce((long) 4);
        mapRequest.setSourceEid(new SourceEidBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
        mapRequest.setEidRecord(new ArrayList<EidRecord>());
        mapRequest.setAuthoritative(false);
        mapRequest.setMapDataPresent(false);
        mapRequest.setPitr(false);
        mapRequest.setProbe(false);
        mapRequest.setSmr(false);
        mapRequest.setSmrInvoked(false);
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) mask).setLispAddressContainer(LispAFIConvertor.toContainer(matchedAddress)).build());
        mapRequest.setItrRloc(new ArrayList<ItrRloc>());
        mapRequest.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
        sendMapRequest(mapRequest.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(record.getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer());
        mapRequest.setEidRecord(new ArrayList<EidRecord>());
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) mask).setLispAddressContainer(LispAFIConvertor.toContainer(unMatchedAddress)).build());
        sendMapRequest(mapRequest.build());
        mapReply = receiveMapReply();
        assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
    }

    // This registers an IP with a MapRegister, then adds a password via the
    // northbound REST API
    // and checks that the password works
    public void testPasswordExactMatch() throws Exception {
        cleanUP();
        String ipString = "10.0.0.1";
        LispIpv4Address address = LispAFIConvertor.asIPAfiAddress(ipString);
        int mask = 32;
        String pass = "pass";

        URL url = createPutURL("key");

        String jsonAuthData = createAuthKeyJSON(pass, address, mask);

        LOG.trace("Sending this JSON to LISP server: \n" + jsonAuthData);
        LOG.trace("Address: " + address);

        byte[] expectedSha = new byte[] { (byte) 146, (byte) 234, (byte) 52, (byte) 247, (byte) 186, (byte) 232, (byte) 31, (byte) 249, (byte) 87,
                (byte) 73, (byte) 234, (byte) 54, (byte) 225, (byte) 160, (byte) 129, (byte) 251, (byte) 73, (byte) 53, (byte) 196, (byte) 62 };

        byte[] zeros = new byte[20];

        callURL("PUT", "application/json", "text/plain", jsonAuthData, url);

        // build a MapRegister
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(address));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());

        mapRegister.setKeyId((short) 1); // LispKeyIDEnum.SHA1.getKeyID()
        mapRegister.setAuthenticationData(zeros);

        sendMapRegister(mapRegister.build());
        assertNoPacketReceived(3000);

        mapRegister.setAuthenticationData(expectedSha);

        sendMapRegister(mapRegister.build());

        assertMapNotifyRecieved();
    }

    public void testPasswordMaskMatch() throws Exception {
        cleanUP();
        LispIpv4Address addressInRange = LispAFIConvertor.asIPAfiAddress("10.20.30.40");
        LispIpv4Address addressOutOfRange = LispAFIConvertor.asIPAfiAddress("20.40.30.40");
        LispIpv4Address range = LispAFIConvertor.asIPAfiAddress("10.20.30.0");

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
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(addressInRange));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        record.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());

        mapRegister.setKeyId((short) 1); // LispKeyIDEnum.SHA1.getKeyID()
        mapRegister
                .setAuthenticationData(new byte[] { -15, -52, 38, -94, 125, -111, -68, -79, 68, 6, 101, 45, -1, 47, -4, -67, -113, 104, -110, -71 });

        sendMapRegister(mapRegister.build());

        assertMapNotifyRecieved();

        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(addressOutOfRange));
        mapRegister
                .setAuthenticationData(new byte[] { -54, 68, -58, -91, -23, 22, -88, -31, 113, 39, 115, 78, -68, -123, -71, -14, -99, 67, -23, -73 });

        sendMapRegister(mapRegister.build());
        assertNoPacketReceived(3000);
    }

    private MapReply registerAddressAndQuery(LispAFIAddress eid) throws SocketTimeoutException {
        return registerAddressAndQuery(eid, -1);
    }

    // takes an address, packs it in a MapRegister and sends it
    private void registerAddress(LispAFIAddress eid, int maskLength) throws SocketTimeoutException {
        MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setWantMapNotify(true);
        mapRegisterBuilder.setKeyId((short) 0);
        mapRegisterBuilder.setAuthenticationData(new byte[0]);
        mapRegisterBuilder.setNonce((long) 8);
        mapRegisterBuilder.setProxyMapReply(false);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        if (maskLength != -1) {
            etlrBuilder.setMaskLength((short) maskLength);
        } else {
            etlrBuilder.setMaskLength((short) 0);
        }
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
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        sendMapRegister(mapRegisterBuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
    }

    private MapReply queryForAddress(LispAFIAddress eid, String srcEid) throws SocketTimeoutException {
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        if (srcEid != null) {
            mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(LispAFIConvertor.
                    toContainer(LispAFIConvertor.asIPAfiAddress(srcEid))).build());
        } else {
            mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(LispAFIConvertor.
                    toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
        }
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
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
    private MapReply registerAddressAndQuery(LispAFIAddress eid, int maskLength) throws SocketTimeoutException {
        MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setWantMapNotify(true);
        mapRegisterBuilder.setKeyId((short) 0);
        mapRegisterBuilder.setAuthenticationData(new byte[0]);
        mapRegisterBuilder.setNonce((long) 8);
        mapRegisterBuilder.setProxyMapReply(false);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        if (maskLength != -1) {
            etlrBuilder.setMaskLength((short) maskLength);
        } else {
            etlrBuilder.setMaskLength((short) 0);
        }
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
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        sendMapRegister(mapRegisterBuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress)))
                .build());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
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

    @Test
    public void registerAndQuery__SrcDestLCAF() throws SocketTimeoutException {
        cleanUP();
        String ipString = "10.20.30.200";
        String macString = "01:02:03:04:05:06";
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 addrToSend1 = LispAFIConvertor.asPrimitiveIPAfiAddress(ipString);
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Mac addrToSend2 = LispAFIConvertor.asPrimitiveMacAfiAddress(macString);
        LcafSourceDestAddrBuilder builder = new LcafSourceDestAddrBuilder();
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());
        builder.setSrcMaskLength((short) 32);
        builder.setDstMaskLength((short) 0);
        builder.setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(addrToSend1).build());
        builder.setDstAddress(new DstAddressBuilder().setPrimitiveAddress(addrToSend2).build());

        MapReply reply = registerAddressAndQuery(builder.build());

        LispAddressContainer fromNetwork = reply.getEidToLocatorRecord().get(0).getLispAddressContainer();
        assertTrue(fromNetwork.getAddress() instanceof LcafSourceDest);
        LcafSourceDest sourceDestFromNetwork = (LcafSourceDest) fromNetwork.getAddress();

        LispAFIAddress receivedAddr1 = LispAFIConvertor.toAFIfromPrimitive(sourceDestFromNetwork.getLcafSourceDestAddr().getSrcAddress().getPrimitiveAddress());
        LispAFIAddress receivedAddr2 = LispAFIConvertor.toAFIfromPrimitive(sourceDestFromNetwork.getLcafSourceDestAddr().getDstAddress().getPrimitiveAddress());

        assertTrue(receivedAddr1 instanceof LispIpv4Address);
        assertTrue(receivedAddr2 instanceof LispMacAddress);

        LispIpv4Address receivedIP = (LispIpv4Address) receivedAddr1;
        LispMacAddress receivedMAC = (LispMacAddress) receivedAddr2;

        assertEquals(ipString, receivedIP.getIpv4Address().getValue());
        assertEquals(macString, receivedMAC.getMacAddress().getValue());
    }

    @Test
    public void registerAndQuery__SrcDestLCAFOverlap() throws SocketTimeoutException {
        cleanUP();
        String ipString1 = "10.10.10.0";
        String ipString2 = "20.20.20.0";
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 addrToSend1 = LispAFIConvertor.asPrimitiveIPAfiAddress(ipString1);
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 addrToSend2 = LispAFIConvertor.asPrimitiveIPAfiAddress(ipString2);
        LcafSourceDestAddrBuilder builder = new LcafSourceDestAddrBuilder();
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());
        builder.setSrcMaskLength((short) 24);
        builder.setDstMaskLength((short) 24);
        builder.setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(addrToSend1).build());
        builder.setDstAddress(new DstAddressBuilder().setPrimitiveAddress(addrToSend2).build());

        LcafSourceDestAddr srcDst = builder.build();
        registerAddress(LispAFIConvertor.asIPAfiAddress(ipString2), 24);
        registerAddress(srcDst, -1);

        // exact match
        MapReply reply = queryForAddress(srcDst, null);

        LispAddressContainer fromNetwork = reply.getEidToLocatorRecord().get(0).getLispAddressContainer();
        assertTrue(fromNetwork.getAddress() instanceof LcafSourceDest);
        LcafSourceDest sourceDestFromNetwork = (LcafSourceDest) fromNetwork.getAddress();

        LispAFIAddress receivedAddr1 = LispAFIConvertor.toAFIfromPrimitive(sourceDestFromNetwork.getLcafSourceDestAddr().getSrcAddress().getPrimitiveAddress());
        LispAFIAddress receivedAddr2 = LispAFIConvertor.toAFIfromPrimitive(sourceDestFromNetwork.getLcafSourceDestAddr().getDstAddress().getPrimitiveAddress());

        assertTrue(receivedAddr1 instanceof LispIpv4Address);
        assertTrue(receivedAddr2 instanceof LispIpv4Address);

        LispIpv4Address receivedIP1 = (LispIpv4Address) receivedAddr1;
        LispIpv4Address receivedIP2 = (LispIpv4Address) receivedAddr2;

        assertEquals(ipString1, receivedIP1.getIpv4Address().getValue());
        assertEquals(ipString2, receivedIP2.getIpv4Address().getValue());

        // srcEid/dstEid match
        reply = queryForAddress(LispAFIConvertor.asIPAfiAddress("20.20.20.1"), "10.10.10.1");
        fromNetwork = reply.getEidToLocatorRecord().get(0).getLispAddressContainer();
        assertTrue(fromNetwork.getAddress() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv4);

        Ipv4Address ipAddr2 = ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv4) fromNetwork.getAddress()).getIpv4Address();
        assertEquals(ipString2, ipAddr2.getIpv4Address().getValue());

        // dstEid match only
        reply = queryForAddress(LispAFIConvertor.asIPAfiAddress("20.20.20.1"), "1.2.3.4");
        fromNetwork = reply.getEidToLocatorRecord().get(0).getLispAddressContainer();
        assertTrue(fromNetwork.getAddress() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv4);

        ipAddr2 = ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv4) fromNetwork.getAddress()).getIpv4Address();
        assertEquals(ipString2, ipAddr2.getIpv4Address().getValue());
    }

    @Test
    public void registerAndQuery__KeyValueLCAF() throws SocketTimeoutException {
        cleanUP();
        String ipString = "10.20.30.200";
        String macString = "01:02:03:04:05:06";
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4 addrToSend1 = LispAFIConvertor.asPrimitiveIPAfiAddress(ipString);
        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Mac addrToSend2 = LispAFIConvertor.asPrimitiveMacAfiAddress(macString);
        LcafKeyValueAddressAddrBuilder builder = new LcafKeyValueAddressAddrBuilder();
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode());
        builder.setKey(new KeyBuilder().setPrimitiveAddress(addrToSend1).build());
        builder.setValue(new ValueBuilder().setPrimitiveAddress(addrToSend2).build());

        MapReply reply = registerAddressAndQuery(builder.build());

        LispAddressContainer fromNetwork = reply.getEidToLocatorRecord().get(0).getLispAddressContainer();
        assertTrue(fromNetwork.getAddress() instanceof LcafKeyValue);
        LcafKeyValue keyValueFromNetwork = (LcafKeyValue) fromNetwork.getAddress();

        LispAFIAddress receivedAddr1 = LispAFIConvertor.toAFIfromPrimitive(keyValueFromNetwork.getLcafKeyValueAddressAddr().getKey().getPrimitiveAddress());
        LispAFIAddress receivedAddr2 = LispAFIConvertor.toAFIfromPrimitive(keyValueFromNetwork.getLcafKeyValueAddressAddr().getValue().getPrimitiveAddress());

        assertTrue(receivedAddr1 instanceof LispIpv4Address);
        assertTrue(receivedAddr2 instanceof LispMacAddress);

        LispIpv4Address receivedIP = (LispIpv4Address) receivedAddr1;
        LispMacAddress receivedMAC = (LispMacAddress) receivedAddr2;

        assertEquals(ipString, receivedIP.getIpv4Address().getValue());
        assertEquals(macString, receivedMAC.getMacAddress().getValue());
    }

    public void registerAndQuery__ListLCAF() throws SocketTimeoutException {
        cleanUP();
        String macString = "01:02:03:04:05:06";
        String ipString = "10.20.255.30";
        LcafListAddrBuilder listbuilder = new LcafListAddrBuilder();
        listbuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.LIST.getLispCode());
        listbuilder.setAddresses(new ArrayList<Addresses>());
        listbuilder.getAddresses().add(new AddressesBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPAfiAddress(ipString))).build());
        listbuilder.getAddresses().add(new AddressesBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(LispAFIConvertor.asMacAfiAddress(macString))).build());

        MapReply reply = registerAddressAndQuery(listbuilder.build());

        LispAFIAddress receivedAddress = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());

        assertTrue(receivedAddress instanceof LcafListAddress);

        LcafListAddress listAddrFromNetwork = (LcafListAddress) receivedAddress;
        LispAFIAddress receivedAddr1 = LispAFIConvertor.toAFIfromPrimitive(listAddrFromNetwork.getAddresses().get(0).getPrimitiveAddress());
        LispAFIAddress receivedAddr2 = LispAFIConvertor.toAFIfromPrimitive(listAddrFromNetwork.getAddresses().get(1).getPrimitiveAddress());

        assertTrue(receivedAddr1 instanceof LispIpv4Address);
        assertTrue(receivedAddr2 instanceof LispMacAddress);

        assertEquals(macString, ((LispMacAddress) receivedAddr2).getMacAddress().getValue());
        assertEquals(ipString, ((LispIpv4Address) receivedAddr1).getIpv4Address().getValue());
    }

    public void registerAndQuery__SegmentLCAF() throws SocketTimeoutException {
        cleanUP();
        String ipString = "10.20.255.30";
        int instanceId = 6;
        LcafSegmentAddrBuilder builder = new LcafSegmentAddrBuilder();
        builder.setInstanceId((long) instanceId);
        builder.setIidMaskLength((short) 32);
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
        builder.setAddress(new AddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPAfiAddress(ipString))).build());

        MapReply reply = registerAddressAndQuery(builder.build());

        LispAFIAddress receivedAddress = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());
        assertTrue(receivedAddress instanceof LcafSegmentAddress);

        LcafSegmentAddress segmentfromNetwork = (LcafSegmentAddress) receivedAddress;
        LispAFIAddress addrFromSegment = LispAFIConvertor.toAFIfromPrimitive(segmentfromNetwork.getAddress().getPrimitiveAddress());
        assertTrue(addrFromSegment instanceof LispIpv4Address);
        assertEquals(ipString, ((LispIpv4Address) addrFromSegment).getIpv4Address().getValue());

        assertEquals(instanceId, segmentfromNetwork.getInstanceId().intValue());
    }

    public void registerAndQuery__TrafficEngineering() throws SocketTimeoutException {
        cleanUP();
        String macString = "01:02:03:04:05:06";
        String ipString = "10.20.255.30";
        HopBuilder hopBuilder = new HopBuilder();
        hopBuilder.setPrimitiveAddress(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPAfiAddress(ipString)));
        Hop hop1 = hopBuilder.build();
        hopBuilder.setPrimitiveAddress(LispAFIConvertor.toPrimitive(LispAFIConvertor.asMacAfiAddress(macString)));
        Hop hop2 = hopBuilder.build();
        HopsBuilder hb = new HopsBuilder();
        hb.setHop(hop1);
        hb.setLookup(true);
        hb.setRLOCProbe(false);
        hb.setStrict(true);
        HopsBuilder hb2 = new HopsBuilder();
        hb2.setHop(hop2);
        hb2.setLookup(false);
        hb2.setRLOCProbe(true);
        hb2.setStrict(false);
        Hops hops1 = hb.build();
        Hops hops2 = hb2.build();
        LcafTrafficEngineeringAddrBuilder trafficBuilder = new LcafTrafficEngineeringAddrBuilder();
        trafficBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType(
                (short) LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode());
        trafficBuilder.setHops(new ArrayList<Hops>());
        trafficBuilder.getHops().add(hb.build());
        trafficBuilder.getHops().add(hb2.build());

        MapReply reply = registerAddressAndQuery(trafficBuilder.build());

        assertTrue(LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer()) instanceof LcafTrafficEngineeringAddress);

        LcafTrafficEngineeringAddress receivedAddress = (LcafTrafficEngineeringAddress) LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());

        ReencapHop receivedHop1 = receivedAddress.getHops().get(0);
        ReencapHop receivedHop2 = receivedAddress.getHops().get(1);

        assertEquals(true, hops1.isLookup());
        assertEquals(false, hops1.isRLOCProbe());
        assertEquals(true, hops1.isStrict());

        assertEquals(false, hops2.isLookup());
        assertEquals(true, hops2.isRLOCProbe());
        assertEquals(false, hops2.isStrict());

        assertTrue(receivedHop1.getHop().getPrimitiveAddress() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4);
        assertTrue(receivedHop2.getHop().getPrimitiveAddress() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Mac);

        assertEquals(ipString, ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4) receivedHop1.getHop().getPrimitiveAddress()).getIpv4Address().getIpv4Address().getValue());
        assertEquals(macString, ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Mac) receivedHop2.getHop().getPrimitiveAddress()).getMacAddress().getMacAddress().getValue());
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

        LcafApplicationDataAddrBuilder builder = new LcafApplicationDataAddrBuilder();
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode());
        builder.setIpTos(ipTOs);
        builder.setProtocol(protocol);
        builder.setLocalPortLow(new PortNumber(localPortLow));
        builder.setLocalPortHigh(new PortNumber(localPortHigh));
        builder.setRemotePortLow(new PortNumber(remotePortLow));
        builder.setRemotePortHigh(new PortNumber(remotePortHigh));
        builder.setAddress(new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafapplicationdataaddress.AddressBuilder()
                .setPrimitiveAddress(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPAfiAddress(ipString))).build());

        LcafApplicationDataAddress addressToSend = builder.build();

        MapReply reply = registerAddressAndQuery(addressToSend);

        LispAFIAddress receivedAddress = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());

        assertTrue(receivedAddress instanceof LcafApplicationDataAddress);

        LcafApplicationDataAddress receivedApplicationDataAddress = (LcafApplicationDataAddress) receivedAddress;
        assertEquals(protocol, receivedApplicationDataAddress.getProtocol().intValue());
        assertEquals(ipTOs, receivedApplicationDataAddress.getIpTos().intValue());
        assertEquals(localPortLow, receivedApplicationDataAddress.getLocalPortLow().getValue().intValue());
        assertEquals(localPortHigh, receivedApplicationDataAddress.getLocalPortHigh().getValue().intValue());
        assertEquals(remotePortLow, receivedApplicationDataAddress.getRemotePortLow().getValue().intValue());
        assertEquals(remotePortHigh, receivedApplicationDataAddress.getRemotePortHigh().getValue().intValue());

        LispIpv4Address ipAddressReceived = (LispIpv4Address) LispAFIConvertor.toAFIfromPrimitive(receivedApplicationDataAddress.getAddress().getPrimitiveAddress());
        assertEquals(ipString, ipAddressReceived.getIpv4Address().getValue());
    }

    // ------------------- TimeOut Tests -----------

    public void mapRequestMapRegisterAndMapRequestTestTimeout() throws SocketTimeoutException {
        cleanUP();
        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(new NoAddressBuilder().setAfi((short) 0).build())).build());
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
        sendMapRequest(mapRequestBuilder.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlrBuilder.setMaskLength((short) 32);
        etlrBuilder.setRecordTtl(254);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterbuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        sendMapRegister(mapRegisterbuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(recordBuilder.getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());
        causeEntryToBeCleaned();
        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
    }

    public void mapRequestMapRegisterAndMapRequestTestNativelyForwardTimeoutResponse() throws Exception {
        cleanUP();
        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        MapRequest mapRequest = createMapRequest(eid);

        testTTLBeforeRegister(mapRequest);

        registerForTTL(eid);

        testTTLAfterRegister(mapRequest);

        causeEntryToBeCleaned();
        testTTLAfterClean(mapRequest);

        northboundAddKey();
        testTTLAfterAutherize(mapRequest);

    }

    private void testTTLAfterClean(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertCorrectMapReplyTTLAndAction(mapReply, 15, Action.NativelyForward);
    }

    private void causeEntryToBeCleaned() {
        // TODO for the time being, to keep master and stable/lithium in sync, we need to remove the forceful
        // expiration of DAO entries. Once we're past this, we'll have to expose methods to setTimeUnit(TimeUnit)
        // and cleanOld() (expired) entries in IFlowMapping (and perhaps ILispDAO) and use them here.
        this.lms.clean();
    }

    private void testTTLAfterRegister(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());
        assertCorrectMapReplyTTLAndAction(mapReply, 254, Action.NoAction);
    }

    private void registerForTTL(LispIpv4Address eid) throws SocketTimeoutException {
        MapRegister mapRegister = createMapRegister(eid);
        sendMapRegister(mapRegister);
        assertMapNotifyRecieved();
    }

    private void testTTLBeforeRegister(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertCorrectMapReplyTTLAndAction(mapReply, 15, Action.NativelyForward);
    }

    private void testTTLAfterAutherize(MapRequest mapRequest) throws SocketTimeoutException {
        MapReply mapReply;
        sendMapRequest(mapRequest);
        mapReply = receiveMapReply();
        assertCorrectMapReplyTTLAndAction(mapReply, 1, Action.NativelyForward);
    }

    private void assertCorrectMapReplyTTLAndAction(MapReply mapReply, int expectedTTL, Action expectedAction) {
        assertEquals(expectedTTL, mapReply.getEidToLocatorRecord().get(0).getRecordTtl().intValue());
        assertEquals(expectedAction, mapReply.getEidToLocatorRecord().get(0).getAction());
    }

    private MapRegister createMapRegister(LispAFIAddress eid, LispAFIAddress rloc) {
        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);
        mapRegisterbuilder.setKeyId((short) 0);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlrBuilder.setMaskLength((short) 24);
        etlrBuilder.setRecordTtl(254);
        etlrBuilder.setAuthoritative(false);
        etlrBuilder.setAction(Action.NoAction);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterbuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        MapRegister mapRegister = mapRegisterbuilder.build();
        return mapRegister;
    }

    private MapRegister createMapRegister(LispIpv4Address eid) {
        return createMapRegister(eid, LispAFIConvertor.asIPAfiAddress("4.3.2.1"));
    }

    private MapRequest createMapRequest(LispAFIAddress eid) {
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setPitr(false);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(new NoAddressBuilder().setAfi((short) 0).build())).build());
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
        MapRequest mr = mapRequestBuilder.build();
        return mr;
    }

    public void testSimpleNonProxy() throws SocketTimeoutException, SocketException {
        cleanUP();
        String rloc = "127.0.0.3";
        int port = LispMessage.PORT_NUM;
        LispAFIAddress ipRloc = LispAFIConvertor.asIPAfiAddress(rloc);
        sendProxyMapRequest(rloc, port, ipRloc);

    }

    public void testNonProxyOtherPort() throws SocketTimeoutException, SocketException {
        cleanUP();
        String rloc = "127.0.0.3";
        int port = 4350;
        LcafApplicationDataAddr adLcaf = new LcafApplicationDataAddrBuilder()
                .setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode())
                .setAddress(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafapplicationdataaddress.AddressBuilder().setPrimitiveAddress(
                                LispAFIConvertor.asPrimitiveIPAfiAddress(rloc)).build()).setLocalPortLow(new PortNumber(port)).build();
        LOG.info("testNonProxyOtherPort:" + adLcaf.toString());
        sendProxyMapRequest(rloc, port, adLcaf);

    }

    public void testRecievingNonProxyOnXtrPort() throws SocketTimeoutException, SocketException, Throwable {
        cleanUP();
        configLispPlugin.shouldListenOnXtrPort(true);
        notificationCalled = false;
        final String eid = "10.10.10.10";
        String rloc = "127.0.0.3";
        int port = LispMessage.XTR_PORT_NUM;
        LcafApplicationDataAddr adLcaf = new LcafApplicationDataAddrBuilder()
                .setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode())
                .setAddress(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafapplicationdataaddress.AddressBuilder().setPrimitiveAddress(
                                LispAFIConvertor.asPrimitiveIPAfiAddress(rloc)).build()).setLocalPortLow(new PortNumber(port)).build();
        final MapRequest mapRequest = createNonProxyMapRequest(eid, adLcaf);
        ((LispMappingService) lms).registerNotificationListener(XtrRequestMapping.class, new NotificationListener<XtrRequestMapping>() {

            @Override
            public void onNotification(XtrRequestMapping notification) {
                assertEquals(((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv4) mapRequest.getEidRecord().get(0).getLispAddressContainer().getAddress()).getIpv4Address().getIpv4Address().getValue(),
                        eid);
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

    private void sendProxyMapRequest(String rloc, int port, LispAFIAddress adLcaf) throws SocketTimeoutException, SocketException {
        String eid = "10.1.0.1";
        MapRequest mapRequest = createNonProxyMapRequest(eid, adLcaf);
        sendMapRequest(mapRequest);
        DatagramSocket nonProxySocket = new DatagramSocket(new InetSocketAddress(rloc, port));
        MapRequest recievedMapRequest = receiveMapRequest(nonProxySocket);
        assertEquals(mapRequest.getNonce(), recievedMapRequest.getNonce());
        assertEquals(mapRequest.getSourceEid(), recievedMapRequest.getSourceEid());
        assertEquals(mapRequest.getItrRloc(), recievedMapRequest.getItrRloc());
        assertEquals(mapRequest.getEidRecord(), recievedMapRequest.getEidRecord());
        nonProxySocket.close();
    }

    private MapRequest createNonProxyMapRequest(String eid, LispAFIAddress adLcaf) throws SocketTimeoutException {
        MapRegister mr = createMapRegister(LispAFIConvertor.asIPAfiAddress(eid));
        LocatorRecord record = new LocatorRecordBuilder(mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)).setLispAddressContainer(
                LispAFIConvertor.toContainer(adLcaf)).build();
        mr.getEidToLocatorRecord().get(0).getLocatorRecord().set(0, record);
        sendMapRegister(mr);
        assertMapNotifyRecieved();
        MapRequest mapRequest = createMapRequest(LispAFIConvertor.asIPAfiAddress(eid));
        MapRequestBuilder builder = new MapRequestBuilder(mapRequest);
        builder.setPitr(true);
        mapRequest = builder.build();
        return mapRequest;
    }

    private void assertMapNotifyRecieved() throws SocketTimeoutException {
        receiveMapNotify();
    }

    private MapReply receiveMapReply() throws SocketTimeoutException {
        return MapReplySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    }

    private MapRequest receiveMapRequest(DatagramSocket datagramSocket) throws SocketTimeoutException {
        return MapRequestSerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket(datagramSocket, 30000).getData()));
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
            LOG.trace("Recieved packet from socket!");
            return receivePacket;
        } catch (SocketTimeoutException ste) {
            throw ste;
        } catch (Throwable t) {
            fail();
            return null;
        }
    }

    private void initPacketAddress(DatagramPacket packet, int port) throws UnknownHostException {
        packet.setAddress(InetAddress.getByName(lispBindAddress));
        packet.setPort(port);
    }

    private DatagramSocket initSocket(DatagramSocket socket, int port) {
        try {
            socket = new DatagramSocket(new InetSocketAddress(ourAddress, port));
        } catch (SocketException e) {
            e.printStackTrace();
            fail();
        }
        return socket;
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void areWeReady() throws InvalidSyntaxException {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        assertNotNull(bc);
        boolean debugit = false;
        Bundle b[] = bc.getBundles();
        for (Bundle element : b) {
            int state = element.getState();
            LOG.trace("Bundle[" + element.getBundleId() + "]:" + element.getSymbolicName() + ",v" + element.getVersion() + ", state:"
                    + stateToString(state));
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

        ServiceReference r = null;
        configLispPlugin.setLispAddress(lispBindAddress);

        // Uncomment this code to Know which services were actually loaded to
        // BundleContext

        /*
        for (ServiceReference sr : bc.getAllServiceReferences(null, null)) {
            LOG.info(sr.getBundle().getSymbolicName());
            LOG.info(sr.toString());
        }
        */

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void cleanUP() {
        after();
        lms.clean();
        configLispPlugin.shouldListenOnXtrPort(false);
        socket = initSocket(socket, LispMessage.PORT_NUM);

    }

}
