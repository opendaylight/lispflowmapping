/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public class LispMACAddress extends LispAddress {
    private byte[] mac = new byte[6];

    public LispMACAddress(byte[] mac) {
        super(AddressFamilyNumberEnum.MAC);
        System.arraycopy(mac, 0, this.mac, 0, 6);
    }

    public static LispMACAddress valueOf(ByteBuffer buffer) {
        throw new RuntimeException("Not implemented");
    }

    public byte[] getMAC() {
        return mac;
    }

    @Override
    public int getAddressSize() {
        throw new RuntimeException("Not implemented");

    }

    @Override
    public void serialize(ByteBuffer buffer) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String toString() {
        return "LispMACAddress#" + DatatypeConverter.printHexBinary(mac) + super.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(mac);
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
        LispMACAddress other = (LispMACAddress) obj;
        if (!Arrays.equals(mac, other.mac))
            return false;
        return true;
    }
}
