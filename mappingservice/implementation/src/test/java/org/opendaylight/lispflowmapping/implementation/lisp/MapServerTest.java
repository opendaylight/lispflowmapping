/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import junitx.framework.ArrayAssert;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;

public class MapServerTest extends BaseTestCase {

    private MapServer testedMapServer;
    private ILispDAO lispDAO;
    private MapRegister mapRegister;
    private LispIpv4Address eid;
    private LispIpv4Address rloc;
    private ValueSaverAction<MappingEntry<?>[]> mappingEntriesSaver;
    private MapRegister mapRegisterWithAuthentication;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        lispDAO = context.mock(ILispDAO.class);
        testedMapServer = new MapServer(lispDAO);
        mapRegister = new MapRegister();
        eid = new LispIpv4Address("10.31.0.5");
        rloc = new LispIpv4Address(0xC0A8880A);
        EidToLocatorRecord record = new EidToLocatorRecord();
        record.setPrefix(eid).setMaskLength(32);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);
        mapRegisterWithAuthentication = MapRegisterSerializer.getInstance().deserialize(hexToByteBuffer("38 00 01 01 FF BB " //
                + "00 00 00 00 00 00 00 01 00 14 2c 61 b9 c9 9a 20 " //
                + "ba d8 f5 40 d3 55 6f 5f 6e 5a b2 0a bf b5 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        mappingEntriesSaver = new ValueSaverAction<MappingEntry<?>[]>();
    }

    @Test
    public void handleMapRegister__NonSetMBit() throws Exception {
        mapRegister.setWantMapNotify(false);

        addDAOExpectations(eid, 32);
        assertNull(testedMapServer.handleMapRegister(mapRegister));

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals("value", entries[0].getKey());
        assertEquals(rloc, ((MappingServiceValue) entries[0].getValue()).getRlocs().get(0).getRecord().getLocator());
    }

    @Test
    public void handleMapRegisterIpv4__ValidNotifyEchoesRegister() throws Exception {
        mapRegister.addEidToLocator(new EidToLocatorRecord().setPrefix(new LispNoAddress()));
        mapRegister.setWantMapNotify(true);

        addDAOExpectations(eid, 32);
        addDAOExpectations(new LispNoAddress(), 32);
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__CloneNotOwnYouClown() throws Exception {
        mapRegister = new MapRegister();
        mapRegister.setKeyId((byte) 0);
        mapRegister.setWantMapNotify(true);
        EidToLocatorRecord eidToLocator = new EidToLocatorRecord();
        eid = new LispIpv4Address(1);
        eidToLocator.setPrefix(eid);

        LocatorRecord locator = new LocatorRecord();
        locator.setLocator(new LispIpv4Address(2));
        locator.setPriority((byte) 55);
        eidToLocator.addLocator(locator);

        mapRegister.addEidToLocator(eidToLocator);

        addDAOExpectations(eid, 0);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);

        mapRegister.getEidToLocatorRecords().get(0).setPrefix(new LispIpv4Address(55));
        mapRegister.getEidToLocatorRecords().get(0).getLocators().get(0).setPriority((byte) 1);

        EidToLocatorRecord actualEidToLocator = mapNotify.getEidToLocatorRecords().get(0);
        assertEquals(new LispIpv4Address(1), actualEidToLocator.getPrefix());
        assertEquals((byte) 55, actualEidToLocator.getLocators().get(0).getPriority());

    }

    @Test
    public void handleMapRegisterIpv4__ValidMask() throws Exception {
        int mask = 16;
        mapRegister = new MapRegister();
        EidToLocatorRecord record = new EidToLocatorRecord();
        record.setPrefix(eid).setMaskLength(mask);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);
        mapRegister.setWantMapNotify(true);

        addDAOExpectations(eid, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(new LispIpv4Address("10.31.0.0"), mapNotify.getEidToLocatorRecords().get(0).getPrefix());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__ValidMask32() throws Exception {
        int mask = 32;
        mapRegister = new MapRegister();
        EidToLocatorRecord record = new EidToLocatorRecord();
        record.setPrefix(new LispIpv4Address("10.31.0.5")).setMaskLength(mask);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);
        mapRegister.setWantMapNotify(true);

        addDAOExpectations(eid, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(new LispIpv4Address("10.31.0.5"), mapNotify.getEidToLocatorRecords().get(0).getPrefix());
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask96() throws Exception {
        int mask = 96;
        mapRegister = new MapRegister();
        EidToLocatorRecord record = new EidToLocatorRecord();
        LispIpv6Address addr = new LispIpv6Address("1:1:1:1:1:1:1:0");
        record.setPrefix(addr).setMaskLength(mask);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);
        mapRegister.setWantMapNotify(true);

        addDAOExpectations(addr, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(new LispIpv6Address("1:1:1:1:1:1:0:0"), mapNotify.getEidToLocatorRecords().get(0).getPrefix());
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIPV4AndIpv6__ValidMask96() throws Exception {
        int mask = 96;
        EidToLocatorRecord record = new EidToLocatorRecord();
        LispIpv6Address addr = new LispIpv6Address("1:1:1:1:1:1:1:0");
        record.setPrefix(addr).setMaskLength(mask);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);
        mapRegister.setWantMapNotify(true);

        addDAOExpectations(eid, 32);
        addDAOExpectations(addr, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(new LispIpv6Address("1:1:1:1:1:1:0:0"), mapNotify.getEidToLocatorRecords().get(1).getPrefix());
        assertEquals(eid, mapNotify.getEidToLocatorRecords().get(0).getPrefix());
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask32() throws Exception {
        int mask = 48;
        mapRegister = new MapRegister();
        EidToLocatorRecord record = new EidToLocatorRecord();
        LispIpv6Address addr = new LispIpv6Address("1:1:1:1:0:0:0:0");
        record.setPrefix(addr).setMaskLength(mask);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);
        mapRegister.setWantMapNotify(true);

        addDAOExpectations(addr, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(new LispIpv6Address("1:1:1:0:0:0:0:0"), mapNotify.getEidToLocatorRecords().get(0).getPrefix());
        assertEquals(mask, mapNotify.getEidToLocatorRecords().get(0).getMaskLength());
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask128() throws Exception {
        int mask = 128;
        mapRegister = new MapRegister();
        EidToLocatorRecord record = new EidToLocatorRecord();
        LispIpv6Address addr = new LispIpv6Address("1:1:1:1:1:1:1:2");
        record.setPrefix(addr).setMaskLength(mask);
        record.addLocator(new LocatorRecord().setLocator(rloc));
        mapRegister.addEidToLocator(record);
        mapRegister.setWantMapNotify(true);

        addDAOExpectations(addr, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(new LispIpv6Address("1:1:1:1:1:1:1:2"), mapNotify.getEidToLocatorRecords().get(0).getPrefix());
        assertEquals(mapRegister.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__MultipleRLOCs() throws Exception {
        addDAOExpectations(eid, 32);

        LispIpv4Address rloc0 = rloc;
        LispIpv6Address rloc1 = new LispIpv6Address("::7");
        mapRegister.getEidToLocatorRecords().get(0).addLocator(new LocatorRecord().setLocator(rloc1));

        testedMapServer.handleMapRegister(mapRegister);

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals("value", entries[0].getKey());
        assertEquals(rloc0, ((MappingServiceValue) entries[0].getValue()).getRlocs().get(0).getRecord().getLocator());
        assertEquals(rloc1, ((MappingServiceValue) entries[0].getValue()).getRlocs().get(1).getRecord().getLocator());

    }

    @Test
    public void handleMapRegister__MultipleEIDs() throws Exception {
        addDAOExpectations(eid, 32);

        LispIpv4Address rloc0 = rloc;
        LispIpv6Address rloc1 = new LispIpv6Address("::7");
        mapRegister.getEidToLocatorRecords().get(0).addLocator(new LocatorRecord().setLocator(rloc1));
        mapRegister.setWantMapNotify(true);

        EidToLocatorRecord etlr = new EidToLocatorRecord();
        LispIpv4Address address = new LispIpv4Address("1.1.1.1");
        etlr.setPrefix(address);
        int recordTtl = 5;
        etlr.setRecordTtl(recordTtl);
        etlr.addLocator(new LocatorRecord().setLocator(new LispIpv4Address("2.2.2.2")).setPriority((byte) 10));
        mapRegister.addEidToLocator(etlr);
        addDAOExpectations(address, 32);
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);

        assertEquals(rloc0, mapNotify.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator());
        assertEquals(rloc1, mapNotify.getEidToLocatorRecords().get(0).getLocators().get(1).getLocator());
        assertEquals(new LispIpv4Address("2.2.2.2"), mapNotify.getEidToLocatorRecords().get(1).getLocators().get(0).getLocator());
        assertEquals(recordTtl, mapNotify.getEidToLocatorRecords().get(1).getRecordTtl());

    }

    @Test
    public void handleMapRegisterIpv4__ChekWrongPassword() throws Exception {

        addDAOEpectations(new LispIpv4Address("153.16.254.1"), 32, 0, 31, "bla");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__ChcekNoPasswordAndThenPassword() throws Exception {

        addDAOEpectations(new LispIpv4Address("153.16.254.1"), 32, 30, 25, "password");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(mapRegisterWithAuthentication.getEidToLocatorRecords(), mapNotify.getEidToLocatorRecords());
        assertEquals(mapRegisterWithAuthentication.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegisterWithAuthentication.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__ChcekNoPassword() throws Exception {

        addDAOEpectations(new LispIpv4Address("153.16.254.1"), 32, 30, 0, "password");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__ChcekNoreturn() throws Exception {

        addDAOExpectations(new LispIpv4Address("153.16.254.1"), 32);
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(mapNotify, null);
    }

    @Test
    public void handleAddAuthenticationKey() throws Exception {
        IMappingServiceKey key = getDefualtKey();
        MappingServiceValue value = getDefualtValue();
        MappingEntry<MappingServiceValue> mappingEntry = new MappingEntry<MappingServiceValue>("value", value);
        oneOf(lispDAO).get(weq(key));
        oneOf(lispDAO).put(weq(key), weq(convertToArray(mappingEntry)));
        assertEquals(true, testedMapServer.addAuthenticationKey(eid, key.getMask(), value.getKey()));
    }

    @Test
    public void handleGetAuthenticationKey() throws Exception {
        IMappingServiceKey key = getDefualtKey();
        MappingServiceValue value = getDefualtValue();
        Map<String, MappingServiceValue> map = new HashMap<String, MappingServiceValue>();
        map.put("value", value);
        atLeast(1).of(lispDAO).get(weq(key));
        ret(map);
        assertEquals(value.getKey(), testedMapServer.getAuthenticationKey(eid, key.getMask()));
    }

    @Test
    public void handleGetAuthenticationKeyNoIteration() throws Exception {
        testedMapServer.setShouldIterateMask(false);
        IMappingServiceKey key = getDefualtKey();
        IMappingServiceKey passKey = getKey(30);
        MappingServiceValue value = getDefualtValue();
        Map<String, MappingServiceValue> map = new HashMap<String, MappingServiceValue>();
        map.put("value", value);
        oneOf(lispDAO).get(weq(key));
        allowing(lispDAO).get(weq(passKey));
        ret(map);
        assertEquals(null, testedMapServer.getAuthenticationKey(eid, key.getMask()));
    }

    @Test
    public void handleRemoveAuthenticationKey() throws Exception {
        IMappingServiceKey key = getDefualtKey();
        MappingServiceValue value = new MappingServiceValue();
        Map<String, MappingServiceValue> map = new HashMap<String, MappingServiceValue>();
        map.put("value", value);
        oneOf(lispDAO).get(weq(key));
        ret(map);
        oneOf(lispDAO).remove(weq(key));
        assertEquals(true, testedMapServer.removeAuthenticationKey(eid, key.getMask()));
    }

    @Test
    public void handleRemoveAuthenticationKeyWhereKeyDosntExist() throws Exception {
        IMappingServiceKey key = getDefualtKey();
        MappingServiceValue value = new MappingServiceValue();
        Map<String, MappingServiceValue> map = new HashMap<String, MappingServiceValue>();
        map.put("value", value);
        oneOf(lispDAO).get(weq(key));
        assertEquals(false, testedMapServer.removeAuthenticationKey(eid, key.getMask()));
    }

    @SuppressWarnings("rawtypes")
    private MappingEntry[] convertToArray(MappingEntry<MappingServiceValue> entry) {
        MappingEntry[] arr = new MappingEntry[1];
        arr[0] = entry;
        return arr;
    }

    private void addDAOExpectations(LispAddress address, int mask) {
        addDAOEpectations(address, mask, 0, 0, "password");
    }

    private void addDAOEpectations(LispAddress address, int mask, int withoutPassword, int withPassword, String password) {
        LispAddress clonedAddress = address;
        addPasswordExpextations(address, mask, withoutPassword, withPassword, password);
        if (address instanceof IMaskable) {
            clonedAddress = (LispAddress) ((IMaskable) address).clone();
        }
        allowing(lispDAO).put(weq(MappingServiceKeyUtil.generateMappingServiceKey(clonedAddress, (byte) mask)), with(mappingEntriesSaver));
    }

    private void addPasswordExpextations(LispAddress address, int mask, int withoutPassword, int withPassword, String password) {
        LispAddress clonedAddress = address;
        if (address instanceof IMaskable) {
            clonedAddress = (LispAddress) ((IMaskable) address).clone();
        }

        if (withoutPassword > 0) {
            Map<String, MappingServiceValue> result = new HashMap<String, MappingServiceValue>();
            result.put("value", new MappingServiceValue());
            allowing(lispDAO)
                    .getSpecific(with(MappingServiceKeyUtil.generateMappingServiceKey(clonedAddress, (byte) withoutPassword)), with("value"));
            ret(result);
        }
        if (address instanceof IMaskable) {
            clonedAddress = (LispAddress) ((IMaskable) address).clone();
        }
        if (withPassword > 0) {
            Map<String, MappingServiceValue> result = new HashMap<String, MappingServiceValue>();
            result.put("value", new MappingServiceValue());
            result.get("value").setKey(password);
            allowing(lispDAO).get(with(MappingServiceKeyUtil.generateMappingServiceKey(clonedAddress, (byte) withPassword)));
            ret(result);
        }
        for (int i = mask; i >= 0; i--) {
            if (address instanceof IMaskable) {
                clonedAddress = (LispAddress) ((IMaskable) address).clone();
            }
            allowing(lispDAO).get(with(MappingServiceKeyUtil.generateMappingServiceKey(clonedAddress, (byte) i)));
            ret(new HashMap<String, MappingServiceValue>());
        }
    }

    private IMappingServiceKey getDefualtKey() {
        return getKey(32);
    }

    private IMappingServiceKey getKey(int mask) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid, mask);
        return key;
    }

    private MappingServiceValue getDefualtValue() {
        MappingServiceValue value = new MappingServiceValue();
        String password = "pass";
        value.setKey(password);
        return value;
    }
}
