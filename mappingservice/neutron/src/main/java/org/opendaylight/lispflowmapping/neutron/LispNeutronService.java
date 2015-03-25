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
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;



public class LispNeutronService implements ILispNeutronService {

	protected static final Logger LOG = LoggerFactory.getLogger(LispNeutronService.class);
    protected IFlowMapping mappingService;

    public IFlowMapping getMappingService() {
        return this.mappingService;
    }

    public void setMappingService(IFlowMapping mappingService) {
        LOG.debug("MappingService set in Lisp Neutron");
        this.mappingService = mappingService;
    }

    public void unsetMappingService(IFlowMapping mappingService) {
        LOG.debug("MappingService was unset in LISP Neutron");
        this.mappingService = null;
    }

 /*   protected IContainerManager containerManager;

    public IContainerManager getContainerManager() {
        return containerManager;
    }

    public void unsetContainerManager(IContainerManager s) {
        if (s == this.containerManager) {
            this.containerManager = null;
        }
    }

    public void setContainerManager(IContainerManager s) {
        this.containerManager = s;
    }
*/
    public void stop() {
        LOG.info("LISP Neutron Service is down!");
    }

    public void destroy() {
        LOG.debug("LISP Neutron Service is destroyed!");
        mappingService = null;
    }

}
