/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;

import java.net.HttpURLConnection;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.Networks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * LISP Service Implementation of creation and deletion of a Network.
 *
 */
public class LispNeutronNetworkHandler extends LispNeutronService implements ClusteredDataTreeChangeListener<Network> {

    private static final InstanceIdentifier<Network> iid = InstanceIdentifier.create(Networks.class).child(Network.class);

    public LispNeutronNetworkHandler() {
        broker.registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, iid),
                this);
    }
    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Network>> changes) {
        for (DataTreeModification<Network> change : changes) {
            DataObjectModification<Network> mod = change.getRootNode();
            final Network network = mod.getDataAfter();

            if (mod.getModificationType() == DataObjectModification.ModificationType.WRITE) {
                neutronNetworkCreated(network);
            } else if (mod.getModificationType() == DataObjectModification.ModificationType.DELETE) {
                neutronNetworkDeleted(network);
            } else {
                neutronNetworkUpdated(network);
            }
        }
    }

    private void neutronNetworkCreated(Network network) {
        LOG.info("Neutron Network Created : Network name: " + network.getName());
        LOG.debug("Lisp Neutron Network: " + network.toString());
    }

    private void neutronNetworkUpdated(Network network) {
        LOG.info("Neutron Network Updated : Network name: " + network.getName());
        LOG.debug("Lisp Neutron Network: " + network.toString());
    }

    private void neutronNetworkDeleted(Network network) {
        LOG.info("Neutron Network Deleted : Network name: " + network.getName());
        LOG.debug("Lisp Neutron Network: " + network.toString());
    }

    public int canCreateNetwork(Network network) {
        LOG.info("Neutron canCreateNetwork : Network name: " + network.getName());
        LOG.debug("Lisp Neutron Network: " + network.toString());

        return HttpURLConnection.HTTP_OK;
    }

    public int canUpdateNetwork(Network delta, Network original) {
        LOG.info("Neutron canUpdateNetwork : Network name: " + original.getName());
        LOG.debug("Lisp Neutron Network: " + original.toString());

        return HttpURLConnection.HTTP_OK;
    }

    public int canDeleteNetwork(Network network) {
        LOG.info("Neutron canDeleteNetwork : Network name: " + network.getName());
        LOG.debug("Lisp Neutron Network: " + network.toString());

        return HttpURLConnection.HTTP_OK;
    }
}
