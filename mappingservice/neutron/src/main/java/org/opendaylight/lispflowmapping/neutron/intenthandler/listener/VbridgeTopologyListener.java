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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vbridge.topology.rev160129.network.topology.topology.topology.types.VbridgeTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 1/19/17.
 */
public class VbridgeTopologyListener implements ClusteredDataTreeChangeListener<VbridgeTopology>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(VbridgeTopologyListener.class);

    private DataBroker dataBroker;
    private MountPointService mountPointService;

    @GuardedBy("this")
    private final Map<TopologyKey, VppEndpointListener> domains = new ConcurrentHashMap<>();

    public VbridgeTopologyListener(final DataBroker dataBroker,
                                   final MountPointService mountPointService) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.mountPointService = Preconditions.checkNotNull(mountPointService);
    }

    @Override
    public synchronized void onDataTreeChanged(@Nonnull Collection<DataTreeModification<VbridgeTopology>> changes) {
        for (DataTreeModification<VbridgeTopology> topologyData : changes) {
            final KeyedInstanceIdentifier<Topology, TopologyKey> topologyInstanceIdentifier =
                    (KeyedInstanceIdentifier<Topology, TopologyKey>) topologyData
                                                                        .getRootPath()
                                                                        .getRootIdentifier()
                                                                        .firstIdentifierOf(Topology.class);

            Preconditions.checkArgument(!topologyInstanceIdentifier
                    .isWildcarded(), "Wildcard topology %s is not supported",
                    topologyInstanceIdentifier);

            final DataObjectModification<VbridgeTopology> modification =  topologyData.getRootNode();

            switch (modification.getModificationType()) {
                case DELETE:
                    handleVbridgeTopologyDelete(topologyInstanceIdentifier);
                    break;
                case WRITE:
                    handleVbridgeTopologyWrite(topologyInstanceIdentifier);
                    break;
                default:
                    LOG.warn("Ignoring unhandled modification type {}", modification.getModificationType());
                    break;

            }
        }
    }

    private void handleVbridgeTopologyDelete(KeyedInstanceIdentifier<Topology, TopologyKey> topology) {
        VppEndpointListener endpointListener = domains.get(topology.getKey());
        endpointListener.close();
        domains.remove(topology.getKey());
    }

    private void handleVbridgeTopologyWrite(KeyedInstanceIdentifier<Topology, TopologyKey> topology) {
        if (domains.containsKey(topology.getKey())) {
            domains.get(topology.getKey()).close();
            domains.remove(topology.getKey());
        }
        domains.put(topology.getKey(), new VppEndpointListener(dataBroker, mountPointService, topology));
    }

    @Override
    public void close() {
        domains.forEach((topologyKey, vppEndpointListener) -> {
            vppEndpointListener.close();
        });
    }
}
