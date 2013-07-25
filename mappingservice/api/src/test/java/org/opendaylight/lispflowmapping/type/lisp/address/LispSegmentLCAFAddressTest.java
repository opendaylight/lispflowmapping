/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;

import junitx.framework.ArrayAssert;
import junitx.framework.Assert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;

public class LispSegmentLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "AA BB CC DD " + // instance ID
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispSegmentLCAFAddress segAddress = (LispSegmentLCAFAddress) address;

        assertEquals(new LispIpv4Address(0x11223344), segAddress.getAddress());
        assertEquals(0xAABBCCDD, segAddress.getInstanceId());
        assertEquals(0x20, segAddress.getIdMaskLen());
        assertEquals(LispCanonicalAddressFormatEnum.SEGMENT, segAddress.getType());
    }

    @Test(expected = LispMalformedPacketException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "AA BB "));
    }

    @Test(expected = LispMalformedPacketException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 0A " + // Type AA is unknown
                "AA BB CC DD " + // instance ID
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LispSegmentLCAFAddress segAddress = (LispSegmentLCAFAddress) LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "AA BB CC DD " + // instance ID
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(new LispIpv6Address(new byte[] { 0x11, 0x22, 0x33, 0x44, //
                0x55, 0x66, 0x77, (byte) 0x88, //
                (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, //
                (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD }), segAddress.getAddress());

    }

    @Test
    public void serialize__Simple() throws Exception {
        LispSegmentLCAFAddress address = new LispSegmentLCAFAddress((byte) 0x06, 0x01020304, new LispIpv4Address(0x11223344));
        ByteBuffer buf = ByteBuffer.allocate(address.getAddressSize());
        address.serialize(buf);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "02 06 00 0A " + //
                "01 02 03 04 " + // instance ID
                "00 01 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void equals__Simple() throws Exception {
        LispSegmentLCAFAddress address1 = new LispSegmentLCAFAddress((byte) 0x06, 0x01020304, new LispIpv4Address(0x11223344));
        LispSegmentLCAFAddress address2 = new LispSegmentLCAFAddress((byte) 0x06, 0x01020304, new LispIpv4Address(0x11223344));
        LispSegmentLCAFAddress address3 = new LispSegmentLCAFAddress((byte) 0x06, 0x01020305, new LispIpv4Address(0x11223344));
        LispSegmentLCAFAddress address4 = new LispSegmentLCAFAddress((byte) 0x05, 0x01020304, new LispIpv4Address(0x11223344));
        LispSegmentLCAFAddress address5 = new LispSegmentLCAFAddress((byte) 0x06, 0x01020304, new LispIpv4Address(0x11223343));

        assertEquals(address1, address2);
        assertEquals(address1, address1);
        Assert.assertNotEquals(address1, address3);
        Assert.assertNotEquals(address1, address4);
        Assert.assertNotEquals(address1, address5);
        Assert.assertNotEquals(address3, address4);
        Assert.assertNotEquals(address3, address5);
        Assert.assertNotEquals(address4, address5);
    }
}
