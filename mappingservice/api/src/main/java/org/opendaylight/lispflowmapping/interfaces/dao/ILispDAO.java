/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface ILispDAO extends IQueryAll {

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
    public <K> Map<String, Object> get(K key);

    /**
     * Remove an entry from the DAO
     * 
     * @param key
     *            The key of the entry
     * @return true if success, false otherwise
     */
    public boolean remove(Object key);

    /**
     * Remove an entry from the DAO
     * 
     * @param key
     *            The key of the entry
     * @param valueKey
     *            The value to delete
     * @return true if success, false otherwise
     */
    public boolean removeSpecific(Object key, String valueKey);

    /**
     * Clear the DAO and remove all of the entries.
     */
    public void clearAll();

    /**
     * @return The time unit of the DAO cleaner
     */
    public TimeUnit getTimeUnit();

    /**
     * Set the time unit of the DAO cleaner
     * 
     * @param timeUnit
     */
    public void setTimeUnit(TimeUnit timeUnit);

}
