/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafSegmentAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsegmentaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.PrimitiveAddress;

public class LispSegmentLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final int MAX_INSTANCE_ID = 16777216;
    private static final LispSegmentLCAFAddressSerializer INSTANCE = new LispSegmentLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispSegmentLCAFAddressSerializer() {
    }

    public static LispSegmentLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        return (short) (Length.INSTANCE + LispAddressSerializer.getInstance().getAddressSize(
                LispAFIConvertor.toAFIfromPrimitive(((LcafSegmentAddress) lispAddress).getAddress().getPrimitiveAddress())));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        // The IID mask-len field is in the LCAF header on the res2 position
        buffer.put(buffer.position() - 3, ((LcafSegmentAddress) lispAddress).getIidMaskLength().byteValue());

        buffer.putInt(((LcafSegmentAddress) lispAddress).getInstanceId().intValue());
        LispAddressSerializer.getInstance().serialize(buffer, LispAFIConvertor.toAFIfromPrimitive(((LcafSegmentAddress) lispAddress).getAddress().getPrimitiveAddress()));
    }

    @Override
    protected LcafSegmentAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        long instanceId = (int) ByteUtil.asUnsignedInteger(buffer.getInt());

        if (instanceId > MAX_INSTANCE_ID) {
            throw new LispSerializationException("Instance ID is longer than 24 bits. got " + instanceId);
        }
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
        LcafSegmentAddrBuilder builder = new LcafSegmentAddrBuilder();
        builder.setInstanceId(instanceId);
        builder.setIidMaskLength((short) res2);
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.SEGMENT.getLispCode())
                .setAddress(new AddressBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(address)).build());

        return builder.build();
    }

    private interface Length {
        int INSTANCE = 4;
    }
}
