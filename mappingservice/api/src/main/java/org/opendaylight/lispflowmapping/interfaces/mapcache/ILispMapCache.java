/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * LISP southbound control protocol specific additions to the map-cache
 * interface.
 *
 * @author Lorand Jakab
 *
 */
public interface ILispMapCache extends IMapCache {
    /**
     * Add mapping.
     *
     * @param key
     *            Key of the mapping
     * @param xtrId
     *            xTR-ID of the mapping
     * @param data
     *            Value to be stored
     */
    void addMapping(Eid key, byte[] xtrId, MappingRecord data);

    /**
     * Retrieves mapping for the provided srcKey, dstKey and a XtrId.
     *
     * @param key
     *            Key to be looked up
     * @param xtrId
     *            xTR-ID for which look-up to be done
     * @return Returns the object found in the cache
     */
    Object getMapping(Eid key, byte[] xtrId);

    /**
     * Remove mapping.
     *
     * @param key
     *            Key to be removed
     * @param xtrId
     *            xTR-ID of the mapping to be removed
     */
    void removeMapping(Eid key, byte[] xtrId);

    /**
     * Refresh mapping registration with current time as the timestamp.
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
     * Retrieve all xTR-ID sub-mappings for an EID. Used for merging logic.
     *
     * @param key
     *            Key to be looked up
     * @return The list of Objects which should be mappings
     */
    public List<Object> getAllXtrIdMappings(Eid key);

    /**
     * Batch remove several xTR-ID sub-mappings under a certain key.
     *
     * This is a performance optimization, since interative calls to
     * removeMapping() would result in LPM lookups of the key for each call.
     * With this method, only one such lookup is performed.
     *
     * @param key
     *            Key to be looked up
     * @param xtrIds
     *            List of xTR-IDs that need to be removed
     */
    void removeXtrIdMappings(Eid key, List<XtrId> xtrIds);
}
