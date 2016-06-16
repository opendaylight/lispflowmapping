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
 * Class responsible for turning on|off module in node cluster
 */
public class ClusterNodeModulSwitcherImpl implements EntityOwnershipListener  {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterNodeModulSwitcherImpl.class);;
    private final EntityOwnershipService entityOwnershipService;
    private final Entity entity;
    private boolean moduleIsRunning;
    private ClusterNodeModuleSwitcher module;

    public ClusterNodeModulSwitcherImpl(final EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
        this.entityOwnershipService.registerListener(LISPFLOWMAPPING_ENTITY_NAME, this);
        entity = new Entity(LISPFLOWMAPPING_ENTITY_NAME, LISPFLOWMAPPING_ENTITY_TYPE);
        try {
            this.entityOwnershipService.registerCandidate(entity);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.debug("Candidate already registered.");
        }
    }

    @Override
    public void ownershipChanged(final EntityOwnershipChange entityOwnershipChange) {
        LOG.debug("Entity ownership change message obtained.");

        final boolean isOwner = entityOwnershipChange.isOwner();
        final boolean wasOwner = entityOwnershipChange.wasOwner();
        if (module != null) {
            //lost entity ownership
            if (!isOwner && (wasOwner || moduleIsRunning)) {
                module.stopModule();
                moduleIsRunning = false;
                LOG.debug("Module {} was stopped.", module.getClass().getName());
                //become entity owner
            } else if (isOwner && !wasOwner) {
                module.startModule();
                moduleIsRunning = true;
                LOG.debug("Module {} was restarted.", module.getClass().getName());
            }
        } else {
            LOG.debug("Module wasn't initialized yet.");
        }
    }

    public void switchModuleByEntityOwnership() {
        final Optional<EntityOwnershipState> ownershipState = entityOwnershipService.getOwnershipState(entity);
        if (ownershipState.isPresent()) {
            if (!ownershipState.get().isOwner()) {
                module.stopModule();
                moduleIsRunning = false;
            } else {
                moduleIsRunning = true;
            }
        }

    }

    public void setModule(final ClusterNodeModuleSwitcher module) {
        this.module = module;
    }
}
