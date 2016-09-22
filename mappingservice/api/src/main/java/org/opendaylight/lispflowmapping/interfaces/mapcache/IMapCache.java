/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;

/**
 * Map-cache interface.
 *
 * @author Florin Coras
 *
 */

public interface IMapCache {
    /**
     * Add mapping.
     *
     * @param key
     *            Key of the mapping
     * @param data
     *            Value to be stored
     * @param shouldOverwrite
     *            Select if mappings with the same key are overwritten
     * @param merge
     *            Select if mappings with the same key are merged
     * @param dsbeObject
     *            A object reference to datastore backend
     */
    void addMapping(Eid key, Object data, boolean shouldOverwrite, boolean merge, Object dsbeObject);


    /**
     * Add mapping.
     *
     * @param key
     *            Key of the mapping
     * @param data
     *            Value to be stored
     * @param shouldOverwrite
     *            Select if mappings with the same key are overwritten
     * @param merge
     *            Select if mappings with the same key are merged
     */
    void addMapping(Eid key, Object data, boolean shouldOverwrite, boolean merge);

    /**
     * Retrieves mapping for the provided srcKey and dstKey.
     *
     * @param srcKey
     *            Source Key to be looked up
     * @param dstKey
     *            Destination Key to be looked up
     * @return Returns the object found in the cache or null if nothing is found.
     */
    Object getMapping(Eid srcKey, Eid dstKey);

    /**
     * Retrieves mapping for the provided srcKey, dstKey and a XtrId. In case the IMapCache is not a
     * Simple Map Cache i.e. Flat Map Cache or Multi Table Map Cache, this method returns null.
     *
     * @param srcKey
     *            Source Key to be looked up
     * @param dstKey
     *            Destination Key to be looked up
     * @param xtrId
     *            xtr id for which look up to be done
     * @return Returns the object found in the cache or null if nothing is found or IMapCache is not a Simple
     *         Map Cache.
     */
    Object getMapping(Eid srcKey, Eid dstKey, byte[] xtrId);

    /**
     * Retrieves widest negative prefix.
     *
     * @param key
     *            Source Key to be looked up
     * @return Returns the widest negative prefix or null if nothing is found.
     */
    Eid getWidestNegativeMapping(Eid key);

    /**
     * Remove mapping.
     *
     * @param key
     *            Key to be removed
     * @param overwrite
     *            Select if mappings with the same key were overwritten on store
     *
     */
    void removeMapping(Eid key, boolean overwrite);

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
     * Update mapping registration.
     *
     * @param key
     *            The EID whose registration must be updated
     * @param timestamp
     *            New timestamp for the mapping
     */
    void updateMappingRegistration(Eid key, Long timestamp);

    /**
     * Add data for key.
     *
     * @param key
     *            The key for which data is inserted
     * @param subKey
     *            The subKey where data should be inserted
     * @param data
     *            The data to be stored
     */
    void addData(Eid key, String subKey, Object data);

    /**
     * Generic retrieval of data.
     *
     * @param key
     *            The key where the data is stored
     * @param subKey
     *            The subKey where data is stored
     * @return The data
     */
    Object getData(Eid key, String subKey);

    /**
     * Generic removal of data.
     *
     * @param key
     *            The key of the data to be removed
     * @param subKey
     *            The subKey of the data to be removed
     */
    void removeData(Eid key, String subKey);

    /**
     * Print mappings in cache. Used for testing, debugging and the karaf shell.
     *
     * @return a String consisting of all the mappings in the cache
     */
    String printMappings();
}
