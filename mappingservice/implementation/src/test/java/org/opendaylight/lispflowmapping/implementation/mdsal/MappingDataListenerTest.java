/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.lispflowmapping.implementation.util.MSNotificationInputUtil;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModified;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.VniUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifierKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

public class MappingDataListenerTest {
    private static final VirtualNetworkIdentifierKey VNI_KEY = new VirtualNetworkIdentifierKey(new VniUri("urn:foo"));
    private static final String IPV4_STRING_1 = "192.168.0.1";
    private static final String IPV4_STRING_2 = "192.168.0.2";
    private static final String IPV4_STRING_3 = "192.168.0.3";
    private static final Eid IPV4_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid IPV4_EID_3 = LispAddressUtil.asIpv4Eid(IPV4_STRING_3);

    private static final Mapping MAPPING_EID_1_NB = getDefaultMapping(IPV4_EID_1, MappingOrigin.Northbound);
    private static final Mapping MAPPING_EID_1_SB = getDefaultMapping(IPV4_EID_1, MappingOrigin.Southbound);
    private static final Mapping MAPPING_EID_2_NB = getDefaultMapping(IPV4_EID_2, MappingOrigin.Northbound);
    private static final Mapping MAPPING_EID_2_SB = getDefaultMapping(IPV4_EID_2, MappingOrigin.Southbound);
    private static final Mapping MAPPING_EID_3_NB = getDefaultMapping(IPV4_EID_3, MappingOrigin.Northbound);
    private static final Mapping MAPPING_EID_3_SB = getDefaultMapping(IPV4_EID_3, MappingOrigin.Southbound);

    private IMappingSystem mappingSystemMock;
    private NotificationPublishService notificationPublishServiceMock;

    private DataTreeModification<Mapping> changeDel;
    private DataTreeModification<Mapping> changeSubtreeModified;
    private DataTreeModification<Mapping> changeWrite;
    private DataObjectDeleted<Mapping> modDel;
    private DataObjectModified<Mapping> modSubtreeModified;
    private DataObjectWritten<Mapping> modWrite;

    private MappingDataListener mappingDataListener;

