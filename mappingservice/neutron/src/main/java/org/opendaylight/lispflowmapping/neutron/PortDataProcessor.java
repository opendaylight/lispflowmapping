/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class PortDataProcessor implements DataProcessor<Port> {
    private static final Logger LOG = LoggerFactory.getLogger(PortDataProcessor.class);

    // The implementation for each of these services is resolved by the OSGi
    // Service Manager
    private volatile ILispNeutronService lispNeutronService;

    public PortDataProcessor(ILispNeutronService lispNeutronService) {
        this.lispNeutronService = lispNeutronService;
    }

    @Override
    public void create(Port port) {
        // TODO Consider adding Port MAC -> Port fixed IP in MS
        // TODO Add Port fixed ip -> host ip , if Port.host_id mapping to
        // host_ip exists in MS

        LOG.debug("Neutron Port Created : " + port.toString());

        // Check if port.hostID is in map-server, if it is, get host eidtoloc
        // record?
        if (port.getDeviceId() == null) {
            LOG.error("Adding new Neutron port to lisp mapping service failed. Port does not have Host_ID. Port: {}",
                    port.toString());
            return;
        }
        Eid hostAddress = LispAddressUtil.asDistinguishedNameEid(port.getDeviceId());

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

        List<FixedIps> fixedIPs = port.getFixedIps();
        if (fixedIPs != null && fixedIPs.size() > 0) {
            Eid eidAddress;
            for (FixedIps ip : fixedIPs) {

                // TODO Add check/support for IPv6.
                // Get subnet for this port, based on v4 or v6 decide address
                // iana code.

                eidAddress = LispAddressUtil.asIpv4PrefixEid(ip.getIpAddress() + "/32");
                lispNeutronService.getMappingDbService().addMapping(LispUtil.buildAddMappingInput(eidAddress,
                        hostLocRecords));
            }
        }

        LOG.info("Neutron Port Created: Port name: "
                + port.getName()
                + " Port Fixed IP: "
                + (port.getFixedIps() != null ? port.getFixedIps().get(0)
                : "No Fixed IP assigned"));
    }

    @Override
    public void update(Port port) {
        // TODO Port IP and port's host ip is stored by Lisp Neutron Service. If
        // there is change to these fields, the update needs to be processed.

        LOG.info("Neutron Port updated: Port name: "
                + port.getName()
                + " Port Fixed IP: "
                + (port.getFixedIps() != null ? port.getFixedIps().get(0)
                : "No Fixed IP assigned"));
        LOG.debug("Neutron Port Updated : " + port.toString());
    }

    @Override
    public void delete(Port port) {
        // TODO if port ips existed in MapServer, delete them. Else, log error.

        LOG.info("Neutron Port Deleted: Port name: "
                + port.getName()
                + " Port Fixed IP: "
                + (port.getFixedIps() != null ? port.getFixedIps().get(0)
                : "No Fixed IP assigned"));
        LOG.debug("Neutron Port Deleted : " + port.toString());

        List<FixedIps> fixedIPs = port.getFixedIps();
        if (fixedIPs != null && fixedIPs.size() > 0) {
            Eid eidAddress;

            for (FixedIps ip : fixedIPs) {

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

//    public int canCreatePort(Port port) {
//        LOG.info("Neutron canCreatePort : Port name: " + port.getName());
//
//        return HttpURLConnection.HTTP_OK;
//    }
//
//    public int canUpdatePort(Port delta, Port original) {
//        // TODO Change of Fixed IPs are not allowed as we are storing ports by
//        // fixed IPs for now
//
//        if (original.getFixedIps().equals(original.getFixedIps())) {
//            LOG.info("Neutron canUpdatePort : Port name: "
//                    + original.getName()
//                    + " Port Fixed IP: "
//                    + (original.getFixedIps() != null ? original.getFixedIps()
//                            .get(0) : "No Fixed IP assigned")
//                    + "New Port Fixed IP: "
//                    + (delta.getFixedIps() != null ? delta.getFixedIps().get(0)
//                            : "No Fixed IP assigned"));
//            LOG.debug("Neutron canUpdatePort : original" + original.toString()
//                    + " delta : " + delta.toString());
//
//            return HttpURLConnection.HTTP_OK;
//        }
//        return HttpURLConnection.HTTP_NOT_IMPLEMENTED;
//    }
//
//    public int canDeletePort(Port port) {
//        // TODO Check if Port IPs are stored by Lisp Neutron Service. if not
//        // return error code.
//
//        LOG.info("Neutron canDeletePort : Port name: "
//                + port.getName()
//                + " Port Fixed IP: "
//                + (port.getFixedIps() != null ? port.getFixedIps().get(0)
//                        : "No Fixed IP assigned"));
//        LOG.debug("Neutron canDeltePort: " + port.toString());
//
//        return HttpURLConnection.HTTP_OK;
//    }
}
