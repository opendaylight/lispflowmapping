/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.clustering;

import static org.opendaylight.lispflowmapping.clustering.util.ClusteringUtil.LISPFLOWMAPPING_ENTITY_NAME;
import static org.opendaylight.lispflowmapping.clustering.util.ClusteringUtil.LISPFLOWMAPPING_ENTITY_TYPE;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.lispflowmapping.clustering.api.ClusterNodeModuleSwitcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for turning on|off module in node cluster.
 */
public class ClusterNodeModulSwitcherImpl implements EntityOwnershipListener  {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterNodeModulSwitcherImpl.class);
    private final EntityOwnershipService entityOwnershipService;
    private final Entity entity;
    private boolean moduleIsRunning = true;
    private ClusterNodeModuleSwitcher module;

    public ClusterNodeModulSwitcherImpl(final EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
        this.entityOwnershipService.registerListener(LISPFLOWMAPPING_ENTITY_NAME, this);
        entity = new Entity(LISPFLOWMAPPING_ENTITY_NAME, LISPFLOWMAPPING_ENTITY_TYPE);
        try {
            this.entityOwnershipService.registerCandidate(entity);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.debug("Candidate already registered. Trace: {}", e);
        }
    }

    @Override
    public void ownershipChanged(final EntityOwnershipChange entityOwnershipChange) {
        LOG.debug("Entity ownership change message received.");
        switchModuleState(entityOwnershipChange.isOwner());
    }

    public void switchModuleByEntityOwnership() {
        switchModuleState(isMaster());
    }

    private void switchModuleState(final boolean isOwner) {
        if (module != null) {
            if (!isOwner && moduleIsRunning) {
                module.stopModule();
                moduleIsRunning = false;
                LOG.debug("Module {} was stopped.", module.getClass().getName());
            } else if (isOwner && !moduleIsRunning) {
                module.startModule();
                moduleIsRunning = true;
                LOG.debug("Module {} was restarted.", module.getClass().getName());
            }
        } else {
            LOG.debug("Module wasn't initialized yet.");
        }
    }

    public boolean isMaster() {
        final Optional<EntityOwnershipState> ownershipState = entityOwnershipService.getOwnershipState(entity);
        if (ownershipState.isPresent()) {
            return ownershipState.get().isOwner();
        } else {
            LOG.debug("Ownership state information wasn't present in entity ownership service.");
        }
        return false;
    }

    public void setModule(final ClusterNodeModuleSwitcher module) {
        this.module = module;
    }
}
