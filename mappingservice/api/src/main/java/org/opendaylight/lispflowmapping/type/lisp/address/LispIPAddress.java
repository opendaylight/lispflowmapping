package org.opendaylight.lispflowmapping.type.lisp.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public abstract class LispIPAddress extends LispAddress{
	
	protected InetAddress address;

	protected LispIPAddress(InetAddress address, AddressFamilyNumberEnum afi) {
		super(afi);
		this.address = address;
	}
	
	protected LispIPAddress(int address, AddressFamilyNumberEnum afi) {
        super(afi);
        try {
            this.address = InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(address).array());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
	protected LispIPAddress(byte[] address, AddressFamilyNumberEnum afi) {
        super(afi);
        try {

            this.address = InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
	protected LispIPAddress(String name, AddressFamilyNumberEnum afi) {
        super(afi);
        try {
            this.address = InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
	public InetAddress getAddress() {
        return address;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((address == null) ? 0 : address.hashCode());
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
        LispIPAddress other = (LispIPAddress) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return address.getHostAddress();
    }

}
