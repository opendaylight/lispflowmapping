/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.lispflowmapping.neutron.dataprocessors.DataProcessor;
import org.opendaylight.lispflowmapping.neutron.listeners.NetworkListener;
import org.opendaylight.lispflowmapping.neutron.listeners.PortListener;
import org.opendaylight.lispflowmapping.neutron.listeners.SubnetListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet;

@SuppressWarnings("unchecked")
public class ListenerTest {

    /**
     * Tests {@link NetworkListener#toString}, {@link PortListener#toString}, {@link SubnetListener#toString} method.
     */
    @Test
    public void toStringTest() {
        final DataProcessor<Network> networkDataProcessor = Mockito.mock(DataProcessor.class);
        final DataBroker dataBroker = Mockito.mock(DataBroker.class);
        final NetworkListener networkListener = new NetworkListener(networkDataProcessor, dataBroker);

        final DataProcessor<Port> portDataProcessor = Mockito.mock(DataProcessor.class);
        final PortListener portListener = new PortListener(portDataProcessor, dataBroker);

        final DataProcessor<Subnet> subnetDataProcessor = Mockito.mock(DataProcessor.class);
        final SubnetListener subnetListener = new SubnetListener(subnetDataProcessor, dataBroker);

        assertEquals("NetworkListener", networkListener.toString());
        assertEquals("PortListener", portListener.toString());
        assertEquals("SubnetListener", subnetListener.toString());
    }
}
