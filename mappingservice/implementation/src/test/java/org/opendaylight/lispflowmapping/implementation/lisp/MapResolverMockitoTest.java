/*
 * Copyright (c) 2016 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;

public class MapResolverMockitoTest {

    private static MapResolver mapResolver;
    private static ILispDAO daoMock;
    private static ILispDAO tableMock;
    private static ILispDAO srcDstMock;
    private static MappingService mapServiceMock;
    private static LispMappingService lispMappingserviceMock;

    private static final String ITR_RLOC_KEY_STRING =   "itr_rloc_key";
    private static final String ITR_RLOC_ID_STRING =    "itr_rloc_id";
    private static final String IPV4_STRING =           "1.2.3.0";
    private static final String IPV4_PREFIX_STRING =    "/24";
    private static final String IPV4_RLOC_STRING =      "4.3.2.1";
    // private static final String IPV4_RLOC_STRING_2 =    "4.3.2.2";
    private static final String IPV6_STRING =           "0:0:0:0:0:0:0:1";
    private static final String IPV6_PREFIX_STRING =    "/128";
    private static final LocatorRecordKey LOCATOR_RECORD_KEY = new LocatorRecordKey("key");

    private static final int TTL_RLOC_TIMED_OUT = 1;

    private static final Address IPV_4_PREFIX_ADDRESS = new Ipv4PrefixBuilder()
            .setIpv4Prefix(new Ipv4Prefix(IPV4_STRING + IPV4_PREFIX_STRING)).build();
    private static final Eid IPV_4_PREFIX_EID = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING + IPV4_PREFIX_STRING);
    // private static final Eid IPV_6_PREFIX_EID = LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_PREFIX_STRING);
    // private static final Rloc RLOC = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING);
    private static final MapRequestBuilder MAP_REQUEST_BUILDER = getDefaultMapRequestBuilder();

    @Before
    public void init() throws Exception {
        daoMock = Mockito.mock(ILispDAO.class, "dao");
        tableMock = Mockito.mock(ILispDAO.class, "table");
        srcDstMock = Mockito.mock(ILispDAO.class, "srcDst");
        mapServiceMock = Mockito.mock(MappingService.class, "mapService");
        lispMappingserviceMock = Mockito.mock(LispMappingService.class, "requestHandler");
        mapResolver = new MapResolver(mapServiceMock, true, ConfigIni.getInstance().getElpPolicy(),
                lispMappingserviceMock);
    }

    @Test
    public void handleMapRequest__withSingleLocator() throws Exception {
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder()
                .setKey(LOCATOR_RECORD_KEY)
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));

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

    private static Set<SubscriberRLOC> getDefaultSubscriberSet() {
        final SubscriberRLOC subscriberRLOC = new SubscriberRLOC(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING),
                IPV_4_PREFIX_EID);

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
                        .setAddress(IPV_4_PREFIX_ADDRESS).build()).build();
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
        MappingRecordBuilder builder = new MappingRecordBuilder()
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(false)
                .setLocatorRecord(new ArrayList<>())
                .setMapVersion((short) 0)
                .setRecordTtl(60)
                .setEid(IPV_4_PREFIX_EID);
        return builder;
    }

    private static LocatorRecordBuilder getDefaultLocatorBuilder() {
        LocatorRecordBuilder builder = new LocatorRecordBuilder()
                .setLocalLocator(false)
                .setMulticastPriority((short) 0)
                .setMulticastWeight((short) 0)
                .setPriority((short) 0)
                .setRlocProbed(false)
                .setRouted(true)
                .setWeight((short) 0)
                .setKey(LOCATOR_RECORD_KEY)
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING));

        return builder;
    }

    private static MapReplyBuilder getDefaultMapReplyBuilder() {
        MapReplyBuilder replyBuilder = new MapReplyBuilder();
        replyBuilder.setEchoNonceEnabled(false);
        replyBuilder.setProbe(false);
        replyBuilder.setSecurityEnabled(false);
        replyBuilder.setNonce(MAP_REQUEST_BUILDER.getNonce());
        replyBuilder.setMappingRecordItem(new ArrayList<>());
        return replyBuilder;
    }
}
