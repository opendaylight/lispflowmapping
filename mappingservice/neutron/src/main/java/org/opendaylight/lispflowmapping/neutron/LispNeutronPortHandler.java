/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;

import java.net.HttpURLConnection;
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.util.MapServerMapResolverUtil;
import org.opendaylight.neutron.spi.INeutronPortAware;
import org.opendaylight.neutron.spi.NeutronPort;
import org.opendaylight.neutron.spi.Neutron_IPs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;

/**
 * Lisp Service implementation of NeutronPortAware API Creation of a new port
 * results adding the mapping for the port's IP addresses to the port's host_ip
 * in the mapping service. Currently the NetronPort object does not carry the
 * required information to achieve the port's host_ip. Once such extension is
 * available this class shall be updated.
 *
 */
public class LispNeutronPortHandler extends LispNeutronService implements
		INeutronPortAware {

	// The implementation for each of these services is resolved by the OSGi
	// Service Manager
	private volatile ILispNeutronService lispNeutronService;

	// private static final Integer SIX = Integer.valueOf(6);

	@Override
	public int canCreatePort(NeutronPort port) {
		LOG.info("Neutron canCreatePort : Port name: " + port.getName());

		return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronPortCreated(NeutronPort port) {

		// TODO Consider adding Port MAC -> Port fixed IP in MS
		// TODO Add Port fixed ip -> host ip , if Port.host_id mapping to
		// host_ip exists in MS

		LOG.info("Neutron Port Created: Port name: "
				+ port.getName()
				+ " Port Fixed IP: "
				+ (port.getFixedIPs() != null ? port.getFixedIPs().get(0)
						: "No Fixed IP assigned"));
		LOG.debug("Neutron Port Created : " + port.toString());

		// Check if port.hostID is in map-server, if it is, get host eidtoloc
		// record?
		if (port.getBindinghostID() == null) {
			LOG.error("Adding new Neutron port to lisp service mapping service failed. Port does not have Host_ID. Port : "
					+ port.toString());
			return;
		}
		// LispAFIAddress hostAddress =
		// LispAFIConvertor.asDistinguishedNameAddress(port.getBindinghostID());
		LispAFIAddress hostAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedNameBuilder()
				.setDistinguishedName(port.getBindinghostID()).build();
		MapRequest request = MapServerMapResolverUtil.getMapRequest(
				LispAFIConvertor.toContainer(hostAddress), (short) 0);

		MapReply reply = lispNeutronService.getMappingService()
				.handleMapRequest(request, false);

		if (reply == null) {
			LOG.error("Adding new Neutron port to lisp service mapping service failed. Host_ID mapping does not exist. Port : "
					+ port.toString());
			return;
		}
		// TODO for now only selecting the first EidToLocatorRecord from the
		// Host_ID mapping
		List<LocatorRecord> hostLocRecords = reply.getEidToLocatorRecord()
				.get(0).getLocatorRecord();

		List<Neutron_IPs> fixedIPs = port.getFixedIPs();
		if (fixedIPs != null && fixedIPs.size() > 0) {
			EidToLocatorRecordBuilder etlrBuilder;
			MapRegister mapRegister;
			LispIpv4Address eidAddress;
			for (Neutron_IPs ip : fixedIPs) {

				// TODO Add check/support for IPv6.
				// Get subnet for this port, based on v4 or v6 decide address
				// iana code.

				eidAddress = LispAFIConvertor.asIPAfiAddress(ip.getIpAddress());

				// Prepare Map Register
				etlrBuilder = new EidToLocatorRecordBuilder();
				etlrBuilder.setLispAddressContainer(LispAFIConvertor
						.toContainer(eidAddress));
				etlrBuilder.setMaskLength((short) 32);
				etlrBuilder.setLocatorRecord(hostLocRecords);
				mapRegister = MapServerMapResolverUtil
						.getMapRegister(etlrBuilder.build());
				LOG.info("Neutron Port mapping added to lisp: "
						+ " Port Fixed IP: " + ip + "Port host IP: "
						+ hostLocRecords.toString());

				// SMR is false, since we don't want to subscribe for SMRs
				// (that's for SB Map-Registers)
				lispNeutronService.getMappingService().handleMapRegister(
						mapRegister, false);
			}
		}
	}

	@Override
	public int canUpdatePort(NeutronPort delta, NeutronPort original) {
		// TODO Change of Fixed IPs are not allowed as we are storing ports by
		// fixed IPs for now

		if (original.getFixedIPs().equals(original.getFixedIPs())) {
			LOG.info("Neutron canUpdatePort : Port name: "
					+ original.getName()
					+ " Port Fixed IP: "
					+ (original.getFixedIPs() != null ? original.getFixedIPs()
							.get(0) : "No Fixed IP assigned")
					+ "New Port Fixed IP: "
					+ (delta.getFixedIPs() != null ? delta.getFixedIPs().get(0)
							: "No Fixed IP assigned"));
			LOG.debug("Neutron canUpdatePort : original" + original.toString()
					+ " delta : " + delta.toString());

			return HttpURLConnection.HTTP_OK;
		}
		return HttpURLConnection.HTTP_NOT_IMPLEMENTED;
	}

	@Override
	public void neutronPortUpdated(NeutronPort port) {
		// TODO Port IP and port's host ip is stored by Lisp Neutron Service. If
		// there is change to these fields, the update needs to be processed.

		LOG.info("Neutron Port updated: Port name: "
				+ port.getName()
				+ " Port Fixed IP: "
				+ (port.getFixedIPs() != null ? port.getFixedIPs().get(0)
						: "No Fixed IP assigned"));
		LOG.debug("Neutron Port Updated : " + port.toString());

		// Check if port.hostID is in map-server, if it is, get host eidtoloc
		// record?
		if (port.getBindinghostID() == null) {
			LOG.error("Updating Neutron port to lisp service mapping service failed. Port does not have Host_ID. Port : "
					+ port.toString());
			return;
		}
		// LispAFIAddress hostAddress =
		// LispAFIConvertor.asDistinguishedNameAddress(port.getBindinghostID());
		LispAFIAddress hostAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedNameBuilder()
				.setDistinguishedName(port.getBindinghostID()).build();
		MapRequest request = MapServerMapResolverUtil.getMapRequest(
				LispAFIConvertor.toContainer(hostAddress), (short) 0);

		MapReply reply = lispNeutronService.getMappingService()
				.handleMapRequest(request, false);

		if (reply == null) {
			LOG.error("Adding new Neutron port to lisp service mapping service failed. Host_ID mapping does not exist. Port : "
					+ port.toString());
			return;
		}
		// TODO for now only selecting the first EidToLocatorRecord from the
		// Host_ID mapping
		List<LocatorRecord> hostLocRecords = reply.getEidToLocatorRecord()
				.get(0).getLocatorRecord();

		List<Neutron_IPs> fixedIPs = port.getFixedIPs();
		if (fixedIPs != null && fixedIPs.size() > 0) {
			EidToLocatorRecordBuilder etlrBuilder;
			MapRegister mapRegister;
			LispIpv4Address eidAddress;
			for (Neutron_IPs ip : fixedIPs) {

				// TODO Add check/support for IPv6.
				// Get subnet for this port, based on v4 or v6 decide address
				// iana code.

				eidAddress = LispAFIConvertor.asIPAfiAddress(ip.getIpAddress());

				// Prepare Map Register
				etlrBuilder = new EidToLocatorRecordBuilder();
				etlrBuilder.setLispAddressContainer(LispAFIConvertor
						.toContainer(eidAddress));
				etlrBuilder.setMaskLength((short) 32);
				etlrBuilder.setLocatorRecord(hostLocRecords);
				mapRegister = MapServerMapResolverUtil
						.getMapRegister(etlrBuilder.build());
				LOG.info("Neutron Port mapping added to lisp: "
						+ " Port Fixed IP: " + ip + "Port host IP: "
						+ hostLocRecords.toString());

				// SMR is false, since we don't want to subscribe for SMRs
				// (that's for SB Map-Registers)
				lispNeutronService.getMappingService().handleMapRegister(
						mapRegister, false);
			}
		}

	}

	@Override
	public int canDeletePort(NeutronPort port) {
		// TODO Check if Port IPs are stored by Lisp Neutron Service. if not
		// return error code.

		LOG.info("Neutron canDeletePort : Port name: "
				+ port.getName()
				+ " Port Fixed IP: "
				+ (port.getFixedIPs() != null ? port.getFixedIPs().get(0)
						: "No Fixed IP assigned"));
		LOG.debug("Neutron canDeltePort: " + port.toString());

		return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronPortDeleted(NeutronPort port) {
		// TODO if port ips existed in MapServer, delete them. Else, log error.

		LOG.info("Neutron Port Deleted: Port name: "
				+ port.getName()
				+ " Port Fixed IP: "
				+ (port.getFixedIPs() != null ? port.getFixedIPs().get(0)
						: "No Fixed IP assigned"));
		LOG.debug("Neutron Port Deleted : " + port.toString());

		List<Neutron_IPs> fixedIPs = port.getFixedIPs();
		if (fixedIPs != null && fixedIPs.size() > 0) {
			LispIpv4Address eidAddress;
			for (Neutron_IPs ip : fixedIPs) {

				// TODO Add check/support for IPv6.
				// Get subnet for this port, based on v4 or v6 decide address
				// iana code.

				eidAddress = LispAFIConvertor.asIPAfiAddress(ip.getIpAddress());
				lispNeutronService.getMappingService().removeMapping(
						LispAFIConvertor.toContainer(eidAddress), (short) 32);

				LOG.info("Neutron Port mapping deleted from lisp: "
						+ " Port Fixed IP: " + ip + "Port host IP: ");

			}
		}

	}
}
