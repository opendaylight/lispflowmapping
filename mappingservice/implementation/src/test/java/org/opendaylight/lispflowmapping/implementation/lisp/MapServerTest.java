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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.NoAddressAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;

public class MapServerTest extends BaseTestCase {

    private LispMappingService testedMapServer;
    private ILispDAO dao;
    private MappingService mapService;
    private DataStoreBackEnd dsbe;
    private MappingSystem mapSystem;

    private MapRegisterBuilder mapRegisterBuilder;
    private Eid eid;
    private Rloc rloc;
    private ValueSaverAction<MappingEntry<?>[]> mappingEntriesSaver;
    private MapRegister mapRegisterWithAuthentication;
    private String eidIpv4String = "10.31.0.5/32";
    private String eidIpv6String = "1:1:1:1:1:1:1:0/128";

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
        eid = LispAddressUtil.asIpv4PrefixEid(eidIpv4String);
        rloc = LispAddressUtil.asIpv4Rloc("192.168.136.10");
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setEid(eid);
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        recordBuilder.getLocatorRecord().add(new LocatorRecordBuilder().setRloc(rloc).build());
        recordBuilder.setAction(Action.NoAction).setMapVersion((short) 0).setAuthoritative(false).setRecordTtl(60).setMaskLength((short) 32);
        mapRegisterBuilder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
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
        assertEquals(rloc, ((MappingRecord) entries[0].getValue()).getLocatorRecord().get(0).getRloc());
    }

    @Test
    public void handleMapRegisterIpv4__ValidNotifyEchoesRegister() throws Exception {
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(
                getDefaultMappingRecordBuilder().setEid(new EidBuilder().setAddressType(NoAddressAfi.class).setAddress(
                        (Address) new NoAddressBuilder().setNoAddress(true).build()).build()).build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(eid);
        addDefaultPutAndGetExpectations(new EidBuilder().setAddressType(NoAddressAfi.class).setAddress(
                (Address) new NoAddressBuilder().setNoAddress(true).build()).build());
        MapRegister mr = mapRegisterBuilder.build();
        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
        assertEquals(mr.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__CloneNotOwnYouClown() throws Exception {
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        mapRegisterBuilder.setWantMapNotify(true);
        MappingRecordBuilder eidToLocatorBuilder = getDefaultMappingRecordBuilder();
        eidToLocatorBuilder.setMaskLength((short) 32);
        eid = LispAddressUtil.asIpv4PrefixEid("0.0.0.1/32");
        eidToLocatorBuilder.setEid(eid);

        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(LispAddressUtil.asIpv4Rloc("0.0.0.2"));
        locatorBuilder.setPriority((short) 55);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder.build());

        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(eidToLocatorBuilder.build()).build());

        addDefaultPutAndGetExpectations(eid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        MappingRecord actualEidToLocator = mapNotify.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(eid, actualEidToLocator.getEid());
        assertEquals((byte) 55, actualEidToLocator.getLocatorRecord().get(0).getPriority().byteValue());

    }

    @Test
    public void handleMapRegisterIpv4__ValidMask() throws Exception {
        int mask = 16;
        Eid newEid = LispAddressUtil.asIpv4PrefixEid(eidIpv4String + "/" + mask);
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.setEid(newEid).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(rloc);
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(newEid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
        assertEquals(newEid, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__NonMaskable() throws Exception {
        int mask = 16;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Eid addr = LispAddressUtil.asMacEid("01:01:01:01:01:01");
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        recordBuilder.setEid(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(addr, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__ZeroMask() throws Exception {
        int mask = 0;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Eid addr = LispAddressUtil.asMacEid("01:01:01:01:01:01");
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        recordBuilder.setEid(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(addr, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIPv4__ZeroMask() throws Exception {
        int mask = 0;
        Eid newEid = LispAddressUtil.asIpv4PrefixEid(eidIpv4String + "/" + mask);
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.setEid(newEid).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(rloc);
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(newEid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
        assertEquals(newEid, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__ValidMask32() throws Exception {
        int mask = 32;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.setEid(eid).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(rloc);
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(eid);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
        assertEquals(eid, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask96() throws Exception {
        int mask = 96;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Eid addr = LispAddressUtil.asIpv6PrefixEid(eidIpv6String + "/" + mask);
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        recordBuilder.setEid(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(addr, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ZeroMask() throws Exception {
        int mask = 0;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Eid addr = LispAddressUtil.asIpv6PrefixEid(eidIpv6String + "/" + mask);
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        recordBuilder.setEid(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(addr, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask48() throws Exception {
        int mask = 48;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Eid addr = LispAddressUtil.asIpv6PrefixEid(eidIpv6String +"/" + mask);
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        recordBuilder.setEid(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(addr, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask128() throws Exception {
        int mask = 128;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Eid addr = LispAddressUtil.asIpv6PrefixEid(eidIpv6String);
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        recordBuilder.setEid(addr).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(addr, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIPV4AndIpv6__ValidMask96() throws Exception {
        int mask = 96;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        MappingRecordBuilder recordBuilder0 = getDefaultMappingRecordBuilder();
        recordBuilder0.setEid(eid).setMaskLength((short) 32);
        Eid addr = LispAddressUtil.asIpv6PrefixEid(eidIpv6String + "/" + mask);
        MappingRecordBuilder recordBuilder1 = getDefaultMappingRecordBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        locatorBuilder.setRloc(rloc);
        recordBuilder1.setEid(addr).setMaskLength((short) mask);
        recordBuilder1.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder0.build()).build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder1.build()).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(eid);
        addDefaultPutAndGetExpectations(addr);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(eid, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getEid());
        assertEquals(addr, mapNotify.getMappingRecordItem().get(1).getMappingRecord().getEid());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__MultipleRLOCs() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Rloc rloc0 = rloc;
        Rloc rloc1 = LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:7");
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.setEid(eid);
        LocatorRecordBuilder locatorBuilder1 = getDefaultLocatorBuilder();
        locatorBuilder1.setRloc(rloc0);
        LocatorRecordBuilder locatorBuilder2 = getDefaultLocatorBuilder();
        locatorBuilder2.setRloc(rloc1);
        recordBuilder.getLocatorRecord().add(locatorBuilder1.build());
        recordBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc0, ((MappingRecord) entries[0].getValue()).getLocatorRecord()
                .get(0).getRloc());
        assertEquals(rloc1, ((MappingRecord) entries[0].getValue()).getLocatorRecord()
                .get(1).getRloc());

    }

    @Test
    public void handleMapRegister__MultipleTypes() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        Rloc rloc0 = rloc;
        // LispAFIAddress rloc1 =
        // LispAddressUtil.asIPv6AfiAddress("0:0:0:0:0:0:0:7");
        String subkey = "bla";
        Rloc rloc1 = LispAddressUtil.asKeyValueAddress(subkey, SimpleAddressBuilder.getDefaultInstance("0:0:0:0:0:0:0:7"));
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.setEid(eid);
        LocatorRecordBuilder locatorBuilder1 = getDefaultLocatorBuilder();
        locatorBuilder1.setRloc(rloc0);
        LocatorRecordBuilder locatorBuilder2 = getDefaultLocatorBuilder();
        locatorBuilder2.setRloc(rloc1);
        recordBuilder.getLocatorRecord().add(locatorBuilder1.build());
        recordBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);
        MappingRecord storedMapping = (MappingRecord) entries[0].getValue();
        assertEquals(2, storedMapping.getLocatorRecord().size());
        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc0, storedMapping.getLocatorRecord().get(0).getRloc());
        assertEquals(rloc1, storedMapping.getLocatorRecord().get(1).getRloc());

    }

    @Test
    public void handleMapRegister__TestOverwrite() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        addEidToLocatorRecord();

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals(SubKeys.RECORD, entries[0].getKey());
        assertEquals(rloc, ((MappingRecord) entries[0].getValue()).getLocatorRecord().get(0).getRloc());
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
//                .get(0).getEid());
//    }

    private void addEidToLocatorRecord() {
        MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.setEid(eid);
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setRloc(rloc);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());

        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(recordBuilder.build()).build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build());
    }

    @Test
    public void handleMapRegister__MultipleEIDs() throws Exception {
        addDefaultPutAndGetExpectations(eid);

        Rloc rloc1 = LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:7");
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        MappingRecordBuilder etlrBuilder = getDefaultMappingRecordBuilder();
        etlrBuilder.getLocatorRecord().add(getDefaultLocatorBuilder().setRloc(rloc1).build());
        mapRegisterBuilder.setWantMapNotify(true);
        etlrBuilder.setEid(eid);

        MappingRecordBuilder etlr2Builder = getDefaultMappingRecordBuilder();
        Eid address = LispAddressUtil.asIpv4PrefixEid("1.1.1.1");
        etlr2Builder.setEid(address);
        etlr2Builder.setMaskLength((short) 32);
        int recordTtl = 5;
        etlr2Builder.setRecordTtl(recordTtl);
        etlr2Builder.getLocatorRecord().add(
                getDefaultLocatorBuilder().setPriority((short) 10)
                        .setRloc(LispAddressUtil.asIpv4Rloc("2.2.2.2")).build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlrBuilder.build()).build());
        mapRegisterBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(etlr2Builder.build()).build());
        addDefaultPutAndGetExpectations(address);
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        assertEquals(rloc1, mapNotify.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0)
                .getRloc());
        assertEquals(LispAddressUtil.asIpv4Rloc("2.2.2.2"), mapNotify.getMappingRecordItem().get(1).getMappingRecord()
                .getLocatorRecord().get(0).getRloc());
        assertEquals(address, mapNotify.getMappingRecordItem().get(1).getMappingRecord().getEid());
        assertEquals(recordTtl, mapNotify.getMappingRecordItem().get(1).getMappingRecord().getRecordTtl().intValue());

    }

    @Test
    public void handleMapRegisterIpv4__CheckWrongPassword() throws Exception {

        addGetExpectations(LispAddressUtil.asIpv4PrefixEid("153.16.254.1"), 0, 31, "bla");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__CheckNoPasswordAndThenPassword() throws Exception {

        addGetExpectations(LispAddressUtil.asIpv4PrefixEid("153.16.254.1"), 0, 25, "password");
        addPutExpectations(LispAddressUtil.asIpv4PrefixEid("153.16.254.1"));
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(mapRegisterWithAuthentication.getMappingRecordItem(), mapNotify.getMappingRecordItem());
        assertEquals(mapRegisterWithAuthentication.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegisterWithAuthentication.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv4__CheckNoPassword() throws Exception {

        addGetExpectations(LispAddressUtil.asIpv4PrefixEid("153.16.254.1/32"), 30, 0, "password");
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(null, mapNotify);
    }

    @Test
    public void handleMapRegisterIpv4__CheckNoreturn() throws Exception {

        addGetExpectations(LispAddressUtil.asIpv4PrefixEid("153.16.254.1/32"));
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
        assertEquals(mapNotify, null);
    }

    private void addDefaultPutAndGetExpectations(Eid addr) {
        addPutExpectations(addr);
        addGetExpectations(addr);
    }

    private void addGetExpectations(Eid address) {
        addGetExpectations(address, 0, 0, SubKeys.AUTH_KEY);
    }

    private void prepareDsbe() {
        ValueSaverAction<Mapping> dsbeAddMappingSaverAction = new ValueSaverAction<Mapping>() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                mapSystem.addMapping(lastValue.getOrigin(), lastValue.getMappingRecord().getEid(),
                        (Object) lastValue.getMappingRecord());
                return null;
            }
        };
        oneOf(dsbe).addMapping(with(dsbeAddMappingSaverAction)); will(dsbeAddMappingSaverAction);
    }

    private void addPutExpectations(Eid address) {
        // needed for mapping-service addMapping
        prepareDsbe();
        exactly(2).of(dao).put(weq(address), with(mappingEntriesSaver));
    }

    private void addGetExpectations(Eid address, int withoutPassword, int withPassword, String password) {
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
        for (int i = MaskUtil.getMaskForAddress(address.getAddress()); i >= 0; i--) {
            Eid addr = MaskUtil.normalize(address, (short) i);
            allowing(dao).getSpecific(with(addr), with(SubKeys.AUTH_KEY)); ret(null);
            // check if mapping exists before insertion
            allowing(dao).get(with(addr));will(returnValue(null));
        }

    }

    private MapRegisterBuilder getDefaultMapRegisterBuilder() {
        MapRegisterBuilder mrb = new MapRegisterBuilder();
        mrb.setKeyId((short) 0);
        mrb.setNonce((long) 0);
        mrb.setWantMapNotify(false);
        mrb.setProxyMapReply(false);
        mrb.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        mrb.setAuthenticationData(new byte[0]);
        return mrb;
    }

    private MappingRecordBuilder getDefaultMappingRecordBuilder() {
        MappingRecordBuilder builder = new MappingRecordBuilder();
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
