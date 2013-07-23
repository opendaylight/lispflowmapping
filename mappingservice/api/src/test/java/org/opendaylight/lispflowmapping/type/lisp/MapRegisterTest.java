/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;

public class MapRegisterTest extends BaseTestCase {

    @Test
    public void deserialize__AllFields() throws Exception {
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0001
        // AuthDataLength: 20 Data:
        // e8:f5:0b:c5:c5:f2:b0:21:27:a8:21:41:04:f3:46:5a:a5:68:89:ec
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapRegister mr = MapRegister.deserialize(hexToByteBuffer("38 00 01 01 FF BB " //
                + "00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 " //
                + "b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertTrue(mr.isProxyMapReply());
        assertTrue(mr.isWantMapNotify());

        assertEquals(1, mr.getEidToLocatorRecords().size());
        assertEquals(0xFFBB000000000000L, mr.getNonce());
        assertEquals(0x0001, mr.getKeyId());
        byte[] expectedAuthenticationData = { (byte) 0xe8, (byte) 0xf5, (byte) 0x0b, (byte) 0xc5, (byte) 0xc5, (byte) 0xf2, (byte) 0xb0, (byte) 0x21, //
                (byte) 0x27, (byte) 0xa8, (byte) 0x21, (byte) 0x41, (byte) 0x04, (byte) 0xf3, (byte) 0x46, (byte) 0x5a, //
                (byte) 0xa5, (byte) 0x68, (byte) 0x89, (byte) 0xec };
        ArrayAssert.assertEquals(expectedAuthenticationData, mr.getAuthenticationData());
    }

    @Test
    public void deserialize__MultipleRecords() throws Exception {
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 4
        // EID prefixes: 153.16.254.1 -- 152.16.254.1 -- 151.16.254.1 --
        // 150.16.254.1
        // Local RLOCs: 192.168.136.10 -- 192.168.136.11 -- 192.168.136.12 --
        // 192.168.136.13
        //
        MapRegister mr = MapRegister.deserialize(hexToByteBuffer("38 00 01 " //
                + "04 " // Record count
                + "FF BB 00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 " //
                + "c5 f2 b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec " //
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

        assertEquals(4, mr.getEidToLocatorRecords().size());
        assertEquals(new LispIpv4Address("153.16.254.1"), mr.getEidToLocatorRecords().get(0).getPrefix());
        assertEquals(new LispIpv4Address("151.16.254.1"), mr.getEidToLocatorRecords().get(2).getPrefix());
        assertEquals(new LispIpv4Address("192.168.136.11"), mr.getEidToLocatorRecords().get(1).getLocators().get(0).getLocator());
        assertEquals(new LispIpv4Address("192.168.136.13"), mr.getEidToLocatorRecords().get(3).getLocators().get(0).getLocator());
    }

    @Test
    public void deserialize__Locators() throws Exception {
        MapRegister mr = MapRegister.deserialize(hexToByteBuffer("38 00 01 01 " //
                + "FF BB 00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 " //
                + "c5 f2 b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec " //
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

        assertEquals(1, mr.getEidToLocatorRecords().size());
        EidToLocatorRecord eidToLocator = mr.getEidToLocatorRecords().get(0);
        assertEquals(3, eidToLocator.getLocators().size());
        LocatorRecord loc0 = eidToLocator.getLocators().get(0);
        LocatorRecord loc1 = eidToLocator.getLocators().get(1);
        LocatorRecord loc2 = eidToLocator.getLocators().get(2);
        assertEquals((byte)0x01, loc0.getPriority());
        assertEquals((byte)0x67, loc1.getPriority());
        assertEquals((byte)0x60, loc2.getPriority());

        assertEquals((byte)0x64, loc0.getWeight());
        assertEquals((byte)0x00, loc1.getWeight());
        assertEquals((byte)0x11, loc2.getWeight());

        assertEquals((byte)0x1F, loc0.getMulticastPriority());
        assertEquals((byte)0x30, loc1.getMulticastPriority());
        assertEquals((byte)0x34, loc2.getMulticastPriority());

        assertEquals((byte)0x00, loc0.getMulticastWeight());
        assertEquals((byte)0x34, loc1.getMulticastWeight());
        assertEquals((byte)0xA4, loc2.getMulticastWeight());

        assertTrue(loc0.isLocalLocator());
        assertFalse(loc1.isLocalLocator());
        assertFalse(loc2.isLocalLocator());
        
        assertFalse(loc0.isRlocProbed());
        assertTrue(loc1.isRlocProbed());
        assertTrue(loc2.isRlocProbed());
        
        assertTrue(loc0.isRouted());
        assertFalse(loc1.isRouted());
        assertTrue(loc2.isRouted());
        
        assertEquals(new LispIpv4Address(0xC0A8880A), loc0.getLocator());
        assertEquals(new LispIpv4Address(0xCCAAAA11), loc1.getLocator());
    }

    @Test
    public void deserialize__SomeEidToLocatorFiels() throws Exception {
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 4
        // EID prefixes: 153.16.254.1 -- 152.16.254.1 -- 151.16.254.1 --
        // 150.16.254.1
        // Local RLOCs: 192.168.136.10 -- 192.168.136.11 -- 192.168.136.12 --
        // 192.168.136.13
        //
        MapRegister mr = MapRegister.deserialize(hexToByteBuffer("38 00 01 " //
                + "04 " // Record count
                + "FF BB 00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 " //
                + "c5 f2 b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec " //
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

        assertEquals(4, mr.getEidToLocatorRecords().size());

        EidToLocatorRecord record0 = mr.getEidToLocatorRecords().get(0);
        EidToLocatorRecord record1 = mr.getEidToLocatorRecords().get(1);
        EidToLocatorRecord record2 = mr.getEidToLocatorRecords().get(2);
        EidToLocatorRecord record3 = mr.getEidToLocatorRecords().get(3);

        assertEquals(10, record0.getRecordTtl());
        assertEquals(13, record3.getRecordTtl());

        assertEquals(32, record0.getMaskLength());
        assertEquals(23, record1.getMaskLength());

        assertEquals(MapReplyAction.NoAction, record0.getAction());
        assertEquals(MapReplyAction.SendMapRequest, record1.getAction());
        assertEquals(MapReplyAction.NoAction, record2.getAction());
        assertEquals(MapReplyAction.NativelyForward, record3.getAction());

        assertTrue(record0.isAuthoritative());
        assertTrue(record1.isAuthoritative());
        assertFalse(record2.isAuthoritative());
        assertFalse(record3.isAuthoritative());

        assertEquals(0x000, record0.getMapVersion());
        assertEquals(0x111, record1.getMapVersion());
        assertEquals(0x222, record2.getMapVersion());
        assertEquals(0x333, record3.getMapVersion());
    }

    @Test
    public void deserialize__IllegalAction() throws Exception {
        MapRegister mr = MapRegister.deserialize(hexToByteBuffer("38 00 01 01 FF BB " //
                + "00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 " //
                + "b0 21 27 a8 21 41 04 f3 46 5a a5 68 89 ec 00 00 " //
                + "00 0a 01 20 F0 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertEquals(1, mr.getEidToLocatorRecords().size());
        assertEquals(MapReplyAction.NoAction, mr.getEidToLocatorRecords().get(0).getAction());
    }

}
