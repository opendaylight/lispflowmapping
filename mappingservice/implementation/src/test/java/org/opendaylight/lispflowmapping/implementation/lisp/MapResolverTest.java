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
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class MapResolverTest extends BaseTestCase {

    // private MapResolver testedMapResolver;
    private LispMappingService testedMapResolver;
    private ILispDAO dao;
    private MappingService mapService;
    private MapRequestBuilder mapRequest;
    private Eid v4Address;
    private Eid v6Address;

    private HashMap<Eid, Map<String, MappingRecord>> daoResults;

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
        v4Address = LispAddressUtil.asIpv4PrefixEid("1.2.3.4/32");
        v6Address = LispAddressUtil.asIpv6PrefixEid("0:0:0:0:0:0:0:1/128");
        daoResults = new HashMap<Eid, Map<String, MappingRecord>>();
    }

    @Test
    @Ignore
    public void handleMapRequest__ReplyWithSingleLocator() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v4Address).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(v4Address);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setLocalLocator(false);
        locator.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        locator.setRouted(true);
        locator.setMulticastPriority((short) 5);
        locator.setWeight((short) 17);
        locator.setPriority((short) 16);
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(1, eidToLocators.getLocatorRecord().size());
        LocatorRecord resultLocator = mapReply.getMappingRecordItem().get(0).getMappingRecord().getLocatorRecord().get(0);
        assertEquals(locator.isLocalLocator(), resultLocator.isLocalLocator());
        assertEquals(locator.isRouted(), resultLocator.isRouted());
        assertEquals(locator.getMulticastPriority(), resultLocator.getMulticastPriority());
        assertEquals(locator.getMulticastWeight(), resultLocator.getMulticastWeight());
        assertEquals(locator.getPriority(), resultLocator.getPriority());
        assertEquals(locator.getWeight(), resultLocator.getWeight());

        assertLocator(LispAddressUtil.asIpv4Rloc("4.3.2.1"), eidToLocators.getLocatorRecord().get(0));
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyBasicFields() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v4Address).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(v4Address);
        record.setRecordTtl(100);

        record.setAuthoritative(true);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        assertEquals(mapRequest.getNonce(), mapReply.getNonce());
        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(v4Address, eidToLocators.getEid());
        assertEquals(record.isAuthoritative(), eidToLocators.isAuthoritative());
        assertEquals(record.getAction(), eidToLocators.getAction());
        assertEquals(record.getRecordTtl(), eidToLocators.getRecordTtl());
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMask() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v4Address).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv4PrefixEid("1.2.3.0/24"));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();
        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        assertEquals(mr.getNonce(), mapReply.getNonce());
        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(LispAddressUtil.asIpv4PrefixEid("1.2.3.0/24"), eidToLocators.getEid());
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMaskIPv6() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v6Address).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv6PrefixEid("0:0:0:0:0:0:0:0/128"));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();

        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        assertEquals(mr.getNonce(), mapReply.getNonce());
        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(v6Address, eidToLocators.getEid());
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMaskIPv6NoMatch() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v6Address).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv6PrefixEid("0:1:0:0:0:0:0:1/112"));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();

        MapReply mapReply = testedMapResolver.handleMapRequest(mr);
        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(null, eidToLocators.getLocatorRecord());
    }

    @Test
    @Ignore
    public void handleMapRequest_VerifyNativelyForwardAuthorized() {
        MapRequest mr = getDefaultMapRequest();

        Map<String, MappingRecord> result = new HashMap<String, MappingRecord>();
        result.put(SubKeys.RECORD, null);

        MapReply mapReply = getNativelyForwardMapReply(mr, result);

        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(1, eidToLocators.getRecordTtl().intValue());
        assertEquals(Action.NativelyForward, eidToLocators.getAction());
    }

    @Ignore
    private MapReply getNativelyForwardMapReply(MapRequest mr, Map<String, MappingRecord> result) {
        allowing(dao).get(wany(Eid.class));
        ret(result);
        allowing(dao).getSpecific(wany(Eid.class), with(SubKeys.AUTH_KEY));
        ret("pass");
        MapReply mapReply = testedMapResolver.handleMapRequest(mr);
        return mapReply;
    }

    private MapRequest getDefaultMapRequest() {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v4Address).build());
        MapRequest mr = mapRequest.build();
        return mr;
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMaskNoMatch() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v4Address).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv4PrefixEid("1.2.4.0/24"));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();

        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(null, eidToLocators.getLocatorRecord());
    }

    @Test
    @Ignore
    public void handleMapRequest__ReplyWithMultipleLocators() throws Exception {

        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v4Address).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(v4Address);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record.getLocatorRecord().add(locator.build());
        locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:1"));
        record.getLocatorRecord().add(locator.build());
        locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc("1.8.2.7"));
        record.getLocatorRecord().add(locator.build());

        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(3, eidToLocators.getLocatorRecord().size());

        assertLocator(LispAddressUtil.asIpv4Rloc("4.3.2.1"), eidToLocators.getLocatorRecord().get(0));
        assertLocator(LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:1"), eidToLocators.getLocatorRecord().get(1));
        assertLocator(LispAddressUtil.asIpv4Rloc("1.8.2.7"), eidToLocators.getLocatorRecord().get(2));
    }

    @Test
    @Ignore
    public void handleMapRequest__MultipleEIDs() throws Exception {

        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v4Address).build());
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(v6Address).build());

        MappingRecordBuilder record1 = getDefaultMappingRecordBuilder();
        record1.setEid(v4Address);
        record1.setRecordTtl(100);

        LocatorRecordBuilder locator1 = getDefaultLocatorBuilder();
        locator1.setRloc(LispAddressUtil.asIpv4Rloc("4.3.2.1"));
        record1.getLocatorRecord().add(locator1.build());

        MappingRecordBuilder record2 = getDefaultMappingRecordBuilder();
        record2.setEid(v6Address);
        record2.setRecordTtl(100);

        LocatorRecordBuilder locator2 = getDefaultLocatorBuilder();
        locator2.setRloc(LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:1"));
        record2.getLocatorRecord().add(locator2.build());

        prepareMapping(record1.build(), record2.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        MappingRecord eidToLocators1 = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(1, eidToLocators1.getLocatorRecord().size());
        assertLocator(LispAddressUtil.asIpv4Rloc("4.3.2.1"), eidToLocators1.getLocatorRecord().get(0));

        MappingRecord eidToLocators2 = mapReply.getMappingRecordItem().get(1).getMappingRecord();
        assertEquals(1, eidToLocators2.getLocatorRecord().size());
        assertLocator(LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:1"), eidToLocators2.getLocatorRecord().get(0));
    }

    private void assertLocator(Rloc expectedAddress, LocatorRecord locatorRecord) {
        assertEquals(expectedAddress, locatorRecord.getRloc());
        Assert.assertTrue(locatorRecord.isRouted());
    }

    private Map<String, MappingRecord> prepareMapping(MappingRecord... records) {
        if (records.length > 0) {
            for (MappingRecord eidToLocatorRecord : records) {
                Map<String, MappingRecord> result = new HashMap<String, MappingRecord>();
                result.put(SubKeys.RECORD, eidToLocatorRecord);

                daoResults.put(eidToLocatorRecord.getEid(), result);
            }
        }

        ValueSaverAction<Eid> daoGetSaverAction = new ValueSaverAction<Eid>() {
            @Override
            protected boolean validate(Eid value) {
                return true;
            }

            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                return daoResults.get(lastValue);
            }
        };

        allowing(dao).get(with(daoGetSaverAction));
        will(daoGetSaverAction);
        allowing(dao).getSpecific(wany(Eid.class), with(SubKeys.AUTH_KEY));

        return daoResults.get(v4Address);
    }

    private MapRequestBuilder getDefaultMapRequestBuilder() {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setAuthoritative(false);
        mrBuilder.setEidItem(new ArrayList<EidItem>());
        mrBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mrBuilder.setMapDataPresent(true);
        mrBuilder.setNonce((long) 0);
        mrBuilder.setPitr(false);
        mrBuilder.setProbe(false);
        mrBuilder.setSmr(false);
        mrBuilder.setSmrInvoked(true);
        mrBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid("127.0.0.1")).build());
        return mrBuilder;
    }

    private MappingRecordBuilder getDefaultMappingRecordBuilder() {
        MappingRecordBuilder builder = new MappingRecordBuilder();
        builder.setAction(Action.NoAction);
        builder.setAuthoritative(false);
        builder.setLocatorRecord(new ArrayList<LocatorRecord>());
        builder.setMapVersion((short) 0);
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
