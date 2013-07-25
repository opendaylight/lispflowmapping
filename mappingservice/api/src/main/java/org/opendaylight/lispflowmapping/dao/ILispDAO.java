/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.dao;

import java.util.Map;

public interface ILispDAO {

    public <K> void put(K key, MappingEntry<?>... values);

    public <K, V> V getSpecific(K key, MappingValueKey<V> valueKey);

    public <K> Object getSpecific(K key, String valueKey);

    public <K> Map<String, ?> get(K key);

    public <K> boolean remove(K key);

    public void clearAll();

    public <UserType, DbType> void register(Class<? extends ILispTypeConverter<UserType, DbType>> userType);

    public static class MappingValueKey<V> {
        private String key;

        public MappingValueKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MappingValueKey<?> other = (MappingValueKey<?>) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }
    }

    public static class MappingEntry<V> {
        private MappingValueKey<V> mappingValueKey;
        private V value;

        public MappingEntry(String key, V value) {
            this.mappingValueKey = new MappingValueKey<V>(key);
            this.value = value;
        }

        public String getKey() {
            return mappingValueKey.getKey();
        }

        public V getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mappingValueKey == null) ? 0 : mappingValueKey.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MappingEntry<?> other = (MappingEntry<?>) obj;
            if (mappingValueKey == null) {
                if (other.mappingValueKey != null)
                    return false;
            } else if (!mappingValueKey.equals(other.mappingValueKey))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
    }
}
