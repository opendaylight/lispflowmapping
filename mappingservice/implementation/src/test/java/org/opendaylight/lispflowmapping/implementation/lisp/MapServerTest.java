/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;
import junitx.framework.ArrayAssert;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO.MappingEntry;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;

public class MapServerTest extends BaseTestCase {

    private MapServer testedMapServer;
    private ILispDAO lispDAO;
    private MapRegister mapRegister;
    private LispIpv4Address eid;
    private LispIpv4Address rloc;
    private ValueSaverAction<MappingEntry<?>[]> mappingEntriesSaver;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        lispDAO = context.mock(ILispDAO.class);
        testedMapServer = new MapServer(lispDAO);
        mapRegister = new MapRegister();
        eid = new LispIpv4Address(0x9910FE01);
        rloc = new LispIpv4Address(0xC0A8880A);
        EidToLocatorRecord record = new EidToLocatorRecord();
        record.setPrefix(eid);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);

        mappingEntriesSaver = new ValueSaverAction<MappingEntry<?>[]>();
    }

    @Test
    public void handleMapRegister__NonSetMBit() throws Exception {
        mapRegister.setWantMapNotify(false);

        expectPut();
        assertNull(testedMapServer.handleMapRegister(mapRegister));

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(2, entries.length);

        assertEquals("NumRLOCs", entries[0].getKey());
        assertEquals(1, entries[0].getValue());
        assertEquals("RLOC0", entries[1].getKey());
        assertEquals(rloc, entries[1].getValue());
    }

    @Test
    public void handleMapRegisterIpv4__ValidNotifyEchoesRegister() throws Exception {
        mapRegister.addEidToLocator(new EidToLocatorRecord().setPrefix(new LispNoAddress()));
        mapRegister.setWantMapNotify(true);

        expectPut();
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__CloneNotOwnYouClown() throws Exception {
        mapRegister = new MapRegister();
        mapRegister.setKeyId((byte)0);
        mapRegister.setWantMapNotify(true);
        EidToLocatorRecord eidToLocator = new EidToLocatorRecord();
        eid = new LispIpv4Address(1);
        eidToLocator.setPrefix(eid);

        LocatorRecord locator = new LocatorRecord();
        locator.setLocator(new LispIpv4Address(2));
        locator.setPriority((byte) 55);
        eidToLocator.addLocator(locator);

        mapRegister.addEidToLocator(eidToLocator);

        expectPut();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);

        mapRegister.getEidToLocatorRecords().get(0).setPrefix(new LispIpv4Address(55));
        mapRegister.getEidToLocatorRecords().get(0).getLocators().get(0).setPriority((byte) 1);

        EidToLocatorRecord actualEidToLocator = mapNotify.getEidToLocatorRecords().get(0);
        assertEquals(new LispIpv4Address(1), actualEidToLocator.getPrefix());
        assertEquals((byte) 55, actualEidToLocator.getLocators().get(0).getPriority());

    }

    @Test
    public void handleMapRegister__MultipleRLOCs() throws Exception {
        expectPut();

        LispIpv4Address rloc0 = rloc;
        LispIpv6Address rloc1 = new LispIpv6Address("::7");
        mapRegister.getEidToLocatorRecords().get(0).addLocator(new LocatorRecord().setLocator(rloc1));

        testedMapServer.handleMapRegister(mapRegister);

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(3, entries.length);

        assertEquals("NumRLOCs", entries[0].getKey());
        assertEquals(2, entries[0].getValue());
        assertEquals("RLOC0", entries[1].getKey());
        assertEquals(rloc0, entries[1].getValue());
        assertEquals("RLOC1", entries[2].getKey());
        assertEquals(rloc1, entries[2].getValue());

        // When refactoring: reconsider arrays/count
    }

    private void expectPut() {
        oneOf(lispDAO).put(weq(eid), with(mappingEntriesSaver));
    }
}
