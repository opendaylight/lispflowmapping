/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.address.factory.LispAFIAddressSerializerFactory;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;

public class LispAddressSerializer {

    private static final LispAddressSerializer INSTANCE = new LispAddressSerializer();

    // Private constructor prevents instantiation from other classes
    protected LispAddressSerializer() {
    }

    public static LispAddressSerializer getInstance() {
        return INSTANCE;
    }

    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        throw new RuntimeException("UnImplemented");
    }

    protected LispAFIAddress deserializeData(ByteBuffer buffer) {
        throw new RuntimeException("UnImplemented");
    }

    public void serialize(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(AddressFamilyNumberEnum.valueOf(lispAddress.getAfi()));
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI type=" + lispAddress.getAfi());
        }
        serializeAFIAddressHeader(buffer, lispAddress);
        serializer.serializeData(buffer, lispAddress);
    }

    protected static void serializeAFIAddressHeader(ByteBuffer buffer, LispAFIAddress lispAddress) {
        buffer.putShort(lispAddress.getAfi());
    }

    public int getAddressSize(LispAFIAddress lispAddress) {
        AddressFamilyNumberEnum afiType = AddressFamilyNumberEnum.valueOf(lispAddress.getAfi());
        LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(afiType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI type=" + afiType);
        }
        return Length.AFI + serializer.getAddressSize(lispAddress);
    }

    public LispAFIAddress deserialize(ByteBuffer buffer) {
        short afi = buffer.getShort();
        AddressFamilyNumberEnum afiType = AddressFamilyNumberEnum.valueOf(afi);
        LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(afiType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI type=" + afiType);
        }
        try {
            return serializer.deserializeData(buffer);
        } catch (RuntimeException e) {
            throw new LispSerializationException("Problem deserializing AFI=" + afiType, e);
        }
    }

    private interface Length {
        int AFI = 2;
    }

}
