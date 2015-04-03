/*
 * Copyright (c) 2014 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;


import java.net.HttpURLConnection;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.opendaylight.neutron.spi.INeutronSubnetAware;
import org.opendaylight.neutron.spi.NeutronSubnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;

/**
 * Lisp Service implementation of NeutronSubnetAware API
 * Creation of a new Subnet results in defining the subnet as an EID prefix
 * in the LISP Mapping System with subnet's network UUID as the key to use
 * for registering mappings for the subnet.
 *
 */
public class LispNeutronSubnetHandler extends LispNeutronService implements INeutronSubnetAware {
    private static final Integer SIX = Integer.valueOf(6);

    // The implementation for each of these services is resolved by the OSGi Service Manager
    private volatile ILispNeutronService lispNeutronService;

	@Override
	public int canCreateSubnet(NeutronSubnet subnet) {
		LOG.info("Neutron canCreateSubnet : Subnet name: " + subnet.getName() + " Subnet Cidr: " +
        		subnet.getCidr());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

        return HttpURLConnection.HTTP_OK;
	}

	/**
	 * Method adds the newly created subnet as an EID prefix to the
	 * MappingService. The subnet's network UUID is used as the key
	 * for this EID prefix.
	 *
	 */
	@Override
	public void neutronSubnetCreated(NeutronSubnet subnet) {
		//TODO update for multi-tenancy

		LOG.info("Neutron Subnet Created request : Subnet name: " + subnet.getName() + " Subnet Cidr: " +
        		subnet.getCidr());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

	    int masklen = Integer.parseInt(subnet.getCidr().split("/")[1]);
	    SubnetUtils util = new SubnetUtils(subnet.getCidr());
        SubnetInfo info = util.getInfo();

        // Determine the IANA code for the subnet IP version
        // Default is set to IPv4 for neutron subnets

        short ianaCode = 1;
        if ( SIX.equals(subnet.getIpVersion()) )
        	ianaCode = 2;

        LispAFIAddress lispAddress = new Ipv4AddressBuilder().setIpv4Address(new Ipv4Address(info.getNetworkAddress())).setAfi(ianaCode).build();
        LispAddressContainer addressContainer =	new LispAddressContainerBuilder().setAddress((Address) lispAddress).build();

        try{
        	lispNeutronService.getMappingService().addAuthenticationKey(addressContainer, masklen, subnet.getNetworkUUID());

        	LOG.debug("Neutron Subnet Added to MapServer : Subnet name: " + subnet.getName() + " EID Prefix: " +
            		addressContainer.toString() + " Key: " + subnet.getNetworkUUID());
        }
        catch (Exception e){
        	LOG.error("Adding new subnet to lisp service mapping service failed. Subnet : " + subnet.toString());
        }

	}

	/**
	 * Method to check whether new Subnet can be created by LISP
	 * implementation of Neutron service API.
	 * Since we store the Cidr part of the subnet as the main key to the Lisp
	 * mapping service, we do not support updates to subnets that change it's cidr.
	 */
	@Override
	public int canUpdateSubnet(NeutronSubnet delta, NeutronSubnet original) {
		if (delta == null || original == null){
	        LOG.error("Neutron canUpdateSubnet rejected: subnet objects were null");
	        return HttpURLConnection.HTTP_BAD_REQUEST;
		}
		LOG.info("Neutron canUpdateSubnet : Subnet name: " + original.getName() + " Subnet Cidr: " +
        		original.getCidr());
        LOG.debug("Lisp Neutron Subnet update: original : " + original.toString() + " delta : " + delta.toString());


		// We do not accept a Subnet update that changes the cidr. If cider or network UUID is changed, return error.
		if ( !(original.getCidr().equals(delta.getCidr())) ){
			LOG.error("Neutron canUpdateSubnet rejected: Subnet name: " + original.getName() + " Subnet Cidr: " +
	        		original.getCidr());
			return HttpURLConnection.HTTP_CONFLICT;
		}
        return HttpURLConnection.HTTP_OK;
	}

	@Override
	public void neutronSubnetUpdated(NeutronSubnet subnet) {
		// Nothing to do here.
	}

