/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.lfm.mappingservice_shell.impl;

import org.opendaylight.lispflowmapping.implementation.MappingServiceShell;

public class MappingServiceShellModule extends org.opendaylight.controller.config.yang.config.lfm.mappingservice_shell.impl.AbstractMappingServiceShellModule {
    private MappingServiceShell mappingServiceShell;

    public MappingServiceShellModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public MappingServiceShellModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lfm.mappingservice_shell.impl.MappingServiceShellModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        mappingServiceShell = new MappingServiceShell();
        mappingServiceShell.setBindingAwareBroker(getOsgiBrokerDependency());
        mappingServiceShell.setMappingService(getMappingserviceDependency());
        return mappingServiceShell;
    }

}
