/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.listener;

import com.google.common.base.Preconditions;

import java.util.Collection;
import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.neutron.intenthandler.manager.VppNodeManager;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 6/19/17.
 */
public class VppNodeListener implements ClusteredDataTreeChangeListener<Node>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(VppNodeListener.class);
    private static final TopologyId TOPOLOGY_ID = new TopologyId("topology-netconf");

    private final ListenerRegistration<VppNodeListener> listenerRegistration;
    private final VppNodeManager nodeManager;

    public VppNodeListener(DataBroker dataBroker, VppNodeManager nodeManager) {
        this.nodeManager = Preconditions.checkNotNull(nodeManager);

        final DataTreeIdentifier<Node> networkTopologyPath = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(NetworkTopology.class)
                        .child(Topology.class, new TopologyKey(TOPOLOGY_ID))
                        .child(Node.class)
                        .build());
        this.listenerRegistration =
                Preconditions.checkNotNull(dataBroker.registerDataTreeChangeListener(networkTopologyPath, this));
        LOG.info("Network-Topology VppNodeListener registered from LISP Neutron service");
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {
        LOG.debug("Netconf topology node change detected, VppNodeListener processing started.");
        for (DataTreeModification<Node> modification : changes) {
            DataObjectModification<Node> rootNode = modification.getRootNode();
            Node dataAfter = rootNode.getDataAfter();
            Node dataBefore = rootNode.getDataBefore();
            nodeManager.syncNodes(dataAfter, dataBefore, rootNode.getModificationType());
        }
    }

    @Override
    public void close() throws Exception {
        this.listenerRegistration.close();
    }
}
