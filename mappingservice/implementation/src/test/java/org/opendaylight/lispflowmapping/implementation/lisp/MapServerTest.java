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
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregistermessage.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

@RunWith(MockitoJUnitRunner.class)
public class MapServerTest {

    @Mock private static IMappingService mapService;
    @Mock private static IMapNotifyHandler notifyHandler;
    @Mock private static NotificationService notificationService;

    @Spy private static Set<SubscriberRLOC> subscriberSetMock_1 = new HashSet<>();
    @Spy private static Set<SubscriberRLOC> subscriberSetMock_2 = new HashSet<>();
    @Spy private static Set<SubscriberRLOC> subscriberSetMock_3 = new HashSet<>();
    private static MapServer mapServer;
    private static MapRegister mapRegister;
    private static MappingData mappingData;

    private static final String IPV4_STRING_1 =        "1.2.3.0";
    private static final String IPV4_STRING_2 =        "1.2.4.0";
    private static final String IPV4_STRING_3 =        "192.168.0.1";
    private static final String IPV4_STRING_4 =        "192.168.0.2";
    private static final String IPV4_STRING_5 =        "192.168.0.3";
    private static final String IPV4_STRING_6 =        "192.168.0.4";
    private static final String IPV4_SOURCE_STRING_1 = "127.0.0.1";
    private static final String IPV4_SOURCE_STRING_2 = "127.0.0.2";
    private static final String IPV4_SOURCE_STRING_3 = "127.0.0.3";
    private static final String IPV4_SOURCE_STRING_4 = "127.0.0.4";
    private static final String IPV4_SOURCE_STRING_5 = "127.0.0.5";
    private static final String IPV4_SOURCE_STRING_6 = "127.0.0.6";
    private static final String IPV4_PREFIX_1 =        "/24";
    private static final int MASK = 24;
    private static final int VNI = 10;

