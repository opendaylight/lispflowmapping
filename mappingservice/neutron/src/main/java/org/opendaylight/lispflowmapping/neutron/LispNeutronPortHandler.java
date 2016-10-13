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
import java.util.concurrent.Future;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.neutron.spi.INeutronPortAware;
import org.opendaylight.neutron.spi.NeutronPort;
import org.opendaylight.neutron.spi.Neutron_IPs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Lisp Service implementation of NeutronPortAware API Creation of a new port
 * results adding the mapping for the port's IP addresses to the port's host_ip
 * in the mapping service. Currently the NetronPort object does not carry the
 * required information to achieve the port's host_ip. Once such extension is
 * available this class shall be updated.
 *
 * @author Vina Ermagan
 *
 */

public class LispNeutronPortHandler extends LispNeutronService implements
        INeutronPortAware {

    // The implementation for each of these services is resolved by the OSGi
    // Service Manager
    private volatile ILispNeutronService lispNeutronService;

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

        LOG.debug("Neutron Port Created : " + port.toString());

        // Check if port.hostID is in map-server, if it is, get host eidtoloc
        // record?
        if (port.getBindinghostID() == null) {
            LOG.error("Adding new Neutron port to lisp service mapping service failed. Port does not have Host_ID. Port : "
                    + port.toString());
            return;
        }
        Eid hostAddress = LispAddressUtil.asDistinguishedNameEid(port.getBindinghostID());

        MappingRecord eidRecord;
        List<LocatorRecord> hostLocRecords;
        GetMappingInput input = LispUtil.buildGetMappingInput(hostAddress);
        try {
            OdlMappingserviceService lfmdb = lispNeutronService.getMappingDbService();
            if (lfmdb == null) {
                LOG.debug("lfmdb is null!!!");
                return;
            }
            Future<RpcResult<GetMappingOutput>> result = lfmdb.getMapping(input);
            GetMappingOutput output = result.get().getResult();
            if (output == null) {
                LOG.debug("No mapping found to Host Id {}", port.getBindinghostID());
                return;
            }

            // TODO for now only selecting the first EidToLocatorRecord from the
            // Host_ID mapping

            eidRecord = output.getMappingRecord();
            hostLocRecords = eidRecord.getLocatorRecord();
            LOG.debug("hostLocRecords is : {}",hostLocRecords);


        } catch (Exception e) {
            LOG.warn("Failed to GET mapping for EID {}: , mappingInput: {} , Exception: {}", hostAddress, input,
                    ExceptionUtils.getStackTrace(e));
            return;
        }

        List<Neutron_IPs> fixedIPs = port.getFixedIPs();
        if (fixedIPs != null && fixedIPs.size() > 0) {
          Eid eidAddress;
            for (Neutron_IPs ip : fixedIPs) {

                // TODO Add check/support for IPv6.
                // Get subnet for this port, based on v4 or v6 decide address
                // iana code.

                eidAddress = LispAddressUtil.asIpv4PrefixEid(ip.getIpAddress() + "/32");
                lispNeutronService.getMappingDbService().addMapping(LispUtil.buildAddMappingInput(eidAddress, hostLocRecords));
            }
        }

        LOG.info("Neutron Port Created: Port name: "
                    + port.getName()
                    + " Port Fixed IP: "
                    + (port.getFixedIPs() != null ? port.getFixedIPs().get(0)
                            : "No Fixed IP assigned"));

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
            Eid eidAddress;

            for (Neutron_IPs ip : fixedIPs) {

                // TODO Add check/support for IPv6.
                // Get subnet for this port, based on v4 or v6 decide address
                // iana code.

                eidAddress = LispAddressUtil.asIpv4PrefixEid(ip.getIpAddress() + "/32");
                lispNeutronService.getMappingDbService().removeMapping(LispUtil.buildRemoveMappingInput(eidAddress));

                LOG.info("Neutron Port mapping deleted from lisp: "
                        + " Port Fixed IP: " + ip + "Port host IP: ");

            }
        }

    }

}
