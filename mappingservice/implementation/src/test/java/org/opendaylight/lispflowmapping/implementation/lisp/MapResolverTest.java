/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import org.jmock.api.Invocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class MapResolverTest extends BaseTestCase {

    // private MapResolver testedMapResolver;
    private static LispMappingService testedMapResolver;
    private static ILispDAO daoMock;
    private static ILispDAO tableMock;
    private static ILispDAO srcDstMock;
    private static MappingService mapService;
    private static MapRequestBuilder mapRequest;

    private static final String ITR_RLOC_KEY_STRING = "itr_rloc_key";
    private static final String ITR_RLOC_ID_STRING = "itr_rloc_id";
    private static final String IPV4_STRING = "1.2.3.0";
    private static final String IPV4_PREFIX_STRING = "/24";
    private static final String IPV4_RLOC_STRING = "4.3.2.1";
    private static final String IPV4_RLOC_STRING_2 = "1.8.2.7";
    private static final String IPV6_STRING = "0:0:0:0:0:0:0:1";
    private static final String IPV6_PREFIX_STRING = "/128";
    private static final LocatorRecordKey LOCATOR_RECORD_KEY = new LocatorRecordKey("key");

    private static final Eid IPV4_ADDRESS = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING + IPV4_PREFIX_STRING);
    private static final Eid IPV6_ADDRESS = LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_PREFIX_STRING);

    private HashMap<Eid, Map<String, MappingRecord>> daoResults;

    @Override
    @Before
    public void before() throws Exception {
        super.before();

        daoMock = context.mock(ILispDAO.class);
        tableMock = context.mock(ILispDAO.class, "table");
        srcDstMock = context.mock(ILispDAO.class, "srcDst");

        // map-cache init and table creation
        allowing(daoMock).putTable(wany(String.class));will(returnValue(daoMock));

        MappingSystem mapSystem = new MappingSystem(daoMock, true, true, true);
        mapService = new MappingService();
        mapService.setDaoService(daoMock);
        inject(mapService, "mappingSystem", mapSystem);

        testedMapResolver = new LispMappingService();
        testedMapResolver.setMappingService(mapService);
        testedMapResolver.basicInit();

        mapRequest = new MapRequestBuilder();
        daoResults = new HashMap<>();
    }

    @Test
    // @Ignore
    public void handleMapRequest__ReplyWithSingleLocator() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.setItrRloc(getDefaultItrRlocList());
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV4_ADDRESS).build());

        final LocatorRecordBuilder locator = getDefaultLocatorBuilder()
                .setKey(LOCATOR_RECORD_KEY)
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));

        final MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder()
                .setEid(IPV4_ADDRESS);
        recordBuilder.getLocatorRecord().add(locator.build());
        final MappingRecord record = recordBuilder.build();

        final Map<String, Object> map = Maps.newConcurrentMap();
        map.put(SubKeys.LCAF_SRCDST, srcDstMock);
        map.put(SubKeys.RECORD, record);

        final Set<SubscriberRLOC> subscriberRLOCSet = getDefaultSubscriberSet();

        allowing(daoMock).getSpecific(0L, SubKeys.VNI); will(returnValue(tableMock));
        allowing(tableMock).get(IPV4_ADDRESS);
        will(returnValue(map));
        allowing(srcDstMock).get(wany(Eid.class)); will(returnValue(map));
        allowing(tableMock).getSpecific(MaskUtil.normalize(IPV4_ADDRESS), SubKeys.SUBSCRIBERS);
        will(returnValue(subscriberRLOCSet));
        allowing(tableMock).put(IPV4_ADDRESS, new MappingEntry<Object>(SubKeys.SUBSCRIBERS, subscriberRLOCSet));

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        List<LocatorRecord> list = eidToLocators.getLocatorRecord();
        assertEquals(1, list.size());
        LocatorRecord resultLocator = mapReply.getMappingRecordItem().get(0).getMappingRecord()
                .getLocatorRecord().get(0);
        assertEquals(locator.isLocalLocator(), resultLocator.isLocalLocator());
        assertEquals(locator.isRouted(), resultLocator.isRouted());
        assertEquals(locator.getMulticastPriority(), resultLocator.getMulticastPriority());
        assertEquals(locator.getMulticastWeight(), resultLocator.getMulticastWeight());
        assertEquals(locator.getPriority(), resultLocator.getPriority());
        assertEquals(locator.getWeight(), resultLocator.getWeight());
        assertEquals(LOCATOR_RECORD_KEY, resultLocator.getKey());
        assertEquals(locator.getRloc(), resultLocator.getRloc());

        assertLocator(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING), eidToLocators.getLocatorRecord().get(0));
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyBasicFields() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV4_ADDRESS).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(IPV4_ADDRESS);
        record.setRecordTtl(100);

        record.setAuthoritative(true);
        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        assertEquals(mapRequest.getNonce(), mapReply.getNonce());
        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(IPV4_ADDRESS, eidToLocators.getEid());
        assertEquals(record.isAuthoritative(), eidToLocators.isAuthoritative());
        assertEquals(record.getAction(), eidToLocators.getAction());
        assertEquals(record.getRecordTtl(), eidToLocators.getRecordTtl());
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMask() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV4_ADDRESS).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv4PrefixEid(IPV4_STRING + IPV4_PREFIX_STRING));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();
        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        assertEquals(mr.getNonce(), mapReply.getNonce());
        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(LispAddressUtil.asIpv4PrefixEid(IPV4_STRING + IPV4_PREFIX_STRING), eidToLocators.getEid());
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMaskIPv6() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV6_ADDRESS).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_PREFIX_STRING));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));
        record.getLocatorRecord().add(locator.build());
        prepareMapping(record.build());

        MapRequest mr = mapRequest.build();

        MapReply mapReply = testedMapResolver.handleMapRequest(mr);

        assertEquals(mr.getNonce(), mapReply.getNonce());
        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(IPV6_ADDRESS, eidToLocators.getEid());
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMaskIPv6NoMatch() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV6_ADDRESS).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_PREFIX_STRING));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));
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

        Map<String, MappingRecord> result = new HashMap<>();
        result.put(SubKeys.RECORD, null);

        MapReply mapReply = getNativelyForwardMapReply(mr, result);

        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(1, eidToLocators.getRecordTtl().intValue());
        assertEquals(Action.NativelyForward, eidToLocators.getAction());
    }

    @Ignore
    private MapReply getNativelyForwardMapReply(MapRequest mr, Map<String, MappingRecord> result) {
        allowing(daoMock).get(wany(Eid.class));
        ret(result);
        allowing(daoMock).getSpecific(wany(Eid.class), with(SubKeys.AUTH_KEY));
        ret("pass");
        MapReply mapReply = testedMapResolver.handleMapRequest(mr);
        return mapReply;
    }

    private MapRequest getDefaultMapRequest() {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV4_ADDRESS).build());
        MapRequest mr = mapRequest.build();
        return mr;
    }

    @Test
    @Ignore
    public void handleMapRequest__VerifyMaskNoMatch() throws Exception {
        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV4_ADDRESS).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(LispAddressUtil.asIpv4PrefixEid("1.2.4.0/24"));

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));
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
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV4_ADDRESS).build());

        MappingRecordBuilder record = getDefaultMappingRecordBuilder();
        record.setEid(IPV4_ADDRESS);

        LocatorRecordBuilder locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));
        record.getLocatorRecord().add(locator.build());
        locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv6Rloc(IPV6_STRING));
        record.getLocatorRecord().add(locator.build());
        locator = getDefaultLocatorBuilder();
        locator.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_2));
        record.getLocatorRecord().add(locator.build());

        prepareMapping(record.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        MappingRecord eidToLocators = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(3, eidToLocators.getLocatorRecord().size());

        assertLocator(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING), eidToLocators.getLocatorRecord().get(0));
        assertLocator(LispAddressUtil.asIpv6Rloc(IPV6_STRING), eidToLocators.getLocatorRecord().get(1));
        assertLocator(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_2), eidToLocators.getLocatorRecord().get(2));
    }

    @Test
    @Ignore
    public void handleMapRequest__MultipleEIDs() throws Exception {

        mapRequest = getDefaultMapRequestBuilder();
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV4_ADDRESS).build());
        mapRequest.getEidItem().add(new EidItemBuilder().setEid(IPV6_ADDRESS).build());

        MappingRecordBuilder record1 = getDefaultMappingRecordBuilder();
        record1.setEid(IPV4_ADDRESS);
        record1.setRecordTtl(100);

        LocatorRecordBuilder locator1 = getDefaultLocatorBuilder();
        locator1.setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));
        record1.getLocatorRecord().add(locator1.build());

        MappingRecordBuilder record2 = getDefaultMappingRecordBuilder();
        record2.setEid(IPV6_ADDRESS);
        record2.setRecordTtl(100);

        LocatorRecordBuilder locator2 = getDefaultLocatorBuilder();
        locator2.setRloc(LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:1"));
        record2.getLocatorRecord().add(locator2.build());

        prepareMapping(record1.build(), record2.build());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest.build());

        MappingRecord eidToLocators1 = mapReply.getMappingRecordItem().get(0).getMappingRecord();
        assertEquals(1, eidToLocators1.getLocatorRecord().size());
        assertLocator(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING), eidToLocators1.getLocatorRecord().get(0));

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
                Map<String, MappingRecord> result = new HashMap<>();
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

        allowing(daoMock).get(with(daoGetSaverAction));
        will(daoGetSaverAction);
        allowing(daoMock).getSpecific(wany(Eid.class), with(SubKeys.AUTH_KEY));

        return daoResults.get(IPV4_ADDRESS);
    }

    private static Set<SubscriberRLOC> getDefaultSubscriberSet() {
        final SubscriberRLOC subscriberRLOC = new SubscriberRLOC(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING),
                IPV4_ADDRESS);

        Set<SubscriberRLOC> subscriberRLOCSet = new HashSet<>();
        subscriberRLOCSet.add(subscriberRLOC);
        return subscriberRLOCSet;
    }

    private static List<ItrRloc> getDefaultItrRlocList() {
        final List<ItrRloc> itrRlocList = new ArrayList<>();
        final ItrRloc itrRloc = new ItrRlocBuilder()
                .setKey(new ItrRlocKey(ITR_RLOC_KEY_STRING))
                .setItrRlocId(ITR_RLOC_ID_STRING)
                .setRloc(new RlocBuilder()
                        .setAddress(IPV4_ADDRESS.getAddress()).build()).build();
        itrRlocList.add(itrRloc);

        return itrRlocList;
    }

    private static MapRequestBuilder getDefaultMapRequestBuilder() {
        MapRequestBuilder mrBuilder = new MapRequestBuilder();
        mrBuilder.setAuthoritative(false);
        mrBuilder.setEidItem(new ArrayList<>());
        mrBuilder.setItrRloc(new ArrayList<>());
        mrBuilder.setMapDataPresent(true);
        mrBuilder.setNonce((long) 0);
        mrBuilder.setPitr(false);
        mrBuilder.setProbe(false);
        mrBuilder.setSmr(false);
        mrBuilder.setSmrInvoked(true);
        mrBuilder.setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid("127.0.0.1")).build());
        return mrBuilder;
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        MappingRecordBuilder builder = new MappingRecordBuilder();
        builder.setAction(Action.NoAction);
        builder.setAuthoritative(false);
        builder.setLocatorRecord(new ArrayList<>());
        builder.setMapVersion((short) 0);
        builder.setRecordTtl(60);
        return builder;
    }

    private static LocatorRecordBuilder getDefaultLocatorBuilder() {
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
