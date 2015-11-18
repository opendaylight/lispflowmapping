/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.KeyValueAddressLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.key.value.address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.key.value.address.KeyValueAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lisp.address.grouping.LispAddressContainerBuilder;

public class KeyValueAddressSerializer extends LcafSerializer {

    private static final KeyValueAddressSerializer INSTANCE = new KeyValueAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private KeyValueAddressSerializer() {
    }

    public static KeyValueAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        KeyValueAddress kva = (KeyValueAddress) lispAddress.getAddress();
        return (short) (SimpleAddressSerializer.getInstance().getAddressSize(kva.getKey())
                + SimpleAddressSerializer.getInstance().getAddressSize(kva.getValue()));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        KeyValueAddress kva = (KeyValueAddress) lispAddress.getAddress();
        SimpleAddressSerializer.getInstance().serialize(buffer, kva.getKey());
        SimpleAddressSerializer.getInstance().serialize(buffer, kva.getValue());
    }

    @Override
    protected LispAddress deserializeLcafData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        SimpleAddress keyAddress = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
        SimpleAddress valueAddress = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
        KeyValueAddressBuilder kvab = new KeyValueAddressBuilder();
        kvab.setKey(keyAddress);
        kvab.setValue(valueAddress);
        LispAddressContainerBuilder lab = new LispAddressContainerBuilder();
        lab.setAddressType(KeyValueAddressLcaf.class);
        lab.setVirtualNetworkId(ctx.getVni());
        lab.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddressBuilder()
                .setKeyValueAddress(kvab.build()).build());
        return lab.build();
    }

}
