/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.HostInformationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispNeutronService implements ILispNeutronService {

    private static final Logger LOG = LoggerFactory.getLogger(LispNeutronService.class);
    private IFlowMapping mappingService;
    private OdlMappingserviceService lfmDbService;
    private final DataBroker broker;

    public LispNeutronService(IFlowMapping mappingService, DataBroker dataBroker,
            OdlMappingserviceService odlMappingService) {
        this.mappingService = mappingService;
        this.broker = dataBroker;

        HostInformationManager.getInstance().setOdlMappingserviceService(odlMappingService);
        LOG.info("LISP NEUTRON SERVICE has been registered");
    }

    @Override
    public IFlowMapping getMappingService() {
        return this.mappingService;
    }

    @Override
    public OdlMappingserviceService getMappingDbService() {
        return this.lfmDbService;
    }

    public void init() {
        LOG.info("LFMDBSERVICE IS BEING FILLED! SESSION INITIATED");
        DelegatingDataTreeListener.initiateListener(Network.class, this, broker);
        DelegatingDataTreeListener.initiateListener(Subnet.class, this, broker);
        DelegatingDataTreeListener.initiateListener(Port.class, this, broker);
        LOG.debug("LFMDBSERVICE was FILLED! SESSION INITIATED");
    }

    public void close() {
        mappingService = null;
        LOG.info("LISP Neutron Service is down!");
    }
}
