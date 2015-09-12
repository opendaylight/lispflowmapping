/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junitx.framework.ArrayAssert;
import junitx.framework.Assert;

import org.apache.commons.lang3.ArrayUtils;
import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.type.LispMessageEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.RequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yangtools.yang.binding.Notification;

public class LispSouthboundServiceTest extends BaseTestCase {

    private LispSouthboundService testedLispService;
    private NotificationProviderService nps;
    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacket;
    private ValueSaverAction<Notification> lispNotificationSaver;
    // private ValueSaverAction<MapRegister> mapRegisterSaver;
    // private ValueSaverAction<MapRequest> mapRequestSaver;
    private MapNotifyBuilder mapNotifyBuilder;
    private MapReplyBuilder mapReplyBuilder;
    private EidToLocatorRecordBuilder eidToLocatorBuilder;

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

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        // mapResolver = context.mock(IMapResolver.class);
        // mapServer = context.mock(IMapServer.class);
        testedLispService = new LispSouthboundService(null);
        nps = context.mock(NotificationProviderService.class);
        testedLispService.setNotificationProvider(nps);
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
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " //
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 38 d4 31 00 00 ff 11 56 f3 c0 a8 88 0a 01 02 "
                + "0040   03 04 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "
                + "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 c0 a8 88 0a 00 20 " //
                + "0060   00 01 01 02 03 04"));
        mapReplyBuilder = new MapReplyBuilder();
        mapReplyBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapReplyBuilder.setNonce((long) 0);
        mapReplyBuilder.setEchoNonceEnabled(false);
        mapReplyBuilder.setProbe(true);
        mapReplyBuilder.setSecurityEnabled(true);
        eidToLocatorBuilder = new EidToLocatorRecordBuilder();
        String ip = "0.0.0.0";
        eidToLocatorBuilder.setLispAddressContainer(LispAFIConvertor.asIPv4Prefix(ip, 0));
        eidToLocatorBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        eidToLocatorBuilder.setRecordTtl(10);
        eidToLocatorBuilder.setMapVersion((short) 0);
        eidToLocatorBuilder.setMaskLength((short) 0);
        eidToLocatorBuilder.setAction(Action.NativelyForward);
        eidToLocatorBuilder.setAuthoritative(false);
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

        mapRegisterPacket = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
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
        mapRegisterPacket = extractWSUdpByteArray(new String("0000   00 0c 29 7a ce 8d 00 0c 29 e4 ef 70 08 00 45 00 "
                + "0010   00 68 00 00 40 00 40 11 26 15 0a 01 00 6e 0a 01 " //
                + "0020   00 01 10 f6 10 f6 00 54 03 3b 38 00 01 01 00 00 "));

        handleMapRegisterPacket(mapRegisterPacket);
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__IllegalPacket() throws Exception {
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " //
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 38 d4 31 00 00 ff 11 56 f3 c0 a8 88 0a 01 02 " //
                + "0040   03 04 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "));
        handleMapRequestPacket(mapRequestPacket);
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__IllegalEncapsulatedPacket() throws Exception {
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " //
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 " //
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
        mapRegisterPacket = extractWSUdpByteArray(new String("0000   00 0c 29 7a ce 8d 00 0c 29 e4 ef 70 08 00 45 00 "
                + "0010   00 68 00 00 40 00 40 11 26 15 0a 01 00 6e 0a 01 " //
                + "0020   00 01 10 f6 10 f6 00 54 03 3b 38 00 01 01 00 00 " //
                + "0030   00 00 00 00 00 00 00 01 00 14 ae d8 7b d4 9c 59 " //
                + "0040   e9 35 75 6e f1 29 27 a3 45 20 96 06 c2 e1 00 00 " //
                + "0050   00 0a 02 20 10 00 00 00 00 01 ac 01 01 02 01 64 " //
                + "0060   ff 00 00 05 00 01 0a 01 00 6e 06 64 ff 00 00 05 " //
                + "0070   00 01 c0 a8 88 33"));

        oneOf(nps).publish(with(lispNotificationSaver));

        handleMapRegisterPacket(mapRegisterPacket);

        List<EidToLocatorRecord> eidRecords = lastMapRegister().getEidToLocatorRecord();
        assertEquals(1, eidRecords.size());
        EidToLocatorRecord eidRecord = eidRecords.get(0);
        assertEquals(2, eidRecord.getLocatorRecord().size());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.1.0.110")), eidRecord.getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("192.168.136.51")), eidRecord.getLocatorRecord().get(1).getLispAddressContainer());
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

        mapRegisterPacket = extractWSUdpByteArray(new String("0000   00 0c 29 34 3e 1b 00 0c 29 f6 d6 0d 08 00 45 00 " //
                + "0010   00 68 00 00 40 00 40 11 ea c3 0a 00 3a 9c 0a 00 " //
                + "0020   01 26 10 f6 10 f6 00 54 f5 9a 38 00 03 01 00 00 " //
                + "0030   00 00 00 00 00 00 00 01 00 14 22 97 ff 61 ec d8 " //
                + "0040   0f 91 c6 c4 01 ef 7f bb 77 58 39 5c 92 23 00 00 " //
                + "0050   00 0a 01 80 10 00 00 00 00 02 26 10 00 d0 ff ff " //
                + "0060   01 92 00 00 00 00 00 00 00 01 01 64 ff 00 00 05 " //
                + "0070   00 01 0a 00 3a 9c"));

        oneOf(nps).publish(with(lispNotificationSaver));

        handleMapRegisterPacket(mapRegisterPacket);

        EidToLocatorRecord eidToLocatorRecord = lastMapRegister().getEidToLocatorRecord().get(0);
        assertEquals(LispAFIConvertor.asIPv6Address("2610:d0:ffff:192:0:0:0:1"), eidToLocatorRecord.getLispAddressContainer());
        assertEquals(AddressFamilyNumberEnum.IP6,
                AddressFamilyNumberEnum.valueOf(LispAFIConvertor.toAFI(eidToLocatorRecord.getLispAddressContainer()).getAfi()));

        assertEquals(LispAFIConvertor.asIPAfiAddress("10.0.58.156"), LispAFIConvertor.toAFI(eidToLocatorRecord.getLocatorRecord().get(0).getLispAddressContainer()));
    }

    @Test
    public void mapRegister__VerifyBasicFields() throws Exception {
        oneOf(nps).publish(with(lispNotificationSaver));
        handleMapRegisterPacket(mapRegisterPacket);

        EidToLocatorRecord eidToLocator = lastMapRegister().getEidToLocatorRecord().get(0);
        assertEquals(LispAFIConvertor.asIPv4Address("153.16.254.1"), eidToLocator.getLispAddressContainer());

        assertEquals(1, eidToLocator.getLocatorRecord().size());
        assertEquals(LispAFIConvertor.asIPAfiAddress("192.168.136.10"), LispAFIConvertor.toAFI(eidToLocator.getLocatorRecord().get(0).getLispAddressContainer()));
    }

    @Test
    @Ignore
    public void mapRegister__NoResponseFromMapServerShouldReturnNullPacket() throws Exception {
        oneOf(nps).publish(with(lispNotificationSaver));
        mapNotifyBuilder = null;

        assertNull(handleMapRegisterPacket(mapRegisterPacket));
    }

    @Test
    public void mapRegister__NonSetMBit() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df " //
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 00 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 79 d1 44 66 19 99 "
                + "0040   83 63 a7 79 6e f0 40 97 54 26 3a 44 b4 eb 00 00 " //
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handleMapRegisterPacket(registerWithNonSetMBit);

        assertFalse(lastMapRegister().isWantMapNotify());
    }

    @Test
    public void mapRegister__NonSetMBitWithNonZeroReservedBits() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 02 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 c0 c7 c5 2f 57 f6 "
                + "0040   e7 20 25 3d e8 b2 07 e2 63 de 62 2b 7a 20 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handleMapRegisterPacket(registerWithNonSetMBit);
        assertFalse(lastMapRegister().isWantMapNotify());
    }

    @Test
    public void mapRegister__SetMBitWithNonZeroReservedBits() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 03 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 a2 72 40 7b 1a ae "
                + "0040   4e 6b e2 e5 e1 01 40 8a c9 e1 d1 80 cb 72 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handleMapRegisterPacket(registerWithNonSetMBit);
        assertTrue(lastMapRegister().isWantMapNotify());
    }

    @Test
    @Ignore
    public void mapRegisterAndNotify__ValidExtraDataParsedSuccessfully() throws Exception {
        byte[] extraDataPacket = new byte[mapRegisterPacket.length + 3];
        extraDataPacket[mapRegisterPacket.length] = 0x9;
        System.arraycopy(mapRegisterPacket, 0, extraDataPacket, 0, mapRegisterPacket.length);
        stubMapRegister(true);

        DatagramPacket dp = new DatagramPacket(extraDataPacket, extraDataPacket.length);
        dp.setLength(mapRegisterPacket.length);
        testedLispService.handlePacket(dp);
        // Check map register fields.
        // XXX: test
        // byte[] notifyResult = testedLispService.handlePacket(dp).getData();
        byte[] notifyResult = lastMapNotifyPacket().getData();
        assertEquals(mapRegisterPacket.length, notifyResult.length);

    }

    private DatagramPacket lastMapReplyPacket() {
        ByteBuffer serialize = MapReplySerializer.getInstance().serialize(mapReplyBuilder.build());
        return new DatagramPacket(serialize.array(), serialize.array().length);
    }

    private DatagramPacket lastMapNotifyPacket() {
        if (mapNotifyBuilder.getEidToLocatorRecord() == null) {
            mapNotifyBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        }
        mapNotifyBuilder.getEidToLocatorRecord().add(eidToLocatorBuilder.build());
        mapNotifyBuilder.setNonce((long) 0);
        mapNotifyBuilder.setKeyId((short) 0);
        mapNotifyBuilder.setAuthenticationData(new byte[0]);
        ByteBuffer serialize = MapNotifySerializer.getInstance().serialize(mapNotifyBuilder.build());
        return new DatagramPacket(serialize.array(), serialize.array().length);
    }

    @Test
    @Ignore
    public void mapNotify__VerifyBasicFields() throws Exception {
        byte registerType = mapRegisterPacket[0];
        assertEquals(LispMessageEnum.MapRegister.getValue(), registerType >> 4);

        stubMapRegister(true);

        byte[] result = handleMapRegisterAsByteArray(mapRegisterPacket);

        assertEquals(mapRegisterPacket.length, result.length);

        byte expectedType = (byte) (LispMessageEnum.MapNotify.getValue() << 4);
        assertHexEquals(expectedType, result[0]);
        assertHexEquals((byte) 0x00, result[1]);
        assertHexEquals((byte) 0x00, result[2]);

        byte[] registerWithoutTypeWithoutAuthenticationData = ArrayUtils.addAll(Arrays.copyOfRange(mapRegisterPacket, 3, 16),
                Arrays.copyOfRange(mapRegisterPacket, 36, mapRegisterPacket.length));
        byte[] notifyWithoutTypeWithOutAuthenticationData = ArrayUtils.addAll(Arrays.copyOfRange(result, 3, 16),
                Arrays.copyOfRange(result, 36, result.length));
        ArrayAssert.assertEquals(registerWithoutTypeWithoutAuthenticationData, notifyWithoutTypeWithOutAuthenticationData);
    }

    @Ignore
    @Test
    public void mapNotify__VerifyPort() throws Exception {
        stubMapRegister(true);

        DatagramPacket notifyPacket = handleMapRegisterPacket(mapRegisterPacket);
        assertEquals(LispMessage.PORT_NUM, notifyPacket.getPort());
    }

    @Test
    public void mapRequest__VerifyBasicFields() throws Exception {
        oneOf(nps).publish(with(lispNotificationSaver));
        handleMapRequestAsByteArray(mapRequestPacket);
        List<EidRecord> eids = lastMapRequest().getEidRecord();
        assertEquals(1, eids.size());
        LispAddressContainer lispAddress = eids.get(0).getLispAddressContainer();
        assertTrue(LispAFIConvertor.toAFI(lispAddress) instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address);
        assertEquals(LispAFIConvertor.asIPv4Address("1.2.3.4"), lispAddress);
        assertEquals((byte) 0x20, eids.get(0).getMask().byteValue());
        assertEquals(0x3d8d2acd39c8d608L, lastMapRequest().getNonce().longValue());
        // assertEquals(AddressFamilyNumberEnum.RESERVED,
        // AddressFamilyNumberEnum.valueOf(LispAFIToContainerConvertorFactory.toAFI(
        // lastMapRequest().getSourceEid().getLispAddressContainer()).getAfi()));
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

        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 0c 29 34 3e 1b 00 0c 29 f6 d6 0d 08 00 45 00 " //
                + "0010   00 b0 00 00 40 00 40 11 ea 7b 0a 00 3a 9c 0a 00 "
                + "0020   01 26 10 f6 10 f6 00 9c 9b 19 80 00 00 00 60 00 "
                + "0030   00 00 00 68 11 ff 26 10 00 d0 ff ff 01 92 00 00 "
                + "0040   00 00 00 00 00 01 26 10 00 d0 ff ff 01 92 00 00 "
                + "0050   00 00 00 00 00 02 10 f6 10 f6 00 68 94 8b 10 00 "
                + "0060   00 01 ff f5 bf 5d 7b 75 93 e6 00 02 26 10 00 d0 "
                + "0070   ff ff 01 92 00 00 00 00 00 00 00 01 00 01 0a 00 "
                + "0080   3a 9c 00 80 00 02 26 10 00 d0 ff ff 01 92 00 00 "
                + "0090   00 00 00 00 00 02 00 00 00 0a 01 80 10 00 00 00 " //
                + "00a0   00 02 26 10 00 d0 ff ff 01 92 00 00 00 00 00 00 " //
                + "00b0   00 01 01 64 ff 00 00 05 00 01 0a 00 3a 9c"));

        oneOf(nps).publish(with(lispNotificationSaver));
        // ret(mapReply);

        handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(LispAFIConvertor.asIPv6AfiAddress("2610:d0:ffff:192:0:0:0:1"), LispAFIConvertor.toAFI(lastMapRequest().getSourceEid().getLispAddressContainer()));
        assertEquals(LispAFIConvertor.asIPv6Address("2610:d0:ffff:192:0:0:0:2"), lastMapRequest().getEidRecord().get(0).getLispAddressContainer());
    }

    @Ignore
    @Test
    public void mapRequest__UsesIpv6EncapsulatedUdpPort() throws Exception {
        // Internet Protocol Version 6, Src: 2610:d0:ffff:192::1
        // (2610:d0:ffff:192::1), Dst: 2610:d0:ffff:192::2
        // (2610:d0:ffff:192::2)
        // encapsulated UDP source port: 4342

        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 0c 29 34 3e 1b 00 0c 29 f6 d6 0d 08 00 45 00 " //
                + "0010   00 b0 00 00 40 00 40 11 ea 7b 0a 00 3a 9c 0a 00 "
                + "0020   01 26 10 f6 10 f6 00 9c 9b 19 80 00 00 00 60 00 "
                + "0030   00 00 00 68 11 ff 26 10 00 d0 ff ff 01 92 00 00 "
                + "0040   00 00 00 00 00 01 26 10 00 d0 ff ff 01 92 00 00 "
                + "0050   00 00 00 00 00 02 10 f6 10 f6 00 68 94 8b 14 00 "
                + "0060   00 01 ff f5 bf 5d 7b 75 93 e6 00 02 26 10 00 d0 "
                + "0070   ff ff 01 92 00 00 00 00 00 00 00 01 00 01 0a 00 "
                + "0080   3a 9c 00 80 00 02 26 10 00 d0 ff ff 01 92 00 00 "
                + "0090   00 00 00 00 00 02 00 00 00 0a 01 80 10 00 00 00 " //
                + "00a0   00 02 26 10 00 d0 ff ff 01 92 00 00 00 00 00 00 " //
                + "00b0   00 01 01 64 ff 00 00 05 00 01 0a 00 3a 9c"));
        oneOf(nps).publish(with(lispNotificationSaver));
        // ret(mapReply);

        DatagramPacket replyPacket = handleMapRequestPacket(mapRequestPacket);
        assertEquals(4342, replyPacket.getPort());
    }

    @Test
    public void mapRequest__WithSourceEid() throws Exception {
        // encapsulated LISP packet
        // Source EID = 153.16.254.1

        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 0c 29 7a ce 83 00 15 17 c6 4a c9 08 00 45 00 " //
                + "0010   00 78 00 00 40 00 3e 11 ec b1 0a 00 01 26 0a 00 "
                + "0020   3a 9e 10 f6 10 f6 00 64 c3 a5 80 00 00 00 45 00 "
                + "0030   00 58 d4 31 00 00 ff 11 31 89 99 10 fe 01 0a 00 "
                + "0040   14 c8 10 f6 10 f6 00 44 84 ee 10 00 00 01 ba f9 "
                + "0050   ff 53 27 36 38 3a 00 01 99 10 fe 01 00 01 0a 00 "
                + "0060   01 26 00 20 00 01 0a 00 14 c8 00 00 00 0a 01 20 "
                + "0070   10 00 00 00 00 01 99 10 fe 01 01 64 ff 00 00 05 " //
                + "0080   00 01 0a 00 01 26"));

        oneOf(nps).publish(with(lispNotificationSaver));
        // ret(mapReply);

        handleMapRequestAsByteArray(mapRequestPacket);
        Assert.assertNotEquals(AddressFamilyNumberEnum.IP, LispAFIConvertor.toAFI(lastMapRequest().getSourceEid().getLispAddressContainer()));

    }

    @Test
    @Ignore
    public void mapReply__VerifyBasicIPv4Fields() throws Exception {
        eidToLocatorBuilder.setMaskLength((short) 0x20).setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.0.20.200")));
        mapReplyBuilder.setNonce(0x3d8d2acd39c8d608L);

        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);

        assertEquals(28, result.length);

        byte expectedLispMessageType = 2;
        assertEquals(expectedLispMessageType, (byte) (result[LispMessage.Pos.TYPE] >> 4));
        assertEquals(0x3d8d2acd39c8d608L, ByteUtil.getLong(result, MapReplyIpv4SingleLocatorPos.NONCE));

        byte expectedRecordCount = (byte) 1;
        assertEquals(expectedRecordCount, result[MapReplyIpv4SingleLocatorPos.RECORD_COUNT]);

        assertEquals(eidToLocatorBuilder.getMaskLength().byteValue(), result[MapReplyIpv4SingleLocatorPos.EID_MASK_LEN]);
        assertEquals(AddressFamilyNumberEnum.IP.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.AFI_TYPE));
        assertEquals(0x0a0014c8, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.EID_PREFIX));
    }

    @Test
    @Ignore
    public void mapReply__VerifyBasicIPv6() throws Exception {
        eidToLocatorBuilder.setMaskLength((short) 0x80).setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1")));

        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);

        assertEquals(40, result.length);

        byte expectedRecordCount = (byte) 1;
        assertEquals(expectedRecordCount, result[MapReplyIpv4SingleLocatorPos.RECORD_COUNT]);

        assertEquals(eidToLocatorBuilder.getMaskLength().byteValue(), result[MapReplyIpv4SingleLocatorPos.EID_MASK_LEN]);
        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.AFI_TYPE));
        byte[] expectedIpv6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };

        ArrayAssert.assertEquals(expectedIpv6, Arrays.copyOfRange(result, 24, 40));
    }

    @Test
    @Ignore
    public void mapReply__VerifyIPv6EidAndLocator() throws Exception {
        eidToLocatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1")));
        eidToLocatorBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:2"))).build());

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

        assertEquals(LispMessage.PORT_NUM, handleMapRequestPacket(mapRequestPacket).getPort());
    }

    @Test
    @Ignore
    public void mapReply__WithNonRoutableSingleLocator() throws Exception {
        eidToLocatorBuilder.setMaskLength((short) 0x20).setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.0.20.200")));
        eidToLocatorBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(false).setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1"))).build());
        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(0x00, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    @Ignore
    public void mapReply__WithSingleLocator() throws Exception {
        eidToLocatorBuilder.setMaskLength((short) 0x20)//
                .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.0.20.200")));
        eidToLocatorBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(true).setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1"))).build());
        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(40, result.length);

        byte expectedLocCount = 1;
        assertEquals(expectedLocCount, result[MapReplyIpv4SingleLocatorPos.LOCATOR_COUNT]);

        assertEquals(AddressFamilyNumberEnum.IP.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.LOC_AFI));

        assertEquals(0x04030201, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOCATOR));
        assertEquals(0x01, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    @Ignore
    public void mapReply__WithMultipleLocator() throws Exception {
        eidToLocatorBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(true).setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1"))).build());
        eidToLocatorBuilder.getLocatorRecord().add(
                new LocatorRecordBuilder().setRouted(true).setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1"))).build());
        stubHandleRequest();

        byte[] result = handleMapRequestAsByteArray(mapRequestPacket);
        assertEquals(64, result.length);

        assertEquals(2, result[MapReplyIpv4SingleLocatorPos.LOCATOR_COUNT]);

        assertEquals(AddressFamilyNumberEnum.IP.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.LOC_AFI));
        assertEquals(0x04030201, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOCATOR));
        assertEquals(0x01, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);

        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SecondLocatorPos.LOC_AFI));

        byte[] expectedIpv6Rloc = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        ArrayAssert.assertEquals(expectedIpv6Rloc,
                Arrays.copyOfRange(result, MapReplyIpv4SecondLocatorPos.LOCATOR, MapReplyIpv4SecondLocatorPos.LOCATOR + 16));

        assertEquals(0x01, result[MapReplyIpv4SecondLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    public void handleUnknownLispMessage() throws Exception {
        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 14 UNKNOWN!!!, P=1, M=1

        byte[] unknownTypePacket = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 F8 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 "
                + "0040   b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
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
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 0c 29 7a ce 8d 00 0c 29 e4 ef 70 08 00 45 00 "
                + "0010   00 8a 00 00 40 00 40 11 25 f2 0a 01 00 6f 0a 01 " //
                + "0020   00 01 10 f6 10 f6 00 76 06 1f 80 00 00 00 45 00 " //
                + "0030   00 6a d4 31 00 00 ff 11 2a 3e ac 01 01 02 08 08 " //
                + "0040   08 08 10 f6 10 f6 00 56 63 14 10 00 01 01 79 67 " //
                + "0050   ff 75 a0 61 66 19 00 01 ac 01 01 02 00 01 0a 01 " //
                + "0060   00 6f 00 01 c0 a8 88 33 00 20 00 01 08 08 08 08 " //
                + "0070   00 00 00 0a 02 20 10 00 00 00 00 01 ac 01 01 02 " //
                + "0080   01 64 ff 00 00 05 00 01 0a 01 00 6f 06 64 ff 00 " //
                + "0090   00 05 00 01 c0 a8 88 33"));

        oneOf(nps).publish(with(lispNotificationSaver));
        handleMapRequestAsByteArray(mapRequestPacket);

    }

    private void stubMapRegister(final boolean setNotifyFromRegister) {
        allowing(nps).publish(with(lispNotificationSaver));
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
        allowing(nps).publish(wany(Notification.class));
    }

    private byte[] handleMapRequestAsByteArray(byte[] inPacket) {
        handleMapRequestPacket(inPacket);
        return lastMapReplyPacket().getData();
    }

    private byte[] handleMapRegisterAsByteArray(byte[] inPacket) {
        handleMapRegisterPacket(inPacket);
        return lastMapNotifyPacket().getData();
    }

    private DatagramPacket handleMapRequestPacket(byte[] inPacket) {
        DatagramPacket dp = new DatagramPacket(inPacket, inPacket.length);
        // Unless we explicitly set the source port, it will be -1, which breaks some tests
        // This is till not the real port number, but it's better
        dp.setPort(LispMessage.PORT_NUM);
        testedLispService.handlePacket(dp);
        return lastMapReplyPacket();
    }

    private DatagramPacket handleMapRegisterPacket(byte[] inPacket) {
        DatagramPacket dp = new DatagramPacket(inPacket, inPacket.length);
        // Unless we explicitly set the source port, it will be -1, which breaks some tests
        // This is till not the real port number, but it's better
        dp.setPort(LispMessage.PORT_NUM);
        testedLispService.handlePacket(dp);
        if (mapNotifyBuilder == null) {
            return null;
        } else {
            return lastMapNotifyPacket();
        }
    }

    private DatagramPacket handlePacket(byte[] inPacket) {
        // TODO get from mock
        testedLispService.handlePacket(new DatagramPacket(inPacket, inPacket.length));
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

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__NoIPITRRLOC() throws Exception {
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
    // oneOf(nps).publish(with(lispNotificationSaver));
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
    // oneOf(nps).publish(with(lispNotificationSaver));
    // // ret(mapReply);
    // DatagramPacket packet = handleMapRequestPacket(mapRequestPacket);
    // assertEquals(2, lastMapRequest().getItrRloc().size());
    // assertEquals((new LispIpv4Address("1.2.3.4")).getAddress(),
    // packet.getAddress());
    // }

}
