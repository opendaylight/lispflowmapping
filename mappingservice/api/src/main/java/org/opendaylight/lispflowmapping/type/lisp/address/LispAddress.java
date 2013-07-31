/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public abstract class LispAddress {
    private AddressFamilyNumberEnum afi;

    protected LispAddress(AddressFamilyNumberEnum afi) {
        this.afi = afi;
    }

    public AddressFamilyNumberEnum getAfi() {
        return afi;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((afi == null) ? 0 : afi.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LispAddress other = (LispAddress) obj;
        if (afi != other.afi)
            return false;
        return true;
    }

}
