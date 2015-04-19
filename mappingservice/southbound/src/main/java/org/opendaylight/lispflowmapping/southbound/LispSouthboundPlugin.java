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
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.southbound.lisp.ILispSouthboundService;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundService;
import org.opendaylight.lispflowmapping.southbound.lisp.LispXtrSouthboundService;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LfmControlPlaneService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapNotifyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.SendMapRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.transportaddress.TransportAddress;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.Futures;

public class LispSouthboundPlugin extends AbstractBindingAwareProvider implements IConfigLispSouthboundPlugin, CommandProvider, LfmControlPlaneService {
    protected static final Logger LOG = LoggerFactory.getLogger(LispSouthboundPlugin.class);

    private static Object startLock = new Object();
    private LispIoThread lispThread;
    private LispIoThread xtrThread;
    private LispSouthboundService lispSouthboundService;
    private LispXtrSouthboundService lispXtrSouthboundService;
    private volatile DatagramSocket socket = null;
    private final String MAP_NOTIFY = "MapNotify";
    private final String MAP_REPlY = "MapReply";
    private final String MAP_REQUEST = "MapRequest";
    private volatile String bindingAddress = null;
    private volatile boolean alreadyInit = false;
    private volatile int xtrPort = LispMessage.XTR_PORT_NUM;
    private volatile boolean listenOnXtrPort = false;

    private DatagramSocket xtrSocket;

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
        bundleContext.registerService(IConfigLispSouthboundPlugin.class.getName(), this, null);
    }

    protected void stopImpl(BundleContext context) {
        unloadActions();
    }

    private void unloadActions() {
        if (lispThread != null) {
            lispThread.stopRunning();
        }
        lispSouthboundService = null;
        lispXtrSouthboundService = null;
        lispThread = null;
        xtrThread = null;
        LOG.info("LISP (RFC6830) Mapping Service is down!");
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
        }
    }

    public void destroy() {
        unloadActions();
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

            LOG.info("LISP (RFC6830) Mapping Service is running and listening on address: " + bindingAddress + " port: "
                    + threadSocket.getLocalPort());
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
                LOG.trace(String.format("Handling packet from {%s}:{%d} (len={%d})", packet.getAddress().getHostAddress(), packet.getPort(),
                        packet.getLength()));

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
        try {
            socket = new DatagramSocket(new InetSocketAddress(bindingAddress, LispMessage.PORT_NUM));
            lispThread = new LispIoThread(socket, lispSouthboundService);
            lispThread.start();
            LOG.info("LISP (RFC6830) Mapping Service Southbound Plugin is up!");
            if (listenOnXtrPort) {
                restartXtrThread();
            }
        } catch (SocketException e) {
            LOG.error("couldn't start socket {}", e.getMessage());
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
            LOG.warn("failed to start xtr thread: {}", e.getMessage());
        }
    }

    public void onSessionInitiated(ProviderContext session) {
        LOG.info("LISP (RFC6830) Mapping Service is up!");
        synchronized (startLock) {
            if (!alreadyInit) {
                alreadyInit = true;
                lispSouthboundService = new LispSouthboundService();
                lispXtrSouthboundService = new LispXtrSouthboundService();
                registerWithOSGIConsole();
                registerRPCs(session);
                LOG.trace("Provider Session initialized");
                if (bindingAddress == null) {
                    setLispAddress("0.0.0.0");
                }
            }

        }
    }

    private void registerRPCs(ProviderContext session) {
        try {
            lispSouthboundService.setNotificationProvider(session.getSALService(NotificationProviderService.class));
            lispXtrSouthboundService.setNotificationProvider(session.getSALService(NotificationProviderService.class));
            session.addRpcImplementation(LfmControlPlaneService.class, this);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void handleSerializedLispBuffer(TransportAddress address, ByteBuffer outBuffer, String packetType) {
        DatagramPacket packet = new DatagramPacket(outBuffer.array(), outBuffer.limit());
        packet.setPort(address.getPort().getValue());
        InetAddress ip = InetAddresses.forString(address.getIpAddress().getIpv4Address().getValue());
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
    public Future<RpcResult<Void>> sendMapNotify(SendMapNotifyInput mapNotifyInput) {
        LOG.trace("sendMapNotify called!!");
        if (mapNotifyInput != null) {
            ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotifyInput.getMapNotify());
            handleSerializedLispBuffer(mapNotifyInput.getTransportAddress(), outBuffer, MAP_NOTIFY);
        } else {
            LOG.warn("MapNotify was null");
            return Futures.immediateFuture(RpcResultBuilder.<Void> failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
    }

    @Override
    public Future<RpcResult<Void>> sendMapReply(SendMapReplyInput mapReplyInput) {
        LOG.trace("sendMapReply called!!");
        if (mapReplyInput != null) {
            ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReplyInput.getMapReply());
            handleSerializedLispBuffer(mapReplyInput.getTransportAddress(), outBuffer, MAP_REPlY);
        } else {
            LOG.warn("MapReply was null");
            return Futures.immediateFuture(RpcResultBuilder.<Void> failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
    }

    @Override
    public Future<RpcResult<Void>> sendMapRequest(SendMapRequestInput mapRequestInput) {
        LOG.trace("sendMapRequest called!!");
        if (mapRequestInput != null) {
            ByteBuffer outBuffer = MapRequestSerializer.getInstance().serialize(mapRequestInput.getMapRequest());
            handleSerializedLispBuffer(mapRequestInput.getTransportAddress(), outBuffer, MAP_REQUEST);
        } else {
            LOG.debug("MapRequest was null");
            return Futures.immediateFuture(RpcResultBuilder.<Void> failed().build());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
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
}
