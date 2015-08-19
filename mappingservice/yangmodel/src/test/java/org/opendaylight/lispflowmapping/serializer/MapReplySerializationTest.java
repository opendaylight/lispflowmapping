/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.serializer;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import junitx.framework.ArrayAssert;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapreplymessage.MapReplyBuilder;

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
        mr.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mr.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1")))
                        .build());
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
        //
                + "00 00 00 00 00 00"));
        assertEquals(true, mr.isProbe());
        assertEquals(false, mr.isEchoNonceEnabled());
        assertEquals(true, mr.isSecurityEnabled());
    }

    @Test
    public void deserialize__All() throws Exception {
        MapReply mr = MapReplySerializer.getInstance().deserialize(hexToByteBuffer("20 00 00 02 00 00 "
        //
                + "00 00 00 00 00 02 00 00 " //
                + "00 02 02 20 00 00 00 00 " //
                + "00 01 01 02 03 04 01 02 " //
                + "03 04 00 06 00 01 0a 0a " //
                + "0a 0a 04 03 02 01 00 01 " //
                + "00 02 00 01 00 02 00 03 " //
                + "00 04 00 05 00 06 00 07 00 08 00 00 00 00 00 10 30 00 00 02 00 01 04 03 02 01"));
        assertEquals(2, mr.getNonce().longValue());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("1.2.3.4"))), mr.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("4.3.2.1"))), mr.getEidToLocatorRecord().get(1)
                .getLispAddressContainer());
        assertEquals(false, mr.getEidToLocatorRecord().get(0).isAuthoritative());
        assertEquals(true, mr.getEidToLocatorRecord().get(1).isAuthoritative());
        assertEquals(Action.NoAction, mr.getEidToLocatorRecord().get(0).getAction());
        assertEquals(Action.NativelyForward, mr.getEidToLocatorRecord().get(1).getAction());
        assertEquals(0, mr.getEidToLocatorRecord().get(0).getMapVersion().shortValue());
        assertEquals(2, mr.getEidToLocatorRecord().get(1).getMapVersion().shortValue());
        assertEquals(32, mr.getEidToLocatorRecord().get(0).getMaskLength().shortValue());
        assertEquals(16, mr.getEidToLocatorRecord().get(1).getMaskLength().shortValue());
        assertEquals(2, mr.getEidToLocatorRecord().get(0).getRecordTtl().byteValue());
        assertEquals(0, mr.getEidToLocatorRecord().get(1).getRecordTtl().byteValue());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress(("1:2:3:4:5:6:7:8"))), mr.getEidToLocatorRecord().get(0)
                .getLocatorRecord().get(1).getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("10.10.10.10"))), mr.getEidToLocatorRecord().get(0)
                .getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(1, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getPriority().byteValue());
        assertEquals(2, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getWeight().byteValue());
        assertEquals(3, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getMulticastPriority().byteValue());
        assertEquals(4, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getMulticastWeight().byteValue());
        assertEquals(4, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).getPriority().byteValue());
        assertEquals(3, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).getWeight().byteValue());
        assertEquals(2, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).getMulticastPriority().byteValue());
        assertEquals(1, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).getMulticastWeight().byteValue());
        assertEquals(true, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).isLocalLocator());
        assertEquals(true, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).isRlocProbed());
        assertEquals(false, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).isRouted());
        assertEquals(false, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).isLocalLocator());
        assertEquals(false, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).isRlocProbed());
        assertEquals(true, mr.getEidToLocatorRecord().get(0).getLocatorRecord().get(1).isRouted());
    }

    @Test
    public void serialize__MultipleRecordsWithoutRLOCs() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mrBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(
                        LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress(("0:0:0:0:0:0:0:8")))).build());
        mrBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("8.2.4.5")))
                        .build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());
        assertEquals(2, packet.get(3));

        packet.position(24); /* EID in first record */
        byte[] expected = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08 };
        byte[] actual = new byte[16];
        packet.get(actual);
        ArrayAssert.assertEquals(expected, actual);

        packet.position(packet.position() + 12); /* EID in second record */
        assertEquals(0x08020405, packet.getInt());
    }

    @Test
    public void deserialize__MultipleRecordsWithoutRLOCs() throws Exception {
        MapReply mr = MapReplySerializer.getInstance().deserialize(hexToByteBuffer("20 00 00 02 00 00 "
        //
                + "00 00 00 00 00 00 00 00 00 01 00 20 00 00 00 00 00 01 01 02 03 04 00 00 00 00 00 10 30 00 00 02 00 01 04 03 02 01"));
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("1.2.3.4"))), mr.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("4.3.2.1"))), mr.getEidToLocatorRecord().get(1)
                .getLispAddressContainer());
        assertEquals(false, mr.getEidToLocatorRecord().get(0).isAuthoritative());
        assertEquals(true, mr.getEidToLocatorRecord().get(1).isAuthoritative());
        assertEquals(Action.NoAction, mr.getEidToLocatorRecord().get(0).getAction());
        assertEquals(Action.NativelyForward, mr.getEidToLocatorRecord().get(1).getAction());
        assertEquals(0, mr.getEidToLocatorRecord().get(0).getMapVersion().shortValue());
        assertEquals(2, mr.getEidToLocatorRecord().get(1).getMapVersion().shortValue());
        assertEquals(32, mr.getEidToLocatorRecord().get(0).getMaskLength().shortValue());
        assertEquals(16, mr.getEidToLocatorRecord().get(1).getMaskLength().shortValue());
        assertEquals(1, mr.getEidToLocatorRecord().get(0).getRecordTtl().byteValue());
        assertEquals(0, mr.getEidToLocatorRecord().get(1).getRecordTtl().byteValue());
    }

    @Test
    public void serialize__EidRecordDefaultAction() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mrBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("0.0.0.1"))))
                        .build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(18);
        assertHexEquals((byte) 0x00, packet.get()); // MapReplyAction.NoAction
    }

    @Test
    public void serialize__EidRecordNullActionShouldTranslateToDefault() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mrBuilder.getEidToLocatorRecord().add(
                new EidToLocatorRecordBuilder().setAction(null)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(("0.0.0.1")))).build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(18);
        assertHexEquals((byte) 0x00, packet.get()); // MapReplyAction.NoAction
    }

    @Test
    public void serialize__EidRecordFields() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());

        EidToLocatorRecordBuilder eidToLocator1 = new EidToLocatorRecordBuilder();
        eidToLocator1.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1")));
        eidToLocator1.setRecordTtl(7);
        eidToLocator1.setAction(Action.SendMapRequest);
        eidToLocator1.setAuthoritative(true);
        eidToLocator1.setMapVersion((short) 3072);
        mrBuilder.getEidToLocatorRecord().add(eidToLocator1.build());

        EidToLocatorRecordBuilder eidToLocator2 = new EidToLocatorRecordBuilder();
        eidToLocator2.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.7")));
        eidToLocator2.setRecordTtl(1000000);
        eidToLocator2.setAction(Action.Drop);
        eidToLocator2.setAuthoritative(false);
        eidToLocator2.setMapVersion((short) 29);
        mrBuilder.getEidToLocatorRecord().add(eidToLocator2.build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(12); /* First record */
        assertEquals(7, packet.getInt());
        packet.position(packet.position() + 2); /*
                                                 * skip Locator Count & Mask-len
                                                 */
        assertHexEquals((byte) 0x50, packet.get());
        packet.position(packet.position() + 1); /* skip Reserved byte */
        assertEquals((short) 3072, packet.getShort());

        packet.position(packet.position() + 6); /* Second record */
        assertEquals(1000000, packet.getInt());
        packet.position(packet.position() + 2); /*
                                                 * skip Locator Count & Mask-len
                                                 */
        assertHexEquals((byte) 0x60, packet.get());
        packet.position(packet.position() + 1); /* skip Reserved byte */
        assertEquals((short) 29, packet.getShort());
    }

    @Test
    public void serialize__LocatorRecordFields() throws Exception {
        MapReplyBuilder mrBuilder = new MapReplyBuilder();
        mrBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());

        EidToLocatorRecordBuilder eidToLocatorBuilder = new EidToLocatorRecordBuilder();
        eidToLocatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1")));
        eidToLocatorBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());

        LocatorRecordBuilder locatorBuilder1 = new LocatorRecordBuilder();
        locatorBuilder1.setPriority((short) 0xF3);
        locatorBuilder1.setWeight((short) 0xF6);
        locatorBuilder1.setMulticastPriority((short) 0xA3);
        locatorBuilder1.setMulticastWeight((short) 0x06);
        locatorBuilder1.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.1")));
        locatorBuilder1.setLocalLocator(true);
        locatorBuilder1.setRlocProbed(true);
        locatorBuilder1.setRouted(true);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder1.build());

        LocatorRecordBuilder locatorBuilder2 = new LocatorRecordBuilder();
        locatorBuilder2.setPriority((short) 0x03);
        locatorBuilder2.setWeight((short) 0x06);
        locatorBuilder2.setMulticastPriority((short) 0x03);
        locatorBuilder2.setMulticastWeight((short) 0xF1);
        locatorBuilder2.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.2")));
        locatorBuilder2.setLocalLocator(false);
        locatorBuilder2.setRlocProbed(false);
        locatorBuilder2.setRouted(false);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mrBuilder.getEidToLocatorRecord().add(eidToLocatorBuilder.build());

        ByteBuffer packet = MapReplySerializer.getInstance().serialize(mrBuilder.build());

        packet.position(12 + 16); /* First locator record */
        assertHexEquals((byte) 0xF3, packet.get());
        assertHexEquals((byte) 0xF6, packet.get());
        assertHexEquals((byte) 0xA3, packet.get());
        assertHexEquals((byte) 0x06, packet.get());
        packet.position(packet.position() + 1);
        assertHexEquals((byte) 0x07, packet.get());

        packet.position(packet.position() + 6); /* Second locator record */
        assertHexEquals((byte) 0x03, packet.get());
        assertHexEquals((byte) 0x06, packet.get());
        assertHexEquals((byte) 0x03, packet.get());
        assertHexEquals((byte) 0xF1, packet.get());
        packet.position(packet.position() + 1);
        assertHexEquals((byte) 0x00, packet.get());
    }
}
