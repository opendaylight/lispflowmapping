/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface ILispDAO {

    public <K> void put(K key, MappingEntry<?>... values);

    public <K, V> V getSpecific(K key, MappingValueKey<V> valueKey);

    public <K> Object getSpecific(K key, String valueKey);

    public <K> Map<String, ?> get(K key);

    public <K> boolean remove(K key);

    public void clearAll();

    public <UserType, DbType> void register(Class<? extends ILispTypeConverter<UserType, DbType>> userType);

    public TimeUnit getTimeUnit();

    public void setTimeUnit(TimeUnit timeUnit);

    public int getCleanInterval();

    public void setCleanInterval(int cleanInterval);
}
