/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public class LispIpv6Address extends LispAddress {
    private InetAddress address;

    private LispIpv6Address(InetAddress address) {
        super(AddressFamilyNumberEnum.IP6);
        this.address = address;
    }

    public LispIpv6Address(byte[] address) {
        super(AddressFamilyNumberEnum.IP6);
        try {

            this.address = Inet6Address.getByAddress(address);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public LispIpv6Address(String name) {
        super(AddressFamilyNumberEnum.IP6);
        try {
            this.address = InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static LispIpv6Address valueOf(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[16];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new LispIpv6Address(address);
    }

    @Override
    public String toString() {
        return address.getHostAddress();
    }

    @Override
    public int getAddressSize() {
        return super.getAddressSize() + 16;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        super.internalSerialize(buffer);
        buffer.put(address.getAddress());
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
        LispIpv6Address other = (LispIpv6Address) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        return true;
    }
}
