/*
 * Copyright (c) 2016 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list
        .MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container
        .MappingAuthkeyBuilder;

public class MapResolverMockitoTest {

    private static MapResolver mapResolver;
    // private static ILispDAO daoMock;
    // private static ILispDAO tableMock;
    // private static ILispDAO srcDstMock;
    private static MappingService mapServiceMock;
    private static Set<SubscriberRLOC> subscriberSetMock;
    private static LispMappingService lispMappingserviceMock;

    private static final String ITR_RLOC_KEY_STRING =   "itr_rloc_key";
    private static final String ITR_RLOC_ID_STRING =    "itr_rloc_id";
    private static final String IPV4_STRING =           "1.2.3.0";
    private static final String IPV4_PREFIX_STRING =    "/24";
    private static final String IPV4_RLOC_STRING_1 =    "100.100.100.100";
    //private static final String IPV4_RLOC_STRING_2 =    "200.200.200.200";
    // private static final String IPV6_STRING =           "0:0:0:0:0:0:0:1";
    // private static final String IPV6_PREFIX_STRING =    "/128";
    private static final LocatorRecordKey LOCATOR_RECORD_KEY = new LocatorRecordKey("key");

    private static final int TTL_RLOC_TIMED_OUT = 1;

    // private static final Address IPV_4_PREFIX_ADDRESS = new Ipv4PrefixBuilder()
    //        .setIpv4Prefix(new Ipv4Prefix(IPV4_STRING + IPV4_PREFIX_STRING)).build();
    private static final Eid IPV_4_PREFIX_EID = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING + IPV4_PREFIX_STRING);
    // private static final Eid IPV_6_PREFIX_EID = LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_PREFIX_STRING);
    private static final Rloc RLOC_1 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1);
    // private static final Rloc RLOC_2 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_2);
    private static final MapRequestBuilder MAP_REQUEST_BUILDER = getDefaultMapRequestBuilder();
    private static final SubscriberRLOC SUBSCRIBER_RLOC_1 = new SubscriberRLOC(RLOC_1,
            MAP_REQUEST_BUILDER.getSourceEid().getEid());
    // private static final SubscriberRLOC SUBSCRIBER_RLOC_2 = new SubscriberRLOC(RLOC_2, IPV_4_PREFIX_EID);

    @Before
    public void init() throws Exception {
        // daoMock = Mockito.mock(ILispDAO.class, "dao");
        // tableMock = Mockito.mock(ILispDAO.class, "table");
        // srcDstMock = Mockito.mock(ILispDAO.class, "srcDst");
        mapServiceMock = Mockito.mock(MappingService.class, "mapService");
        subscriberSetMock = Mockito.mock(Set.class);
        lispMappingserviceMock = Mockito.mock(LispMappingService.class, "requestHandler");
        mapResolver = new MapResolver(mapServiceMock, true, ConfigIni.getInstance().getElpPolicy(),
                lispMappingserviceMock);
    }

    @Test
    public void handleMapRequest__withSingleLocator() throws Exception {
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder()
                .setKey(LOCATOR_RECORD_KEY)
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1));

        final MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.getLocatorRecord().add(locatorRecordBuilder.build());
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(recordBuilder.build()).build());

        Mockito.when(mapServiceMock.getMapping(MAP_REQUEST_BUILDER.getSourceEid().getEid(), IPV_4_PREFIX_EID))
                .thenReturn(recordBuilder.build());

        mapResolver.handleMapRequest(MAP_REQUEST_BUILDER.build());
        Mockito.verify(lispMappingserviceMock).handleMapReply(mapReplyBuilder.build());
    }

    @Test
    public void handleMapRequest__withNoMapping() throws Exception {
        final MappingRecordBuilder recordBuilder = new MappingRecordBuilder()
                .setAuthoritative(false)
                .setMapVersion((short) 0)
                .setEid(IPV_4_PREFIX_EID)
                .setAction(MappingRecord.Action.NativelyForward)
                .setRecordTtl(TTL_RLOC_TIMED_OUT);

        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(recordBuilder.build()).build());

        Mockito.when(mapServiceMock.getMapping(MAP_REQUEST_BUILDER.getSourceEid().getEid(), IPV_4_PREFIX_EID))
                .thenReturn(null);
        Mockito.when(mapServiceMock.getAuthenticationKey(IPV_4_PREFIX_EID))
                .thenReturn(new MappingAuthkeyBuilder().build());

        mapResolver.handleMapRequest(MAP_REQUEST_BUILDER.build());
        Mockito.verify(lispMappingserviceMock).handleMapReply(mapReplyBuilder.build());
    }

    @Test
    public void handleMapRequest__withSubscribers() throws Exception {
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder()
                .setKey(LOCATOR_RECORD_KEY)
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1));

        final MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.getLocatorRecord().add(locatorRecordBuilder.build());
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(recordBuilder.build()).build());

        Mockito.when(mapServiceMock.getMapping(MAP_REQUEST_BUILDER.getSourceEid().getEid(), IPV_4_PREFIX_EID))
                .thenReturn(recordBuilder.build());
        Mockito.when(mapServiceMock.getData(MappingOrigin.Southbound, IPV_4_PREFIX_EID, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);
        Mockito.when(subscriberSetMock.contains(Mockito.any(SubscriberRLOC.class))).thenReturn(false);

        mapResolver.handleMapRequest(MAP_REQUEST_BUILDER.build());
        Mockito.verify(lispMappingserviceMock).handleMapReply(mapReplyBuilder.build());
        Mockito.verify(subscriberSetMock, Mockito.never()).remove(Mockito.any(SubscriberRLOC.class));
    }

    @Test
    public void handleMapRequest__withSubscribersToRemove() throws Exception {
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder()
                .setKey(LOCATOR_RECORD_KEY)
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1));

        final MappingRecordBuilder recordBuilder = getDefaultMappingRecordBuilder();
        recordBuilder.getLocatorRecord().add(locatorRecordBuilder.build());
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(recordBuilder.build()).build());

        Mockito.when(mapServiceMock.getMapping(MAP_REQUEST_BUILDER.getSourceEid().getEid(), IPV_4_PREFIX_EID))
                .thenReturn(recordBuilder.build());
        Mockito.when(mapServiceMock.getData(MappingOrigin.Southbound, IPV_4_PREFIX_EID, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);
        Mockito.when(subscriberSetMock.contains(new SubscriberRLOC(
                MAP_REQUEST_BUILDER.getItrRloc().get(0).getRloc(),
                MAP_REQUEST_BUILDER.getSourceEid().getEid())))
                .thenReturn(true);

        // check if a subscriber is re-instantiating when there already is one in the subscriber set
        mapResolver.handleMapRequest(MAP_REQUEST_BUILDER.build());
        Mockito.verify(subscriberSetMock).remove(SUBSCRIBER_RLOC_1);
        Mockito.verify(lispMappingserviceMock).handleMapReply(mapReplyBuilder.build());
    }

    private static List<ItrRloc> getDefaultItrRlocList() {
        final List<ItrRloc> itrRlocList = new ArrayList<>();
        final ItrRloc itrRloc = new ItrRlocBuilder()
                .setKey(new ItrRlocKey(ITR_RLOC_KEY_STRING))
                .setItrRlocId(ITR_RLOC_ID_STRING)
                .setRloc(RLOC_1).build();
        itrRlocList.add(itrRloc);

        return itrRlocList;
    }

    private static MapRequestBuilder getDefaultMapRequestBuilder() {
        MapRequestBuilder mrBuilder = new MapRequestBuilder()
                .setAuthoritative(false)
                .setEidItem(new ArrayList<>())
                .setItrRloc(new ArrayList<>())
                .setMapDataPresent(true)
                .setNonce((long) 0)
                .setPitr(false)
                .setProbe(false)
                .setSmr(false)
                .setSmrInvoked(true)
                .setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid("127.0.0.1")).build())
                .setItrRloc(getDefaultItrRlocList());

        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(IPV_4_PREFIX_EID).build());

        return mrBuilder;
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(false)
                .setLocatorRecord(new ArrayList<>())
                .setMapVersion((short) 0)
                .setRecordTtl(60)
                .setEid(IPV_4_PREFIX_EID);
    }

    private static LocatorRecordBuilder getDefaultLocatorBuilder() {
        return new LocatorRecordBuilder()
                .setLocalLocator(false)
                .setMulticastPriority((short) 0)
                .setMulticastWeight((short) 0)
                .setPriority((short) 0)
                .setRlocProbed(false)
                .setRouted(true)
                .setWeight((short) 0)
                .setKey(LOCATOR_RECORD_KEY)
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1));
    }

    private static MapReplyBuilder getDefaultMapReplyBuilder() {
        return new MapReplyBuilder()
                .setEchoNonceEnabled(false)
                .setProbe(false)
                .setSecurityEnabled(false)
                .setNonce(MAP_REQUEST_BUILDER.getNonce())
                .setMappingRecordItem(new ArrayList<>());
    }
}
