package org.opendaylight.lispflowmapping.implementation.authentication;

import java.util.HashMap;
import java.util.Map;

public class LispAuthenticationFactory {
	
	private static Map<LispKeyIDEnum, ILispAuthentication> keyIDToAuthenticationMap;
	
	private static void initializeMap() {
		keyIDToAuthenticationMap = new HashMap<LispKeyIDEnum, ILispAuthentication>();
		keyIDToAuthenticationMap.put(LispKeyIDEnum.NONE, LispNoAuthentication.getInstance());
		keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA1, new LispAuthentication(LispKeyIDEnum.SHA1.getAuthenticationName()));
		keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA256, new LispAuthentication(LispKeyIDEnum.SHA256.getAuthenticationName()));
		
	}
	
	public static ILispAuthentication getAuthentication(LispKeyIDEnum keyID) {
		if (keyIDToAuthenticationMap == null) {
			initializeMap();
		}
		return keyIDToAuthenticationMap.get(keyID);
	}
	

}
