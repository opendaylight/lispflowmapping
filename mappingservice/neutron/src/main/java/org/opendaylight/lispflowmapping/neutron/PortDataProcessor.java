/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import java.util.Map;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.HostInformationManager;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.PortData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.binding.rev150712.PortBindingExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
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
 */
public class PortDataProcessor implements DataProcessor<Port> {
    private static final Logger LOG = LoggerFactory.getLogger(PortDataProcessor.class);

    // The implementation for each of these services is resolved by the OSGi
    // Service Manager
    private volatile ILispNeutronService lispNeutronService;

    private final HostInformationManager hostInformationManager = HostInformationManager.getInstance();

    public PortDataProcessor(ILispNeutronService lispNeutronService) {
        this.lispNeutronService = lispNeutronService;
    }

    @Override
    public void create(Port port) {
        // TODO Consider adding Port MAC -> Port fixed IP in MS
        // host_ip exists in MS

        LOG.debug("Neutron Port Created : " + port.toString());

        final String hostId = port.augmentation(PortBindingExtension.class).getHostId();
        if (hostId == null) {
            LOG.error("Adding new Neutron port to lisp mapping service failed. Port does not have a HostID. Port: {}",
                    port.toString());
            return;
        }

        Map<FixedIpsKey, FixedIps> fixedIPs = port.nonnullFixedIps();
        if (fixedIPs != null && fixedIPs.size() > 0) {
            Eid eidAddress;
            for (FixedIps ip : fixedIPs.values()) {

                // TODO Add check/support for IPv6.
                // Get subnet for this port, based on v4 or v6 decide address
                // iana code.
                eidAddress = getEid(port, ip);
                PortData portData = new PortData(port.getUuid().getValue(), eidAddress);
                hostInformationManager.addHostRelatedInfo(hostId, portData);
            }
        }

        LOG.info("Neutron Port Created: Port name: "
                + port.getName()
                + " Port Fixed IP: "
                + (port.getFixedIps() != null ? port.getFixedIps().values().iterator().next()
                : "No Fixed IP assigned"));
    }

    private Eid getEid(Port port, FixedIps ip) {
        InstanceIdType vni = new InstanceIdType(hostInformationManager
                                                    .getInstanceId(port.getTenantId().getValue()));
        return LispAddressUtil.asIpv4PrefixEid(ip.getIpAddress().getIpv4Address(), vni);
    }

    @Override
    public void update(Port port) {

        final String hostId = port.augmentation(PortBindingExtension.class).getHostId();
        if (hostId == null) {
            LOG.error("Updating port to lisp mapping service failed. Port does not have a HostID. Port: {}",
                    port.toString());
            return;
        }

        Map<FixedIpsKey, FixedIps> fixedIPs = port.getFixedIps();
        if (fixedIPs != null && fixedIPs.size() > 0) {
            Eid eidAddress;
            for (FixedIps ip : fixedIPs.values()) {

                eidAddress = getEid(port, ip);

                PortData portData = new PortData(port.getUuid().getValue(), eidAddress);
                hostInformationManager.addHostRelatedInfo(hostId, portData);
            }
        }

        LOG.info("Neutron Port updated: Port name: "
                + port.getName()
                + " Port Fixed IP: "
                + (port.getFixedIps() != null ? port.getFixedIps().values().iterator().next()
                : "No Fixed IP assigned"));
        LOG.debug("Neutron Port Updated : " + port.toString());
    }

    @Override
    public void delete(Port port) {
        // TODO if port ips existed in MapServer, delete them. Else, log error.

        LOG.info("Neutron Port Deleted: Port name: "
                + port.getName()
                + " Port Fixed IP: "
                + (port.getFixedIps() != null ? port.getFixedIps().values().iterator().next()
                : "No Fixed IP assigned"));
        LOG.debug("Neutron Port Deleted : " + port.toString());

        Map<FixedIpsKey, FixedIps> fixedIPs = port.getFixedIps();
        if (fixedIPs != null && fixedIPs.size() > 0) {
            Eid eidAddress;

            for (FixedIps ip : fixedIPs.values()) {

                // TODO Add check/support for IPv6.
                // Get subnet for this port, based on v4 or v6 decide address
                // iana code.

                eidAddress = LispAddressUtil.asIpv4PrefixEid(ip.getIpAddress().getIpv4Address().getValue() + "/32");
                lispNeutronService.getMappingDbService().removeMapping(LispUtil.buildRemoveMappingInput(eidAddress));

                LOG.info("Neutron Port mapping deleted from lisp: "
                        + " Port Fixed IP: " + ip + "Port host IP: ");

            }
        }
    }
}
