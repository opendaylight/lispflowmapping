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
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafSourceDestAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;

public class LispSourceDestLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0C 20 00 10 " + //
                "00 00 10 18 " + // reserved + masks
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55")); // AFI=1, IP=0x22334455

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafSourceDestAddress srcDestAddress = (LcafSourceDestAddress) address;

        assertEquals((byte) 0x10, srcDestAddress.getSrcMaskLength().byteValue());
        assertEquals((byte) 0x18, srcDestAddress.getDstMaskLength().byteValue());

        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68"), srcDestAddress.getSrcAddress().getPrimitiveAddress());
        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("34.51.68.85"), srcDestAddress.getDstAddress().getPrimitiveAddress());
        assertEquals(LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode(), srcDestAddress.getLcafType().byteValue());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "AA BB "));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 0A " + // Type AA is unknown
                "00 00 CC DD " + // reserved + masks
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55")); // AFI=1, IP=0x22334455
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LcafSourceDestAddress srcAddress = (LcafSourceDestAddress) LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0C 20 00 28 " + //
                "00 00 CC DD " + // reserved + masks
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD " + // AFI=2,
                "00 02 44 33 22 11 88 77 66 55 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd")), srcAddress
                .getSrcAddress().getPrimitiveAddress());
        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("4433:2211:8877:6655:99aa:bbcc:aabb:ccdd")), srcAddress
                .getDstAddress().getPrimitiveAddress());
    }

    @Test
    public void serialize__Simple() throws Exception {
        LcafSourceDestAddrBuilder addressBuilder = new LcafSourceDestAddrBuilder();
        addressBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType(
                (short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());
        addressBuilder.setSrcMaskLength((short) 0xCC);
        addressBuilder.setDstMaskLength((short) 0xDD);
        addressBuilder.setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68")).build());
        addressBuilder.setDstAddress(new DstAddressBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("34.51.68.85")).build());
        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(addressBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, addressBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0C 00 00 10 " + //
                "00 00 CC DD " + // reserved + masks
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"); // AFI=1, IP=0x22334455
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
