/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializerContext.AddressContext;
import org.opendaylight.lispflowmapping.lisp.serializer.address.factory.LispAddressSerializerFactory;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.AddressTypeMap;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;

public class LispAddressSerializer {

    private static final LispAddressSerializer INSTANCE = new LispAddressSerializer();
    public static final InstanceIdType DEFAULT_VNI = new InstanceIdType(0L);
    public static final LispAddressSerializerContext DEFAULT_EID_CTX =
            new LispAddressSerializerContext(DEFAULT_VNI, AddressContext.EID);
    public static final LispAddressSerializerContext DEFAULT_RLOC_CTX =
            new LispAddressSerializerContext(AddressContext.RLOC);

    // Private constructor prevents instantiation from other classes
    protected LispAddressSerializer() {
    }

    public static LispAddressSerializer getInstance() {
        return INSTANCE;
    }

    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        throw new RuntimeException("Unimplemented");
    }

    protected void serializeData(ByteBuffer buffer, SimpleAddress lispAddress) {
        throw new RuntimeException("Unimplemented");
    }

    protected void serializeData(ByteBuffer buffer, IpPrefix lispAddress) {
        throw new RuntimeException("Unimplemented");
    }

    protected LispAddress deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        throw new RuntimeException("Unimplemented");
    }

    protected LispAddress deserializeLcafData(ByteBuffer buffer, byte res2, short length,
            LispAddressSerializerContext ctx) {
        throw new RuntimeException("Unimplemented");
    }

    protected short getAfi() {
        throw new RuntimeException("Unimplemented");
    }

    protected byte getLcafType() {
        throw new RuntimeException("Unimplemented");
    }

    protected short getLcafLength(LispAddress lispAddress) {
        throw new RuntimeException("Unimplemented");
    }

    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(lispAddress.getAddressType());
        if (serializer == null) {
            throw new LispSerializationException("Unknown address type: " + lispAddress.getAddressType().getSimpleName());
        }
        buffer.putShort(serializer.getAfi());
        serializer.serializeData(buffer, lispAddress);
    }

    public int getAddressSize(LispAddress lispAddress) {
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(lispAddress.getAddressType());
        if (serializer == null) {
            throw new LispSerializationException("Unknown address type: " + lispAddress.getAddressType().getSimpleName());
        }
        return Length.AFI + serializer.getAddressSize(lispAddress);
    }

    public LispAddress deserialize(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        short afi = buffer.getShort();
        Class <? extends LispAddressFamily> addressType = AddressTypeMap.getAddressType(afi, ctx.getAddrCtx());
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(addressType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI: " + afi);
        }
        try {
            return serializer.deserializeData(buffer, ctx);
        } catch (RuntimeException e) {
            throw new LispSerializationException("Problem deserializing AFI " + afi, e);
        }
    }

    private interface Length {
        int AFI = 2;
    }

}
