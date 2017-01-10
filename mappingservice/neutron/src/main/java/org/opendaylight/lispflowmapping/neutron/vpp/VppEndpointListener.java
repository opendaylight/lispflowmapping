/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.vpp;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.vbd.impl.transaction.VbdNetconfConnectionProbe;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vbd.impl.rev160202.VbdProvider;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 1/11/17.
 */
public class VppEndpointListener implements AutoCloseable, ClusteredDataTreeChangeListener<Topology> {
    private static final Logger LOG = LoggerFactory.getLogger(VppEndpointListener.class);

    private final DataBroker dataBroker;
    private final MountPointService mountService;
    private final VppNodeReader vppNodeReader;
    private final HostIdToRlocMapper hostIdToRlocMapper;

    private final Map<NodeId, KeyedInstanceIdentifier<Node, NodeKey>> nodeIdToKeyedInstanceIdentifierMap;

    public VppEndpointListener(final DataBroker dataBroker,
                               final BindingAwareBroker bindingAwareBroker) {
        final BindingAwareBroker.ProviderContext session = Preconditions.checkNotNull(bindingAwareBroker)
                .registerProvider(new VbdProvider());

        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.mountService = Preconditions.checkNotNull(session.getSALService(MountPointService.class));
        nodeIdToKeyedInstanceIdentifierMap = new HashMap<>();

        vppNodeReader = new VppNodeReader(this.dataBroker, this.mountService);
        hostIdToRlocMapper = HostIdToRlocMapper.getInstance();
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Topology>> changes) {
        for (DataTreeModification<Topology> change : changes) {
            final DataObjectModification<Topology> modification = change.getRootNode();
            switch (modification.getModificationType()) {
                case WRITE:
                    handleChange(modification);
                    break;
                case SUBTREE_MODIFIED:
                    handleChange(modification);
                    break;
                case DELETE:
                    handleDeleteOnTopology();
                    break;
                default:
                    LOG.warn("Ignored topology modification {}", modification);
                    break;
            }
        }
    }

    private void handleChange(DataObjectModification modification) {
        Collection<DataObjectModification<? extends DataObject>> modifiedChildren = modification.getModifiedChildren();

        for (DataObjectModification modifiedNode : modifiedChildren) {
            final Node newOrModifiedNode = (Node) modifiedNode.getDataAfter();
            KeyedInstanceIdentifier<Node, NodeKey> kiiToNode = processNode(newOrModifiedNode);
            hostIdToRlocMapper.addMapping(newOrModifiedNode.getNodeId().getValue(),
                    LispAddressUtil.toRloc(vppNodeReader.rlocIpOfNode(kiiToNode)));
        }
    }

    private KeyedInstanceIdentifier<Node, NodeKey> processNode(Node newOrModifiedNode) {
        processNodeOnConnection(newOrModifiedNode);
        return nodeIdToKeyedInstanceIdentifierMap.get(newOrModifiedNode.getNodeId());
    }

    private void processNodeOnConnection(Node newOrModifiedNode) {
        for (SupportingNode supportingNode : newOrModifiedNode.getSupportingNode()) {
            final NodeId nodeMount = supportingNode.getNodeRef();
            final VbdNetconfConnectionProbe probe = new VbdNetconfConnectionProbe(supportingNode.getNodeRef(),
                    dataBroker);

            try {
                // Verify netconf connection
                boolean connectionReady = probe.startProbing();
                if (connectionReady) {
                    LOG.debug("Node {} is connected, creating ...", supportingNode.getNodeRef());
                    final TopologyId topologyMount = supportingNode.getTopologyRef();
                    final KeyedInstanceIdentifier<Node, NodeKey> iiToVpp =
                            InstanceIdentifier.create(NetworkTopology.class)
                            .child(Topology.class, new TopologyKey(topologyMount))
                            .child(Node.class, new NodeKey(nodeMount));
                    nodeIdToKeyedInstanceIdentifierMap.put(newOrModifiedNode.getNodeId(), iiToVpp);
                } else {
                    LOG.debug("Failed while connecting to node {}", supportingNode.getNodeRef());
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Exception while processing node {} ... ", supportingNode.getNodeRef(), e);
            } catch (TimeoutException e) {
                LOG.warn("Node {} was not connected within {} seconds. "
                                + "Check node configuration and connectivity to proceed",
                        supportingNode.getNodeRef(), VbdNetconfConnectionProbe.NODE_CONNECTION_TIMER);
            }
        }
    }

    private void handleDeleteOnTopology() {
        //TODO
    }

    @Override
    public void close() throws Exception {

    }
}
