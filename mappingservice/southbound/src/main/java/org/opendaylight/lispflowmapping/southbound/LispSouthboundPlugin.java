/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundService;
import org.opendaylight.lispflowmapping.type.lisp.LispMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundPlugin implements ILispSouthboundPlugin {
    protected static final Logger logger = LoggerFactory.getLogger(LispSouthboundPlugin.class);

    private LispIoThread thread;
    private LispSouthboundService service;


    void setFlowMappingService(IFlowMapping mappingService) {
        logger.debug("FlowMapping set in LispSouthbound");
        service = new LispSouthboundService(mappingService, mappingService);
        logger.debug("Registering LispIpv4Address");
        logger.debug("Registering LispIpv6Address");
    }

    void unsetFlowMappingService(IFlowMapping mappingService) {
        logger.debug("LispDAO was unset in LispMappingService");
        service = null;
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


    public static String intToIpv4(int address) {
        return ((address >> 24) & 0xff) + "." + //
                ((address >> 16) & 0xff) + "." + //
                ((address >> 8) & 0xff) + "." + //
                ((address >> 0) & 0xff);
    }

    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---LISP Southbound Plugin---\n");
        return help.toString();
    }

}
