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

import junitx.framework.ArrayAssert;

import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispaddress.lispaddresscontainer.address.no.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;

public class MapServerTest extends BaseTestCase {

    private LispMappingService testedMapServer;
    private ILispDAO dao;
    private MappingService mapService;
    private DataStoreBackEnd dsbe;
    private MappingSystem mapSystem;

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
        dao = context.mock(ILispDAO.class);
        dsbe = context.mock(DataStoreBackEnd.class);

        // map-caches init and table creation
        allowing(dao).putTable(with(MappingOrigin.Northbound.toString()));will(returnValue(dao));
        allowing(dao).putTable(with(MappingOrigin.Southbound.toString()));will(returnValue(dao));

        mapSystem = new MappingSystem(dao, true, true, true);

        mapService = new MappingService();
        mapService.setDaoService(dao);
        inject(mapService, "dsbe", dsbe);
        inject(mapService, "mappingSystem", mapSystem);

        testedMapServer = new LispMappingService();
        testedMapServer.setMappingService(mapService);
        testedMapServer.setShouldUseSmr(false);
        testedMapServer.basicInit();

        mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setKeyId((short) 0);
        mapRegisterBuilder.setAuthenticationData(new byte[0]);
        eid = LispAddressUtil.asIPv4Address(eidIpv4String);
        rloc = LispAddressUtil.asIPv4Address("192.168.136.10");
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
        assertNull(testedMapServer.handleMapRegister(mapRegisterBuilder.build()));

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);
        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc, ((EidToLocatorRecord) entries[0].getValue())
                .getLocatorRecord().get(0).getLispAddressContainer());
    }

    @Test
    public void handleMapRegisterIpv4__ValidNotifyEchoesRegister() throws Exception {
        mapRegisterBuilder.getEidToLocatorRecord().add(
                getDefaultEidToLocatorBuilder().setLispAddressContainer(LispAddressUtil.toContainer(new NoAddressBuilder().build())).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(eid);
        addDefaultPutAndGetExpectations(LispAddressUtil.toContainer(new NoAddressBuilder().build()));
        MapRegister mr = mapRegisterBuilder.build();
        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
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
        eid = LispAddressUtil.asIPv4Address("0.0.0.1");
        eidToLocatorBuilder.setLispAddressContainer(eid);

        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAddressUtil.toContainer(LispAddressUtil.asIPAfiAddress("0.0.0.2")));
        locatorBuilder.setPriority((short) 55);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(eidToLocatorBuilder.build());

        addDefaultPutAndGetExpectations(eid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        EidToLocatorRecord actualEidToLocator = mapNotify.getEidToLocatorRecord().get(0);
        assertEquals(eid, actualEidToLocator.getLispAddressContainer());
        assertEquals((byte) 55, actualEidToLocator.getLocatorRecord().get(0).getPriority().byteValue());

    }

    @Test
    public void handleMapRegisterIpv4__ValidMask() throws Exception {
        int mask = 16;
        LispAddressContainer newEid = LispAddressUtil.asIPv4Prefix(eidIpv4String, mask);
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

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
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
        LispAddressContainer addr = LispAddressUtil.toContainer(LispAddressUtil.asMacAfiAddress("01:01:01:01:01:01"));
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
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
        LispAddressContainer addr = LispAddressUtil.toContainer(LispAddressUtil.asMacAfiAddress("01:01:01:01:01:01"));
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(addr, mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIPv4__ZeroMask() throws Exception {
        int mask = 0;
        LispAddressContainer newEid = LispAddressUtil.asIPv4Prefix(eidIpv4String, mask);
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

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
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

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
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
        LispAddressContainer addr = LispAddressUtil.asIPv6Prefix(eidIpv6String, mask);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
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
        LispAddressContainer addr = LispAddressUtil.asIPv6Prefix(eidIpv6String, mask);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
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
        LispAddressContainer addr = LispAddressUtil.asIPv6Prefix(eidIpv6String, mask);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
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
        LispAddressContainer addr = LispAddressUtil.asIPv6Address(eidIpv6String);
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(rloc);
        recordBuilder.setLispAddressContainer(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
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
        LispAddressContainer addr = LispAddressUtil.asIPv6Prefix(eidIpv6String, mask);
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

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
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
        LispAddressContainer rloc1 = LispAddressUtil.asIPv6Address("0:0:0:0:0:0:0:7");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(eid);
        LocatorRecordBuilder locatorBuilder1 = getDefaultLocatorBuilder();
        locatorBuilder1.setLispAddressContainer(rloc0);
        LocatorRecordBuilder locatorBuilder2 = getDefaultLocatorBuilder();
        locatorBuilder2.setLispAddressContainer(rloc1);
        recordBuilder.getLocatorRecord().add(locatorBuilder1.build());
        recordBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build());

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
        // LispAddressUtil.asIPv6AfiAddress("0:0:0:0:0:0:0:7");
        String subkey = "bla";
        LispAFIAddress rloc1 = LispAddressUtil
                .asKeyValue(subkey, LispAddressUtil.toPrimitive(LispAddressUtil.asIPv6AfiAddress("0:0:0:0:0:0:0:7")));
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(eid);
        LocatorRecordBuilder locatorBuilder1 = getDefaultLocatorBuilder();
        locatorBuilder1.setLispAddressContainer(rloc0);
        LocatorRecordBuilder locatorBuilder2 = getDefaultLocatorBuilder();
        locatorBuilder2.setLispAddressContainer(LispAddressUtil.toContainer(rloc1));
        recordBuilder.getLocatorRecord().add(locatorBuilder1.build());
        recordBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);
        EidToLocatorRecord storedMapping = (EidToLocatorRecord) entries[0].getValue();
        assertEquals(2, storedMapping.getLocatorRecord().size());
        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc0, storedMapping.getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(LispAddressUtil.toContainer(rloc1), storedMapping.getLocatorRecord().get(1)
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

        testedMapServer.handleMapRegister(mapRegisterBuilder.build());
    }

    @Test
    public void handleMapRegister__MultipleEIDs() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        LispAddressContainer rloc1 = LispAddressUtil.asIPv6Address("0:0:0:0:0:0:0:7");
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder etlrBuilder = getDefaultEidToLocatorBuilder();
        etlrBuilder.getLocatorRecord().add(getDefaultLocatorBuilder().setLispAddressContainer(rloc1).build());
        mapRegisterBuilder.setWantMapNotify(true);
        etlrBuilder.setLispAddressContainer(eid);

        EidToLocatorRecordBuilder etlr2Builder = getDefaultEidToLocatorBuilder();
        LispAddressContainer address = LispAddressUtil.asIPv4Address("1.1.1.1");
        etlr2Builder.setLispAddressContainer(address);
        etlr2Builder.setMaskLength((short) 32);
        int recordTtl = 5;
        etlr2Builder.setRecordTtl(recordTtl);
        etlr2Builder.getLocatorRecord().add(
                getDefaultLocatorBuilder().setPriority((short) 10)
                        .setLispAddressContainer(LispAddressUtil.toContainer(LispAddressUtil.asIPAfiAddress("2.2.2.2"))).build());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlr2Builder.build());
        addDefaultPutAndGetExpectations(address);
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        assertEquals(rloc1, mapNotify.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());
        assertEquals(LispAddressUtil.toContainer(LispAddressUtil.asIPAfiAddress("2.2.2.2")), mapNotify.getEidToLocatorRecord().get(1)
                .getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(address, mapNotify.getEidToLocatorRecord().get(1).getLispAddressContainer());
        assertEquals(recordTtl, mapNotify.getEidToLocatorRecord().get(1).getRecordTtl().intValue());

    }

    @Test
    public void handleMapRegisterIpv4__CheckWrongPassword() throws Exception {

        addGetExpectations(LispAddressUtil.asIPv4Address("153.16.254.1"), 0, 31, "bla");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__CheckNoPasswordAndThenPassword() throws Exception {

        addGetExpectations(LispAddressUtil.asIPv4Address("153.16.254.1"), 0, 25, "password");
        addPutExpectations(LispAddressUtil.asIPv4Address("153.16.254.1"));
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(mapRegisterWithAuthentication.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegisterWithAuthentication.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegisterWithAuthentication.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__CheckNoPassword() throws Exception {

        addGetExpectations(LispAddressUtil.asIPv4Address("153.16.254.1"), 30, 0, "password");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__CheckNoreturn() throws Exception {

        addGetExpectations(LispAddressUtil.asIPv4Address("153.16.254.1"));
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(mapNotify, null);
    }

    private void addDefaultPutAndGetExpectations(LispAddressContainer addr) {
        addPutExpectations(addr);
        addGetExpectations(addr);
    }

    private void addGetExpectations(LispAddressContainer address) {
        addGetExpectations(address,  0, 0, SubKeys.AUTH_KEY);
    }

    private EidToLocatorRecord toEidToLocatorRecord(Mapping record) {
        return new EidToLocatorRecordBuilder(
                (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.EidToLocatorRecord) record)
                .build();
    }

    private void prepareDsbe() {
        ValueSaverAction<Mapping> dsbeAddMappingSaverAction = new ValueSaverAction<Mapping>() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                mapSystem.addMapping(lastValue.getOrigin(), lastValue.getLispAddressContainer(), (Object) toEidToLocatorRecord(lastValue));
                return null;
            }
        };
        oneOf(dsbe).addMapping(with(dsbeAddMappingSaverAction)); will(dsbeAddMappingSaverAction);
    }

    private void addPutExpectations(LispAddressContainer address) {
        // needed for mapping-service addMapping
        prepareDsbe();
        exactly(2).of(dao).put(weq(address), with(mappingEntriesSaver));
    }

    private void addGetExpectations(LispAddressContainer address, int withoutPassword, int withPassword, String password) {
        if (withoutPassword > 0) {
            String result = null;
            result = null;
            allowing(dao).getSpecific(with(address), with(SubKeys.AUTH_KEY));
            ret(result);
        }
        if (withPassword > 0) {
            String result = null;
            result = password;
            allowing(dao).getSpecific(with(address), with(SubKeys.AUTH_KEY));
            ret(result);
        }
        for (int i = MaskUtil.getMaskForAddress(address); i >= 0; i--) {
            LispAddressContainer addr = MaskUtil.normalize(address, (short) i);
            allowing(dao).getSpecific(with(addr), with(SubKeys.AUTH_KEY)); ret(null);
            // check if mapping exists before insertion
            allowing(dao).get(with(addr));will(returnValue(null));
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
