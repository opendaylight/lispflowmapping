/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junitx.framework.ArrayAssert;

import org.eclipse.osgi.baseadaptor.bundlefile.MRUBundleFileList;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.NoBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegisterBuilder;

public class MapServerTest extends BaseTestCase {

    private LispMappingService testedMapServer;
    private ILispDAO lispDAO;
    private MapRegisterBuilder mapRegisterBuilder;
    private Ipv4 eid;
    private Ipv4 rloc;
    private ValueSaverAction<MappingEntry<?>[]> mappingEntriesSaver;
    private MapRegister mapRegisterWithAuthentication;

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
        eid = LispAFIConvertor.asIPAfiAddress("10.31.0.5");
        rloc = LispAFIConvertor.asIPAfiAddress("0x192.168.136.10");
        EidToLocatorRecordBuilder recordBuilder = new EidToLocatorRecordBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid)).setMaskLength((short) 32);
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        recordBuilder.getLocatorRecord().add(new LocatorRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(rloc)).build());
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

        addDefaultPutAndGetExpectations(eid, 32);
        assertNull(testedMapServer.handleMapRegister(mapRegisterBuilder.build()));

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals("value", entries[0].getKey());
        assertEquals(rloc, ((MappingServiceValue) entries[0].getValue()).getRlocs().get(0).getRecord().getLispAddressContainer().getAddress());
    }

    @Test
    public void handleMapRegisterIpv4__ValidNotifyEchoesRegister() throws Exception {
        mapRegisterBuilder.getEidToLocatorRecord().add(
                getDefaultEidToLocatorBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(new NoBuilder().build())).build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(eid, 32);
        addDefaultPutAndGetExpectations(new NoBuilder().build(), 32);
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
        eid = LispAFIConvertor.asIPAfiAddress("0.0.0.1");
        eidToLocatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));

        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("0.0.0.2")));
        locatorBuilder.setPriority((short) 55);
        eidToLocatorBuilder.getLocatorRecord().add(locatorBuilder.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(eidToLocatorBuilder.build());

        addDefaultPutAndGetExpectations(eid, 32);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        EidToLocatorRecord actualEidToLocator = mapNotify.getEidToLocatorRecord().get(0);
        assertEquals(LispAFIConvertor.toContainer(eid), actualEidToLocator.getLispAddressContainer());
        assertEquals((byte) 55, actualEidToLocator.getLocatorRecord().get(0).getPriority().byteValue());

    }

    @Test
    public void handleMapRegisterIpv4__ValidMask() throws Exception {
        int mask = 16;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid)).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(eid, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.31.0.5")), mapNotify.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__NonMaskable() throws Exception {
        int mask = 16;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAFIAddress addr = LispAFIConvertor.asMacAfiAddress("01:01:01:01:01:01");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(addr)).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr, mask);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(LispAFIConvertor.toContainer(addr), mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__ZeroMask() throws Exception {
        int mask = 0;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAFIAddress addr = LispAFIConvertor.asMacAfiAddress("01:01:01:01:01:01");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(addr)).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr, mask);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(LispAFIConvertor.toContainer(addr), mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIPv4__ZeroMask() throws Exception {
        int mask = 0;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid)).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(eid, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.31.0.5")), mapNotify.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());
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
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid)).setMaskLength((short) mask);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.getLocatorRecord().add(locator.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);
        MapRegister mr = mapRegisterBuilder.build();

        addDefaultPutAndGetExpectations(eid, mask);

        MapNotify mapNotify = testedMapServer.handleMapRegister(mr);
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("10.31.0.5")), mapNotify.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());
        ArrayAssert.assertEquals(mr.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mr.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mr.getKeyId(), mapNotify.getKeyId());
        assertEquals(mr.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask96() throws Exception {
        int mask = 96;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAFIAddress addr = LispAFIConvertor.asIPv6AfiAddress("1:1:1:1:1:1:1:0");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(addr)).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr, mask);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(LispAFIConvertor.toContainer(addr), mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ZeroMask() throws Exception {
        int mask = 0;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAFIAddress addr = LispAFIConvertor.asIPv6AfiAddress("1:1:1:1:1:1:1:0");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(addr)).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr, mask);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(LispAFIConvertor.toContainer(addr), mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask48() throws Exception {
        int mask = 48;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAFIAddress addr = LispAFIConvertor.asIPv6AfiAddress("1:1:1:1:1:1:1:0");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(addr)).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr, mask);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(LispAFIConvertor.toContainer(addr), mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegisterIpv6__ValidMask128() throws Exception {
        int mask = 128;
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAFIAddress addr = LispAFIConvertor.asIPv6AfiAddress("1:1:1:1:1:1:1:0");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(addr)).setMaskLength((short) mask);
        recordBuilder.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(addr, mask);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(LispAFIConvertor.toContainer(addr), mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
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
        recordBuilder0.setLispAddressContainer(LispAFIConvertor.toContainer(eid)).setMaskLength((short) 32);
        LispAFIAddress addr = LispAFIConvertor.asIPv6AfiAddress("1:1:1:1:1:1:1:0");
        EidToLocatorRecordBuilder recordBuilder1 = getDefaultEidToLocatorBuilder();
        LocatorRecordBuilder locatorBuilder = getDefaultLocatorBuilder();
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        locatorBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        recordBuilder1.setLispAddressContainer(LispAFIConvertor.toContainer(addr)).setMaskLength((short) mask);
        recordBuilder1.getLocatorRecord().add(locatorBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder0.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder1.build());
        mapRegisterBuilder.setWantMapNotify(true);

        addDefaultPutAndGetExpectations(eid, 32);
        addDefaultPutAndGetExpectations(addr, mask);

        MapRegister mapRegister = mapRegisterBuilder.build();

        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegister);
        assertEquals(LispAFIConvertor.toContainer(eid), mapNotify.getEidToLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(addr), mapNotify.getEidToLocatorRecord().get(1).getLispAddressContainer());
        ArrayAssert.assertEquals(mapRegister.getAuthenticationData(), mapNotify.getAuthenticationData());
        assertEquals(mapRegister.getEidToLocatorRecord(), mapNotify.getEidToLocatorRecord());
        assertEquals(mapRegister.getKeyId(), mapNotify.getKeyId());
        assertEquals(mapRegister.getNonce(), mapNotify.getNonce());
    }

    @Test
    public void handleMapRegister__MultipleRLOCs() throws Exception {
        addDefaultPutAndGetExpectations(eid, 32);

        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        LispAFIAddress rloc0 = rloc;
        LispAFIAddress rloc1 = LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:7");
        EidToLocatorRecordBuilder recordBuilder = getDefaultEidToLocatorBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        LocatorRecordBuilder locatorBuilder1 = getDefaultLocatorBuilder();
        locatorBuilder1.setLispAddressContainer(LispAFIConvertor.toContainer(rloc0));
        LocatorRecordBuilder locatorBuilder2 = getDefaultLocatorBuilder();
        locatorBuilder2.setLispAddressContainer(LispAFIConvertor.toContainer(rloc1));
        recordBuilder.getLocatorRecord().add(locatorBuilder1.build());
        recordBuilder.getLocatorRecord().add(locatorBuilder2.build());

        mapRegisterBuilder.getEidToLocatorRecord().add(recordBuilder.build());

        testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        MappingEntry<?>[] entries = mappingEntriesSaver.lastValue;
        assertEquals(1, entries.length);

        assertEquals("value", entries[0].getKey());
        assertEquals(LispAFIConvertor.toContainer(rloc0), ((MappingServiceValue) entries[0].getValue()).getRlocs().get(0).getRecord()
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(rloc1), ((MappingServiceValue) entries[0].getValue()).getRlocs().get(1).getRecord()
                .getLispAddressContainer());

    }

    @Test
    public void handleMapRegister__MultipleEIDs() throws Exception {
        addDefaultPutAndGetExpectations(eid, 32);

        LispAFIAddress rloc0 = rloc;
        LispAFIAddress rloc1 = LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:7");
        mapRegisterBuilder = getDefaultMapRegisterBuilder();
        EidToLocatorRecordBuilder etlrBuilder = getDefaultEidToLocatorBuilder();
        etlrBuilder.getLocatorRecord().add(getDefaultLocatorBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(rloc1)).build());
        mapRegisterBuilder.setWantMapNotify(true);
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));

        EidToLocatorRecordBuilder etlr2Builder = getDefaultEidToLocatorBuilder();
        LispAFIAddress address = LispAFIConvertor.asIPAfiAddress("1.1.1.1");
        etlr2Builder.setLispAddressContainer(LispAFIConvertor.toContainer(address));
        etlr2Builder.setMaskLength((short) 32);
        int recordTtl = 5;
        etlr2Builder.setRecordTtl(recordTtl);
        etlr2Builder.getLocatorRecord().add(
                getDefaultLocatorBuilder().setPriority((short) 10)
                        .setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("2.2.2.2"))).build());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlr2Builder.build());
        addDefaultPutAndGetExpectations(address, 32);
        MapNotify mapNotify = testedMapServer.handleMapRegister(mapRegisterBuilder.build());

        assertEquals(LispAFIConvertor.toContainer(rloc1), mapNotify.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("2.2.2.2")), mapNotify.getEidToLocatorRecord().get(1)
                .getLocatorRecord().get(0).getLispAddressContainer());
        assertEquals(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("1.1.1.1")), mapNotify.getEidToLocatorRecord().get(1)
                .getLispAddressContainer());
        assertEquals(recordTtl, mapNotify.getEidToLocatorRecord().get(1).getRecordTtl().intValue());

    }

    // @Test
    // public void handleMapRegisterIpv4__ChekWrongPassword() throws Exception {
    //
    // addGetExpectations(new LispIpv4Address("153.16.254.1"), 32, 0, 31,
    // "bla");
    // MapNotify mapNotify =
    // testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
    // assertEquals(null, mapNotify);
    // }
    //
    // @Test
    // public void handleMapRegisterIpv4__ChcekNoPasswordAndThenPassword()
    // throws Exception {
    //
    // addGetExpectations(new LispIpv4Address("153.16.254.1"), 32, 30, 25,
    // "password");
    // addPutExpectations(new LispIpv4Address("153.16.254.1"), 32);
    // MapNotify mapNotify =
    // testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
    // assertEquals(mapRegisterWithAuthentication.getEidToLocatorRecords(),
    // mapNotify.getEidToLocatorRecords());
    // assertEquals(mapRegisterWithAuthentication.getKeyId(),
    // mapNotify.getKeyId());
    // assertEquals(mapRegisterWithAuthentication.getNonce(),
    // mapNotify.getNonce());
    // }
    //
    // @Test
    // public void handleMapRegisterIpv4__ChcekNoPassword() throws Exception {
    //
    // addGetExpectations(new LispIpv4Address("153.16.254.1"), 32, 30, 0,
    // "password");
    // MapNotify mapNotify =
    // testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
    // assertEquals(null, mapNotify);
    // }
    //
    // @Test
    // public void handleMapRegisterIpv4__ChcekNoreturn() throws Exception {
    //
    // addGetExpectations(new LispIpv4Address("153.16.254.1"), 32);
    // MapNotify mapNotify =
    // testedMapServer.handleMapRegister(mapRegisterWithAuthentication);
    // assertEquals(mapNotify, null);
    // }
    //
    // @Test
    // public void handleAddAuthenticationKey() throws Exception {
    // IMappingServiceKey key = getDefualtKey();
    // MappingServiceValue value = getDefualtValue();
    // MappingEntry<MappingServiceValue> mappingEntry = new
    // MappingEntry<MappingServiceValue>("value", value);
    // oneOf(lispDAO).get(weq(key));
    // oneOf(lispDAO).put(weq(key), weq(convertToArray(mappingEntry)));
    // assertEquals(true, testedMapServer.addAuthenticationKey(eid,
    // key.getMask(), value.getKey()));
    // }
    //
    // @Test
    // public void handleGetAuthenticationKey() throws Exception {
    // IMappingServiceKey key = getDefualtKey();
    // MappingServiceValue value = getDefualtValue();
    // Map<String, MappingServiceValue> map = new HashMap<String,
    // MappingServiceValue>();
    // map.put("value", value);
    // atLeast(1).of(lispDAO).get(weq(key));
    // ret(map);
    // assertEquals(value.getKey(), testedMapServer.getAuthenticationKey(eid,
    // key.getMask()));
    // }
    //
    // @Test
    // public void handleGetAuthenticationKeyNoIteration() throws Exception {
    // testedMapServer.setShouldIterateMask(false);
    // IMappingServiceKey key = getDefualtKey();
    // IMappingServiceKey passKey = getKey(30);
    // MappingServiceValue value = getDefualtValue();
    // Map<String, MappingServiceValue> map = new HashMap<String,
    // MappingServiceValue>();
    // map.put("value", value);
    // oneOf(lispDAO).get(weq(key));
    // allowing(lispDAO).get(weq(passKey));
    // ret(map);
    // assertEquals(null, testedMapServer.getAuthenticationKey(eid,
    // key.getMask()));
    // }
    //
    // @Test
    // public void handleRemoveAuthenticationKey() throws Exception {
    // IMappingServiceKey key = getDefualtKey();
    // MappingServiceValue value = new MappingServiceValue();
    // Map<String, MappingServiceValue> map = new HashMap<String,
    // MappingServiceValue>();
    // map.put("value", value);
    // oneOf(lispDAO).get(weq(key));
    // ret(map);
    // oneOf(lispDAO).remove(weq(key));
    // assertEquals(true, testedMapServer.removeAuthenticationKey(eid,
    // key.getMask()));
    // }
    //
    // @Test
    // public void handleRemoveAuthenticationKeyWhereKeyDosntExist() throws
    // Exception {
    // IMappingServiceKey key = getDefualtKey();
    // MappingServiceValue value = new MappingServiceValue();
    // Map<String, MappingServiceValue> map = new HashMap<String,
    // MappingServiceValue>();
    // map.put("value", value);
    // oneOf(lispDAO).get(weq(key));
    // assertEquals(false, testedMapServer.removeAuthenticationKey(eid,
    // key.getMask()));
    // }

    private void addDefaultPutAndGetExpectations(LispAFIAddress addr, int mask) {
        addPutExpectations(addr, mask);
        addGetExpectations(addr, mask);
    }

    @SuppressWarnings("rawtypes")
    private MappingEntry[] convertToArray(MappingEntry<MappingServiceValue> entry) {
        MappingEntry[] arr = new MappingEntry[1];
        arr[0] = entry;
        return arr;
    }

    private void addGetExpectations(LispAFIAddress address, int mask) {
        addGetExpectations(address, mask, 0, 0, "password");
    }

    private void addPutExpectations(LispAFIAddress address, int mask) {
        oneOf(lispDAO).put(weq(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(address), mask)),
                with(mappingEntriesSaver));
    }

    private void addGetExpectations(LispAFIAddress address, int mask, int withoutPassword, int withPassword, String password) {
        if (withoutPassword > 0) {
            Map<String, MappingServiceValue> result = new HashMap<String, MappingServiceValue>();
            result.put("value", new MappingServiceValue());
            allowing(lispDAO).getSpecific(
                    with(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(address), withoutPassword)), with("value"));
            ret(result);
        }
        if (withPassword > 0) {
            Map<String, MappingServiceValue> result = new HashMap<String, MappingServiceValue>();
            result.put("value", new MappingServiceValue());
            result.get("value").setKey(password);
            allowing(lispDAO).get(with(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(address), withPassword)));
            ret(result);
        }
        for (int i = mask; i >= 0; i--) {
            allowing(lispDAO).get(with(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(address), i)));
            ret(new HashMap<String, MappingServiceValue>());
        }
    }

    private IMappingServiceKey getDefualtKey() {
        return getKey(32);
    }

    private IMappingServiceKey getKey(int mask) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid), mask);
        return key;
    }

    private MappingServiceValue getDefualtValue() {
        MappingServiceValue value = new MappingServiceValue();
        String password = "pass";
        value.setKey(password);
        return value;
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
