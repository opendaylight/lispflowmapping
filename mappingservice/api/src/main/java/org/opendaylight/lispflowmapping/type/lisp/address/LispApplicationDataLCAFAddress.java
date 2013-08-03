package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispApplicationDataLCAFAddress extends LispLCAFAddress {

    private byte protocol;
    private int IPTos;
    private short localPort;
    private short remotePort;
    private LispAddress address;

    public LispApplicationDataLCAFAddress(byte res2) {
        super(LispCanonicalAddressFormatEnum.APPLICATION_DATA, res2);

    }

    public LispApplicationDataLCAFAddress(byte res2, byte protocol, int iPTos, short localPort, short remotePort, LispAddress address) {
        super(LispCanonicalAddressFormatEnum.APPLICATION_DATA, res2);
        this.protocol = protocol;
        this.IPTos = iPTos;
        this.localPort = localPort;
        this.remotePort = remotePort;
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

    public short getLocalPort() {
        return localPort;
    }

    public void setLocalPort(short localPort) {
        this.localPort = localPort;
    }

    public short getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(short remotePort) {
        this.remotePort = remotePort;
    }

    public LispAddress getAddress() {
        return address;
    }

    public void setAddress(LispAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "LispApplicationDataLCAFAddress [protocol=" + protocol + ", IPTos=" + IPTos + ", localPort=" + localPort + ", remotePort="
                + remotePort + ", address=" + address + "]";
    }

}
