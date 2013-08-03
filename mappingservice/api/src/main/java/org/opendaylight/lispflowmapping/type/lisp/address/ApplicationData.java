package org.opendaylight.lispflowmapping.type.lisp.address;

public class ApplicationData {
	
	public ApplicationData() {
		this.IPTos = new byte[3];
	}
	
	public ApplicationData(byte protocol, byte[] iPTos, short localPort,
			short remotePort, LispAddress address) {
		super();
		this.protocol = protocol;
		IPTos = iPTos;
		this.localPort = localPort;
		this.remotePort = remotePort;
		this.address = address;
	}
	
	private byte protocol;
	private byte[] IPTos;
	private short localPort;
	private short remotePort;
	private LispAddress address;


	public byte getProtocol() {
		return protocol;
	}
	public void setProtocol(byte protocol) {
		this.protocol = protocol;
	}
	public byte[] getIPTos() {
		return IPTos;
	}
	public void setIPTos(byte[] iPTos) {
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
        return "LispApplicationDataLCAFAddress#[" + address + "]" + super.toString();
    }



	
}
