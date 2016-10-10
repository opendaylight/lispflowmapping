/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mappingservice;

import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

/**
 * Mapping Service Java API.
 *
 * @author Florin Coras
 *
 */

public interface IMappingService {

    enum LookupPolicy { NB_FIRST, NB_AND_SB }

    /**
     * Add mapping.
     *
     * @param origin
     *            Table where mapping should be added
     * @param key
     *            Key of the mapping
     * @param siteId
     *            Site that stores the mapping
     * @param mapping
     *            Mapping to be stored
     */
    void addMapping(MappingOrigin origin, Eid key, SiteId siteId, MappingData mapping);

    /**
     * Retrieves mapping with given origin for the provided key. The lookup policy for the key is defined in the Mapping
     * System.
     *
     * @param origin
     *            Table where the mapping should be looked up.
     * @param key
     *            Key to be looked up
     * @return Returns the mapping found in the Mapping System or null if nothing is found.
     */
    MappingData getMapping(MappingOrigin origin, Eid key);

    /**
     * Retrieves mapping for given key.The lookup policy for the key is defined in the Mapping
     * System.
     *
     * @param key
     *            Key to be looked up
     * @return Returns the mapping found in the Mapping System or null if nothing is found.
     */
    MappingData getMapping(Eid key);

    /**
     * Retrieves mapping with a Source/Dest policy. This method is meant to avoid the overhead of building
     * LcafSourceDest addresses.
     *
     * @param srcKey
     *            Source key being looked up
     * @param dstKey
     *            Destination key being looked up
     * @return Returns the mapping found in the Mapping System or null if nothing is found.
     */
    MappingData getMapping(Eid srcKey, Eid dstKey);

    /**
     * Retrieves widest negative prefix found in the Mapping System for given key.
     *
     * @param key
     *            Key being looked up
     * @return Returns the widest negative prefix or null if nothing is found.
     */
    Eid getWidestNegativePrefix(Eid key);

    /**
     * Refresh southbound mapping registration timestamp.
     *
     * @param key
     *            The EID whose registration must be refreshed
     * @param xtrId
     *            xTR-ID of the mapping to be refreshed
     * @param timestamp
     *            New timestamp for the mapping
     */
    void refreshMappingRegistration(Eid key, XtrId xtrId, Long timestamp);

    /**
     * Remove mapping.
     *
     * @param origin
     *            Table from where the mapping should be removed
     * @param key
     *            Key to be removed
     */
    void removeMapping(MappingOrigin origin, Eid key);

    /**
     * Add authentication key.
     *
     * @param key
     *            The key for which the authentication key is added
     * @param authKey
     *            The authentication key
     */
    void addAuthenticationKey(Eid key, MappingAuthkey authKey);

    /**
     * Retrieve authentication key.
     *
     * @param key
     *            The key for which the authentication key is being looked up.
     * @return The authentication key.
     */
    MappingAuthkey getAuthenticationKey(Eid key);

    /**
     * Remove authentication key.
     *
     * @param key
     *            Key for which the authentication key should be removed.
     */
    void removeAuthenticationKey(Eid key);

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
    void addData(MappingOrigin origin, Eid key, String subKey, Object data);

    /**
     * Generic retrieval of data.
     *
     * @param origin
     *            Table from where the data should be read
     * @param key
     *            The key where the data is stored
     * @param subKey
     *            The subKey where data is stored
     * @return The data
     */
    Object getData(MappingOrigin origin, Eid key, String subKey);

    /**
     * Generic removal of data.
     *
     * @param origin
     *            The table from where the data should be removed
     * @param key
     *            The key of the data to be removed
     * @param subKey
     *            The subKey of the data to be removed
     */
    void removeData(MappingOrigin origin, Eid key, String subKey);

    /**
     * Configures Mapping Service mapping overwrite option. If set to true, mappings with the same key are overwritten,
     * otherwise, mappings with the same key but from different xTR-IDs are all stored.
     *
     * @param overwrite
     *            enables or disables mapping overwrite
     */
    void setMappingOverwrite(boolean overwrite);

    /**
     * Configures Mapping Service mapping lookup policy option.
     *
     * @param policy
     *            the policy to be activated
     */
    void setLookupPolicy(LookupPolicy policy);

    /**
     * Print all mappings. Used for testing, debugging and the karaf shell.
     *
     * @return String consisting of all mappings
     */
    String printMappings();

    /**
     * Cleans all cached mappings.Used for testing.
     */
    void cleanCachedMappings();

    /**
     * Set cluster master status.
     *
     * @param isMaster
     *            is|isn't master
     */
    void setIsMaster(final boolean isMaster);

    /**
     * Get cluster master status.
     *
     * @return isMaster
     *            is|isn't master
     */
    boolean isMaster();
}
