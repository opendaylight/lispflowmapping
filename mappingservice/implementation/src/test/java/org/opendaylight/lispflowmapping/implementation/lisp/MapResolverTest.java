/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.NoAddressAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.NoAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

public class MapResolverTest {

    private static MapResolver mapResolver;
    private static MappingService mapServiceMock;
    private static Set<SubscriberRLOC> subscriberSetMock;
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

    private static final byte[] IPV4_BINARY_BYTES_1 = new byte[]{1, 2, 3, 0};
    private static final byte[] IPV4_BINARY_BYTES_2 = new byte[]{1, 2, 5, 0};
    private static final byte[] IPV6_BINARY_BYTES_1 = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    private static final LocatorRecordKey LOCATOR_RECORD_KEY = new LocatorRecordKey("key");
    private static final int TTL_RLOC_TIMED_OUT = 1;

    private static final IpAddress IPV4_ADDRESS_1 = new IpAddress(new Ipv4Address(IPV4_STRING_1));
    private static final IpAddress IPV4_ADDRESS_2 = new IpAddress(new Ipv4Address(IPV4_STRING_2));

    private static final IpAddressBinary IPV4_ADDRESS_BINARY_1 =
            new IpAddressBinary(new Ipv4AddressBinary(IPV4_BINARY_BYTES_1));
    private static final IpAddressBinary IPV4_ADDRESS_BINARY_2 =
            new IpAddressBinary(new Ipv4AddressBinary(IPV4_BINARY_BYTES_2));
    private static final IpAddressBinary IPV6_ADDRESS_BINARY =
            new IpAddressBinary(new Ipv6AddressBinary(IPV6_BINARY_BYTES_1));

    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final Eid IPV4_PREFIX_EID_2 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_2 + IPV4_PREFIX_STRING);
    private static final Eid IPV6_PREFIX_EID = LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_PREFIX_STRING);
    private static final Eid SOURCE_DEST_KEY_EID = LispAddressUtil.asSrcDstEid(IPV4_SOURCE, IPV4_STRING_2, 24, 24, 0);

    private static final Address IPV4_ADDRESS = new Ipv4Builder().setIpv4(new Ipv4Address(IPV4_STRING_1)).build();
    private static final Address IPV6_ADDRESS = new Ipv6Builder().setIpv6(new Ipv6Address(IPV6_STRING)).build();

    private static final Rloc RLOC_1 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1);
    private static MapRequestBuilder mapRequestBuilder = getDefaultMapRequestBuilder();
    private static final SubscriberRLOC SUBSCRIBER_RLOC_1 = new SubscriberRLOC(RLOC_1,
            LispAddressUtil.asIpv4Eid(IPV4_SOURCE), SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT);

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

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingRecordBuilder.build());

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
                .setAction(MappingRecord.Action.NativelyForward)
                .setRecordTtl(TTL_RLOC_TIMED_OUT);

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(null);
        Mockito.when(mapServiceMock.getAuthenticationKey(IPV4_PREFIX_EID_1))
                .thenReturn(new MappingAuthkeyBuilder().build());
        Mockito.when(mapServiceMock.getWidestNegativePrefix(IPV4_PREFIX_EID_1)).thenReturn(IPV4_PREFIX_EID_1);

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

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingRecordBuilder.build());
        Mockito.when(mapServiceMock.getData(MappingOrigin.Southbound, IPV4_PREFIX_EID_1, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);
        Mockito.when(subscriberSetMock.contains(Mockito.any(SubscriberRLOC.class))).thenReturn(false);

        // result
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(mappingRecordBuilder.build()).build());

        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
        Mockito.verify(subscriberSetMock, Mockito.never()).remove(Mockito.any(SubscriberRLOC.class));
    }

    /**
     * Tests {@link MapResolver#isEqualIpVersion} method.
     */
    @Test
    public void isEqualIpVersionTest() {
        // input mapping
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder();
        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder.build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingRecordBuilder.build());
        Mockito.when(mapServiceMock.getData(MappingOrigin.Southbound, IPV4_PREFIX_EID_1, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);
        Mockito.when(subscriberSetMock.contains(Mockito.any(SubscriberRLOC.class))).thenReturn(false);

        // ----------------------
        // with sourceRloc = null
        List<ItrRloc> itrRlocList = Lists.newArrayList(
                newItrRloc(MacAfi.class, null),
                newItrRloc(Ipv4BinaryAfi.class, IPV4_ADDRESS));

        ArgumentCaptor<SubscriberRLOC> captor = ArgumentCaptor.forClass(SubscriberRLOC.class);
        mapResolver.handleMapRequest(mapRequestBuilder.setSourceRloc(null).setItrRloc(itrRlocList).build());
        Mockito.verify(subscriberSetMock).add(captor.capture());
        // Since mapRequest's sourceRloc is null, first ItrRloc from the itrRlocList must be used.
        assertEquals(MacAfi.class, captor.getValue().getSrcRloc().getAddressType());

        // ----------------------
        // with sourceRloc address = itrRloc address
        itrRlocList = Lists.newArrayList(
                newItrRloc(MacAfi.class, null),
                newItrRloc(Ipv4BinaryAfi.class, IPV4_ADDRESS));

        MapRequest mapRequest = mapRequestBuilder
                .setSourceRloc(IPV4_ADDRESS_BINARY_1)
                .setItrRloc(itrRlocList).build();

        captor = ArgumentCaptor.forClass(SubscriberRLOC.class);
        mapResolver.handleMapRequest(mapRequest);
        Mockito.verify(subscriberSetMock, Mockito.times(2)).add(captor.capture());
        assertEquals(IPV4_ADDRESS, captor.getValue().getSrcRloc().getAddress());

        // ----------------------
        // with sourceRloc address Afi = itrRloc address Afi (for Ipv4)
        itrRlocList = Lists.newArrayList(
                newItrRloc(MacAfi.class, null),
                newItrRloc(Ipv6Afi.class, IPV6_ADDRESS),
                newItrRloc(Ipv4Afi.class, IPV4_ADDRESS));

        mapRequest = mapRequestBuilder
                .setSourceRloc(IPV6_ADDRESS_BINARY)
                .setItrRloc(itrRlocList).build();

        captor = ArgumentCaptor.forClass(SubscriberRLOC.class);
        mapResolver.handleMapRequest(mapRequest);
        Mockito.verify(subscriberSetMock, Mockito.times(3)).add(captor.capture());
        assertEquals(IPV6_ADDRESS, captor.getValue().getSrcRloc().getAddress());

        // ----------------------
        // with sourceRloc address Afi = itrRloc address Afi (for Ipv6)
        itrRlocList = Lists.newArrayList(
                newItrRloc(MacAfi.class, null),
                newItrRloc(Ipv6Afi.class, IPV6_ADDRESS),
                newItrRloc(Ipv4Afi.class, IPV4_ADDRESS));

        mapRequest = mapRequestBuilder
                .setSourceRloc(IPV4_ADDRESS_BINARY_2)
                .setItrRloc(itrRlocList).build();

        captor = ArgumentCaptor.forClass(SubscriberRLOC.class);
        mapResolver.handleMapRequest(mapRequest);
        Mockito.verify(subscriberSetMock, Mockito.times(4)).add(captor.capture());
        assertEquals(IPV4_ADDRESS, captor.getValue().getSrcRloc().getAddress());

        // ----------------------
        // with no common ip address nor Afi
        final Mac mac = new MacBuilder().setMac(new MacAddress("aa:bb:cc:dd:ee:ff")).build();
        itrRlocList = Lists.newArrayList(
                newItrRloc(MacAfi.class, mac),
                newItrRloc(NoAddressAfi.class, Mockito.mock(NoAddress.class)));

        mapRequest = mapRequestBuilder
                .setSourceRloc(IPV4_ADDRESS_BINARY_1)
                .setItrRloc(itrRlocList).build();

        captor = ArgumentCaptor.forClass(SubscriberRLOC.class);
        mapResolver.handleMapRequest(mapRequest);
        Mockito.verify(subscriberSetMock, Mockito.times(5)).add(captor.capture());
        assertEquals(mac, captor.getValue().getSrcRloc().getAddress());
    }

    /**
     * Tests {@link MapResolver#handleMapRequest} method.
     */
    @Test
    public void handleMapRequest__withSubscribersToRemove() {
        // input mapping
        final LocatorRecordBuilder locatorRecordBuilder = getDefaultLocatorBuilder();
        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        mappingRecordBuilder.getLocatorRecord().add(locatorRecordBuilder.build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingRecordBuilder.build());
        Mockito.when(mapServiceMock.getData(MappingOrigin.Southbound, IPV4_PREFIX_EID_1, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);
        SubscriberRLOC subscriberRLOCMock = new SubscriberRLOC(
                mapRequestBuilder.getItrRloc().get(0).getRloc(),
                mapRequestBuilder.getSourceEid().getEid(), SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT);
        subscriberRLOCMock.setSubscriberTimeoutByRecordTtl(
                mappingRecordBuilder.getRecordTtl());
        Mockito.when(subscriberSetMock.contains(subscriberRLOCMock))
                .thenReturn(true);

        // result
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(mappingRecordBuilder.build()).build());

        // check if a subscriber is re-instantiating when there already is one in the subscriber set
        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(subscriberSetMock).remove(SUBSCRIBER_RLOC_1);
        Mockito.verify(subscriberSetMock).add(SUBSCRIBER_RLOC_1);
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
        Mockito.verify(mapServiceMock).addData(MappingOrigin.Southbound, IPV4_PREFIX_EID_1,
                SubKeys.SUBSCRIBERS, subscriberSetMock);

        // verify that itrRloc is subscribed to dst address
        mappingRecordBuilder.setEid(SOURCE_DEST_KEY_EID);
        mapRequestBuilder.getEidItem().add(new EidItemBuilder().setEid(IPV4_PREFIX_EID_2).build());
        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_2))
                .thenReturn(mappingRecordBuilder.build());

        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(mapServiceMock).getData(MappingOrigin.Southbound,
                SourceDestKeyHelper.getDstBinary(SOURCE_DEST_KEY_EID), SubKeys.SUBSCRIBERS);
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

        final MapRequestBuilder mapRequestBuilder = getDefaultMapRequestBuilder();
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_1))
                .build());
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_2))
                .build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingRecordBuilder.build());

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

        final MapRequestBuilder mapRequestBuilder = getDefaultMapRequestBuilder();
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_1))
                .build());
        mapRequestBuilder.getItrRloc().add(new ItrRlocBuilder().setRloc(LispAddressUtil.asIpv4Rloc(IPV4_STRING_2))
                .build());

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(), IPV4_PREFIX_EID_1))
                .thenReturn(mappingRecordBuilder.build());

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
        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        mappingRecordBuilder_2.getLocatorRecord().add(locatorRecordBuilder_2.build());
        mappingRecordBuilder_2.setEid(IPV6_PREFIX_EID);

        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(),
                mapRequestBuilder.getEidItem().get(0).getEid())).thenReturn(mappingRecordBuilder_1.build());
        Mockito.when(mapServiceMock.getMapping(mapRequestBuilder.getSourceEid().getEid(),
                mapRequestBuilder.getEidItem().get(1).getEid())).thenReturn(mappingRecordBuilder_2.build());

        //result
        final MapReplyBuilder mapReplyBuilder = getDefaultMapReplyBuilder();
        mapReplyBuilder.getMappingRecordItem()
                .add(new MappingRecordItemBuilder().setMappingRecord(mappingRecordBuilder_1.build()).build());
        mapReplyBuilder.getMappingRecordItem()
                .add(new MappingRecordItemBuilder().setMappingRecord(mappingRecordBuilder_2.build()).build());

        mapResolver.handleMapRequest(mapRequestBuilder.build());
        Mockito.verify(lispMappingServiceMock).handleMapReply(mapReplyBuilder.build());
    }

    private static ItrRloc newItrRloc(Class<? extends LispAddressFamily> clazz, Address address) {
        return new ItrRlocBuilder().setRloc(new RlocBuilder()
                .setAddress(address)
                .setAddressType(clazz).build()).build();
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
                .setSourceEid(new SourceEidBuilder().setEid(LispAddressUtil.asIpv4Eid(IPV4_SOURCE)).build())
                .setItrRloc(getDefaultItrRlocList());

        mrBuilder.getEidItem().add(new EidItemBuilder().setEid(IPV4_PREFIX_EID_1).build());

        return mrBuilder;
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
