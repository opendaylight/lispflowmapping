package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;

public class MappingServiceValue {
    
    private LocatorRecord record;
    private int ttl;
    
    public MappingServiceValue(LocatorRecord record, int ttl) {
        super();
        this.record = record;
        this.ttl = ttl;
    }
    public LocatorRecord getRecord() {
        return record;
    }
    public void setRecord(LocatorRecord record) {
        this.record = record;
    }
    public int getTtl() {
        return ttl;
    }
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((record == null) ? 0 : record.hashCode());
        result = prime * result + ttl;
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
        if (record == null) {
            if (other.record != null)
                return false;
        } else if (!record.equals(other.record))
            return false;
        if (ttl != other.ttl)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return record.getLocator().toString();
    }

}
