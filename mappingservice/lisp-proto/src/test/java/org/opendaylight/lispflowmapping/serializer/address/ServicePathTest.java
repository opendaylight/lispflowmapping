/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.serializer.address;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializerContext;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * @author Lorand Jakab
 *
 */
public class ServicePathTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " +
                "11 00 00 04 " +
                "AA BB CC FF"),
                new LispAddressSerializerContext(null));
        assertEquals(ServicePathLcaf.class, address.getAddressType());
        ServicePath sp = (ServicePath) address.getAddress();

        assertEquals(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }),
                sp.getServicePath().getServicePathId().getValue().intValue());
        assertEquals((byte) 0xFF, sp.getServicePath().getServiceIndex().byteValue());
    }

    @Test
    public void serialize__Simple() throws Exception {
        Eid eid = LispAddressUtil.asServicePathEid(-1, 1L, (short) 0xFF);

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(eid));
        LispAddressSerializer.getInstance().serialize(buf, eid);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " +
                "11 00 00 04 " +
                "00 00 01 FF");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
