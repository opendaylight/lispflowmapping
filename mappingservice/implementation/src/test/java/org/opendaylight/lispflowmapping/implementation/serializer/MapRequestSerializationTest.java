///*
// * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
// *
// * This program and the accompanying materials are made available under the
// * terms of the Eclipse Public License v1.0 which accompanies this distribution,
// * and is available at http://www.eclipse.org/legal/epl-v10.html
// */
//
//package org.opendaylight.lispflowmapping.implementation.serializer;
//
//import static org.junit.Assert.assertArrayEquals;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//import java.nio.ByteBuffer;
//
//import org.junit.Test;
//import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
//import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
//import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
//import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
//import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
//import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
//import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;
//
//public class MapRequestSerializationTest extends BaseTestCase {
//
//    @Test
//    public void prefix__NoPrefix() throws Exception {
//        MapRequest mr = new MapRequest();
//        mr.addEidRecord(new EidRecord((byte) 0, null));
//        mr.addEidRecord(new EidRecord((byte) 0, new LispNoAddress()));
//
//        assertEquals(AddressFamilyNumberEnum.RESERVED, mr.getEids().get(0).getPrefix().getAfi());
//        assertEquals(AddressFamilyNumberEnum.RESERVED, mr.getEids().get(1).getPrefix().getAfi());
//    }
//
//    @Test
//    public void deserialize__FlagsInFirstByte() throws Exception {
//        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 00 00 01 3d 8d " //
//                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
//                + "00 01 01 02 03 04"));
//        assertFalse(mr.isAuthoritative());
//        assertTrue(mr.isMapDataPresent());
//        assertTrue(mr.isProbe());
//        assertFalse(mr.isSmr());
//
//        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("19 00 00 01 3d 8d " //
//                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
//                + "00 01 01 02 03 04"));
//        assertTrue(mr.isAuthoritative());
//        assertFalse(mr.isMapDataPresent());
//        assertFalse(mr.isProbe());
//        assertTrue(mr.isSmr());
//
//        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("1C 00 00 01 3d 8d " //
//                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
//                + "00 01 01 02 03 04"));
//        assertTrue(mr.isAuthoritative());
//        assertTrue(mr.isMapDataPresent());
//        assertFalse(mr.isProbe());
//        assertFalse(mr.isSmr());
//    }
//
//    @Test
//    public void serialize__EmptyMapRequest() throws Exception {
//
//        MapRequest mr = new MapRequest();
//        ByteBuffer expected = hexToByteBuffer("10 00 00 00 00 00 " //
//                + "00 00 00 00 00 00 00 00");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//    }
//
//    @Test
//    public void serialize__FlagsInFirstByte() throws Exception {
//
//        MapRequest mr = new MapRequest();
//        mr.setAuthoritative(true);
//        mr.setProbe(true);
//        ByteBuffer expected = hexToByteBuffer("1A 00 00 00 00 00 " //
//                + "00 00 00 00 00 00 00 00");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//        mr = new MapRequest();
//        mr.setSmr(true);
//        mr.setMapDataPresent(true);
//        expected = hexToByteBuffer("15 00 00 00 00 00 " //
//                + "00 00 00 00 00 00 00 00");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//        mr.setAuthoritative(true);
//        mr.setProbe(true);
//        expected = hexToByteBuffer("1F 00 00 00 00 00 " //
//                + "00 00 00 00 00 00 00 00");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//    }
//
//    @Test
//    public void serialize__FlagsInSecondByte() throws Exception {
//        MapRequest mr = new MapRequest();
//        mr.setPitr(true);
//        mr.setSmrInvoked(true);
//        ByteBuffer expected = hexToByteBuffer("10 C0 00 00 00 00 " //
//                + "00 00 00 00 00 00 00 00");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//        mr.setPitr(false);
//        expected = hexToByteBuffer("10 40 00 00 00 00 " //
//                + "00 00 00 00 00 00 00 00");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//
//    }
//
//    @Test
//    public void deserialize__FlagsInSecondByte() throws Exception {
//        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 01 3d 8d " //
//                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
//                + "00 01 01 02 03 04"));
//        assertTrue(mr.isPitr());
//        assertFalse(mr.isSmrInvoked());
//
//        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("19 40 00 01 3d 8d " //
//                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
//                + "00 01 01 02 03 04"));
//        assertFalse(mr.isPitr());
//        assertTrue(mr.isSmrInvoked());
//    }
//
//    @Test
//    public void deserialize__SingleEidRecord() throws Exception {
//        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 " //
//                + "01 " // single record
//                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a " //
//                + "00 20 00 01 01 02 03 04"));
//
//        assertEquals(1, mr.getEids().size());
//        EidRecord eid = mr.getEids().get(0);
//        assertEquals(0x20, eid.getMaskLength());
//        assertEquals(new LispIpv4Address(0x01020304), eid.getPrefix());
//    }
//
//    @Test
//    public void serialize__SingleEidRecord() throws Exception {
//        MapRequest mr = new MapRequest();
//        mr.addEidRecord(new EidRecord((byte) 32, new LispIpv4Address("1.2.3.4")));
//        ByteBuffer expected = hexToByteBuffer("10 00 00 01 00 00 " //
//                + "00 00 00 00 00 00 00 00 00 20 00 01 01 02 03 04");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//    }
//
//    @Test
//    public void deserialize__MultipleEidRecord() throws Exception {
//        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 " //
//                + "02 " // 2 records
//                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a " //
//                + "00 20 00 01 01 02 03 04 " //
//                + "00 80 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05"));
//
//        assertEquals(2, mr.getEids().size());
//
//        EidRecord eid = mr.getEids().get(0);
//        assertEquals(0x20, eid.getMaskLength());
//        assertEquals(new LispIpv4Address(0x01020304), eid.getPrefix());
//
//        eid = mr.getEids().get(1);
//        assertEquals(0x80, eid.getMaskLength());
//        assertEquals(new LispIpv6Address("::5"), eid.getPrefix());
//    }
//
//    @Test
//    public void serialize__MultipleEidRecord() throws Exception {
//        MapRequest mr = new MapRequest();
//        mr.addEidRecord(new EidRecord((byte) 32, new LispIpv4Address("1.2.3.4")));
//        mr.addEidRecord(new EidRecord((byte) 0, new LispIpv4Address("4.3.2.1")));
//        ByteBuffer expected = hexToByteBuffer("10 00 00 02 00 00 " //
//                + "00 00 00 00 00 00 00 00 00 20 00 01 01 02 03 04 00 00 00 01 04 03 02 01");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//    }
//
//    @Test
//    public void deserialize__SingleItrRloc() throws Exception {
//        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("10 00 " //
//                + "00 " // This means 1 ITR-RLOC
//                + "01 3d 8d 2a cd 39 c8 d6 08 00 00 " //
//                + "00 01 c0 a8 88 0a " // IPv4 (ITR-RLOC #1 of 1)
//                + "00 20 00 01 01 02 03 04"));
//
//        assertEquals(1, mr.getItrRlocs().size());
//        assertEquals(new LispIpv4Address(0xC0A8880A), mr.getItrRlocs().get(0));
//    }
//
//    @Test
//    public void serialize__SingleItrRloc() throws Exception {
//        MapRequest mr = new MapRequest();
//        mr.addItrRloc(new LispIpv4Address("1.2.3.4"));
//        ByteBuffer expected = hexToByteBuffer("10 00 00 00 00 00 " //
//                + "00 00 00 00 00 00 00 00 00 01 01 02 03 04");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//    }
//
//    @Test
//    public void serialize__MultipleItrRloc() throws Exception {
//        MapRequest mr = new MapRequest();
//        mr.addItrRloc(new LispIpv4Address("1.2.3.4"));
//        mr.addItrRloc(new LispIpv4Address("4.3.2.1"));
//        ByteBuffer expected = hexToByteBuffer("10 00 01 00 00 00 " //
//                + "00 00 00 00 00 00 00 00 00 01 01 02 03 04 00 01 04 03 02 01");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//    }
//
//    @Test
//    public void deserialize__MultipleItrRlocs() throws Exception {
//        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("10 00 " //
//                + "02 " // This means 3 ITR - RLOCs
//                + "01 3d 8d 2a cd 39 c8 d6 08 00 00 " //
//                + "00 01 c0 a8 88 0a " // IPv4 (ITR-RLOC #1 of 3)
//                // IPv6 (ITR-RLOC #2 of 3)
//                + "00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 " //
//                + "00 01 11 22 34 56 " // IPv4 (ITR-RLOC #3 of 3)
//                + "00 20 00 01 01 02 03 04"));
//
//        assertEquals(3, mr.getItrRlocs().size());
//        assertEquals(new LispIpv4Address(0xC0A8880A), mr.getItrRlocs().get(0));
//        assertEquals(new LispIpv6Address("::1"), mr.getItrRlocs().get(1));
//        assertEquals(new LispIpv4Address(0x11223456), mr.getItrRlocs().get(2));
//    }
//
//    @Test
//    public void serialize__All() throws Exception {
//        MapRequest mr = new MapRequest();
//        mr.setProbe(true);
//        mr.setPitr(true);
//        mr.setNonce(13);
//        mr.setSourceEid(new LispIpv4Address("10.0.0.1"));
//        mr.addItrRloc(new LispIpv4Address("1.2.3.4"));
//        mr.addItrRloc(new LispIpv6Address("1:2:3:4:5:6:7:8"));
//        mr.addEidRecord(new EidRecord((byte) 32, new LispIpv4Address("1.2.3.4")));
//        ByteBuffer expected = hexToByteBuffer("12 80 01 01 00 00 " //
//                + "00 00 00 00 00 0D 00 01 0a 00 00 01 00 01 01 02 03 04 00 02 00 01 00 02 00 03 00 04 00 05 00 06 00 07 00 08 00 20 00 01 01 02 03 04");
//        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mr).array());
//    }
//
// }
