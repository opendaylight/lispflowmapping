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

import org.apache.felix.dm.Component;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.inventory.IPluginInInventoryService;
import org.opendaylight.controller.sal.inventory.IPluginOutInventoryService;
import org.opendaylight.controller.sal.utils.GlobalConstants;
import org.opendaylight.controller.sal.utils.INodeConnectorFactory;
import org.opendaylight.controller.sal.utils.INodeFactory;
import org.opendaylight.lispflowmapping.implementation.inventory.AdSalLispInventoryService;
import org.opendaylight.lispflowmapping.implementation.inventory.IAdSalLispInventoryService;
import org.opendaylight.lispflowmapping.implementation.inventory.NodeConnectorFactory;
import org.opendaylight.lispflowmapping.implementation.inventory.NodeFactory;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application activator class for registering the dependencies and
 * initialising the Mapping Service application.
 *
 */

public class Activator extends ComponentActivatorAbstractBase {
    protected static final Logger logger = LoggerFactory.getLogger(Activator.class);

    /**
     * Function called when the activator starts just after some initializations
     * are done by the ComponentActivatorAbstractBase.
     *
     */
    @Override
    public void init() {
        logger.debug("Registering \"LISP\" NodeID and NodeConnectorID types");
        Node.NodeIDType.registerIDType("LISP", String.class);
        NodeConnector.NodeConnectorIDType.registerIDType("LISP", String.class, "LISP");
    }

    /**
     * Function called when the activator stops just before the cleanup done by
     * ComponentActivatorAbstractBase
     *
     */
    @Override
    public void destroy() {
        logger.debug("Unregistering \"LISP\" NodeID and NodeConnectorID types");
        Node.NodeIDType.unRegisterIDType("LISP");
        NodeConnector.NodeConnectorIDType.unRegisterIDType("LISP");
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
        Object[] res = {  NodeFactory.class, NodeConnectorFactory.class,
                          AdSalLispInventoryService.class, LispMappingService.class };
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
        if (imp.equals(NodeFactory.class)) {
            // export the service to be used by SAL
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            // Set the protocolPluginType property which will be used
            // by SAL
            props.put(GlobalConstants.PROTOCOLPLUGINTYPE.toString(), "LISP");
            props.put("protocolName", "LISP");
            c.setInterface(INodeFactory.class.getName(), props);
        }

        if (imp.equals(NodeConnectorFactory.class)) {
            // export the service to be used by SAL
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            // Set the protocolPluginType property which will be used
            // by SAL
            props.put(GlobalConstants.PROTOCOLPLUGINTYPE.toString(), "LISP");
            props.put("protocolName", "LISP");
            c.setInterface(INodeConnectorFactory.class.getName(), props);
        }

         if (imp.equals(AdSalLispInventoryService.class)) {
            logger.debug("Exporting AdSalLispInventoryService for IAdSalLispInventoryService and IPluginInInventoryService interfaces");
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put(GlobalConstants.PROTOCOLPLUGINTYPE.toString(), "LISP");
            c.setInterface(
                    new String[] {IPluginInInventoryService.class.getName(),
                            IAdSalLispInventoryService.class.getName()}, props);
            c.add(createServiceDependency()
                    .setService(IPluginOutInventoryService.class, "(scope=Global)")
                    .setCallbacks("setPluginOutInventoryServices",
                            "unsetPluginOutInventoryServices")
                    .setRequired(true));
        }

        if (imp.equals(LispMappingService.class)) {
            // export the service
            logger.debug("Exporting LispMappingService for IFlowMapping interface");
            Dictionary<String, String> props = new Hashtable<String, String>();
            props.put("name", "mappingservice");
            c.setInterface(new String[] { IFlowMapping.class.getName() }, props);
            c.add(createContainerServiceDependency(containerName).setService(ILispDAO.class).setCallbacks("setLispDao", "unsetLispDao")
                    .setRequired(true));
            c.add(createServiceDependency().setService(BindingAwareBroker.class).setRequired(true)
                    .setCallbacks("setBindingAwareBroker", "unsetBindingAwareBroker"));
            c.add(createServiceDependency().setService(IAdSalLispInventoryService.class).setRequired(true)
                    .setCallbacks("setInventoryService", "unsetInventoryService"));
        }
   }

    /**
     * Method which tells how many Global implementations are supported by the
     * bundle. This way we can tune the number of components created. This
     * components will be created ONLY at the time of bundle startup and will be
     * destroyed only at time of bundle destruction, this is the major
     * difference with the implementation retrieved via getImplementations where
     * all of them are assumed to be in a container !
     *
     *
     * @return The list of implementations the bundle will support, in Global
     *         version
     */
    @Override
    protected Object[] getGlobalImplementations() {
        return null;
    }

    /**
     * Configure the dependency for a given instance Global
     *
     * @param c
     *            Component assigned for this instance, this will be what will
     *            be used for configuration
     * @param imp
     *            implementation to be configured
     * @param containerName
     *            container on which the configuration happens
     */
    @Override
    protected void configureGlobalInstance(Component c, Object imp) {
    }
}
