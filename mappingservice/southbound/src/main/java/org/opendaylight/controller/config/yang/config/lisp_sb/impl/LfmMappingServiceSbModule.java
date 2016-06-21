/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.lisp_sb.impl;

import org.opendaylight.lispflowmapping.clustering.ClusterNodeModulSwitcherImpl;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfmMappingServiceSbModule extends org.opendaylight.controller.config.yang.config.lisp_sb.impl.AbstractLfmMappingServiceSbModule {
    private final static Logger LOG = LoggerFactory.getLogger(LfmMappingServiceSbModule.class);
    private LispSouthboundPlugin sbPlugin;

    public LfmMappingServiceSbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LfmMappingServiceSbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lisp_sb.impl.LfmMappingServiceSbModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("LfmMappingServiceSb Module up!");
        final ClusterNodeModulSwitcherImpl clusterNodeModulSwitcher = new ClusterNodeModulSwitcherImpl
                (getEntityOwnershipServiceDependency());

        sbPlugin = new LispSouthboundPlugin();
        clusterNodeModulSwitcher.setModule(sbPlugin);
        sbPlugin.setNotificationPublishService(getNotificationPublishServiceDependency());
        sbPlugin.setRpcRegistryDependency(getRpcRegistryDependency());
        sbPlugin.setDataBroker(getDataBrokerDependency());
        sbPlugin.setLispAddress(getBindAddress());
        sbPlugin.setMapRegisterCacheEnabled(getMapRegisterCache());
        sbPlugin.init();
        clusterNodeModulSwitcher.switchModuleByEntityOwnership();

        return sbPlugin;
    }

}
