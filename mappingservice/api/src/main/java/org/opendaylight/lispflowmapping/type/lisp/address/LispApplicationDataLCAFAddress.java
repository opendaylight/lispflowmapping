/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispApplicationDataLCAFAddress extends LispLCAFAddress {

    private byte protocol;
    private int IPTos;
    private short localPortLow;
    private short localPortHigh;
    private short remotePortLow;
    private short remotePortHigh;
    private LispAddress address;

    public LispApplicationDataLCAFAddress(byte res2) {
        super(LispCanonicalAddressFormatEnum.APPLICATION_DATA, res2);

    }

    public LispApplicationDataLCAFAddress(byte res2, byte protocol, int iPTos, short localPortLow, short localPortHigh,
            short remotePortLow, short remotePortHigh, LispAddress address) {
        super(LispCanonicalAddressFormatEnum.APPLICATION_DATA, res2);
        this.protocol = protocol;
        this.IPTos = iPTos;
        this.localPortLow = localPortLow;
        this.localPortHigh = localPortHigh;
        this.remotePortLow = remotePortLow;
        this.remotePortHigh = remotePortHigh;
        this.address = address;
    }

    public byte getProtocol() {
        return protocol;
    }

    public void setProtocol(byte protocol) {
        this.protocol = protocol;
    }

    public int getIPTos() {
        return IPTos;
    }

    public void setIPTos(int iPTos) {
        IPTos = iPTos;
    }

    public short getLocalPortLow() {
        return localPortLow;
    }

    public void setLocalPortLow(short localPortLow) {
        this.localPortLow = localPortLow;
    }

    public short getLocalPortHigh() {
        return localPortHigh;
    }

    public void setLocalPortHigh(short localPortHigh) {
        this.localPortHigh = localPortHigh;
    }

    public short getRemotePortLow() {
        return remotePortLow;
    }

    public void setRemotePortLow(short remotePortLow) {
        this.remotePortLow = remotePortLow;
    }

    public short getRemotePortHigh() {
        return remotePortHigh;
    }

    public void setRemotePortHigh(short remotePortHigh) {
        this.remotePortHigh = remotePortHigh;
    }

    public LispAddress getAddress() {
        return address;
    }

    public void setAddress(LispAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "LispApplicationDataLCAFAddress [protocol=" + protocol + ", IPTos=" + IPTos
                + ", localPortLow=" + localPortLow + ", localPortHigh=" + localPortHigh
                + ", remotePortLow=" + remotePortLow + ", remotePortHigh=" + remotePortHigh
                + ", address=" + address + "]";
    }

}
