/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc.  All rights reserved.
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
     * @param value
     *            Mapping to be stored
     * @param sourceRlocs
     *            The set of RLOCs from all registrations. Used for merging
     */
    void addMapping(Eid key, Object value, Set<IpAddressBinary> sourceRlocs);

    /**
     * Add mapping.
     *
     * @param key
     *            Key of the mapping
     * @param xtrId
     *            xTR-ID of the mapping
     * @param value
     *            Mapping to be stored
     */
    void addMapping(Eid key, XtrId xtrId, Object value);

    /**
     * Retrieves mapping for the provided srcKey, dstKey and a XtrId.
     *
     * @param key
     *            Key to be looked up
     * @param xtrId
     *            xTR-ID for which look-up to be done
     * @return Returns the mapping found in the cache
     */
    Object getMapping(Eid key, XtrId xtrId);

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

    /**
     * Look up the covering prefix for the argument, but exclude the argument itself, so the result is always less
     * specific than the lookup key.
     *
     * @param key
     *            The eid prefix, IPv4 or IPv6, to be looked up. Key must be normalized.
     * @return The covering prefix.
     */
    Eid getCoveringLessSpecific(Eid key);

    /**
     * Returns the parent prefix for given key.
     *
     * @param key
     *            The key for which parent is to be returned.
     * @return The parent prefix of a specific key.
     */
    Eid getParentPrefix(Eid key);

    /**
     * Returns the sibling prefix for given key.
     *
     * @param key
     *            The key for which sibling is to be returned.
     * @return The sibling prefix of a specific key.
     */
    Eid getSiblingPrefix(Eid key);

    /**
     * Returns the virtual parent sibling prefix for given key.
     *
     * @param key
     *            The key for which virtual parent sibling is to be returned.
     * @return The virtual parent sibling prefix of a specific key.
     */
    Eid getVirtualParentSiblingPrefix(Eid key);
}
