/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

public interface ILispDAO {

    /**
     * Put a entry into the DAO.
     *
     * @param key
     *            The entry's key.
     * @param values
     *            The entry's value.
     */
    void put(Object key, MappingEntry<?>... values);

    /**
     * Get a specific value from the DAO.
     *
     * @param key
     *            The key of the value to fetch
     * @param valueKey
     *            The value to fetch
     * @return The value from the DAO.
     */
    Object getSpecific(Object key, String valueKey);

    /**
     * Get the entries from the DAO.
     *
     * @param key
     *            The key to be looked up as exact match.
     * @return The value from the DAO.
     */
    Map<String, Object> get(Object key);

    /**
     * Get value for longest prefix match from the DAO.
     *
     * @param key
     *            The eid prefix, IPv4 or IPv6, to be looked up. Key must be normalized.
     * @return The value from the DAO.
     */
    Map<String, Object> getBest(Object key);

    /**
     * Get longest prefix match and value from the DAO.
     *
     * @param key
     *            The eid prefix, IPv4 or IPv6, to be looked up. Key must be normalized
     * @return The best match and value pair from the DAO.
     */
    SimpleImmutableEntry<Eid, Map<String, ?>> getBestPair(Object key);

    /**
     * Get parent prefix.
     *
     * @param key
     *            The eid prefix, IPv4 or IPv6, to be looked up. Key must be normalized.
     * @return The parent prefix of the longest prefix match for the key.
     */
    Eid getParentPrefix(Eid key);

    /**
     * Get sibling prefix.
     *
     * @param key
     *            The eid prefix, IPv4 or IPv6, to be looked up. Key must be normalized.
     * @return The sibling prefix of the longest prefix match for the key.
     */
    Eid getSiblingPrefix(Eid key);

    /**
     * Get widest negative prefix.
     *
     * @param key
     *            The eid prefix, IPv4 or IPv6, to be looked up. Key must be normalized.
     * @return The widest negative prefix found.
     */
    Eid getWidestNegativePrefix(Eid key);

    /**
     * Enumerate all the entries from the DAO.
     *
     * @param visitor
     *            The visitor object.
     */
    void getAll(IRowVisitor visitor);

    /**
     * Remove an entry from the DAO.
     *
     * @param key
     *            The key of the entry to delete
     */
    void remove(Object key);

    /**
     * Remove an entry from the DAO.
     *
     * @param key
     *            The key of the entry
     * @param valueKey
     *            The value to delete
     */
    void removeSpecific(Object key, String valueKey);

    /**
     * Clear the DAO and remove all of the entries.
     */
    void removeAll();

    /**
     * Insert a new table for given key.
     *
     * @param key
     *            The key for the table
     * @return The inserted table
     */
    ILispDAO putTable(String key);

    /**
     * Inserts a new, nested table for given key and subkey. Also acts as factory method.
     *
     * @param key
     *              The key for which a new table is linked in
     * @param valueKey
     *              The subkey under which to insert the new table
     * @return The inserted table
     */
    ILispDAO putNestedTable(Object key, String valueKey);
}
