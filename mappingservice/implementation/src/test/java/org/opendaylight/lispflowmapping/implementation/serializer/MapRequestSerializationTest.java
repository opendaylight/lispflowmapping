/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.serializer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.NoBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;

public class MapRequestSerializationTest extends BaseTestCase {

    @Test
    public void prefix__NoPrefix() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setEidRecord(new ArrayList<EidRecord>());
        mrBuilder.getEidRecord().add(
                new EidRecordBuilder().setLispAddressContainer(
                        new LispAddressContainerBuilder().setAddress(new NoBuilder().setAfi((short) 0).build()).build()).build());

        assertEquals(AddressFamilyNumberEnum.NO_ADDRESS.getIanaCode(),
                LispAFIConvertor.toAFI(mrBuilder.getEidRecord().get(0).getLispAddressContainer()).getAfi().shortValue());
    }

    @Test
    public void deserialize__FlagsInFirstByte() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 00 00 01 3d 8d "
        //
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
                + "00 01 01 02 03 04"));
        assertFalse(mr.isAuthoritative());
        assertTrue(mr.isMapDataPresent());
        assertTrue(mr.isProbe());
        assertFalse(mr.isSmr());

        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("19 00 00 01 3d 8d "
        //
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
                + "00 01 01 02 03 04"));
        assertTrue(mr.isAuthoritative());
        assertFalse(mr.isMapDataPresent());
        assertFalse(mr.isProbe());
        assertTrue(mr.isSmr());

        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("1C 00 00 01 3d 8d "
        //
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
                + "00 01 01 02 03 04"));
        assertTrue(mr.isAuthoritative());
        assertTrue(mr.isMapDataPresent());
        assertFalse(mr.isProbe());
        assertFalse(mr.isSmr());
    }

    @Test
    public void serialize__EmptyMapRequest() throws Exception {

        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        ByteBuffer expected = hexToByteBuffer("10 00 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void serialize__FlagsInFirstByte() throws Exception {

        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setAuthoritative(true);
        mrBuilder.setProbe(true);
        ByteBuffer expected = hexToByteBuffer("1A 00 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
        mrBuilder = new MapRequestBuilder();
        mrBuilder.setSmr(true);
        mrBuilder.setMapDataPresent(true);
        expected = hexToByteBuffer("15 00 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
        mrBuilder.setAuthoritative(true);
        mrBuilder.setProbe(true);
        expected = hexToByteBuffer("1F 00 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void serialize__FlagsInSecondByte() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setPitr(true);
        mrBuilder.setSmrInvoked(true);
        ByteBuffer expected = hexToByteBuffer("10 C0 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
        mrBuilder.setPitr(false);
        expected = hexToByteBuffer("10 40 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());

    }

    @Test
    public void deserialize__FlagsInSecondByte() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 01 3d 8d "
        //
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
                + "00 01 01 02 03 04"));
        assertTrue(mr.isPitr());
        assertFalse(mr.isSmrInvoked());

        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("19 40 00 01 3d 8d "
        //
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
                + "00 01 01 02 03 04"));
        assertFalse(mr.isPitr());
        assertTrue(mr.isSmrInvoked());
    }

    @Test
    public void deserialize__SingleEidRecord() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 "
        //
                + "01 " // single record
                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a " //
                + "00 20 00 01 01 02 03 04"));

        assertEquals(1, mr.getEidRecord().size());
        EidRecord eid = mr.getEidRecord().get(0);
        assertEquals(0x20, eid.getMask().byteValue());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4")), eid.getLispAddressContainer());
    }

    @Test
    public void serialize__SingleEidRecord() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setEidRecord(new ArrayList<EidRecord>());
        mrBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        ByteBuffer expected = hexToByteBuffer("10 00 00 01 00 00 " //
                + "00 00 00 00 00 00 00 00 00 20 00 01 01 02 03 04");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void deserialize__MultipleEidRecord() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 "
        //
                + "02 " // 2 records
                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a " //
                + "00 20 00 01 01 02 03 04 " //
                + "00 80 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05"));

        assertEquals(2, mr.getEidRecord().size());

        EidRecord eid = mr.getEidRecord().get(0);
        assertEquals(0x0020, eid.getMask().shortValue());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4")), eid.getLispAddressContainer());

        eid = mr.getEidRecord().get(1);
        assertEquals(0x0080, eid.getMask().shortValue());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:5")), eid.getLispAddressContainer());
    }

    @Test
    public void serialize__MultipleEidRecord() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setEidRecord(new ArrayList<EidRecord>());
        mrBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        mrBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 0)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1"))).build());
        ByteBuffer expected = hexToByteBuffer("10 00 00 02 00 00 " //
                + "00 00 00 00 00 00 00 00 00 20 00 01 01 02 03 04 00 00 00 01 04 03 02 01");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void deserialize__SingleItrRloc() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("10 00 "
        //
                + "00 " // This means 1 ITR-RLOC
                + "01 3d 8d 2a cd 39 c8 d6 08 00 00 " //
                + "00 01 c0 a8 88 0a " // IPv4 (ITR-RLOC #1 of 1)
                + "00 20 00 01 01 02 03 04"));

        assertEquals(1, mr.getItrRloc().size());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("192.168.136.10")), mr.getItrRloc().get(0)
                .getLispAddressContainer());
    }

    @Test
    public void serialize__SingleItrRloc() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        ByteBuffer expected = hexToByteBuffer("10 00 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00 00 01 01 02 03 04");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void serialize__MultipleItrRloc() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        mrBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1"))).build());
        ByteBuffer expected = hexToByteBuffer("10 00 01 00 00 00 " //
                + "00 00 00 00 00 00 00 00 00 01 01 02 03 04 00 01 04 03 02 01");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void deserialize__MultipleItrRlocs() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("10 00 "
        //
                + "02 " // This means 3 ITR - RLOCs
                + "01 3d 8d 2a cd 39 c8 d6 08 00 00 " //
                + "00 01 c0 a8 88 0a " // IPv4 (ITR-RLOC #1 of 3)
                // IPv6 (ITR-RLOC #2 of 3)
                + "00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 " //
                + "00 01 11 22 34 56 " // IPv4 (ITR-RLOC #3 of 3)
                + "00 20 00 01 01 02 03 04"));

        assertEquals(3, mr.getItrRloc().size());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("192.168.136.10")), mr.getItrRloc().get(0)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1")), mr.getItrRloc().get(1)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("17.34.52.86")), mr.getItrRloc().get(2).getLispAddressContainer());
    }

    @Test
    public void serialize__All() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setProbe(true);
        mrBuilder.setPitr(true);
        mrBuilder.setNonce((long) 13);
        mrBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.0.0.1"))).build());
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        mrBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("1:2:3:4:5:6:7:8")))
                        .build());
        mrBuilder.setEidRecord(new ArrayList<EidRecord>());
        mrBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        ByteBuffer expected = hexToByteBuffer("12 80 01 01 00 00 " //
                + "00 00 00 00 00 0D 00 01 0a 00 00 01 00 01 01 02 03 04 00 02 00 01 00 02 00 03 00 04 00 05 00 06 00 07 00 08 00 20 00 01 01 02 03 04");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

}
