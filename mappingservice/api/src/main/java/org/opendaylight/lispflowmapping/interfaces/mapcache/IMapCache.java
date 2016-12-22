/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

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
     * @param value
     *            Value to be stored
     */
    void addMapping(Eid key, Object value);

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
     */
    void removeMapping(Eid key);

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

    /**
     * Print mappings in cache in a human friendly format.
     *
     * @return a String consisting of all the mappings in the cache
     */
    String prettyPrintMappings();
}
