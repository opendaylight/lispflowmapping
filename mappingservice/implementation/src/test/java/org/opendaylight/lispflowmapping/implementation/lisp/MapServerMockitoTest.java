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
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list
        .MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregistermessage.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container
        .MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container
        .MappingAuthkeyBuilder;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;

@RunWith(MockitoJUnitRunner.class)
public class MapServerMockitoTest {

    @Mock(name = "mapService") private static IMappingService mapService;
    @Mock(name = "notifyHandler") private static IMapNotifyHandler notifyHandler;
    @Mock(name = "notificationService") private static NotificationService notificationService;
    @Spy private static Set<SubscriberRLOC> subscriberSetMock = new HashSet<>();
    private static MapServer mapServer;

    private static final String IPV4_STRING = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";
    private static final String IPV4_SOURCE_STRING = "127.0.0.1";
    private static final Eid IPV4_EID = LispAddressUtil.asIpv4Eid(IPV4_STRING);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Rloc RLOC = LispAddressUtil.asIpv4Rloc(IPV4_STRING);
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder().setKeyString("password")
            .setKeyType(1).build();
    private static final SubscriberRLOC SUBSCRIBER_RLOC = new SubscriberRLOC(RLOC,
            LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING));

    @Before
    public void init() {
        mapServer = new MapServer(mapService, true, true, notifyHandler, notificationService);
        subscriberSetMock.add(SUBSCRIBER_RLOC);
    }

    @Test
    @Ignore
    public void handleMapRegisterTest() {
        System.setProperty("lisp.mappingMerge", "false");
        final MapRegister mapRegister = getDefaultMapRegisterBuilder().build();
        final MappingRecord oldMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_EID_2).build();

        Mockito.when(mapService.getAuthenticationKey(IPV4_EID)).thenReturn(MAPPING_AUTHKEY);
        Mockito.when(mapService.getMapping(MappingOrigin.Southbound, IPV4_EID)).thenReturn(oldMappingRecord);
        Mockito.when(mapService.getData(MappingOrigin.Southbound, IPV4_EID, SubKeys.SUBSCRIBERS))
                .thenReturn(subscriberSetMock);

        mapServer.handleMapRegister(mapRegister);

        Mockito.verify(mapService).addMapping(MappingOrigin.Southbound, IPV4_EID, mapRegister.getSiteId(),
                mapRegister.getMappingRecordItem().iterator().next());
        Mockito.verify(subscriberSetMock).remove(SUBSCRIBER_RLOC);
    }

    private static MapRegisterBuilder getDefaultMapRegisterBuilder() {
        MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder()
                .setAuthenticationData(new byte[]{0})
                .setProxyMapReply(true)
                .setWantMapNotify(true)
                .setKeyId((short) 1)
                .setMappingRecordItem(new ArrayList<>())
                .setMergeEnabled(true)
                .setNonce(1L)
                .setSiteId(new SiteId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7}))
                .setXtrId(new XtrId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}))
                .setAuthenticationData(new byte[]{0, 1, 2, 3, 4, 5, 6, 7})
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
}
