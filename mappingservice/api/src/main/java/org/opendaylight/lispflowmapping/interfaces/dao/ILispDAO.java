/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Map;

public interface ILispDAO {

    /**
     * Put a entry into the DAO.
     *
     * @param key
     *            The entry's key.
     * @param values
     *            The entry's value.
     */
    public void put(Object key, MappingEntry<?>... values);

    /**
     * Get a specific value from the DAO.
     *
     * @param key
     *            The key of the value to fetch
     * @param valueKey
     *            The value to fetch
     * @return The value from the DAO.
     */
    public Object getSpecific(Object key, String valueKey);

    /**
     * Get the entries from the DAO
     *
     * @param key
     *            The key.
     * @return The value from the DAO.
     */
    public Map<String, Object> get(Object key);

    /**
     * Enumerate all the entries from the DAO
     *
     * @param visitor
     *            The visitor object.
     */
    public void getAll(IRowVisitor visitor);

    /**
     * Remove an entry from the DAO
     *
     * @param key
     *            The key of the entry to delete
     */
    public void remove(Object key);

    /**
     * Remove an entry from the DAO
     *
     * @param key
     *            The key of the entry
     * @param valueKey
     *            The value to delete
     */
    public void removeSpecific(Object key, String valueKey);

    /**
     * Clear the DAO and remove all of the entries.
     */
    public void removeAll();

    /**
     * Inserts a new, nested table for given key and subkey. Also acts as factory method.
     *
     * @param key
     *              The key for which a new table is linked in
     * @param valueKey
     *              The subkey under which to insert the new table
     * @return The inserted table
     */
    public ILispDAO putNestedTable(Object key, String valueKey);
}
