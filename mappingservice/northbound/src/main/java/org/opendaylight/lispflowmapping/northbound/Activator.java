/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.northbound;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application activator class for registering the dependencies and
 * initialising the Mapping Service application.
 *
 */

public class Activator extends DependencyActivatorBase {
    protected static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("name", "mappingservice");
        manager.add(createComponent().setInterface(new String[] { ILispmappingNorthbound.class.getName() }, props)
                .setImplementation(LispMappingNorthbound.class)
                .add(createServiceDependency().setService(IFlowMapping.class)
                        .setCallbacks("setFlowMappingService", "unsetFlowMappingService").setRequired(true)));

        LOG.info("LISP Northbound Service is up!");
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
        LOG.info("LISP Northbound Service is destroyed!");
    }
}
