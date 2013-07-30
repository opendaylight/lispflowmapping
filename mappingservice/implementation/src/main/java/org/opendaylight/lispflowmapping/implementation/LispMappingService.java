/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.lispflowmapping.implementation.dao.InMemoryDAO;
import org.opendaylight.lispflowmapping.implementation.lisp.MapResolver;
import org.opendaylight.lispflowmapping.implementation.lisp.MapServer;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.interfaces.dao.IQueryAll;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServer;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMappingService implements CommandProvider, IFlowMapping {
    protected static final Logger logger = LoggerFactory.getLogger(LispMappingService.class);

    private ILispDAO lispDao = null;
    private IMapResolver mapResolver;
    private IMapServer mapServer;

    public static void main(String[] args) throws Exception {
        LispMappingService serv = new LispMappingService();
        serv.setLispDao(new InMemoryDAO());
        serv.init();
    }

    class LispIpv4AddressInMemoryConverter implements ILispTypeConverter<LispIpv4Address, Integer> {
    }

    class LispIpv6AddressInMemoryConverter implements ILispTypeConverter<LispIpv6Address, Integer> {
    }

    void setLispDao(ILispDAO dao) {
        logger.info("LispDAO set in LispMappingService");
        lispDao = dao;
        mapResolver = new MapResolver(dao);
        mapServer = new MapServer(dao);
        logger.debug("Registering LispIpv4Address");
        lispDao.register(LispIpv4AddressInMemoryConverter.class);
        logger.debug("Registering LispIpv6Address");
        lispDao.register(LispIpv6AddressInMemoryConverter.class);
    }

    void unsetLispDao(ILispDAO dao) {
        logger.debug("LispDAO was unset in LispMappingService");
        mapServer = null;
        mapResolver = null;
        lispDao = null;
    }

    public void init() {
        logger.debug("LISP (RFC6830) Mapping Service is initialized!");
        registerWithOSGIConsole();
    }

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }


    public void destroy() {
        logger.debug("LISP (RFC6830) Mapping Service is destroyed!");
        mapResolver = null;
        mapServer = null;
    }


    public void _removeEid(final CommandInterpreter ci) {
        lispDao.remove(new LispIpv4Address(ci.nextArgument()));
    }

    public void _dumpAll(final CommandInterpreter ci) {
        ci.println("EID\tRLOCs");
        if (lispDao instanceof IQueryAll) {
            ((IQueryAll) lispDao).getAll(new IRowVisitor() {
                String lastKey = "";

                public void visitRow(Class<?> keyType, Object keyId, String valueKey, Object value) {
                    String key = keyType.getSimpleName() + "#" + keyId;
                    if (!lastKey.equals(key)) {
                        ci.println();
                        ci.print(key + "\t");
                    }
                    ci.print(valueKey + "=" + value + "\t");
                    lastKey = key;
                }
            });
            ci.println();
        } else {
            ci.println("Not implemented by this DAO");
        }
        return;
    }


    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---LISP Mapping Service---\n");
        help.append("\t dumpAll        - Dump all current EID -> RLOC mapping\n");
        help.append("\t removeEid      - Remove a single LispIPv4Address Eid\n");
        return help.toString();
    }

    public MapReply handleMapRequest(MapRequest request) {
        return mapResolver.handleMapRequest(request);
    }

    public MapNotify handleMapRegister(MapRegister mapRegister) {
        return mapServer.handleMapRegister(mapRegister);
    }
}
