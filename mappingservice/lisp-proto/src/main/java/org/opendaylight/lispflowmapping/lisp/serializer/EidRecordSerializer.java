/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializerContext;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

public final class EidRecordSerializer {

    private static final EidRecordSerializer INSTANCE = new EidRecordSerializer();

    // Private constructor prevents instantiation from other classes
    private EidRecordSerializer() {
    }

    public static EidRecordSerializer getInstance() {
        return INSTANCE;
    }

    public Eid deserialize(ByteBuffer requestBuffer) {
        /* byte reserved = */requestBuffer.get();
        short maskLength = (short) (ByteUtil.getUnsignedByte(requestBuffer));
        LispAddressSerializerContext ctx = new LispAddressSerializerContext(maskLength);
        return LispAddressSerializer.getInstance().deserializeEid(requestBuffer, ctx);
    }
}
