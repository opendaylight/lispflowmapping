/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.serializer.address.factory.LispLCAFAddressSerializerFactory;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispLCAFAddress;

public class LispLCAFAddressSerializer extends LispAddressSerializer {

    private static final LispLCAFAddressSerializer INSTANCE = new LispLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    protected LispLCAFAddressSerializer() {
    }

    public static LispLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected LispLCAFAddress deserializeData(ByteBuffer buffer) {
        buffer.position(buffer.position() + Length.RES + Length.FLAGS);
        byte lispCode = buffer.get();
        LispCanonicalAddressFormatEnum lcafType = LispCanonicalAddressFormatEnum.valueOf(lispCode);
        byte res2 = buffer.get();
        short length = buffer.getShort();

        LispLCAFAddressSerializer serializer = LispLCAFAddressSerializerFactory.getLCAFSerializer(lcafType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown LispLCAFAddress type=" + lispCode);
        }
        return serializer.deserializeData(buffer, res2, length);
    }

    protected LispLCAFAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.LCAF_HEADER
                + LispLCAFAddressSerializerFactory.getLCAFSerializer(((LispLCAFAddress) lispAddress).getType()).getLcafLength(lispAddress);
    }

    protected short getLcafLength(LispAddress lispAddress) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        serializeLCAFAddressHeader(buffer, lispAddress);
        
        LispLCAFAddressSerializer lcafSerializer = LispLCAFAddressSerializerFactory.getLCAFSerializer(((LispLCAFAddress) lispAddress).getType());
        lcafSerializer.serializeData(buffer, lispAddress);
    }

    private void serializeLCAFAddressHeader(ByteBuffer buffer, LispAddress lispAddress) {
        LispLCAFAddress lispLCAFAddress = (LispLCAFAddress) lispAddress;
        buffer.putShort((short) 0); // RES + Flags.
        buffer.put(lispLCAFAddress.getType().getLispCode());
        buffer.put(lispLCAFAddress.getRes2());
        LispLCAFAddressSerializer lcafSerializer = LispLCAFAddressSerializerFactory.getLCAFSerializer(lispLCAFAddress.getType());
        buffer.putShort(lcafSerializer.getLcafLength(lispAddress));
    }

    private interface Length {
        int RES = 1;
        int FLAGS = 1;

        int LCAF_HEADER = 6;
    }
}
