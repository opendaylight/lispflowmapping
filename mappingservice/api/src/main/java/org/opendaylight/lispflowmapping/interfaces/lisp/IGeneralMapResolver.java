package org.opendaylight.lispflowmapping.interfaces.lisp;

public interface IGeneralMapResolver {
    boolean shouldIterateMask();

    boolean shouldAuthenticate();

    void setShouldIterateMask(boolean iterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);
}
