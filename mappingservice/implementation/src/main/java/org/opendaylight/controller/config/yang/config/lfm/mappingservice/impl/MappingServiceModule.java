/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.opendaylight.lispflowmapping.common.LispflowmappingUtil.LISPFLOWMAPPING_ENTITY_NAME;
import static org.opendaylight.lispflowmapping.common.LispflowmappingUtil.LISPFLOWMAPPING_ENTITY_TYPE;

public class MappingServiceModule extends org.opendaylight.controller.config.yang.config.lfm.mappingservice.impl
        .AbstractMappingServiceModule implements EntityOwnershipListener {
    private static final Logger LOG = LoggerFactory.getLogger(MappingServiceModule.class);
    private MappingService mappingService;
    private EntityOwnershipService entityOwnershipService;
    private Entity entity;
    private boolean moduleIsRunning;

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
        entityOwnershipService = getEntityOwnershipServiceDependency();
        entityOwnershipService.registerListener(LISPFLOWMAPPING_ENTITY_NAME, this);
        entity = new Entity(LISPFLOWMAPPING_ENTITY_NAME, LISPFLOWMAPPING_ENTITY_TYPE);
        try {
            entityOwnershipService.registerCandidate(entity);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.debug("Candidate already registered.");
        }

        mappingService = new MappingService();
        mappingService.setBindingAwareBroker(getBrokerDependency());
        mappingService.setDataBroker(getDataBrokerDependency());
        mappingService.setRpcProviderRegistry(getRpcRegistryDependency());
        mappingService.setNotificationPublishService(getNotificationPublishServiceDependency());
        mappingService.setDaoService(getDaoDependency());
        mappingService.initialize();
        final Optional<EntityOwnershipState> ownershipState = entityOwnershipService.getOwnershipState(entity);
        if (ownershipState.isPresent()) {
            if (!ownershipState.get().isOwner()) {
                mappingService.stopModuleServices();
                moduleIsRunning = false;
            } else {
                moduleIsRunning = true;
            }
        }

        return mappingService;
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange entityOwnershipChange) {
        LOG.debug("Entity ownership change message obtained.");

        final boolean isOwner = entityOwnershipChange.isOwner();
        final boolean wasOwner = entityOwnershipChange.wasOwner();
        //lost entity ownership
        if (!isOwner && (wasOwner || moduleIsRunning)) {
            moduleIsRunning = false;
            mappingService.stopModuleServices();
        //become entity owner
        } else if (isOwner && !wasOwner) {
            mappingService.startModuleServices();
            moduleIsRunning = true;
        }
    }

}
