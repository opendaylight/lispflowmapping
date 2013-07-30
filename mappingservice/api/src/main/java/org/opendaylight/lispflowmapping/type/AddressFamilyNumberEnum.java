/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type;

import org.opendaylight.lispflowmapping.type.lisp.address.LispASAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispMACAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;

public enum AddressFamilyNumberEnum {
    RESERVED(0, LispNoAddress.class), //
    IP(1, LispIpv4Address.class), //
    IP6(2, LispIpv6Address.class), //
    AS(18, LispASAddress.class), //
    LCAF(16387, LispLCAFAddress.class), //
    MAC(16389, LispMACAddress.class), //
    UNKNOWN(-1, null);

    private short ianaCode;
    private Class<? extends LispAddress> lispAddressClass;

    private AddressFamilyNumberEnum(int ianaCode, Class<? extends LispAddress> lispAddressClass) {
        this.ianaCode = (short) ianaCode;
        this.lispAddressClass = lispAddressClass;
    }

    public Class<? extends LispAddress> getLispAddressClass() {
        return lispAddressClass;
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
