/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class SocketListener implements Runnable{

    DatagramPacket receivePacket;
    final private Thread thread;

    public SocketListener() {
        thread = new Thread(this);
        thread.setName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(new InetSocketAddress("127.0.0.2", 4342));
            socket.setSoTimeout(10000);
            socket.receive(receivePacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public byte[] provideData() {
        return receivePacket.getData();
    }

    public void waitForFinish() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() throws SocketException {
        byte[] buffer = new byte[4096];
        receivePacket = new DatagramPacket(buffer, buffer.length);
        thread.run();
    }
}