    @Before
    public void init() {
        final DataBroker dataBrokerMock = mock(DataBroker.class);
        mappingSystemMock = mock(IMappingSystem.class);
        notificationPublishServiceMock = mock(NotificationPublishService .class);
        mappingDataListener =
                new MappingDataListener(dataBrokerMock, mappingSystemMock, notificationPublishServiceMock);

        changeDel = mock(DataTreeModification.class);
        changeSubtreeModified = mock(DataTreeModification.class);
        changeWrite = mock(DataTreeModification.class);
        modDel = spy(DataObjectDeleted.class);
        modSubtreeModified = spy(DataObjectModified.class);
        modWrite = spy(DataObjectWritten.class);

        when(changeDel.path()).thenReturn(DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, VNI_KEY)
            .child(Mapping.class, MAPPING_EID_1_NB.key())
            .build());
        when(changeDel.getRootNode()).thenReturn(modDel);
        when(changeSubtreeModified.path()).thenReturn(DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, VNI_KEY)
            .child(Mapping.class, MAPPING_EID_2_NB.key())
            .build());
        when(changeSubtreeModified.getRootNode()).thenReturn(modSubtreeModified);
        when(changeWrite.path()).thenReturn(DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, VNI_KEY)
            .child(Mapping.class, MAPPING_EID_3_NB.key())
            .build());
        when(changeWrite.getRootNode()).thenReturn(modWrite);
        when(mappingSystemMock.isMaster()).thenReturn(true);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with DELETE modification type from northbound.
     */
    @Test
    public void onDataTreeChangedTest_delete_NB() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes = List.of(changeDel);
        when(modDel.dataBefore()).thenReturn(MAPPING_EID_1_NB);

        mappingDataListener.onDataTreeChanged(changes);
        verify(mappingSystemMock).removeMapping(MappingOrigin.Northbound, IPV4_EID_1);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with DELETE modification type from southbound.
     */
    @Test
    public void onDataTreeChangedTest_delete_SB() {
        final List<DataTreeModification<Mapping>> changes = List.of(changeDel);
        when(modDel.dataBefore()).thenReturn(MAPPING_EID_1_SB);

        mappingDataListener.onDataTreeChanged(changes);
        //verifyZeroInteractions(iMappingSystemMock);
        verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type from
     * northbound.
     */
    @Test
    @Ignore
    public void onDataTreeChangedTest_subtreeModified_NB() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes = List.of(changeSubtreeModified);
        final MappingChanged mapChanged = MSNotificationInputUtil.toMappingChanged(
                MAPPING_EID_2_NB.getMappingRecord(), null, null, null, MappingChange.Updated);
        when(modSubtreeModified.dataAfter()).thenReturn(MAPPING_EID_2_NB);

        mappingDataListener.onDataTreeChanged(changes);
        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        verify(mappingSystemMock).addMapping(eq(MappingOrigin.Northbound), eq(IPV4_EID_2), captor.capture());
        assertEquals(captor.getValue().getRecord(), MAPPING_EID_2_NB.getMappingRecord());
        verify(notificationPublishServiceMock).putNotification(mapChanged);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type from
     * southbound.
     */
    @Test
    public void onDataTreeChangedTest_subtreeModified_SB() {
        final List<DataTreeModification<Mapping>> changes = List.of(changeSubtreeModified);
        when(modSubtreeModified.dataAfter()).thenReturn(MAPPING_EID_2_SB);

        mappingDataListener.onDataTreeChanged(changes);
        //verifyZeroInteractions(iMappingSystemMock);
        verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with WRITE modification type from northbound.
     */
    @Test
    @Ignore
    public void onDataTreeChangedTest_write_NB() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes = List.of(changeWrite);
        final MappingChanged mapChanged = MSNotificationInputUtil.toMappingChanged(
                MAPPING_EID_3_NB.getMappingRecord(), null, null, null, MappingChange.Created);
        when(modWrite.dataAfter()).thenReturn(MAPPING_EID_3_NB);

        mappingDataListener.onDataTreeChanged(changes);
        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        verify(mappingSystemMock).addMapping(eq(MappingOrigin.Northbound), eq(IPV4_EID_3), captor.capture());
        assertEquals(captor.getValue().getRecord(), MAPPING_EID_3_NB.getMappingRecord());
        verify(notificationPublishServiceMock).putNotification(mapChanged);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with WRITE modification type from southbound.
     */
    @Test
    public void onDataTreeChangedTest_write_SB() {
        final List<DataTreeModification<Mapping>> changes = List.of(changeWrite);
        when(modWrite.dataAfter()).thenReturn(MAPPING_EID_3_SB);

        mappingDataListener.onDataTreeChanged(changes);
        //verifyZeroInteractions(iMappingSystemMock);
        verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with multiple changes.
     */
    @Test
    @Ignore
    public void onDataTreeChangedTest_multipleChanges() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes =
                List.of(changeDel, changeSubtreeModified, changeWrite);
        final MappingChanged mapChangedSubtreeMod = MSNotificationInputUtil.toMappingChanged(
                MAPPING_EID_2_NB.getMappingRecord(), null, null, null, MappingChange.Updated);

        when(modDel.dataBefore()).thenReturn(MAPPING_EID_1_NB);
        when(modSubtreeModified.dataAfter()).thenReturn(MAPPING_EID_2_NB);
        when(modWrite.dataAfter()).thenReturn(MAPPING_EID_3_SB);

        mappingDataListener.onDataTreeChanged(changes);
        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        verify(mappingSystemMock).removeMapping(MappingOrigin.Northbound, IPV4_EID_1);
        verify(mappingSystemMock).addMapping(eq(MappingOrigin.Northbound), eq(IPV4_EID_2), captor.capture());
        assertEquals(captor.getValue().getRecord(), MAPPING_EID_2_NB.getMappingRecord());
        verify(notificationPublishServiceMock).putNotification(mapChangedSubtreeMod);
        //verifyNoMoreInteractions(iMappingSystemMock);
        verifyNoMoreInteractions(notificationPublishServiceMock);
    }

    private static Mapping getDefaultMapping(final Eid eid, final MappingOrigin origin) {
        final MappingRecord record = new MappingRecordBuilder().setEid(eid).build();
        return new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(eid)), origin))
                .setOrigin(origin)
                .setMappingRecord(record).build();
    }
}
