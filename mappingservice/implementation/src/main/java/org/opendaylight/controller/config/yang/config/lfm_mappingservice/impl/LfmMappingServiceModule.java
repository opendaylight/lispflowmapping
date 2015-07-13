/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.lfm_mappingservice.impl;

import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfmMappingServiceModule extends org.opendaylight.controller.config.yang.config.lfm_mappingservice.impl.AbstractLfmMappingServiceModule {
    private static final Logger LOG = LoggerFactory.getLogger(LfmMappingServiceModule.class);
    private LispMappingService lfmService;
    public LfmMappingServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LfmMappingServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lfm_mappingservice.impl.LfmMappingServiceModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("LfmMappingService Module up!");

        lfmService = new LispMappingService();
        lfmService.setDataBrokerService(getDataBrokerDependency());
        lfmService.setRpcProviderRegistry(getRpcRegistryDependency());
        lfmService.setBindingAwareBroker(getBrokerDependency());
        lfmService.setLispDao(getDaoDependency());
        lfmService.initialize();

        return lfmService;
    }

}