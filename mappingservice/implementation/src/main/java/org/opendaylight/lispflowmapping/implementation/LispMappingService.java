/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.lispflowmapping.ILispMapping;
import org.opendaylight.lispflowmapping.dao.ILispDAO;
import org.opendaylight.lispflowmapping.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.dao.IQueryAll;
import org.opendaylight.lispflowmapping.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.implementation.dao.InMemoryDAO;
import org.opendaylight.lispflowmapping.implementation.lisp.LispService;
import org.opendaylight.lispflowmapping.implementation.lisp.MapResolver;
import org.opendaylight.lispflowmapping.implementation.lisp.MapServer;
import org.opendaylight.lispflowmapping.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.lisp.IMapServer;
import org.opendaylight.lispflowmapping.type.lisp.LispMessage;
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

public class LispMappingService implements ILispMapping, CommandProvider, IMapResolver, IMapServer {
    protected static final Logger logger = LoggerFactory.getLogger(LispMappingService.class);

    private LispIoThread thread;
    private ILispDAO lispDao = null;
    private LispService service;
    private IMapResolver mapResolver;
    private IMapServer mapServer;

    public static void main(String[] args) throws Exception {
        LispMappingService serv = new LispMappingService();
        serv.setLispDao(new InMemoryDAO());
        serv.init();
        serv.thread.start();
        while (true) {
            Thread.sleep(1000);
        }
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
        service = new LispService(mapResolver, mapServer);
        logger.debug("Registering LispIpv4Address");
        lispDao.register(LispIpv4AddressInMemoryConverter.class);
        logger.debug("Registering LispIpv6Address");
        lispDao.register(LispIpv6AddressInMemoryConverter.class);
    }

    void unsetLispDao(ILispDAO dao) {
        logger.debug("LispDAO was unset in LispMappingService");
        service = null;
        mapServer = null;
        mapResolver = null;
        lispDao = null;
    }

    public void init() {
        logger.debug("LISP (RFC6830) Mapping Service is initialized!");
        thread = new LispIoThread();
    }

    public void start() {
        logger.info("LISP (RFC6830) Mapping Service is up!");
        thread.start();

        // OSGI console
        registerWithOSGIConsole();
    }

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }

    public void stop() {
        thread.stopRunning();
        logger.info("LISP (RFC6830) Mapping Service is down!");
    }

    public void destroy() {
        logger.debug("LISP (RFC6830) Mapping Service is destroyed!");
        thread = null;
        service = null;
        mapResolver = null;
        mapServer = null;
    }

    private class LispIoThread extends Thread {
        private boolean running;

        public LispIoThread() {
            super("Lisp Thread");
            running = true;
        }

        @Override
        public void run() {
            DatagramSocket socket;
            int lispPortNumber = LispMessage.PORT_NUM;
            int lispReceiveTimeout = 1000;
            String lispBindAddress = "0.0.0.0";
            logger.info("LISP (RFC6830) Mapping Service is running and listening on " + lispBindAddress);
            try {
                socket = new DatagramSocket(new InetSocketAddress(lispBindAddress, lispPortNumber));
                socket.setSoTimeout(lispReceiveTimeout);
            } catch (SocketException e) {
                logger.warn("Cannot open socket on UDP port " + lispPortNumber, e);
                return;
            }

            while (running) {
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ste) {
                    continue;
                } catch (IOException e) {
                    // TODO: log
                }
                logger.debug("Handling packet from {}:{} (len={})", packet.getAddress().getHostAddress(), packet.getPort(), packet.getLength());
                try {
                    DatagramPacket reply = service.handlePacket(packet);

                    if (reply == null) {
                        continue;
                    }

                    reply.setAddress(packet.getAddress());
                    try {
                        logger.debug("sending reply to {}:{} (len={})", reply.getAddress().getHostAddress(), reply.getPort(), reply.getLength());
                        socket.send(reply);
                    } catch (IOException e) {
                        // TODO: log
                    }
                } catch (RuntimeException e) {
                    logger.warn("", e);
                }
            }

            socket.close();
        }

        public void stopRunning() {
            running = false;
        }
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

    public static String intToIpv4(int address) {
        return ((address >> 24) & 0xff) + "." + //
                ((address >> 16) & 0xff) + "." + //
                ((address >> 8) & 0xff) + "." + //
                ((address >> 0) & 0xff);
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
