package org.opendaylight.lispflowmapping.southbound.authentication;




public class LispSHA1Authentication extends LispNamedAuthentication {
	
	private static final LispSHA1Authentication INSTANCE = new LispSHA1Authentication();

	public static LispSHA1Authentication getInstance() {
		return INSTANCE;
	}

	private LispSHA1Authentication() {
		super("HmacSHA1");
	}


}
