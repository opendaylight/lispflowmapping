package org.opendaylight.lispflowmapping.implementation.authentication;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;

public interface ILispAuthentication {
    public boolean validate(MapRegister mapRegister, String key);

    public byte[] getAuthenticationData(MapNotify mapNotify, String key);

    public int getAuthenticationLength();

    public static final int MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION = 16;

}