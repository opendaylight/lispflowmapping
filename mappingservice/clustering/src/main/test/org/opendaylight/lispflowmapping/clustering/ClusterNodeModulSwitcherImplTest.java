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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.lispflowmapping.clustering.api.ClusterNodeModuleSwitcher;

public class ClusterNodeModulSwitcherImplTest  {

    private EntityOwnershipService entityOwnershipServiceMocked;
    private ClusterNodeModulSwitcherImpl clusterNodeModulSwitcherImpl;
    private ClusterNodeModuleSwitcher module;
    private Entity entity = new Entity(LISPFLOWMAPPING_ENTITY_NAME, LISPFLOWMAPPING_ENTITY_TYPE);

    @Before
    public void init() {
        entityOwnershipServiceMocked = Mockito.mock(EntityOwnershipService.class);
        clusterNodeModulSwitcherImpl = new ClusterNodeModulSwitcherImpl(entityOwnershipServiceMocked);
        module = Mockito.mock(ClusterNodeModuleSwitcher.class);
    }

    @Test
    public void constructorCallTest() throws CandidateAlreadyRegisteredException {
        Mockito.verify(entityOwnershipServiceMocked).registerListener(Matchers.eq(LISPFLOWMAPPING_ENTITY_NAME),
                Matchers.any(ClusterNodeModulSwitcherImpl.class));
        Mockito.verify(entityOwnershipServiceMocked).registerCandidate(Matchers.eq(entity));
    }

    @Test
    public void ownershipChangedTest() {
        //is not owner
        ownershipChanged(false);
        Mockito.verify(module).stopModule();
        //is owner
        ownershipChanged(true);
        Mockito.verify(module).startModule();
    }

    @Test
    public void isMasterTest_OptionalAbsent() {
        Mockito.when(entityOwnershipServiceMocked.getOwnershipState(Matchers.eq(entity))).thenReturn(Optional
                .absent
                        ());
        Assert.assertFalse(clusterNodeModulSwitcherImpl.isMaster());
    }

    @Test
    public void isMasterTest_True() {
        Mockito.when(entityOwnershipServiceMocked.getOwnershipState(Matchers.eq(entity))).thenReturn(Optional
                .of(new EntityOwnershipState(true, true)));
        Assert.assertTrue(clusterNodeModulSwitcherImpl.isMaster());
    }

    @Test
    public void isMasterTest_False() {
        Mockito.when(entityOwnershipServiceMocked.getOwnershipState(Matchers.eq(entity))).thenReturn(Optional
                .of(new EntityOwnershipState(false, true)));
        Assert.assertFalse(clusterNodeModulSwitcherImpl.isMaster());
    }

    @Test
    public void switchModuleByEntityOwnershipTest() {
        Mockito.when(entityOwnershipServiceMocked.getOwnershipState(Matchers.eq(entity))).thenReturn(Optional
                .of(new EntityOwnershipState(false, true)));
        clusterNodeModulSwitcherImpl.switchModuleByEntityOwnership();
    }

    private void ownershipChanged(boolean isOwner) {
        final EntityOwnershipChange entityOwnershipChangeMock = Mockito.mock(EntityOwnershipChange.class);
        clusterNodeModulSwitcherImpl.setModule(module);
        Mockito.when(entityOwnershipChangeMock.isOwner()).thenReturn(isOwner);
        clusterNodeModulSwitcherImpl.ownershipChanged(entityOwnershipChangeMock);
    }
}
