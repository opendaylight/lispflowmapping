package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public interface IGeneralMapServer {
    boolean shouldAuthenticate();

    boolean shouldIterateMask();

    void setShouldIterateMask(boolean shouldIterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);

    String getAuthenticationKey(LispAddress address, int maskLen);

    boolean removeAuthenticationKey(LispAddress address, int maskLen);

    boolean addAuthenticationKey(LispAddress address, int maskLen, String key);
}
