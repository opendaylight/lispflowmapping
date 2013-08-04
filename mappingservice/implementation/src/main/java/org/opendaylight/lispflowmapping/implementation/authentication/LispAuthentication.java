package org.opendaylight.lispflowmapping.implementation.authentication;

import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;

public class LispAuthentication implements ILispAuthentication {
    
    private static final LispAuthentication INSTANCE = new LispAuthentication();
    protected static final int MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION = 16;

    public static LispAuthentication getInstance() {
        return INSTANCE;
    }


    public boolean validate(MapRegister mapRegister) {
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapRegister.getKeyId()));
        return authentication.validate(mapRegister);
    }

    public byte[] getAuthenticationData(MapNotify mapNotify) {
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapNotify.getKeyId()));
        return authentication.getAuthenticationData(mapNotify);
    }

    public int getAuthenticationLength() {
        return 0;
    }

	

}
