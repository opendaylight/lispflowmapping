/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.implementation.util.MSNotificationInputUtil;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MappingDataListenerTest {

    private static IMappingSystem iMappingSystemMock;
    private static NotificationPublishService notificationPublishServiceMock;

    private static DataTreeModification<Mapping> change_del;
    private static DataTreeModification<Mapping> change_subtreeModified;
    private static DataTreeModification<Mapping> change_write;
    private static DataObjectModification<Mapping> mod_del;
    private static DataObjectModification<Mapping> mod_subtreeModified;
    private static DataObjectModification<Mapping> mod_write;

    private static MappingDataListener mappingDataListener;

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

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        final DataBroker dataBrokerMock = Mockito.mock(DataBroker.class);
        iMappingSystemMock = Mockito.mock(IMappingSystem.class);
        notificationPublishServiceMock = Mockito.mock(NotificationPublishService .class);
        mappingDataListener =
                new MappingDataListener(dataBrokerMock, iMappingSystemMock, notificationPublishServiceMock);

        final InstanceIdentifier<Mapping> instanceIdentifierMock = Mockito.mock(InstanceIdentifier.class);
        final DataTreeIdentifier<Mapping> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, instanceIdentifierMock);

        change_del = Mockito.mock(DataTreeModification.class);
        change_subtreeModified = Mockito.mock(DataTreeModification.class);
        change_write = Mockito.mock(DataTreeModification.class);
        mod_del = Mockito.mock(DataObjectModification.class);
        mod_subtreeModified = Mockito.mock(DataObjectModification.class);
        mod_write = Mockito.mock(DataObjectModification.class);

        Mockito.when(change_del.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_del.getRootNode()).thenReturn(mod_del);
        Mockito.when(change_subtreeModified.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_subtreeModified.getRootNode()).thenReturn(mod_subtreeModified);
        Mockito.when(change_write.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_write.getRootNode()).thenReturn(mod_write);
        Mockito.when(mod_del.getModificationType()).thenReturn(ModificationType.DELETE);
        Mockito.when(mod_subtreeModified.getModificationType()).thenReturn(ModificationType.SUBTREE_MODIFIED);
        Mockito.when(mod_write.getModificationType()).thenReturn(ModificationType.WRITE);
        Mockito.when(iMappingSystemMock.isMaster()).thenReturn(true);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with DELETE modification type from northbound.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_delete_NB() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(change_del);
        final MappingChanged mapChanged = MSNotificationInputUtil
                .toMappingChanged(MAPPING_EID_1_NB, MappingChange.Removed);
        Mockito.when(mod_del.getDataBefore()).thenReturn(MAPPING_EID_1_NB);

        mappingDataListener.onDataTreeChanged(changes);
        Mockito.verify(iMappingSystemMock).removeMapping(MappingOrigin.Northbound, IPV4_EID_1);
        Mockito.verify(notificationPublishServiceMock).putNotification(mapChanged);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with DELETE modification type from southbound.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_delete_SB() {
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(change_del);
        Mockito.when(mod_del.getDataBefore()).thenReturn(MAPPING_EID_1_SB);

        mappingDataListener.onDataTreeChanged(changes);
        //Mockito.verifyZeroInteractions(iMappingSystemMock);
        Mockito.verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type from
     * northbound.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_subtreeModified_NB() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(change_subtreeModified);
        final MappingChanged mapChanged = MSNotificationInputUtil
                .toMappingChanged(MAPPING_EID_2_NB, MappingChange.Updated);
        Mockito.when(mod_subtreeModified.getDataAfter()).thenReturn(MAPPING_EID_2_NB);

        mappingDataListener.onDataTreeChanged(changes);
        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        Mockito.verify(iMappingSystemMock)
                .addMapping(Mockito.eq(MappingOrigin.Northbound), Mockito.eq(IPV4_EID_2), captor.capture());
        assertEquals(captor.getValue().getRecord(), MAPPING_EID_2_NB.getMappingRecord());
        Mockito.verify(notificationPublishServiceMock).putNotification(mapChanged);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type from
     * southbound.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_subtreeModified_SB() {
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(change_subtreeModified);
        Mockito.when(mod_subtreeModified.getDataAfter()).thenReturn(MAPPING_EID_2_SB);

        mappingDataListener.onDataTreeChanged(changes);
        //Mockito.verifyZeroInteractions(iMappingSystemMock);
        Mockito.verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with WRITE modification type from northbound.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_write_NB() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(change_write);
        final MappingChanged mapChanged = MSNotificationInputUtil
                .toMappingChanged(MAPPING_EID_3_NB, MappingChange.Created);
        Mockito.when(mod_write.getDataAfter()).thenReturn(MAPPING_EID_3_NB);

        mappingDataListener.onDataTreeChanged(changes);
        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        Mockito.verify(iMappingSystemMock)
                .addMapping(Mockito.eq(MappingOrigin.Northbound), Mockito.eq(IPV4_EID_3), captor.capture());
        assertEquals(captor.getValue().getRecord(), MAPPING_EID_3_NB.getMappingRecord());
        Mockito.verify(notificationPublishServiceMock).putNotification(mapChanged);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with WRITE modification type from southbound.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_write_SB() {
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(change_write);
        Mockito.when(mod_write.getDataAfter()).thenReturn(MAPPING_EID_3_SB);

        mappingDataListener.onDataTreeChanged(changes);
        //Mockito.verifyZeroInteractions(iMappingSystemMock);
        Mockito.verifyZeroInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with multiple changes.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_multipleChanges() throws InterruptedException {
        final List<DataTreeModification<Mapping>> changes =
                Lists.newArrayList(change_del, change_subtreeModified, change_write);
        final MappingChanged mapChangedDel = MSNotificationInputUtil
                .toMappingChanged(MAPPING_EID_1_NB, MappingChange.Removed);
        final MappingChanged mapChangedSubtreeMod = MSNotificationInputUtil
                .toMappingChanged(MAPPING_EID_2_NB, MappingChange.Updated);

        Mockito.when(mod_del.getDataBefore()).thenReturn(MAPPING_EID_1_NB);
        Mockito.when(mod_subtreeModified.getDataAfter()).thenReturn(MAPPING_EID_2_NB);
        Mockito.when(mod_write.getDataAfter()).thenReturn(MAPPING_EID_3_SB);

        mappingDataListener.onDataTreeChanged(changes);
        final ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        Mockito.verify(iMappingSystemMock).removeMapping(MappingOrigin.Northbound, IPV4_EID_1);
        Mockito.verify(notificationPublishServiceMock).putNotification(mapChangedDel);
        Mockito.verify(iMappingSystemMock)
                .addMapping(Mockito.eq(MappingOrigin.Northbound), Mockito.eq(IPV4_EID_2), captor.capture());
        assertEquals(captor.getValue().getRecord(), MAPPING_EID_2_NB.getMappingRecord());
        Mockito.verify(notificationPublishServiceMock).putNotification(mapChangedSubtreeMod);
        //Mockito.verifyNoMoreInteractions(iMappingSystemMock);
        Mockito.verifyNoMoreInteractions(notificationPublishServiceMock);
    }

    /**
     * Tests {@link MappingDataListener#onDataTreeChanged} method with no modification type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_noModType() {
        final DataTreeModification<Mapping> changeNoModType = Mockito.mock(DataTreeModification.class);
        final DataObjectModification<Mapping> modNoType = Mockito.mock(DataObjectModification.class);
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(changeNoModType);

        Mockito.when(changeNoModType.getRootNode()).thenReturn(modNoType);
        Mockito.when(modNoType.getModificationType()).thenReturn(null);

        mappingDataListener.onDataTreeChanged(changes);

        Mockito.verifyZeroInteractions(iMappingSystemMock);
        Mockito.verifyZeroInteractions(notificationPublishServiceMock);
    }


    private static Mapping getDefaultMapping(Eid eid, MappingOrigin origin) {
        final MappingRecord record = new MappingRecordBuilder().setEid(eid).build();
        return new MappingBuilder().setOrigin(origin).setMappingRecord(record).build();
    }
}
