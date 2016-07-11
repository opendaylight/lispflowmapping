package org.opendaylight.lispflowmapping.neutron;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.Networks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150712.Neutron;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetworkListener extends DelegatingDataTreeListener<Network> {

    private static final DataTreeIdentifier<Network> IDENTIFIER =
            new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
                    InstanceIdentifier.create(Neutron.class).child(Networks.class).child(Network.class));

    public NetworkListener(DataProcessor<Network> dataProcessor, DataBroker dataBroker) {
        super(dataProcessor, dataBroker, IDENTIFIER);
    }
}
