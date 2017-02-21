/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.timebucket.interfaces;

import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Created by Shakib Ahmed on 12/1/16.
 */
public interface ISouthBoundMappingTimeoutService {

    /**
     * Add mapping in Southbound Mapping Timeout Manager
     * which is currently Time Bucket Wheel.
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
     * from Time Bucket Wheel and add the mapping in proper time bucket.
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
     * @param presentBucketId
     *            The id of the present bucket the key is in
     */
    void removeMappingFromTimeoutService(Eid key, int presentBucketId);

    /**
     * Remove the expired mappings from the Time Bucket Wheel. This
     * should remove mapping from both SimpleMapCache and DataStoreBackEnd.
     */
    void removeExpiredMappings();
}
