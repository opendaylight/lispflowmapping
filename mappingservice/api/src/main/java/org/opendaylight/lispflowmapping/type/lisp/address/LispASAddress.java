/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public class LispASAddress extends LispAddress {
    private int asNum;

    public LispASAddress(int num) {
        super(AddressFamilyNumberEnum.AS);
        this.asNum = num;
    }

    public static LispASAddress valueOf(ByteBuffer buffer) {
        throw new RuntimeException("Not implemented");
    }

    public int getAS() {
        return asNum;
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + asNum;
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
        LispASAddress other = (LispASAddress) obj;
        if (asNum != other.asNum)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "" + asNum;
    }
}
