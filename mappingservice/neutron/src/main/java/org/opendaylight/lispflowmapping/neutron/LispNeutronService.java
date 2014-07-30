/*
 * Copyright (c) 2014 Cisco, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.controller.networkconfig.neutron.INeutronNetworkAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronNetwork;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispNeutronService implements ILispNeutronService, INeutronNetworkAware {
    protected static final Logger logger = LoggerFactory.getLogger(LispNeutronService.class);
    private IFlowMapping mappingService;

    public IFlowMapping getMappingService() {
        return this.mappingService;
    }

    void setFlowMappingService(IFlowMapping mappingService) {
        logger.debug("FlowMapping set in LispNeutron");
        this.mappingService = mappingService;
    }

    void unsetFlowMappingService(IFlowMapping mappingService) {
        logger.debug("LispDAO was unset in LISP Neutron");
        this.mappingService = null;
    }

    public void init() {
        logger.debug("LISP Neutron Service is initialized!");
    }

    public void start() {
        logger.info("LISP Neutron Service is up!");

        // OSGI console
        registerWithOSGIConsole();
    }

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }

    public void stop() {
        logger.info("LISP Neutron Service is down!");
    }

    public void destroy() {
        logger.debug("LISP Neutron Service is destroyed!");
        mappingService = null;
    }

    // callbacks for INeutronNetworkAware. Based on OpenDove project

    public int canCreateNetwork(NeutronNetwork network) {
        logger.info("hello world!");
        logger.info("canCreateNetwork called!");
        logger.info("Network name: " + network.getNetworkName());
        // if (network.getAdminStateUp() != null && !network.isAdminStateUp())
        // return 400;
        return 200;
    }

    public void neutronNetworkCreated(NeutronNetwork input) {
        logger.info("neutronNetworkCreated called!");
        return;
    }

    public int canUpdateNetwork(NeutronNetwork delta, NeutronNetwork original) {
        logger.info("canUpdateNetwork called!");
        if (delta.getNetworkName() != null || delta.getAdminStateUp() != null || delta.getShared() != null || delta.getRouterExternal() != null)
            return 403;
        return 200;
    }

    public void neutronNetworkUpdated(NeutronNetwork network) {
        logger.info("neutronNetworkUpdated called!");
        return;
    }

    public int canDeleteNetwork(NeutronNetwork network) {
        logger.info("canDeleteNetwork called!");
        return 200;
    }

    public void neutronNetworkDeleted(NeutronNetwork network) {
        logger.info("neutronNetworkDeleted called!");
        return;
    }

}
