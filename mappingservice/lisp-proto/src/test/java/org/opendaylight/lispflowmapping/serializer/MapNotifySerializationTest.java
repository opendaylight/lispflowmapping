/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispaddress.lispaddresscontainer.address.no.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;

public class MapNotifySerializationTest extends BaseTestCase {

    @Test
    public void serialize__Fields() throws Exception {
        MapNotifyBuilder mnBuilder = new MapNotifyBuilder();
        mnBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mnBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1")))
                        .build());
        mnBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.73")))
                        .build());

        mnBuilder.setNonce(6161616161L);
        mnBuilder.setKeyId((short) 0x0001);
        byte[] authenticationData = new byte[] { (byte) 0x16, (byte) 0x98, (byte) 0x96, (byte) 0xeb, (byte) 0x88, (byte) 0x2d, (byte) 0x4d,
                (byte) 0x22, (byte) 0xe5, (byte) 0x8f, (byte) 0xe6, (byte) 0x89, (byte) 0x64, (byte) 0xb9, (byte) 0x17, (byte) 0xa4, (byte) 0xba,
                (byte) 0x4e, (byte) 0x8c, (byte) 0x41 };
        mnBuilder.setAuthenticationData(authenticationData);

        ByteBuffer bb = MapNotifySerializer.getInstance().serialize(mnBuilder.build());
        assertHexEquals((byte) 0x40, bb.get()); // Type + MSByte of reserved
        assertEquals(0, bb.getShort()); // Rest of reserved
        assertEquals(2, bb.get()); // Record Count
        assertEquals(6161616161L, bb.getLong()); // Nonce
        assertHexEquals((short) 0x0001, bb.getShort()); // Key ID
        assertEquals(authenticationData.length, bb.getShort());

        byte[] actualAuthenticationData = new byte[20];
        bb.get(actualAuthenticationData);
        ArrayAssert.assertEquals(authenticationData, actualAuthenticationData);

        bb.position(bb.position() + 12); /* EID in first record */
        assertEquals(0x1, bb.getInt());

        bb.position(bb.position() + 12); /* EID in second record */
        assertEquals(73, bb.getInt());

        assertEquals(bb.position(), bb.capacity());
    }

    @Test
    public void serialize__deserizlise() throws Exception {
        MapNotifyBuilder mnBuilder = new MapNotifyBuilder();
        mnBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mnBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1")))
                        .build());
        mnBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.73")))
                        .build());

        mnBuilder.setNonce(6161616161L);
        mnBuilder.setKeyId((short) 0x0001);
        byte[] authenticationData = new byte[] { (byte) 0x16, (byte) 0x98, (byte) 0x96, (byte) 0xeb, (byte) 0x88, (byte) 0x2d, (byte) 0x4d,
                (byte) 0x22, (byte) 0xe5, (byte) 0x8f, (byte) 0xe6, (byte) 0x89, (byte) 0x64, (byte) 0xb9, (byte) 0x17, (byte) 0xa4, (byte) 0xba,
                (byte) 0x4e, (byte) 0x8c, (byte) 0x41 };
        mnBuilder.setAuthenticationData(authenticationData);

        MapNotify mn = mnBuilder.build();
        ArrayAssert.assertEquals(
                MapNotifySerializer.getInstance().serialize(mn).array(),
                MapNotifySerializer.getInstance()
                        .serialize(MapNotifySerializer.getInstance().deserialize(MapNotifySerializer.getInstance().serialize(mn))).array());

    }

    @Test
    public void deserialize__AllFields() throws Exception {
        // LISP(Type = 4 Map-Notify, I=1
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0000
        // AuthDataLength: 00 Data:
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        // xTR-ID 1
        // Site-ID 1
        MapNotify mn = MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("48 00 00 01 FF BB "
        //
                + "00 00 00 00 00 00 00 00 00 00 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a "
                + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 " // xTR-ID
                + "00 00 00 00 00 00 00 01 " // Site-ID
                ));


        assertTrue(mn.isXtrSiteIdPresent());

        assertEquals(1, mn.getEidToLocatorRecord().size());
        assertEquals(0xFFBB000000000000L, mn.getNonce().longValue());
        assertEquals(0x0000, mn.getKeyId().shortValue());
        byte[] expectedAuthenticationData = {};
        ArrayAssert.assertEquals(expectedAuthenticationData, mn.getAuthenticationData());
    }

    @Test
    public void serialize__NoAuthenticationData() throws Exception {
        MapNotifyBuilder mnBuilder = new MapNotifyBuilder();
        mnBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mnBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setRecordTtl(55)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1"))).build());

        ByteBuffer bb = MapNotifySerializer.getInstance().serialize(mnBuilder.build());
        bb.position(bb.position() + 14); // jump to AuthenticationDataLength
        assertEquals(0, bb.getShort());
        assertEquals(55, bb.getInt());

        mnBuilder.setAuthenticationData(null);

        bb = MapNotifySerializer.getInstance().serialize(mnBuilder.build());
        bb.position(bb.position() + 14); // jump to AuthenticationDataLength
        assertEquals(0, bb.getShort());
        assertEquals(55, bb.getInt());

        mnBuilder.setAuthenticationData(new byte[0]);

        bb = MapNotifySerializer.getInstance().serialize(mnBuilder.build());
        bb.position(bb.position() + 14); // jump to AuthenticationDataLength
        assertEquals(0, bb.getShort());
        assertEquals(55, bb.getInt());
    }

    @Test
    public void serialize__NoPrefixInEidToLocator() throws Exception {
        MapNotifyBuilder mnBuilder = new MapNotifyBuilder();
        mnBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mnBuilder.getEidToLocatorRecord().add(new EidToLocatorRecordBuilder().build());
        mnBuilder.getEidToLocatorRecord().add(new EidToLocatorRecordBuilder().setLispAddressContainer(null).build());
        mnBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(
                        LispAFIConvertor.toContainer(new NoAddressBuilder().setAfi(AddressFamilyNumberEnum.NO_ADDRESS.getIanaCode()).build())).build());

        ByteBuffer bb = MapNotifySerializer.getInstance().serialize(mnBuilder.build());
        bb.position(bb.position() + 16); // jump to first record prefix AFI
        assertEquals(0, bb.getShort());

        bb.position(bb.position() + 10); // jump to second record prefix AFI
        assertEquals(0, bb.getShort());

        bb.position(bb.position() + 10); // jump to third record prefix AFI
        assertEquals(0, bb.getShort());
    }

    @Test
    public void deserialize__MultipleRecords() throws Exception {
        // LISP(Type = 4 Map-Notify, I=1
        // Record Counter: 4
        // EID prefixes: 153.16.254.1 -- 152.16.254.1 -- 151.16.254.1 --
        // 150.16.254.1
        // Local RLOCs: 192.168.136.10 -- 192.168.136.11 -- 192.168.136.12 --
        // 192.168.136.13
        //
        MapNotify mn = MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("40 00 00 "
        //
                + "04 " // Record count
                + "FF BB 00 00 00 00 00 00 00 01 00 14 87 c1 33 cd " //
                + "d1 1e bc 80 fd 3e 71 11 81 17 40 74 26 25 44 bd " //
                + "00 00 00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " // Record
                // 1
                + "ff 00 00 05 00 01 c0 a8 88 0a " // contd
                + "00 00 00 0a 01 20 10 00 00 00 00 01 98 10 fe 01 01 64 " // Record
                // 2
                + "ff 00 00 05 00 01 c0 a8 88 0b " // contd
                + "00 00 00 0a 01 20 10 00 00 00 00 01 97 10 fe 01 01 64 " // Record
                // 3
                + "ff 00 00 05 00 01 c0 a8 88 0c " // contd
                + "00 00 00 0a 01 20 10 00 00 00 00 01 96 10 fe 01 01 64 " // Record
                // 4
                + "ff 00 00 05 00 01 c0 a8 88 0d " // contd
        ));

        assertEquals(4, mn.getEidToLocatorRecord().size());
        assertEquals(LispAFIConvertor.asIPv4Address("153.16.254.1"), mn.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.asIPv4Address("151.16.254.1"), mn.getEidToLocatorRecord().get(2)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("192.168.136.11")), mn.getEidToLocatorRecord().get(1)
                .getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("192.168.136.13")), mn.getEidToLocatorRecord().get(3)
                .getLocatorRecord().get(0).getLispAddressContainer());
    }

    @Test
    public void deserialize__Locators() throws Exception {
        MapNotify mn = MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("40 00 01 01 "
        //
                + "FF BB 00 00 00 00 00 00 00 01 00 14 f1 b8 ab f0 " //
                + "66 bb 2e ef 12 70 74 46 6f 6b 8e ca bf 1e 68 40 " //
                + "00 00 00 0a " //
                + "03 " // Locator Count
                + "20 10 00 00 00 00 01 99 10 fe 01 "
                // Locator 1
                + "01 64 1f 00 " // priorities + weights
                + "00 05 " // Flags
                + "00 01 c0 a8 88 0a " // Locator
                // Locator 2
                + "67 00 30 34 " // priorities + weights
                + "00 02 " // Flags
                + "00 01 cc aa AA 11 " // Locator
                // Locator 3
                + "60 11 34 A4 " // priorities + weights
                + "00 03 " // Flags
                + "00 01 c0 a8 88 0a " // Locator
        ));

        assertEquals(1, mn.getEidToLocatorRecord().size());
        EidToLocatorRecord eidToLocator = mn.getEidToLocatorRecord().get(0);
        assertEquals(3, eidToLocator.getLocatorRecord().size());
        LocatorRecord loc0 = eidToLocator.getLocatorRecord().get(0);
        LocatorRecord loc1 = eidToLocator.getLocatorRecord().get(1);
        LocatorRecord loc2 = eidToLocator.getLocatorRecord().get(2);
        assertEquals((byte) 0x01, loc0.getPriority().byteValue());
        assertEquals((byte) 0x67, loc1.getPriority().byteValue());
        assertEquals((byte) 0x60, loc2.getPriority().byteValue());

        assertEquals((byte) 0x64, loc0.getWeight().byteValue());
        assertEquals((byte) 0x00, loc1.getWeight().byteValue());
        assertEquals((byte) 0x11, loc2.getWeight().byteValue());

        assertEquals((byte) 0x1F, loc0.getMulticastPriority().byteValue());
        assertEquals((byte) 0x30, loc1.getMulticastPriority().byteValue());
        assertEquals((byte) 0x34, loc2.getMulticastPriority().byteValue());

        assertEquals((byte) 0x00, loc0.getMulticastWeight().byteValue());
        assertEquals((byte) 0x34, loc1.getMulticastWeight().byteValue());
        assertEquals((byte) 0xA4, loc2.getMulticastWeight().byteValue());

        assertTrue(loc0.isLocalLocator());
        assertFalse(loc1.isLocalLocator());
        assertFalse(loc2.isLocalLocator());

        assertFalse(loc0.isRlocProbed());
        assertTrue(loc1.isRlocProbed());
        assertTrue(loc2.isRlocProbed());

        assertTrue(loc0.isRouted());
        assertFalse(loc1.isRouted());
        assertTrue(loc2.isRouted());

        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("192.168.136.10")), loc0.getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("204.170.170.17")), loc1.getLispAddressContainer());
    }

    @Test
    public void deserialize__SomeEidToLocatorFiels() throws Exception {
        // LISP(Type = 4 Map-Notify, I=0
        // Record Counter: 4
        // EID prefixes: 153.16.254.1 -- 152.16.254.1 -- 151.16.254.1 --
        // 150.16.254.1
        // Local RLOCs: 192.168.136.10 -- 192.168.136.11 -- 192.168.136.12 --
        // 192.168.136.13
        //
        MapNotify mn = MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("40 00 00 "
        //
                + "04 " // Record count
                + "FF BB 00 00 00 00 00 00 00 01 00 14 b9 cd 7b 89 " //
                + "65 c2 56 03 be dd 81 20 47 e5 c3 4f 56 02 e1 59 " //
                + "00 00 00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " // Record
                // 0
                + "ff 00 00 05 00 01 c0 a8 88 0a " // contd
                + "00 00 00 0b 01 17 50 00 01 11 00 01 98 10 fe 01 01 64 " // Record
                // 1
                + "ff 00 00 05 00 01 c0 a8 88 0b " // contd
                + "00 00 00 0c 01 20 00 00 02 22 00 01 97 10 fe 01 01 64 " // Record
                // 2
                + "ff 00 00 05 00 01 c0 a8 88 0c " // contd
                + "00 00 00 0d 01 20 20 00 03 33 00 01 96 10 fe 01 01 64 " // Record
                // 3
                + "ff 00 00 05 00 01 c0 a8 88 0d " // contd
        ));

        assertEquals(4, mn.getEidToLocatorRecord().size());

        EidToLocatorRecord record0 = mn.getEidToLocatorRecord().get(0);
        EidToLocatorRecord record1 = mn.getEidToLocatorRecord().get(1);
        EidToLocatorRecord record2 = mn.getEidToLocatorRecord().get(2);
        EidToLocatorRecord record3 = mn.getEidToLocatorRecord().get(3);

        assertEquals(10, record0.getRecordTtl().intValue());
        assertEquals(13, record3.getRecordTtl().intValue());

        assertEquals(32, record0.getMaskLength().intValue());
        assertEquals(23, record1.getMaskLength().intValue());

        assertEquals(Action.NoAction, record0.getAction());
        assertEquals(Action.SendMapRequest, record1.getAction());
        assertEquals(Action.NoAction, record2.getAction());
        assertEquals(Action.NativelyForward, record3.getAction());

        assertTrue(record0.isAuthoritative());
        assertTrue(record1.isAuthoritative());
        assertFalse(record2.isAuthoritative());
        assertFalse(record3.isAuthoritative());

        assertEquals(0x000, record0.getMapVersion().shortValue());
        assertEquals(0x111, record1.getMapVersion().shortValue());
        assertEquals(0x222, record2.getMapVersion().shortValue());
        assertEquals(0x333, record3.getMapVersion().shortValue());
    }

    @Test
    public void deserialize__IllegalAction() throws Exception {
        MapNotify mn = MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("40 00 01 01 FF BB "
        //
                + "00 00 00 00 00 00 00 01 00 14 ec 47 1e 53 25 91 " //
                + "2f 68 10 75 13 dd 2c e8 6e 3c ac 94 ed e4 00 00 " //
                + "00 0a 01 20 F0 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertEquals(1, mn.getEidToLocatorRecord().size());
        assertEquals(Action.NoAction, mn.getEidToLocatorRecord().get(0).getAction());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize_WrongAuthenticationLength() throws Exception {
        // LISP(Type = 4 Map-Notify, I=0
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0000
        // AuthDataLength: 20 Data:
        // e8:f5:0b:c5:c5:f2:b0:21:27:a8:a5:68:89:ec
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("48 00 00 01 FF BB "
        //
                + "00 00 00 00 00 00 00 00 00 14 e8 f5 0b c5 c5 f2 " //
                + "b0 21 27 a8 21 a5 68 89 ec 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));
    }

    @Test
    public void deserialize__SHA1() throws Exception {
        // LISP(Type = 4 Map-Notify, I=0
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0001
        // AuthDataLength: 20 Data:
        // b2:dd:1a:25:c0:60:b1:46:e8:dc:6d:a6:ae:2e:92:92:a6:ca:b7:9d
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapNotify mn = MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("40 00 00 01 FF BB "
        //
                + "00 00 00 00 00 00 00 01 00 14 2c 61 b9 c9 9a 20 " //
                + "ba d8 f5 40 d3 55 6f 5f 6e 5a b2 0a bf b5 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertFalse(mn.isXtrSiteIdPresent());

        assertEquals(1, mn.getEidToLocatorRecord().size());
        assertEquals(0xFFBB000000000000L, mn.getNonce().longValue());
        assertEquals(0x0001, mn.getKeyId().shortValue());
        byte[] expectedAuthenticationData = { (byte) 0x2c, (byte) 0x61, (byte) 0xb9, (byte) 0xc9, (byte) 0x9a, (byte) 0x20, (byte) 0xba, (byte) 0xd8, //
                (byte) 0xf5, (byte) 0x40, (byte) 0xd3, (byte) 0x55, (byte) 0x6f, (byte) 0x5f, (byte) 0x6e, (byte) 0x5a, //
                (byte) 0xb2, (byte) 0x0a, (byte) 0xbf, (byte) 0xb5 };
        ArrayAssert.assertEquals(expectedAuthenticationData, mn.getAuthenticationData());
    }

    @Test
    public void deserialize__SHA256() throws Exception {
        // LISP(Type = 4 Map-Notify, I=0
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0002
        // AuthDataLength: 32 Data:
        // 70 30 d4 c6 10 44 0d 83 be 4d bf fd a9 8c 57 6d 68 a5 bf 32 11 c9 7b
        // 58 c4 b9 9f 06 11 23 b9 38
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapNotify mn = MapNotifySerializer.getInstance().deserialize(hexToByteBuffer("40 00 00 01 FF BB "
        //
                + "00 00 00 00 00 00 00 02 00 20 70 30 d4 c6 10 44 0d 83 be 4d bf fd a9 8c 57 6d 68 a5 bf 32 "
                //
                + "11 c9 7b 58 c4 b9 9f 06 11 23 b9 38 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertFalse(mn.isXtrSiteIdPresent());

        assertEquals(1, mn.getEidToLocatorRecord().size());
        assertEquals(0xFFBB000000000000L, mn.getNonce().longValue());
        assertEquals(0x0002, mn.getKeyId().shortValue());
        byte[] expectedAuthenticationData = { (byte) 0x70, (byte) 0x30, (byte) 0xd4, (byte) 0xc6, (byte) 0x10, (byte) 0x44, (byte) 0x0d, (byte) 0x83,
                (byte) 0xbe, (byte) 0x4d, (byte) 0xbf, (byte) 0xfd, (byte) 0xa9, (byte) 0x8c, (byte) 0x57, (byte) 0x6d, (byte) 0x68, (byte) 0xa5,
                (byte) 0xbf, (byte) 0x32, (byte) 0x11, (byte) 0xc9, (byte) 0x7b, (byte) 0x58, (byte) 0xc4, (byte) 0xb9, (byte) 0x9f, (byte) 0x06,
                (byte) 0x11, (byte) 0x23, (byte) 0xb9, (byte) 0x38 };
        ArrayAssert.assertEquals(expectedAuthenticationData, mn.getAuthenticationData());
    }
}
