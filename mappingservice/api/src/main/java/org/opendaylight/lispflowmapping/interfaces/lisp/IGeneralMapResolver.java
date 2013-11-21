package org.opendaylight.lispflowmapping.interfaces.lisp;

/**
 * The general methods of the map resolver
 */
public interface IGeneralMapResolver {
    /**
     * @return Should the map resolver use masking.
     */
    boolean shouldIterateMask();

    /**
     * @return Should the map reslover use authentication
     */
    boolean shouldAuthenticate();

    void setShouldIterateMask(boolean iterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);
}
