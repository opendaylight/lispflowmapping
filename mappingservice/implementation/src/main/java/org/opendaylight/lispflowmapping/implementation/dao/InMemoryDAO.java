/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.dao;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.dao.ILispDAO;
import org.opendaylight.lispflowmapping.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.dao.IQueryAll;
import org.opendaylight.lispflowmapping.dao.IRowVisitor;

public class InMemoryDAO implements ILispDAO, IQueryAll {
    private Map<Class<?>, Map<Object, Map<String, Object>>> typeToKeysToValues;

    public InMemoryDAO() {
        typeToKeysToValues = new HashMap<Class<?>, Map<Object, Map<String, Object>>>();
    }

    public void getAll(IRowVisitor visitor) {
        for (Map.Entry<Class<?>, Map<Object, Map<String, Object>>> typeEntry : typeToKeysToValues.entrySet()) {
            for (Map.Entry<Object, Map<String, Object>> keyEntry : typeEntry.getValue().entrySet()) {
                for (Map.Entry<String, Object> valueEntry : keyEntry.getValue().entrySet()) {
                    visitor.visitRow(typeEntry.getKey(), keyEntry.getKey(), valueEntry.getKey(), valueEntry.getValue());
                }
            }
        }
    }

    public <K> void put(K key, MappingEntry<?>... values) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        Map<String, Object> keyToValues = new HashMap<String, Object>();
        for (MappingEntry<?> entry : values) {
            keyToValues.put(entry.getKey(), entry.getValue());
        }
        keysToValues.put(key, keyToValues);
    }

    private <K> Map<Object, Map<String, Object>> getTypeMap(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Illegal null key.");
        }
        Map<Object, Map<String, Object>> keysToValues = typeToKeysToValues.get(key.getClass());
        if (keysToValues == null) {
            throw new IllegalArgumentException("Unknown key type " + key.getClass() + ". must register with IConverter first.");
        }
        return keysToValues;
    }

    @SuppressWarnings("unchecked")
    public <K, V> V getSpecific(K key, MappingValueKey<V> valueKey) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        Map<String, Object> keyToValues = keysToValues.get(key);
        if (keyToValues == null) {
            return null;
        }
        return (V) keyToValues.get(valueKey.getKey());
    }

    public <K> Object getSpecific(K key, String valueKey) {
        return getSpecific(key, new MappingValueKey<Object>(valueKey));
    }

    public <K> Map<String, ?> get(K key) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        return keysToValues.get(key);
    }

    public <K> boolean remove(K key) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        return keysToValues.remove(key) != null;
    }

    public <UserType, DbType> void register(Class<? extends ILispTypeConverter<UserType, DbType>> userType) {
        Class<?> eidType = (Class<?>) ((ParameterizedType) userType.getGenericInterfaces()[0]).getActualTypeArguments()[0];
        typeToKeysToValues.put(eidType, new HashMap<Object, Map<String, Object>>());
    }

    public void clearAll() {
        typeToKeysToValues.clear();
    }
}
