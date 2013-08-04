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
import org.opendaylight.lispflowmapping.type.lisp.address.LispSourceDestLCAFAddress;

public class LispSourceDestLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0C 20 00 10 " + //
                "00 00 10 18 " + // reserved + masks
                "00 01 11 22 33 44 " +  // AFI=1, IP=0x11223344
    			"00 01 22 33 44 55")); // AFI=1, IP=0x22334455

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispSourceDestLCAFAddress srcDestAddress = (LispSourceDestLCAFAddress) address;
        
        assertEquals(0, srcDestAddress.getReserved());
        assertEquals((byte)0x10, srcDestAddress.getSrcMaskLength());
        assertEquals((byte)0x18, srcDestAddress.getDstMaskLength());

        assertEquals(new LispIpv4Address(0x11223344), srcDestAddress.getSrcAddress());
        assertEquals(new LispIpv4Address(0x22334455), srcDestAddress.getDstAddress());
        assertEquals(LispCanonicalAddressFormatEnum.SOURCE_DEST, srcDestAddress.getType());
        System.out.println(address);
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
                "00 01 11 22 33 44 " +  // AFI=1, IP=0x11223344
    			"00 01 22 33 44 55")); // AFI=1, IP=0x22334455
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
    	LispSourceDestLCAFAddress srcAddress = (LispSourceDestLCAFAddress) LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
        		"0C 20 00 28 " + //
                "00 00 CC DD " + // reserved + masks
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD " + // AFI=2,
    			"00 02 44 33 22 11 88 77 66 55 99 AA BB CC AA BB CC DD")); // AFI=2,
        // IPv6

        assertEquals(new LispIpv6Address(new byte[] { 0x11, 0x22, 0x33, 0x44, //
                0x55, 0x66, 0x77, (byte) 0x88, //
                (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, //
                (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD }), srcAddress.getSrcAddress());
        assertEquals(new LispIpv6Address(new byte[] { 0x44, 0x33, 0x22, 0x11, //
        		(byte) 0x88, 0x77, 0x66, 0x55, //
        		(byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, //
        		(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD }), srcAddress.getDstAddress());

    }

    @Test
    public void serialize__Simple() throws Exception {
    	LispSourceDestLCAFAddress address = new LispSourceDestLCAFAddress((byte) 0x20, (short)0x0000, (byte) 0xCC, (byte) 0xDD, new LispIpv4Address(0x11223344), new LispIpv4Address(0x22334455));
        ByteBuffer buf = ByteBuffer.allocate(LispSourceDestLCAFAddressSerializer.getInstance().getAddressSize(address));
        LispSourceDestLCAFAddressSerializer.getInstance().serialize(buf,address);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
        		 "0C 20 00 10 " + //
                 "00 00 CC DD " + // reserved + masks
                 "00 01 11 22 33 44 " +  // AFI=1, IP=0x11223344
     			"00 01 22 33 44 55"); // AFI=1, IP=0x22334455
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
