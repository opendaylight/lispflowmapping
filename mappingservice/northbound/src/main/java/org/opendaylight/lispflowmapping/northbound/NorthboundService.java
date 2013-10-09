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
import org.opendaylight.controller.northbound.commons.exception.BadRequestException;
import org.opendaylight.controller.northbound.commons.exception.ResourceConflictException;
import org.opendaylight.controller.northbound.commons.exception.ResourceNotFoundException;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.lispflowmapping.northbound.MapRegisterNB;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.lispflowmapping.interfaces.dao.IQueryAll;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;


@Path("/")
public class NorthboundService implements INorthboundService, CommandProvider{
    protected static final Logger logger = LoggerFactory.getLogger(NorthboundService.class);
    private IFlowMapping mappingService;

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
        return;
    }

    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---Northbound Service---\n");
        help.append("\t runRegister        - Run a map register example\n");
        help.append("\t runRequest      - run a map request example\n");
        return help.toString();
    } 
    
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
    
    

    
    @Path("/{containerName}/addmapping")
    @POST
    @Produces("text/plain")
    @Consumes(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The containerName passed was not found"),
        @ResponseCode(code = 503, condition = "Service unavailable") })
    
    public String addmapping(@PathParam("containerName") String containerName, @TypeHint(MapRegisterNB.class) MapRegisterNB mapregisterNB) {
    	
    	handleContainerDoesNotExist(containerName);
    	
    	INorthboundService nbService = 
    			(INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName,this);
    	
    	//Temporal EID/RLOC processing based on String and assuming IPv4
    	LispIpv4Address EID;
    	List<LocatorRecord> locRecordList;
    	LispIpv4Address RLOC;
    	int EIDtoLocatorRecordCount = mapregisterNB.getMapRegister().getEidToLocatorRecords().size();
    	
    	for (int i=0;i<EIDtoLocatorRecordCount;i++){
	    	EID = new LispIpv4Address(mapregisterNB.getMapRegister().getEidToLocatorRecords().get(i).getPrefixString());
	    	mapregisterNB.getMapRegister().getEidToLocatorRecords().get(i).setPrefix(EID);
	    	locRecordList = mapregisterNB.getMapRegister().getEidToLocatorRecords().get(i).getLocators();
	    	
	    	for(int j=0;j<locRecordList.size();j++){
	    		RLOC = new LispIpv4Address(locRecordList.get(j).getLocatorString());
	    		locRecordList.get(j).setLocator(RLOC);
	    	}
    	
    	}
    	
    	MapNotify mapNotify = nbService.getMappingService().handleMapRegister(mapregisterNB.getMapRegister());
    	
    	String response;
    	
    	if (mapNotify!=null){
    		response = "Mapping added with EID "+ mapNotify.getEidToLocatorRecords().get(0).getPrefix()+
        			" and RLOC "+mapNotify.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator()+"\n";
    	}else{
    		response = "Add mapping message received\n";
    	}
    	
    	return response;
    }    
       
}


