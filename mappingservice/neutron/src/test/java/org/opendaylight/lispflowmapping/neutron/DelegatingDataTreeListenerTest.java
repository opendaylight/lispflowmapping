/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.neutron.dataprocessors.DataProcessor;
import org.opendaylight.lispflowmapping.neutron.listeners.DelegatingDataTreeListener;
import org.opendaylight.lispflowmapping.neutron.listeners.NetworkListener;
import org.opendaylight.lispflowmapping.neutron.listeners.PortListener;
import org.opendaylight.lispflowmapping.neutron.listeners.SubnetListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.Networks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150712.Neutron;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@SuppressWarnings("unchecked")
public class DelegatingDataTreeListenerTest {
    private static final DataProcessor<Network> DATA_PROCESSOR_NETWORK_MOCK = Mockito.mock(DataProcessor.class);

    private static ILispNeutronService iLispNeutronServiceMock = Mockito.mock(ILispNeutronService.class);
    private static DataBroker dataBrokerMock = Mockito.mock(DataBroker.class);
    private static final DataTreeIdentifier<Network> NETWORK_ID =
            new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Neutron.class)
                    .child(Networks.class).child(Network.class));

    private final DelegatingDataTreeListener<Network> networkDelegatingDataTreeListener =
            new DelegatingDataTreeListener<>(DATA_PROCESSOR_NETWORK_MOCK, dataBrokerMock, NETWORK_ID);

    private static final Network DUMMY_NETWORK_BEFORE = Mockito.mock(Network.class);
    private static final Network DUMMY_NETWORK_AFTER = Mockito.mock(Network.class);

    private static final DataTreeModification<Network> DATA_TREE_MODIFICATION =
            Mockito.mock(DataTreeModification.class);
    private static final DataObjectModification<Network> DATA_OBJECT_MODIFICATION =
            Mockito.mock(DataObjectModification.class);

    /**
     * Tests {@link DelegatingDataTreeListener#initiateListener} method.
     */
    @Test
    public void initiateListenerTest() {
        assertTrue(DelegatingDataTreeListener
                .initiateListener(Network.class, iLispNeutronServiceMock, dataBrokerMock) instanceof NetworkListener);
        assertTrue(DelegatingDataTreeListener
                .initiateListener(Port.class, iLispNeutronServiceMock, dataBrokerMock) instanceof PortListener);
        assertTrue(DelegatingDataTreeListener
                .initiateListener(Subnet.class, iLispNeutronServiceMock, dataBrokerMock) instanceof SubnetListener);
    }

    /**
     * Tests {@link DelegatingDataTreeListener#onDataTreeChanged} method with WRITE modification type.
     */
    @Test
    public void onDataTreeChangedTest_write() {
        Mockito.when(DATA_TREE_MODIFICATION.getRootNode()).thenReturn(DATA_OBJECT_MODIFICATION);
        Mockito.when(DATA_OBJECT_MODIFICATION.getDataAfter()).thenReturn(DUMMY_NETWORK_AFTER);
        Mockito.when(DATA_OBJECT_MODIFICATION.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.WRITE);

        networkDelegatingDataTreeListener.onDataTreeChanged(Lists.newArrayList(DATA_TREE_MODIFICATION));
        Mockito.verify(DATA_PROCESSOR_NETWORK_MOCK).create(DUMMY_NETWORK_AFTER);
    }

    /**
     * Tests {@link DelegatingDataTreeListener#onDataTreeChanged} method with DELETE modification type.
     */
    @Test
    public void onDataTreeChangedTest_delete() {
        Mockito.when(DATA_TREE_MODIFICATION.getRootNode()).thenReturn(DATA_OBJECT_MODIFICATION);
        Mockito.when(DATA_OBJECT_MODIFICATION.getDataBefore()).thenReturn(DUMMY_NETWORK_BEFORE);
        Mockito.when(DATA_OBJECT_MODIFICATION.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.DELETE);

        networkDelegatingDataTreeListener.onDataTreeChanged(Lists.newArrayList(DATA_TREE_MODIFICATION));
        Mockito.verify(DATA_PROCESSOR_NETWORK_MOCK).delete(DUMMY_NETWORK_BEFORE);
    }

    /**
     * Tests {@link DelegatingDataTreeListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type.
     */
    @Test
    public void onDataTreeChangedTest_subtreeModified() {
        Mockito.when(DATA_TREE_MODIFICATION.getRootNode()).thenReturn(DATA_OBJECT_MODIFICATION);
        Mockito.when(DATA_OBJECT_MODIFICATION.getDataBefore()).thenReturn(DUMMY_NETWORK_BEFORE);
        Mockito.when(DATA_OBJECT_MODIFICATION.getDataAfter()).thenReturn(DUMMY_NETWORK_AFTER);
        Mockito.when(DATA_OBJECT_MODIFICATION.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED);

        networkDelegatingDataTreeListener.onDataTreeChanged(Lists.newArrayList(DATA_TREE_MODIFICATION));
        Mockito.verify(DATA_PROCESSOR_NETWORK_MOCK).update(DUMMY_NETWORK_BEFORE, DUMMY_NETWORK_AFTER);
    }
}
