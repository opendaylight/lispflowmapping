package org.opendaylight.lispflowmapping.implementation.authentication;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;

public class LispNoAuthentication implements ILispAuthentication {

    private static final LispNoAuthentication INSTANCE = new LispNoAuthentication();

    private static byte[] authenticationData;

    public static LispNoAuthentication getInstance() {
        return INSTANCE;
    }

    private LispNoAuthentication() {
        authenticationData = new byte[0];
    }

    public int getAuthenticationLength() {
        return 0;
    }

    public byte[] getAuthenticationData(MapNotify mapNotify, String key) {
        return authenticationData;
    }

    public boolean validate(MapRegister mapRegister, String key) {
        return true;
    }

}
