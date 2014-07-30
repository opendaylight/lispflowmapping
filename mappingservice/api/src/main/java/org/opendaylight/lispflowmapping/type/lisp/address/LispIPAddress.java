/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public abstract class LispIPAddress extends LispAddress implements IMaskable {

    protected InetAddress address;

    @Override
    public abstract LispIPAddress clone();

    protected LispIPAddress(InetAddress address, AddressFamilyNumberEnum afi) {
        super(afi);
        this.address = address;
    }

    protected LispIPAddress(int address, AddressFamilyNumberEnum afi) {
        super(afi);
        try {
            this.address = InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(address).array());
        } catch (UnknownHostException e) {
        }
    }

    protected LispIPAddress(byte[] address, AddressFamilyNumberEnum afi) {
        super(afi);
        try {

            this.address = InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
        }
    }

    protected LispIPAddress(String name, AddressFamilyNumberEnum afi) {
        super(afi);
        try {
            this.address = InetAddress.getByName(name);
        } catch (UnknownHostException e) {
        }
    }

    public InetAddress getAddress() {
        return address;
    }

    public void normalize(int mask) {
        if (mask < 0 || mask >= getMaxMask()) {
            return;
        }
        ByteBuffer byteRepresentation = ByteBuffer.wrap(address.getAddress());
        byte b = (byte) 0xff;
        for (int i = 0; i < byteRepresentation.array().length; i++) {
            if (mask >= 8)
                byteRepresentation.put(i, (byte) (b & byteRepresentation.get(i)));

            else if (mask > 0) {
                byteRepresentation.put(i, (byte) ((byte) (b << (8 - mask)) & byteRepresentation.get(i)));
            } else {
                byteRepresentation.put(i, (byte) (0 & byteRepresentation.get(i)));
            }

            mask -= 8;
        }
        try {
            this.address = InetAddress.getByAddress(byteRepresentation.array());
        } catch (UnknownHostException e) {
        }

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
