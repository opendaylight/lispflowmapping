/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.serializer.address;

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.southbound.lisp.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.southbound.util.ByteUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispApplicationDataLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;

public class LispApplicationDataLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0A " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 FF DD " + // local port & remote port
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispApplicationDataLCAFAddress appAddress = (LispApplicationDataLCAFAddress) address;

        assertEquals(new LispIpv4Address(0x11223344), appAddress.getAddress());
        assertEquals(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }), appAddress.getIPTos());
        assertEquals((byte) 0xDD, appAddress.getProtocol());
        assertEquals((short) 0xA6A1, appAddress.getLocalPort());
        assertEquals((short) 0xFFDD, appAddress.getRemotePort());
        assertEquals(LispCanonicalAddressFormatEnum.APPLICATION_DATA, appAddress.getType());
    }

    @Test(expected = LispMalformedPacketException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0A " + //
                "AA BB "));
    }

    @Test(expected = LispMalformedPacketException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 0A " + // Type AA is unknown
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 FF DD " + // local port & remote port
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LispApplicationDataLCAFAddress appAddress = (LispApplicationDataLCAFAddress) LispAddressSerializer.getInstance().deserialize(
                hexToByteBuffer("40 03 00 00 " + //
                        "04 20 00 0A " + //
                        "AA BB CC DD " + // IPTOS & protocol
                        "A6 A1 FF DD " + // local port & remote port
                        "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(new LispIpv6Address(new byte[] { 0x11, 0x22, 0x33, 0x44, //
                0x55, 0x66, 0x77, (byte) 0x88, //
                (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, //
                (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD }), appAddress.getAddress());

    }

    @Test
    public void serialize__Simple() throws Exception {
        LispApplicationDataLCAFAddress address = new LispApplicationDataLCAFAddress((byte) 0x20);
        address.setIPTos(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }));
        address.setProtocol((byte) 0xDD);
        address.setLocalPort((short) 0xA6A1);
        address.setRemotePort((short) 0xFFDD);
        address.setAddress(new LispIpv4Address(0x11223344));
        ByteBuffer buf = ByteBuffer.allocate(LispApplicationDataLCAFAddressSerializer.getInstance().getAddressSize(address));
        LispApplicationDataLCAFAddressSerializer.getInstance().serialize(buf, address);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0E " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 FF DD " + // local port & remote port
                "00 01 11 22 33 44"); // AFI=1, IP=0x11223344
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
