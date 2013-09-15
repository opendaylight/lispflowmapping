package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public class MappingServiceKey {
    
    private LispAddress EID;
    private byte mask;
    
    
    
    public MappingServiceKey(LispAddress EID, byte mask) {
        this.EID=EID;
        this.mask = mask;
    }
    
    public MappingServiceKey(LispAddress EID) {
        this(EID,(byte)0);
    }
    
    
    public LispAddress getEID() {
        return EID;
    }
    public void setEID(LispAddress eID) {
        EID = eID;
    }
    public byte getMask() {
        return mask;
    }
    public void setMask(byte mask) {
        this.mask = mask;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((EID == null) ? 0 : EID.hashCode());
        result = prime * result + mask;
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
        MappingServiceKey other = (MappingServiceKey) obj;
        if (EID == null) {
            if (other.EID != null)
                return false;
        } else if (!EID.equals(other.EID))
            return false;
        if (mask != other.mask)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        if (mask > 0){
            return EID.toString() + "/" + mask;
        }
        return EID.toString();
    }
    
}
