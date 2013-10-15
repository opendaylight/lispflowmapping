package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.List;

public class MappingServiceValue {

    private List<MappingServiceRLOC> rlocs;
    private String key;

    private final static MappingServiceValue EMPTY_MAPPING_SERVICE_VALUE = new MappingServiceValue();

    public MappingServiceValue() {
    }

    public MappingServiceValue(List<MappingServiceRLOC> rlocs, String key) {
        this.rlocs = rlocs;
        this.key = key;
    }

    public List<MappingServiceRLOC> getRlocs() {
        return rlocs;
    }

    public void setRlocs(List<MappingServiceRLOC> rlocs) {
        this.rlocs = rlocs;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEmpty() {
        return equals(EMPTY_MAPPING_SERVICE_VALUE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((rlocs == null) ? 0 : rlocs.hashCode());
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
        MappingServiceValue other = (MappingServiceValue) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (rlocs == null) {
            if (other.rlocs != null)
                return false;
        } else if (!rlocs.equals(other.rlocs))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("MappingServiceValue: Password: ").append(key).append(" RLOCs: ").append(rlocs).toString();
    }

}
