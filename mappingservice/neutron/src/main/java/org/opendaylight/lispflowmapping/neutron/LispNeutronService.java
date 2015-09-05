/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingserviceService;


public class LispNeutronService implements ILispNeutronService, BindingAwareProvider  {

	protected static final Logger LOG = LoggerFactory.getLogger(LispNeutronService.class);
    private IFlowMapping mappingService;
    private MappingserviceService lfmDbService;
    private static ILispNeutronService neutronService;


    void setBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {
        LOG.debug("LISP NEUTRON BindingAwareBroker set!");
        bindingAwareBroker.registerProvider(this);
        neutronService = this;
    }

    void unsetBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {

    }

    public static ILispNeutronService getLispNeutronService() {
        return neutronService;
    }


    public IFlowMapping getMappingService() {
        return this.mappingService;
    }

    public MappingserviceService getMappingDbService() {
        return this.lfmDbService;
    }

    public void setMappingService(IFlowMapping mappingService) {
        LOG.debug("MappingService set in Lisp Neutron");
        this.mappingService = mappingService;
    }

    public void unsetMappingService(IFlowMapping mappingService) {
        LOG.debug("MappingService was unset in LISP Neutron");
        this.mappingService = null;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("LFMDBSERVICE IS BEING FILLED! SESSION INITIATED");
        RpcProviderRegistry rpcRegistry = session.getSALService(RpcProviderRegistry.class);
        lfmDbService = rpcRegistry.getRpcService(MappingserviceService.class);
        LOG.debug("LFMDBSERVICE was FILLED! SESSION INITIATED");

    }

    public void stop() {
        LOG.info("LISP Neutron Service is down!");
    }

    public void destroy() {
        LOG.debug("LISP Neutron Service is destroyed!");
        mappingService = null;
    }

}
