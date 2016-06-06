/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.lfm.mappingservice_dao.inmemorydb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;

public class InMemoryDbModuleTest {

    private InMemoryDbModule module;

    @Before
    public void setUp() throws Exception {
        new InMemoryDbModule(
                Mockito.mock(ModuleIdentifier.class),
                Mockito.mock(DependencyResolver.class),
                Mockito.mock(InMemoryDbModule.class),
                Mockito.mock(AutoCloseable.class)
        );

        module = new InMemoryDbModule(
                Mockito.mock(ModuleIdentifier.class),
                Mockito.mock(DependencyResolver.class)
        );
    }

    @Test
    public void testCustomValidation() throws Exception {
        module.customValidation();
    }

    @Test
    public void testCreateInstance() throws Exception {
        int recordTimeout = 200;
        module.setRecordTimeout(recordTimeout);
        AutoCloseable instance = module.createInstance();
        Assert.assertTrue(instance instanceof HashMapDb);
        Assert.assertEquals(recordTimeout, ((HashMapDb) instance).getRecordTimeOut());
    }
}
