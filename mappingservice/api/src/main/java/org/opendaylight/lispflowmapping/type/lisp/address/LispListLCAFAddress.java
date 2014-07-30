/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import java.util.List;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispListLCAFAddress extends LispLCAFAddress {
    List<? extends LispAddress> addresses;

    public LispListLCAFAddress(byte res2, List<? extends LispAddress> addresses) {
        super(LispCanonicalAddressFormatEnum.LIST, res2);
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
