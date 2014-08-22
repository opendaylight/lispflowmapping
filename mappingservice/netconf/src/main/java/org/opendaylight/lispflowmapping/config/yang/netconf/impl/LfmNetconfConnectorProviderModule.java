/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.config.yang.netconf.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.lispflowmapping.netconf.impl.LispDeviceNetconfConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.LfmNetconfConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfmNetconfConnectorProviderModule extends org.opendaylight.lispflowmapping.config.yang.netconf.impl.AbstractLfmNetconfConnectorProviderModule {
    private static final Logger log = LoggerFactory.getLogger(LfmNetconfConnectorProviderModule.class);

    public LfmNetconfConnectorProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LfmNetconfConnectorProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.lispflowmapping.config.yang.netconf.impl.LfmNetconfConnectorProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        final LispDeviceNetconfConnector lnconfConnector = LispDeviceNetconfConnector.createLispDeviceNetconfConnector();

        final BindingAwareBroker.RpcRegistration<LfmNetconfConnectorService> rpcRegistration = getRpcRegistryDependency()
                .addRpcImplementation(LfmNetconfConnectorService.class, lnconfConnector);


        // Wrap as AutoCloseable and close registrations to md-sal at close()
        final class AutoCloseableNCC implements AutoCloseable {

            @Override
            public void close() throws Exception {
                rpcRegistration.close();
                lnconfConnector.close();
                log.info("Lisp netconf connector (instance {}) torn down.", this);
            }
        }

        AutoCloseable ret = new AutoCloseableNCC();
        log.info("Lisp netconf connector provider (instance {}) initialized.", ret);
        return ret;

    }


}