package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.lispflowmapping.type.lisp.address.IMaskable;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;

public class MappingServiceNoMaskKey implements IMappingServiceKey {

    private LispAddressContainer EID;

    public MappingServiceNoMaskKey(LispAddressContainer lispAddressContainer) {
        this.EID = lispAddressContainer;
    }

    public LispAddressContainer getEID() {
        return EID;
    }

    public void setEID(LispAddressContainer eID) {
        EID = eID;
    }

    public int getMask() {
        if (EID instanceof IMaskable) {
            return ((IMaskable) EID).getMaxMask() & 0xFF;
        } else {
            return 0;
        }
    }

    public void setMask(byte mask) {
        return;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((EID == null) ? 0 : EID.hashCode());
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
        MappingServiceNoMaskKey other = (MappingServiceNoMaskKey) obj;
        if (EID == null) {
            if (other.EID != null)
                return false;
        } else if (!EID.equals(other.EID))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return EID.toString();
    }

}
