/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispSegmentLCAFAddress extends LispLCAFAddress {

    private LispAddress address;
    private int instanceId;

    public LispSegmentLCAFAddress(byte idMaskLen, int instanceId, LispAddress address) {
        super(LispCanonicalAddressFormatEnum.SEGMENT, idMaskLen);
        this.address = address;
        this.instanceId = instanceId;
    }


    public LispAddress getAddress() {
        return address;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public byte getIdMaskLen() {
        return getRes2();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + instanceId;
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
        LispSegmentLCAFAddress other = (LispSegmentLCAFAddress) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (instanceId != other.instanceId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LispSegmentLCAFAddress#[address=" + address + ", instanceId=" + instanceId + "]" + super.toString();
    }


}
