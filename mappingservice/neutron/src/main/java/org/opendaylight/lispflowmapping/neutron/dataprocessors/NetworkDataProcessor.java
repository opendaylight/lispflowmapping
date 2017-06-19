/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.dataprocessors;

import org.opendaylight.lispflowmapping.neutron.ILispNeutronService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LISP Service Implementation of creation and deletion of a Network.
 */
public class NetworkDataProcessor implements DataProcessor<Network> {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkDataProcessor.class);
    private volatile ILispNeutronService lispNeutronService;

    public NetworkDataProcessor(ILispNeutronService lispNeutronService) {
        this.lispNeutronService = lispNeutronService;
    }

    @Override
    public void create(Network objectAfter) {
        LOG.info("Neutron Network Created : Network name: {}", objectAfter.getName());
        LOG.debug("Lisp Neutron Network: {}", objectAfter.toString());
    }

    @Override
    public void update(Network objectBefore, Network objectAfter) {
        LOG.info("Neutron Network Updated : Network name: {}", objectAfter.getName());
        LOG.debug("Lisp Neutron Network: {}", objectAfter.toString());
    }

    @Override
    public void delete(Network objectBefore) {
        LOG.info("Neutron Network Deleted : Network name: {}", objectBefore.getName());
        LOG.debug("Lisp Neutron Network: {}", objectBefore.toString());
    }
}
