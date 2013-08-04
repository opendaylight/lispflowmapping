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
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;

public class LispSegmentLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
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
                "AA BB CC DD " + // instance ID
                "00 01 11 22 33 44")); // AFI=1, IP=0x11223344
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        LispSegmentLCAFAddress segAddress = (LispSegmentLCAFAddress) LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
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
        ByteBuffer buf = ByteBuffer.allocate(LispSegmentLCAFAddressSerializer.getInstance().getAddressSize(address));
        LispSegmentLCAFAddressSerializer.getInstance().serialize(buf,address);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "02 06 00 0A " + //
                "01 02 03 04 " + // instance ID
                "00 01 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
    
    @Test
    public void serialize__Recursive_Segment() throws Exception {
        LispSegmentLCAFAddress address = new LispSegmentLCAFAddress((byte) 0x06, 0x01020304, new LispSegmentLCAFAddress((byte) 0x06, 0x01020305, new LispIpv4Address(0x11223344)));
        ByteBuffer buf = ByteBuffer.allocate(LispSegmentLCAFAddressSerializer.getInstance().getAddressSize(address));
        LispSegmentLCAFAddressSerializer.getInstance().serialize(buf,address);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "02 06 00 16 " + //
                "01 02 03 04 " + // instance ID
                "40 03 00 00 " + //
                "02 06 00 0A " + //
                "01 02 03 05 " + // instance ID
                "00 01 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
