package org.opendaylight.lispflowmapping.type.lisp.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispListLCAFAddress extends LispLCAFAddress {
    List<LispAddress> addresses;

    public LispListLCAFAddress(byte res2, List<LispAddress> addresses) {
        super(LispCanonicalAddressFormatEnum.LIST, res2);
        this.addresses = addresses;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        super.internalSerialize(buffer);
        for (LispAddress address : addresses) {
            address.serialize(buffer);
        }
    }

    @Override
    public short getLcafLength() {
        short totalSize = 0;
        for (LispAddress address : addresses) {
            totalSize += address.getAddressSize();
        }
        return totalSize;
    }

    public List<LispAddress> getAddresses() {
        return addresses;
    }

    public static LispListLCAFAddress valueOf(byte res2, short length, ByteBuffer buffer) {
        List<LispAddress> addresses = new ArrayList<LispAddress>();
        while (length > 0) {
            LispAddress address = LispAddress.valueOf(buffer);
            length -= address.getAddressSize();
            addresses.add(address);
        }
        return new LispListLCAFAddress(res2, addresses);
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
