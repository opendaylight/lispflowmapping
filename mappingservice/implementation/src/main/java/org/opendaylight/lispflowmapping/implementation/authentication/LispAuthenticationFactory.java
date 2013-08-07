package org.opendaylight.lispflowmapping.implementation.authentication;

import java.util.HashMap;
import java.util.Map;

public class LispAuthenticationFactory {
	
	private static Map<LispKeyIDEnum, ILispAuthentication> keyIDToAuthenticationMap;
	
	private static void initializeMap() {
		keyIDToAuthenticationMap = new HashMap<LispKeyIDEnum, ILispAuthentication>();
		keyIDToAuthenticationMap.put(LispKeyIDEnum.NONE, LispNoAuthentication.getInstance());
		keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA1, new LispMACAuthentication(LispKeyIDEnum.SHA1.getAuthenticationName()));
		keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA256, new LispMACAuthentication(LispKeyIDEnum.SHA256.getAuthenticationName()));
		keyIDToAuthenticationMap.put(LispKeyIDEnum.UNKNOWN, LispNoAuthentication.getInstance());
		
	}
	
	public static ILispAuthentication getAuthentication(LispKeyIDEnum keyID) {
		if (keyIDToAuthenticationMap == null) {
			initializeMap();
		}
		return keyIDToAuthenticationMap.get(keyID);
	}
	

}
