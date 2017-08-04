/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.DATA1;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.DATA2;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.DATA3;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.KEY_ID;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.NONCE;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.SITE_ID;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.XTR_ID;
import static org.opendaylight.lispflowmapping.southbound.lisp.MapRegisterCacheTestUtil.joinArrays;

import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junitx.framework.ArrayAssert;
import org.apache.commons.lang3.ArrayUtils;
import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.mapcache.AuthKeyDb;
import org.opendaylight.lispflowmapping.southbound.ConcurrentLispSouthboundStats;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterCache;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.RequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.MapRegisterCacheMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yangtools.yang.binding.Notification;

public class LispSouthboundServiceTest extends BaseTestCase {

    private LispSouthboundHandler testedLispService;
    private NotificationPublishService nps;
    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacket;
    private ValueSaverAction<Notification> lispNotificationSaver;
    // private ValueSaverAction<MapRegister> mapRegisterSaver;
    // private ValueSaverAction<MapRequest> mapRequestSaver;
    private MapNotifyBuilder mapNotifyBuilder;
    private MapReplyBuilder mapReplyBuilder;
    private MappingRecordBuilder mappingRecordBuilder;
    private MapRegisterCache mapRegisterCache;
    private LispSouthboundPlugin mockLispSouthboundPlugin;
    private ConcurrentLispSouthboundStats lispSouthboundStats;
    private static final long CACHE_RECORD_TIMEOUT = 90000;

    private static AuthKeyDb akdb;

    private interface MapReplyIpv4SingleLocatorPos {
        int RECORD_COUNT = 3;
        int NONCE = 4;
        int LOCATOR_COUNT = 16;
        int EID_MASK_LEN = 17;
        int AFI_TYPE = 22;
        int EID_PREFIX = 24;
        int LOC_AFI = 34;
        int LOCATOR_RBIT = 33;
        int LOCATOR = 36;
    }

    private interface MapReplyIpv4SecondLocatorPos {
        int FIRST_LOCATOR_IPV4_LENGTH = 12;
        int LOC_AFI = MapReplyIpv4SingleLocatorPos.LOC_AFI + FIRST_LOCATOR_IPV4_LENGTH;
        int LOCATOR_RBIT = MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT + FIRST_LOCATOR_IPV4_LENGTH;
        int LOCATOR = MapReplyIpv4SingleLocatorPos.LOCATOR + FIRST_LOCATOR_IPV4_LENGTH;
    }

