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
import junitx.framework.Assert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AfiListLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AfiList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class AfiListSerializerTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44")); // IPv6

        assertEquals(AfiListLcaf.class, address.getAddressType());
        AfiList afiList = (AfiList) address.getAddress();

        List<SimpleAddress> addressList = afiList.getAfiList().getAddressList();
        assertEquals(2, addressList.size());

        assertEquals("170.187.204.221", String.valueOf(addressList.get(0).getValue()));
        assertEquals("1122:3344:1122:3344:1122:3344:1122:3344", String.valueOf(addressList.get(1).getValue()));
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        Rloc address = LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 00 "));

        assertEquals(AfiListLcaf.class, address.getAddressType());
        AfiList afiList = (AfiList) address.getAddress();

        List<SimpleAddress> addressList = afiList.getAfiList().getAddressList();
        assertEquals(0, addressList.size());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44"));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserializeRloc(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        AfiListBuilder listBuilder = new AfiListBuilder();
        List<SimpleAddress> addressList = new ArrayList<SimpleAddress>();
        addressList.add(SimpleAddressBuilder.getDefaultInstance("170.187.204.221"));
        addressList.add(SimpleAddressBuilder.getDefaultInstance("1122:3344:1122:3344:1122:3344:1122:3344"));
        listBuilder.setAddressList(addressList);

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(AfiListLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress((Address)
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AfiListBuilder()
                .setAfiList(listBuilder.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        AfiListBuilder listBuilder = new AfiListBuilder();
        List<SimpleAddress> addressList = new ArrayList<SimpleAddress>();
        listBuilder.setAddressList(addressList);

        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(AfiListLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress((Address)
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AfiListBuilder()
                .setAfiList(listBuilder.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(rb.build()));
        LispAddressSerializer.getInstance().serialize(buf, rb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void equals__Simple() throws Exception {
        SimpleAddress ip1 = SimpleAddressBuilder.getDefaultInstance("0:0:0:0:0:0:0:1");
        SimpleAddress ip2 = SimpleAddressBuilder.getDefaultInstance("0:0:0:0:0:0:0:2");

        AfiListBuilder listBuilder = new AfiListBuilder().setAddressList(new ArrayList<SimpleAddress>());

        listBuilder.getAddressList().add(ip1);
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiList address1 = listBuilder.build();
        listBuilder.setAddressList(new ArrayList<SimpleAddress>());
        listBuilder.getAddressList().add(ip1);
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiList address2 = listBuilder.build();
        listBuilder.setAddressList(new ArrayList<SimpleAddress>());
        listBuilder.getAddressList().add(ip2);
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.afi.list.AfiList address3 = listBuilder.build();

        assertEquals(address1, address2);
        Assert.assertNotEquals(address2, address3);
        Assert.assertNotEquals(address1, address3);
    }
}
