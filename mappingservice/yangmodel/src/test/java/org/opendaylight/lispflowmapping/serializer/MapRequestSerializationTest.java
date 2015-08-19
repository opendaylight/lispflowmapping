/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.serializer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.NoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.no.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequestBuilder;

public class MapRequestSerializationTest extends BaseTestCase {

    @Test
    public void prefix__NoPrefix() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setEidRecord(new ArrayList<EidRecord>());
        mrBuilder.getEidRecord().add(
                new EidRecordBuilder().setLispAddressContainer(
                        new LispAddressContainerBuilder().setAddress(
                                new NoBuilder().setNoAddress(
                                        new NoAddressBuilder().setAfi((short) 0)
                                        .build()).build()).build()).build());


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
    public void deserialize__LispMobMapRequestWithReply() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("14 00 00 01 3e f7 " //
                + "7f 5b 41 7c 77 3c 00 01 01 01 01 01 00 01 c0 a8 " //
                + "38 66 00 20 00 01 01 02 03 04 00 00 00 0a 01 20 " //
                + "10 00 00 00 00 01 01 01 01 01 01 64 ff 00 00 05 " //
                + "00 01 c0 a8 38 66"));
        assertEquals("1.1.1.1", ((Ipv4) mr.getSourceEid().getLispAddressContainer().getAddress()).getIpv4Address().getIpv4Address().getValue());
        assertEquals("1.2.3.4", ((Ipv4) mr.getEidRecord().get(0).getLispAddressContainer().getAddress()).getIpv4Address().getIpv4Address().getValue());

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
    public void deserialize__ContainsMapReply() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 " //
                + "01 " // single record
                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a " //
                + "00 20 00 01 01 02 03 04 "// end of map request
                + "00 00 " //
                + "00 02 01 20 00 00 00 00 " //
                + "00 01 01 02 03 04 01 02 " //
                + "03 04 00 06 00 01 0a 0a " //
                + "0a 0a"

        ));

        assertEquals(1, mr.getEidRecord().size());
        EidRecord eid = mr.getEidRecord().get(0);
        assertEquals(0x20, eid.getMask().byteValue());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4")), eid.getLispAddressContainer());
        MapReply mapReply = mr.getMapReply();
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("1.2.3.4"))), mapReply.getLispAddressContainer());
        assertEquals(false, mapReply.isAuthoritative());
        assertEquals(Action.NoAction, mapReply.getAction());
        assertEquals(0, mapReply.getMapVersion().shortValue());
        assertEquals(32, mapReply.getMaskLength().shortValue());
        assertEquals(2, mapReply.getRecordTtl().byteValue());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("10.10.10.10"))), mapReply.getLocatorRecord().get(0)
                .getLispAddressContainer());
        assertEquals(1, mapReply.getLocatorRecord().get(0).getPriority().byteValue());
        assertEquals(2, mapReply.getLocatorRecord().get(0).getWeight().byteValue());
        assertEquals(3, mapReply.getLocatorRecord().get(0).getMulticastPriority().byteValue());
        assertEquals(4, mapReply.getLocatorRecord().get(0).getMulticastWeight().byteValue());
        assertEquals(true, mapReply.getLocatorRecord().get(0).isLocalLocator());
        assertEquals(true, mapReply.getLocatorRecord().get(0).isRlocProbed());
        assertEquals(false, mapReply.getLocatorRecord().get(0).isRouted());
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

    @Test
    public void serialize__AllWithMapReply() throws Exception {
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setProbe(true);
        mapRequestBuilder.setMapDataPresent(true);
        mapRequestBuilder.setPitr(true);
        mapRequestBuilder.setNonce((long) 13);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.0.0.1"))).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("1:2:3:4:5:6:7:8")))
                        .build());
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.2.3.4"))).build());
        MapReplyBuilder mapreplyBuilder = new MapReplyBuilder();

        mapreplyBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1")));
        mapreplyBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());

        LocatorRecordBuilder locatorBuilder = new LocatorRecordBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.2")));
        mapreplyBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRequestBuilder.setMapReply(mapreplyBuilder.build());

        ByteBuffer expected = hexToByteBuffer("16 80 01 01 00 00 " //
                + "00 00 00 00 00 0D 00 01 0a 00 00 01 00 01 01 02 03 04 00 02 00 01 00 02 00 03 00 04 00 05 00 06 00 07 00 08 00 20 00 01 01 02 03 04 "// map
                                                                                                                                                        // request
                + "00 00 00 00 01 00 00 00 00 00 00 01 00 00 00 01 00 00 00 00 00 00 00 01 00 00 00 02");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mapRequestBuilder.build()).array());
    }

}
