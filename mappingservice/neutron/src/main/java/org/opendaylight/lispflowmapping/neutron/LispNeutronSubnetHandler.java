/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;

import java.net.HttpURLConnection;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.Subnets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Lisp Service implementation of creation and deletion of a Subnet results
 * in defining the subnet as an EID prefix in the LISP Mapping System with
 * subnet's network UUID as the key to use for registering mappings for the subnet.
 *
 * @author Vina Ermagan
 *
 */
public class LispNeutronSubnetHandler extends LispNeutronService implements
        ClusteredDataTreeChangeListener<Subnet> {
    private static final Integer SIX = Integer.valueOf(6);

    // The implementation for each of these services is resolved by the OSGi
    // Service Manager
    private volatile ILispNeutronService lispNeutronService;
    private static final InstanceIdentifier<Subnet> iid = InstanceIdentifier.create(Subnets.class).child(Subnet.class);

    public LispNeutronSubnetHandler() {
        broker.registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, iid),
                this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Subnet>> changes) {
        for (DataTreeModification<Subnet> change : changes) {
            DataObjectModification<Subnet> mod = change.getRootNode();
            final Subnet subnet = mod.getDataAfter();

            if (mod.getModificationType() == ModificationType.WRITE) {
                neutronSubnetCreated(subnet);
            } else if (mod.getModificationType() == ModificationType.DELETE) {
                neutronSubnetDeleted(subnet);
            } else {
                neutronSubnetUpdated(subnet);
            }
        }
    }

    /**
     * Method adds the newly created subnet as an EID prefix to the
     * MappingService. The subnet's network UUID is used as the key for this EID
     * prefix.
     *
     */
    private void neutronSubnetCreated(final Subnet subnet) {
        // TODO update for multi-tenancy
        LOG.info("Neutron Subnet Created request : Subnet name: "
                + subnet.getName() + " Subnet Cidr: " + subnet.getCidr());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

        // Determine the IANA code for the subnet IP version
        // Default is set to IPv4 for neutron subnets
        final Eid eid = LispAddressUtil.asIpv4PrefixEid(String.valueOf(subnet.getCidr().getValue()));

        try {
            lispNeutronService.getMappingDbService()
                    .addKey(LispUtil.buildAddKeyInput(eid, subnet.getUuid().getValue()));

            LOG.debug("Neutron Subnet Added to MapServer : Subnet name: "
                    + subnet.getName() + " EID Prefix: "
                    + subnet.getCidr() + " Key: "
                    + subnet.getUuid());
        } catch (Exception e) {
            LOG.error("Adding new subnet to lisp service mapping service failed. Subnet : "
                    + subnet.toString() + "Error: " + ExceptionUtils.getStackTrace(e));
        }
        LOG.info("Neutron Subnet Created request : Subnet name: "
                + subnet.getName() + " Subnet Cidr: " + subnet.getCidr());
    }

    private void neutronSubnetUpdated(final Subnet subnet) {
        // Nothing to do here.
    }

    /**
     * Method removes the EID prefix and key associated with the deleted subnet
     * from Lisp mapping service.
     */
    private void neutronSubnetDeleted(final Subnet subnet) {
        LOG.info("Neutron Subnet Deleted Request : Subnet name: "
                + subnet.getName() + " Subnet Cidr: " + subnet.getCidr()
                + "Key: " + subnet.getUuid());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

        // Determine the IANA code for the subnet IP version
        // Default is set to IPv4 for neutron subnets
        final Eid eid = LispAddressUtil.asIpv4PrefixEid(String.valueOf(subnet.getCidr().getValue()));
        try {
            lispNeutronService.getMappingDbService().removeKey(LispUtil.buildRemoveKeyInput(eid));

            LOG.debug("Neutron Subnet Deleted from MapServer : Subnet name: "
                    + subnet.getName() + " Eid Prefix: "
                    + subnet.getCidr() + " Key: "
                    + subnet.getUuid());
        } catch (Exception e) {
            LOG.error("Deleting subnet's EID prefix from mapping service failed + Subnet: "
                    + subnet.toString() + "Error: " + ExceptionUtils.getStackTrace(e));
        }
    }

    public int canCreateSubnet(Subnet subnet) {
        LOG.info("Neutron canCreateSubnet : Subnet name: " + subnet.getName()
                + " Subnet Cidr: " + subnet.getCidr());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());
        if (SIX.equals(subnet.getIpVersion())) {
            return HttpURLConnection.HTTP_PARTIAL;
        }
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Method to check whether new Subnet can be created by LISP implementation
     * of Neutron service API. Since we store the Cidr part of the subnet as the
     * main key to the Lisp mapping service, we do not support updates to
     * subnets that change it's cidr.
     */
    public int canUpdateSubnet(Subnet delta, Subnet original) {
        if (delta == null || original == null) {
            LOG.error("Neutron canUpdateSubnet rejected: subnet objects were null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        LOG.info("Neutron canUpdateSubnet : Subnet name: " + original.getName()
                + " Subnet Cidr: " + original.getCidr());
        LOG.debug("Lisp Neutron Subnet update: original : "
                + original.toString() + " delta : " + delta.toString());

        // We do not accept a Subnet update that changes the cidr. If cider or
        // network UUID is changed, return error.
        if (!(original.getCidr().equals(delta.getCidr()))) {
            LOG.error("Neutron canUpdateSubnet rejected: Subnet name: "
                    + original.getName() + " Subnet Cidr: "
                    + original.getCidr());
            return HttpURLConnection.HTTP_CONFLICT;
        }
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Method to check if subnet can be deleted. Returns error only if subnet
     * does not exist in the lisp mapping service.
     */
    public int canDeleteSubnet(Subnet subnet) {
        LOG.info("Neutron canDeleteSubnet : Subnet name: " + subnet.getName()
                + " Subnet Cidr: " + subnet.getCidr() + "Key: "
                + subnet.getUuid());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());


        return HttpURLConnection.HTTP_OK;
    }
}
