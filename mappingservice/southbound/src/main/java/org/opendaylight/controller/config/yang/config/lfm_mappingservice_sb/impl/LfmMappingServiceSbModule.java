/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.lfm_mappingservice_sb.impl;

import java.net.DatagramPacket;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundRPC;
import org.opendaylight.lispflowmapping.southbound.lisp.ILispSouthboundService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class LfmMappingServiceSbModule extends org.opendaylight.controller.config.yang.config.lfm_mappingservice_sb.impl.AbstractLfmMappingServiceSbModule {
    private final static Logger LOG = LoggerFactory.getLogger(LfmMappingServiceSbModule.class);
    private LispSouthboundPlugin sbPlugin;

    public LfmMappingServiceSbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LfmMappingServiceSbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lfm_mappingservice_sb.impl.LfmMappingServiceSbModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("LfmMappingServiceSb Module up!");

        sbPlugin = new LispSouthboundPlugin();
        sbPlugin.setNotificationProviderService(getNotificationServiceDependency());
        sbPlugin.setRpcRegistryDependency(getRpcRegistryDependency());
        sbPlugin.setBindingAwareBroker(getBrokerDependency());
        sbPlugin.init();

        return sbPlugin;
    }

}
