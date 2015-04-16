/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.index;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingKey;

/**
 * The {@link StorageIndex} stores pointers to objects in different backend
 * storage systems, with initial implementations for the MD-SAL and the
 * ClusterDAO datastores.
 *
 * Methods which access the backend storage start with "store" and "retrieve",
 * while methods accessing only in-memory structures start with "set" and "get"
 * respectively.
 *
 * @author Lorand Jakab
 *
 */
public interface StorageIndex {

    /**
     * Stores a mapping in the backend storage
     *
     * @param mapping is the actual object, for which we build the index.
     * @param orig holds the origin of the mapping (northbound/southbound).
     */
    public void storeMapping(EidToLocatorRecord mapping, MappingOrigin orig);

    /**
     * Stores a key object in the backend storage, while caching the string
     * version of it in memory.
     *
     * @param key is the mapping key object received from an RPC.
     */
    public void storeKey(MappingKey key);

    /**
     * @param orig selects which type of mapping are we interested in.
     * @return the mapping object from the backend storage.
     */
    public EidToLocatorRecord retrieveMapping(MappingOrigin orig);

    /**
     * @return the String representation of the authentication key from the
     * in-memory cache.
     */
    public String getKey();
}
