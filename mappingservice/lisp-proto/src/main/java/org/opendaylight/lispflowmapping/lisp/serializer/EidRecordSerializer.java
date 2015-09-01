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
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecordBuilder;

public class EidRecordSerializer {

    private static final EidRecordSerializer INSTANCE = new EidRecordSerializer();

    // Private constructor prevents instantiation from other classes
    private EidRecordSerializer() {
    }

    public static EidRecordSerializer getInstance() {
        return INSTANCE;
    }

    public EidRecord deserialize(ByteBuffer requestBuffer) {
        /* byte reserved = */requestBuffer.get();
        short maskLength = (short) (ByteUtil.getUnsignedByte(requestBuffer));
        LispAFIAddress prefix = LispAddressSerializer.getInstance().deserialize(requestBuffer);
        prefix = MaskUtil.fixMask(prefix, maskLength);
        return new EidRecordBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(prefix)).setMask(maskLength)
                .build();
    }
}
