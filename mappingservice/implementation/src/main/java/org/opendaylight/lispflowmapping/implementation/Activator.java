/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMappingShell;
import org.osgi.framework.BundleContext;

/**
 * Main application activator class for registering the dependencies and
 * initialising the Mapping Service application.
 *
 */

public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("name", "mappingservice");
        manager.add(createComponent().setInterface(new String[] { IFlowMapping.class.getName(), IFlowMappingShell.class.getName() }, props)
                .setImplementation(LispMappingService.class)
                .add(createServiceDependency().setService(BindingAwareBroker.class).setRequired(true)
                        .setCallbacks("setBindingAwareBroker", "unsetBindingAwareBroker")));

    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
        // TODO Auto-generated method stub

    }
}
