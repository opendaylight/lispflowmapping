/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public abstract class LispLCAFAddress extends LispAddress {

    protected LispCanonicalAddressFormatEnum lcafType;
    protected byte res2;

    public LispLCAFAddress(LispCanonicalAddressFormatEnum lcafType, byte res2) {
        super(AddressFamilyNumberEnum.LCAF);
        this.lcafType = lcafType;
        this.res2 = res2;
    }


    public LispCanonicalAddressFormatEnum getType() {
        return lcafType;
    }

    public byte getRes2() {
        return res2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((lcafType == null) ? 0 : lcafType.hashCode());
        result = prime * result + res2;
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
        LispLCAFAddress other = (LispLCAFAddress) obj;
        if (lcafType != other.lcafType)
            return false;
        if (res2 != other.res2)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[lcafType=" + lcafType + ", res2=" + res2 + "]";
    }

}
