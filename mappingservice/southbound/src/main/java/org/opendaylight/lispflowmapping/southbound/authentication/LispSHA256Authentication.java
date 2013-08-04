package org.opendaylight.lispflowmapping.southbound.authentication;


public class LispSHA256Authentication extends LispNamedAuthentication {

	private static final LispSHA256Authentication INSTANCE = new LispSHA256Authentication();

	public static LispSHA256Authentication getInstance() {
		return INSTANCE;
	}
	
	public LispSHA256Authentication() {
		super("HmacSHA256");
	}


}
