/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

public class MapResolverTest {

    private static MapResolver mapResolver;
    private static MappingService mapServiceMock;
    private static Set<Subscriber> subscriberSetMock;
    private static LispMappingService lispMappingServiceMock;

    private static final String ITR_RLOC_KEY_STRING =   "itr_rloc_key";
    private static final String ITR_RLOC_ID_STRING =    "itr_rloc_id";
    private static final String IPV4_STRING_1 =         "1.2.3.0";
    private static final String IPV4_STRING_2 =         "1.2.4.0";
    private static final String IPV4_RLOC_STRING_1 =    "100.100.100.100";
    private static final String IPV4_SOURCE =           "127.0.0.1";
    private static final String IPV4_PREFIX_STRING =    "/24";
    private static final String IPV6_STRING =           "0:0:0:0:0:0:0:1";
    private static final String IPV6_PREFIX_STRING =    "/128";

    private static final LocatorRecordKey LOCATOR_RECORD_KEY = new LocatorRecordKey("key");
    private static final int TTL_RLOC_TIMED_OUT = 1;

    private static final IpAddress IPV4_ADDRESS_1 = new IpAddress(new Ipv4Address(IPV4_STRING_1));
    private static final IpAddress IPV4_ADDRESS_2 = new IpAddress(new Ipv4Address(IPV4_STRING_2));

    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final Eid IPV6_PREFIX_EID = LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_PREFIX_STRING);

    private static final Rloc RLOC_1 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1);
    private static MapRequestBuilder mapRequestBuilder = getDefaultMapRequestBuilder();

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        mapServiceMock = Mockito.mock(MappingService.class, "mapService");
        subscriberSetMock = Mockito.mock(Set.class);
        lispMappingServiceMock = Mockito.mock(LispMappingService.class, "requestHandler");
        mapResolver = new MapResolver(mapServiceMock, true, ConfigIni.getInstance().getElpPolicy(),
                lispMappingServiceMock);
        mapRequestBuilder = getDefaultMapRequestBuilder();
    }

    /**
     * Tests {@link MapResolver#handleMapRequest} method.
     */
    @Test
    public void handleMapRequest__withSingleLocator() {
        // input mapping
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder();
        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder.build());
        final MappingData mappingData = getDefaultMappingData(mappingRecordBuilder.build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingData);

        // result
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(mappingRecordBuilder.build()).build());

        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
    }

    /**
     * Tests {@link MapResolver#handleMapRequest} method.
     */
    @Test
    public void handleMapRequest__withNoMapping() {
        final MappingRecordBuilder mappingRecordBuilder = new MappingRecordBuilder()
                .setAuthoritative(false)
                .setMapVersion((short) 0)
                .setEid(IPV4_PREFIX_EID_1)
                .setAction(LispMessage.NEGATIVE_MAPPING_ACTION)
                .setRecordTtl(TTL_RLOC_TIMED_OUT);

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(null);
        Mockito.when(mapServiceMock.addNegativeMapping(IPV4_PREFIX_EID_1))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder.build()));

        // result
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(mappingRecordBuilder.build()).build());

        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
    }

    /**
     * Tests {@link MapResolver#handleMapRequest} method.
     */
    @Test
    public void handleMapRequest__withSubscribers() {
        // input mapping
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder();
        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder.build());
        final MappingData mappingData = getDefaultMappingData(mappingRecordBuilder.build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingData);
        Mockito.when(mapServiceMock.getData(MappingOrigin.Southbound, IPV4_PREFIX_EID_1, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);
        Mockito.when(subscriberSetMock.contains(Mockito.any(Subscriber.class))).thenReturn(false);

        // result
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(mappingRecordBuilder.build()).build());

        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
        Mockito.verify(subscriberSetMock, Mockito.never()).remove(Mockito.any(Subscriber.class));
    }

    /**
     * Tests {@link MapResolver#handleMapRequest} method.
     */
    @Test
    public void handleMapRequest_withBothPolicy() {
        mapResolver = new MapResolver(mapServiceMock, true, "both", lispMappingServiceMock);

        final List<IpAddress> ipAddressList = new ArrayList<>();
        ipAddressList.add(IPV4_ADDRESS_1); // hop 1
        ipAddressList.add(IPV4_ADDRESS_2); // hop 2

        final Rloc rloc = LispAddressUtil.asTeLcafRloc(ipAddressList);
        final LocatorRecordBuilder locatorRecordBuilder_1 = getDefaultLocatorBuilder();
        final LocatorRecordBuilder locatorRecordBuilder_2 = getDefaultLocatorBuilder().setRloc(rloc);

        // input mapping
        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_1.build());
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_2.build());
        final MappingData mappingData = getDefaultMappingData(mappingRecordBuilder.build());

        final MapRequestBuilder mapRequestBuilder = getDefaultMapRequestBuilder();
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_1))
                .build());
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_2))
                .build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingData);

        // result
        final LocatorRecordBuilder locatorRecordBuilder_3 = getDefaultLocatorBuilder()
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_2)).setPriority((short) 1); // priority increased by 1
        final MappingRecordBuilder resultMappingRecordBuilder = getDefaultMappingRecordBuilder();

        resultMappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_1.build()); // as Ipv4
        resultMappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_2.build()); // as ELP
        resultMappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_3.build()); // added to the result

        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(resultMappingRecordBuilder.build()).build());

        // invocation
        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
    }

    /**
     * Tests {@link MapResolver#handleMapRequest} method.
     */
    @Test
    public void handleMapRequest_withReplacePolicy() {
        mapResolver = new MapResolver(mapServiceMock, true, "replace", lispMappingServiceMock);

        final List<IpAddress> ipAddressList = new ArrayList<>();
        ipAddressList.add(IPV4_ADDRESS_1); // hop 1
        ipAddressList.add(IPV4_ADDRESS_2); // hop 2

        final Rloc rloc = LispAddressUtil.asTeLcafRloc(ipAddressList);
        final LocatorRecordBuilder locatorRecordBuilder_1 = getDefaultLocatorBuilder();
        final LocatorRecordBuilder locatorRecordBuilder_2 = getDefaultLocatorBuilder().setRloc(rloc);

        // input mapping
        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_1.build());
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_2.build());
        final MappingData mappingData = getDefaultMappingData(mappingRecordBuilder.build());

        final MapRequestBuilder mapRequestBuilder = getDefaultMapRequestBuilder();
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_1))
                .build());
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_2))
                .build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingData);

        // result
        final LocatorRecordBuilder locatorRecordBuilder_3 = getDefaultLocatorBuilder()
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_2));
        final MappingRecordBuilder resultMappingRecordBuilder = getDefaultMappingRecordBuilder();

        resultMappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_1.build()); // as Ipv4
        resultMappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder_3.build()); // added to the result

        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(resultMappingRecordBuilder.build()).build());

        // invocation
        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
    }

    /**
     * Tests {@link MapResolver#handleMapRequest} method.
     */
    @Test
    public void handleMapRequest_withMultipleEids() {
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(IPV6_PREFIX_EID).build());

        final LocatorRecordBuilder locatorRecordBuilder_1 = getDefaultLocatorBuilder();
        final LocatorRecordBuilder locatorRecordBuilder_2 = getDefaultLocatorBuilder();
        locatorRecordBuilder_2.setRloc(LispAddressUtil.asIpv6Rloc(IPV6_STRING));

        //input mapping
        final MappingRecordBuilder mappingRecordBuilder_1 = getDefaultMappingRecordBuilder();
        mappingRecordBuilder_1.getLocatorRecord().add(locatorRecordBuilder_1.build());
        final MappingData mappingData_1 = getDefaultMappingData(mappingRecordBuilder_1.build());
        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        mappingRecordBuilder_2.getLocatorRecord().add(locatorRecordBuilder_2.build());
        mappingRecordBuilder_2.setEid(IPV6_PREFIX_EID);
        final MappingData mappingData_2 = getDefaultMappingData(mappingRecordBuilder_2.build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(),
                mapRequestBuilder.getEidItem().get(0).getEid())).thenReturn(mappingData_1);
        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(),
                mapRequestBuilder.getEidItem().get(1).getEid())).thenReturn(mappingData_2);

        //result
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem()
                .add(new MappingRecordItemBuilder().setMappingRecord(mappingRecordBuilder_1.build()).build());
        mapReplyBuilder.getMappingRecordItem()
                .add(new MappingRecordItemBuilder().setMappingRecord(mappingRecordBuilder_2.build()).build());

        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
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
                .setSmrInvoked(false)
                .setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(IPV4_SOURCE)).build())
                .setItrRloc(getDefaultItrRlocList());

        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(IPV4_PREFIX_EID_1).build());

        return mrBuilder;
    }

    private static MappingData getDefaultMappingData(MappingRecord mappingRecord) {
        if (mappingRecord == null) {
            mappingRecord = getDefaultMappingRecordBuilder().build();
        }
        return new MappingData(MappingOrigin.Southbound, mappingRecord, System.currentTimeMillis());
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(false)
                .setLocatorRecord(new ArrayList<>())
                .setMapVersion((short) 0)
                .setRecordTtl(60)
                .setEid(IPV4_PREFIX_EID_1);
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
                .setNonce(mapRequestBuilder.getNonce())
                .setMappingRecordItem(new ArrayList<>());
    }
}
