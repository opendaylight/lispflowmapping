/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;


import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.Component;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.controller.networkconfig.neutron.INeutronNetworkAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronSubnetAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronPortAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application activator class for registering the dependencies and
 * Initializing the Mapping Service application.
 *
 */

public class Activator extends ComponentActivatorAbstractBase {

    /*
     * Logger instance
     */
    protected static final Logger logger = LoggerFactory.getLogger(Activator.class);

    /**
     * Function called when the activator starts just after some initializations
     * are done by the ComponentActivatorAbstractBase.
     *
     */
    @Override
    public void init() {
        logger.debug("LISP Neutron Service is initialized!");

    }

    /**
     * Function called when the activator stops just before the cleanup done by
     * ComponentActivatorAbstractBase
     *
     */
    @Override
    public void destroy() {
        logger.debug("LISP Neutron Service is destroyed!");
    }

    /**
     * Function that is used to communicate to dependency manager the list of
     * known implementations for services inside a container
     *
     *
     * @return An array containing all the CLASS objects that will be
     *         instantiated in order to get an fully working implementation
     *         Object
     */
    @Override
    public Object[] getImplementations() {
        Object[] res = { LispNeutronService.class,
        				 LispNeutronSubnetHandler.class,
        				 LispNeutronPortHandler.class,
        				 LispNeutronNetworkHandler.class};
        return res;
    }

    /**
     * Function that is called when configuration of the dependencies is
     * required.
     *
     * @param c
     *            dependency manager Component object, used for configuring the
     *            dependencies exported and imported
     * @param imp
     *            Implementation class that is being configured, needed as long
     *            as the same routine can configure multiple implementations
     * @param containerName
     *            The containerName being configured, this allow also optional
     *            per-container different behavior if needed, usually should not
     *            be the case though.
     */
    @Override
    public void configureInstance(Component c, Object imp, String containerName) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("name", "mappingservice");

        if (imp.equals(LispNeutronService.class)) {
            c.setInterface(ILispNeutronService.class.getName(), props);
        }

        if (imp.equals(LispNeutronNetworkHandler.class)) {
            c.setInterface(new String[] { ILispNeutronService.class.getName(), INeutronNetworkAware.class.getName()}, props);
        }

        if (imp.equals(LispNeutronSubnetHandler.class)) {
        	c.setInterface(new String[] { ILispNeutronService.class.getName(), INeutronSubnetAware.class.getName()}, props);
        }

        if (imp.equals(LispNeutronPortHandler.class)) {
            c.setInterface(new String[] { ILispNeutronService.class.getName(), INeutronPortAware.class.getName()}, props);
        }

        c.add(createContainerServiceDependency(containerName)
        		.setService(IFlowMapping.class)
        		.setCallbacks("setMappingService", "unsetMappingService")
                .setRequired(true));


    }

}
