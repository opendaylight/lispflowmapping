/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.address.factory.LispLCAFAddressSerializerFactory;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispLcafAddress;

public class LispLCAFAddressSerializer extends LispAddressSerializer {

    private static final LispLCAFAddressSerializer INSTANCE = new LispLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    protected LispLCAFAddressSerializer() {
    }

    public static LispLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected LispLcafAddress deserializeData(ByteBuffer buffer) {
        buffer.position(buffer.position() + Length.RES + Length.FLAGS);
        byte lispCode = (byte) ByteUtil.getUnsignedByte(buffer);
        LispCanonicalAddressFormatEnum lcafType = LispCanonicalAddressFormatEnum.valueOf(lispCode);
        byte res2 = buffer.get();
        short length = buffer.getShort();

        LispLCAFAddressSerializer serializer = LispLCAFAddressSerializerFactory.getLCAFSerializer(lcafType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown LispLCAFAddress type=" + lispCode);
        }
        return serializer.deserializeData(buffer, res2, length);
    }

    protected LispLcafAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return Length.LCAF_HEADER
                + LispLCAFAddressSerializerFactory.getLCAFSerializer(
                        LispCanonicalAddressFormatEnum.valueOf(((LispLcafAddress) lispAddress).getLcafType())).getLcafLength(lispAddress);
    }

    protected short getLcafLength(LispAFIAddress lispAddress) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        serializeLCAFAddressHeader(buffer, lispAddress);

        LispLCAFAddressSerializer lcafSerializer = LispLCAFAddressSerializerFactory.getLCAFSerializer(LispCanonicalAddressFormatEnum
                .valueOf(((LispLcafAddress) lispAddress).getLcafType()));
        lcafSerializer.serializeData(buffer, lispAddress);
    }

    private void serializeLCAFAddressHeader(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispLcafAddress lispLcafAddress = (LispLcafAddress) lispAddress;
        buffer.putShort((short) 0); // RES + Flags.
        buffer.put(lispLcafAddress.getLcafType().byteValue());
        buffer.put((byte) 0);
        LispLCAFAddressSerializer lcafSerializer = LispLCAFAddressSerializerFactory.getLCAFSerializer(LispCanonicalAddressFormatEnum
                .valueOf(lispLcafAddress.getLcafType()));
        buffer.putShort(lcafSerializer.getLcafLength(lispAddress));
    }

    private interface Length {
        int RES = 1;
        int FLAGS = 1;

        int LCAF_HEADER = 6;
    }
}
