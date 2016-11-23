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
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public final class SourceDestKeySerializer extends LcafSerializer {

    private static final SourceDestKeySerializer INSTANCE = new SourceDestKeySerializer();

    // Private constructor prevents instantiation from other classes
    private SourceDestKeySerializer() {
    }

    public static SourceDestKeySerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected byte getLcafType() {
        return LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode();
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        SourceDestKey sdk = (SourceDestKey) lispAddress.getAddress();
        return (short) (Length.ALL_FIELDS
                + SimpleAddressSerializer.getInstance().getAddressSize(
                        new SimpleAddress(sdk.getSourceDestKey().getSource()))
                + SimpleAddressSerializer.getInstance().getAddressSize(
                        new SimpleAddress(sdk.getSourceDestKey().getDest())));
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.LispCanonicalAddressFormat.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        SourceDestKey sdk = (SourceDestKey) lispAddress.getAddress();
        buffer.putShort((short) 0);
        short srcMaskLength = MaskUtil.getMaskForAddress(sdk.getSourceDestKey().getSource());
        short dstMaskLength = MaskUtil.getMaskForAddress(sdk.getSourceDestKey().getDest());
        // TODO need to use LispAddressSerializerContext.MASK_LEN_MISSING everywhere instead of -1 but move that from
        // LispAddressSerializerContext to some more generic place, maybe
        // org.opendaylight.lispflowmammping.type.Constants
        if (srcMaskLength == -1) {
            srcMaskLength = 0;
        }
        if (dstMaskLength == -1) {
            dstMaskLength = 0;
        }
        buffer.put((byte) srcMaskLength);
        buffer.put((byte) dstMaskLength);
        SimpleAddressSerializer.getInstance().serialize(buffer, new SimpleAddress(sdk.getSourceDestKey().getSource()));
        SimpleAddressSerializer.getInstance().serialize(buffer, new SimpleAddress(sdk.getSourceDestKey().getDest()));
    }

    @Override
    protected Eid deserializeLcafEidData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(SourceDestKeyLcaf.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(deserializeData(buffer, ctx));
        return eb.build();
    }

    @Override
    protected Rloc deserializeLcafRlocData(ByteBuffer buffer, byte res2, short length,
            LispAddressSerializerContext ctx) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(SourceDestKeyLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(deserializeData(buffer, ctx));
        return rb.build();
    }

    private Address deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        // reserved bytes
        buffer.getShort();

        short srcMaskLength = (short) ByteUtil.getUnsignedByte(buffer);
        short dstMaskLength = (short) ByteUtil.getUnsignedByte(buffer);
        ctx.setMaskLen(srcMaskLength);
        SimpleAddress srcAddress = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
        ctx.setMaskLen(dstMaskLength);
        SimpleAddress dstAddress = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
        SourceDestKeyBuilder sdb = new SourceDestKeyBuilder();
        sdb.setSource(srcAddress);
        sdb.setDest(dstAddress);
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.SourceDestKeyBuilder().setSourceDestKey(sdb.build()).build();
    }

    private interface Length {
        int SOURCE_MASK_LENGTH = 1;
        int DEST_MASK_LENGTH = 1;
        int RESERVED = 2;
        int ALL_FIELDS = SOURCE_MASK_LENGTH + DEST_MASK_LENGTH + RESERVED;
    }
}
