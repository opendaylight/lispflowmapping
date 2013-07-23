/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type;

import org.opendaylight.lispflowmapping.type.lisp.address.LispLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;

public enum LispCanonicalAddressFormatEnum {
    LIST(1, LispListLCAFAddress.class), //
    SEGMENT(2, LispSegmentLCAFAddress.class), //
    UNKNOWN(-1, null);

    private byte lispCode;
    private Class<? extends LispLCAFAddress> lcafClass;

    private LispCanonicalAddressFormatEnum(int lispCode, Class<? extends LispLCAFAddress> lcafClass) {
        this.lispCode = (byte) lispCode;
        this.lcafClass = lcafClass;
    }

    public byte getLispCode() {
        return lispCode;
    }

    public Class<? extends LispLCAFAddress> getLcafClass() {
        return lcafClass;
    }

    public static LispCanonicalAddressFormatEnum valueOf(int lispCode) {
        for (LispCanonicalAddressFormatEnum val : values()) {
            if (val.getLispCode() == lispCode) {
                return val;
            }
        }
        return UNKNOWN;
    }
}