    private static final Eid IPV4_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixBinaryEid(IPV4_STRING_1 + IPV4_PREFIX_1);
    private static final Eid IPV4_SOURCE_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING_1);
    private static final Eid IPV4_SOURCE_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING_2);
    private static final Eid IPV4_SOURCE_EID_3 = LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING_3);
    private static final Eid IPV4_SOURCE_EID_4 = LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING_4);
    private static final Eid IPV4_SOURCE_EID_5 = LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING_5);
    private static final Eid IPV4_SOURCE_EID_6 = LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING_6);
    private static final Rloc RLOC_1 = LispAddressUtil.asIpv4Rloc(IPV4_STRING_1);
    private static final Rloc RLOC_2 = LispAddressUtil.asIpv4Rloc(IPV4_STRING_2);
    private static final Rloc RLOC_3 = LispAddressUtil.asIpv4Rloc(IPV4_STRING_3);
    private static final Rloc RLOC_4 = LispAddressUtil.asIpv4Rloc(IPV4_STRING_4);
    private static final Rloc RLOC_5 = LispAddressUtil.asIpv4Rloc(IPV4_STRING_5);
    private static final Rloc RLOC_6 = LispAddressUtil.asIpv4Rloc(IPV4_STRING_6);

    private static final IpAddressBinary IPV4_BINARY_1 =
            new IpAddressBinary(new Ipv4AddressBinary(new byte[] {1, 2, 3, 0}));
    private static final IpAddressBinary IPV4_BINARY_2 =
            new IpAddressBinary(new Ipv4AddressBinary(new byte[] {1, 2, 4, 0}));

    private static final long TWO_DAYS = 86400000L * 2;

    private static final SubscriberRLOC SUBSCRIBER_RLOC_1 = new SubscriberRLOC(RLOC_1,         // timedOut() == true
            IPV4_SOURCE_EID_1, SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT,
            new Date(System.currentTimeMillis() - TWO_DAYS));
    private static final SubscriberRLOC SUBSCRIBER_RLOC_2 = new SubscriberRLOC(RLOC_2,         // timedOut() == false
            IPV4_SOURCE_EID_2, SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT);
    private static final SubscriberRLOC SUBSCRIBER_RLOC_3 = new SubscriberRLOC(RLOC_3,         // timedOut() == true
            IPV4_SOURCE_EID_3, SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT,
            new Date(System.currentTimeMillis() - TWO_DAYS));
    private static final SubscriberRLOC SUBSCRIBER_RLOC_4 = new SubscriberRLOC(RLOC_4,         // timedOut() == false
            IPV4_SOURCE_EID_4, SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT);
    private static final SubscriberRLOC SUBSCRIBER_RLOC_5 = new SubscriberRLOC(RLOC_5,         // timedOut() == true
            IPV4_SOURCE_EID_5, SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT,
            new Date(System.currentTimeMillis() - TWO_DAYS));
    private static final SubscriberRLOC SUBSCRIBER_RLOC_6 = new SubscriberRLOC(RLOC_6,         // timedOut() == false
            IPV4_SOURCE_EID_6, SubscriberRLOC.DEFAULT_SUBSCRIBER_TIMEOUT);

    private static final Eid SOURCE_DEST_KEY_EID = LispAddressUtil
            .asSrcDstEid(IPV4_STRING_1, IPV4_STRING_2, MASK, MASK, VNI);
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder().setKeyType(0).build();
    private static final ConfigIni CONFIG_INI = ConfigIni.getInstance();

    private static final LocatorRecord LOCATOR_RECORD_1 = new LocatorRecordBuilder().setRloc(RLOC_1).build();
    private static final LocatorRecord LOCATOR_RECORD_2 = new LocatorRecordBuilder().setRloc(RLOC_2).build();

    private static final MappingRecord OLD_MAPPING_RECORD_1 = getDefaultMappingRecordBuilder()
            .setLocatorRecord(Lists.newArrayList(LOCATOR_RECORD_1)).build();
    private static final MappingRecord OLD_MAPPING_RECORD_2 = getDefaultMappingRecordBuilder()
            .setLocatorRecord(Lists.newArrayList(LOCATOR_RECORD_2)).build();
    private static final MappingData OLD_MAPPING_DATA_1 = new MappingData(OLD_MAPPING_RECORD_1);
    private static final MappingData OLD_MAPPING_DATA_2 = new MappingData(OLD_MAPPING_RECORD_2);

    private static final Set<IpAddressBinary> DEFAULT_IP_ADDRESS_SET = getDefaultIpAddressSet();

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException  {
        mapServer = new MapServer(mapService, true, notifyHandler, notificationService);
        subscriberSetMock_1.add(SUBSCRIBER_RLOC_1);
        subscriberSetMock_1.add(SUBSCRIBER_RLOC_2);
        subscriberSetMock_2.add(SUBSCRIBER_RLOC_3);
        subscriberSetMock_2.add(SUBSCRIBER_RLOC_4);
        subscriberSetMock_3.add(SUBSCRIBER_RLOC_5);
        subscriberSetMock_3.add(SUBSCRIBER_RLOC_6);
        mapRegister = getDefaultMapRegisterBuilder().build();
        mappingData = new MappingData(mapRegister.getMappingRecordItem().iterator().next().getMappingRecord(),
                System.currentTimeMillis());
        setConfigIniMappingMergeField(false);
    }

    @Test
    public void handleMapRegisterTest_MappingMergeFalse() throws NoSuchFieldException, IllegalAccessException {
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1)).thenReturn(OLD_MAPPING_DATA_1);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock_1);

        mappingData.setMergeEnabled(false);
        mapServer.handleMapRegister(mapRegister);

        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        Mockito.verify(mapService).addMapping(Mockito.eq(MappingOrigin.Southbound), Mockito.eq(IPV4_EID_1),
                Mockito.eq(mapRegister.getSiteId()), captor.capture());
        assertEquals(captor.getValue().getRecord(), mappingData.getRecord());
        Mockito.verify(mapService).addData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SUBSCRIBERS,
                subscriberSetMock_1);
        Mockito.verify(notifyHandler).handleMapNotify(getDefaultMapNotifyBuilder(mapRegister)
                .setAuthenticationData(null).build(), null);

        // only 1 subscriber has timed out.
        assertEquals(1, subscriberSetMock_1.size());
    }

    @Test
    public void handleMapRegisterTest_MappingMergeTrue() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordItemBuilder mappingRecordItemBuilder = new MappingRecordItemBuilder()
                .setMappingRecord(OLD_MAPPING_RECORD_1);
        final MapNotifyBuilder mapNotifyBuilder = getDefaultMapNotifyBuilder(mapRegister)
                .setMappingRecordItem(new ArrayList<>());
        mapNotifyBuilder.getMappingRecordItem().add(mappingRecordItemBuilder.build());

        // no mapping changes
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(OLD_MAPPING_DATA_1);

        mappingData.setMergeEnabled(true);
        mapServer.handleMapRegister(mapRegister);

        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        Mockito.verify(mapService).addMapping(Mockito.eq(MappingOrigin.Southbound), Mockito.eq(IPV4_EID_1),
                Mockito.eq(mapRegister.getSiteId()), captor.capture());
        assertEquals(captor.getValue().getRecord(), mappingData.getRecord());
        Mockito.verify(notifyHandler).handleMapNotify(mapNotifyBuilder.setAuthenticationData(null).build(), null);
    }

    @Test
    public void handleMapRegisterTest_findNegativeSubscribers() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        mapRegister.getMappingRecordItem().clear();
        mapRegister.getMappingRecordItem().add(getDefaultMappingRecordItemBuilder(IPV4_PREFIX_EID_1).build());

        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        final Eid maskedEid1 = LispAddressUtil.asIpv4Eid("1.2.0.0");

        final SubscriberRLOC subscriber1 = Mockito.mock(SubscriberRLOC.class);
        Mockito.when(subscriber1.timedOut()).thenReturn(true);
        Mockito.when(subscriber1.toString()).thenReturn("sub1");

        final Set<SubscriberRLOC> set1 = Sets.newHashSet(subscriber1);

        Mockito.when(mapService.getAuthenticationKey(IPV4_PREFIX_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_PREFIX_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);

        Mockito.when(mapService.getParentPrefix(IPV4_PREFIX_EID_1)).thenReturn(maskedEid1);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_PREFIX_EID_1, SubKeys.SUBSCRIBERS))
                .thenReturn(null);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, maskedEid1, SubKeys.SUBSCRIBERS)).thenReturn(set1);

        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_PREFIX_EID_1))
                .thenReturn(null)
                .thenReturn(getDefaultMappingData(mappingRecordBuilder.build()))
                .thenReturn(null);

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(subscriber1).timedOut();
    }

    @Test
    public void handleMapRegisterTest_verifyTransportAddresses() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        // input
        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(OLD_MAPPING_DATA_1)
                .thenReturn(OLD_MAPPING_DATA_2)
                .thenReturn(getDefaultMappingData(getDefaultMappingRecordBuilder().build()));
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock_1);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);

        // result
        final List<TransportAddress> transportAddressList = getTransportAddressList();
        final MapNotifyBuilder mapNotifyBuilder = getDefaultMapNotifyBuilder(mapRegister);
        mapNotifyBuilder.setMappingRecordItem(new ArrayList<>());
        mapNotifyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(getDefaultMappingRecordBuilder().build()).build());

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(notifyHandler).handleMapNotify(mapNotifyBuilder.build(), transportAddressList);
    }

    @Test
    public void handleMapRegisterTest_withTwoMappingRecords() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        // Input
        // Add a MappingRecord with SrcDestKey Eid Type
        final MappingRecordItemBuilder mappingRecordItemBuilder = new MappingRecordItemBuilder()
                .setMappingRecord(getDefaultMappingRecordBuilder().setEid(SOURCE_DEST_KEY_EID).build());
        final MapRegisterBuilder mapRegisterSrcDstBuilder = getDefaultMapRegisterBuilder();

        final List<MappingRecordItem> list = mapRegisterSrcDstBuilder.getMappingRecordItem();
        list.add(mappingRecordItemBuilder.build());

        // ------------- Stubbing for SourceDestKey type Eid mapping -------------------

        Mockito.when(mapService.getAuthenticationKey(SOURCE_DEST_KEY_EID)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, SOURCE_DEST_KEY_EID))
                // ensure mappings are different
                .thenReturn(OLD_MAPPING_DATA_1)
                .thenReturn(OLD_MAPPING_DATA_2)
                .thenReturn(OLD_MAPPING_DATA_2);
        // return a subscriberSet for SrcDestKeyEid MappingRecord
        Mockito.when(mapService.getData(MappingOrigin.Southbound, SOURCE_DEST_KEY_EID, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock_1);

        // return a subscriberSet for SrcDestKeyEid destination MappingRecord
        Mockito.when(mapService.getData(MappingOrigin.Southbound, SourceDestKeyHelper.getDstBinary(SOURCE_DEST_KEY_EID),
                SubKeys.SUBSCRIBERS)).thenReturn(subscriberSetMock_2);

        // ----------------- Stubbing for Ipv4 type Eid mapping ------------------------

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                // ensure mappings are different
                .thenReturn(OLD_MAPPING_DATA_1)
                .thenReturn(OLD_MAPPING_DATA_2);
        // return a subscriberSet for Ipv4Eid MappingRecord
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1,SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock_3);

        // -----------------------------------------------------------------------------

        // result
        mapServer.handleMapRegister(mapRegisterSrcDstBuilder.build());

        // for SrcDstKey mapping
        final ArgumentCaptor<MapRequest> captor_1 = ArgumentCaptor.forClass(MapRequest.class);
        Mockito.verify(notifyHandler, Mockito.times(1)).handleSMR(captor_1.capture(), Mockito.eq(RLOC_2));
        Mockito.verify(mapService).addData(MappingOrigin.Southbound, SOURCE_DEST_KEY_EID, SubKeys.SUBSCRIBERS,
                subscriberSetMock_1);
        final Eid resultEid_1 = captor_1.getValue().getEidItem().iterator().next().getEid();
        assertEquals(IPV4_SOURCE_EID_2, resultEid_1);

        // for SrcDst destination mapping
        final ArgumentCaptor<MapRequest> captor_2 = ArgumentCaptor.forClass(MapRequest.class);
        Mockito.verify(notifyHandler, Mockito.times(1)).handleSMR(captor_2.capture(), Mockito.eq(RLOC_4));
        Mockito.verify(mapService).addData(MappingOrigin.Southbound,
                SourceDestKeyHelper.getDstBinary(SOURCE_DEST_KEY_EID),
                SubKeys.SUBSCRIBERS, subscriberSetMock_2);
        final Eid resultEid_2 = captor_2.getValue().getEidItem().iterator().next().getEid();
        assertEquals(IPV4_SOURCE_EID_4, resultEid_2);

        // for Ipv4 mapping
        final ArgumentCaptor<MapRequest> captor_3 = ArgumentCaptor.forClass(MapRequest.class);
        Mockito.verify(notifyHandler, Mockito.times(1)).handleSMR(captor_3.capture(), Mockito.eq(RLOC_6));
        Mockito.verify(mapService).addData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SUBSCRIBERS,
                subscriberSetMock_3);
        final Eid resultEid_3 = captor_3.getValue().getEidItem().iterator().next().getEid();
        assertEquals(IPV4_SOURCE_EID_6, resultEid_3);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mappingChangedTest_withDifferentEid() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordBuilder mappingRecordBuilder_1 = getDefaultMappingRecordBuilder()
                // apply the change
                .setEid(IPV4_EID_2);
        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);

        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_1.build()))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_2.build()))
                .thenReturn(null);

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(notifyHandler).handleMapNotify(Mockito.any(MapNotify.class), captor.capture());
        // verify that a list of transport addresses has 2 values - happens only if mappingUpdated == true
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mappingChangedTest_withDifferentRLOC() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordBuilder mappingRecordBuilder_1 = getDefaultMappingRecordBuilder();
        // apply the change
        mappingRecordBuilder_1.getLocatorRecord().add(new LocatorRecordBuilder().build());
        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);

        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_1.build()))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_2.build()))
                .thenReturn(null);

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(notifyHandler).handleMapNotify(Mockito.any(MapNotify.class), captor.capture());
        // verify that a list of transport addresses has 2 values - happens only if mappingUpdated == true
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mappingChangedTest_withDifferentAction() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordBuilder mappingRecordBuilder_1 = getDefaultMappingRecordBuilder()
                // apply the change
                .setAction(MappingRecord.Action.NativelyForward);
        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);

        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_1.build()))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_2.build()))
                .thenReturn(null);

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(notifyHandler).handleMapNotify(Mockito.any(MapNotify.class), captor.capture());
        // verify that a list of transport addresses has 2 values - happens only if mappingUpdated == true
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mappingChangedTest_withDifferentTTL() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordBuilder mappingRecordBuilder_1 = getDefaultMappingRecordBuilder()
                // apply the change
                .setRecordTtl(10);
        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);

        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_1.build()))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_2.build()))
                .thenReturn(null);

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(notifyHandler).handleMapNotify(Mockito.any(MapNotify.class), captor.capture());
        // verify that a list of transport addresses has 2 values - happens only if mappingUpdated == true
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mappingChangedTest_withDifferentMapVersion() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordBuilder mappingRecordBuilder_1 = getDefaultMappingRecordBuilder()
                // apply the change
                .setMapVersion((short) 10);
        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);


        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_1.build()))
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_2.build()))
                .thenReturn(null);

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(notifyHandler).handleMapNotify(Mockito.any(MapNotify.class), captor.capture());
        // verify that a list of transport addresses has 2 values - happens only if mappingUpdated == true
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mappingChangedTest_withNullMap() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordBuilder mappingRecordBuilder_2 = getDefaultMappingRecordBuilder();
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID_1)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID_1, SubKeys.SRC_RLOCS))
                .thenReturn(DEFAULT_IP_ADDRESS_SET);

        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID_1))
                .thenReturn(null)
                .thenReturn(getDefaultMappingData(mappingRecordBuilder_2.build()))
                .thenReturn(null);

        mapServer.handleMapRegister(mapRegister);
        Mockito.verify(notifyHandler).handleMapNotify(Mockito.any(MapNotify.class), captor.capture());
        // verify that a list of transport addresses has 2 values - happens only if mappingUpdated == true
        assertEquals(2, captor.getValue().size());
    }

    private static MapRegisterBuilder getDefaultMapRegisterBuilder() {
        final MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder()
                .setProxyMapReply(true)
                .setWantMapNotify(true)
                .setKeyId((short) 0)
                .setMappingRecordItem(new ArrayList<>())
                .setMergeEnabled(true)
                .setNonce(1L)
                .setSiteId(new SiteId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7}))
                .setXtrId(new XtrId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}))
                .setXtrSiteIdPresent(true);
        mapRegisterBuilder.getMappingRecordItem().add(getDefaultMappingRecordItemBuilder().build());

        return mapRegisterBuilder;
    }

    private static MappingData getDefaultMappingData(MappingRecord mappingRecord) {
        if (mappingRecord == null) {
            mappingRecord = getDefaultMappingRecordBuilder().build();
        }
        return new MappingData(mappingRecord, System.currentTimeMillis());
    }

    private static MappingRecordItemBuilder getDefaultMappingRecordItemBuilder() {
        return getDefaultMappingRecordItemBuilder(IPV4_EID_1);
    }

    private static MappingRecordItemBuilder getDefaultMappingRecordItemBuilder(Eid eid) {
        return new MappingRecordItemBuilder()
                .setMappingRecordItemId("mapping-record-item-id")
                .setKey(new MappingRecordItemKey("mapping-record-item-key"))
                .setMappingRecord(getDefaultMappingRecordBuilder(eid).build());
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return getDefaultMappingRecordBuilder(IPV4_EID_1);
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder(Eid eid) {
        return new MappingRecordBuilder()
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(false)
                .setLocatorRecord(new ArrayList<>())
                .setMapVersion((short) 0)
                .setRecordTtl(60)
                .setEid(eid);
    }

    private static MapNotifyBuilder getDefaultMapNotifyBuilder(MapRegister mapRegister) {
        final MapNotifyBuilder mapNotifyBuilder = new MapNotifyBuilder()
                .setXtrSiteIdPresent(mapRegister.isXtrSiteIdPresent())
                .setSiteId(mapRegister.getSiteId())
                .setXtrId(mapRegister.getXtrId())
                .setNonce(mapRegister.getNonce())
                .setKeyId(mapRegister.getKeyId())
                .setMergeEnabled(mapRegister.isMergeEnabled())
                .setMappingRecordItem(new ArrayList<>())
                .setAuthenticationData(new byte[]{});
        mapNotifyBuilder.getMappingRecordItem().add(getDefaultMappingRecordItemBuilder().build());

        return mapNotifyBuilder;
    }

    private static void setConfigIniMappingMergeField(boolean value) throws NoSuchFieldException,
            IllegalAccessException {
        final Field mappingMergeField = CONFIG_INI.getClass().getDeclaredField("mappingMerge");
        mappingMergeField.setAccessible(true);
        mappingMergeField.setBoolean(CONFIG_INI, value);
    }

    private static Set<IpAddressBinary> getDefaultIpAddressSet() {
        final Set<IpAddressBinary> addressSet = Sets.newHashSet(IPV4_BINARY_1, IPV4_BINARY_2);

        return addressSet;
    }

    private static List<TransportAddress> getTransportAddressList() {
        TransportAddressBuilder transportAddressBuilder1 = new TransportAddressBuilder()
                .setIpAddress(IPV4_BINARY_1)
                .setPort(new PortNumber(LispMessage.PORT_NUM));

        TransportAddressBuilder transportAddressBuilder2 = new TransportAddressBuilder()
                .setIpAddress(IPV4_BINARY_2)
                .setPort(new PortNumber(LispMessage.PORT_NUM));

        final List<TransportAddress> transportAddressList = Lists.newArrayList(
                transportAddressBuilder1.build(),
                transportAddressBuilder2.build());

        return transportAddressList;
    }
}
