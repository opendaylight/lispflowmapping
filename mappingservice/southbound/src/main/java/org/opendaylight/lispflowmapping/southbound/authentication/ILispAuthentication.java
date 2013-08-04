package org.opendaylight.lispflowmapping.southbound.authentication;


public interface ILispAuthentication {
	public byte[] getAuthenticationData(byte[] data);
	public int getAuthenticationLength();
}
