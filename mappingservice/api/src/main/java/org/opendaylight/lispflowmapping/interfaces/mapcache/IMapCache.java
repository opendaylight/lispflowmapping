/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;

/**
 * Map-cache interface
 *
 * @author Florin Coras
 *
 */

public interface IMapCache {
    /**
     * Add mapping
     *
     * @param key
     *            Key of the mapping
     * @param data
     *            Value to be stored
     */
    void addMapping(LispAddressContainer key, Object data, boolean shouldOverwrite);

    /**
     * Retrieves mapping for the provided srcKey and dstKey.
     *
     * @param srcKey
     *            Source Key to be looked up
     * @param dstKey
     *            Destination Key to be looked up
     * @return Returns the object found in the cache or null if nothing is found.
     */
    Object getMapping(LispAddressContainer srcKey, LispAddressContainer dstKey);

    /**
     * Remove mapping
     *
     * @param key
     *            Key to be removed
     * @param overwrite
     *            Set overwrite on or off
     *
     */
    void removeMapping(LispAddressContainer eid, boolean overwrite);

    /**
     * Add authentication key
     *
     * @param key
     *            The key for which the authentication key is added
     * @param authKey
     *            The authentication key
     */
    void addAuthenticationKey(LispAddressContainer key, String authKey);

    /**
     * Retrieve authentication key
     *
     * @param key
     *            The key for which the authentication key is being looked up.
     * @return The authentication key.
     */
    String getAuthenticationKey(LispAddressContainer key);

    /**
     * Remove authentication key
     *
     * @param key
     *            Key for which the authentication key should be removed.
     */
    void removeAuthenticationKey(LispAddressContainer key);

    /**
     * Update key registration
     *
     * @param key
     *            The key whose registration must be updated
     */
    void updateMappingRegistration(LispAddressContainer key);

    /**
     * Add data for key
     *
     * @param key
     *            The key for which data is inserted
     * @param subKey
     *            The subKey where data should be inserted
     * @param data
     *            The data to be stored
     */
    void addData(LispAddressContainer key, String subKey, Object data);

    /**
     * Generic retrieval of data
     *
     * @param key
     *            The key where the data is stored
     * @param subKey
     *            The subKey where data is stored
     * @return The data
     */
    Object getData(LispAddressContainer key, String subKey);

    /**
     * Generic removal of data
     *
     * @param key
     *            The key of the data to be removed
     * @param subKey
     *            The subKey of the data to be removed
     */
    void removeData(LispAddressContainer key, String subKey);

    /**
     * Print mappings in cache. Should be used for testing and debuggin only.
     *
     * @return a String consisting of all the mappings in the cache
     */
    String printMappings();
}
