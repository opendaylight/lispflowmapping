/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.neutron.intenthandler.exception.RlocNotFoundOnVppNode;
import org.opendaylight.lispflowmapping.neutron.util.LispNeutronUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.Interface2;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces.state._interface.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces.state._interface.ipv4.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev161214.Loopback;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by sheikahm on 1/12/17.
 */
public class VppNodeReader {
    private static final Logger LOG = LoggerFactory.getLogger(VppNodeReader.class);

    private final DataBroker dataBroker;
    private final MountPointService mountService;

    public VppNodeReader(DataBroker dataBroker, MountPointService mountService) {
        this.dataBroker = dataBroker;
        this.mountService = mountService;
    }

    public Ipv4Address rlocIpOfNode(KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifierToVppNode) {
        try {
            Optional<Ipv4Address> ipv4AddressOptional =
                    readFirstAvailableIpOfVppNode(instanceIdentifierToVppNode).get();
            if (!ipv4AddressOptional.isPresent()) {
                throw new RlocNotFoundOnVppNode(InfoUtil.node(instanceIdentifierToVppNode));
            }

            return ipv4AddressOptional.get();
        } catch (final InterruptedException | ExecutionException ex) {
            LOG.warn("Got exception while reading IP addresses from nodes {}",
                    InfoUtil.node(instanceIdentifierToVppNode));
            throw new RlocNotFoundOnVppNode(InfoUtil.node(instanceIdentifierToVppNode));
        }
    }

    private ListenableFuture<Optional<Ipv4Address>>
        readFirstAvailableIpOfVppNode(final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifierToVppNode) {

        final SettableFuture<Optional<Ipv4Address>> resultFuture = SettableFuture.create();

        final DataBroker vppDataBroker = LispNeutronUtil.resolveDataBrokerForMountPoint(instanceIdentifierToVppNode,
                mountService);
        if (vppDataBroker != null) {
            final Optional<InterfacesState> interfacesOnVppNodeOptional = VppNetconfTrasaction.read(vppDataBroker,
                    LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(InterfacesState.class));
            if (interfacesOnVppNodeOptional.isPresent()) {

                for (Interface intf : interfacesOnVppNodeOptional.get().getInterface()) {

                    if (intf.getType().equals(Loopback.class)) {
                        continue;
                    }

                    final Optional<Ipv4Address> ipv4AddressOptional = readIpAddressFromInterface(intf,
                            instanceIdentifierToVppNode);

                    if (ipv4AddressOptional.isPresent()) {
                        resultFuture.set(ipv4AddressOptional);
                        break;
                    }
                }

            }

            resultFuture.set(Optional.absent());
        } else {
            LOG.debug("Data broker for vpp {} is missing.", instanceIdentifierToVppNode);
        }
        return resultFuture;
    }

    private Optional<Ipv4Address> readIpAddressFromInterface(Interface intf,
                                                                   KeyedInstanceIdentifier iiToVpp) {
        Interface2 augIntf = intf.getAugmentation(Interface2.class);

        if (augIntf == null) {
            LOG.debug("Cannot get Interface2 augmentation for intf {}");
            return Optional.absent();
        }

        Ipv4 ipv4 = augIntf.getIpv4();

        if (ipv4 == null) {
            LOG.debug("Ipv4 address for interface {} on node {} is null!", augIntf, InfoUtil.node(iiToVpp));
            return Optional.absent();
        }

        final List<Address> addresses = ipv4.getAddress();
        if (addresses == null || addresses.isEmpty()) {
            LOG.debug("Ipv4 addresses list is empty for interface {} on node {}", augIntf, InfoUtil.node(iiToVpp));
            return Optional.absent();
        }

        final Ipv4AddressNoZone ip = addresses.iterator().next().getIp();
        if (ip == null) {
            LOG.debug("Ipv4AddressNoZone is null for node {}", InfoUtil.node(iiToVpp));
            return Optional.absent();
        }

        LOG.debug("Got ip address {} from interface {} on node {}", ip.getValue(), intf.getName(),
                InfoUtil.node(iiToVpp));
        return Optional.of(ip);
    }

}
