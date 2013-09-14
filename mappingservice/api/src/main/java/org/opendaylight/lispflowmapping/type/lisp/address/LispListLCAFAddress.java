package org.opendaylight.lispflowmapping.type.lisp.address;

import java.util.List;

public class LispListLCAFAddress extends LispBaseOneLCAFAddress {
    List<? extends LispAddress> addresses;

    public LispListLCAFAddress(byte res2, List<? extends LispAddress> addresses) {
        super(res2);
        this.addresses = addresses;
    }

    public List<? extends LispAddress> getAddresses() {
        return addresses;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((addresses == null) ? 0 : addresses.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LispListLCAFAddress other = (LispListLCAFAddress) obj;
        if (addresses == null) {
            if (other.addresses != null)
                return false;
        } else if (!addresses.equals(other.addresses))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LispListLCAFAddress#[addresses=" + addresses + "]" + super.toString();
    }
}
