/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.northbound;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.containermanager.IContainerManager;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ResourceNotFoundException;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class NorthboundService implements INorthboundService, CommandProvider {
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

    public IFlowMapping getMappingService() {
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
        mapRegister.setKeyId((short) 0).setNonce(0).setWantMapNotify(true);
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

        logger.info("EID:" + mapReply.getEidToLocatorRecords().get(0).getPrefix() + " TTL " + mapReply.getEidToLocatorRecords().get(0).getRecordTtl());

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
        IContainerManager containerManager = (IContainerManager) ServiceHelper.getGlobalInstance(IContainerManager.class, this);
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

    private void authorizationCheck(String containerName, Privilege privilege) {

        if (!NorthboundUtils.isAuthorized(getUserName(), containerName, privilege, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation on container " + containerName);
        }
    }

    private EidToLocatorRecord lookupEID(String containerName, int mask, LispAddress EID) {

        INorthboundService nbService = (INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName, this);

        MapRequest mapRequest = new MapRequest();
        EidRecord EIDRecord = new EidRecord((byte) mask, EID);
        mapRequest.addEidRecord(EIDRecord);

        MapReply mapReply = nbService.getMappingService().handleMapRequest(mapRequest);

        if (mapReply == null) {
            logger.warn("Couldn't lookup EID, got null mapReply");
            return null;
        }

        EidToLocatorRecord record = null;

        record = mapReply.getEidToLocatorRecords().get(0);

        return record;
    }

    private void keyCheck(IFlowMapping mappingService, MapRegisterNB mapRegisterNB) {

        String usedKey = mapRegisterNB.getKey();

        LispAddressGeneric lispAddressGeneric;
        LispAddress lispAddress;
        int mask;
        String storedKey;

        int numEidRecords = mapRegisterNB.getMapRegister().getEidToLocatorRecords().size();

        for (int i = 0; i < numEidRecords; i++) {

            lispAddressGeneric = mapRegisterNB.getMapRegister().getEidToLocatorRecords().get(i).getPrefixGeneric();
            lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);
            mask = mapRegisterNB.getMapRegister().getEidToLocatorRecords().get(i).getMaskLength();
            storedKey = mappingService.getAuthenticationKey(lispAddress, mask);

            if (!usedKey.equals(storedKey)) {
                throw new UnauthorizedException("The key used to register the mapping " + "does not match with the stored key for that mapping");
            }
        }
    }

    /**
     * Add a mapping to the LISP mapping system
     * 
     * @param containerName
     *            name of the container context in which the mapping needs to be
     *            added
     * @param mapRegisterNB
     *            JSON object that contains the mapping information
     * 
     * @return Text plain confirming reception
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/lispflowmapping/default/mapping
     * 
     * Request body in JSON:
     * 
     * { 
     * "key" : "asdf", 
     * "mapregister" : 
     *   { 
     *   "wantMapNotify" : true, 
     *   "proxyMapReply" : false, 
     *   "eidToLocatorRecords" : 
     *     [ 
     *       { 
     *       "authoritative" : true, 
     *       "prefixGeneric" : 
     *         { 
     *         "ipAddress" : "10.0.0.1", 
     *         "afi" : 1
     *         },
     *       "mapVersion" : 3, 
     *       "maskLength" : 32, 
     *       "action" : "NoAction", 
     *       "locators" : 
     *         [ 
     *           { 
     *           "multicastPriority" : 3, 
     *           "locatorGeneric" : 
     *             { 
     *             "ipAddress" : "3.3.3.3", 
     *             "afi" : 1
     *             }, 
     *           "routed" : true, 
     *           "multicastWeight" : 3, 
     *           "rlocProbed" : false, 
     *           "localLocator" : false, 
     *           "priority" : 3, 
     *           "weight" : 3 
     *           } 
     *         ], 
     *       "recordTtl" : 3 
     *       } 
     *     ], 
     *   "nonce" : 3, 
     *   "keyId" : 0 
     *   } 
     * }
     * </pre>
     */

    @Path("/{containerName}/mapping")
    @PUT
    @Produces("text/plain")
    @Consumes(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public String addMapping(@PathParam("containerName") String containerName, @TypeHint(MapRegisterNB.class) MapRegisterNB mapRegisterNB) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.WRITE);

        INorthboundService nbService = (INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName, this);

        keyCheck(nbService.getMappingService(), mapRegisterNB);

        LispAddressConvertorNB.convertGenericToLispAddresses(mapRegisterNB.getMapRegister());

        MapNotify mapNotify = nbService.getMappingService().handleMapRegister(mapRegisterNB.getMapRegister());

        String response;

        if (mapNotify != null) {
            response = "Mapping added with EID " + mapNotify.getEidToLocatorRecords().get(0).getPrefix() + " and RLOC "
                    + mapNotify.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator() + "\n";
        } else {
            response = "Add mapping message received\n";
        }

        return response;
    }

    /**
     * Retrieve a mapping from the LISP mapping system
     * 
     * @param containerName
     *            name of the container context from which the mapping is going
     *            to be retrieved
     * @param iid
     *            Instance-ID of the address (0 if none)
     * 
     * @param afi
     *            Address Family of the address (IPv4, IPv6 or MAC)
     * 
     * @param address
     *            Address of type defined by afi
     * 
     * @param mask
     *            Network mask length
     * 
     * @return EidToLocatorRecord as a JSON object
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/lispflowmapping/default/mapping/0/1/10.0.0.1/32
     * 
     * </pre>
     */

    @Path("/{containerName}/mapping/{iid}/{afi}/{address}/{mask}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public EidToLocatorRecord getMapping(@PathParam("containerName") String containerName, @PathParam("iid") int iid, @PathParam("afi") int afi,
            @PathParam("address") String address, @PathParam("mask") int mask) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric EIDGeneric = new LispAddressGeneric(afi, address);

        if (iid != 0) {
            EIDGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode(), EIDGeneric);
            EIDGeneric.setLcafType(LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
            EIDGeneric.setInstanceId(iid);
        }

        EidToLocatorRecord record = lookupEID(containerName, mask, LispAddressConvertorNB.convertToLispAddress(EIDGeneric));

        LispAddressConvertorNB.convertRecordToGenericAddress(record);

        return record;
    }

    /**
     * Retrieve a mapping from the LISP mapping system, using Source-Destination
     * LCAF as EID
     * 
     * @param containerName
     *            name of the container context from which the mapping is going
     *            to be retrieved
     * @param iid
     *            Instance-ID of the addresses (0 if none)
     * 
     * @param afi
     *            Address Family of the addresses (IPv4, IPv6 or MAC)
     * 
     * @param srcAdd
     *            Source address of type defined by afi
     * 
     * @param srcML
     *            Network mask length of the source address
     * 
     * @param dstAdd
     *            Destination address of type defined by afi
     * 
     * @param dstML
     *            Network mask length of the destination address
     * 
     * @return EidToLocatorRecord as a JSON object
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/lispflowmapping/default/mapping/0/1/10.0.0.1/32/20.0.0.2/32
     * 
     * </pre>
     */

    @Path("/{containerName}/mapping/{iid}/{afi}/{srcAdd}/{srcML}/{dstAdd}/{dstML}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public EidToLocatorRecord getMapping(@PathParam("containerName") String containerName, @PathParam("iid") int iid, @PathParam("afi") int afi,
            @PathParam("srcAdd") String srcAdd, @PathParam("srcML") int srcML, @PathParam("dstAdd") String dstAdd, @PathParam("dstML") int dstML) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric srcGeneric = new LispAddressGeneric(afi, srcAdd);
        LispAddressGeneric dstGeneric = new LispAddressGeneric(afi, dstAdd);

        if (iid != 0) {
            srcGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode(), srcGeneric);
            srcGeneric.setLcafType(LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
            srcGeneric.setInstanceId(iid);

            dstGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode(), dstGeneric);
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

        LispAddressConvertorNB.convertRecordToGenericAddress(record);

        return record;
    }

    /**
     * Set the authentication key for an EID prefix
     * 
     * @param containerName
     *            name of the container context in which the key needs to be
     *            setted
     * @param authKeyNB
     *            JSON object that contains the key information
     * 
     * @return Text plain confirming reception
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/lispflowmapping/default/key
     * 
     * Request body in JSON:
     * 
     * { 
     * "key" : "asdf", 
     * "maskLength" : 24, 
     * "address" : 
     *   { 
     *   "ipAddress" : "10.0.0.1", 
     *   "afi" : 1
     *   }
     * }
     * 
     * </pre>
     */

    @Path("/{containerName}/key")
    @PUT
    @Produces("text/plain")
    @Consumes(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public String addAuthKey(@PathParam("containerName") String containerName, @TypeHint(AuthKeyNB.class) AuthKeyNB authKeyNB) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.WRITE);

        LispAddress lispAddress = LispAddressConvertorNB.convertToLispAddress(authKeyNB.getAddress());

        INorthboundService nbService = (INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName, this);

        boolean success = nbService.getMappingService().addAuthenticationKey(lispAddress, authKeyNB.getMaskLength(), authKeyNB.getKey());

        if (success) {
            return "Key added successfully";
        } else {
            return "There was a problem while adding the key";
        }

    }

    /**
     * Retrieve the key used to register an EID prefix
     * 
     * @param containerName
     *            name of the container context from which the key is going to
     *            be retrieved
     * 
     * @param afi
     *            Address Family of the address (IPv4, IPv6 or MAC)
     * 
     * @param address
     *            Address of type defined by afi
     * 
     * @param mask
     *            Network mask length
     * 
     * @return AuthKeyNB as a JSON object
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/lispflowmapping/default/key/1/10.0.0.1/32
     * 
     * </pre>
     */

    @Path("/{containerName}/key/{afi}/{address}/{mask}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public AuthKeyNB getAuthKey(@PathParam("containerName") String containerName, @PathParam("afi") int afi, @PathParam("address") String address,
            @PathParam("mask") int mask) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric lispAddressGeneric = new LispAddressGeneric(afi, address);

        LispAddress lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);

        INorthboundService nbService = (INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName, this);

        String key = nbService.getMappingService().getAuthenticationKey(lispAddress, mask);

        if (key == null) {
            return null;
        }

        AuthKeyNB authKeyNB = new AuthKeyNB();

        authKeyNB.setKey(key);
        authKeyNB.setAddress(lispAddressGeneric);
        authKeyNB.setMaskLength(mask);

        return authKeyNB;
    }

    /**
     * Retrieve a key used to register a Source-Destination LCAF EID prefix
     * 
     * @param containerName
     *            name of the container context from which the key is going to
     *            be retrieved
     * 
     * @param afi
     *            Address Family of the addresses (IPv4, IPv6 or MAC)
     * 
     * @param srcAdd
     *            Source address of type defined by afi
     * 
     * @param srcML
     *            Network mask length of the source address
     * 
     * @param dstAdd
     *            Destination address of type defined by afi
     * 
     * @param dstML
     *            Network mask length of the destination address
     * 
     * @return AuthKeyNB as a JSON object
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/lispflowmapping/default/key/1/10.0.0.1/32/20.0.0.2/32
     * 
     * </pre>
     */

    @Path("/{containerName}/key/{afi}/{srcAdd}/{srcML}/{dstAdd}/{dstML}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public AuthKeyNB getAuthKey(@PathParam("containerName") String containerName, @PathParam("afi") int afi, @PathParam("srcAdd") String srcAdd,
            @PathParam("srcML") int srcML, @PathParam("dstAdd") String dstAdd, @PathParam("dstML") int dstML) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric srcGeneric = new LispAddressGeneric(afi, srcAdd);
        LispAddressGeneric dstGeneric = new LispAddressGeneric(afi, dstAdd);

        LispAddressGeneric lispAddressGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode());

        lispAddressGeneric.setLcafType(LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());
        lispAddressGeneric.setSrcAddress(srcGeneric);
        lispAddressGeneric.setSrcMaskLength((byte) srcML);
        lispAddressGeneric.setDstAddress(dstGeneric);
        lispAddressGeneric.setDstMaskLength((byte) dstML);

        LispAddress lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);

        INorthboundService nbService = (INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName, this);

        int mask = 0; //Not used here

        String key = nbService.getMappingService().getAuthenticationKey(lispAddress, mask);

        if (key == null) {
            return null;
        }

        AuthKeyNB authKeyNB = new AuthKeyNB();

        authKeyNB.setKey(key);
        authKeyNB.setAddress(lispAddressGeneric);
        authKeyNB.setMaskLength(mask);

        return authKeyNB;
    }

    /**
     * Delete the key used to register an EID prefix
     * 
     * @param containerName
     *            name of the container context from which the key is going to
     *            be deleted
     * 
     * @param afi
     *            Address Family of the address (IPv4, IPv6 or MAC)
     * 
     * @param address
     *            Address of type defined by afi
     * 
     * @param mask
     *            Network mask length
     * 
     * @return Text plain confirming deletion
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/lispflowmapping/default/key/1/10.0.0.1/32
     * 
     * </pre>
     */

    @Path("/{containerName}/key/{afi}/{address}/{mask}")
    @DELETE
    @Produces("text/plain")
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public String delAuthKey(@PathParam("containerName") String containerName, @PathParam("afi") int afi, @PathParam("address") String address,
            @PathParam("mask") int mask) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.WRITE);

        LispAddress lispAddress = LispAddressConvertorNB.convertToLispAddress(new LispAddressGeneric(afi, address));

        INorthboundService nbService = (INorthboundService) ServiceHelper.getInstance(INorthboundService.class, containerName, this);

        String response;

        if (nbService.getMappingService().removeAuthenticationKey(lispAddress, mask)) {
            response = "Success to remove authentication key";
        } else {
            response = "Failure to remove authentication key";
        }

        return response;
    }

}
