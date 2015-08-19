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
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafapplicationdataaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;

public class LispApplicationDataLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0E " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 A6 A2 " + // local port range
                "FF DD FF DE " + // remote port range
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafApplicationDataAddress appAddress = (LcafApplicationDataAddress) address;

        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68"), appAddress.getAddress().getPrimitiveAddress());
        assertEquals(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }), appAddress.getIpTos().intValue());
        assertEquals((byte) 0xDD, appAddress.getProtocol().byteValue());
        assertEquals((short) 0xA6A1, appAddress.getLocalPortLow().getValue().shortValue());
        assertEquals((short) 0xA6A2, appAddress.getLocalPortHigh().getValue().shortValue());
        assertEquals((short) 0xFFDD, appAddress.getRemotePortLow().getValue().shortValue());
        assertEquals((short) 0xFFDE, appAddress.getRemotePortHigh().getValue().shortValue());
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
                "AA 20 00 12 " + // Type AA is unknown
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 A6 A2 " + // local port range
                "FF DD FF DE " + // remote port range
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LcafApplicationDataAddress appAddress = (LcafApplicationDataAddress) LispAddressSerializer.getInstance().deserialize(
                hexToByteBuffer("40 03 00 00 " + //
                        "04 20 00 1E " + //
                        "AA BB CC DD " + // IPTOS & protocol
                        "A6 A1 A6 A2 " + // local port range
                        "FF DD FF DE " + // remote port range
                        "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd")), appAddress
                .getAddress().getPrimitiveAddress());

    }

    @Test
    public void serialize__Simple() throws Exception {
        LcafApplicationDataAddrBuilder addressBuilder = new LcafApplicationDataAddrBuilder();
        addressBuilder.setIpTos(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }));
        addressBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType(
                (short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode());
        addressBuilder.setProtocol((short) 0xDD);
        addressBuilder.setLocalPortLow(new PortNumber(0xA6A1));
        addressBuilder.setLocalPortHigh(new PortNumber(0xA6A2));
        addressBuilder.setRemotePortLow(new PortNumber(0xFFDD));
        addressBuilder.setRemotePortHigh(new PortNumber(0xFFDE));
        addressBuilder.setAddress(new AddressBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68")).build());
        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(addressBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, addressBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "04 00 00 12 " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 A6 A2 " + // local port range
                "FF DD FF DE " + // remote port range
                "00 01 11 22 33 44"); // AFI=1, IP=0x11223344
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
