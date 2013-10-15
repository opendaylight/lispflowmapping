package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReplyAction;

public class MappingServiceRLOC {

    private LocatorRecord record;
    private int ttl;
    private MapReplyAction action;
    private boolean authoritative;

    public MappingServiceRLOC(LocatorRecord record, int ttl, MapReplyAction action, boolean authoritative) {
        super();
        this.record = record;
        this.ttl = ttl;
        this.action = action;
        this.authoritative = authoritative;
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

    public MapReplyAction getAction() {
        return action;
    }

    public void setAction(MapReplyAction action) {
        this.action = action;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public void setAuthoritative(boolean authoritative) {
        this.authoritative = authoritative;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + (authoritative ? 1231 : 1237);
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
        MappingServiceRLOC other = (MappingServiceRLOC) obj;
        if (action != other.action)
            return false;
        if (authoritative != other.authoritative)
            return false;
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
