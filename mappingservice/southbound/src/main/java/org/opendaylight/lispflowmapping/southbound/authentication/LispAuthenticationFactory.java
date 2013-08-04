package org.opendaylight.lispflowmapping.southbound.authentication;

import java.util.HashMap;
import java.util.Map;

public class LispAuthenticationFactory {
	
	private static Map<LispKeyIDEnum, ILispAuthentication> keyIDToAuthenticationMap;
	
	private static void initializeMap() {
		keyIDToAuthenticationMap = new HashMap<LispKeyIDEnum, ILispAuthentication>();
		keyIDToAuthenticationMap.put(LispKeyIDEnum.NONE, LispNoAuthentication.getInstance());
		keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA1, LispSHA1Authentication.getInstance());
		keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA256, LispSHA256Authentication.getInstance());
		
	}
	
	public static ILispAuthentication getAuthentication(LispKeyIDEnum keyID) {
		if (keyIDToAuthenticationMap == null) {
			initializeMap();
		}
		return keyIDToAuthenticationMap.get(keyID);
	}
	

}
