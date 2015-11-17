/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lisp.address.grouping.LispAddressContainerBuilder;

public class DistinguishedNameSerializer extends LispAddressSerializer {

    private static final DistinguishedNameSerializer INSTANCE = new DistinguishedNameSerializer();

    // Private constructor prevents instantiation from other classes
    private DistinguishedNameSerializer() {
    }

    public static DistinguishedNameSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return ((DistinguishedName) lispAddress.getAddress()).getDistinguishedName().getValue().length() + 1;

    }

    @Override
    protected LispAddress deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        StringBuilder sb = new StringBuilder();
        byte b = buffer.get();
        while (b != 0) {
            sb.append((char) b);
            b = buffer.get();
        }
        LispAddressContainerBuilder lab = new LispAddressContainerBuilder();
        lab.setAddressType(DistinguishedNameAfi.class);
        lab.setVirtualNetworkId(ctx.getVni());
        lab.setAddress(new DistinguishedNameBuilder().setDistinguishedName(new DistinguishedNameType(sb.toString())).build());
        return lab.build();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        DistinguishedName distinguishedNameAddress = (DistinguishedName) lispAddress.getAddress();
        buffer.put(distinguishedNameAddress.getDistinguishedName().getValue().getBytes());
        buffer.put((byte) 0);
    }

}
