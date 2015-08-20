/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.type;

/**
 * The AFI enum:
 * http://www.iana.org/assignments/address-family-numbers/address-family
 * -numbers.xhtml
 */
public enum AddressFamilyNumberEnum {
    NO_ADDRESS(0), //
    IP(1), //
    IP6(2), //
    DISTINGUISHED_NAME(17), //
    AS(18), //
    LCAF(16387), //
    MAC(16389), //
    UNKNOWN(-1);

    private short ianaCode;

    private AddressFamilyNumberEnum(int ianaCode) {
        this.ianaCode = (short) ianaCode;
    }

    public short getIanaCode() {
        return ianaCode;
    }

    public static AddressFamilyNumberEnum valueOf(short ianaCode) {
        for (AddressFamilyNumberEnum val : values()) {
            if (val.getIanaCode() == ianaCode) {
                return val;
            }
        }
        return UNKNOWN;
    }
}
