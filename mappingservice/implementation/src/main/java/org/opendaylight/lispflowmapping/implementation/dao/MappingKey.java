/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.dao;

import org.opendaylight.lispflowmapping.interfaces.dao.IMappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;

public class MappingKey implements IMappingKey {

    private LispAddressContainer EID;
    private int mask;

    public MappingKey(LispAddressContainer lispAddressContainer, int mask) {
        this.EID = lispAddressContainer;
        this.mask = mask;
    }

    public LispAddressContainer getEID() {
        return EID;
    }

    public int getMask() {
        return mask & 0xFF;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((EID == null) ? 0 : EID.hashCode());
        result = prime * result + mask;
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
        MappingKey other = (MappingKey) obj;
        if (EID == null) {
            if (other.EID != null)
                return false;
        } else if (!EID.equals(other.EID))
            return false;
        if (mask != other.mask)
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (mask > 0) {
            return EID.toString() + "/" + mask;
        }
        return EID.toString();
    }

}
