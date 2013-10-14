package org.opendaylight.lispflowmapping.interfaces.dao;


public class MappingEntry<V> {
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

    @Override
    public String toString() {
        return "MappingServiceEntry: " + value.toString();
    }
}