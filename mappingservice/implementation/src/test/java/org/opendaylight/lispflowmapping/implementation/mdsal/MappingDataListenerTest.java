/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class MappingDataListenerTest {

    private static DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private static IMappingSystem iMappingSystemMock = Mockito.mock(IMappingSystem.class);
    private static NotificationPublishService notificationPublishService =
            Mockito.mock(NotificationPublishService .class);
    private static DataTreeModification<Mapping> change_1;
    private static DataTreeModification<Mapping> change_2;

    @Mock(name = "mapSystem") private static IMappingSystem mapSystemMock;
    @Mock(name = "notificationPublishService") private static NotificationPublishService notificationPublishServiceMock;
    @InjectMocks private static MappingDataListener mappingDataListener =
            new MappingDataListener(dataBroker, iMappingSystemMock, notificationPublishService);

    private static final String IPV4_STRING_1 = "192.168.0.1";
    private static final String IPV4_STRING_2 = "192.168.0.2";
    private static final Eid IPV4_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        InstanceIdentifier instanceIdentifierMock = Mockito.mock(InstanceIdentifier.class);
        DataTreeIdentifier dataTreeIdentifier =
                new DataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, instanceIdentifierMock);

        change_2 = Mockito.mock(DataTreeModification.class);
        change_1 = Mockito.mock(DataTreeModification.class);
        Mockito.when(change_1.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_2.getRootPath()).thenReturn(dataTreeIdentifier);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_delete() {
        final List<DataTreeModification<Mapping>> changes = Lists.newArrayList(change_1, change_2);

        final DataObjectModification<Mapping> mod_1 = Mockito.mock(DataObjectModification.class);
        final DataObjectModification<Mapping> mod_2 = Mockito.mock(DataObjectModification.class);

        final Mapping mappingRecord_1 = getDefaultMapping(IPV4_EID_1);
        final Mapping mappingRecord_2 = getDefaultMapping(IPV4_EID_2);

        Mockito.when(change_1.getRootNode()).thenReturn(mod_1);
        Mockito.when(change_2.getRootNode()).thenReturn(mod_2);
        Mockito.when(mod_1.getModificationType()).thenReturn(DataObjectModification.ModificationType.DELETE);
        Mockito.when(mod_2.getModificationType()).thenReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED);
        Mockito.when(mod_1.getDataBefore()).thenReturn(mappingRecord_1);
        Mockito.when(mod_2.getDataAfter()).thenReturn(mappingRecord_2);

        mappingDataListener.onDataTreeChanged(changes);
        Mockito.verify(mapSystemMock).removeMapping(MappingOrigin.Northbound, IPV4_EID_1);
    }

    private static Mapping getDefaultMapping(Eid eid) {
        final MappingRecord record = new MappingRecordBuilder().setEid(eid).build();

        return new MappingBuilder().setOrigin(MappingOrigin.Northbound).setMappingRecord(record).build();
    }
}
