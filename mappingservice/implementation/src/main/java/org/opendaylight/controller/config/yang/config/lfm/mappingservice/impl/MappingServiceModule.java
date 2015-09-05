package org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl;

import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingServiceModule extends org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl.AbstractMappingServiceModule {
    private static final Logger LOG = LoggerFactory.getLogger(MappingServiceModule.class);
    private MappingService mappingService;

    public MappingServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public MappingServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl.MappingServiceModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("MappingService module up!");
        mappingService = new MappingService();
        mappingService.setBindingAwareBroker(getBrokerDependency());
        mappingService.setDataBroker(getDataBrokerDependency());
        mappingService.setRpcProviderRegistry(getRpcRegistryDependency());
        mappingService.setNotificationService(getNotificationServiceDependency());
        mappingService.setDaoService(getDaoDependency());
        mappingService.initialize();

        return mappingService;
    }

}
