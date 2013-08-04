package org.opendaylight.lispflowmapping.implementation.authentication;


public class LispNoAuthentication implements ILispAuthentication {

	private static final LispNoAuthentication INSTANCE = new LispNoAuthentication();

	public static LispNoAuthentication getInstance() {
		return INSTANCE;
	}

	private LispNoAuthentication() {
	}
	
	public byte[] getAuthenticationData(byte[] data) {
		return new byte[0];
	}

	public int getAuthenticationLength() {
		return 0;
	}

}
