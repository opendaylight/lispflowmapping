/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import junitx.framework.ArrayAssert;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.no.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegisterBuilder;

public class MapServerTest extends BaseTestCase {

    private LispMappingService testedMapServer;
    private ILispDAO lispDAO;
    private MapRegisterBuilder mapRegisterBuilder;
    private LispAddressContainer eid;
    private LispAddressContainer rloc;
    private ValueSaverAction<MappingEntry<?>[]> mappingEntriesSaver;
    private MapRegister mapRegisterWithAuthentication;
    private String eidIpv4String = "10.31.0.5";
    private String eidIpv6String = "1:1:1:1:1:1:1:0";

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        lispDAO = context.mock(ILispDAO.class);
        testedMapServer = new LispMappingService();
        testedMapServer.basicInit(lispDAO);
        mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setKeyId((short) 0);
        mapRegisterBuilder.setAuthenticationData(new byte[0]);
        eid = LispAFIConvertor.asIPv4Address(eidIpv4String);
        rloc = LispAFIConvertor.asIPv4Address("192.168.136.10");
        EidToLocatorRecordBuilder recordBuilder = new EidToLocatorRecordBuilder();
        recordBuilder.setLispAddressContainer(eid).setMaskLength((short) 32);
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        recordBuilder.getLocatorRecord().add(new LocatorRecordBuilder().setLispAddressContainer(rloc).build());
        recordBuilder.setAction(Action.NoAction).setMapVersion((short) 0).setAuthoritative(false).setRecordTtl(60).setMaskLength((short) 32);
        mapRegisterBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterWithAuthentication = MapRegisterSerializer.getInstance().deserialize(hexToByteBuffer("38 00 01 01 FF BB "
        //
                + "00 00 00 00 00 00 00 01 00 14 2c 61 b9 c9 9a 20 " //
                + "ba d8 f5 40 d3 55 6f 5f 6e 5a b2 0a bf b5 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        mappingEntriesSaver = new ValueSaverAction<MappingEntry<?>[]>();
    }

    @Test
    public void handleMapRegister__NonSetMBit() throws Exception {
        mapRegisterBuilder.setWantMapNotify(false);

        addDefaultPutAndGetExpectations(eid);
        assertNull(testedMapServer.handleMapRegister(mapRegisterBuilder.build(), false));

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);
        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc, ((EidToLocatorRecord) entries[0].getValue())
                .getLocatorRecord().get(0).getLispAddressContainer());
    }

    @Test
    public void handleMapRegisterIpv4__ValidNotifyEchoesRegister() throws Exception {
        mapRegisterBuilder.getEidToLocatorRecord().add(
                getDefaultEidToLocatorBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(new NoAddressBuilder().build())).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(eid);
        addDefaultPutAndGetExpectations(LispAFIConvertor.toContainer(new NoAddressBuilder().build()));
        MapRegister mr = mapRegisterBuilder.build();
        MapNotify mapNotify = testedMapServer.handleMapRegister(mr, false);
        assertEquals(mr.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__CloneNotOwnYouClown() throws Exception {
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        mapRegisterBuilder.setWantMapNotify(true);
        EidToLocatorRecordBuilder eidToLocatorBuilder = getDefaultEidToLocatorBuilder();
        eidToLocatorBuilder.setMaskLength((short) 32);
        eid = LispAFIConvertor.asIPv4Address("0.0.0.1");
        eidToLocatorBuilder.setLispAddressContainer(eid);

        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.2")));
        locatorBuilder.setPriority((short) 55);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(eidToLocatorBuilder.build());

        addDefaultPutAndGetExpectations(eid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build(), false);

        EidToLocatorRecord actualEidToLocator = mapNotify.getEidToLocatorRecord().get(0);
        assertEquals(eid, actualEidToLocator.getLispAddressContainer());
        assertEquals((byte) 55, actualEidToLocator.getLocatorRecord().get(0).getPriority().byteValue());

    }

    @Test
    public void handleMapRegisterIpv4__ValidMask() throws Exception {
        int mask = 16;
        LispAddressContainer newEid = LispAFIConvertor.asIPv4Prefix(eidIpv4String, mask);
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(newEid).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(rloc);
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(newEid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr, false);
        assertEquals(newEid, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__NonMaskable() throws Exception {
        int mask = 16;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer addr = LispAFIConvertor.toContainer(LispAFIConvertor.asMacAfiAddress("01:01:01:01:01:01"));
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister, false);
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__ZeroMask() throws Exception {
        int mask = 0;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer addr = LispAFIConvertor.toContainer(LispAFIConvertor.asMacAfiAddress("01:01:01:01:01:01"));
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister, false);
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIPv4__ZeroMask() throws Exception {
        int mask = 0;
        LispAddressContainer newEid = LispAFIConvertor.asIPv4Prefix(eidIpv4String, mask);
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(newEid).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(rloc);
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(newEid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr, false);
        assertEquals(newEid, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__ValidMask32() throws Exception {
        int mask = 32;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(eid).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(rloc);
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(eid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr, false);
        assertEquals(eid, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask96() throws Exception {
        int mask = 96;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer addr = LispAFIConvertor.asIPv6Prefix(eidIpv6String, mask);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister, false);
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ZeroMask() throws Exception {
        int mask = 0;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer addr = LispAFIConvertor.asIPv6Prefix(eidIpv6String, mask);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister, false);
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask48() throws Exception {
        int mask = 48;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer addr = LispAFIConvertor.asIPv6Prefix(eidIpv6String, mask);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister, false);
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask128() throws Exception {
        int mask = 128;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer addr = LispAFIConvertor.asIPv6Address(eidIpv6String);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister, false);
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIPV4AndIpv6__ValidMask96() throws Exception {
        int mask = 96;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder recordBuilder0 = getDefaultEidToLocatorBuilder();
        recordBuilder0.setLispAddressContainer(eid).setMaskLength((short) 32);
        LispAddressContainer addr = LispAFIConvertor.asIPv6Prefix(eidIpv6String, mask);
        EidToLocatorRecordBuilder recordBuilder1 = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder1.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder1.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder0.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder1.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(eid);
        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister, false);
        assertEquals(eid, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(1).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__MultipleRLOCs() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer rloc0 = rloc;
        LispAddressContainer rloc1 = LispAFIConvertor.asIPv6Address("0:0:0:0:0:0:0:7");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(eid);
        LocatorRecordBuilder locatorBuilder1 = getDefaultLocatorBuilder();
        locatorBuilder1.setLispAddressContainer(rloc0);
        LocatorRecordBuilder locatorBuilder2 = getDefaultLocatorBuilder();
        locatorBuilder2.setLispAddressContainer(rloc1);
        recordBuilder.getLocatorRecord().add(locatorBuilder1.build());
        recordBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build(), false);

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc0, ((EidToLocatorRecord) entries[0].getValue()).getLocatorRecord()
                .get(0).getLispAddressContainer());
        assertEquals(rloc1, ((EidToLocatorRecord) entries[0].getValue()).getLocatorRecord()
                .get(1).getLispAddressContainer());

    }

    @Test
    public void handleMapRegister__MultipleTypes() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAddressContainer rloc0 = rloc;
        // LispAFIAddress rloc1 =
        // LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:7");
        String subkey = "bla";
        LispAFIAddress rloc1 = LispAFIConvertor
                .asKeyValue(subkey, LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:7")));
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(eid);
        LocatorRecordBuilder locatorBuilder1 = getDefaultLocatorBuilder();
        locatorBuilder1.setLispAddressContainer(rloc0);
        LocatorRecordBuilder locatorBuilder2 = getDefaultLocatorBuilder();
        locatorBuilder2.setLispAddressContainer(LispAFIConvertor.toContainer(rloc1));
        recordBuilder.getLocatorRecord().add(locatorBuilder1.build());
        recordBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build(), false);

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);
        EidToLocatorRecord storedMapping = (EidToLocatorRecord) entries[0].getValue();
        assertEquals(2, storedMapping.getLocatorRecord().size());
        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc0, storedMapping.getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(rloc1), storedMapping.getLocatorRecord().get(1)
                .getLispAddressContainer());

    }

    @Test
    public void handleMapRegister__TestOverwrite() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        addEidToLocatorRecord();

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc, ((EidToLocatorRecord) entries[0].getValue()).getLocatorRecord()
                .get(0).getLispAddressContainer());
    }

//    @Test
//    public void handleMapRegister__TestDontOverwrite() throws Exception {
//        int hc = rloc.getAddress().hashCode();
//
//        addDefaultPutAndGetExpectations(eid);
//        testedMapServer.setOverwrite(false);
//
//        addEidToLocatorRecord();
//
//        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
//        assertEquals(1, entries.length);
//
//        assertEquals(String.valueOf(hc), entries[0].getKey());
//        assertEquals(rloc, ((EidToLocatorRecord) entries[0].getValue()).getLocatorRecord()
//                .get(0).getLispAddressContainer());
//    }

    private void addEidToLocatorRecord() {
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(eid);
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());

        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build(), false);
    }

    @Test
    public void handleMapRegister__MultipleEIDs() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        LispAddressContainer rloc1 = LispAFIConvertor.asIPv6Address("0:0:0:0:0:0:0:7");
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder etlrBuilder = getDefaultEidToLocatorBuilder();
        etlrBuilder.getLocatorRecord().add(getDefaultLocatorBuilder().setLispAddressContainer(rloc1).build());
        mapRegisterBuilder.setWantMapNotify(true);
        etlrBuilder.setLispAddressContainer(eid);

        EidToLocatorRecordBuilder etlr2Builder = getDefaultEidToLocatorBuilder();
        LispAddressContainer address = LispAFIConvertor.asIPv4Address("1.1.1.1");
        etlr2Builder.setLispAddressContainer(address);
        etlr2Builder.setMaskLength((short) 32);
        int recordTtl = 5;
        etlr2Builder.setRecordTtl(recordTtl);
        etlr2Builder.getLocatorRecord().add(
                getDefaultLocatorBuilder().setPriority((short) 10)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("2.2.2.2"))).build());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlr2Builder.build());
        addDefaultPutAndGetExpectations(address);
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build(), false);

        assertEquals(rloc1, mapNotify.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("2.2.2.2")), mapNotify.getEidToLocatorRecord().get(1)
                .getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(address, mapNotify.getEidToLocatorRecord().get(1).getLispAddressContainer());
        assertEquals(recordTtl, mapNotify.getEidToLocatorRecord().get(1).getRecordTtl().intValue());

    }

    @Test
    public void handleMapRegisterIpv4__ChekWrongPassword() throws Exception {

        addGetExpectations(LispAFIConvertor.asIPv4Address("153.16.254.1"), 0, 31, "bla");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication, false);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__ChcekNoPasswordAndThenPassword() throws Exception {

        addGetExpectations(LispAFIConvertor.asIPv4Address("153.16.254.1"), 0, 25, "password");
        addPutExpectations(LispAFIConvertor.asIPv4Address("153.16.254.1"));
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication, false);
        assertEquals(mapRegisterWithAuthentication.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegisterWithAuthentication.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegisterWithAuthentication.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__ChcekNoPassword() throws Exception {

        addGetExpectations(LispAFIConvertor.asIPv4Address("153.16.254.1"), 30, 0, "password");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication, false);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__ChcekNoreturn() throws Exception {

        addGetExpectations(LispAFIConvertor.asIPv4Address("153.16.254.1"));
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication, false);
        assertEquals(mapNotify, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void handleAddAuthenticationKey() throws Exception {
        String password = "pass";
        LispAddressContainer key = getDefaultKey();
        oneOf(lispDAO).put(weq(key),
                weq((MappingEntry<String>[]) (Arrays.asList(new MappingEntry<String>(SubKeys.PASSWORD, password)).toArray())));
        testedMapServer.addAuthenticationKey(eid, password);
    }

    @Test
    public void handleGetAuthenticationKey() throws Exception {
        LispAddressContainer key = getDefaultKey();
        oneOf(lispDAO).getSpecific(weq(key), with(SubKeys.PASSWORD));
        ret("password");
        assertEquals("password", testedMapServer.getAuthenticationKey(eid));
    }

    @Test
    public void handleGetAuthenticationKeyNoIteration() throws Exception {
        testedMapServer.setShouldIterateMask(false);
        LispAddressContainer key = getDefaultKey();
        LispAddressContainer passKey = getKey(30);
        oneOf(lispDAO).getSpecific(weq(key), with(SubKeys.PASSWORD));
        allowing(lispDAO).getSpecific(weq(passKey), with(SubKeys.PASSWORD));
        ret("password");
        assertEquals(null, testedMapServer.getAuthenticationKey(eid));
    }

    @Test
    public void handleRemoveAuthenticationKey() throws Exception {
        LispAddressContainer key = getDefaultKey();
        oneOf(lispDAO).removeSpecific(weq(key), with(SubKeys.PASSWORD));
        testedMapServer.removeAuthenticationKey(eid);
    }

    private void addDefaultPutAndGetExpectations(LispAddressContainer addr) {
        addPutExpectations(addr);
        addGetExpectations(addr);
    }

    private void addGetExpectations(LispAddressContainer address) {
        addGetExpectations(address,  0, 0, SubKeys.PASSWORD);
    }

    private void addPutExpectations(LispAddressContainer address) {
        exactly(2).of(lispDAO).put(weq(address), with(mappingEntriesSaver));
    }

    private void addGetExpectations(LispAddressContainer address, int withoutPassword, int withPassword, String password) {
        if (withoutPassword > 0) {
            String result = null;
            result = null;
            allowing(lispDAO).getSpecific(with(address), with(SubKeys.PASSWORD));
            ret(result);
        }
        if (withPassword > 0) {
            String result = null;
            result = password;
            allowing(lispDAO).getSpecific(with(address), with(SubKeys.PASSWORD));
            ret(result);
        }
        for (int i = MaskUtil.getMaskForAddress(address); i >= 0; i--) {
            allowing(lispDAO).getSpecific(with(MaskUtil.normalize(address, (short) i)), with(SubKeys.PASSWORD));
            ret(null);
        }
    }

    private LispAddressContainer getDefaultKey() {
        return getKey(32);
    }

    private LispAddressContainer getKey(int mask) {
        return MaskUtil.normalize(eid, (short)mask);
    }

    private MapRegisterBuilder getDefaultMapRegisterBuilder() {
        MapRegisterBuilder mrb = new MapRegisterBuilder();
        mrb.setKeyId((short) 0);
        mrb.setNonce((long) 0);
        mrb.setWantMapNotify(false);
        mrb.setProxyMapReply(false);
        mrb.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mrb.setAuthenticationData(new byte[0]);
        return mrb;
    }

    private EidToLocatorRecordBuilder getDefaultEidToLocatorBuilder() {
        EidToLocatorRecordBuilder builder = new EidToLocatorRecordBuilder();
        builder.setAction(Action.NoAction);
        builder.setAuthoritative(false);
        builder.setLocatorRecord(new ArrayList<LocatorRecord>());
        builder.setMapVersion((short) 0);
        builder.setMaskLength((short) 32);
        builder.setRecordTtl(60);
        return builder;
    }

    private LocatorRecordBuilder getDefaultLocatorBuilder() {
        LocatorRecordBuilder builder = new LocatorRecordBuilder();
        builder.setLocalLocator(false);
        builder.setMulticastPriority((short) 0);
        builder.setMulticastWeight((short) 0);
        builder.setPriority((short) 0);
        builder.setRlocProbed(false);
        builder.setRouted(false);
        builder.setWeight((short) 0);
        return builder;
    }
}
