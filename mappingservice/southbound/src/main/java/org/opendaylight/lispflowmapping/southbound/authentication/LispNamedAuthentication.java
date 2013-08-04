package org.opendaylight.lispflowmapping.southbound.authentication;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class LispNamedAuthentication implements ILispAuthentication {
	
	public LispNamedAuthentication(String algorithem) {
		this.algorithem = algorithem;
		try {
			byte[] keyBytes = KEY.getBytes();           
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithem);

            mac = Mac.getInstance(algorithem);
            mac.init(signingKey);
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeyException e) {
		}
	}
	
	private static String KEY="password";
	protected String algorithem;
	protected Mac mac;

	public byte[] getAuthenticationData(byte[] data) {
		return mac.doFinal(data);
	}

	public int getAuthenticationLength() {
		return mac.getMacLength();
	}

	public String getAlgorithem() {
		return algorithem;
	}

	public void setAlgorithem(String algorithem) {
		this.algorithem = algorithem;
	}
	
	

}
