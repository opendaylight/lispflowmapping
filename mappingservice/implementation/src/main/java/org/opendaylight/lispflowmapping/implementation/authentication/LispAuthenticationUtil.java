package org.opendaylight.lispflowmapping.implementation.authentication;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;

public class LispAuthenticationUtil {

    public static boolean validate(MapRegister mapRegister, String key) {
        short keyId = 0;
        if (mapRegister.getKeyId() != null) {
            keyId = mapRegister.getKeyId();
        }
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(keyId));
        return authentication.validate(mapRegister, key);
    }

    public static byte[] createAuthenticationData(MapNotify mapNotify, String key) {
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapNotify.getKeyId()));
        return authentication.getAuthenticationData(mapNotify, key);
    }

}
