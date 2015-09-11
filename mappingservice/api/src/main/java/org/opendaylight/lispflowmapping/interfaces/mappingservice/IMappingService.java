/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mappingservice;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.SiteId;

/**
 *
 * Mapping Service Java API
 *
 * @author Florin Coras
 *
 */

public interface IMappingService {
    /**
     * Add mapping
     *
     * @param origin
     *            Table where mapping should be added
     * @param key
     *            Key of the mapping
     * @param siteId
     *            Site that stores the mapping
     * @param data
     *            Value to be stored
     */
    void addMapping(MappingOrigin origin, LispAddressContainer key, SiteId siteId, Object data);

    /**
     * Retrieves mapping with given origin for the provided key. The lookup policy for the key is defined in the Mapping
     * System
     *
     * @param origin
     *            Table where the mapping should be looked up.
     * @param key
     *            Key to be looked up
     * @return Returns the object found in the Mapping System or null if nothing is found.
     */
    Object getMapping(MappingOrigin origin, LispAddressContainer key);

    /**
     * Retrieves mapping for given key.The lookup policy for the key is defined in the Mapping
     * System
     *
     * @param key
     *            Key to be looked up
     * @return Returns the object found in the Mapping System or null if nothing is found.
     */
    Object getMapping(LispAddressContainer key);

    /**
     * Retrieves mapping with a Source/Dest policy. This method is meant to avoid the overhead of building
     * LcafSourceDest addresses.
     *
     * @param srcKey
     *            Source key being looked up
     * @param dstKey
     *            Destination key being looked up
     * @return Returns the object found in the Mapping System or null if nothing is found.
     */
    Object getMapping(LispAddressContainer srcKey, LispAddressContainer dstKey);

    /**
     * Remove mapping
     *
     * @param origin
     *            Table from where the mapping should be removed
     * @param key
     *            Key to be removed
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
     * Generic addition of data. Not stored in MD-SAL datastore!
     *
     * @param origin
     *            Table where data should be inserted
     * @param key
     *            The key where data should be inserted
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
     *            Table from where the data should be read
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
     *            The table from where the data should be removed
     * @param key
     *            The key of the data to be removed
     * @param subKey
     *            The subKey of the data to be removed
     */
    void removeData(MappingOrigin origin, LispAddressContainer key, String subKey);

    /**
     * Configures Mapping Service mapping overwrite option. If set to true, mappings with the same key are overwritten,
     * otherwise, mappings are merged
     *
     * @param overwrite
     *            enables or disables mapping overwite
     */
    void setMappingOverwrite(boolean overwrite);

    /**
     * Cleans all cached mappings.Used for testing.
     */
    void cleanCachedMappings();
}
