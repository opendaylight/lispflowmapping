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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junitx.framework.ArrayAssert;
import junitx.framework.Assert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispTrafficEngineeringLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.ReencapHop;

public class LispTrafficEngineeringLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 01 00 00 AA BB CC DD " + // IPv4
                "00 01 00 00 11 22 33 44")); // IPv4

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispTrafficEngineeringLCAFAddress trafficEngineering = (LispTrafficEngineeringLCAFAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING, trafficEngineering.getType());

        List<ReencapHop> hops = trafficEngineering.getHops();
        assertEquals(2, hops.size());

        assertEquals(new LispIpv4Address(0xAABBCCDD), hops.get(0).getHop());
        assertEquals(new LispIpv4Address(0x11223344), hops.get(1).getHop());
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 00 "));

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispTrafficEngineeringLCAFAddress trafficEngineering = (LispTrafficEngineeringLCAFAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING, trafficEngineering.getType());

        List<ReencapHop> hops = trafficEngineering.getHops();
        assertEquals(0, hops.size());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44"));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        List<ReencapHop> hops = new ArrayList<ReencapHop>();
        hops.add(new ReencapHop(new LispIpv4Address(0xAABBCCDD), (short)0, false, false, false));
        hops.add(new ReencapHop(new LispIpv4Address(0x11223344), (short)0, false, false, false));
        LispTrafficEngineeringLCAFAddress address = new LispTrafficEngineeringLCAFAddress((byte) 0, hops);

        ByteBuffer buf = ByteBuffer.allocate(LispTrafficEngineeringLCAFAddressSerializer.getInstance().getAddressSize(address));
        LispAddressSerializer.getInstance().serialize(buf,address);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 01 00 00 AA BB CC DD " + // IPv4
                "00 01 00 00 11 22 33 44"); // IPv4
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        LispTrafficEngineeringLCAFAddress address = new LispTrafficEngineeringLCAFAddress((byte) 0, new ArrayList<ReencapHop>());

        ByteBuffer buf = ByteBuffer.allocate(LispTrafficEngineeringLCAFAddressSerializer.getInstance().getAddressSize(address));
        LispAddressSerializer.getInstance().serialize(buf,address);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
