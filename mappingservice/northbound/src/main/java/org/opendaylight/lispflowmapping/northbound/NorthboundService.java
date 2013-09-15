/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.northbound;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NorthboundService implements INorthboundService, CommandProvider{
    
    protected static final Logger logger = LoggerFactory.getLogger(NorthboundService.class);
    private IFlowMapping mappingService;

    
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

    
    

}
