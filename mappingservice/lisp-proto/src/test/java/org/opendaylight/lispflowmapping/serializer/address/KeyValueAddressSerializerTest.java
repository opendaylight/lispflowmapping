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

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.KeyValueAddressLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.key.value.address.KeyValueAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class KeyValueAddressSerializerTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "0F 20 00 0C " + //
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"), null); // AFI=1, IP=0x22334455

        assertEquals(KeyValueAddressLcaf.class, address.getAddressType());
        KeyValueAddress srcDestAddress = (KeyValueAddress) address.getAddress();

        assertEquals("17.34.51.68", String.valueOf(srcDestAddress.getKeyValueAddress().getKey().getValue()));
        assertEquals("34.51.68.85", String.valueOf(srcDestAddress.getKeyValueAddress().getValue().getValue()));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "02 20 00 0A " + //
                "AA BB "), null);
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 0A " + // Type AA is unknown
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"), null); // AFI=1, IP=0x22334455
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        Eid srcAddress = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "0F 20 00 24 " + //
                "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD " + // AFI=2,
                "00 02 44 33 22 11 88 77 66 55 99 AA BB CC AA BB CC DD"), null); // AFI=2,
        // IPv6

        assertEquals("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd", String.valueOf(
                ((KeyValueAddress) srcAddress.getAddress()).getKeyValueAddress().getKey().getValue()));
        assertEquals("4433:2211:8877:6655:99aa:bbcc:aabb:ccdd", String.valueOf(
                ((KeyValueAddress) srcAddress.getAddress()).getKeyValueAddress().getValue().getValue()));
    }

    @Test
    public void serialize__Simple() throws Exception {
        KeyValueAddressBuilder addressBuilder = new KeyValueAddressBuilder();
        addressBuilder.setKey(new SimpleAddress(new IpAddress(new Ipv4Address("17.34.51.68"))));
        addressBuilder.setValue(new SimpleAddress(new IpAddress(new Ipv4Address("34.51.68.85"))));

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(KeyValueAddressLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress((Address)
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.KeyValueAddressBuilder()
                .setKeyValueAddress(addressBuilder.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(eb.build()));
        LispAddressSerializer.getInstance().serialize(buf, eb.build());

        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0F 00 00 0C " + //
                "00 01 11 22 33 44 " + // AFI=1, IP=0x11223344
                "00 01 22 33 44 55"); // AFI=1, IP=0x22334455
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
