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
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.LcafSourceDestAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispsimpleaddress.PrimitiveAddress;

public class LispSourceDestLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispSourceDestLCAFAddressSerializer INSTANCE = new LispSourceDestLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispSourceDestLCAFAddressSerializer() {
    }

    public static LispSourceDestLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        return (short) (Length.ALL_FIELDS
                + LispAddressSerializer.getInstance().getAddressSize(
                        LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddress) lispAddress).getSrcAddress().getPrimitiveAddress())) + LispAddressSerializer
                .getInstance().getAddressSize(LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddress) lispAddress).getDstAddress().getPrimitiveAddress())));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LcafSourceDestAddress lispSourceDestLCAFAddress = ((LcafSourceDestAddress) lispAddress);
        buffer.putShort((short) 0);
        buffer.put(lispSourceDestLCAFAddress.getSrcMaskLength().byteValue());
        buffer.put(lispSourceDestLCAFAddress.getDstMaskLength().byteValue());
        LispAddressSerializer.getInstance().serialize(buffer, LispAFIConvertor.toAFIfromPrimitive(lispSourceDestLCAFAddress.getSrcAddress().getPrimitiveAddress()));
        LispAddressSerializer.getInstance().serialize(buffer, LispAFIConvertor.toAFIfromPrimitive(lispSourceDestLCAFAddress.getDstAddress().getPrimitiveAddress()));
    }

    @Override
    protected LcafSourceDestAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        short res = buffer.getShort();
        short srcMaskLength = (short) ByteUtil.getUnsignedByte(buffer);
        short dstMaskLength = (short) ByteUtil.getUnsignedByte(buffer);
        LispAFIAddress srcAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        srcAddress = MaskUtil.fixMask(srcAddress, srcMaskLength);
        LispAFIAddress dstAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        dstAddress = MaskUtil.fixMask(dstAddress, dstMaskLength);
        LcafSourceDestAddrBuilder builder = new LcafSourceDestAddrBuilder();
        builder.setDstMaskLength((short) dstMaskLength).setSrcMaskLength((short) srcMaskLength);
        builder.setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(srcAddress)).build());
        builder.setDstAddress(new DstAddressBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(dstAddress)).build());
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());

        return builder.build();
    }

    private interface Length {
        int SOURCE_MASK_LENGTH = 1;
        int DEST_MASK_LENGTH = 1;
        int RESERVED = 2;
        int ALL_FIELDS = SOURCE_MASK_LENGTH + DEST_MASK_LENGTH + RESERVED;
    }
}
