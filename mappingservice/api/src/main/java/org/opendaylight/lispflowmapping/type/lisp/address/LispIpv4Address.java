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

public class LispIpv4Address extends LispIPAddress {
    
    public LispIpv4Address(InetAddress address) {
        super(address, AddressFamilyNumberEnum.IP);
    }

    public LispIpv4Address(int address) {
        super(address, AddressFamilyNumberEnum.IP);
    }

    public LispIpv4Address(String name) {
        super(name, AddressFamilyNumberEnum.IP);
    }
    
    public static LispIpv4Address valueOf(String substring) {
        return new LispIpv4Address(substring);
    }


    @Override
    protected int getMaxMask() {
        return 32;
    }


}
