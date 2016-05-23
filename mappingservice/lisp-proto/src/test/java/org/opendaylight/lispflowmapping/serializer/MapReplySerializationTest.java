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

import java.nio.ByteBuffer;
import java.util.ArrayList;

import junitx.framework.ArrayAssert;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;

public class MapReplySerializationTest extends BaseTestCase {

    @Test
    @Ignore
    public void todo() throws Exception {
        fail("support no eid to locator???");
        fail("Validation of values in setters (map-version)");
        fail("Non NPE for no locator in LocatorRecord.serialize");
    }

    @Test
    public void serialize__SomeFlags() throws Exception {
        MapReplyBuilder mr = new MapReplyBuilder();
        mr.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("0.0.0.1/32"));
        mr.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mr.setProbe(true);
        mr.setEchoNonceEnabled(false);

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mr.build());
        byte firstByte = packet.get(0);
        assertHexEquals((byte) 0x28, firstByte);

        mr.setProbe(false);
        mr.setEchoNonceEnabled(true);

        packet = MapReplySerializer.getInstance().serialize(mr.build());
        firstByte = packet.get(0);
        assertHexEquals((byte) 0x24, firstByte);
    }

    @Test
    public void deserialize__SomeFlags() throws Exception {

        MapReply mr = MapReplySerializer.getInstance().deserialize(hexToByteBuffer("2A 00 00 00 00 00 "
                + "00 00 00 00 00 00"));
        assertEquals(true, mr.isProbe());
        assertEquals(false, mr.isEchoNonceEnabled());
        assertEquals(true, mr.isSecurityEnabled());
    }

    @Test
    public void deserialize__All() throws Exception {
        MapReply mr = MapReplySerializer.getInstance().deserialize(hexToByteBuffer("20 00 00 02 00 00 "
                + "00 00 00 00 00 02 00 00 "
                + "00 02 02 20 00 00 00 00 "
                + "00 01 01 02 03 04 01 02 "
                + "03 04 00 06 00 01 0a 0a "
                + "0a 0a 04 03 02 01 00 01 "
                + "00 02 00 01 00 02 00 03 "
                + "00 04 00 05 00 06 00 07 00 08 00 00 00 00 00 10 30 00 00 02 00 01 04 03 00 00"));
        assertEquals(2, mr.getNonce().longValue());
        assertArrayEquals(new byte[] {1, 2, 3, 4}, ((Ipv4PrefixBinary) mr.getMappingRecordItem().get(0)
                .getMappingRecord().getEid().getAddress()).getIpv4AddressBinary().getValue());
        // XXX Why here normalized and other cases not normalized?
        assertArrayEquals(new byte[] {4, 3, 0, 0}, ((Ipv4PrefixBinary) mr.getMappingRecordItem().get(1)
                .getMappingRecord().getEid().getAddress()).getIpv4AddressBinary().getValue());
        assertEquals(false, mr.getMappingRecordItem().get(0).getMappingRecord().isAuthoritative());
        assertEquals(true, mr.getMappingRecordItem().get(1).getMappingRecord().isAuthoritative());
        assertEquals(Action.NoAction, mr.getMappingRecordItem().get(0).getMappingRecord().getAction());
        assertEquals(Action.NativelyForward, mr.getMappingRecordItem().get(1).getMappingRecord().getAction());
        assertEquals(0, mr.getMappingRecordItem().get(0).getMappingRecord().getMapVersion().shortValue());
        assertEquals(2, mr.getMappingRecordItem().get(1).getMappingRecord().getMapVersion().shortValue());
        assertEquals(32, MaskUtil.getMaskForAddress(mr.getMappingRecordItem().get(0).getMappingRecord()
                .getEid().getAddress()));
        assertEquals(16, MaskUtil.getMaskForAddress(mr.getMappingRecordItem().get(1).getMappingRecord()
                .getEid().getAddress()));
        assertEquals(2, mr.getMappingRecordItem().get(0).getMappingRecord().getRecordTtl().byteValue());
        assertEquals(0, mr.getMappingRecordItem().get(1).getMappingRecord().getRecordTtl().byteValue());
        assertArrayEquals(new byte[] {0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8},
                ((Ipv6Binary) mr.getMappingRecordItem().get(0).getMappingRecord()
                .getLocatorRecord().get(1).getRloc().getAddress()).getIpv6Binary().getValue());
        assertArrayEquals(new byte[] {10, 10, 10, 10},
                ((Ipv4Binary) mr.getMappingRecordItem().get(0).getMappingRecord()
                .getLocatorRecord().get(0).getRloc().getAddress()).getIpv4Binary().getValue());
        assertEquals(1, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getPriority().byteValue());
        assertEquals(2, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getWeight().byteValue());
        assertEquals(3, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getMulticastPriority().byteValue());
        assertEquals(4, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getMulticastWeight().byteValue());
        assertEquals(4, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1)
                .getPriority().byteValue());
        assertEquals(3, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1)
                .getWeight().byteValue());
        assertEquals(2, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1)
                .getMulticastPriority().byteValue());
        assertEquals(1, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1)
                .getMulticastWeight().byteValue());
        assertEquals(true, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .isLocalLocator());
        assertEquals(true, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .isRlocProbed());
        assertEquals(false, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .isRouted());
        assertEquals(false, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1)
                .isLocalLocator());
        assertEquals(false, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1)
                .isRlocProbed());
        assertEquals(true, mr.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(1).isRouted());
    }

    @Test
    public void serialize__MultipleRecordsWithoutRLOCs() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setEid(LispAddressUtil.asIpv6PrefixEid("0:0:0:0:0:0:0:8/128"));
        mrBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());

        recordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("8.2.4.5/32"));
        mrBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());
        assertEquals(2, packet.get(3));

        packet.position(24); // EID in first record
        byte[] expected = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x08 };
        byte[] actual = new byte[16];
        packet.get(actual);
        ArrayAssert.assertEquals(expected, actual);

        packet.position(packet.position() + 12); // EID in second record
        assertEquals(0x08020405, packet.getInt());
    }

    @Test
    public void deserialize__MultipleRecordsWithoutRLOCs() throws Exception {
        MapReply mr = MapReplySerializer.getInstance().deserialize(hexToByteBuffer("20 00 00 02 00 00 "
                + "00 00 00 00 00 00 00 00 00 01 00 20 00 00 00 00 "
                + "00 01 01 02 03 04 00 00 00 00 00 10 30 00 00 02 00 01 04 03 00 00"));
        assertArrayEquals(new byte[] {1, 2, 3, 4}, ((Ipv4PrefixBinary) mr.getMappingRecordItem().get(0)
                .getMappingRecord().getEid().getAddress()).getIpv4AddressBinary().getValue());
        // XXX Why here normalized and other cases not normalized?
        assertArrayEquals(new byte[] {4, 3, 0, 0}, ((Ipv4PrefixBinary) mr.getMappingRecordItem().get(1)
                .getMappingRecord().getEid().getAddress()).getIpv4AddressBinary().getValue());
        assertEquals(false, mr.getMappingRecordItem().get(0).getMappingRecord().isAuthoritative());
        assertEquals(true, mr.getMappingRecordItem().get(1).getMappingRecord().isAuthoritative());
        assertEquals(Action.NoAction, mr.getMappingRecordItem().get(0).getMappingRecord().getAction());
        assertEquals(Action.NativelyForward, mr.getMappingRecordItem().get(1).getMappingRecord().getAction());
        assertEquals(0, mr.getMappingRecordItem().get(0).getMappingRecord().getMapVersion().shortValue());
        assertEquals(2, mr.getMappingRecordItem().get(1).getMappingRecord().getMapVersion().shortValue());
        assertEquals(32, MaskUtil.getMaskForAddress(mr.getMappingRecordItem().get(0).getMappingRecord()
                .getEid().getAddress()));
        assertEquals(16, MaskUtil.getMaskForAddress(mr.getMappingRecordItem().get(1).getMappingRecord()
                .getEid().getAddress()));
        assertEquals(1, mr.getMappingRecordItem().get(0).getMappingRecord().getRecordTtl().byteValue());
        assertEquals(0, mr.getMappingRecordItem().get(1).getMappingRecord().getRecordTtl().byteValue());
    }

    @Test
    public void serialize__EidRecordDefaultAction() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("0.0.0.1/32"));
        mrBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(18);
        assertHexEquals((byte) 0x00, packet.get()); // MapReplyAction.NoAction
    }

    @Test
    public void serialize__EidRecordNullActionShouldTranslateToDefault() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("0.0.0.1/32"));
        recordBuilder.setAction(null);
        mrBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(18);
        assertHexEquals((byte) 0x00, packet.get()); // MapReplyAction.NoAction
    }

    @Test
    public void serialize__EidRecordFields() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());

        MappingRecordBuilder eidToLocator1 = new MappingRecordBuilder();
        eidToLocator1.setEid(LispAddressUtil.asIpv4PrefixEid("0.0.0.1/32"));
        eidToLocator1.setRecordTtl(7);
        eidToLocator1.setAction(Action.SendMapRequest);
        eidToLocator1.setAuthoritative(true);
        eidToLocator1.setMapVersion((short) 3072);
        mrBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(eidToLocator1.build()).build());

        MappingRecordBuilder eidToLocator2 = new MappingRecordBuilder();
        eidToLocator2.setEid(LispAddressUtil.asIpv4PrefixEid("0.0.0.7/32"));
        eidToLocator2.setRecordTtl(1000000);
        eidToLocator2.setAction(Action.Drop);
        eidToLocator2.setAuthoritative(false);
        eidToLocator2.setMapVersion((short) 29);
        mrBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(eidToLocator2.build()).build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(12); // First record
        assertEquals(7, packet.getInt());
        packet.position(packet.position() + 2); // skip Locator Count & Mask-len

        assertHexEquals((byte) 0x50, packet.get());
        packet.position(packet.position() + 1); // skip Reserved byte
        assertEquals((short) 3072, packet.getShort());

        packet.position(packet.position() + 6); // Second record
        assertEquals(1000000, packet.getInt());
        packet.position(packet.position() + 2); // skip Locator Count & Mask-len

        assertHexEquals((byte) 0x60, packet.get());
        packet.position(packet.position() + 1); // skip Reserved byte
        assertEquals((short) 29, packet.getShort());
    }

    @Test
    public void serialize__LocatorRecordFields() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());

        MappingRecordBuilder eidToLocatorBuilder = new MappingRecordBuilder();
        eidToLocatorBuilder.setEid(LispAddressUtil.asIpv4PrefixEid("0.0.0.1/32"));
        eidToLocatorBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());

        LocatorRecordBuilder locatorBuilder1 = new LocatorRecordBuilder();
        locatorBuilder1.setPriority((short) 0xF3);
        locatorBuilder1.setWeight((short) 0xF6);
        locatorBuilder1.setMulticastPriority((short) 0xA3);
        locatorBuilder1.setMulticastWeight((short) 0x06);
        locatorBuilder1.setRloc(LispAddressUtil.asIpv4Rloc("0.0.0.1"));
        locatorBuilder1.setLocalLocator(true);
        locatorBuilder1.setRlocProbed(true);
        locatorBuilder1.setRouted(true);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder1.build());

        LocatorRecordBuilder locatorBuilder2 = new LocatorRecordBuilder();
        locatorBuilder2.setPriority((short) 0x03);
        locatorBuilder2.setWeight((short) 0x06);
        locatorBuilder2.setMulticastPriority((short) 0x03);
        locatorBuilder2.setMulticastWeight((short) 0xF1);
        locatorBuilder2.setRloc(LispAddressUtil.asIpv4Rloc("0.0.0.2"));
        locatorBuilder2.setLocalLocator(false);
        locatorBuilder2.setRlocProbed(false);
        locatorBuilder2.setRouted(false);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mrBuilder.getMappingRecordItem().add(
                new MappingRecordItemBuilder().setMappingRecord(eidToLocatorBuilder.build()).build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(12 + 16); // First locator record
        assertHexEquals((byte) 0xF3, packet.get());
        assertHexEquals((byte) 0xF6, packet.get());
        assertHexEquals((byte) 0xA3, packet.get());
        assertHexEquals((byte) 0x06, packet.get());
        packet.position(packet.position() + 1);
        assertHexEquals((byte) 0x07, packet.get());

        packet.position(packet.position() + 6); // Second locator record
        assertHexEquals((byte) 0x03, packet.get());
        assertHexEquals((byte) 0x06, packet.get());
        assertHexEquals((byte) 0x03, packet.get());
        assertHexEquals((byte) 0xF1, packet.get());
        packet.position(packet.position() + 1);
        assertHexEquals((byte) 0x00, packet.get());
    }
}
