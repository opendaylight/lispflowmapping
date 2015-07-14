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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.southbound.lisp.ILispSouthboundService;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundService;
import org.opendaylight.lispflowmapping.southbound.lisp.LispXtrSouthboundService;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LfmControlPlaneService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.transportaddress.TransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

public class LispSouthboundPlugin implements IConfigLispSouthboundPlugin, AutoCloseable, BindingAwareProvider {
    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundPlugin.class);

    private static Object startLock = new Object();
    private LispIoThread lispThread;
    private LispIoThread xtrThread;
    private LispSouthboundService lispSouthboundService;
    private LispXtrSouthboundService lispXtrSouthboundService;
    private NotificationProviderService notificationService;
    private RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker broker;
    private volatile DatagramSocket socket = null;
    private volatile String bindingAddress = null;
    private volatile int xtrPort = LispMessage.XTR_PORT_NUM;
    private volatile boolean listenOnXtrPort = false;
    private BindingAwareBroker.RpcRegistration<LfmControlPlaneService> controlPlaneRpc;
    private DatagramSocket xtrSocket;

    public void init() {
        LOG.info("LISP (RFC6830) Mapping Service is up!");
        final LfmControlPlaneRpc lfmCpRpc = new LfmControlPlaneRpc(this);

        controlPlaneRpc = rpcRegistry.addRpcImplementation(LfmControlPlaneService.class, lfmCpRpc);
        broker.registerProvider(this);

        synchronized (startLock) {
            lispSouthboundService = new LispSouthboundService();
            lispXtrSouthboundService = new LispXtrSouthboundService();
            lispSouthboundService.setNotificationProvider(this.notificationService);
            lispXtrSouthboundService.setNotificationProvider(this.notificationService);
            LOG.trace("Provider Session initialized");
            if (bindingAddress == null) {
                setLispAddress("0.0.0.0");
            }
            LOG.info("LISP (RFC6830) Mapping Service is up!");
        }
    }

    public void setNotificationProviderService(NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }

    public void setRpcRegistryDependency(RpcProviderRegistry rpcRegistry) {
        this.rpcRegistry = rpcRegistry;
    }

    public void setBindingAwareBroker(BindingAwareBroker broker) {
        this.broker = broker;
    }

    private void unloadActions() {
        if (lispThread != null) {
            lispThread.stopRunning();
        }
        lispSouthboundService = null;
        lispXtrSouthboundService = null;
        lispThread = null;
        xtrThread = null;
        bindingAddress = null;
        LOG.info("LISP (RFC6830) Mapping Service is down!");
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
        }
    }

    private class LispIoThread extends Thread {
        private volatile boolean shouldRun;
        private volatile DatagramSocket threadSocket = null;
        private volatile ILispSouthboundService service;
        private volatile boolean running;

        public LispIoThread(DatagramSocket socket, ILispSouthboundService service) {
            super("Lisp Thread");
            this.threadSocket = socket;
            this.service = service;
            shouldRun = true;
        }

        @Override
        public void run() {
            running = true;

            int lispReceiveTimeout = 1000;

            LOG.info("LISP (RFC6830) Mapping Service is running and listening on address: " + bindingAddress
                    + " port: " + threadSocket.getLocalPort());
            try {

                threadSocket.setSoTimeout(lispReceiveTimeout);
            } catch (SocketException e) {
                LOG.error("Cannot open socket on UDP port " + threadSocket.getLocalPort(), e);
                return;
            }

            while (shouldRun) {
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    threadSocket.receive(packet);
                    LOG.trace("Received a packet!");
                } catch (SocketTimeoutException ste) {
                    continue;
                } catch (IOException e) {
                    LOG.warn("IO Exception while trying to recieve packet", e);
                }
                LOG.trace(String.format("Handling packet from {%s}:{%d} (len={%d})", packet.getAddress()
                        .getHostAddress(), packet.getPort(), packet.getLength()));

                try {
                    this.service.handlePacket(packet);
                } catch (Exception e) {
                    LOG.warn("Error while handling packet", e);
                }
            }

            threadSocket.close();
            LOG.trace("Socket closed");
            running = false;
        }

        public void stopRunning() {
            shouldRun = false;
        }

        public boolean isRunning() {
            return running;
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
        if (socket != null) {
            while (!socket.isClosed()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
        try {
            socket = new DatagramSocket(new InetSocketAddress(bindingAddress, LispMessage.PORT_NUM));
            lispThread = new LispIoThread(socket, lispSouthboundService);
            lispThread.start();
            LOG.info("LISP (RFC6830) Mapping Service Southbound Plugin is up!");
            if (listenOnXtrPort) {
                restartXtrThread();
            }
        } catch (SocketException e) {
            LOG.error("couldn't start socket: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private void restartXtrThread() {
        try {
            stopXtrThread();
            xtrSocket = new DatagramSocket(new InetSocketAddress(bindingAddress, xtrPort));
            xtrThread = new LispIoThread(xtrSocket, lispXtrSouthboundService);
            xtrThread.start();
            LOG.info("xTR Southbound Plugin is up!");
        } catch (SocketException e) {
            LOG.warn("failed to start xtr thread: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    public void handleSerializedLispBuffer(TransportAddress address, ByteBuffer outBuffer, String packetType) {
        DatagramPacket packet = new DatagramPacket(outBuffer.array(), outBuffer.limit());
        packet.setPort(address.getPort().getValue());
        InetAddress ip = InetAddresses.forString(new String(address.getIpAddress().getValue()));
        packet.setAddress(ip);
        try {
            if (LOG.isDebugEnabled()) {
                LOG.trace("Sending " + packetType + " on port " + address.getPort().getValue() + " to address: " + ip);
            }
            socket.send(packet);
        } catch (IOException e) {
            LOG.warn("Failed to send " + packetType, e);
        }
    }

    public void setLispAddress(String address) {
        synchronized (startLock) {
            if (bindingAddress != null && bindingAddress.equals(address)) {
                LOG.trace("configured lisp binding address didn't change.");
            } else {
                String action = (bindingAddress == null ? "Setting" : "Resetting");
                LOG.trace(action + " lisp binding address to: " + address);
                bindingAddress = address;
                if (lispThread != null) {
                    lispThread.stopRunning();
                    while (lispThread.isRunning()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                stopXtrThread();
                startIOThread();
            }
        }
    }

    private void stopXtrThread() {
        if (xtrThread != null) {
            xtrThread.stopRunning();
            while (xtrThread.isRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public void shouldListenOnXtrPort(boolean shouldListenOnXtrPort) {
        listenOnXtrPort = shouldListenOnXtrPort;
        if (listenOnXtrPort) {
            LOG.debug("restarting xtr thread");
            restartXtrThread();
        } else {
            LOG.debug("terminating thread");
            stopXtrThread();
        }
    }

    @Override
    public void setXtrPort(int port) {
        this.xtrPort = port;
        if (listenOnXtrPort) {
            restartXtrThread();
        }
    }

    @Override
    public void close() throws Exception {
        unloadActions();
        controlPlaneRpc.close();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("LispSouthboundPlugin Provider Session Initiated");
    }
}
