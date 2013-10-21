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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundService;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.sbplugin.ILispSouthboundPlugin;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundPlugin extends AbstractBindingAwareProvider implements ILispSouthboundPlugin, CommandProvider {
    protected static final Logger logger = LoggerFactory.getLogger(LispSouthboundPlugin.class);

    private LispIoThread thread;
    private LispSouthboundService service;
    private volatile DatagramSocket socket = null;

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }

    protected void stopImpl(BundleContext context) {
        if (thread != null) {
            thread.stopRunning();
        }
        thread = null;
        service = null;
        logger.info("LISP (RFC6830) Mapping Service is down!");
    }

    private class LispIoThread extends Thread {
        private volatile boolean running;

        public LispIoThread() {
            super("Lisp Thread");
            running = true;
        }

        @Override
        public void run() {
            String lispBindAddress = "0.0.0.0";
            String lispIp = System.getProperty("lispip");
            if (lispIp != null) {
                lispBindAddress = lispIp;
            }

            int lispPortNumber = LispMessage.PORT_NUM;
            int lispReceiveTimeout = 1000;

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
                    logger.debug("Received a packet!");
                } catch (SocketTimeoutException ste) {
                    continue;
                } catch (IOException e) {
                    logger.error("IO Exception while trying to recieve packet", e);
                }
                logger.debug("Handling packet from {}:{} (len={})", packet.getAddress().getHostAddress(), packet.getPort(), packet.getLength());

                try {
                    service.handlePacket(packet);
                } catch (Throwable t) {
                    logger.error("Error while handling packet", t);
                }
            }

            socket.close();
            logger.info("Closed socket!");
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

    public void onSessionInitiated(ProviderContext session) {
        service = new LispSouthboundService();
        thread = new LispIoThread();
        logger.info("LISP (RFC6830) Mapping Service is up!");
        thread.start();

        // OSGI console
        registerWithOSGIConsole();

        logger.info("Provider Session initialized");

        service.setNotificationProvider(session.getSALService(NotificationProviderService.class));
        session.addRpcImplementation(ILispSouthboundPlugin.class, this);
    }

    public Future<RpcResult<Void>> handleMapNotify(MapNotify mapNotify, InetAddress address) {
        logger.trace("handleMapNotify called!!");
        if (mapNotify != null) {
            ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotify);
            DatagramPacket notify = new DatagramPacket(outBuffer.array(), outBuffer.limit());
            notify.setPort(LispMessage.PORT_NUM);
            notify.setAddress(address);
            try {
                logger.debug("Sending MapNotify on port " + LispMessage.PORT_NUM + " to address: " + address);
                socket.send(notify);
            } catch (IOException e) {
                logger.error("Failed to send MapNotify", e);
            }
        } else {
            logger.debug("MapNotify was null");
        }
        return null;
    }

    @Override
    public Future<RpcResult<Void>> handleMapReply(MapReply mapReply, InetAddress address) {
        logger.trace("handleMapReply called!!");
        if (mapReply != null) {
            ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReply);
            DatagramPacket reply = new DatagramPacket(outBuffer.array(), outBuffer.limit());
            reply.setPort(LispMessage.PORT_NUM);
            reply.setAddress(address);
            try {
                logger.debug("Sending MapReply on port " + LispMessage.PORT_NUM + " to address: " + address);
                socket.send(reply);
            } catch (IOException e) {
                logger.error("Failed to send MapReply", e);
            }
        } else {
            logger.debug("MapReply was null");
        }
        return null;
    }
}
