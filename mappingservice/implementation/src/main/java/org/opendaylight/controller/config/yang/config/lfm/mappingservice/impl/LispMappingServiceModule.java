/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl;

import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.lispflowmapping.clustering.ClusterNodeModulSwitcherImpl;
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMappingServiceModule extends org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl
        .AbstractLispMappingServiceModule {
    private static final Logger LOG = LoggerFactory.getLogger(LispMappingServiceModule.class);

    private LispMappingService lmsService;
    private EntityOwnershipService entityOwnershipService;
    private Entity entity;
    private boolean moduleIsRunning;


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
        final ClusterNodeModulSwitcherImpl clusterNodeModulSwitcher = new ClusterNodeModulSwitcherImpl(
                getEntityOwnershipServiceDependency());

        lmsService = new LispMappingService();
        clusterNodeModulSwitcher.setModule(lmsService);
        lmsService.setBindingAwareBroker(getOsgiBrokerDependency());
        lmsService.setNotificationService(getNotificationServiceDependency());
        lmsService.setMappingService(getMappingserviceDependency());
        lmsService.initialize();
        clusterNodeModulSwitcher.switchModuleByEntityOwnership();

        return lmsService;
    }

}
