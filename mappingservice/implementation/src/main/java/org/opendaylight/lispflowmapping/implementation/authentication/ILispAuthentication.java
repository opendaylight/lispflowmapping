package org.opendaylight.lispflowmapping.implementation.authentication;


public interface ILispAuthentication {
	public byte[] getAuthenticationData(byte[] data);
	public int getAuthenticationLength();
}
