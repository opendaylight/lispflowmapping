/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

/**
 * Mapping System interface.
 *
 * @author Florin Coras
 *
 */

public interface IMappingSystem {
    /**
     * Add mapping.
     *
     * @param origin
     *            Table where mapping should be added
     * @param key
     *            Key of the mapping
     * @param mapping
     *            Mapping to be stored
     */
    void addMapping(MappingOrigin origin, Eid key, MappingData mapping);

    /**
     * Generate and add a negative mapping entry originated from the southbound, and return the generated mapping.
     *
     * @param key
     *            Key of the mapping
     * @return Returns the generated negative mapping (which is never null).
     */
    MappingData addNegativeMapping(Eid key);

    /**
     * Update mapping.
     *
     * @param origin
     *            Table where mapping should be added
     * @param key
     *            Key of the mapping
     * @param mapping
     *            Mapping to be stored
     */
    void updateMapping(MappingOrigin origin, Eid key, MappingData mapping);

    /**
     * Retrieves mapping for the provided src and dst key.
     *
     * @param src
     *            Source Key to be looked up
     * @param dst
     *            Destination Key to be looked up
     * @return Returns the mapping found in the MappingSystem or null if nothing is found.
     */
    MappingData getMapping(Eid src, Eid dst);

    /**
     * Retrieves mapping for the provided dst key.
     *
     * @param dst
     *            Destination Key to be looked up
     * @return Returns the mapping found in the Mapping System or null if nothing is found.
     */
    MappingData getMapping(Eid dst);

    /**
     * Retrieves mapping for the provided dst key for a particular xtr id.
     * @param src
     *            Source Key to be looked up.
     * @param dst
     *            Destination Key to be looked up.
     * @param xtrId
     *            Xtr Id for which this look to be done. If null, this method works like
     *            regular getMapping(src, dst)
     * @return Returns the mapping found in the simple map cache or null if nothing is found.
     */
    MappingData getMapping(Eid src, Eid dst, XtrId xtrId);

    /**
     * Retrieves mapping from table for provided key.
     *
     * @param origin
     *            Table where mapping should be looked up
     * @param key
     *            Key to be looked up
     * @return Returns the mapping found in the cache or null if nothing is found.
     */
    MappingData getMapping(MappingOrigin origin, Eid key);

    /**
     * Retrieves widest negative prefix from table for provided key.
     *
     * @param key
     *            Key to be looked up
     * @return Returns the prefix found in the cache or null if nothing is found.
     */
    Eid getWidestNegativePrefix(Eid key);

    /**
     * Retrieves the subtree of a maskable prefix from the given map-cache.
     *
     * @param origin
     *            Table where the key should be looked up
     * @param key
     *            Key to be looked up
     * @return The child prefixes of the prefix, including the prefix itself if present
     */
    Set<Eid> getSubtree(MappingOrigin origin, Eid key);

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
     *            Table for mapping that should be removed
     * @param key
     *            Key to be removed
     *
     */
    void removeMapping(MappingOrigin origin, Eid key);

    /**
     * Subscribe a Subscriber to receive updates about mapping changes for an EID.
     *
     * @param subscriber
     *            The Subscriber object with information about the subscriber
     * @param subscribedEid
     *            The EID for which the subscriber will receive updates
     */
    void subscribe(Subscriber subscriber, Eid subscribedEid);

    /**
     * Retrieves the subscribers for an EID.
     *
     * @param eid
     *            The EID to be looked up
     * @return
     *            The set of subscribers for the EID
     */
    Set<Subscriber> getSubscribers(Eid eid);

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
     * Add data for key.
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
    void addData(MappingOrigin origin, Eid key, String subKey, Object data);

    /**
     * Generic retrieval of data.
     *
     * @param origin
     *            Table from where data should be retrieved
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
     *            Table from where data should be removed
     * @param key
     *            The key of the data to be removed
     * @param subKey
     *            The subKey of the data to be removed
     */
    void removeData(MappingOrigin origin, Eid key, String subKey);

    /**
     * Returns the parent prefix for given key.
     *
     * @param key
     *            The key which parent is to be returned.
     * @return The parent perfix of a specific key.
     */
    Eid getParentPrefix(Eid key);

    /**
     * Sets iterateMask. If set to true, longest prefix matching for IP keys is used.
     *
     * @param iterate
     *            Value to configure
     *
     */
    void setIterateMask(boolean iterate);

    /**
     * Configure merge policy. If set to true, mappings are merged.
     *
     * @param mappingMerge
     *            Value to configure
     */
    void setMappingMerge(boolean mappingMerge);

    /**
     * Print all mappings. Used for testing, debugging and the karaf shell.
     *
     * @return String consisting of all mappings
     */
    String printMappings();

    /**
     * Print mappings in cache in a human friendly format.
     *
     * @return a String consisting of all the mappings in the cache
     */
    String prettyPrintMappings();

    /**
     * Print all authentication keys. Used for testing, debugging and the karaf shell.
     *
     * @return String consisting of all mappings
     */
    String printKeys();

    /**
     * Print keys in cache in a human friendly format.
     *
     * @return a String consisting of all the keys in the cache
     */
    String prettyPrintKeys();

    /**
     * Set cluster master status.
     *
     * @param isMaster
     *            is|isn't master
     */
    void setIsMaster(boolean isMaster);

    /**
     * Get cluster master status.
     *
     * @return isMaster
     *            is|isn't master
     */
    boolean isMaster();
}
