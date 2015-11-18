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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

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
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        DistinguishedName distinguishedNameAddress = (DistinguishedName) lispAddress.getAddress();
        buffer.put(distinguishedNameAddress.getDistinguishedName().getValue().getBytes());
        buffer.put((byte) 0);
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(DistinguishedNameAfi.class);
        eb.setVirtualNetworkId(ctx.getVni());
        eb.setAddress(deserializeData(buffer));
        return eb.build();
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(DistinguishedNameAfi.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(deserializeData(buffer));
        return rb.build();
    }

    private Address deserializeData(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        byte b = buffer.get();
        while (b != 0) {
            sb.append((char) b);
            b = buffer.get();
        }
        return new DistinguishedNameBuilder().setDistinguishedName(new DistinguishedNameType(sb.toString())).build();
    }
}
