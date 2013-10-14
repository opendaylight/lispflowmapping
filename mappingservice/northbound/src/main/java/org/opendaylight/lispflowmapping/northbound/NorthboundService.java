/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.northbound;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.containermanager.IContainerManager;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ResourceNotFoundException;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.northbound.MapRegisterNB;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/")
public class NorthboundService implements INorthboundService, CommandProvider{
    protected static final Logger logger = LoggerFactory.getLogger(NorthboundService.class);
    private IFlowMapping mappingService;

    private String userName;

    //Based on code from controller project
    @Context
    public void setSecurityContext(SecurityContext context) {
        if (context != null && context.getUserPrincipal() != null) {
            userName = context.getUserPrincipal().getName();
        }
    }

    protected String getUserName() {
        return userName;
    }
    
    
    
    public IFlowMapping getMappingService(){
    	return this.mappingService;
    }
    
    void setFlowMappingService(IFlowMapping mappingService) {
        logger.debug("FlowMapping set in LispNorthbound");
        this.mappingService = mappingService;
    }

    void unsetFlowMappingService(IFlowMapping mappingService) {
        logger.debug("LispDAO was unset in LISP Northbound");
        this.mappingService = null;
    }
    
    public void init() {
        logger.debug("LISP Northbound Service is initialized!");
    }

