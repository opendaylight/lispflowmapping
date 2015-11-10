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
import java.util.HashMap;
import java.util.Map;

import org.jmock.api.Invocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;

public class MapResolverTest extends BaseTestCase {

    // private MapResolver testedMapResolver;
    private LispMappingService testedMapResolver;
    private ILispDAO dao;
    private MappingService mapService;
    private MapRequestBuilder mapRequest;
    private LispAddressContainer v4Address;
    private LispAddressContainer v6Address;

    private HashMap<LispAddressContainer, Map<String, EidToLocatorRecord>> daoResults;

    @Override
    @Before
    public void before() throws Exception {
        super.before();

        dao = context.mock(ILispDAO.class);

        // map-cache init and table creation
        allowing(dao).putTable(wany(String.class));will(returnValue(dao));

        MappingSystem mapSystem = new MappingSystem(dao, true, true, true);
        mapService = new MappingService();
        mapService.setDaoService(dao);
        inject(mapService, "mappingSystem", mapSystem);

        testedMapResolver = new LispMappingService();
        testedMapResolver.setMappingService(mapService);
        testedMapResolver.basicInit();

        mapRequest = new MapRequestBuilder();
        v4Address = LispAFIConvertor.asIPv4Address("1.2.3.4");
        v6Address = LispAFIConvertor.asIPv6Address("0:0:0:0:0:0:0:1");
        daoResults = new HashMap<LispAddressContainer, Map<String, EidToLocatorRecord>>();
    }

