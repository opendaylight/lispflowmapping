/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.manager;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.intenthandler.util.VppNodeReader;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.HostInformationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 6/19/17.
 */
public class VppNodeManager {
    private static final Logger LOG = LoggerFactory.getLogger(VppNodeManager.class);

    private static final TopologyId TOPOLOGY_ID = new TopologyId("topology-netconf");
    private static final NodeId CONTROLLER_CONFIG_NODE = new NodeId("controller-config");

    private final DataBroker dataBroker;
    private final MountPointService mountPointService;

    private HostInformationManager hostInformationManager = HostInformationManager.getInstance();

    private VppNodeReader vppNodeReader;

    public VppNodeManager(@Nonnull final DataBroker dataBroker,
                          @Nonnull final MountPointService mountPointService) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.mountPointService = Preconditions.checkNotNull(mountPointService);

        this.vppNodeReader = new VppNodeReader(dataBroker, mountPointService);
    }

    public void syncNodes(final Node dataAfter, final Node dataBefore, DataObjectModification.ModificationType type) {
        if (isControllerConfigNode(dataAfter, dataBefore)) {
            // ignore
            LOG.trace("{} is ignored by VPP-renderer", CONTROLLER_CONFIG_NODE);
            return;
        }

        switch (type) {
            case WRITE:
                createNode(dataAfter);
                break;
            case SUBTREE_MODIFIED:
                updateNode(dataAfter);
                break;
            case DELETE:
                removeNode(dataBefore);
                break;
            default:
                LOG.warn("Ignored topology modification {}", type);
                break;
        }

    }

    private boolean isControllerConfigNode(final Node dataAfter, final Node dataBefore) {
        if (dataAfter != null) {
            return CONTROLLER_CONFIG_NODE.equals(dataAfter.getNodeId());
        }
        return CONTROLLER_CONFIG_NODE.equals(dataBefore.getNodeId());
    }

    private void createNode(final Node node) {
        final String nodeId = node.getNodeId().getValue();
        LOG.info("Registering new node {}", nodeId);
        final NetconfNode netconfNode = getNodeAugmentation(node);
        if (netconfNode == null) {
            LOG.warn("Netconf Node augmentation missing from {}", node);
        }
        final NetconfNodeConnectionStatus.ConnectionStatus connectionStatus = netconfNode.getConnectionStatus();
        switch (connectionStatus) {
            case Connecting: {
                LOG.debug("Connecting device {} ...", nodeId);
                return;
            }
            case Connected: {
                processConnectedNode(node);
                return;
            }
            case UnableToConnect: {
                LOG.debug("Unable to connect for node {}", nodeId);
                return;
            }
            default: {
                LOG.debug("Unknown connection status for node {}", nodeId);
            }
        }
    }

    private void updateNode(final Node node) {
        final String nodeId = node.getNodeId().getValue();
        LOG.info("Updating node {}", nodeId);
        final NetconfNode netconfNode = getNodeAugmentation(node);
        if (netconfNode == null) {
            LOG.debug("Node {} is not an netconf node", nodeId);
        }
        final NetconfNodeConnectionStatus.ConnectionStatus afterNodeStatus = netconfNode.getConnectionStatus();

        switch (afterNodeStatus) {
            case Connecting: {
                LOG.debug("Connecting device {} ...", nodeId);
                processDisconnectedNode(node);
                return;
            }
            case Connected: {
                processConnectedNode(node);
                return;
            }
            case UnableToConnect: {
                LOG.debug("Unable to connect for node {} ...", nodeId);
                return;
            }
            default: {
                LOG.debug("Unknown connection status for node {}", nodeId);
            }
        }
    }

    private void removeNode(final Node node) {
        LOG.debug("Host {} has been removed", node.getNodeId().getValue());
        processDisconnectedNode(node);
    }

    private NetconfNode getNodeAugmentation(final Node node) {
        final NetconfNode netconfNode = node.getAugmentation(NetconfNode.class);
        if (netconfNode == null) {
            LOG.warn("Node {} is not a netconf device", node.getNodeId().getValue());
            return null;
        }
        return netconfNode;
    }

    private void processConnectedNode(final Node node) {
        KeyedInstanceIdentifier<Node, NodeKey> vppIid = getMountPointKeyedIId(node);
        Rloc rlocOfComputeNode = LispAddressUtil.toRloc(vppNodeReader.rlocIpOfNode(vppIid));
        hostInformationManager.addHostRelatedInfo(node.getNodeId().getValue(), rlocOfComputeNode);
    }

    private void processDisconnectedNode(final Node node) {
        hostInformationManager.attemptToDeleteAllHostMapping(node.getNodeId().getValue());
    }

    private KeyedInstanceIdentifier<Node, NodeKey> getMountPointKeyedIId(final Node node) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(TOPOLOGY_ID))
                .child(Node.class, new NodeKey(node.getNodeId()));
    }
}
