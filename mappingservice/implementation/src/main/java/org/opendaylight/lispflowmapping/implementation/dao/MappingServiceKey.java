package org.opendaylight.lispflowmapping.implementation.dao;

import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;

public class MappingServiceKey implements IMappingServiceKey {

    private LispAddressContainer EID;
    private int mask;

    public MappingServiceKey(LispAddressContainer lispAddressContainer, int mask) {
        this.EID = lispAddressContainer;
        this.mask = mask;
    }

    public LispAddressContainer getEID() {
        return EID;
    }

    public void setEID(LispAddressContainer eID) {
        EID = eID;
    }

    public int getMask() {
        return mask & 0xFF;
    }

    public void setMask(int mask) {
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
        if (mask > 0) {
            return EID.toString() + "/" + mask;
        }
        return EID.toString();
    }

}
