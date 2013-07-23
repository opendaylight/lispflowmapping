/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public class LispNoAddress extends LispAddress {
    public LispNoAddress() {
        super(AddressFamilyNumberEnum.RESERVED);
    }

    public static LispNoAddress valueOf(ByteBuffer buffer) {
        return new LispNoAddress();
    }

    @Override
    public int getAddressSize() {
        return super.getAddressSize();
    }

    @Override
    public void serialize(ByteBuffer buffer) {
    }
}
