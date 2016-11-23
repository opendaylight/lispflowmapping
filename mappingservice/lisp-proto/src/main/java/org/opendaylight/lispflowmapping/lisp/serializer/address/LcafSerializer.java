/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.address.factory.LispAddressSerializerFactory;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.AddressTypeMap;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class LcafSerializer extends LispAddressSerializer {

    private static final LcafSerializer INSTANCE = new LcafSerializer();
    private static final byte DEFAULT_IID_MASK_LENGTH = (byte) 32;

    // Private constructor prevents instantiation from other classes
    protected LcafSerializer() {
    }

    public static LcafSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.LCAF_HEADER
                + LispAddressSerializerFactory.getSerializer(lispAddress.getAddressType()).getLcafLength(lispAddress);
    }

    protected int getLcafHeaderSize() {
        return Length.LCAF_HEADER;
    }

    protected short getLcafLength(LispAddress lispAddress) {
        throw new LispSerializationException("Unimplemented method");
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.LispCanonicalAddressFormat.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        LispAddressSerializer lcafSerializer = LispAddressSerializerFactory.getSerializer(lispAddress.getAddressType());
        serializeLCAFAddressHeader(buffer, lispAddress, lcafSerializer);
        buffer.putShort(lcafSerializer.getLcafLength(lispAddress));
        lcafSerializer.serializeData(buffer, lispAddress);
    }

    protected void serializeLCAFAddressHeaderForInstanceId(ByteBuffer buffer, LispAddress lispAddress) {
        LispAddressSerializer lcafSerializer = InstanceIdSerializer.getInstance();
        serializeLCAFAddressHeader(buffer, lispAddress, lcafSerializer);
        buffer.put(buffer.position() - 1, DEFAULT_IID_MASK_LENGTH);
        buffer.putShort((short) (lcafSerializer.getLcafLength(lispAddress)
                - LispAddressSerializer.getInstance().getInstanceIdExtraSize()));
    }

    private void serializeLCAFAddressHeader(ByteBuffer buffer, LispAddress lispAddress,
            LispAddressSerializer serializer) {
        // RES + Flags
        buffer.putShort((short) 0);
        buffer.put(serializer.getLcafType());
        buffer.put((byte) 0);
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        buffer.position(buffer.position() + Length.RES + Length.FLAGS);
        byte lcafType = (byte) ByteUtil.getUnsignedByte(buffer);
        Class<? extends LispAddressFamily> addressType = AddressTypeMap.getLcafType(lcafType);
        // TODO move these to ctx to shorten the list of arguments
        byte res2 = buffer.get();
        short length = buffer.getShort();

        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(addressType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown LCAF type: " + lcafType);
        }
        // Reset the mask context here, since the general mask length field in mapping records doesn't apply to LCAF
        // address types; except for Instance ID, since we don't store it as an LCAF
        if (ctx != null && addressType != InstanceIdLcaf.class) {
            ctx.setMaskLen(LispAddressSerializerContext.MASK_LEN_MISSING);
        }
        return serializer.deserializeLcafEidData(buffer, res2, length, ctx);
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        buffer.position(buffer.position() + Length.RES + Length.FLAGS);
        byte lcafType = (byte) ByteUtil.getUnsignedByte(buffer);
        Class<? extends LispAddressFamily> addressType = AddressTypeMap.getLcafType(lcafType);
        byte res2 = buffer.get();
        short length = buffer.getShort();

        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(addressType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown LCAF type: " + lcafType);
        }
        return serializer.deserializeLcafRlocData(buffer, res2, length, null);
    }

    private interface Length {
        int RES = 1;
        int FLAGS = 1;

        int LCAF_HEADER = 6;
    }
}
