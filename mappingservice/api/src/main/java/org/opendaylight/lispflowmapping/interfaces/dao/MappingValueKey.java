package org.opendaylight.lispflowmapping.interfaces.dao;

/**
 * A value in the mapping service DAO
 * 
 * @param <V>
 *            The value type.
 */
public class MappingValueKey<V> {
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