    public void start() {
        logger.info("LISP Northbound Service is up!");

        // OSGI console
        registerWithOSGIConsole();
    }

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }

    public void stop() {
        logger.info("LISP Northbound Service is down!");
    }

    public void destroy() {
        logger.debug("LISP Northbound Service is destroyed!");
        mappingService = null;
    }
    
    public void _runRegister(final CommandInterpreter ci) {
        LispIpv4Address EID = new LispIpv4Address("10.0.0.1");
        LocatorRecord RLOC1 = new LocatorRecord();
        RLOC1.setLocator(new LispIpv4Address("10.0.0.2"));
        LocatorRecord RLOC2 = new LocatorRecord();
        RLOC2.setLocator(new LispIpv4Address("10.0.0.3"));
        EidToLocatorRecord etlr = new EidToLocatorRecord();
        etlr.setPrefix(EID);
        etlr.addLocator(RLOC1);
        etlr.addLocator(RLOC2);
        MapRegister mapRegister = new MapRegister();
        mapRegister.setKeyId((short)0).setNonce(0).setWantMapNotify(true);
        mapRegister.addEidToLocator(etlr);
        mappingService.handleMapRegister(mapRegister);
        return;
    }
    
    public void _runRequest(final CommandInterpreter ci) {
        LispIpv4Address EID = new LispIpv4Address("10.0.0.1");
        MapRequest mapRequest = new MapRequest();
        EidRecord EIDRecord = new EidRecord((byte) 0, EID);
        mapRequest.addEidRecord(EIDRecord);
        MapReply mapReply = mappingService.handleMapRequest(mapRequest);
        
        ci.println("EID:" + mapRequest.getEids().get(0).getPrefix());
        for (LocatorRecord record : mapReply.getEidToLocatorRecords().get(0).getLocators()) {
            ci.println("RLOC:" + record.getLocator());
        }
        
        logger.info("EID:" + mapReply.getEidToLocatorRecords().get(0).getPrefix()+
        		" TTL "+mapReply.getEidToLocatorRecords().get(0).getRecordTtl());

        return;
    }

    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---Northbound Service---\n");
        help.append("\t runRegister        - Run a map register example\n");
        help.append("\t runRequest      - run a map request example\n");
        return help.toString();
    } 
    
    //Based on code from controller project
    private void handleContainerDoesNotExist(String containerName) {
        IContainerManager containerManager = (IContainerManager) ServiceHelper.getGlobalInstance(
                IContainerManager.class, this);
        if (containerManager == null) {
            throw new ServiceUnavailableException("Container " + RestMessages.NOCONTAINER.toString());
        }

        List<String> containerNames = containerManager.getContainerNames();
        for (String cName : containerNames) {
            if (cName.trim().equalsIgnoreCase(containerName.trim())) {
                return;
            }
        }

        throw new ResourceNotFoundException(containerName + " " + RestMessages.NOCONTAINER.toString());
    }
    
    private void authorizationCheck(String containerName, Privilege privilege){
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), containerName, privilege, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation on container "
                    + containerName);   
    	}
    }

    private EidToLocatorRecord lookupEID(String containerName, int mask, LispAddress EID){
    	
    	INorthboundService nbService = 
    			(INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName,this);
    	
        MapRequest mapRequest = new MapRequest();
        EidRecord EIDRecord = new EidRecord((byte) mask, EID);
        mapRequest.addEidRecord(EIDRecord);
        
        MapReply mapReply = nbService.getMappingService().handleMapRequest(mapRequest);
        
        EidToLocatorRecord record = mapReply.getEidToLocatorRecords().get(0);
    	        
        return record;
    }
    
    
    @Path("/{containerName}/mapping")
    @PUT
    @Produces("text/plain")
    @Consumes(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The containerName passed was not found"),
        @ResponseCode(code = 503, condition = "Service unavailable") })
    
    public String addmapping(@PathParam("containerName") String containerName, @TypeHint(MapRegisterNB.class) MapRegisterNB mapRegisterNB) {
    	
    	handleContainerDoesNotExist(containerName);
    	
    	authorizationCheck(containerName, Privilege.WRITE);
    	
    	//TODO Check LISP key here
    	
    	LispAddressConvertorNB.convertGenericToLispAddresses(mapRegisterNB.getMapRegister());
    	
    	INorthboundService nbService = 
    			(INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName,this);
    	
    	MapNotify mapNotify = nbService.getMappingService().handleMapRegister(mapRegisterNB.getMapRegister());
    	    	
    	String response;
    	
    	if (mapNotify!=null){
    		response = "Mapping added with EID "+ mapNotify.getEidToLocatorRecords().get(0).getPrefix()+
        			" and RLOC "+mapNotify.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator()+"\n";
    	}else{
    		response = "Add mapping message received\n";
    	}
    	
    	return response;
    }    
 
    @Path("/{containerName}/mapping/{iid}/{afi}/{address}/{mask}")
    @GET
    @Produces(MediaType.APPLICATION_JSON) 
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The containerName passed was not found"),
        @ResponseCode(code = 503, condition = "Service unavailable") })
    
    public EidToLocatorRecord getMapping(@PathParam("containerName") String containerName, 
    		@PathParam("iid") int iid, @PathParam("afi") int afi, 
    		@PathParam("address") String address, @PathParam("mask") int mask) {
    	
    	handleContainerDoesNotExist(containerName);
    	
    	authorizationCheck(containerName, Privilege.READ);
    	
    	//TODO Check LISP key here
  
    	LispAddressGeneric EIDGeneric = new LispAddressGeneric(afi,address);
    	
    	if (iid!=0){
    		EIDGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode(),EIDGeneric);
    		EIDGeneric.setLcafType(LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
    		EIDGeneric.setInstanceId(iid);
    	}
    	
    	
    	EidToLocatorRecord record = lookupEID(containerName, mask, LispAddressConvertorNB.convertToLispAddress(EIDGeneric));
        
        LispAddressConvertorNB.convertRecordFromLispToGenericAddresses(record);
        
        return record;
    }   
    
    
    @Path("/{containerName}/mapping/{iid}/{afi}/{srcAdd}/{srcML}/{dstAdd}/{dstML}")
    @GET
    @Produces(MediaType.APPLICATION_JSON) 
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The containerName passed was not found"),
        @ResponseCode(code = 503, condition = "Service unavailable") })
    
    public EidToLocatorRecord getMapping(@PathParam("containerName") String containerName, 
    		@PathParam("iid") int iid, @PathParam("afi") int afi, 
    		@PathParam("srcAdd") String srcAdd, @PathParam("srcML") int srcML,
    		@PathParam("srcAdd") String dstAdd, @PathParam("srcML") int dstML) {
    	
    	handleContainerDoesNotExist(containerName);
    	
    	authorizationCheck(containerName, Privilege.READ);

    	//TODO Check LISP key here
    	
    	LispAddressGeneric srcGeneric = new LispAddressGeneric(afi,srcAdd);
    	LispAddressGeneric dstGeneric = new LispAddressGeneric(afi,dstAdd);
    	
    	if (iid!=0){
    		srcGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode(),srcGeneric);
    		srcGeneric.setLcafType(LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
    		srcGeneric.setInstanceId(iid);
    		
    		dstGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode(),dstGeneric);
    		dstGeneric.setLcafType(LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
    		dstGeneric.setInstanceId(iid);
    	}
    	
    	LispAddressGeneric eidGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode());
    	
    	eidGeneric.setLcafType(LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());
    	eidGeneric.setSrcAddress(srcGeneric);
    	eidGeneric.setSrcMaskLength((byte) srcML);
    	eidGeneric.setDstAddress(dstGeneric);
    	eidGeneric.setDstMaskLength((byte) dstML);
    	
    	
    	int mask = 0; //Not used here
    	
    	EidToLocatorRecord record = lookupEID(containerName, mask, LispAddressConvertorNB.convertToLispAddress(eidGeneric));
        
        LispAddressConvertorNB.convertRecordFromLispToGenericAddresses(record);
        
        return record;
    }    
}


