package org.opendaylight.lispflowmapping.implementation.authentication;

import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;

public class LispAuthenticationUtil {

    public static boolean validate(MapRegister mapRegister, String key) {
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapRegister.getKeyId()));
        return authentication.validate(mapRegister, key);
    }

    public static byte[] createAuthenticationData(MapNotify mapNotify, String key) {
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapNotify.getKeyId()));
        return authentication.getAuthenticationData(mapNotify, key);
    }

}