    @BeforeClass
    public static void initTests() {
        akdb = Mockito.mock(AuthKeyDb.class);
        Mockito.when(akdb.getAuthenticationKey(Matchers.eq(LispAddressUtil.asIpv4PrefixBinaryEid("10.10.10.10/8"))))
                .thenReturn(new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build());
        Mockito.when(akdb.getAuthenticationKey(Matchers.eq(LispAddressUtil.asIpv6PrefixBinaryEid(
                "2610:d0:ffff:192:0:0:0:1/128"))))
                .thenReturn(new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build());
        Mockito.when(akdb.getAuthenticationKey(Matchers.eq(LispAddressUtil.asIpv4PrefixBinaryEid("153.16.254.1/32"))))
                .thenReturn(new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build());
        Mockito.when(akdb.getAuthenticationKey(Matchers.eq(LispAddressUtil.asIpv4PrefixBinaryEid("125.124.123.122/8",
                new InstanceIdType(21L)))))
                .thenReturn(new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build());
        Mockito.when(akdb.getAuthenticationKey(Matchers.eq(LispAddressUtil.asMacEid("0a:0b:0c:0d:0e:0f"))))
                .thenReturn(new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build());
        Mockito.when(akdb.getAuthenticationKey(Matchers.eq(LispAddressUtil.asIpv6PrefixBinaryEid(
                "f0f:f0f:f0f:f0f:f0f:f0f:f0f:f0f/8"))))
                .thenReturn(new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build());
        Mockito.when(akdb.getAuthenticationKey(Matchers.eq(LispAddressUtil.asIpv4PrefixBinaryEid("172.1.1.2/32"))))
                .thenReturn(new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build());
    }

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        // mapResolver = context.mock(IMapResolver.class);
        // mapServer = context.mock(IMapServer.class);
        mockLispSouthboundPlugin = mock(LispSouthboundPlugin.class);
        Mockito.when(mockLispSouthboundPlugin.isMapRegisterCacheEnabled()).thenReturn(true);
        Mockito.when(mockLispSouthboundPlugin.getMapRegisterCacheTimeout()).thenReturn(CACHE_RECORD_TIMEOUT);
        mapRegisterCache = new MapRegisterCache();
        Mockito.when(mockLispSouthboundPlugin.getMapRegisterCache()).thenReturn(mapRegisterCache);
        Mockito.when(mockLispSouthboundPlugin.getDataBroker()).thenReturn(Mockito.mock(DataBroker.class));
        Mockito.when(mockLispSouthboundPlugin.getAkdb()).thenReturn(akdb);
        Mockito.when(mockLispSouthboundPlugin.getAuthenticationKeyDataListener()).thenReturn(
                Mockito.mock(AuthenticationKeyDataListener.class));
        nps = context.mock(NotificationPublishService.class);
        Mockito.when(mockLispSouthboundPlugin.getNotificationPublishService()).thenReturn(nps);
        lispSouthboundStats = new ConcurrentLispSouthboundStats();
        Mockito.when(mockLispSouthboundPlugin.getStats()).thenReturn(lispSouthboundStats);
        testedLispService = new LispSouthboundHandler(mockLispSouthboundPlugin);
        lispNotificationSaver = new ValueSaverAction<Notification>();
        // mapRegisterSaver = new ValueSaverAction<MapRegister>();
        // mapRequestSaver = new ValueSaverAction<MapRequest>();
        // SRC: 127.0.0.1:58560 to 127.0.0.1:4342
        // LISP(Type = 8 - Encapsulated)
        // IP: 192.168.136.10 -> 1.2.3.4
        // UDP: 56756
        // LISP(Type = 1 Map-Request
        // Record Count: 1
        // ITR-RLOC count: 0
        // Source EID AFI: 0
        // Source EID not present
        // Nonce: 0x3d8d2acd39c8d608
        // ITR-RLOC AFI=1 Address=192.168.136.10
        // Record 1: 1.2.3.4/32
        mapRequestPacket = extractWSUdpByteArray(new String(
                  "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 "
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 38 d4 31 00 00 ff 11 56 f3 c0 a8 88 0a 01 02 "
                + "0040   03 04 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "
                + "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 c0 a8 88 0a 00 20 "
                + "0060   00 01 01 02 03 04"));
        mapReplyBuilder = new MapReplyBuilder();
        mapReplyBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapReplyBuilder.setNonce((long) 0);
        mapReplyBuilder.setEchoNonceEnabled(false);
        mapReplyBuilder.setProbe(true);
        mapReplyBuilder.setSecurityEnabled(true);
        mappingRecordBuilder = new MappingRecordBuilder();
        String ip = "0.0.0.0";
        mappingRecordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid(ip + "/0"));
        mappingRecordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        mappingRecordBuilder.setRecordTtl(10);
        mappingRecordBuilder.setMapVersion((short) 0);
        mappingRecordBuilder.setAction(Action.NativelyForward);
        mappingRecordBuilder.setAuthoritative(false);
        // eidToLocatorBuilder.setPrefix(new LispIpv4Address(0));
        // mapReply.addEidToLocator(eidToLocatorBuilder);

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

        mapRegisterPacket = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 "
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 "
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        mapNotifyBuilder = new MapNotifyBuilder();
        mapNotifyBuilder.setAuthenticationData(new byte[0]);
    }

    @Test
    @Ignore
    public void todos() throws Exception {

        // TODO: MapRequest: usage of Map-Reply-Record in MapRequest packet.
        // TODO: Non-Encapsulated packets
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRegister__IllegalPacket() throws Exception {
        mapRegisterPacket = extractWSUdpByteArray(new String(
                  "0000   00 0c 29 7a ce 8d 00 0c 29 e4 ef 70 08 00 45 00 "
                + "0010   00 68 00 00 40 00 40 11 26 15 0a 01 00 6e 0a 01 "
                + "0020   00 01 10 f6 10 f6 00 54 03 3b 38 00 01 01 00 00 "));

        handleMapRegisterPacket(mapRegisterPacket);
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__IllegalPacket() throws Exception {
        mapRequestPacket = extractWSUdpByteArray(new String(
                  "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 "
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 38 d4 31 00 00 ff 11 56 f3 c0 a8 88 0a 01 02 "
                + "0040   03 04 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "));
        handleMapRequestPacket(mapRequestPacket);
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__IllegalEncapsulatedPacket() throws Exception {
        mapRequestPacket = extractWSUdpByteArray(new String(
                  "0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 "
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "));
        handleMapRequestPacket(mapRequestPacket);
    }

    private MapRegister lastMapRegister() {
        assertTrue(lispNotificationSaver.lastValue instanceof AddMapping);
        AddMapping lastValue = (AddMapping) lispNotificationSaver.lastValue;
        return lastValue.getMapRegister();
    }

    private MapRequest lastMapRequest() {
        RequestMapping lastValue = (RequestMapping) lispNotificationSaver.lastValue;
        return lastValue.getMapRequest();
    }

    @Test
    public void mapRegister__TwoRlocs() throws Exception {
        // P Bit & M Bit set
        // EID prefix: 172.1.1.2/32, TTL: 10, Authoritative, No-Action
        // Local RLOC: 10.1.0.110, Reachable, Priority/Weight: 1/100, Multicast
        // Priority/Weight: 255/0
        // Local RLOC: 192.168.136.51, Reachable, Priority/Weight: 6/100,
        // Multicast Priority/Weight: 255/0
        mapRegisterPacket = extractWSUdpByteArray(new String(
                  "0000   00 0c 29 7a ce 8d 00 0c 29 e4 ef 70 08 00 45 00 "
                + "0010   00 68 00 00 40 00 40 11 26 15 0a 01 00 6e 0a 01 "
                + "0020   00 01 10 f6 10 f6 00 54 03 3b 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 ae d8 7b d4 9c 59 "
                + "0040   e9 35 75 6e f1 29 27 a3 45 20 96 06 c2 e1 00 00 "
                + "0050   00 0a 02 20 10 00 00 00 00 01 ac 01 01 02 01 64 "
                + "0060   ff 00 00 05 00 01 0a 01 00 6e 06 64 ff 00 00 05 "
                + "0070   00 01 c0 a8 88 33"));

        oneOf(nps).putNotification(with(lispNotificationSaver));

        handleMapRegisterPacket(mapRegisterPacket);

        List<MappingRecordItem> eidRecords = lastMapRegister().getMappingRecordItem();
        assertEquals(1, eidRecords.size());
        MappingRecord eidRecord = eidRecords.get(0).getMappingRecord();
        assertEquals(2, eidRecord.getLocatorRecord().size());
        assertEquals(LispAddressUtil.asIpv4Rloc("10.1.0.110"), eidRecord.getLocatorRecord().get(0).getRloc());
        assertEquals(LispAddressUtil.asIpv4Rloc("192.168.136.51"), eidRecord.getLocatorRecord().get(1).getRloc());
    }

    @Test
    public void mapRegister__Ipv6Rloc() throws Exception {
        // P bit (Proxy-Map-Reply): Set
        // M bit (Want-Map-Notify): Set
        // Record Counter: 1
        // Nonce: 0
        // Key ID: 1
        // AuthLength: 20
        // Authentication Data: 5bc4d44a57e2a55d577a6f89779c004f5da713fb
        // EID prefix: 2610:d0:ffff:192::1/128, TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 10.0.58.156, Reachable, Priority/Weight: 1/100, Multicast
        // Priority/Weight: 255/0

        mapRegisterPacket = extractWSUdpByteArray(new String(
                  "0000   00 0c 29 34 3e 1b 00 0c 29 f6 d6 0d 08 00 45 00 "
                + "0010   00 68 00 00 40 00 40 11 ea c3 0a 00 3a 9c 0a 00 "
                + "0020   01 26 10 f6 10 f6 00 54 f5 9a 38 00 03 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 22 97 ff 61 ec d8 "
                + "0040   0f 91 c6 c4 01 ef 7f bb 77 58 39 5c 92 23 00 00 "
                + "0050   00 0a 01 80 10 00 00 00 00 02 26 10 00 d0 ff ff "
                + "0060   01 92 00 00 00 00 00 00 00 01 01 64 ff 00 00 05 "
                + "0070   00 01 0a 00 3a 9c"));

        oneOf(nps).putNotification(with(lispNotificationSaver));

        handleMapRegisterPacket(mapRegisterPacket);

        MappingRecord eidToLocatorRecord = lastMapRegister().getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(LispAddressUtil.asIpv6PrefixBinaryEid("2610:d0:ffff:192:0:0:0:1/128"),
                eidToLocatorRecord.getEid());
        assertEquals(Ipv6PrefixBinaryAfi.class, eidToLocatorRecord.getEid().getAddressType());

        assertEquals(LispAddressUtil.asIpv4Rloc("10.0.58.156"), eidToLocatorRecord.getLocatorRecord().get(0).getRloc());
    }

    @Test
    public void mapRegister__VerifyBasicFields() throws Exception {
        oneOf(nps).putNotification(with(lispNotificationSaver));
        handleMapRegisterPacket(mapRegisterPacket);

        MappingRecord eidToLocator = lastMapRegister().getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(LispAddressUtil.asIpv4PrefixBinaryEid("153.16.254.1/32"), eidToLocator.getEid());

        assertEquals(1, eidToLocator.getLocatorRecord().size());
        assertEquals(LispAddressUtil.asIpv4Rloc("192.168.136.10"), eidToLocator.getLocatorRecord().get(0).getRloc());
    }

    @Test
    @Ignore
    public void mapRegister__NoResponseFromMapServerShouldReturnNullPacket() throws Exception {
        oneOf(nps).putNotification(with(lispNotificationSaver));
        mapNotifyBuilder = null;

        assertNull(handleMapRegisterPacket(mapRegisterPacket));
    }

    @Test
    public void mapRegister__NonSetMBit() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 "
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 00 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 79 d1 44 66 19 99 "
                + "0040   83 63 a7 79 6e f0 40 97 54 26 3a 44 b4 eb 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 "
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handleMapRegisterPacket(registerWithNonSetMBit);

        assertFalse(lastMapRegister().isWantMapNotify());
    }

    @Test
    public void mapRegister__NonSetMBitWithNonZeroReservedBits() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 "
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 02 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 c0 c7 c5 2f 57 f6 "
                + "0040   e7 20 25 3d e8 b2 07 e2 63 de 62 2b 7a 20 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 "
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handleMapRegisterPacket(registerWithNonSetMBit);
        assertFalse(lastMapRegister().isWantMapNotify());
    }

    @Test
    public void mapRegister__SetMBitWithNonZeroReservedBits() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 "
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 03 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 a2 72 40 7b 1a ae "
                + "0040   4e 6b e2 e5 e1 01 40 8a c9 e1 d1 80 cb 72 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 "
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handleMapRegisterPacket(registerWithNonSetMBit);
        assertTrue(lastMapRegister().isWantMapNotify());
    }

    /**
     * Tests whether handling of map-register message will generate mapping-keep-alive notification.
     */
    @Test
    public void mapRegister_isMappingKeepAliveAndMapNotifyGenerated() throws InterruptedException,
            UnknownHostException {
        byte[] eidPrefixAfi = new byte[] {
            0x00, 0x01                  //eid-prefix-afi
        };

        byte[] eidPrefix = new byte[] {
            0x0a, 0x0a, 0x0a, 0x0a     //ipv4 address
        };

        NotificationPublishService notifServiceMock = MapRegisterCacheTestUtil.resetMockForNotificationProvider(
                mockLispSouthboundPlugin);

        //send stream of byte -> map register message
        final MapRegisterCacheKey cacheKey = MapRegisterCacheTestUtil.createMapRegisterCacheKey(eidPrefix);
        MapRegisterCacheTestUtil.beforeMapRegisterInvocationValidation(cacheKey, mapRegisterCache);
        mapRegisterInvocationForCacheTest(eidPrefixAfi, eidPrefix);
        MapRegisterCacheTestUtil.afterMapRegisterInvocationValidation(notifServiceMock,
                cacheKey, mapRegisterCache, eidPrefixAfi, eidPrefix);

        //sending the same byte stream -> map register second time
        notifServiceMock = MapRegisterCacheTestUtil.resetMockForNotificationProvider(mockLispSouthboundPlugin);
        mapRegisterInvocationForCacheTest(eidPrefixAfi, eidPrefix);

        //mapping-keep-alive message should be generated
        MapRegisterCacheTestUtil.afterSecondMapRegisterInvocationValidation(notifServiceMock,
                mockLispSouthboundPlugin, eidPrefixAfi, eidPrefix);
    }

    void mapRegisterInvocationForCacheTest(byte[] eidPrefixAfi, byte[] eidPrefix) {
        mapRegisterInvocationForCacheTest(eidPrefixAfi, eidPrefix, MapRegisterCacheTestUtil.AUTH_DATA);
    }

    void mapRegisterInvocationForCacheTest(byte[] eidPrefixAfi, byte[] eidPrefix, byte[] authenticationData) {
        final byte[] mapRegisterMessage = MapRegisterCacheTestUtil.joinArrays(DATA1, NONCE, KEY_ID,
                authenticationData, DATA2, eidPrefixAfi, eidPrefix, DATA3, XTR_ID, SITE_ID);
        handlePacket(mapRegisterMessage);
    }

    /**
     * It tests whether map register message is stored to local cache with Ipv4 EidPrefix.
     */
    @Test
    public void mapRegister_cacheWithEidPrefixIpv4Test() throws InterruptedException {
        byte[] eidPrefixAfi = new byte[] {
            0x00, 0x01                  //eid-prefix-afi
        };

        byte[] eidPrefix = new byte[] {
            0x0a, 0x0a, 0x0a, 0x0a     //ipv4 address
        };

        cacheTest(eidPrefixAfi, eidPrefix, MapRegisterCacheTestUtil.AUTH_DATA);
    }

    /**
     * It tests whether map register message is stored to local cache with Ipv6 EidPrefix.
     */
    @Test
    public void mapRegister_cacheWithEidPrefixIpv6Test() throws InterruptedException {
        byte[] eidPrefixAfi = new byte[] {
            0x00, 0x02                  //eid-prefix-afi
        };

        byte[] eidPrefix = new byte[] {
            0x0f, 0x0f, 0x0f, 0x0f,     //ipv6 address
            0x0f, 0x0f, 0x0f, 0x0f,     //ipv6 address
            0x0f, 0x0f, 0x0f, 0x0f,     //ipv6 address
            0x0f, 0x0f, 0x0f, 0x0f      //ipv6 address
        };

        byte[] authenticationData = new byte[]{
            0x00, 0x14,
            0x41,(byte)0x83,0x13,0x7C,
            0x48,(byte)0xEE,0x75,(byte)0x9A,
            0x4,(byte)0x8C,0x46,(byte)0xA6,
            0x1B,0x13,(byte)0xC8,0x4D,
            (byte)0xA1,0x17,0x53,(byte)0xC3
        };

        cacheTest(eidPrefixAfi, eidPrefix, authenticationData);
    }

    /**
     * It tests whether map register message is stored to local cache with Mac 48bits EidPrefix.
     */
    @Test
    public void mapRegister_cacheWithEidPrefixMac48Test() throws InterruptedException {
        byte[] eidPrefixAfi = new byte[] {
            0x40, 0x05                  //eid-prefix-afi
        };

        byte[] eidPrefix = new byte[] {
            0x0a, 0x0b, 0x0c, 0x0d,     //mac address
            0x0e, 0x0f                 //mac address
        };
        byte[] authenticationData = new byte[]{
            0x00, 0x14,
            (byte)0xB2,(byte)0x8E,0x6,(byte)0x9D,
            0x61,(byte)0xD8,0xC,0x24,
            (byte)0x80,0x61,0x5A,0x20,
            0xD,0x50,0x5E,(byte)0xAE,
            0x47,(byte)0xF7,(byte)0x86,0x36
        };
        cacheTest(eidPrefixAfi, eidPrefix, authenticationData);
    }

    /**
     * It tests whether map register message is stored to local cache with Lcaf EidPrefix (inside Ipv4).
     */
    @Test
    public void mapRegister_cacheWithEidPrefixLcafTest() throws InterruptedException {
        byte[] eidPrefixAfi = new byte[] {
            0x40, 0x03                  //eid-prefix-afi
        };

        //following lcaf prefixed variables are defined according to https://tools.ietf
        // .org/html/draft-ietf-lisp-lcaf-12#section-4.1
        byte[] lcafRsvd1 = new byte[]{0x00};
        byte[] lcafFlags = new byte[]{0x00};
        byte[] lcafType = new byte[]{0x02};
        byte[] lcafIIDMaskLength = new byte[]{0x20};
        byte[] lcafLength = new byte[] {0x00, 0x0a};
        byte[] lcafInstanceId = new byte[]{0x00, 0x00, 0x00, 0x15};
        byte[] lcafAfi = new byte[] {0x00, 0x01};
        byte[] lcafAddress = new byte[] {0x7d, 0x7c, 0x7b, 0x7a};

        byte[] eidPrefix = joinArrays(lcafRsvd1, lcafFlags, lcafType, lcafIIDMaskLength, lcafLength, lcafInstanceId,
                lcafAfi, lcafAddress);

        byte[] authenticationData = new byte[]{
            0x00, 0x14,                 //authentication data length
            0x68, 0x1d, (byte) 0x9e, 0x6e,    //auth data
            0x5e, 0x32, (byte) 0x88, 0x1a,    //auth data
            (byte) 0xae, 0x6b, (byte) 0xe3, 0x40,    //auth data
            0x30, (byte) 0x0b, (byte) 0xb6, (byte) 0xa0,    //auth data
            0x71, (byte) 0xf4, (byte) 0x8c, 0x5f    //auth data
        };


        cacheTest(eidPrefixAfi, eidPrefix, authenticationData);
    }

    /**
     * It tests whether map register message is stored to local cache.
     */
    public void cacheTest(byte[] eidPrefixAfi, byte[] eidPrefix, byte[] authenticationData) throws
            InterruptedException {
        final MapRegisterCacheKey mapRegisterCacheKey = MapRegisterCacheTestUtil.createMapRegisterCacheKey(eidPrefix);

        MapRegisterCacheTestUtil.beforeMapRegisterInvocationValidation(mapRegisterCacheKey, mapRegisterCache);
        mapRegisterInvocationForCacheTest(eidPrefixAfi, eidPrefix, authenticationData);
        MapRegisterCacheTestUtil.afterMapRegisterInvocationValidation(nps,
                mapRegisterCacheKey, mapRegisterCache, eidPrefixAfi, eidPrefix);
    }


    /*
        Checks whether old record from cache will expire and is replaced with new record.

        add to empty cache record with timestamp older then 90 second - one object
        add the same entry through calling of handleMapRegister
        check that removeEntry() and addEntry() (or refreshEntry() was called on mocked object for mapRegisterCache
    */
    @Test
    public void mapRegister_cacheRecordExpirationTest() throws InterruptedException {
        //tests handling of map register message when next message comes:

        //after cache entry timeout
        cacheRecordExpirationTest(true);

        //before cache entry timout
        cacheRecordExpirationTest(false);
    }

    private void cacheRecordExpirationTest(boolean cacheRecordTimeouted) throws InterruptedException {
        final byte[] eidPrefixAfi = new byte[] {0x00, 0x01};
        final byte[] eidPrefix = new byte[] {0x0a, 0x0a, 0x0a, 0x0a};

        MapRegisterCacheKeyBuilder cacheKeyBld = new MapRegisterCacheKeyBuilder();
        cacheKeyBld.setXtrId(XTR_ID);
        cacheKeyBld.setEidPrefix(eidPrefix);
        cacheKeyBld.setSiteId(SITE_ID);

        MapRegisterCacheMetadataBuilder cacheMetadataBld = new MapRegisterCacheMetadataBuilder();
        cacheMetadataBld.setTimestamp(System.currentTimeMillis() - (cacheRecordTimeouted ? CACHE_RECORD_TIMEOUT : 0L));
        cacheMetadataBld.setWantMapNotify(false);

        MapRegisterCacheValueBuilder cacheValueBld = new MapRegisterCacheValueBuilder();
        cacheValueBld.setMapRegisterCacheMetadata(cacheMetadataBld.build());
        cacheValueBld.setPacketData(MapRegisterCacheTestUtil.joinArrays(DATA1, KEY_ID, DATA2, eidPrefixAfi,
                eidPrefix, DATA3, XTR_ID, SITE_ID));


        final MapRegisterCacheKey cacheKey = cacheKeyBld.build();
        final MapRegisterCacheValue cacheValue = cacheValueBld.build();

        Mockito.when(mapRegisterCache.getEntry(Mockito.eq(cacheKey))).thenReturn(cacheRecordTimeouted ? null :
                cacheValue);
        Mockito.when(mapRegisterCache.refreshEntry(Mockito.eq(cacheKey))).thenReturn(cacheValue);

        mapRegisterInvocationForCacheTest(eidPrefixAfi, eidPrefix);

        InOrder inOrder = Mockito.inOrder(mapRegisterCache);
        inOrder.verify(mapRegisterCache).getEntry(Mockito.eq(cacheKey));

        if (cacheRecordTimeouted) {
            inOrder.verify(mapRegisterCache).addEntry(Mockito.eq(cacheKey), AdditionalMatchers.not(Mockito.eq(
                    cacheValue)));
        } else {
            inOrder.verify(mapRegisterCache).refreshEntry(Mockito.eq(cacheKey));
        }
    }

    @Test
    @Ignore
    public void mapRegisterAndNotify__ValidExtraDataParsedSuccessfully() throws Exception {
        byte[] extraDataPacket = new byte[mapRegisterPacket.length + 3];
        extraDataPacket[mapRegisterPacket.length] = 0x9;
        System.arraycopy(mapRegisterPacket, 0, extraDataPacket, 0, mapRegisterPacket.length);
        stubMapRegister(true);

        DatagramPacket dp = new DatagramPacket(wrappedBuffer(extraDataPacket), new InetSocketAddress(0),
                new InetSocketAddress(0));
        testedLispService.handlePacket(dp);
        // Check map register fields.
        // XXX: test
        // byte[] notifyResult = testedLispService.handlePacket(dp).getData();
        byte[] notifyResult = lastMapNotifyPacket().content().array();
        assertEquals(mapRegisterPacket.length, notifyResult.length);

    }

    private DatagramPacket lastMapReplyPacket() {
        ByteBuffer serialize = MapReplySerializer.getInstance().serialize(mapReplyBuilder.build());
        return new DatagramPacket(wrappedBuffer(serialize), new InetSocketAddress(0), new InetSocketAddress(0));
    }

    private DatagramPacket lastMapNotifyPacket() {
        if (mapNotifyBuilder.getMappingRecordItem() == null) {
            mapNotifyBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        }
        mapNotifyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(
                mappingRecordBuilder.build()).build());
        mapNotifyBuilder.setNonce((long) 0);
        mapNotifyBuilder.setKeyId((short) 0);
        mapNotifyBuilder.setAuthenticationData(new byte[0]);
        ByteBuffer serialize = MapNotifySerializer.getInstance().serialize(mapNotifyBuilder.build());
        return new DatagramPacket(wrappedBuffer(serialize), new InetSocketAddress(0), new InetSocketAddress(0));
    }

    @Test
    @Ignore
    public void mapNotify__VerifyBasicFields() throws Exception {
        byte registerType = mapRegisterPacket[0];
        assertEquals(MessageType.MapRegister.getIntValue(), registerType >> 4);

        stubMapRegister(true);

        byte[] result = handleMapRegisterAsByteArray(mapRegisterPacket);

        assertEquals(mapRegisterPacket.length, result.length);

        byte expectedType = (byte) (MessageType.MapNotify.getIntValue() << 4);
        assertHexEquals(expectedType, result[0]);
        assertHexEquals((byte) 0x00, result[1]);
        assertHexEquals((byte) 0x00, result[2]);

        byte[] registerWithoutTypeWithoutAuthenticationData = ArrayUtils.addAll(
                Arrays.copyOfRange(mapRegisterPacket, 3, 16),
                Arrays.copyOfRange(mapRegisterPacket, 36, mapRegisterPacket.length));
        byte[] notifyWithoutTypeWithOutAuthenticationData = ArrayUtils.addAll(Arrays.copyOfRange(result, 3, 16),
                Arrays.copyOfRange(result, 36, result.length));
        ArrayAssert.assertEquals(registerWithoutTypeWithoutAuthenticationData,
                notifyWithoutTypeWithOutAuthenticationData);
    }

    @Ignore
    @Test
    public void mapNotify__VerifyPort() throws Exception {
        stubMapRegister(true);

        DatagramPacket notifyPacket = handleMapRegisterPacket(mapRegisterPacket);
        assertEquals(LispMessage.PORT_NUM, notifyPacket.recipient().getPort());
    }

    @Test
    public void mapRequest__VerifyBasicFields() throws Exception {
        oneOf(nps).putNotification(with(lispNotificationSaver));
        handleMapRequestAsByteArray(mapRequestPacket);
        List<EidItem> eids = lastMapRequest().getEidItem();
        assertEquals(1, eids.size());
        Eid lispAddress = eids.get(0).getEid();
        assertEquals(Ipv4PrefixBinaryAfi.class, lispAddress.getAddressType());
        assertEquals(LispAddressUtil.asIpv4PrefixBinaryEid("1.2.3.4/32"), lispAddress);
        assertEquals(0x3d8d2acd39c8d608L, lastMapRequest().getNonce().longValue());
    }

    @Test
    public void mapRequest__Ipv6Eid() throws Exception {
        // Internet Protocol Version 6, Src: 2610:d0:ffff:192::1
        // (2610:d0:ffff:192::1), Dst: 2610:d0:ffff:192::2
        // (2610:d0:ffff:192::2)
        // MBIT: SET
        // EID AFI: 2
        // Source EID: 2610:d0:ffff:192::1 (2610:d0:ffff:192::1)
        // ITR-RLOC 1: 10.0.58.156
        // Record 1: 2610:d0:ffff:192::2/128
        // Map-Reply Record: EID prefix: 2610:d0:ffff:192::1/128, TTL: 10,
        // Authoritative, No-Action

        mapRequestPacket = extractWSUdpByteArray(new String(
                  "0000   00 0c 29 34 3e 1b 00 0c 29 f6 d6 0d 08 00 45 00 "
                + "0010   00 b0 00 00 40 00 40 11 ea 7b 0a 00 3a 9c 0a 00 "
                + "0020   01 26 10 f6 10 f6 00 9c 9b 19 80 00 00 00 60 00 "
                + "0030   00 00 00 68 11 ff 26 10 00 d0 ff ff 01 92 00 00 "
                + "0040   00 00 00 00 00 01 26 10 00 d0 ff ff 01 92 00 00 "
                + "0050   00 00 00 00 00 02 10 f6 10 f6 00 68 94 8b 10 00 "
                + "0060   00 01 ff f5 bf 5d 7b 75 93 e6 00 02 26 10 00 d0 "
                + "0070   ff ff 01 92 00 00 00 00 00 00 00 01 00 01 0a 00 "
                + "0080   3a 9c 00 80 00 02 26 10 00 d0 ff ff 01 92 00 00 "
                + "0090   00 00 00 00 00 02 00 00 00 0a 01 80 10 00 00 00 "
                + "00a0   00 02 26 10 00 d0 ff ff 01 92 00 00 00 00 00 00 "
                + "00b0   00 01 01 64 ff 00 00 05 00 01 0a 00 3a 9c"));

        oneOf(nps).putNotification(with(lispNotificationSaver));
        // ret(mapReply);

        handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(LispAddressUtil.asIpv6Eid("2610:d0:ffff:192:0:0:0:1"), lastMapRequest().getSourceEid().getEid());
        assertEquals(LispAddressUtil.asIpv6PrefixBinaryEid("2610:d0:ffff:192:0:0:0:2/128"),
                lastMapRequest().getEidItem().get(0).getEid());
    }

    @Ignore
    @Test
    public void mapRequest__UsesIpv6EncapsulatedUdpPort() throws Exception {
        // Internet Protocol Version 6, Src: 2610:d0:ffff:192::1
        // (2610:d0:ffff:192::1), Dst: 2610:d0:ffff:192::2
        // (2610:d0:ffff:192::2)
        // encapsulated UDP source port: 4342

        mapRequestPacket = extractWSUdpByteArray(new String(
                  "0000   00 0c 29 34 3e 1b 00 0c 29 f6 d6 0d 08 00 45 00 "
                + "0010   00 b0 00 00 40 00 40 11 ea 7b 0a 00 3a 9c 0a 00 "
                + "0020   01 26 10 f6 10 f6 00 9c 9b 19 80 00 00 00 60 00 "
                + "0030   00 00 00 68 11 ff 26 10 00 d0 ff ff 01 92 00 00 "
                + "0040   00 00 00 00 00 01 26 10 00 d0 ff ff 01 92 00 00 "
                + "0050   00 00 00 00 00 02 10 f6 10 f6 00 68 94 8b 14 00 "
                + "0060   00 01 ff f5 bf 5d 7b 75 93 e6 00 02 26 10 00 d0 "
                + "0070   ff ff 01 92 00 00 00 00 00 00 00 01 00 01 0a 00 "
                + "0080   3a 9c 00 80 00 02 26 10 00 d0 ff ff 01 92 00 00 "
                + "0090   00 00 00 00 00 02 00 00 00 0a 01 80 10 00 00 00 "
                + "00a0   00 02 26 10 00 d0 ff ff 01 92 00 00 00 00 00 00 "
                + "00b0   00 01 01 64 ff 00 00 05 00 01 0a 00 3a 9c"));
        oneOf(nps).putNotification(with(lispNotificationSaver));
        // ret(mapReply);

        DatagramPacket replyPacket = handleMapRequestPacket(mapRequestPacket);
        assertEquals(4342, replyPacket.recipient().getPort());
    }

    @Test
    public void mapRequest__WithSourceEid() throws Exception {
        // encapsulated LISP packet
        // Source EID = 153.16.254.1

        mapRequestPacket = extractWSUdpByteArray(new String(
                  "0000   00 0c 29 7a ce 83 00 15 17 c6 4a c9 08 00 45 00 "
                + "0010   00 78 00 00 40 00 3e 11 ec b1 0a 00 01 26 0a 00 "
                + "0020   3a 9e 10 f6 10 f6 00 64 c3 a5 80 00 00 00 45 00 "
                + "0030   00 58 d4 31 00 00 ff 11 31 89 99 10 fe 01 0a 00 "
                + "0040   14 c8 10 f6 10 f6 00 44 84 ee 10 00 00 01 ba f9 "
                + "0050   ff 53 27 36 38 3a 00 01 99 10 fe 01 00 01 0a 00 "
                + "0060   01 26 00 20 00 01 0a 00 14 c8 00 00 00 0a 01 20 "
                + "0070   10 00 00 00 00 01 99 10 fe 01 01 64 ff 00 00 05 "
                + "0080   00 01 0a 00 01 26"));

        oneOf(nps).putNotification(with(lispNotificationSaver));
        // ret(mapReply);

        handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(Ipv4BinaryAfi.class, lastMapRequest().getSourceEid().getEid().getAddressType());

    }

    @Test
    @Ignore
    public void mapReply__VerifyBasicIPv4Fields() throws Exception {
        mappingRecordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("10.0.20.200/32"));
        mapReplyBuilder.setNonce(0x3d8d2acd39c8d608L);

        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);

        assertEquals(28, result.length);

        byte expectedLispMessageType = 2;
        assertEquals(expectedLispMessageType, (byte) (result[LispMessage.Pos.TYPE] >> 4));
        assertEquals(0x3d8d2acd39c8d608L, ByteUtil.getLong(result, MapReplyIpv4SingleLocatorPos.NONCE));

        byte expectedRecordCount = (byte) 1;
        assertEquals(expectedRecordCount, result[MapReplyIpv4SingleLocatorPos.RECORD_COUNT]);

        assertEquals(MaskUtil.getMaskForAddress(mappingRecordBuilder.getEid().getAddress()),
                result[MapReplyIpv4SingleLocatorPos.EID_MASK_LEN]);
        assertEquals(AddressFamily.IpV4.getIntValue(), ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.AFI_TYPE));
        assertEquals(0x0a0014c8, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.EID_PREFIX));
    }

    @Test
    @Ignore
    public void mapReply__VerifyBasicIPv6() throws Exception {
        mappingRecordBuilder.setEid(LispAddressUtil.asIpv6PrefixEid("0:0:0:0:0:0:0:1/128"));

        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);

        assertEquals(40, result.length);

        byte expectedRecordCount = (byte) 1;
        assertEquals(expectedRecordCount, result[MapReplyIpv4SingleLocatorPos.RECORD_COUNT]);

        assertEquals(MaskUtil.getMaskForAddress(mappingRecordBuilder.getEid().getAddress()),
                result[MapReplyIpv4SingleLocatorPos.EID_MASK_LEN]);
        assertEquals(AddressFamily.IpV6.getIntValue(), ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.AFI_TYPE));
        byte[] expectedIpv6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };

        ArrayAssert.assertEquals(expectedIpv6, Arrays.copyOfRange(result, 24, 40));
    }

    @Test
    @Ignore
    public void mapReply__VerifyIPv6EidAndLocator() throws Exception {
        mappingRecordBuilder.setEid(LispAddressUtil.asIpv6PrefixEid("0:0:0:0:0:0:0:1/128"));
        mappingRecordBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRloc(LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:2")).build());

        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);

        assertEquals(64, result.length);

        byte[] expectedIpv6Eid = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        ArrayAssert.assertEquals(expectedIpv6Eid, Arrays.copyOfRange(result, 24, 40));

        byte[] expectedIpv6Rloc = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2 };
        ArrayAssert.assertEquals(expectedIpv6Rloc, Arrays.copyOfRange(result, 48, 64));
    }

    @Ignore
    @Test
    public void mapReply__UseEncapsulatedUdpPort() throws Exception {
        stubHandleRequest();

        assertEquals(LispMessage.PORT_NUM, handleMapRequestPacket(mapRequestPacket).recipient().getPort());
    }

    @Test
    @Ignore
    public void mapReply__WithNonRoutableSingleLocator() throws Exception {
        mappingRecordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("10.0.20.200/32"));
        mappingRecordBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(false).setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1")).build());
        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(0x00, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    @Ignore
    public void mapReply__WithSingleLocator() throws Exception {
        mappingRecordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("10.0.20.200/32"));
        mappingRecordBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(true).setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1")).build());
        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(40, result.length);

        byte expectedLocCount = 1;
        assertEquals(expectedLocCount, result[MapReplyIpv4SingleLocatorPos.LOCATOR_COUNT]);

        assertEquals(AddressFamily.IpV4.getIntValue(), ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOC_AFI));

        assertEquals(0x04030201, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOCATOR));
        assertEquals(0x01, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    @Ignore
    public void mapReply__WithMultipleLocator() throws Exception {
        mappingRecordBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(true).setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1")).build());
        mappingRecordBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(true).setRloc(LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:1"))
                .build());
        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(64, result.length);

        assertEquals(2, result[MapReplyIpv4SingleLocatorPos.LOCATOR_COUNT]);

        assertEquals(AddressFamily.IpV4.getIntValue(), ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOC_AFI));
        assertEquals(0x04030201, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOCATOR));
        assertEquals(0x01, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);

        assertEquals(AddressFamily.IpV6.getIntValue(), ByteUtil.getInt(result, MapReplyIpv4SecondLocatorPos.LOC_AFI));

        byte[] expectedIpv6Rloc = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        ArrayAssert.assertEquals(expectedIpv6Rloc, Arrays.copyOfRange(result, MapReplyIpv4SecondLocatorPos.LOCATOR,
                MapReplyIpv4SecondLocatorPos.LOCATOR + 16));

        assertEquals(0x01, result[MapReplyIpv4SecondLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    public void handleUnknownLispMessage() throws Exception {
        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 14 UNKNOWN!!!, P=1, M=1

        byte[] unknownTypePacket = extractWSUdpByteArray(new String(
                  "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 "
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 F8 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 "
                + "0040   b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 "
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        assertNull(handlePacket(unknownTypePacket));
    }

    @Test
    public void mapRequest__MultipleItrRlocs() throws Exception {
        // this is what LISPmob sends when configured multiple RLOCs for single
        // EID.
        // ITR-RLOC 1: 10.1.0.111
        // ITR-RLOC 2: 192.168.136.51
        //
        mapRequestPacket = extractWSUdpByteArray(new String(
                  "0000   00 0c 29 7a ce 8d 00 0c 29 e4 ef 70 08 00 45 00 "
                + "0010   00 8a 00 00 40 00 40 11 25 f2 0a 01 00 6f 0a 01 "
                + "0020   00 01 10 f6 10 f6 00 76 06 1f 80 00 00 00 45 00 "
                + "0030   00 6a d4 31 00 00 ff 11 2a 3e ac 01 01 02 08 08 "
                + "0040   08 08 10 f6 10 f6 00 56 63 14 10 00 01 01 79 67 "
                + "0050   ff 75 a0 61 66 19 00 01 ac 01 01 02 00 01 0a 01 "
                + "0060   00 6f 00 01 c0 a8 88 33 00 20 00 01 08 08 08 08 "
                + "0070   00 00 00 0a 02 20 10 00 00 00 00 01 ac 01 01 02 "
                + "0080   01 64 ff 00 00 05 00 01 0a 01 00 6f 06 64 ff 00 "
                + "0090   00 05 00 01 c0 a8 88 33"));

        oneOf(nps).putNotification(with(lispNotificationSaver));
        handleMapRequestAsByteArray(mapRequestPacket);

    }

    private void stubMapRegister(final boolean setNotifyFromRegister) {
        try {
            allowing(nps).putNotification(with(lispNotificationSaver));
        } catch (InterruptedException e) {
            LOG.debug("Interrupted", e);
        }
        will(new SimpleAction() {

            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                if (setNotifyFromRegister) {
                    MapNotifyBuilderHelper.setFromMapRegister(mapNotifyBuilder, lastMapRegister());
                }
                return null;
            }
        });
    }

    private void stubHandleRequest() {
        try {
            allowing(nps).putNotification(wany(Notification.class));
        } catch (InterruptedException e) {
            LOG.debug("Interrupted", e);
        }
    }

    private byte[] handleMapRequestAsByteArray(byte[] inPacket) {
        handleMapRequestPacket(inPacket);
        return lastMapReplyPacket().content().array();
    }

    private byte[] handleMapRegisterAsByteArray(byte[] inPacket) {
        handleMapRegisterPacket(inPacket);
        return lastMapNotifyPacket().content().array();
    }

    private DatagramPacket handleMapRequestPacket(byte[] inPacket) {
        DatagramPacket dp = new DatagramPacket(wrappedBuffer(inPacket), new InetSocketAddress(0),
                new InetSocketAddress(0));
        // Unless we explicitly set the source port, it will be -1, which breaks some tests
        // This is till not the real port number, but it's better
        //dp.setPort(LispMessage.PORT_NUM);
        testedLispService.handlePacket(dp);
        return lastMapReplyPacket();
    }

    private DatagramPacket handleMapRegisterPacket(byte[] inPacket) {
        DatagramPacket dp = new DatagramPacket(wrappedBuffer(inPacket), new InetSocketAddress(0),
                new InetSocketAddress(0));
        // Unless we explicitly set the source port, it will be -1, which breaks some tests
        // This is till not the real port number, but it's better
        //dp.setPort(LispMessage.PORT_NUM);
        testedLispService.handlePacket(dp);
        if (mapNotifyBuilder == null) {
            return null;
        } else {
            return lastMapNotifyPacket();
        }
    }

    private DatagramPacket handlePacket(byte[] inPacket) {
        // TODO get from mock
        testedLispService.handlePacket(new DatagramPacket(wrappedBuffer(inPacket), new InetSocketAddress(0),
                new InetSocketAddress(0)));
        return null;
    }

    private byte[] extractWSUdpByteArray(String wiresharkHex) {
        final int headerLen = 42;
        byte[] res = new byte[1000];
        String[] split = wiresharkHex.split(" ");
        int counter = 0;
        for (String cur : split) {
            cur = cur.trim();
            if (cur.length() == 2) {
                ++counter;
                if (counter > headerLen) {
                    res[counter - headerLen - 1] = (byte) Integer.parseInt(cur, 16);
                }

            }
        }
        return Arrays.copyOf(res, counter - headerLen);
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__NoIpItrRloc() throws Exception {
        mapRequestPacket = hexToByteBuffer("10 00 " //
                + "02 " // This means 3 ITR - RLOCs
                + "01 3d 8d 2a cd 39 c8 d6 08 00 00 " //
                + "40 05 c0 a8 88 0a 01 02 " // MAC (ITR-RLOC #1 of 3)
                + "40 05 00 00 00 00 00 00 " // MAC (ITR-RLOC #2 of 3)
                + "40 05 11 22 34 56 78 90 " // MAC (ITR-RLOC #3 of 3)
                + "00 20 00 01 01 02 03 04").array();
        handleMapRequestPacket(mapRequestPacket);
    }

    // @Ignore
    // @Test
    // public void mapRequest__IPITRRLOCIsSecond() throws Exception {
    // mapRequestPacket = hexToByteBuffer("10 00 " //
    // + "01 " // This means 3 ITR - RLOCs
    // + "01 3d 8d 2a cd 39 c8 d6 08 00 00 " //
    // + "40 05 c0 a8 88 0a 01 02 " // MAC (ITR-RLOC #1 of 2)
    // + "00 01 01 02 03 04 " // IP (ITR-RLOC #2 of 2)
    // + "00 20 00 01 01 02 03 04").array();
    // oneOf(nps).putNotification(with(lispNotificationSaver));
    // // ret(mapReply);
    // DatagramPacket packet = handleMapRequestPacket(mapRequestPacket);
    // assertEquals(2, lastMapRequest().getItrRlocs().size());
    // assertEquals((new LispIpv4Address("1.2.3.4")).getAddress(),
    // packet.getAddress());
    // }
    //
    // @Ignore
    // @Test
    // public void mapRequest__MULTIPLEIPITRRLOCs() throws Exception {
    // mapRequestPacket = hexToByteBuffer("10 00 " //
    // + "01 " // This means 3 ITR - RLOCs
    // + "01 3d 8d 2a cd 39 c8 d6 08 00 00 " //
    // + "00 01 01 02 03 04 " // IP (ITR-RLOC #1 of 2)
    // + "00 01 c0 a8 88 0a " // MAC (ITR-RLOC #2 of 2)
    // + "00 20 00 01 01 02 03 04").array();
    // oneOf(nps).putNotification(with(lispNotificationSaver));
    // // ret(mapReply);
    // DatagramPacket packet = handleMapRequestPacket(mapRequestPacket);
    // assertEquals(2, lastMapRequest().getItrRloc().size());
    // assertEquals((new LispIpv4Address("1.2.3.4")).getAddress(),
    // packet.getAddress());
    // }

}
