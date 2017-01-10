/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.listener;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.intenthandler.IntentHandlerAsyncExecutorProvider;
import org.opendaylight.lispflowmapping.neutron.intenthandler.util.VppNetconfConnectionProbe;
import org.opendaylight.lispflowmapping.neutron.intenthandler.util.VppNodeReader;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.HostIdToRlocMapper;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNode;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 1/11/17.
 */
public class VppEndpointListener implements AutoCloseable, ClusteredDataTreeChangeListener<Topology> {
    private static final Logger LOG = LoggerFactory.getLogger(VppEndpointListener.class);

    private final DataBroker dataBroker;
    private final MountPointService mountService;
    private final ListenerRegistration<?> reg;

    private final VppNodeReader vppNodeReader;
    private final HostIdToRlocMapper hostIdToRlocMapper;

    private final Multimap<NodeId, KeyedInstanceIdentifier<Node, NodeKey>> nodeIdToKeyedInstanceIdentifierMap =
            ArrayListMultimap.create();

    private final ListeningExecutorService executorService;

    public VppEndpointListener(final DataBroker dataBroker,
                               final MountPointService mountPointService,
                               KeyedInstanceIdentifier<Topology, TopologyKey> topologyII) {

        this.dataBroker = dataBroker;
        this.mountService = mountPointService;

        vppNodeReader = new VppNodeReader(this.dataBroker, this.mountService);

        hostIdToRlocMapper = HostIdToRlocMapper.getInstance();

        reg = dataBroker.registerDataTreeChangeListener(
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, topologyII), this);

        executorService = IntentHandlerAsyncExecutorProvider.getInstace().getExecutor();
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Topology>> changes) {
        for (DataTreeModification<Topology> change : changes) {
            final DataObjectModification<Topology> modification = change.getRootNode();
            ListenableFuture<Void> modificationTaskHandler;
            switch (modification.getModificationType()) {
                case WRITE:
                    modificationTaskHandler = handleChange(modification);
                    break;
                case SUBTREE_MODIFIED:
                    modificationTaskHandler = handleChange(modification);
                    break;
                case DELETE:
                    modificationTaskHandler = handleDeleteOnTopology();
                    break;
                default:
                    LOG.warn("Ignored topology modification {}", modification);
                    modificationTaskHandler = Futures.immediateFuture(null);
                    break;
            }
            Futures.addCallback(modificationTaskHandler, new FutureCallback<Void>() {
                @Override
                public void onSuccess(@Nullable Void vd) {
                    LOG.debug("VppEndpoint modification handled successfully!");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    LOG.debug("Failed to handle VppEndpoint mdification!");
                }
            });
        }
    }

    private ListenableFuture<Void> handleChange(DataObjectModification modification) {
        Collection<DataObjectModification<? extends DataObject>> modifiedChildren = modification.getModifiedChildren();
        List<ListenableFuture<KeyedInstanceIdentifier<Node, NodeKey>>> processingTasks = new ArrayList<>();
        for (DataObjectModification modifiedNode : modifiedChildren) {
            final Node newOrModifiedNode = (Node) modifiedNode.getDataAfter();
            ListenableFuture<KeyedInstanceIdentifier<Node, NodeKey>> processingTask = processNode(newOrModifiedNode);
            Futures.addCallback(processingTask, new FutureCallback<KeyedInstanceIdentifier<Node, NodeKey>>() {
                @Override
                public void onSuccess(@Nullable KeyedInstanceIdentifier<Node, NodeKey> kiiToNode) {
                    hostIdToRlocMapper.addMapping(newOrModifiedNode.getNodeId().getValue(),
                            LispAddressUtil.toRloc(vppNodeReader.rlocIpOfNode(kiiToNode)));
                }

                @Override
                public void onFailure(Throwable throwable) {
                    LOG.debug("Couldn't process {}", newOrModifiedNode.getNodeId().getValue());
                }
            });
            processingTasks.add(processNode(newOrModifiedNode));
        }
        return Futures.immediateFuture(null);
    }

    private ListenableFuture<KeyedInstanceIdentifier<Node, NodeKey>> processNode(final Node newOrModifiedNode) {
        ListenableFuture<Void> probeVppNodeForConnection = executorService
                .submit(() -> {
                    processNodeOnConnection(newOrModifiedNode);
                    return null;
                });

        return Futures.transform(probeVppNodeForConnection,
                new Function<Void, KeyedInstanceIdentifier<Node, NodeKey>>() {
                    @Nullable
                    @Override
                    public KeyedInstanceIdentifier<Node, NodeKey> apply(@Nullable Void vd) {
                        return nodeIdToKeyedInstanceIdentifierMap.get(newOrModifiedNode.getNodeId()).iterator().next();
                    }
                });
    }

    private void processNodeOnConnection(final Node newOrModifiedNode) {
        for (SupportingNode supportingNode : newOrModifiedNode.getSupportingNode()) {
            final NodeId nodeMount = supportingNode.getNodeRef();
            final VppNetconfConnectionProbe probe = new VppNetconfConnectionProbe(supportingNode.getNodeRef(),
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
                        supportingNode.getNodeRef(), VppNetconfConnectionProbe.NODE_CONNECTION_TIMER);
            }
        }
    }

    private ListenableFuture<Void> handleDeleteOnTopology() {
        //TODO
        return Futures.immediateFuture(null);
    }

    @Override
    public void close() {
        reg.close();
    }
}
