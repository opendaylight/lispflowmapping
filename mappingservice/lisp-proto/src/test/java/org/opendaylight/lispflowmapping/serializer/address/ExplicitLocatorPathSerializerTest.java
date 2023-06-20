/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.serializer.address;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.lispflowmapping.TestUtils.hexToByteBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class ExplicitLocatorPathSerializerTest {

    @Test
    public void deserialize__Simple() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 "
                + "0A 00 00 10 "
                + "00 00 00 01 AA BB CC DD "   // IPv4
                + "00 00 00 01 11 22 33 44")); // IPv4

        assertEquals(ExplicitLocatorPathLcaf.VALUE, address.getAddressType());
        ExplicitLocatorPath elp = (ExplicitLocatorPath) address.getAddress();

        List<Hop> hops = elp.getExplicitLocatorPath().getHop();
        assertEquals(2, hops.size());

        assertEquals("170.187.204.221", hops.get(0).getAddress().stringValue());
        assertEquals("17.34.51.68", hops.get(1).getAddress().stringValue());
    }

    @Test
    public void deserialize__Bits() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 "
                + "0A 00 00 10 "
                + "00 05 00 01 AA BB CC DD "   // IPv4
                + "00 02 00 01 11 22 33 44")); // IPv4

        assertEquals(ExplicitLocatorPathLcaf.VALUE, address.getAddressType());
        ExplicitLocatorPath elp = (ExplicitLocatorPath) address.getAddress();

        List<Hop> hops = elp.getExplicitLocatorPath().nonnullHop();
        assertEquals(2, hops.size());

        assertEquals("170.187.204.221", hops.get(0).getAddress().stringValue());
        assertTrue(hops.get(0).getLrsBits().getLookup());
        assertFalse(hops.get(0).getLrsBits().getRlocProbe());
        assertTrue(hops.get(0).getLrsBits().getStrict());
        assertEquals("17.34.51.68", hops.get(1).getAddress().stringValue());
        assertFalse(hops.get(1).getLrsBits().getLookup());
        assertTrue(hops.get(1).getLrsBits().getRlocProbe());
        assertFalse(hops.get(1).getLrsBits().getStrict());
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 "
                + "0A 00 00 00 "));

        assertEquals(ExplicitLocatorPathLcaf.VALUE, address.getAddressType());
        ExplicitLocatorPath elp = (ExplicitLocatorPath) address.getAddress();

        List<Hop> hops = elp.getExplicitLocatorPath().getHop();
        assertNull(hops);
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("""
            40 03 00 00 \
            0A 00 00 18 \
            00 01 AA BB CC DD \
            00 02 11 22 33 44 11 22 33 44 11 22 33 44"""));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 "
                + "0A 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        List<Hop> hops = new ArrayList<>();
        hops.add(new HopBuilder().setAddress(SimpleAddressBuilder.getDefaultInstance("170.187.204.221"))
                .setHopId("hubba").build());
        hops.add(new HopBuilder().setAddress(SimpleAddressBuilder.getDefaultInstance("17.34.51.68"))
                .setHopId("bubba").build());

        ExplicitLocatorPathBuilder elpb = new ExplicitLocatorPathBuilder();
        elpb.setHop(hops);

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(ExplicitLocatorPathLcaf.VALUE);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.ExplicitLocatorPathBuilder()
            .setExplicitLocatorPath(elpb.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 "
                + "0A 00 00 10 "
                + "00 00 00 01 AA BB CC DD "  // IPv4
                + "00 00 00 01 11 22 33 44"); // IPv4
        assertArrayEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(ExplicitLocatorPathLcaf.VALUE);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.ExplicitLocatorPathBuilder().build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 "
                + "0A 00 00 00");
        assertArrayEquals(expectedBuf.array(), buf.array());
    }
}
