/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is availabl at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp.serializer;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import junitx.framework.ArrayAssert;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSerializer;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapReplyAction;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;

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
        MapReply mr = new MapReply();
        mr.addEidToLocator(new EidToLocatorRecord().setPrefix(new LispIpv4Address(1)));
        mr.setProbe(true);
        mr.setEchoNonceEnabled(false);

        ByteBuffer packet = LispSerializer.serializeMapReply(mr);
        byte firstByte = packet.get(0);
        assertHexEquals((byte) 0x28, firstByte);

        mr.setProbe(false);
        mr.setEchoNonceEnabled(true);

        packet = LispSerializer.serializeMapReply(mr);
        firstByte = packet.get(0);
        assertHexEquals((byte) 0x24, firstByte);
    }

    @Test
    public void serialize__MultipleRecordsWithoutRLOCs() throws Exception {
        MapReply mr = new MapReply();
        mr.addEidToLocator(new EidToLocatorRecord().setPrefix(new LispIpv6Address("::8")));
        mr.addEidToLocator(new EidToLocatorRecord().setPrefix(new LispIpv4Address(0x08020405)));

        ByteBuffer packet = LispSerializer.serializeMapReply(mr);
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
    public void serialize__EidRecordDefaultAction() throws Exception {
        MapReply mr = new MapReply();
        EidToLocatorRecord eidToLocator = new EidToLocatorRecord();
        eidToLocator.setPrefix(new LispIpv4Address(1));
        mr.addEidToLocator(eidToLocator);

        ByteBuffer packet = LispSerializer.serializeMapReply(mr);

        packet.position(18);
        assertHexEquals((byte) 0x00, packet.get()); // MapReplyAction.NoAction
    }

    @Test
    public void serialize__EidRecordNullActionShouldTranslateToDefault() throws Exception {
        MapReply mr = new MapReply();
        EidToLocatorRecord eidToLocator = new EidToLocatorRecord();
        eidToLocator.setPrefix(new LispIpv4Address(1));
        eidToLocator.setAction(null);
        mr.addEidToLocator(eidToLocator);

        ByteBuffer packet = LispSerializer.serializeMapReply(mr);

        packet.position(18);
        assertHexEquals((byte) 0x00, packet.get()); // MapReplyAction.NoAction
    }

    @Test
    public void serialize__EidRecordFields() throws Exception {
        MapReply mr = new MapReply();

        EidToLocatorRecord eidToLocator1 = new EidToLocatorRecord();
        eidToLocator1.setPrefix(new LispIpv4Address(1));
        eidToLocator1.setRecordTtl(7);
        eidToLocator1.setAction(MapReplyAction.SendMapRequest);
        eidToLocator1.setAuthoritative(true);
        eidToLocator1.setMapVersion((short) 3072);
        mr.addEidToLocator(eidToLocator1);

        EidToLocatorRecord eidToLocator2 = new EidToLocatorRecord();
        eidToLocator2.setPrefix(new LispIpv4Address(7));
        eidToLocator2.setRecordTtl(1000000);
        eidToLocator2.setAction(MapReplyAction.Drop);
        eidToLocator2.setAuthoritative(false);
        eidToLocator2.setMapVersion((short) 29);
        mr.addEidToLocator(eidToLocator2);

        ByteBuffer packet = LispSerializer.serializeMapReply(mr);

        packet.position(12); /* First record */
        assertEquals(7, packet.getInt());
        packet.position(packet.position() + 2); /* skip Locator Count & Mask-len */
        assertHexEquals((byte) 0x50, packet.get());
        packet.position(packet.position() + 1); /* skip Reserved byte */
        assertEquals((short) 3072, packet.getShort());

        packet.position(packet.position() + 6); /* Second record */
        assertEquals(1000000, packet.getInt());
        packet.position(packet.position() + 2); /* skip Locator Count & Mask-len */
        assertHexEquals((byte) 0x60, packet.get());
        packet.position(packet.position() + 1); /* skip Reserved byte */
        assertEquals((short) 29, packet.getShort());
    }

    @Test
    public void serialize__LocatorRecordFields() throws Exception {
        MapReply mr = new MapReply();

        EidToLocatorRecord eidToLocator = new EidToLocatorRecord();
        eidToLocator.setPrefix(new LispIpv4Address(1));

        LocatorRecord locator1 = new LocatorRecord();
        locator1.setPriority((byte) 0xF3);
        locator1.setWeight((byte) 0xF6);
        locator1.setMulticastPriority((byte) 0xA3);
        locator1.setMulticastWeight((byte) 0x06);
        locator1.setLocator(new LispIpv4Address(1));
        locator1.setLocalLocator(true);
        locator1.setRlocProbed(true);
        locator1.setRouted(true);
        eidToLocator.addLocator(locator1);

        LocatorRecord locator2 = new LocatorRecord();
        locator2.setPriority((byte) 0x03);
        locator2.setWeight((byte) 0x06);
        locator2.setMulticastPriority((byte) 0x03);
        locator2.setMulticastWeight((byte) 0xF1);
        locator2.setLocator(new LispIpv4Address(2));
        locator2.setLocalLocator(false);
        locator2.setRlocProbed(false);
        locator2.setRouted(false);
        eidToLocator.addLocator(locator2);

        mr.addEidToLocator(eidToLocator);

        ByteBuffer packet = LispSerializer.serializeMapReply(mr);

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