	/**
	 * Method to check if subnet can be deleted.
	 * Returns error only if subnet does not exist
	 * in the lisp mapping service.
	 */
	@Override
	public int canDeleteSubnet(NeutronSubnet subnet) {
        LOG.info("Neutron canDeleteSubnet : Subnet name: " + subnet.getName() + " Subnet Cidr: " +
        		subnet.getCidr() + "Key: " + subnet.getNetworkUUID());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

        int result;
	    int masklen = Integer.parseInt(subnet.getCidr().split("/")[1]);
	    SubnetUtils util = new SubnetUtils(subnet.getCidr());
        SubnetInfo info = util.getInfo();

        // Determine the IANA code for the subnet IP version
        // Default is set to IPv4 for neutron subnets
        short ianaCode = 1;
        if ( SIX.equals(subnet.getIpVersion()) )
        	ianaCode = 2;

        LispAFIAddress lispAddress = new Ipv4AddressBuilder().setIpv4Address(new Ipv4Address(info.getNetworkAddress())).setAfi(ianaCode).build();
        LispAddressContainer addressContainer = new LispAddressContainerBuilder().setAddress((Address) lispAddress).build();

        // if subnet does not exist in MapServer, return error
        try{
	        if (lispNeutronService.getMappingService().getAuthenticationKey(addressContainer, masklen) == null){

	        	LOG.error("Neutron canDeleteSubnet rejected : Subnet does not exist: Subnet name: " +
	        			subnet.getName() +
	        			" Eid Prefix: " + addressContainer.toString() +
	        			" Key: " + subnet.getNetworkUUID());
	        	return HttpURLConnection.HTTP_BAD_REQUEST;
	        }
	        result =  HttpURLConnection.HTTP_OK;
        }
        catch (Exception e){
        	LOG.error("canDeleteSubnet request rejected. Subnet : " + subnet.toString());
	        result =  HttpURLConnection.HTTP_BAD_REQUEST;
        }
        return result;
	}

	/**
	 * Method removes the EID prefix and key associated
	 * with the deleted subnet from Lisp mapping service.
	 */
	@Override
	public void neutronSubnetDeleted(NeutronSubnet subnet) {
  		LOG.info("Neutron Subnet Deleted Request : Subnet name: " + subnet.getName() + " Subnet Cidr: " +
        		subnet.getCidr() + "Key: " + subnet.getNetworkUUID());
        LOG.debug("Lisp Neutron Subnet: " + subnet.toString());

        int masklen = Integer.parseInt(subnet.getCidr().split("/")[1]);
	    SubnetUtils util = new SubnetUtils(subnet.getCidr());
        SubnetInfo info = util.getInfo();

        // Determine the IANA code for the subnet IP version
        // Default is set to IPv4 for neutron subnets

        short ianaCode = 1;
        if ( SIX.equals(subnet.getIpVersion()) )
        	ianaCode = 2;

        LispAFIAddress lispAddress = new Ipv4AddressBuilder().setIpv4Address(new Ipv4Address(info.getNetworkAddress())).setAfi(ianaCode).build();
        LispAddressContainer addressContainer = new LispAddressContainerBuilder().setAddress((Address) lispAddress).build();

        try{
	        // if subnet does not exist in MapServer, return error
	        if (lispNeutronService.getMappingService().getAuthenticationKey(addressContainer,masklen) == null){
	            LOG.error("Neutron Delete Subnet Failed: Subnet does not exist: Subnet name: " + subnet.getName() +
	            		" Eid Prefix: " + addressContainer.toString() +
	            		"Key: " + subnet.getNetworkUUID());
	            return;
	        }
	        lispNeutronService.getMappingService().removeAuthenticationKey(addressContainer, masklen);

	        LOG.debug("Neutron Subnet Deleted from MapServer : Subnet name: " + subnet.getName() +
	        		" Eid Prefix: " + addressContainer.toString() +
	        		" Key: " + subnet.getNetworkUUID());
        }
        catch (Exception e){
        	LOG.error("Deleting subnet's EID prefix from mapping service failed + Subnet: " + subnet.toString());
        }
	}

}
