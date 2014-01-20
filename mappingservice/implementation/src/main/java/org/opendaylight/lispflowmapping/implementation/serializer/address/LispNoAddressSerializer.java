/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispNoAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.NoBuilder;

public class LispNoAddressSerializer extends LispAddressSerializer {

    private static final LispNoAddressSerializer INSTANCE = new LispNoAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispNoAddressSerializer() {
    }

    public static LispNoAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return Length.NO;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
    }

    @Override
    protected LispNoAddress deserializeData(ByteBuffer buffer) {
        return new NoBuilder().setAfi(AddressFamilyNumberEnum.NO_ADDRESS.getIanaCode()).build();
    }

    private interface Length {
        int NO = 0;
    }

}
