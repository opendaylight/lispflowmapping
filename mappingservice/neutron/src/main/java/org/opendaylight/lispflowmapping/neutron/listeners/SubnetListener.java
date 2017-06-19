/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.listeners;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.neutron.dataprocessors.DataProcessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150712.Neutron;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.Subnets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation of a ClusteredDataTreeChangeListener that listens for northbound requests on a {@link Subnet} subtree.
 */
public class SubnetListener extends DelegatingDataTreeListener<Subnet> {

    private static final DataTreeIdentifier<Subnet> IDENTIFIER =
            new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
                    InstanceIdentifier.create(Neutron.class).child(Subnets.class).child(Subnet.class));

    public SubnetListener(DataProcessor<Subnet> dataProcessor, DataBroker dataBroker) {
        super(dataProcessor, dataBroker, IDENTIFIER);
    }

    @Override
    public String toString() {
        return "SubnetListener";
    }
}
