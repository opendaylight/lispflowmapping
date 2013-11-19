package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;

public interface IGeneralMapServer {
    boolean shouldAuthenticate();

    boolean shouldIterateMask();

    void setShouldIterateMask(boolean shouldIterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);

    String getAuthenticationKey(LispAddressContainer address, int maskLen);

    boolean removeAuthenticationKey(LispAddressContainer address, int maskLen);

    boolean addAuthenticationKey(LispAddressContainer address, int maskLen, String key);
}
