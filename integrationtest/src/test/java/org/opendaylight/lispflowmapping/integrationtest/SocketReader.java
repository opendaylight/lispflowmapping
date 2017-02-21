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
import java.net.SocketException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

/**
 * Read data from specified socket in standalone thread. Packets are stored to array of buffer. In other words each
 * packet is stored in standalone buffer. So whenever during existence of instance of this class it is possible to
 * access red packets.
 */
public class SocketReader implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SocketReader.class);
    /**
     * max number of packets which can be stored to this to buffer
     */
    private static final int MAX_NUMBER_OF_PACKETS_TO_STORE = 100;
    private final DatagramSocket socket;

    /**
     * array of buffers where SMR messages are stored
     */
    private byte[][] buffers = new byte[MAX_NUMBER_OF_PACKETS_TO_STORE][4096];

    /**
     * Index to array of buffers where current writting is done
     */
    private int currentBufferWriteIndex = 0;

    /**
     * Index to array of buffers from where current reading is done
     */
    private int currentBufferReaderIndex = 0;
    private volatile boolean readFromSocket = true;

    private SocketReader(DatagramSocket receivedSocket) {
        this.socket = receivedSocket;
    }

    private DatagramPacket receivePacket;

    static SocketReader startReadingInStandaloneThread(final DatagramSocket socket) {
        return SocketReader.startReadingInStandaloneThread(socket, 0);
    }

    static SocketReader startReadingInStandaloneThread(final DatagramSocket socket, int timeout) {
        try {
            socket.setSoTimeout(timeout);
            final SocketReader socketReader = new SocketReader(socket);
            final Thread thread = new Thread(socketReader);
            thread.setName("Socket reader - multisite integration test - lispflowmapping");
            thread.start();
            return socketReader;
        } catch (SocketException t) {
            fail("Socket timed out after " + timeout + " miliseconds.");
            return null;
        }
    }

    @Override
    public void run() {
        while (readFromSocket && currentBufferReaderIndex < MAX_NUMBER_OF_PACKETS_TO_STORE) {
            receivePacket = new DatagramPacket(buffers[currentBufferWriteIndex], buffers[currentBufferWriteIndex].
                    length);
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                LOG.debug("Problem while reading SMR test socket.", e);
            }
            currentBufferWriteIndex++;
        }
    }

    void stopReading() {
        readFromSocket = false;
        socket.close();
    }

    /**
     * Read from buffers {@code count} number of buffers from current postion.
     *
     * @param count how many buffer should be returned.
     * @return array of buffers
     */
    byte[][] getBuffers(final int count) {
        if (currentBufferReaderIndex >= currentBufferWriteIndex) {
            LOG.warn("Reading past current cursor, no new packets received on socket since last read!");
        }
        final byte[][] subBuffer = Arrays.copyOfRange(buffers, currentBufferReaderIndex, currentBufferReaderIndex +
                count);
        currentBufferReaderIndex = currentBufferReaderIndex + count;
        return subBuffer;
    }
}
