/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.KeyValueAddressLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.key.value.address.KeyValueAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public final class KeyValueAddressSerializer extends LcafSerializer {

    private static final KeyValueAddressSerializer INSTANCE = new KeyValueAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private KeyValueAddressSerializer() {
    }

    public static KeyValueAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected byte getLcafType() {
        return LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode();
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        KeyValueAddress kva = (KeyValueAddress) lispAddress.getAddress();
        return (short) (SimpleAddressSerializer.getInstance().getAddressSize(kva.getKeyValueAddress().getKey())
                + SimpleAddressSerializer.getInstance().getAddressSize(kva.getKeyValueAddress().getValue()));
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.LispCanonicalAddressFormat.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        KeyValueAddress kva = (KeyValueAddress) lispAddress.getAddress();
        SimpleAddressSerializer.getInstance().serialize(buffer, kva.getKeyValueAddress().getKey());
        SimpleAddressSerializer.getInstance().serialize(buffer, kva.getKeyValueAddress().getValue());
    }

    @Override
    protected Eid deserializeLcafEidData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(KeyValueAddressLcaf.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(deserializeData(buffer, ctx));
        return eb.build();
    }

    @Override
    protected Rloc deserializeLcafRlocData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(KeyValueAddressLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(deserializeData(buffer, ctx));
        return rb.build();
    }

    private Address deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        SimpleAddress keyAddress = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
        SimpleAddress valueAddress = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
        KeyValueAddressBuilder kvab = new KeyValueAddressBuilder();
        kvab.setKey(keyAddress);
        kvab.setValue(valueAddress);
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddressBuilder()
                .setKeyValueAddress(kvab.build()).build();
    }
}
