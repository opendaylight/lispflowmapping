/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.dataprocessors;

import java.util.List;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.ILispNeutronService;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.OpenstackNodeInformationManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.binding.rev150712.PortBindingExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
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
 *
 */

public class PortDataProcessor implements DataProcessor<Port> {
    private static final Logger LOG = LoggerFactory.getLogger(PortDataProcessor.class);

    // The implementation for each of these services is resolved by the OSGi
    // Service Manager
    private volatile ILispNeutronService lispNeutronService;

    private final OpenstackNodeInformationManager
            openstackNodeInformationManager = OpenstackNodeInformationManager.getInstance();

    public PortDataProcessor(ILispNeutronService lispNeutronService) {
        this.lispNeutronService = lispNeutronService;
    }

    @Override
    public void create(Port objectAfter) {
        LOG.debug("Neutron Port Created : {}", objectAfter.toString());
        if (processCreatedPort(objectAfter)) {
            LOG.info("Neutron Port Created: Port uuid: {} Port Fixed Ip: {}", objectAfter.getName(),
                    (objectAfter.getFixedIps() != null ? objectAfter.getFixedIps().get(0) : "No Fixed IP assigned"));
        } else {
            LOG.warn("Neutron Port Create process failed for {}", objectAfter.toString());
        }
    }

    @Override
    public void update(Port objectBefore, Port objectAfter) {

        LOG.debug("Neutron Port Updated: Old port:{} New port:{}", objectBefore, objectAfter);

        if (processDeletedPort(objectBefore)) {
            LOG.debug("Old port Delete: {}", objectBefore);
        } else {
            LOG.warn("Neutron old port delete failed for {}", objectBefore);
        }

        if (processCreatedPort(objectAfter)) {
            LOG.debug("New port Created: {}", objectAfter);
        } else {
            LOG.warn("Neutron new port delete failed for {}", objectAfter);
        }

        LOG.info("Neutron Port updated: Port uuid: {} Port Fixed IP: {}", objectAfter.getUuid(),
                (objectAfter.getFixedIps() != null ? objectAfter.getFixedIps().get(0) : "No Fixed IP assigned"));
    }

    @Override
    public void delete(Port objectBefore) {
        LOG.debug("Neutron Port Deleted : {}", objectBefore.toString());

        if (processDeletedPort(objectBefore)) {
            LOG.info("Neutron Port Deleted: Port uuid: {} Port Fixed IP: {}", objectBefore.getUuid(),
                    (objectBefore.getFixedIps() != null ? objectBefore.getFixedIps().get(0) : "No Fixed IP assigned"));
        } else {
            LOG.debug("Neutron Port Delete process failed for {}", objectBefore.toString());
        }
    }

    private boolean processCreatedPort(Port port) {
        final String hostId = port.getAugmentation(PortBindingExtension.class).getHostId();
        if (hostId == null) {
            LOG.error("Port process failed. Port does not have a HostID. Port: {}", port.toString());
            return false;
        }

        List<FixedIps> fixedIPs = port.getFixedIps();
        if (fixedIPs != null && !fixedIPs.isEmpty()) {
            for (FixedIps ip : fixedIPs) {
                // TODO Add check/support for IPv6.
                openstackNodeInformationManager.addEidInHost(hostId, getEid(port, ip));
            }
        }
        return true;
    }

    private boolean processDeletedPort(Port port) {
        final String hostId = port.getAugmentation(PortBindingExtension.class).getHostId();
        if (hostId == null) {
            LOG.error("Deleting port from lisp mapping service failed. Port does not have a HostID. Port: {}",
                    port.toString());
            return false;
        }

        List<FixedIps> fixedIPs = port.getFixedIps();

        if (fixedIPs != null && !fixedIPs.isEmpty()) {
            for (FixedIps ip : fixedIPs) {
                // TODO Add check/support for IPv6.
                openstackNodeInformationManager.attemptToDeleteExistingMappingRecord(hostId, getEid(port, ip));
            }
        }
        return true;
    }

    private Eid getEid(Port port, FixedIps ip) {
        InstanceIdType vni = new InstanceIdType(openstackNodeInformationManager
                .getInstanceId(port.getTenantId().getValue()));
        return LispAddressUtil.asIpv4PrefixEid(ip.getIpAddress().getIpv4Address(), vni);
    }
}
