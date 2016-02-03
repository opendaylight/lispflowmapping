/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
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
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

public class InstanceIdSerializerTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "00 BB CC DD " + // instance ID
                "00 01 11 22 33 44"), // AFI=1, IP=0x11223344
                new LispAddressSerializerContext(null));

        assertEquals(Ipv4Afi.class, address.getAddressType());
        Ipv4 ipv4 = (Ipv4) address.getAddress();

        assertEquals("17.34.51.68", ipv4.getIpv4().getValue());
        assertEquals(0x00BBCCDD, address.getVirtualNetworkId().getValue().longValue());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "AA BB "),
                new LispAddressSerializerContext(null));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 0A " + // Type AA is unknown
                "00 BB CC DD " + // instance ID
                "00 01 11 22 33 44"), // AFI=1, IP=0x11223344
                new LispAddressSerializerContext(null));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__LongInstanceID() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + // Type AA is unknown
                "AA BB CC DD " + // instance ID
                "00 01 11 22 33 44"), // AFI=1, IP=0x11223344
                new LispAddressSerializerContext(null));
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "00 BB CC DD " + // instance ID
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD"), // AFI=2,
                new LispAddressSerializerContext(null));
        // IPv6

        assertEquals("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd", ((Ipv6) address.getAddress()).getIpv6().getValue());

    }

    @Test
    public void serialize__Simple() throws Exception {
        Eid eid = LispAddressUtil.asIpv4Eid("17.34.51.68", (long) 0x00020304);

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(eid));
        LispAddressSerializer.getInstance().serialize(buf, eid);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "00 02 03 04 " + // instance ID
                "00 01 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
