/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.containermanager.IContainerManager;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.BadRequestException;
import org.opendaylight.controller.northbound.commons.exception.InternalServerErrorException;
import org.opendaylight.controller.northbound.commons.exception.ResourceNotFoundException;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class LispMappingNorthbound implements ILispmappingNorthbound {
    protected static final Logger LOG = LoggerFactory.getLogger(LispMappingNorthbound.class);
    private IFlowMapping mappingService;

    private String userName;

    // Based on code from controller project
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
        LOG.trace("FlowMapping set in LispNorthbound");
        this.mappingService = mappingService;
    }

    void unsetFlowMappingService(IFlowMapping mappingService) {
        LOG.trace("LispDAO was unset in LISP Northbound");
        this.mappingService = null;
    }

    public void init() {
        LOG.trace("LISP Northbound Service is initialized!");
    }

    public void start() {
        LOG.info("LISP Northbound Service is up!");
    }

    public void stop() {
        LOG.info("LISP Northbound Service is down!");
    }

    public void destroy() {
        LOG.trace("LISP Northbound Service is destroyed!");
        mappingService = null;
    }

    // Based on code from controller project
    private void handleContainerDoesNotExist(String containerName) {
        IContainerManager containerManager = (IContainerManager) ServiceHelper.getGlobalInstance(IContainerManager.class, this);
        if (containerManager == null) {
            throw new ServiceUnavailableException("Container " + containerName + RestMessages.NOCONTAINER.toString());
        }

        List<String> containerNames = containerManager.getContainerNames();
        for (String cName : containerNames) {
            if (cName.trim().equalsIgnoreCase(containerName.trim())) {
                return;
            }
        }

        throw new ResourceNotFoundException("Container " + containerName + " " + RestMessages.NOCONTAINER.toString());
    }

    private void authorizationCheck(String containerName, Privilege privilege) {

        if (!NorthboundUtils.isAuthorized(getUserName(), containerName, privilege, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation on container " + containerName);
        }
    }

    private EidToLocatorRecord lookupEID(String containerName, int mask, LispAddress EID) {

        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        MapRequest mapRequest = new MapRequest();
        EidRecord EIDRecord = new EidRecord((byte) mask, EID);
        mapRequest.addEidRecord(EIDRecord);
        mapRequest.setSourceEid(new LispIpv4Address("127.0.0.1"));

        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest mr = YangTransformerNB.transformMapRequest(mapRequest);
        MapReply mapReply;
        try {
            mapReply = nbService.getMappingService().handleMapRequest(mr);
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error looking up the EID");
        }

        if (mapReply == null) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error looking up the EID");
        }

        return mapReply.getEidToLocatorRecord().get(0);
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

            try {
                lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);
            } catch (Exception e) {
                throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
            }
            mask = mapRegisterNB.getMapRegister().getEidToLocatorRecords().get(i).getMaskLength();
            try {
                storedKey = mappingService.getAuthenticationKey(YangTransformerNB.transformLispAddress(lispAddress), mask);
            } catch (Exception e) {
                throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while retrieving the key");
            }

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
     * http://localhost:8080/lispflowmapping/nb/v2/default/mapping
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
    @Consumes(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 400, condition = "Invalid data passed"),
            @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 500, condition = "Internal Server Error: Addition of mapping failed"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public Response addMapping(@PathParam("containerName") String containerName, @TypeHint(MapRegisterNB.class) MapRegisterNB mapRegisterNB) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.WRITE);

        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        try {
            keyCheck(nbService.getMappingService(), mapRegisterNB);

            LispAddressConvertorNB.convertGenericToLispAddresses(mapRegisterNB.getMapRegister());
        } catch (Exception e) {
            throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
        }
        // Always request MapNotify
        mapRegisterNB.getMapRegister().setWantMapNotify(true);

        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister mr = null;
        try {
            mr = YangTransformerNB.transformMapRegister(mapRegisterNB.getMapRegister());
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while converting the map register");

        }
        MapNotify mapNotify;
        try {
            mapNotify = nbService.getMappingService().handleMapRegister(mr);
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while registering the mapping");
        }

        if (mapNotify == null) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while registering the mapping");
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Delete a mapping from the LISP Map-Server database
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
     * http://localhost:8080/lispflowmapping/nb/v2/default/mapping/0/1/10.0.0.1/32
     *
     * </pre>
     */

    @Path("/{containerName}/mapping/{iid}/{afi}/{address}/{mask}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 400, condition = "Invalid data passed"),
            @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 500, condition = "Internal Server Error: Addition of mapping failed"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public Response deleteMapping(@PathParam("containerName") String containerName, @PathParam("iid") int iid,
            @PathParam("afi") int afi, @PathParam("address") String address, @PathParam("mask") int mask) {

        handleContainerDoesNotExist(containerName);
        authorizationCheck(containerName, Privilege.WRITE);

        LispAddressGeneric eidGeneric = parseAddressURL(iid, afi, address, mask);
        LispAddress eid;
        try {
            eid = LispAddressConvertorNB.convertToLispAddress(eidGeneric);
        } catch (Exception e) {
            throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
        }

        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        try {
            nbService.getMappingService().removeMapping(YangTransformerNB.transformLispAddress(eid), mask);
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while deleting the key");
        }

        return Response.status(Response.Status.OK).build();
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
     * http://localhost:8080/lispflowmapping/nb/v2/default/mapping/0/1/10.0.0.1/32
     *
     * </pre>
     */

    private LispAddressGeneric parseAddressURL(int iid, int afi, String address, int mask) {
        LispAddressGeneric eidGeneric = new LispAddressGeneric(afi, address);

        if (iid != 0) {
            eidGeneric = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode(), eidGeneric);
            eidGeneric.setLcafType(LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
            eidGeneric.setInstanceId(iid);
            // XXX since we can't set iid mask length in the URL, set default to max mask corresponding to afi
            eidGeneric.setIidMaskLength(MaskUtil.getMaxMaskForAfi(afi));
        }
        return eidGeneric;
    }

    private LispAddressGeneric parseSrcDstAddressURL(int iid, int afi, String srcAdd, int srcML, String dstAdd, int dstML) {
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

        LispAddressGeneric address = new LispAddressGeneric(AddressFamilyNumberEnum.LCAF.getIanaCode());

        address.setLcafType(LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());
        address.setSrcAddress(srcGeneric);
        address.setSrcMaskLength((byte) srcML);
        address.setDstAddress(dstGeneric);
        address.setDstMaskLength((byte) dstML);

        return address;
    }

    @Path("/{containerName}/mapping/{iid}/{afi}/{address}/{mask}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 400, condition = "Invalid data passed"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 500, condition = "Internal Server Error: Get mapping failed"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord getMapping(@PathParam("containerName") String containerName,
            @PathParam("iid") int iid, @PathParam("afi") int afi, @PathParam("address") String address, @PathParam("mask") int mask) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric eidGeneric = parseAddressURL(iid, afi, address, mask);

        LispAddress eid;
        try {
            eid = LispAddressConvertorNB.convertToLispAddress(eidGeneric);
        } catch (Exception e) {
            throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
        }

        EidToLocatorRecord record = lookupEID(containerName, mask, eid);

        org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord legacyRecord = YangTransformerNB.reTransformEidToLocatorRecord(record);
        LispAddressConvertorNB.convertRecordToGenericAddress(legacyRecord);

        return legacyRecord;
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
     *
     *      http://localhost:8080/lispflowmapping/nb/v2/default/mapping/0/1/10.0.0.1/32/20.0.0.2/32
     *
     * </pre>
     */

    @Path("/{containerName}/mapping/{iid}/{afi}/{srcAdd}/{srcML}/{dstAdd}/{dstML}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord getMapping(@PathParam("containerName") String containerName,
            @PathParam("iid") int iid, @PathParam("afi") int afi, @PathParam("srcAdd") String srcAdd, @PathParam("srcML") int srcML,
            @PathParam("dstAdd") String dstAdd, @PathParam("dstML") int dstML) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric eidGeneric = parseSrcDstAddressURL(iid, afi, srcAdd, srcML, dstAdd, dstML);

        int mask = 0; // Not used here

        EidToLocatorRecord record = lookupEID(containerName, mask, LispAddressConvertorNB.convertToLispAddress(eidGeneric));

        org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord legacyRecord = YangTransformerNB.reTransformEidToLocatorRecord(record);
        LispAddressConvertorNB.convertRecordToGenericAddress(legacyRecord);

        return legacyRecord;
    }

    /**
     * Set the authentication key for an EID prefix
     *
     * @param containerName
     *            name of the container context in which the key needs to be set
     * @param authKeyNB
     *            JSON object that contains the key information
     *
     * @return Text plain confirming reception
     *
     *         <pre>
     * Example:
     *
     * Request URL:
     * http://localhost:8080/lispflowmapping/nb/v2/default/key
     *
     * Request body in JSON:
     *
     * {
     * "key" : "asdf",
     * "maskLength" : 24,
     * "address" :
     * {
     * "ipAddress" : "10.0.0.1",
     * "afi" : 1
     * }
     * }
     *
     * </pre>
     */

    @Path("/{containerName}/key")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 400, condition = "Invalid data passed"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 500, condition = "Internal Server Error: Addition of key failed"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public Response addAuthKey(@PathParam("containerName") String containerName, @TypeHint(AuthKeyNB.class) AuthKeyNB authKeyNB) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.WRITE);

        LispAddress lispAddress;
        try {
            lispAddress = LispAddressConvertorNB.convertToLispAddress(authKeyNB.getAddress());
        } catch (Exception e) {
            throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
        }
        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        try {

            nbService.getMappingService().addAuthenticationKey(YangTransformerNB.transformLispAddress(lispAddress), authKeyNB.getMaskLength(),
                    authKeyNB.getKey());
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while adding the key");
        }

        return Response.status(Response.Status.OK).build();
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
     * http://localhost:8080/lispflowmapping/nb/v2/default/key/0/1/10.0.0.1/32
     *
     * </pre>
     */

    @Path("/{containerName}/key/{iid}/{afi}/{address}/{mask}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 400, condition = "Invalid data passed"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 500, condition = "Internal Server Error: Get key failed"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public AuthKeyNB getAuthKey(@PathParam("containerName") String containerName, @PathParam("iid") int iid, @PathParam("afi") int afi,
            @PathParam("address") String address, @PathParam("mask") int mask) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric lispAddressGeneric = parseAddressURL(iid, afi, address, mask);

        LispAddress lispAddress;

        try {
            lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);
        } catch (Exception e) {
            throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
        }
        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        String key;
        try {
            key = nbService.getMappingService().getAuthenticationKey(YangTransformerNB.transformLispAddress(lispAddress), mask);
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while retrieving the key");
        }

        if (key == null) {
            throw new ResourceNotFoundException(RestMessages.NORESOURCE.toString() + " : The requested key was not found");
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
     *
     * http://localhost:8080/lispflowmapping/nb/v2/default/key/0/1/10.0.0.1/32/20.0.0.2/32
     *
     * </pre>
     */

    @Path("/{containerName}/key/{iid}/{afi}/{srcAdd}/{srcML}/{dstAdd}/{dstML}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public AuthKeyNB getAuthKey(@PathParam("containerName") String containerName, @PathParam("iid") int iid, @PathParam("afi") int afi,
            @PathParam("srcAdd") String srcAdd, @PathParam("srcML") int srcML, @PathParam("dstAdd") String dstAdd, @PathParam("dstML") int dstML) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.READ);

        LispAddressGeneric lispAddressGeneric = parseSrcDstAddressURL(iid, afi, srcAdd, srcML, dstAdd, dstML);

        LispAddress lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);

        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        int mask = 0; // Not used here

        LispAddressContainer yangAddress = YangTransformerNB.transformLispAddress(lispAddress);

        String key = nbService.getMappingService().getAuthenticationKey(yangAddress, mask);

        if (key == null) {
            throw new ResourceNotFoundException(RestMessages.NORESOURCE.toString() + " : The requested key was not found");
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
     * http://localhost:8080/lispflowmapping/nb/v2/default/key/0/1/10.0.0.1/32
     *
     * </pre>
     */

    @Path("/{containerName}/key/{iid}/{afi}/{address}/{mask}")
    @DELETE
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 400, condition = "Invalid data passed"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 500, condition = "Internal Server Error: Delete key failed"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public Response delAuthKey(@PathParam("containerName") String containerName, @PathParam("afi") int afi, @PathParam("iid") int iid,
            @PathParam("address") String address, @PathParam("mask") int mask) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.WRITE);

        LispAddressGeneric lispAddressGeneric = parseAddressURL(iid, afi, address, mask);

        LispAddress lispAddress;
        try {
            lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);
        } catch (Exception e) {
            throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
        }

        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        try {
            nbService.getMappingService().removeAuthenticationKey(YangTransformerNB.transformLispAddress(lispAddress), mask);
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while deleting the key");
        }

        return Response.status(Response.Status.OK).build();
    }

    /**
     * Delete the key used to register an EID prefix
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
     * http://localhost:8080/lispflowmapping/nb/v2/default/key/0/1/10.0.0.1/32/20.0.0.2/32
     *
     * </pre>
     */

    @Path("/{containerName}/key/{iid}/{afi}/{srcAdd}/{srcML}/{dstAdd}/{dstML}")
    @DELETE
    @StatusCodes({ @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 400, condition = "Invalid data passed"),
            @ResponseCode(code = 404, condition = "The containerName passed was not found"),
            @ResponseCode(code = 500, condition = "Internal Server Error: Delete key failed"),
            @ResponseCode(code = 503, condition = "Service unavailable") })
    public Response delAuthKey(@PathParam("containerName") String containerName, @PathParam("iid") int iid, @PathParam("afi") int afi,
            @PathParam("srcAdd") String srcAdd, @PathParam("srcML") int srcML, @PathParam("dstAdd") String dstAdd, @PathParam("dstML") int dstML) {

        handleContainerDoesNotExist(containerName);

        authorizationCheck(containerName, Privilege.WRITE);

        LispAddressGeneric lispAddressGeneric = parseSrcDstAddressURL(iid, afi, srcAdd, srcML, dstAdd, dstML);

        LispAddress lispAddress;
        try {
            lispAddress = LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric);
        } catch (Exception e) {
            throw new BadRequestException(RestMessages.INVALIDDATA.toString() + " : Address is not valid");
        }

        ILispmappingNorthbound nbService = (ILispmappingNorthbound) ServiceHelper.getInstance(ILispmappingNorthbound.class, containerName, this);

        int mask = 0; // Not used here

        try {
            nbService.getMappingService().removeAuthenticationKey(YangTransformerNB.transformLispAddress(lispAddress), mask);
        } catch (Exception e) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString() + " : There was an error while deleting the key");
        }

        return Response.status(Response.Status.OK).build();
    }

}
