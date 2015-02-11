/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;


import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.controller.networkconfig.neutron.INeutronNetworkAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronSubnetAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronPortAware;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application activator class for registering the dependencies and
 * Initializing the Mapping Service application.
 *
 */

public class Activator extends DependencyActivatorBase {

    protected static final Logger logger = LoggerFactory.getLogger(Activator.class);

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("name", "mappingservice");

        manager.add(createComponent()
                .setInterface(ILispNeutronService.class.getName(), null)
                .setImplementation(LispNeutronService.class)
                .add(createServiceDependency().setService(IFlowMapping.class)));

        manager.add(createComponent()
                .setInterface(new String[] { ILispNeutronService.class.getName(), INeutronNetworkAware.class.getName()}, props)
                .setImplementation(LispNeutronNetworkHandler.class));

        manager.add(createComponent()
                .setInterface(new String[] { ILispNeutronService.class.getName(), INeutronSubnetAware.class.getName()}, props)
                .setImplementation(LispNeutronSubnetHandler.class)
                .add(createServiceDependency().setService(ILispNeutronService.class)));

        manager.add(createComponent()
                .setInterface(new String[] { ILispNeutronService.class.getName(), INeutronPortAware.class.getName()}, props)
                .setImplementation(LispNeutronPortHandler.class));

        logger.debug("LISP Neutron Service is initialized!");

    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
        logger.debug("LISP Neutron Service is destroyed!");
    }

}
