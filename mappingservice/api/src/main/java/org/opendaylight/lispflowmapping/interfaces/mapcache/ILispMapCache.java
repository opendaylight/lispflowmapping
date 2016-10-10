/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.mapcache;

import java.util.List;
import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;

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
     * @param mapping
     *            Mapping to be stored
     * @param timestamp
     *            Timestamp of the mapping
     * @param sourceRlocs
     *            The set of RLOCs from all registrations. Used for merging
     */
    void addMapping(Eid key, MappingRecord mapping, Long timestamp, Set<IpAddressBinary> sourceRlocs);

    /**
     * Add mapping.
     *
     * @param key
     *            Key of the mapping
     * @param xtrId
     *            xTR-ID of the mapping
     * @param mapping
     *            Mapping to be stored
     * @param timestamp
     *            Timestamp of the mapping
     */
    void addMapping(Eid key, XtrId xtrId, MappingRecord mapping, Long timestamp);

    /**
     * Retrieves mapping for the provided srcKey, dstKey and a XtrId.
     *
     * @param key
     *            Key to be looked up
     * @param xtrId
     *            xTR-ID for which look-up to be done
     * @return Returns the mapping found in the cache
     */
    MappingRecord getMapping(Eid key, XtrId xtrId);

    /**
     * Remove mapping.
     *
     * @param key
     *            Key to be removed
     * @param xtrId
     *            xTR-ID of the mapping to be removed
     */
    void removeMapping(Eid key, XtrId xtrId);

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
    List<Object> getAllXtrIdMappings(Eid key);

    /**
     * Batch remove several xTR-ID sub-mappings under a certain key.
     *
     * <p>This is a performance optimization, since interactive calls to
     * removeMapping() would result in LPM lookups of the key for each call.
     * With this method, only one such lookup is performed.</p>
     *
     * @param key
     *            Key to be looked up
     * @param xtrIds
     *            List of xTR-IDs that need to be removed
     */
    void removeXtrIdMappings(Eid key, List<XtrId> xtrIds);
}
