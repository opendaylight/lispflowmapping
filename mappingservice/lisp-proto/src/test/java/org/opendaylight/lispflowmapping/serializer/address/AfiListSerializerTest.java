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
import static org.junit.Assert.assertNotEquals;
import static org.opendaylight.lispflowmapping.TestUtils.hexToByteBuffer;

import com.google.common.collect.ImmutableSet;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AfiListLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AfiList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class AfiListSerializerTest {

    @Test
    public void deserialize__Simple() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 "
                + "01 00 00 18 "
                + "00 01 AA BB CC DD " // IPv4
                + "00 02 11 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44")); // IPv6

        assertEquals(AfiListLcaf.VALUE, address.getAddressType());
        AfiList afiList = (AfiList) address.getAddress();

        Set<SimpleAddress> addressList = afiList.getAfiList().getAddressList();
        assertEquals(2, addressList.size());

        Iterator<SimpleAddress> it = addressList.iterator();
        assertEquals("170.187.204.221", it.next().stringValue());
        assertEquals("1122:3344:1122:3344:1122:3344:1122:3344", it.next().stringValue());
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 "
                + "01 00 00 00 "));

        assertEquals(AfiListLcaf.VALUE, address.getAddressType());
        AfiList afiList = (AfiList) address.getAddress();

        assertEquals(Set.of(), afiList.getAfiList().getAddressList());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("""
            40 03 00 00 \
            01 00 00 18 \
            00 01 AA BB CC DD \
            00 02 11 22 33 44 11 22 33 44 11 22 33 44"""));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 "
                + "01 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        AfiListBuilder listBuilder = new AfiListBuilder();
        listBuilder.setAddressList(ImmutableSet.of(
            SimpleAddressBuilder.getDefaultInstance("170.187.204.221"),
            SimpleAddressBuilder.getDefaultInstance("1122:3344:1122:3344:1122:3344:1122:3344")));

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(AfiListLcaf.VALUE);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.AfiListBuilder()
            .setAfiList(listBuilder.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 "
                + "01 00 00 18 "
                + "00 01 AA BB CC DD " // IPv4
                + "00 02 11 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44");
        assertArrayEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        AfiListBuilder listBuilder = new AfiListBuilder();
        listBuilder.setAddressList(Set.of());

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(AfiListLcaf.VALUE);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.AfiListBuilder()
            .setAfiList(listBuilder.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 "
                + "01 00 00 00");
        assertArrayEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void equals__Simple() throws Exception {
        final SimpleAddress ip1 = SimpleAddressBuilder.getDefaultInstance("0:0:0:0:0:0:0:1");
        final SimpleAddress ip2 = SimpleAddressBuilder.getDefaultInstance("0:0:0:0:0:0:0:2");

        AfiListBuilder listBuilder = new AfiListBuilder().setAddressList(Set.of(ip1));

        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.afi.list.AfiList address1 = listBuilder.build();
        listBuilder.setAddressList(Set.of(ip1));
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.afi.list.AfiList address2 = listBuilder.build();
        listBuilder.setAddressList(Set.of(ip2));
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.afi.list.AfiList address3 = listBuilder.build();

        assertEquals(address1, address2);
        assertNotEquals(address2, address3);
        assertNotEquals(address1, address3);
    }
}