    @Test
    public void handleMapRequest__ReplyWithSingleLocator() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32)
                        .setLispAddressContainer(LispAFIConvertor.asIPv4Address("1.2.3.4")).build());

        EidToLocatorRecordBuilder record = getDefaultEidToLocatorBuilder();
        record.setLispAddressContainer(v4Address);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLocalLocator(false);
        locator.setLispAddressContainer(LispAFIConvertor.asIPv4Address("4.3.2.1"));
        locator.setRouted(true);
        locator.setMulticastPriority((short) 5);
        locator.setWeight((short) 17);
        locator.setPriority((short) 16);
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals(1, eidToLocators.getLocatorRecord().size());
        LocatorRecord resultLocator = mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0);
        assertEquals(locator.isLocalLocator(), resultLocator.isLocalLocator());
        assertEquals(locator.isRouted(), resultLocator.isRouted());
        assertEquals(locator.getMulticastPriority(), resultLocator.getMulticastPriority());
        assertEquals(locator.getMulticastWeight(), resultLocator.getMulticastWeight());
        assertEquals(locator.getPriority(), resultLocator.getPriority());
        assertEquals(locator.getWeight(), resultLocator.getWeight());

        assertLocator(LispAFIConvertor.asIPv4Address("4.3.2.1"), eidToLocators.getLocatorRecord().get(0));
    }

    @Test
    public void handleMapRequest__VerifyBasicFields() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(v4Address).build());

        EidToLocatorRecordBuilder record = getDefaultEidToLocatorBuilder();
        record.setLispAddressContainer(v4Address);
        record.setRecordTtl(100);

        record.setAuthoritative(true);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.asIPv4Address("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        assertEquals(mapRequest.getNonce(), mapReply.getNonce());
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals((byte) 32, eidToLocators.getMaskLength().byteValue());
        assertEquals(v4Address, eidToLocators.getLispAddressContainer());
        assertEquals(record.isAuthoritative(), eidToLocators.isAuthoritative());
        assertEquals(record.getAction(), eidToLocators.getAction());
        assertEquals(record.getRecordTtl(), eidToLocators.getRecordTtl());
    }

    @Test
    public void handleMapRequest__VerifyMask() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(v4Address).build());

        EidToLocatorRecordBuilder record = getDefaultEidToLocatorBuilder();
        record.setLispAddressContainer(LispAFIConvertor.asIPv4Prefix("1.2.3.0", 24));
        record.setMaskLength((short) 24);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();
        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        assertEquals(mr.getNonce(), mapReply.getNonce());
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals((byte) 24, eidToLocators.getMaskLength().byteValue());
        assertEquals(LispAFIConvertor.asIPv4Prefix("1.2.3.0", 24), eidToLocators.getLispAddressContainer());
    }

    @Test
    public void handleMapRequest__VerifyMaskIPv6() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 128).setLispAddressContainer(v6Address).build());

        EidToLocatorRecordBuilder record = getDefaultEidToLocatorBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:0")));
        record.setMaskLength((short) 128);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();

        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        assertEquals(mr.getNonce(), mapReply.getNonce());
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals((byte) 128, eidToLocators.getMaskLength().byteValue());
        assertEquals(v6Address, eidToLocators.getLispAddressContainer());
    }

    @Test
    public void handleMapRequest__VerifyMaskIPv6NoMatch() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 128).setLispAddressContainer(v6Address).build());

        EidToLocatorRecordBuilder record = getDefaultEidToLocatorBuilder();
        record.setLispAddressContainer(LispAFIConvertor.asIPv6Prefix("0:1:0:0:0:0:0:1", 112));
        record.setMaskLength((short) 112);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.asIPv4Address("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();

        MapReply mapReply = testedMapResolver.handleMapRequest(mr);
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals(null, eidToLocators.getLocatorRecord());
    }

    @Test
    public void handleMapRequest_VerifyNativelyForwardAutherized() {
        MapRequest mr = getDefaultMapRequest();

        Map<String, EidToLocatorRecord> result = new HashMap<String, EidToLocatorRecord>();
        result.put(SubKeys.RECORD, null);

        MapReply mapReply = getNativelyForwardMapReply(mr, result);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals(1, eidToLocators.getRecordTtl().intValue());
        assertEquals(Action.NativelyForward, eidToLocators.getAction());
    }

    private MapReply getNativelyForwardMapReply(MapRequest mr, Map<String, EidToLocatorRecord> result) {
        allowing(dao).get(wany(LispAddressContainer.class));
        ret(result);
        allowing(dao).getSpecific(wany(LispAddressContainer.class), with(SubKeys.AUTH_KEY));
        ret("pass");
        MapReply mapReply = testedMapResolver.handleMapRequest(mr);
        return mapReply;
    }

    private MapRequest getDefaultMapRequest() {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(v4Address).build());
        MapRequest mr = mapRequest.build();
        return mr;
    }

    @Test
    public void handleMapRequest__VerifyMaskNoMatch() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(v4Address).build());

        EidToLocatorRecordBuilder record = getDefaultEidToLocatorBuilder();
        record.setLispAddressContainer(LispAFIConvertor.asIPv4Prefix("1.2.4.0", 24));
        record.setMaskLength((short) 24);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.asIPv4Address("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();

        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals(null, eidToLocators.getLocatorRecord());
    }

    @Test
    public void handleMapRequest__ReplyWithMultipleLocators() throws Exception {

        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(v4Address).build());

        EidToLocatorRecordBuilder record = getDefaultEidToLocatorBuilder();
        record.setLispAddressContainer(v4Address);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.asIPv4Address("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.asIPv6Address("0:0:0:0:0:0:0:1"));
        record.getLocatorRecord().add(locator.build());
        locator = getDefaultLocatorBuilder();
        locator.setLispAddressContainer(LispAFIConvertor.asIPv4Address("1.8.2.7"));
        record.getLocatorRecord().add(locator.build());

        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecord().get(0);
        assertEquals(3, eidToLocators.getLocatorRecord().size());

        assertLocator(LispAFIConvertor.asIPv4Address("4.3.2.1"), eidToLocators.getLocatorRecord().get(0));
        assertLocator(LispAFIConvertor.asIPv6Address("0:0:0:0:0:0:0:1"), eidToLocators.getLocatorRecord().get(1));
        assertLocator(LispAFIConvertor.asIPv4Address("1.8.2.7"), eidToLocators.getLocatorRecord().get(2));
    }

    @Test
    public void handleMapRequest__MultipleEIDs() throws Exception {

        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(v4Address).build());
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 128).setLispAddressContainer(v6Address).build());

        EidToLocatorRecordBuilder record1 = getDefaultEidToLocatorBuilder();
        record1.setLispAddressContainer(v4Address);
        record1.setRecordTtl(100);

        LocatorRecordBuilder locator1 = getDefaultLocatorBuilder();
        locator1.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")));
        record1.getLocatorRecord().add(locator1.build());

        EidToLocatorRecordBuilder record2 = getDefaultEidToLocatorBuilder();
        record2.setLispAddressContainer(v6Address);
        record2.setMaskLength((short) 128);
        record2.setRecordTtl(100);

        LocatorRecordBuilder locator2 = getDefaultLocatorBuilder();
        locator2.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1")));
        record2.getLocatorRecord().add(locator2.build());

        prepareMapping(record1.build(), record2.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        EidToLocatorRecord eidToLocators1 = mapReply.getEidToLocatorRecord().get(0);
        assertEquals(1, eidToLocators1.getLocatorRecord().size());
        assertLocator(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")), eidToLocators1
                .getLocatorRecord().get(0));

        EidToLocatorRecord eidToLocators2 = mapReply.getEidToLocatorRecord().get(1);
        assertEquals(1, eidToLocators2.getLocatorRecord().size());
        assertLocator(LispAFIConvertor.toContainer(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1")), eidToLocators2
                .getLocatorRecord().get(0));
    }

    private void assertLocator(LispAddressContainer expectedAddress, LocatorRecord locatorRecord) {
        assertEquals(expectedAddress, locatorRecord.getLispAddressContainer());
        Assert.assertTrue(locatorRecord.isRouted());
    }

    private Map<String, EidToLocatorRecord> prepareMapping(EidToLocatorRecord... records) {
        if (records.length > 0) {
            for (EidToLocatorRecord eidToLocatorRecord : records) {
                Map<String, EidToLocatorRecord> result = new HashMap<String, EidToLocatorRecord>();
                result.put(SubKeys.RECORD, eidToLocatorRecord);

                daoResults.put(eidToLocatorRecord.getLispAddressContainer(), result);
            }
        }

        ValueSaverAction<LispAddressContainer> daoGetSaverAction = new ValueSaverAction<LispAddressContainer>() {
            @Override
            protected boolean validate(LispAddressContainer value) {
                return true;
            }

            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                return daoResults.get(lastValue);
            }
        };

        allowing(dao).get(with(daoGetSaverAction));
        will(daoGetSaverAction);
        allowing(dao).getSpecific(wany(LispAddressContainer.class), with(SubKeys.AUTH_KEY));

        return daoResults.get(v4Address);
    }

    private MapRequestBuilder getDefaultMapRequestBuilder() {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setAuthoritative(false);
        mrBuilder.setEidRecord(new ArrayList<EidRecord>());
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.setMapDataPresent(true);
        mrBuilder.setNonce((long) 0);
        mrBuilder.setPitr(false);
        mrBuilder.setProbe(false);
        mrBuilder.setSmr(false);
        mrBuilder.setSmrInvoked(true);
        mrBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("127.0.0.1"))).build());
        return mrBuilder;
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
        builder.setRouted(true);
        builder.setWeight((short) 0);
        return builder;
    }

}
