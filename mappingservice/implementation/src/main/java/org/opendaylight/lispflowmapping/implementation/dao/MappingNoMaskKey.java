/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.dao;

import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingKey;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;

public class MappingNoMaskKey implements IMappingKey {

    private LispAddressContainer EID;

    public MappingNoMaskKey(LispAddressContainer lispAddressContainer) {
        this.EID = lispAddressContainer;
    }

    public LispAddressContainer getEID() {
        return EID;
    }

    public int getMask() {
        LispAFIAddress eidAFIAddress = LispAFIConvertor.toAFI(EID);
        if (MaskUtil.isMaskable(eidAFIAddress)) {
            return MaskUtil.getMaxMask(eidAFIAddress);
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((EID == null) ? 0 : EID.hashCode());
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
        MappingNoMaskKey other = (MappingNoMaskKey) obj;
        if (EID == null) {
            if (other.EID != null)
                return false;
        } else if (!EID.equals(other.EID))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return EID.toString();
    }

}
