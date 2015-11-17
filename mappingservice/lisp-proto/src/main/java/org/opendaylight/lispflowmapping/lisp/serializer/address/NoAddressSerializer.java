/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.NoAddressAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lisp.address.grouping.LispAddressContainerBuilder;

public class NoAddressSerializer extends LispAddressSerializer {

    private static final NoAddressSerializer INSTANCE = new NoAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private NoAddressSerializer() {
    }

    public static NoAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.NO;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
    }

    @Override
    protected LispAddress deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        LispAddressContainerBuilder lab = new LispAddressContainerBuilder();
        lab.setAddressType(NoAddressAfi.class);
        lab.setVirtualNetworkId(ctx.getVni());
        lab.setAddress(new NoAddressBuilder().setNoAddress(true).build());
        return lab.build();
    }

    private interface Length {
        int NO = 0;
    }

}
