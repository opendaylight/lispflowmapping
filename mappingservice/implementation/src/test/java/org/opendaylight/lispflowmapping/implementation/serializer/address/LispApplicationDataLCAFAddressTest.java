/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
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
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispAFIConvertor;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafapplicationdataaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafApplicationDataBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;

public class LispApplicationDataLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0A " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 FF DD " + // local port & remote port
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafApplicationDataAddress appAddress = (LcafApplicationDataAddress) address;

        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68"), appAddress.getAddress().getPrimitiveAddress());
        assertEquals(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }), appAddress.getIpTos().intValue());
        assertEquals((byte) 0xDD, appAddress.getProtocol().byteValue());
        assertEquals((short) 0xA6A1, appAddress.getLocalPort().getValue().shortValue());
        assertEquals((short) 0xFFDD, appAddress.getRemotePort().getValue().shortValue());
        assertEquals(LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode(), appAddress.getLcafType().byteValue());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0A " + //
                "AA BB "));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 0A " + // Type AA is unknown
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 FF DD " + // local port & remote port
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LcafApplicationDataAddress appAddress = (LcafApplicationDataAddress) LispAddressSerializer.getInstance().deserialize(
                hexToByteBuffer("40 03 00 00 " + //
                        "04 20 00 0A " + //
                        "AA BB CC DD " + // IPTOS & protocol
                        "A6 A1 FF DD " + // local port & remote port
                        "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd")), appAddress
                .getAddress().getPrimitiveAddress());

    }

    @Test
    public void serialize__Simple() throws Exception {
        LcafApplicationDataBuilder addressBuilder = new LcafApplicationDataBuilder();
        addressBuilder.setIpTos(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }));
        addressBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType(
                (short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode());
        addressBuilder.setProtocol((short) 0xDD);
        addressBuilder.setLocalPort(new PortNumber(0xA6A1));
        addressBuilder.setRemotePort(new PortNumber(0xFFDD));
        addressBuilder.setAddress(new AddressBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68")).build());
        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(addressBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, addressBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "04 00 00 0E " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 FF DD " + // local port & remote port
                "00 01 11 22 33 44"); // AFI=1, IP=0x11223344
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
