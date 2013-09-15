/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.net.InetAddress;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public class LispIpv6Address extends LispIPAddress {

    public static final LispIpv6Address IP_ALL_ONES = null;

    public LispIpv6Address(InetAddress address) {
        super(address, AddressFamilyNumberEnum.IP6);
    }

    public LispIpv6Address(byte[] address) {
        super(address, AddressFamilyNumberEnum.IP6);
    }

    public LispIpv6Address(String name) {
        super(name, AddressFamilyNumberEnum.IP6);
    }

    public static LispIpv6Address valueOf(String substring) {
        return new LispIpv6Address(substring);
    }

    @Override
    protected int getMaxMask() {
        return 128;
    }
    

}
