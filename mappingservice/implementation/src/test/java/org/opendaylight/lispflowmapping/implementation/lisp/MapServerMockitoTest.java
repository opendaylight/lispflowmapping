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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list
        .MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregistermessage.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container
        .MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container
        .MappingAuthkeyBuilder;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;

@RunWith(MockitoJUnitRunner.class)
public class MapServerMockitoTest {

    @Mock private static IMappingService mapService;
    @Mock private static IMapNotifyHandler notifyHandler;
    @Mock private static NotificationService notificationService;
    @Spy private static Set<SubscriberRLOC> subscriberSetMock = new HashSet<>();
    private static MapServer mapServer;
    private static MapRegister mapRegister;

    private static final String IPV4_STRING =        "1.2.3.0";
    private static final String IPV4_STRING_2 =      "1.2.4.0";
    private static final String IPV4_STRING_3 =      "192.168.0.1";
    private static final String IPV4_STRING_4 =      "192.168.0.2";
    private static final String IPV4_SOURCE_STRING = "127.0.0.1";

    private static final Eid IPV4_EID = LispAddressUtil.asIpv4Eid(IPV4_STRING);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid IPV4_EID_3 = LispAddressUtil.asIpv4Eid(IPV4_STRING_3);
    private static final Rloc RLOC = LispAddressUtil.asIpv4Rloc(IPV4_STRING);
    private static final Rloc RLOC_2 = LispAddressUtil.asIpv4Rloc(IPV4_STRING_2);
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder().setKeyString("password")
            .setKeyType(0).build();
    private static final SubscriberRLOC SUBSCRIBER_RLOC = new SubscriberRLOC(RLOC,
            LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING), new Date(86400000L * 2)); // timedOut() == true
    private static final SubscriberRLOC SUBSCRIBER_RLOC_2 = new SubscriberRLOC(RLOC_2,
            LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING), new Date(1L)); // timedOut() == false
    private static final ConfigIni CONFIG_INI = ConfigIni.getInstance();

    private static final MappingRecord OLD_MAPPING_RECORD = getDefaultMappingRecordBuilder().setEid(IPV4_EID_2).build();
    private static final MappingRecord OLD_MAPPING_RECORD_2 = getDefaultMappingRecordBuilder()
            .setEid(IPV4_EID_3).build();

    @Before
    public void init() {
        mapServer = new MapServer(mapService, true, true, notifyHandler, notificationService);
        subscriberSetMock.add(SUBSCRIBER_RLOC);
        subscriberSetMock.add(SUBSCRIBER_RLOC_2);
        mapRegister = getDefaultMapRegisterBuilder().build();
    }

    @Test
    public void handleMapRegisterTest_MappingMergeFalse() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(false);

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID)).thenReturn(OLD_MAPPING_RECORD);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);

        mapServer.handleMapRegister(mapRegister);

        Mockito.verify(mapService).addMapping(MappingOrigin.Southbound, IPV4_EID, mapRegister.getSiteId(),
                mapRegister.getMappingRecordItem().iterator().next().getMappingRecord());
        Mockito.verify(mapService).addData(MappingOrigin.Southbound, IPV4_EID, SubKeys.SUBSCRIBERS, subscriberSetMock);
        Mockito.verify(notifyHandler).handleMapNotify(getDefaultMapNotifyBuilder(mapRegister).build(), null);

        assertEquals(0, subscriberSetMock.size());
    }

    @Test
    public void handleMapRegisterTest_MappingMergeTrue() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);

        final MappingRecordItemBuilder mappingRecordItemBuilder = new MappingRecordItemBuilder()
                .setMappingRecord(OLD_MAPPING_RECORD);
        final MapNotifyBuilder mapNotifyBuilder = getDefaultMapNotifyBuilder(mapRegister)
                .setMappingRecordItem(new ArrayList<>());
        mapNotifyBuilder.getMappingRecordItem().add(mappingRecordItemBuilder.build());

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID))
                .thenReturn(OLD_MAPPING_RECORD);

        mapServer.handleMapRegister(mapRegister);

        Mockito.verify(mapService).addMapping(MappingOrigin.Southbound, IPV4_EID, mapRegister.getSiteId(),
                mapRegister.getMappingRecordItem().iterator().next().getMappingRecord());
        Mockito.verify(notifyHandler).handleMapNotify(mapNotifyBuilder.build(), null);
    }

    @Test
    public void handleMapRegisterTest_verifyTransportAddresses() throws NoSuchFieldException, IllegalAccessException {
        setConfigIniMappingMergeField(true);
        // input
        Set<IpAddress> sourceRlocs = getIpAddressSet();
        Mockito.when(mapService.getAuthenticationKey(IPV4_EID)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID))
                .thenReturn(OLD_MAPPING_RECORD)
                .thenReturn(OLD_MAPPING_RECORD_2)
                .thenReturn(getDefaultMappingRecordBuilder().build());
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID, SubKeys.SRC_RLOCS)).thenReturn(sourceRlocs);

        // result
        final List<TransportAddress> transportAddressList = getTransportAddressList();
        final MapNotifyBuilder mapNotifyBuilder = getDefaultMapNotifyBuilder(mapRegister);
        mapNotifyBuilder.setMappingRecordItem(new ArrayList<>());
        mapNotifyBuilder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                .setMappingRecord(getDefaultMappingRecordBuilder().build()).build());

        mapServer.handleMapRegister(mapRegister);

        Mockito.verify(notifyHandler).handleMapNotify(mapNotifyBuilder.build(), transportAddressList);
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

    private static MappingRecordItemBuilder getDefaultMappingRecordItemBuilder() {
        return new MappingRecordItemBuilder()
                .setMappingRecordItemId("mapping-record-item-id")
                .setKey(new MappingRecordItemKey("mapping-record-item-key"))
                .setMappingRecord(getDefaultMappingRecordBuilder().build());
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(false)
                .setLocatorRecord(new ArrayList<>())
                .setMapVersion((short) 0)
                .setMaskLength((short) 32)
                .setRecordTtl(60)
                .setEid(IPV4_EID);
    }

    private static MapNotifyBuilder getDefaultMapNotifyBuilder(MapRegister mapRegister) {
        final MapNotifyBuilder mapNotifyBuilder = new MapNotifyBuilder()
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

    private static Set<IpAddress> getIpAddressSet() {
        final IpAddress ipAddress = new IpAddress(new Ipv4Address(IPV4_STRING_3));
        final IpAddress ipAddress_2 = new IpAddress(new Ipv4Address(IPV4_STRING_4));
        final Set<IpAddress> addressSet = Sets.newHashSet(ipAddress, ipAddress_2);

        return addressSet;
    }

    private static List<TransportAddress> getTransportAddressList() {
        TransportAddressBuilder transportAddressBuilder_1 = new TransportAddressBuilder()
                .setIpAddress(new IpAddress(new Ipv4Address(IPV4_STRING_3)))
                .setPort(new PortNumber(LispMessage.PORT_NUM));

        TransportAddressBuilder transportAddressBuilder_2 = new TransportAddressBuilder()
                .setIpAddress(new IpAddress(new Ipv4Address(IPV4_STRING_4)))
                .setPort(new PortNumber(LispMessage.PORT_NUM));

        final List<TransportAddress> transportAddressList = Lists.newArrayList(
                transportAddressBuilder_2.build(),
                transportAddressBuilder_1.build());

        return transportAddressList;
    }
}
