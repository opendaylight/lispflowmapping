/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;

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
     * @return Should the map server overwrite Rlocs.
     */
    boolean shouldOverwriteRlocs();

    /**
     * This method returns the authentication key of the address.
     * 
     * @param address
     * @param maskLen
     * @return The correct key.
     */
    String getAuthenticationKey(LispAddressContainer address, int maskLen);

    void setShouldIterateMask(boolean shouldIterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);
    
    void setShouldOverwriteRlocs(boolean shouldOverwriteRlocs);

    /**
     * This method removes the given authentication key from the map server.
     * 
     * @param address
     * @param maskLen
     * @return
     */
    boolean removeAuthenticationKey(LispAddressContainer address, int maskLen);

    /**
     * This method adds an authentication key to the address.
     * 
     * @param address
     * @param maskLen
     * @param key
     * @return
     */
    boolean addAuthenticationKey(LispAddressContainer address, int maskLen, String key);
}
