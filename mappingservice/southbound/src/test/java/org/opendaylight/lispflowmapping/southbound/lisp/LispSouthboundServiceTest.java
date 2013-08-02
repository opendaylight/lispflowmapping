/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.List;

import junitx.framework.ArrayAssert;
import junitx.framework.Assert;

import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServer;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.serializer.LispMessage;
import org.opendaylight.lispflowmapping.southbound.serializer.LispMessageEnum;
import org.opendaylight.lispflowmapping.southbound.util.ByteUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;

public class LispSouthboundServiceTest extends BaseTestCase {

    private LispSouthboundService testedLispService;
    private IMapResolver mapResolver;
    private IMapServer mapServer;
    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacket;
    private ValueSaverAction<MapRegister> mapRegisterSaver;
    private ValueSaverAction<MapRequest> mapRequestSaver;
    private MapNotify mapNotify;
    private MapReply mapReply;
    private EidToLocatorRecord eidToLocator;

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
        mapResolver = context.mock(IMapResolver.class);
        mapServer = context.mock(IMapServer.class);
        testedLispService = new LispSouthboundService(mapResolver, mapServer);

        mapRegisterSaver = new ValueSaverAction<MapRegister>();
        mapRequestSaver = new ValueSaverAction<MapRequest>();
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
                + "0050   2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
                + "0060   00 01 01 02 03 04"));
        mapReply = new MapReply();
        eidToLocator = new EidToLocatorRecord();
        eidToLocator.setPrefix(new LispIpv4Address(0));
        mapReply.addEidToLocator(eidToLocator);

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
                + "0030   00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 "
                + "0040   b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        mapNotify = new MapNotify();
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

        handlePacket(mapRegisterPacket);
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__IllegalPacket() throws Exception {
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " //
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 38 d4 31 00 00 ff 11 56 f3 c0 a8 88 0a 01 02 " //
                + "0040   03 04 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "));
        handlePacket(mapRequestPacket);
    }

    @Test(expected = LispMalformedPacketException.class)
    public void mapRequest__IllegalEncapsulatedPacket() throws Exception {
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " //
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 " //
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "));
        handlePacket(mapRequestPacket);
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
                + "0030   00 00 00 00 00 00 00 01 00 14 f8 3c d8 9d 92 8b " //
                + "0040   67 20 2c 63 82 63 c5 38 7b 74 b8 70 01 dd 00 00 " //
                + "0050   00 0a 02 20 10 00 00 00 00 01 ac 01 01 02 01 64 " //
                + "0060   ff 00 00 05 00 01 0a 01 00 6e 06 64 ff 00 00 05 " //
                + "0070   00 01 c0 a8 88 33"));

        oneOf(mapServer).handleMapRegister(with(mapRegisterSaver));
        ret(null);

        handlePacket(mapRegisterPacket);

        List<EidToLocatorRecord> eidRecords = mapRegisterSaver.lastValue.getEidToLocatorRecords();
        assertEquals(1, eidRecords.size());
        EidToLocatorRecord eidRecord = eidRecords.get(0);
        assertEquals(2, eidRecord.getLocators().size());
        assertEquals(new LispIpv4Address("10.1.0.110"), eidRecord.getLocators().get(0).getLocator());
        assertEquals(new LispIpv4Address("192.168.136.51"), eidRecord.getLocators().get(1).getLocator());
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
                + "0030   00 00 00 00 00 00 00 01 00 14 5b c4 d4 4a 57 e2 " //
                + "0040   a5 5d 57 7a 6f 89 77 9c 00 4f 5d a7 13 fb 00 00 " //
                + "0050   00 0a 01 80 10 00 00 00 00 02 26 10 00 d0 ff ff " //
                + "0060   01 92 00 00 00 00 00 00 00 01 01 64 ff 00 00 05 " //
                + "0070   00 01 0a 00 3a 9c"));

        oneOf(mapServer).handleMapRegister(with(mapRegisterSaver));
        ret(null);

        handlePacket(mapRegisterPacket);
        byte[] expectedIpv6Address = new byte[] { 0x26, 0x10, 0x00, (byte) 0xd0, (byte) 0xff, (byte) 0xff, 0x01, (byte) 0x92, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01 };

        EidToLocatorRecord eidToLocatorRecord = mapRegisterSaver.lastValue.getEidToLocatorRecords().get(0);
        assertEquals(new LispIpv6Address(expectedIpv6Address), eidToLocatorRecord.getPrefix());
        assertEquals(AddressFamilyNumberEnum.IP6, eidToLocatorRecord.getPrefix().getAfi());

        assertEquals(new LispIpv4Address(0x0A003A9C), eidToLocatorRecord.getLocators().get(0).getLocator());
    }

    @Test
    public void mapRegister__VerifyBasicFields() throws Exception {
        oneOf(mapServer).handleMapRegister(with(mapRegisterSaver));
        ret(null);
        handlePacket(mapRegisterPacket);

        EidToLocatorRecord eidToLocator = mapRegisterSaver.lastValue.getEidToLocatorRecords().get(0);
        assertEquals(new LispIpv4Address(0x9910FE01), eidToLocator.getPrefix());

        assertEquals(1, eidToLocator.getLocators().size());
        assertEquals(new LispIpv4Address(0xC0A8880A), eidToLocator.getLocators().get(0).getLocator());
    }

    @Test
    public void mapRegister__NoResponseFromMapServerShouldReturnNullPacket() throws Exception {
        allowing(mapServer).handleMapRegister(with(mapRegisterSaver));
        ret(null);

        assertNull(handlePacket(mapRegisterPacket));
    }

    @Test
    public void mapRegister__NonSetMBit() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df " //
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 00 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 "
                + "0040   b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 " //
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handlePacket(registerWithNonSetMBit);

        assertFalse(mapRegisterSaver.lastValue.isWantMapNotify());
    }

    @Test
    public void mapRegister__NonSetMBitWithNonZeroReservedBits() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 02 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 "
                + "0040   b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handlePacket(registerWithNonSetMBit);
        assertFalse(mapRegisterSaver.lastValue.isWantMapNotify());
    }

    @Test
    public void mapRegister__SetMBitWithNonZeroReservedBits() throws Exception {
        byte[] registerWithNonSetMBit = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 03 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 "
                + "0040   b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
        stubMapRegister(true);

        handlePacket(registerWithNonSetMBit);
        assertTrue(mapRegisterSaver.lastValue.isWantMapNotify());
    }

    @Test
    public void mapRegisterAndNotify__ValidExtraDataParsedSuccessfully() throws Exception {
        byte[] extraDataPacket = new byte[mapRegisterPacket.length + 3];
        extraDataPacket[mapRegisterPacket.length] = 0x9;
        System.arraycopy(mapRegisterPacket, 0, extraDataPacket, 0, mapRegisterPacket.length);
        stubMapRegister(true);

        DatagramPacket dp = new DatagramPacket(extraDataPacket, extraDataPacket.length);
        dp.setLength(mapRegisterPacket.length);
        byte[] notifyResult = testedLispService.handlePacket(dp).getData();

        assertEquals(mapRegisterPacket.length, notifyResult.length);

    }

    @Test
    public void mapNotify__VerifyBasicFields() throws Exception {
        byte registerType = mapRegisterPacket[0];
        assertEquals(LispMessageEnum.MapRegister.getValue(), registerType >> 4);

        stubMapRegister(true);

        byte[] result = handlePacketAsByteArray(mapRegisterPacket);

        assertEquals(mapRegisterPacket.length, result.length);

        byte expectedType = (byte) (LispMessageEnum.MapNotify.getValue() << 4);
        assertHexEquals(expectedType, result[0]);
        assertHexEquals((byte) 0x00, result[1]);
        assertHexEquals((byte) 0x00, result[2]);

        byte[] registerWithoutType = Arrays.copyOfRange(mapRegisterPacket, 3, mapRegisterPacket.length);
        byte[] notifyWithoutType = Arrays.copyOfRange(result, 3, result.length);
        ArrayAssert.assertEquals(registerWithoutType, notifyWithoutType);
    }

    @Test
    public void mapNotify__VerifyPort() throws Exception {
        stubMapRegister(true);

        DatagramPacket notifyPacket = handlePacket(mapRegisterPacket);
        assertEquals(LispMessage.PORT_NUM, notifyPacket.getPort());
    }

    @Test
    public void mapRequest__VerifyBasicFields() throws Exception {
        oneOf(mapResolver).handleMapRequest(with(mapRequestSaver));
        ret(mapReply);

        handlePacketAsByteArray(mapRequestPacket);
        List<EidRecord> eids = mapRequestSaver.lastValue.getEids();
        assertEquals(1, eids.size());
        LispAddress lispAddress = eids.get(0).getPrefix();
        assertTrue(lispAddress instanceof LispIpv4Address);
        assertEquals(asInet(0x01020304), ((LispIpv4Address) lispAddress).getAddress());
        assertEquals((byte) 0x20, eids.get(0).getMaskLength());
        assertEquals(0x3d8d2acd39c8d608L, mapRequestSaver.lastValue.getNonce());
        assertEquals(AddressFamilyNumberEnum.RESERVED, mapRequestSaver.lastValue.getSourceEid().getAfi());
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
                + "0050   00 00 00 00 00 02 10 f6 10 f6 00 68 94 8b 14 00 "
                + "0060   00 01 ff f5 bf 5d 7b 75 93 e6 00 02 26 10 00 d0 "
                + "0070   ff ff 01 92 00 00 00 00 00 00 00 01 00 01 0a 00 "
                + "0080   3a 9c 00 80 00 02 26 10 00 d0 ff ff 01 92 00 00 "
                + "0090   00 00 00 00 00 02 00 00 00 0a 01 80 10 00 00 00 " //
                + "00a0   00 02 26 10 00 d0 ff ff 01 92 00 00 00 00 00 00 " //
                + "00b0   00 01 01 64 ff 00 00 05 00 01 0a 00 3a 9c"));

        allowing(mapResolver).handleMapRequest(with(mapRequestSaver));
        ret(mapReply);

        handlePacketAsByteArray(mapRequestPacket);
        assertEquals(new LispIpv6Address("2610:d0:ffff:192::1"), mapRequestSaver.lastValue.getSourceEid());
        assertEquals(new LispIpv6Address("2610:d0:ffff:192::2"), mapRequestSaver.lastValue.getEids().get(0).getPrefix());
    }

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

        allowing(mapResolver).handleMapRequest(with(mapRequestSaver));
        ret(mapReply);

        DatagramPacket replyPacket = handlePacket(mapRequestPacket);
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
                + "0040   14 c8 10 f6 10 f6 00 44 84 ee 14 00 00 01 ba f9 "
                + "0050   ff 53 27 36 38 3a 00 01 99 10 fe 01 00 01 0a 00 "
                + "0060   01 26 00 20 00 01 0a 00 14 c8 00 00 00 0a 01 20 "
                + "0070   10 00 00 00 00 01 99 10 fe 01 01 64 ff 00 00 05 " //
                + "0080   00 01 0a 00 01 26"));

        allowing(mapResolver).handleMapRequest(with(mapRequestSaver));
        ret(mapReply);

        handlePacketAsByteArray(mapRequestPacket);
        Assert.assertNotEquals(AddressFamilyNumberEnum.RESERVED, mapRequestSaver.lastValue.getSourceEid().getAfi());

    }

    @Test
    public void mapReply__VerifyBasicIPv4Fields() throws Exception {
        eidToLocator.setMaskLength((byte) 0x20)//
                .setPrefix(new LispIpv4Address(0x0a0014c8));
        mapReply.setNonce(0x3d8d2acd39c8d608L);

        stubHandleRequest();

        byte[] result = handlePacketAsByteArray(mapRequestPacket);

        assertEquals(28, result.length);

        byte expectedLispMessageType = 2;
        assertEquals(expectedLispMessageType, (byte) (result[LispMessage.Pos.TYPE] >> 4));
        assertEquals(0x3d8d2acd39c8d608L, ByteUtil.getLong(result, MapReplyIpv4SingleLocatorPos.NONCE));

        byte expectedRecordCount = (byte) 1;
        assertEquals(expectedRecordCount, result[MapReplyIpv4SingleLocatorPos.RECORD_COUNT]);

        assertEquals(eidToLocator.getMaskLength(), result[MapReplyIpv4SingleLocatorPos.EID_MASK_LEN]);
        assertEquals(AddressFamilyNumberEnum.IP.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.AFI_TYPE));
        assertEquals(0x0a0014c8, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.EID_PREFIX));
    }

    @Test
    public void mapReply__VerifyBasicIPv6() throws Exception {
        eidToLocator.setMaskLength((byte) 0x80)//
                .setPrefix(new LispIpv6Address("::1"));

        stubHandleRequest();

        byte[] result = handlePacketAsByteArray(mapRequestPacket);

        assertEquals(40, result.length);

        byte expectedRecordCount = (byte) 1;
        assertEquals(expectedRecordCount, result[MapReplyIpv4SingleLocatorPos.RECORD_COUNT]);

        assertEquals(eidToLocator.getMaskLength(), result[MapReplyIpv4SingleLocatorPos.EID_MASK_LEN]);
        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.AFI_TYPE));
        byte[] expectedIpv6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };

        ArrayAssert.assertEquals(expectedIpv6, Arrays.copyOfRange(result, 24, 40));
    }

    @Test
    public void mapReply__VerifyIPv6EidAndLocator() throws Exception {
        eidToLocator.setPrefix(new LispIpv6Address("::1"));
        eidToLocator.addLocator(new LocatorRecord().setLocator(new LispIpv6Address("::2")));

        stubHandleRequest();

        byte[] result = handlePacketAsByteArray(mapRequestPacket);

        assertEquals(64, result.length);

        byte[] expectedIpv6Eid = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        ArrayAssert.assertEquals(expectedIpv6Eid, Arrays.copyOfRange(result, 24, 40));

        byte[] expectedIpv6Rloc = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2 };
        ArrayAssert.assertEquals(expectedIpv6Rloc, Arrays.copyOfRange(result, 48, 64));
    }

    @Test
    public void mapReply__UseEncapsulatedUdpPort() throws Exception {
        stubHandleRequest();

        assertEquals(56756, handlePacket(mapRequestPacket).getPort());
    }

    @Test
    public void mapReply__WithNonRoutableSingleLocator() throws Exception {
        eidToLocator.setMaskLength((byte) 0x20)//
                .setPrefix(new LispIpv4Address(0x0a0014c8));
        eidToLocator.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)).setRouted(false));
        stubHandleRequest();

        byte[] result = handlePacketAsByteArray(mapRequestPacket);
        assertEquals(0x00, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    public void mapReply__WithSingleLocator() throws Exception {
        eidToLocator.setMaskLength((byte) 0x20)//
                .setPrefix(new LispIpv4Address(0x0a0014c8));
        eidToLocator.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)).setRouted(true));
        stubHandleRequest();

        byte[] result = handlePacketAsByteArray(mapRequestPacket);
        assertEquals(40, result.length);

        byte expectedLocCount = 1;
        assertEquals(expectedLocCount, result[MapReplyIpv4SingleLocatorPos.LOCATOR_COUNT]);

        assertEquals(AddressFamilyNumberEnum.IP.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.LOC_AFI));

        assertEquals(0x04030201, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOCATOR));
        assertEquals(0x01, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);
    }

    @Test
    public void mapReply__WithMultipleLocator() throws Exception {
        eidToLocator.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)).setRouted(true));
        eidToLocator.addLocator(new LocatorRecord().setLocator(new LispIpv6Address("::1")).setRouted(true));
        stubHandleRequest();

        byte[] result = handlePacketAsByteArray(mapRequestPacket);
        assertEquals(64, result.length);

        assertEquals(2, result[MapReplyIpv4SingleLocatorPos.LOCATOR_COUNT]);

        assertEquals(AddressFamilyNumberEnum.IP.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SingleLocatorPos.LOC_AFI));
        assertEquals(0x04030201, ByteUtil.getInt(result, MapReplyIpv4SingleLocatorPos.LOCATOR));
        assertEquals(0x01, result[MapReplyIpv4SingleLocatorPos.LOCATOR_RBIT] & 0x01);

        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ByteUtil.getShort(result, MapReplyIpv4SecondLocatorPos.LOC_AFI));

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
                + "0040   08 08 10 f6 10 f6 00 56 63 14 14 00 01 01 79 67 " //
                + "0050   ff 75 a0 61 66 19 00 01 ac 01 01 02 00 01 0a 01 " //
                + "0060   00 6f 00 01 c0 a8 88 33 00 20 00 01 08 08 08 08 " //
                + "0070   00 00 00 0a 02 20 10 00 00 00 00 01 ac 01 01 02 " //
                + "0080   01 64 ff 00 00 05 00 01 0a 01 00 6f 06 64 ff 00 " //
                + "0090   00 05 00 01 c0 a8 88 33"));

        allowing(mapResolver).handleMapRequest(with(mapRequestSaver));
        ret(mapReply);

        handlePacketAsByteArray(mapRequestPacket);

        // assertEquals(2, mapRequestSaver.lastValue.getItrRlocs().size());
        // assertEquals(new LispIpv4Address("10.1.0.111"),
        // mapRequestSaver.lastValue.getItrRlocs().get(0));
        // assertEquals(new LispIpv4Address("192.168.136.51"),
        // mapRequestSaver.lastValue.getItrRlocs().get(1));
    }

    private void stubMapRegister(final boolean setNotifyFromRegister) {
        allowing(mapServer).handleMapRegister(with(mapRegisterSaver));
        will(new SimpleAction() {

            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                if (setNotifyFromRegister) {
                    mapNotify.setFromMapRegister(mapRegisterSaver.lastValue);
                }
                return mapNotify;
            }
        });
    }

    private void stubHandleRequest() {
        allowing(mapResolver).handleMapRequest(wany(MapRequest.class));
        ret(mapReply);
    }

    private byte[] handlePacketAsByteArray(byte[] inPacket) {
        return handlePacket(inPacket).getData();
    }

    private DatagramPacket handlePacket(byte[] inPacket) {
        return testedLispService.handlePacket(new DatagramPacket(inPacket, inPacket.length));
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
}
