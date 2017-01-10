/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.util;

/**
 * Created by Shakib Ahmed on 1/23/17.
 */

import static org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus.Connected;
import static org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus.Connecting;


import com.google.common.util.concurrent.SettableFuture;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purpose: verify whether provided netconf node is already connected or wait if not.
 *
 * <p>
 *     VppNetconfConnectionProbe registers istener which catches node-related changes from topology-netconf.
 * A {@link SettableFuture} is set {@link Boolean#TRUE}, if the node is connected within {@link
 * VppNetconfConnectionProbe#NODE_CONNECTION_TIMER} seconds. Else, proper exception is throws.
 * </p>
 *
 */
public class VppNetconfConnectionProbe implements ClusteredDataTreeChangeListener<Node> {
    private static final Logger LOG = LoggerFactory.getLogger(VppNodeReader.class);

    public static final int NODE_CONNECTION_TIMER = 60;
    private final DataBroker dataBroker;
    private ListenerRegistration<VppNetconfConnectionProbe> registeredListener;
    private SettableFuture<Boolean> connectionStatusFuture = SettableFuture.create();

    private static final String TOPOLOGY_IDENTIFIER = "topology-netconf";

    private final DataTreeIdentifier<Node> path;

    public VppNetconfConnectionProbe(final NodeId nodeId, final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        final InstanceIdentifier<Node> nodeIid = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_IDENTIFIER)))
                .child(Node.class, new NodeKey(nodeId))
                .build();

        path = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, nodeIid);
    }

    public boolean startProbing() throws ExecutionException, InterruptedException, TimeoutException {
        registeredListener = dataBroker.registerDataTreeChangeListener(path, this);
        return connectionStatusFuture.get(NODE_CONNECTION_TIMER, TimeUnit.SECONDS);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {
        changes.forEach(modification -> {
            final DataObjectModification<Node> rootNode = modification.getRootNode();
            final Node node = rootNode.getDataAfter();
            final NetconfNode netconfNode = getNodeAugmentation(node);
            if (node == null || node.getNodeId() == null) {
                return;
            }
            if (netconfNode == null || netconfNode.getConnectionStatus() == null) {
                connectionStatusFuture.set(false);
                unregister();
            } else {
                final NetconfNodeConnectionStatus.ConnectionStatus status = netconfNode.getConnectionStatus();
                if (status.equals(Connected)) {
                    connectionStatusFuture.set(true);
                    unregister();
                } else if (!status.equals(Connecting)) {
                    connectionStatusFuture.set(false);
                    unregister();
                }
            }
        });
    }

    private NetconfNode getNodeAugmentation(Node node) {
        NetconfNode netconfNode = node.getAugmentation(NetconfNode.class);
        if (netconfNode == null) {
            return null;
        }
        return netconfNode;
    }

    private void unregister() {
        if (registeredListener != null) {
            registeredListener.close();
        }
    }
}
