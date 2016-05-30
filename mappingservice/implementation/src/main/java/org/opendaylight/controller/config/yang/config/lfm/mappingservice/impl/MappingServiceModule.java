/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

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
        mappingService.setNotificationPublishService(getNotificationPublishServiceDependency());
        mappingService.setDaoService(getDaoDependency());
        mappingService.setDataStoreBackEnd(getDsbackendDependency());
        mappingService.initialize();

        return mappingService;
    }

}
