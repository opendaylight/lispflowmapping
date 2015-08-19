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
import org.opendaylight.lispflowmapping.serializer.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSegmentAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsegmentaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddrBuilder;

public class LispSegmentLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "00 BB CC DD " + // instance ID
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafSegmentAddress segAddress = (LcafSegmentAddress) address;

        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68"), segAddress.getAddress().getPrimitiveAddress());
        assertEquals(0x00BBCCDD, segAddress.getInstanceId().intValue());
        assertEquals(LispCanonicalAddressFormatEnum.SEGMENT.getLispCode(), segAddress.getLcafType().byteValue());
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
                "00 BB CC DD " + // instance ID
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__LongInstanceID() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + // Type AA is unknown
                "AA BB CC DD " + // instance ID
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LcafSegmentAddress segAddress = (LcafSegmentAddress) LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "00 BB CC DD " + // instance ID
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd")), segAddress
                .getAddress().getPrimitiveAddress());

    }

    @Test
    public void serialize__Simple() throws Exception {
        LcafSegmentAddrBuilder addressBuilder = new LcafSegmentAddrBuilder();
        addressBuilder.setInstanceId((long) 0x00020304);
        addressBuilder.setIidMaskLength((short) 0);
        addressBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
        addressBuilder.setAddress(new AddressBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68")).build());
        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(addressBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, addressBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "02 00 00 0A " + //
                "00 02 03 04 " + // instance ID
                "00 01 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

}
