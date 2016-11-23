/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.type;

/**
 * The LCAF enum. http://tools.ietf.org/html/draft-ietf-lisp-lcaf-03
 */
public enum LispCanonicalAddressFormatEnum {
    LIST(1),
    SEGMENT(2),
    APPLICATION_DATA(4),
    SOURCE_DEST(12),
    TRAFFIC_ENGINEERING(10),
    KEY_VALUE(15),
    SERVICE_PATH(17),
    UNKNOWN(-1);

    private byte lispCode;

    LispCanonicalAddressFormatEnum(int lispCode) {
        this.lispCode = (byte) lispCode;
    }

    public byte getLispCode() {
        return lispCode;
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
