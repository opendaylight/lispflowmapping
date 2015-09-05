/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

/**
 * Mapping System interface
 *
 * @author Florin Coras
 *
 */

public interface IMappingSystem {
    /**
     * Add mapping
     *
     * @param origin
     *            Table where mapping should be added
     * @param key
     *            Key of the mapping
     * @param data
     *            Value to be stored
     */
    void addMapping(MappingOrigin origin, LispAddressContainer key, Object data);

    /**
     * Retrieves mapping for the provided src and dst key.
     *
     * @param src
     *            Source Key to be looked up
     * @param dst
     *            Destination Key to be looked up
     * @return Returns the object found in the MappingSystem or null if nothing is found.
     */
    Object getMapping(LispAddressContainer src, LispAddressContainer dst);

    /**
     * Retrieves mapping for the provided dst key.
     *
     * @param dst
     *            Destination Key to be looked up
     * @return Returns the object found in the Mapping System or null if nothing is found.
     */
    Object getMapping(LispAddressContainer dst);

    /**
     * Retrieves mapping from table for provided key
     *
     * @param origin
     *            Table where mapping should be looked up
     * @param key
     *            Key to be looked up
     * @return Returns the object found in the cache or null if nothing is found.
     */
    Object getMapping(MappingOrigin origin, LispAddressContainer key);

    /**
     * Update key registration
     *
     * @param origin
     *            Table for mapping that should be updated
     * @param key
     *            The key whose registration must be updated
     */
    void updateMappingRegistration(MappingOrigin origin, LispAddressContainer key);

    /**
     * Remove mapping
     *
     * @param origin
     *            Table for mapping that should be removed
     * @param key
     *            Key to be removed
     * @param overwrite
     *            Set overwrite on or off
     *
     */
    void removeMapping(MappingOrigin origin, LispAddressContainer key);

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
     * Add data for key
     *
     * @param origin
     *            Table for data that should be added
     * @param key
     *            The key for which data is inserted
     * @param subKey
     *            The subKey where data should be inserted
     * @param data
     *            The data to be stored
     */
    void addData(MappingOrigin origin, LispAddressContainer key, String subKey, Object data);

    /**
     * Generic retrieval of data
     *
     * @param origin
     *            Table from where data should be retrieved
     * @param key
     *            The key where the data is stored
     * @param subKey
     *            The subKey where data is stored
     * @return The data
     */
    Object getData(MappingOrigin origin, LispAddressContainer key, String subKey);


    /**
     * Generic removal of data
     *
     * @param origin
     *            Table from where data should be removed
     * @param key
     *            The key of the data to be removed
     * @param subKey
     *            The subKey of the data to be removed
     */
    void removeData(MappingOrigin origin, LispAddressContainer key, String subKey);

    /**
     * Sets iterateMask. If set to true, longest prefix matching for IP keys is used.
     *
     * @param iterate
     *            Value to configure
     *
     */
    void setIterateMask(boolean iterate);

    /**
     * Configure overwrite policy. If set to true, mappings are overwritten.
     *
     * @param overwrite
     *            Value to configure
     */
    public void setOverwritePolicy(boolean overwrite);

    /**
     * Print all mappings. Used for testing and debuggin purposes.
     *
     * @return String consisting of all mappings
     */
    String printMappings();
}
