/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
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
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispPlugin;
import org.opendaylight.lispflowmapping.type.sbplugin.ILispSouthboundPlugin;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.transportaddress.TransportAddress;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

public class LispSouthboundPlugin extends AbstractBindingAwareProvider implements ILispSouthboundPlugin, IConfigLispPlugin, CommandProvider {
    protected static final Logger logger = LoggerFactory.getLogger(LispSouthboundPlugin.class);

    private static Object startLock = new Object();
    private LispIoThread thread;
    private LispSouthboundService lispSouthboundService;
    private volatile DatagramSocket socket = null;
    private final String MAP_NOTIFY = "MapNotify";
    private final String MAP_REPlY = "MapReply";
    private volatile String bindingAddress = null;
    private volatile boolean stillRunning = false;
    private volatile boolean alreadyInit = false;

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
        bundleContext.registerService(IConfigLispPlugin.class.getName(), this, null);
    }

    protected void stopImpl(BundleContext context) {
        unloadActions();
    }

    private void unloadActions() {
        if (thread != null) {
            thread.stopRunning();
        }
        lispSouthboundService = null;
        thread = null;
        logger.info("LISP (RFC6830) Mapping Service is down!");
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
        }
    }

    public void destroy() {
        unloadActions();
    }

    private class LispIoThread extends Thread {
        private volatile boolean running;

        public LispIoThread() {
            super("Lisp Thread");
            running = true;
        }

        @Override
        public void run() {
            stillRunning = true;

            int lispPortNumber = LispMessage.PORT_NUM;
            int lispReceiveTimeout = 1000;

            logger.info("LISP (RFC6830) Mapping Service is running and listening on " + bindingAddress);
            try {
                socket = new DatagramSocket(new InetSocketAddress(bindingAddress, lispPortNumber));
                socket.setSoTimeout(lispReceiveTimeout);
            } catch (SocketException e) {
                logger.error("Cannot open socket on UDP port " + lispPortNumber, e);
                return;
            }

            while (running) {
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    logger.trace("Received a packet!");
                } catch (SocketTimeoutException ste) {
                    continue;
                } catch (IOException e) {
                    logger.warn("IO Exception while trying to recieve packet", e);
                }
                logger.trace(String.format("Handling packet from {%s}:{%d} (len={%d})", packet.getAddress().getHostAddress(), packet.getPort(),
                        packet.getLength()));

                try {
                    lispSouthboundService.handlePacket(packet);
                } catch (Throwable t) {
                    logger.warn("Error while handling packet", t);
                }
            }

            socket.close();
            logger.trace("Socket closed");
            stillRunning = false;
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

    private void startIOThread() {
        thread = new LispIoThread();
        logger.info("LISP (RFC6830) Mapping Service Southbound Plugin is up!");
        thread.start();
    }

    public void onSessionInitiated(ProviderContext session) {
        logger.info("LISP (RFC6830) Mapping Service is up!");
        synchronized (startLock) {
            if (!alreadyInit) {
                alreadyInit = true;
                lispSouthboundService = new LispSouthboundService();
                registerWithOSGIConsole();
                registerRPCs(session);
                logger.trace("Provider Session initialized");
                if (bindingAddress == null) {
                    setLispAddress("0.0.0.0");
                }
            }

        }
    }

    private void registerRPCs(ProviderContext session) {
        try {
            lispSouthboundService.setNotificationProvider(session.getSALService(NotificationProviderService.class));
            session.addRpcImplementation(ILispSouthboundPlugin.class, this);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    public Future<RpcResult<Void>> handleMapNotify(MapNotify mapNotify, TransportAddress address) {
        logger.trace("handleMapNotify called!!");
        if (mapNotify != null) {
            ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotify);
            handleSerializedLispBuffer(address, outBuffer, MAP_NOTIFY);
        } else {
            logger.warn("MapNotify was null");
        }
        return null;
    }

    private void handleSerializedLispBuffer(TransportAddress address, ByteBuffer outBuffer, String packetType) {
        DatagramPacket packet = new DatagramPacket(outBuffer.array(), outBuffer.limit());
        packet.setPort(address.getPort().getValue());
        InetAddress ip = InetAddresses.forString(address.getIpAddress().getIpv4Address().getValue());
        packet.setAddress(ip);
        try {
            if (logger.isDebugEnabled()) {
                logger.trace("Sending " + packetType + " on port " + address.getPort().getValue() + " to address: " + ip);
            }
            socket.send(packet);
        } catch (IOException e) {
            logger.warn("Failed to send " + packetType, e);
        }
    }

    public Future<RpcResult<Void>> handleMapReply(MapReply mapReply, TransportAddress address) {
        logger.trace("handleMapReply called!!");
        if (mapReply != null) {
            ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReply);
            handleSerializedLispBuffer(address, outBuffer, MAP_REPlY);
        } else {
            logger.warn("MapReply was null");
        }
        return null;
    }

    public void setLispAddress(String address) {
        synchronized (startLock) {
            if (bindingAddress != null && bindingAddress.equals(address)) {
                logger.trace("configured lisp binding address didn't change.");
            } else {
                String action = (bindingAddress == null ? "Setting" : "Resetting");
                logger.trace(action + " lisp binding address to: " + address);
                bindingAddress = address;
                if (thread != null) {
                    thread.stopRunning();
                    while (stillRunning) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                startIOThread();
            }
        }
    }
}
