/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util.timebucket.interfaces;

import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Created by sheikahm on 12/1/16.
 */
public interface ISouthBoundMappingTimeoutService {

    /**
     * Add mapping in Southbound Mapping Timeout Manager
     * which is currently Time Bucket Data Structure.
     *
     * @param key
     *            The key for the mapping
     * @param mappingData
     *            Mapping to be stored
     * @return The id of the bucket the mapping was added to
     */
    int addMapping(Eid key, MappingData mappingData);

    /**
     * Refresh mapping in southbound manager. Remove old mapping
     * from timebucket and add the mapping in proper time bucket.
     * This is either because mapping re-registration or new merged
     * mapping and refresh mapping request in MS/MR.
     *
     * @param key
     *            The key for the mapping
     * @param newMappingData
     *            New Mapping Data for the key
     * @param presentBucketId
     *            The id of the bucket the previous mapping is in
     * @return The new id of the bucket the mapping was added to
     */
    int refreshMapping(Eid key, MappingData newMappingData, int presentBucketId);

    /**
     * Remove mapping from Southbound manager.
     *
     * @param key
     *            The key for the mapping
     */
    void removeMappingFromTimeoutService(Eid key, int presentBucketId);

    /**
     * Remove the expired mappings from the TIme Bucket. This
     * should remove mapping from both SimpleMapCache and DataStoreBackEnd.
     */
    void removeExpiredMappings();

    /**
     * Returns true if the threshold for mapping expiration exceeds
     * the limit allowed.
     *
     * <p>
     * Because expiring a bucket can be a costly task, we only
     * remove expired mapping during map registration. Now, if
     * there is significant delay between the last expiration and
     * the map registration, we remove them anyways.
     * </p>
     */
    boolean mustRemoveExpiredMappings();
}
