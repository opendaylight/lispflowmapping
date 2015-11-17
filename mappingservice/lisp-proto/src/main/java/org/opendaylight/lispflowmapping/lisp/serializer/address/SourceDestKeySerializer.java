/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lisp.address.grouping.LispAddressContainerBuilder;

public class SourceDestKeySerializer extends LcafSerializer {

    private static final SourceDestKeySerializer INSTANCE = new SourceDestKeySerializer();

    // Private constructor prevents instantiation from other classes
    private SourceDestKeySerializer() {
    }

    public static SourceDestKeySerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        SourceDestKey sdk = (SourceDestKey) lispAddress.getAddress();
        return (short) (Length.ALL_FIELDS
                + IpPrefixSerializer.getInstance().getAddressSize(sdk.getSourceDestKey().getSource())
                + IpPrefixSerializer.getInstance().getAddressSize(sdk.getSourceDestKey().getDest()));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        SourceDestKey sdk = (SourceDestKey) lispAddress.getAddress();
        buffer.putShort((short) 0);
        buffer.put((byte) MaskUtil.getMaskForIpPrefix(sdk.getSourceDestKey().getSource()));
        buffer.put((byte) MaskUtil.getMaskForIpPrefix(sdk.getSourceDestKey().getDest()));
        IpPrefixSerializer.getInstance().serialize(buffer, sdk.getSourceDestKey().getSource());
        IpPrefixSerializer.getInstance().serialize(buffer, sdk.getSourceDestKey().getDest());
    }

    @Override
    protected LispAddress deserializeLcafData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        short res = buffer.getShort();
        short srcMaskLength = (short) ByteUtil.getUnsignedByte(buffer);
        ctx.setMaskLen(srcMaskLength);
        SimpleAddress srcAddress = IpPrefixSerializer.getInstance().deserialize(buffer, ctx);
        short dstMaskLength = (short) ByteUtil.getUnsignedByte(buffer);
        ctx.setMaskLen(dstMaskLength);
        SimpleAddress dstAddress = IpPrefixSerializer.getInstance().deserialize(buffer, ctx);
        SourceDestKeyBuilder sdb = new SourceDestKeyBuilder();
        sdb.setSource(srcAddress.getIpPrefix());
        sdb.setDest(dstAddress.getIpPrefix());
        LispAddressContainerBuilder lab = new LispAddressContainerBuilder();
        lab.setAddressType(SourceDestKeyLcaf.class);
        lab.setVirtualNetworkId(ctx.getVni());
        lab.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder()
                .setSourceDestKey(sdb.build()).build());
        return lab.build();
    }

    private interface Length {
        int SOURCE_MASK_LENGTH = 1;
        int DEST_MASK_LENGTH = 1;
        int RESERVED = 2;
        int ALL_FIELDS = SOURCE_MASK_LENGTH + DEST_MASK_LENGTH + RESERVED;
    }
}
