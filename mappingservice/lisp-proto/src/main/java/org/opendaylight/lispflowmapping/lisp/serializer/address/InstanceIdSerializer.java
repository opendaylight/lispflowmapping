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
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

public class InstanceIdSerializer extends LcafSerializer {

    private static final int MAX_INSTANCE_ID = 16777216;
    private static final InstanceIdSerializer INSTANCE = new InstanceIdSerializer();

    // Private constructor prevents instantiation from other classes
    private InstanceIdSerializer() {
    }

    public static InstanceIdSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        return (short) (Length.INSTANCE + LispAddressSerializer.getInstance().getAddressSize(lispAddress));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        InstanceId iid = (InstanceId) lispAddress.getAddress();
        // The IID mask-len field is in the LCAF header on the res2 position
        buffer.put(buffer.position() - 3, iid.getInstanceId().getMaskLength().byteValue());

        buffer.putInt(iid.getInstanceId().getIid().getValue().intValue());
        LispAddressSerializer.getInstance().serialize(buffer, lispAddress);
    }

    @Override
    protected Eid deserializeLcafEidData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        long instanceId = (int) ByteUtil.asUnsignedInteger(buffer.getInt());

        if (instanceId > MAX_INSTANCE_ID) {
            throw new LispSerializationException("Instance ID is longer than 24 bits: " + instanceId);
        }
        ctx.setVni(new InstanceIdType(instanceId));
        return LispAddressSerializer.getInstance().deserializeEid(buffer, ctx);
    }

    private interface Length {
        int INSTANCE = 4;
    }
}
