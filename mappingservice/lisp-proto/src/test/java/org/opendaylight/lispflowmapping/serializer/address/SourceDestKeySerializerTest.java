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
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class SourceDestKeySerializerTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "0C 20 00 10 " + //
                "00 00 10 18 " + // reserved + masks
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"),  // AFI=1, IP=0x22334455
                new LispAddressSerializerContext(null));

        assertEquals(SourceDestKeyLcaf.class, address.getAddressType());
        SourceDestKey srcDestAddress = (SourceDestKey) address.getAddress();

        assertEquals((byte) 0x10, MaskUtil.getMaskForIpPrefix(srcDestAddress.getSourceDestKey().getSource()));
        assertEquals((byte) 0x18, MaskUtil.getMaskForIpPrefix(srcDestAddress.getSourceDestKey().getDest()));

        assertEquals("17.34.51.68/16", String.valueOf(srcDestAddress.getSourceDestKey().getSource().getValue()));
        assertEquals("34.51.68.85/24", String.valueOf(srcDestAddress.getSourceDestKey().getDest().getValue()));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "AA BB "), new LispAddressSerializerContext(null));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 0A " + // Type AA is unknown
                "00 00 CC DD " + // reserved + masks
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"),  // AFI=1, IP=0x22334455
                new LispAddressSerializerContext(null));
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        Eid srcAddress = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "0C 20 00 28 " + //
                "00 00 78 78 " + // reserved + masks
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD " + // AFI=2,
                "00 02 44 33 22 11 88 77 66 55 99 AA BB CC AA BB CC DD"),  // AFI=2,
                new LispAddressSerializerContext(null));
        // IPv6

        assertEquals("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd/120", String.valueOf(
                ((SourceDestKey) srcAddress.getAddress()).getSourceDestKey().getSource().getValue()));
        assertEquals("4433:2211:8877:6655:99aa:bbcc:aabb:ccdd/120", String.valueOf(
                ((SourceDestKey) srcAddress.getAddress()).getSourceDestKey().getDest().getValue()));
    }

    @Test
    public void serialize__Simple() throws Exception {
        SourceDestKeyBuilder addressBuilder = new SourceDestKeyBuilder();
        addressBuilder.setSource(new IpPrefix(new Ipv4Prefix("17.34.51.68/8")));
        addressBuilder.setDest(new IpPrefix(new Ipv4Prefix("34.51.68.85/16")));

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(SourceDestKeyLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress((Address)
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder()
                .setSourceDestKey(addressBuilder.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(eb.build()));
        LispAddressSerializer.getInstance().serialize(buf, eb.build());

        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0C 00 00 10 " + //
                "00 00 08 10 " + // reserved + masks
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"); // AFI=1, IP=0x22334455
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
