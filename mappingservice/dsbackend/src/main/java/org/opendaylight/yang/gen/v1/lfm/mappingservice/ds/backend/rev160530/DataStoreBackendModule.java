package org.opendaylight.yang.gen.v1.lfm.mappingservice.ds.backend.rev160530;

import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;

public class DataStoreBackendModule extends org.opendaylight.yang.gen.v1.lfm.mappingservice.ds.backend.rev160530.AbstractDataStoreBackendModule {
    public DataStoreBackendModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public DataStoreBackendModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.lfm.mappingservice.ds.backend.rev160530.DataStoreBackendModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        return new DataStoreBackEnd(getDataBrokerDependency());
    }

}
