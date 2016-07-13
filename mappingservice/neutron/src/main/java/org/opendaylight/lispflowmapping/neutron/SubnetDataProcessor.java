/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lisp Service implementation of creation and deletion of a Subnet results
 * in defining the subnet as an EID prefix in the LISP Mapping System with
 * subnet's network UUID as the key to use for registering mappings for the subnet.
 *
 * @author Vina Ermagan
 *
 */
public class SubnetDataProcessor implements DataProcessor<Subnet> {
    private static final Logger LOG = LoggerFactory.getLogger(SubnetDataProcessor.class);
//    private static final Integer SIX = Integer.valueOf(6);

    // The implementation for each of these services is resolved by the OSGi
    // Service Manager
    private volatile ILispNeutronService lispNeutronService;

    public SubnetDataProcessor(ILispNeutronService lispNeutronService) {
        this.lispNeutronService = lispNeutronService;
    }

    /**
     * Method adds the newly created subnet as an EID prefix to the
     * MappingService. The subnet's network UUID is used as the key for this EID
     * prefix.
     */
    @Override
    public void create(Subnet subnet) {
        // TODO update for multi-tenancy
        LOG.info("Neutron Subnet Created request : Subnet name: "
                + subnet.getName() + " Subnet Cidr: " + subnet.getCidr());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

        // Determine the IANA code for the subnet IP version
        // Default is set to IPv4 for neutron subnets
        final Eid eid = LispAddressUtil.asIpv4PrefixEid(String.valueOf(subnet.getCidr().getValue()));

        try {
            final OdlMappingserviceService lfmdb = lispNeutronService.getMappingDbService();
            if (lfmdb == null) {
                LOG.debug("lfmdb is null!!!");
                return;
            }
            final AddKeyInput addKeyInput = LispUtil.buildAddKeyInput(eid, subnet.getUuid().getValue());
            final Future<RpcResult<Void>> result = lfmdb.addKey(addKeyInput);
            final Boolean isSuccessful = result.get().isSuccessful();

            if (isSuccessful) {
                LOG.debug("Neutron Subnet Added to MapServer : Subnet name: "
                        + subnet.getName() + " EID Prefix: "
                        + subnet.getCidr() + " Key: "
                        + subnet.getUuid());
        }
        LOG.info("Neutron Subnet Created request : Subnet name: "
                + subnet.getName() + " Subnet Cidr: " + subnet.getCidr());
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Adding new subnet to lisp service mapping service failed. Subnet : "
                    + subnet.toString() + "Error: " + ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void update(Subnet subnet) {
        // Nothing to do here.
    }

    /**
     * Method removes the EID prefix and key associated with the deleted subnet
     * from Lisp mapping service.
     */
    @Override
    public void delete(Subnet subnet) {
        LOG.info("Neutron Subnet Deleted Request : Subnet name: "
                + subnet.getName() + " Subnet Cidr: " + subnet.getCidr()
                + "Key: " + subnet.getUuid());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

        // Determine the IANA code for the subnet IP version
        // Default is set to IPv4 for neutron subnets
        final Eid eid = LispAddressUtil.asIpv4PrefixEid(String.valueOf(subnet.getCidr().getValue()));

        try {
            final OdlMappingserviceService lfmdb = lispNeutronService.getMappingDbService();
            if (lfmdb == null) {
                LOG.debug("lfmdb is null!!!");
                return;
            }
            final RemoveKeyInput removeKeyInput = LispUtil.buildRemoveKeyInput(eid);
            final Future<RpcResult<Void>> result = lfmdb.removeKey(removeKeyInput);
            final Boolean isSuccessful = result.get().isSuccessful();

            if (isSuccessful) {
                LOG.debug("Neutron Subnet Deleted from MapServer : Subnet name: "
                        + subnet.getName() + " Eid Prefix: "
                        + subnet.getCidr() + " Key: "
                        + subnet.getUuid());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Deleting subnet's EID prefix from mapping service failed + Subnet: "
                    + subnet.toString() + "Error: " + ExceptionUtils.getStackTrace(e));
        }
    }
}
