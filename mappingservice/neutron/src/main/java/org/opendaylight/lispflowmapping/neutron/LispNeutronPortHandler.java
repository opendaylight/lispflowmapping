/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;

import java.net.HttpURLConnection;

import org.opendaylight.neutron.spi.INeutronPortAware;
import org.opendaylight.neutron.spi.NeutronPort;

/**
 * Lisp Service implementation of NeutronPortAware API
 * Creation of a new port results adding the mapping for
 * the port's IP addresses to the port's host_ip in the mapping service.
 * Currently the NetronPort object does not carry the required information
 * to achieve the port's host_ip. Once such extension is available
 * this class shall be updated.
 *
 */
public class LispNeutronPortHandler extends LispNeutronService implements INeutronPortAware {


	@Override
	public int canCreatePort(NeutronPort port) {
        LOG.info("Neutron canCreatePort : Port name: " + port.getName());

        return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronPortCreated(NeutronPort port) {
		// TODO Consider adding Port MAC -> Port fixed IP in MS
		// TODO Add Port fixed ip -> host ip mapping in MS - Requires extension of Port object

        LOG.info("Neutron Port Created: Port name: " + port.getName() + " Port Fixed IP: " +
        		( port.getFixedIPs() != null ? port.getFixedIPs().get(0) : "No Fixed IP assigned" ));
        LOG.debug("Neutron Port Created : " + port.toString());

	}

	@Override
	public int canUpdatePort(NeutronPort delta, NeutronPort original) {
		// TODO Port IP and port's host ip is stored by Lisp Neutron Service. If there is change to these fields, the update needs to be processed.

		LOG.info("Neutron canUpdatePort : Port name: " + original.getName() + " Port Fixed IP: " +
	        		( original.getFixedIPs() != null ? original.getFixedIPs().get(0) : "No Fixed IP assigned" ));
		LOG.debug("Neutron canUpdatePort : original" + original.toString() + " delta : " + delta.toString());

        return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronPortUpdated(NeutronPort port) {
		// TODO Port IP and port's host ip is stored by Lisp Neutron Service. If there is change to these fields, the update needs to be processed.

		LOG.info("Neutron Port updated: Port name: " + port.getName() + " Port Fixed IP: " +
        		( port.getFixedIPs() != null ? port.getFixedIPs().get(0) : "No Fixed IP assigned" ));
		LOG.debug("Neutron Port Updated : " + port.toString());

	}

	@Override
	public int canDeletePort(NeutronPort port) {
		// TODO Check if Port IPs are stored by Lisp Neutron Service. if not return error code.

		LOG.info("Neutron canDeletePort : Port name: " + port.getName() + " Port Fixed IP: " +
        		( port.getFixedIPs() != null ? port.getFixedIPs().get(0) : "No Fixed IP assigned" ));
		LOG.debug("Neutron canDeltePort: " + port.toString());

		return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronPortDeleted(NeutronPort port) {
		// TODO if port ips existed in MapServer, delete them. Else, log error.

		LOG.info("Neutron Port Deleted: Port name: " + port.getName() + " Port Fixed IP: " +
        		( port.getFixedIPs() != null ? port.getFixedIPs().get(0) : "No Fixed IP assigned" ));
		LOG.debug("Neutron Port Deleted : " + port.toString());

	}
}
