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
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.NoAddressAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;

public class MapRequestSerializationTest extends BaseTestCase {

    @Test
    public void prefix__NoPrefix() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setEidItem(new ArrayList<EidItem>());
        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(LispAddressUtil.getNoAddressEid()).build());


        assertEquals(NoAddressAfi.class, mrBuilder.getEidItem().get(0).getEid().getAddressType());
    }

    @Test
    public void deserialize__FlagsInFirstByte() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 00 00 01 3d 8d "
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 "
                + "00 01 01 02 03 04"));
        assertFalse(mr.isAuthoritative());
        assertTrue(mr.isMapDataPresent());
        assertTrue(mr.isProbe());
        assertFalse(mr.isSmr());

        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("19 00 00 01 3d 8d "
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 "
                + "00 01 01 02 03 04"));
        assertTrue(mr.isAuthoritative());
        assertFalse(mr.isMapDataPresent());
        assertFalse(mr.isProbe());
        assertTrue(mr.isSmr());

        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("1C 00 00 01 3d 8d "
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 "
                + "00 01 01 02 03 04"));
        assertTrue(mr.isAuthoritative());
        assertTrue(mr.isMapDataPresent());
        assertFalse(mr.isProbe());
        assertFalse(mr.isSmr());
    }

    @Test
    public void deserialize__LispMobMapRequestWithReply() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("14 00 00 01 3e f7 "
                + "7f 5b 41 7c 77 3c 00 01 01 01 01 01 00 01 c0 a8 "
                + "38 66 00 20 00 01 01 02 03 04 00 00 00 0a 01 20 "
                + "10 00 00 00 00 01 01 01 01 01 01 64 ff 00 00 05 "
                + "00 01 c0 a8 38 66"));
        assertArrayEquals(new byte[] {1, 1, 1, 1},
                ((Ipv4Binary) mr.getSourceEid().getEid().getAddress()).getIpv4Binary().getValue());
        assertEquals("1.2.3.4/32",
                ((Ipv4Prefix) mr.getEidItem().get(0).getEid().getAddress()).getIpv4Prefix().getValue());

    }

    @Test
    public void serialize__EmptyMapRequest() throws Exception {

        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        ByteBuffer expected = hexToByteBuffer("10 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void serialize__FlagsInFirstByte() throws Exception {

        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setAuthoritative(true);
        mrBuilder.setProbe(true);
        ByteBuffer expected = hexToByteBuffer("1A 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
        mrBuilder = new MapRequestBuilder();
        mrBuilder.setSmr(true);
        mrBuilder.setMapDataPresent(true);
        expected = hexToByteBuffer("15 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
        mrBuilder.setAuthoritative(true);
        mrBuilder.setProbe(true);
        expected = hexToByteBuffer("1F 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void serialize__FlagsInSecondByte() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setPitr(true);
        mrBuilder.setSmrInvoked(true);
        ByteBuffer expected = hexToByteBuffer("10 C0 00 00 00 00 "
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
        mrBuilder.setPitr(false);
        expected = hexToByteBuffer("10 40 00 00 00 00 "
                + "00 00 00 00 00 00 00 00");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());

    }

    @Test
    public void deserialize__FlagsInSecondByte() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 01 3d 8d "
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 "
                + "00 01 01 02 03 04"));
        assertTrue(mr.isPitr());
        assertFalse(mr.isSmrInvoked());

        mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("19 40 00 01 3d 8d "
                + "2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 "
                + "00 01 01 02 03 04"));
        assertFalse(mr.isPitr());
        assertTrue(mr.isSmrInvoked());
    }

    @Test
    public void deserialize__SingleEidItem() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 "
                + "01 " // single record
                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a "
                + "00 20 00 01 01 02 03 04"));

        assertEquals(1, mr.getEidItem().size());
        Eid eid = mr.getEidItem().get(0).getEid();
        assertEquals("1.2.3.4/32", ((Ipv4Prefix) eid.getAddress()).getIpv4Prefix().getValue());
    }

    @Test
    public void deserialize__ContainsMapReply() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 "
                + "01 " // single record
                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a "
                + "00 20 00 01 01 02 03 04 "// end of map request
                + "00 00 "
                + "00 02 01 20 00 00 00 00 "
                + "00 01 01 02 03 04 01 02 "
                + "03 04 00 06 00 01 0a 0a "
                + "0a 0a"

        ));

        assertEquals(1, mr.getEidItem().size());
        Eid eid = mr.getEidItem().get(0).getEid();
        assertEquals("1.2.3.4/32", ((Ipv4Prefix) eid.getAddress()).getIpv4Prefix().getValue());
        MappingRecord record = mr.getMapReply().getMappingRecord();
        assertEquals("1.2.3.4/32", ((Ipv4Prefix) record.getEid().getAddress()).getIpv4Prefix().getValue());
        assertEquals(false, record.isAuthoritative());
        assertEquals(Action.NoAction, record.getAction());
        assertEquals(0, record.getMapVersion().shortValue());
        assertEquals(32, MaskUtil.getMaskForAddress(record.getEid().getAddress()));
        assertEquals(2, record.getRecordTtl().byteValue());
        assertArrayEquals(new byte[] {10, 10, 10, 10},
                ((Ipv4Binary) record.getLocatorRecord().get(0).getRloc().getAddress()).getIpv4Binary().getValue());
        assertEquals(1, record.getLocatorRecord().get(0).getPriority().byteValue());
        assertEquals(2, record.getLocatorRecord().get(0).getWeight().byteValue());
        assertEquals(3, record.getLocatorRecord().get(0).getMulticastPriority().byteValue());
        assertEquals(4, record.getLocatorRecord().get(0).getMulticastWeight().byteValue());
        assertEquals(true, record.getLocatorRecord().get(0).isLocalLocator());
        assertEquals(true, record.getLocatorRecord().get(0).isRlocProbed());
        assertEquals(false, record.getLocatorRecord().get(0).isRouted());
    }

    @Test
    public void serialize__SingleEidItem() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setEidItem(new ArrayList<EidItem>());
        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixEid("1.2.3.4/32")).build());
        ByteBuffer expected = hexToByteBuffer("10 00 00 01 00 00 " //
                + "00 00 00 00 00 00 00 00 00 20 00 01 01 02 03 04");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void deserialize__MultipleEidItem() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("16 80 00 "
        //
                + "02 " // 2 records
                + "3d 8d 2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a " //
                + "00 20 00 01 01 02 03 04 " //
                + "00 80 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05"));

        assertEquals(2, mr.getEidItem().size());

        Eid eid = mr.getEidItem().get(0).getEid();
        assertEquals("1.2.3.4/32", ((Ipv4Prefix) eid.getAddress()).getIpv4Prefix().getValue());

        eid = mr.getEidItem().get(1).getEid();
        assertEquals("0:0:0:0:0:0:0:5/128", ((Ipv6Prefix) eid.getAddress()).getIpv6Prefix().getValue());
    }

    @Test
    public void serialize__MultipleEidItem() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setEidItem(new ArrayList<EidItem>());
        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixEid("1.2.3.4/32")).build());
        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixEid("4.3.2.1/0")).build());
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
        assertArrayEquals(new byte[] {(byte) 192, (byte) 168, (byte) 136, (byte) 10},
                ((Ipv4Binary) mr.getItrRloc().get(0).getRloc().getAddress()).getIpv4Binary().getValue());
    }

    @Test
    public void serialize__SingleItrRloc() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc("1.2.3.4")).build());
        ByteBuffer expected = hexToByteBuffer("10 00 00 00 00 00 " //
                + "00 00 00 00 00 00 00 00 00 01 01 02 03 04");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void serialize__MultipleItrRloc() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc("1.2.3.4")).build());
        mrBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1")).build());
        ByteBuffer expected = hexToByteBuffer("10 00 01 00 00 00 " //
                + "00 00 00 00 00 00 00 00 00 01 01 02 03 04 00 01 04 03 02 01");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void deserialize__MultipleItrRlocs() throws Exception {
        MapRequest mr = MapRequestSerializer.getInstance().deserialize(hexToByteBuffer("10 00 "
                + "02 " // This means 3 ITR - RLOCs
                + "01 3d 8d 2a cd 39 c8 d6 08 00 00 "
                + "00 01 c0 a8 88 0a " // IPv4 (ITR-RLOC #1 of 3)
                // IPv6 (ITR-RLOC #2 of 3)
                + "00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 " //
                + "00 01 11 22 34 56 " // IPv4 (ITR-RLOC #3 of 3)
                + "00 20 00 01 01 02 03 04"));

        assertEquals(3, mr.getItrRloc().size());
        assertArrayEquals(new byte[] {(byte) 192, (byte) 168, (byte) 136, (byte) 10},
                ((Ipv4Binary) mr.getItrRloc().get(0).getRloc().getAddress()).getIpv4Binary().getValue());
        assertArrayEquals(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                ((Ipv6Binary) mr.getItrRloc().get(1).getRloc().getAddress()).getIpv6Binary().getValue());
        assertArrayEquals(new byte[] {17, 34, 52, 86},
                ((Ipv4Binary) mr.getItrRloc().get(2).getRloc().getAddress()).getIpv4Binary().getValue());
    }

    @Test
    public void serialize__All() throws Exception {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setProbe(true);
        mrBuilder.setPitr(true);
        mrBuilder.setNonce((long) 13);
        mrBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(("10.0.0.1"))).build());
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc("1.2.3.4")).build());
        mrBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv6Rloc("1:2:3:4:5:6:7:8")).build());
        mrBuilder.setEidItem(new ArrayList<EidItem>());
        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixEid("1.2.3.4/32")).build());
        ByteBuffer expected = hexToByteBuffer("12 80 01 01 00 00 " //
                + "00 00 00 00 00 0D 00 01 0a 00 00 01 00 01 01 02 03 04 00 02 00 01 00 02 00 03 00 04 00 05 00 06 "
                + "00 07 00 08 00 20 00 01 01 02 03 04");
        assertArrayEquals(expected.array(), MapRequestSerializer.getInstance().serialize(mrBuilder.build()).array());
    }

    @Test
    public void serialize__AllWithMapReply() throws Exception {
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setProbe(true);
        mapRequestBuilder.setMapDataPresent(true);
        mapRequestBuilder.setPitr(true);
        mapRequestBuilder.setNonce((long) 13);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(("10.0.0.1")))
                .build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc("1.2.3.4"))
                .build());
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv6Rloc("1:2:3:4:5:6:7:8"))
                .build());
        mapRequestBuilder.setEidItem(new ArrayList<EidItem>());
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(LispAddressUtil.asIpv4PrefixEid("1.2.3.4/32"))
                .build());
        MapReplyBuilder mapreplyBuilder = new MapReplyBuilder();
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();

        recordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("0.0.0.1/0"));
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());

        LocatorRecordBuilder locatorBuilder = new LocatorRecordBuilder();
        locatorBuilder.setRloc(LispAddressUtil.asIpv4Rloc("0.0.0.2"));
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapreplyBuilder.setMappingRecord(recordBuilder.build());
        mapRequestBuilder.setMapReply(mapreplyBuilder.build());

        ByteBuffer expected = hexToByteBuffer("16 80 01 01 00 00 "
                + "00 00 00 00 00 0D 00 01 0a 00 00 01 00 01 01 02 03 04 00 02 00 01 00 02 00 03 00 04 00 05 00 06 "
                + "00 07 00 08 00 20 00 01 01 02 03 04 "// map
                + "00 00 00 00 01 00 00 00 00 00 00 01 00 00 00 01 00 00 00 00 00 00 00 01 00 00 00 02");
        assertArrayEquals(expected.array(),
                MapRequestSerializer.getInstance().serialize(mapRequestBuilder.build()).array());
    }
}
