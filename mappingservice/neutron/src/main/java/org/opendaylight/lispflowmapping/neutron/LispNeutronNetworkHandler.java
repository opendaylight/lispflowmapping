/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;

import java.net.HttpURLConnection;

import org.opendaylight.neutron.spi.INeutronNetworkAware;
import org.opendaylight.neutron.spi.NeutronNetwork;

/**
 * LISP Service Implementation of NeutronNetworkAware API
 *
 */
public class LispNeutronNetworkHandler extends LispNeutronService implements INeutronNetworkAware {


	@Override
	public int canCreateNetwork(NeutronNetwork network) {
        logger.info("Neutron canCreateNetwork : Network name: " + network.getNetworkName());
        logger.debug("Lisp Neutron Network: " + network.toString());

        return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronNetworkCreated(NeutronNetwork network) {
        logger.info("Neutron Network Created : Network name: " + network.getNetworkName());
        logger.debug("Lisp Neutron Network: " + network.toString());

	}

	@Override
	public int canUpdateNetwork(NeutronNetwork delta, NeutronNetwork original) {
        logger.info("Neutron canUpdateNetwork : Network name: " + original.getNetworkName());
        logger.debug("Lisp Neutron Network: " + original.toString());

        return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronNetworkUpdated(NeutronNetwork network) {
        logger.info("Neutron Network Updated : Network name: " + network.getNetworkName());
        logger.debug("Lisp Neutron Network: " + network.toString());

	}

	@Override
	public int canDeleteNetwork(NeutronNetwork network) {
        logger.info("Neutron canDeleteNetwork : Network name: " + network.getNetworkName());
        logger.debug("Lisp Neutron Network: " + network.toString());

        return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronNetworkDeleted(NeutronNetwork network) {
        logger.info("Neutron Network Deleted : Network name: " + network.getNetworkName());
        logger.debug("Lisp Neutron Network: " + network.toString());

        return;
	}

}
