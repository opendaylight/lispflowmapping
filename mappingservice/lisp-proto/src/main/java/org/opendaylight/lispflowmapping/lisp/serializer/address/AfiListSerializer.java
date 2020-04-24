/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AfiListLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public final class AfiListSerializer extends LcafSerializer {
    private static final AfiListSerializer INSTANCE = new AfiListSerializer();

    // Private constructor prevents instantiation from other classes
    private AfiListSerializer() {
    }

    public static AfiListSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected byte getLcafType() {
        return LispCanonicalAddressFormatEnum.LIST.getLispCode();
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        short totalSize = 0;
        AfiList afiList = (((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.AfiList) lispAddress.getAddress()).getAfiList());
        for (SimpleAddress address : afiList.getAddressList()) {
            totalSize += SimpleAddressSerializer.getInstance().getAddressSize(address);
        }
        return totalSize;
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.LispCanonicalAddressFormat.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        AfiList afiList = (((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.AfiList) lispAddress.getAddress()).getAfiList());
        for (SimpleAddress address : afiList.getAddressList()) {
            SimpleAddressSerializer.getInstance().serialize(buffer, address);
        }
    }

    @Override
    public Eid deserializeLcafEidData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(AfiListLcaf.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(deserializeData(buffer, length, ctx));
        return eb.build();
    }

    @Override
    public Rloc deserializeLcafRlocData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(AfiListLcaf.class);
        rb.setVirtualNetworkId(getVni(ctx));
        rb.setAddress(deserializeData(buffer, length, ctx));
        return rb.build();
    }

    private Address deserializeData(ByteBuffer buffer, short lcafLength, LispAddressSerializerContext ctx) {
        List<SimpleAddress> addresses = new ArrayList<SimpleAddress>();
        short length = lcafLength;
        while (length > 0) {
            SimpleAddress address = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
            length -= SimpleAddressSerializer.getInstance().getAddressSize(address);
            addresses.add(address);
        }
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp
                .address.address.AfiListBuilder().setAfiList(new AfiListBuilder().setAddressList(addresses).build())
                .build();
    }
}
