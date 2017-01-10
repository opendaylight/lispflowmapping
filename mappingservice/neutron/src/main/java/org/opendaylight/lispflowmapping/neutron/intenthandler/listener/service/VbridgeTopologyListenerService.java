/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.listener.service;

import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.neutron.intenthandler.listener.VbridgeTopologyListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vbridge.topology.rev160129.TopologyTypesVbridgeAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vbridge.topology.rev160129.network.topology.topology.topology.types.VbridgeTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.TopologyTypes;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 1/19/17.
 */
public class VbridgeTopologyListenerService implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(VbridgeTopologyListenerService.class);

    private final ListenerRegistration<VbridgeTopologyListener> listenerRegistration;

    private boolean closed;

    private static final DataTreeIdentifier<VbridgeTopology> TREE_LISTENER_IDENTIFIER =
            new DataTreeIdentifier<>(
                    LogicalDatastoreType.OPERATIONAL,
                    InstanceIdentifier.builder(NetworkTopology.class)
                            .child(Topology.class)
                            .child(TopologyTypes.class)
                            .augmentation(TopologyTypesVbridgeAugment.class)
                            .child(VbridgeTopology.class).build());

    public VbridgeTopologyListenerService(final ListenerRegistration<VbridgeTopologyListener> reg) {
        this.listenerRegistration = reg;
        this.closed = false;
    }

    public static VbridgeTopologyListenerService initialize(@Nonnull final DataBroker dataBroker,
                                                            @Nonnull final MountPointService mountPointService) {
        final ListenerRegistration<VbridgeTopologyListener> reg =
                dataBroker.registerDataTreeChangeListener( TREE_LISTENER_IDENTIFIER,
                        new VbridgeTopologyListener(dataBroker, mountPointService));

        return new VbridgeTopologyListenerService(reg);
    }


    @Override
    public void close() {
        if (!closed) {

            final VbridgeTopologyListener listener = listenerRegistration.getInstance();

            listenerRegistration.close();
            listener.close();

            closed = true;
        }
    }
}
