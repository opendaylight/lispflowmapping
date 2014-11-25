/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.serializer.address;

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafKeyValueAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafkeyvalueaddress.KeyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafkeyvalueaddress.ValueBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafKeyValueBuilder;

public class LispKeyValueLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0F 20 00 0C " + //
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55")); // AFI=1, IP=0x22334455

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafKeyValueAddress srcDestAddress = (LcafKeyValueAddress) address;

        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68"), srcDestAddress.getKey().getPrimitiveAddress());
        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("34.51.68.85"), srcDestAddress.getValue().getPrimitiveAddress());
        assertEquals(LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode(), srcDestAddress.getLcafType().byteValue());
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
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55")); // AFI=1, IP=0x22334455
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LcafKeyValueAddress srcAddress = (LcafKeyValueAddress) LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0F 20 00 24 " + //
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD " + // AFI=2,
                "00 02 44 33 22 11 88 77 66 55 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd")), srcAddress.getKey()
                .getPrimitiveAddress());
        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("4433:2211:8877:6655:99aa:bbcc:aabb:ccdd")), srcAddress
                .getValue().getPrimitiveAddress());
    }

    @Test
    public void serialize__Simple() throws Exception {
        LcafKeyValueBuilder addressBuilder = new LcafKeyValueBuilder();
        addressBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode());
        addressBuilder.setKey(new KeyBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68")).build());
        addressBuilder.setValue(new ValueBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("34.51.68.85")).build());
        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(addressBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, addressBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0F 00 00 0C " + //
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"); // AFI=1, IP=0x22334455
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
