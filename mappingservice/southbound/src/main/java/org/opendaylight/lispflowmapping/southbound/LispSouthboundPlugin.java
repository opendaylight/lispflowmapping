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
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.Future;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.southbound.lisp.ILispSouthboundService;
import org.opendaylight.lispflowmapping.southbound.lisp.LispSouthboundService;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispPlugin;
import org.opendaylight.lispflowmapping.type.sbplugin.ILispSouthboundPlugin;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispSouthboundPlugin extends AbstractBindingAwareProvider implements ILispSouthboundPlugin, IConfigLispPlugin, CommandProvider {
    protected static final Logger logger = LoggerFactory.getLogger(LispSouthboundPlugin.class);

    private static Object startLock = new Object();
    private LispIoThread thread;
    private LispSouthboundIO threadIO;
    private LispSouthboundService lispSouthboundService;
    private volatile DatagramSocket socket = null;
    private final String MAP_NOTIFY = "MapNotify";
    private final String MAP_REPlY = "MapReply";
    private volatile String bindingAddress = null;
    private volatile boolean stillRunning = false;
    private volatile boolean alreadyInit = false;
    private NetworkInterface netInt;

    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this, null);
        bundleContext.registerService(IConfigLispPlugin.class.getName(), this, null);
    }

    protected void stopImpl(BundleContext context) {
        unloadActions();
    }

    private void unloadActions() {
        // if (thread != null) {
        // thread.stopRunning();
        // }
        if (threadIO != null) {
            try {
                threadIO.shutDown();
            } catch (IOException e) {
            }
        }
        lispSouthboundService = null;
        thread = null;
        threadIO = null;
        logger.info("LISP (RFC6830) Mapping Service is down!");
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
        }
    }

    public void destroy() {
        unloadActions();
    }

    private class LispSouthboundIO extends Thread {
        private Short lispPort = LispMessage.PORT_NUM;;
        private InetAddress controllerIP;
        private NetworkInterface netInt;
        private SelectionKey serverSelectionKey;
        private ServerSocketChannel serverSocket;
        private LispSouthboundService lispSouthboundService;
        private Selector selector;
        private boolean running;
        private Thread southboundIOThread;
        private volatile boolean stillRunning = false;
        private volatile boolean alreadyInit = false;

        public LispSouthboundIO() {
            String addressString = "0.0.0.0";
            try {
                controllerIP = InetAddress.getByName(addressString);
            } catch (Exception e) {
                controllerIP = null;
                logger.warn("Invalid IP: {}, use wildcard *", addressString);
            }
        }

        public void run() {
            this.running = true;
            this.netInt = null;
            southboundIOThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    waitUntilInterfaceUp();
                    if (!startAcceptConnections()) {
                        return;
                    }
                    logger.info("Controller is now listening on {}:{}", (controllerIP == null) ? "any" : controllerIP.getHostAddress(), lispPort);
                    boolean netInterfaceUp = true;
                    while (running) {
                        try {
                            // wait for an incoming connection
                            // check interface state every 5sec
                            selector.select();
                            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                            netInterfaceUp = isNetInterfaceUp(netInterfaceUp);
                            while (selectedKeys.hasNext()) {
                                SelectionKey skey = selectedKeys.next();
                                selectedKeys.remove();
                                if (skey.isValid() && skey.isAcceptable()) {
                                    ServerSocketChannel ssc = (ServerSocketChannel) serverSelectionKey.channel();
                                    SocketChannel sc = null;
                                    sc = ssc.accept();
                                    Socket socket = sc.socket();
                                    byte[] buffer = new byte[4096];
                                    socket.getInputStream().read(buffer);
                                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                                    lispSouthboundService.handlePacket(packet);

                                    // (listener).handleNewConnection(selector,
                                    // serverSelectionKey);
                                }
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }
            }, "SouthboundI/O Thread");
            southboundIOThread.start();
        }

        private boolean startAcceptConnections() {
            if (running) {
                try {
                    // obtain a selector
                    selector = SelectorProvider.provider().openSelector();
                    // create the listening socket
                    serverSocket = ServerSocketChannel.open();
                    serverSocket.configureBlocking(false);
                    serverSocket.socket().bind(new java.net.InetSocketAddress(controllerIP, lispPort));
                    serverSocket.socket().setReuseAddress(true);
                    // register this socket for accepting incoming
                    // connections
                    serverSelectionKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
                } catch (IOException e) {
                    logger.error("Failed to listen on {}:{}, exit", (controllerIP == null) ? "" : controllerIP.getHostAddress(), lispPort);
                    return false;
                }
                return true;
            }
            return false;
        }

        private boolean isNetInterfaceUp(boolean currentlyUp) {
            if (controllerIP == null) {
                // for wildcard address, return since there is always an "up"
                // interface (such as loopback)
                return true;
            }
            boolean up;
            try {
                if (netInt == null) {
                    logger.warn("Can't find any operational interface for address {}", controllerIP.getHostAddress());
                    return false;
                }
                up = netInt.isUp();
                if (!up) {
                    // always generate log if the interface is down
                    logger.warn("Interface {} with address {} is DOWN!", netInt.getDisplayName(), controllerIP.getHostAddress());
                } else {
                    if (!currentlyUp) {
                        // only generate log if the interface changes from down
                        // to
                        // up
                        logger.info("Interface {} with address {} is UP!", netInt.getDisplayName(), controllerIP.getHostAddress());
                    }
                }
            } catch (SocketException e) {
                logger.warn("Interface {} with address {} is DOWN!", netInt.getDisplayName(), controllerIP.getHostAddress());
                up = false;
            }
            return up;
        }

        private void waitUntilInterfaceUp() {
            if (controllerIP == null) {
                // for wildcard address, return since there is always an "up"
                // interface (such as loopback)
                return;
            }
            boolean isUp = false;
            do {
                try {
                    // get the network interface from the address
                    netInt = NetworkInterface.getByInetAddress(controllerIP);
                    isUp = isNetInterfaceUp(isUp);
                    if (!isUp) {
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                }
            } while ((!isUp) && (running));
        }

        public void shutDown() throws IOException {
            this.running = false;
            this.selector.wakeup();
            this.serverSocket.close();
        }
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
                    lispSouthboundService.handlePacket(packet);
                } catch (Throwable t) {
                    logger.error("Error while handling packet", t);
                }
            }

            socket.close();
            logger.info("Socket closed");
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

    private void startSouthboundIOThread() {
        threadIO = new LispSouthboundIO();
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
                logger.debug("Provider Session initialized");
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

    public Future<RpcResult<Void>> handleMapNotify(MapNotify mapNotify, InetAddress address) {
        logger.trace("handleMapNotify called!!");
        if (mapNotify != null) {
            ByteBuffer outBuffer = MapNotifySerializer.getInstance().serialize(mapNotify);
            handleSerializedLispBuffer(address, outBuffer, MAP_NOTIFY);
        } else {
            logger.debug("MapNotify was null");
        }
        return null;
    }

    private void handleSerializedLispBuffer(InetAddress address, ByteBuffer outBuffer, String packetType) {
        DatagramPacket packet = new DatagramPacket(outBuffer.array(), outBuffer.limit());
        packet.setPort(LispMessage.PORT_NUM);
        packet.setAddress(address);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending " + packetType + " on port " + LispMessage.PORT_NUM + " to address: " + address);
            }
            socket.send(packet);
        } catch (IOException e) {
            logger.error("Failed to send " + packetType, e);
        }
    }

    public Future<RpcResult<Void>> handleMapReply(MapReply mapReply, InetAddress address) {
        logger.trace("handleMapReply called!!");
        if (mapReply != null) {
            ByteBuffer outBuffer = MapReplySerializer.getInstance().serialize(mapReply);
            handleSerializedLispBuffer(address, outBuffer, MAP_REPlY);
        } else {
            logger.debug("MapReply was null");
        }
        return null;
    }

    public void setLispAddress(String address) {
        synchronized (startLock) {
            if (bindingAddress != null && bindingAddress.equals(address)) {
                logger.debug("configured lisp binding address didn't change.");
            } else {
                String action = (bindingAddress == null ? "Setting" : "Resetting");
                logger.info(action + " lisp binding address to: " + address);
                bindingAddress = address;
                // if (thread != null) {
                // thread.stopRunning();
                // while (stillRunning) {
                // try {
                // Thread.sleep(500);
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
                // }
                // }
                // startIOThread();
                if (threadIO != null) {
                    try {
                        threadIO.shutDown();
                    } catch (IOException e1) {
                    }
                    while (stillRunning) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                startSouthboundIOThread();
            }
        }
    }
}
