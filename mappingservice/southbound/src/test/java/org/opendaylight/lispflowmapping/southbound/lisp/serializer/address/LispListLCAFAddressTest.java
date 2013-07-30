/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp.serializer.address;

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junitx.framework.ArrayAssert;
import junitx.framework.Assert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;

public class LispListLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44")); // IPv6

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispListLCAFAddress lcafList = (LispListLCAFAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.LIST, lcafList.getType());

        List<LispAddress> addressList = lcafList.getAddresses();
        assertEquals(2, addressList.size());

        assertEquals(new LispIpv4Address(0xAABBCCDD), addressList.get(0));
        assertEquals(new LispIpv6Address("1122:3344:1122:3344:1122:3344:1122:3344"), addressList.get(1));
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        LispAddress address = LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 00 "));

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispListLCAFAddress lcafList = (LispListLCAFAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.LIST, lcafList.getType());

        List<LispAddress> addressList = lcafList.getAddresses();
        assertEquals(0, addressList.size());
    }

    @Test(expected = LispMalformedPacketException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44"));
    }

    @Test(expected = LispMalformedPacketException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddress.valueOf(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        List<LispAddress> addressList = new ArrayList<LispAddress>();
        addressList.add(new LispIpv4Address(0xAABBCCDD));
        addressList.add(new LispIpv6Address("1222:3344:1122:3344:1122:3344:1122:3344"));
        LispListLCAFAddress address = new LispListLCAFAddress((byte) 0, addressList);

        ByteBuffer buf = ByteBuffer.allocate(address.getAddressSize());
        address.serialize(buf);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 12 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        LispListLCAFAddress address = new LispListLCAFAddress((byte) 0, new ArrayList<LispAddress>());

        ByteBuffer buf = ByteBuffer.allocate(address.getAddressSize());
        address.serialize(buf);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void equals__Simple() throws Exception {
        LispListLCAFAddress address1 = new LispListLCAFAddress((byte) 0x06, Arrays.asList(new LispIpv4Address(0x11223344), //
                new LispIpv6Address("::1")));
        LispListLCAFAddress address2 = new LispListLCAFAddress((byte) 0x06, Arrays.asList(new LispIpv4Address(0x11223344), //
                new LispIpv6Address("::1")));
        LispListLCAFAddress address3 = new LispListLCAFAddress((byte) 0x06, Arrays.asList(new LispIpv4Address(0x11223344), //
                new LispIpv6Address("::2")));
        LispListLCAFAddress address4 = new LispListLCAFAddress((byte) 0x05, Arrays.asList(new LispIpv4Address(0x11223344), //
                new LispIpv6Address("::1")));
        LispListLCAFAddress address5 = new LispListLCAFAddress((byte) 0x06, new ArrayList<LispAddress>());

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
