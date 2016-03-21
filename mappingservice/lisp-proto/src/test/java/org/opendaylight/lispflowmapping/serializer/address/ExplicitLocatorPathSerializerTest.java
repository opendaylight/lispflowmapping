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
import java.util.ArrayList;
import java.util.List;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class ExplicitLocatorPathSerializerTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 00 00 01 AA BB CC DD " + // IPv4
                "00 00 00 01 11 22 33 44")); // IPv4

        assertEquals(ExplicitLocatorPathLcaf.class, address.getAddressType());
        ExplicitLocatorPath elp = (ExplicitLocatorPath) address.getAddress();

        List<Hop> hops = elp.getExplicitLocatorPath().getHop();
        assertEquals(2, hops.size());

        assertEquals("170.187.204.221", String.valueOf(hops.get(0).getAddress().getValue()));
        assertEquals("17.34.51.68", String.valueOf(hops.get(1).getAddress().getValue()));
    }

    @Test
    public void deserialize__Bits() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 05 00 01 AA BB CC DD " + // IPv4
                "00 02 00 01 11 22 33 44")); // IPv4

        assertEquals(ExplicitLocatorPathLcaf.class, address.getAddressType());
        ExplicitLocatorPath elp = (ExplicitLocatorPath) address.getAddress();

        List<Hop> hops = elp.getExplicitLocatorPath().getHop();
        assertEquals(2, hops.size());

        assertEquals("170.187.204.221", String.valueOf(hops.get(0).getAddress().getValue()));
        assertEquals(true, hops.get(0).getLrsBits().isLookup().booleanValue());
        assertEquals(false, hops.get(0).getLrsBits().isRlocProbe().booleanValue());
        assertEquals(true, hops.get(0).getLrsBits().isStrict().booleanValue());
        assertEquals("17.34.51.68", String.valueOf(hops.get(1).getAddress().getValue()));
        assertEquals(false, hops.get(1).getLrsBits().isLookup().booleanValue());
        assertEquals(true, hops.get(1).getLrsBits().isRlocProbe().booleanValue());
        assertEquals(false, hops.get(1).getLrsBits().isStrict().booleanValue());
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 00 "));

        assertEquals(ExplicitLocatorPathLcaf.class, address.getAddressType());
        ExplicitLocatorPath elp = (ExplicitLocatorPath) address.getAddress();

        List<Hop> hops = elp.getExplicitLocatorPath().getHop();
        assertEquals(0, hops.size());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44"));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        List<Hop> hops = new ArrayList<Hop>();
        hops.add(new HopBuilder().setAddress(SimpleAddressBuilder.getDefaultInstance("170.187.204.221")).build());
        hops.add(new HopBuilder().setAddress(SimpleAddressBuilder.getDefaultInstance("17.34.51.68")).build());

        ExplicitLocatorPathBuilder elpb = new ExplicitLocatorPathBuilder();
        elpb.setHop(hops);

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(ExplicitLocatorPathLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress((Address)
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ExplicitLocatorPathBuilder()
                .setExplicitLocatorPath(elpb.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 00 00 01 AA BB CC DD " + // IPv4
                "00 00 00 01 11 22 33 44"); // IPv4
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(ExplicitLocatorPathLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress((Address)
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ExplicitLocatorPathBuilder().build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
