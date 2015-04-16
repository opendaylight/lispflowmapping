/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.backends;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingKey;

/**
 * The {@link IStorageBackend} interface defines methods for storing and
 * retrieving mapping data in different back-end implementations
 *
 * @author Lorand Jakab
 *
 */
public interface IStorageBackend {

    /**
     * Stores a mapping in the back-end storage
     *
     * @param mapping is the actual object, for which we build the index.
     * @param orig holds the origin of the mapping (northbound/southbound).
     * @return the index object from the back-end where the data was stored
     */
    public StorageIndex storeMapping(EidToLocatorRecord mapping, MappingOrigin orig);

    /**
     * @param orig selects which type of mapping are we interested in.
     * @return the mapping object from the back-end storage.
     */
    public EidToLocatorRecord retrieveMapping(StorageIndex index);

    /**
     * Stores a key object in the back-end storage, while caching the string
     * version of it in memory.
     *
     * @param key is the mapping key object received from an RPC.
     * @return the index object from the back-end where the data was stored
     */
    public StorageIndex storeKey(MappingKey key);

    /**
     * @return the String representation of the authentication key
     */
    public String retrieveKey(StorageIndex index);
}
