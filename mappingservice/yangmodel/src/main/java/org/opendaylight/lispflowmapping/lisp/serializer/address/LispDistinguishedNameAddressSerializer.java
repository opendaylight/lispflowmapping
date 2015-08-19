/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispDistinguishedNameAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedNameBuilder;

public class LispDistinguishedNameAddressSerializer extends LispAddressSerializer {

    private static final LispDistinguishedNameAddressSerializer INSTANCE = new LispDistinguishedNameAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispDistinguishedNameAddressSerializer() {
    }

    public static LispDistinguishedNameAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return ((LispDistinguishedNameAddress) lispAddress).getDistinguishedName().length() + 1;

    }

    @Override
    protected LispDistinguishedNameAddress deserializeData(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        byte b = buffer.get();
        while (b != 0) {
            sb.append((char) b);
            b = buffer.get();
        }
        return new DistinguishedNameBuilder().setAfi(AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode()).setDistinguishedName((sb.toString()))
                .build();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) lispAddress;
        buffer.put(distinguishedNameAddress.getDistinguishedName().getBytes());
        buffer.put((byte) 0);
    }

}
