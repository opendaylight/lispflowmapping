package org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl;

import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMappingServiceModule extends org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl.AbstractLispMappingServiceModule {
    private static final Logger LOG = LoggerFactory.getLogger(LispMappingServiceModule.class);

    private LispMappingService lmsService;

    public LispMappingServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LispMappingServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl.LispMappingServiceModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("LispMappingService Module up!");

        lmsService = new LispMappingService();
        lmsService.setBindingAwareBroker(getOsgiBrokerDependency());
        lmsService.setMappingService(getMappingserviceDependency());
        lmsService.initialize();

        return lmsService;
    }

}
