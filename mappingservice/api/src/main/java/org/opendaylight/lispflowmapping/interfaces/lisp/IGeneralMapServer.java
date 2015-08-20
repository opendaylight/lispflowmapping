/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;

/**
 * The general methods of the map server
 */
public interface IGeneralMapServer {

    /**
     * @return Should the map server use authentication.
     */
    boolean shouldAuthenticate();

    /**
     * @return Should the map server use masking.
     */
    boolean shouldIterateMask();

    /**
     * @return Should the map server use overwrite.
     */
    boolean shouldOverwrite();

    /**
     * This method returns the authentication key of the address.
     *
     * @param address EID to look up
     * @param maskLen EID mask length
     * @return The correct key.
     */
    String getAuthenticationKey(LispAddressContainer address, int maskLen);

    void setShouldIterateMask(boolean shouldIterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);

    void setOverwrite(boolean overwrite);

    /**
     * This method removes the given authentication key from the map server.
     *
     * @param address EID to remove
     * @param maskLen EID mask length
     */
    void removeAuthenticationKey(LispAddressContainer address, int maskLen);

    /**
     * This method adds an authentication key to the address.
     *
     * @param address EID to add
     * @param maskLen EID mask length
     * @param key Value of the key for the EID
     */
    void addAuthenticationKey(LispAddressContainer address, int maskLen, String key);
}
