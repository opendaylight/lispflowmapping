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
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ApplicationDataLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.application.data.ApplicationDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class LispApplicationDataLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0E " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 A6 A2 " + // local port range
                "FF DD FF DE " + // remote port range
                "00 01 11 22 33 44"), null); // AFI=1, IP=0x11223344

        assertEquals(ApplicationDataLcaf.class, address.getAddressType());
        ApplicationData appAddress = (ApplicationData) address.getAddress();

        assertEquals("17.34.51.68", String.valueOf(appAddress.getApplicationData().getAddress().getValue()));
        assertEquals(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }),
                appAddress.getApplicationData().getIpTos().intValue());
        assertEquals((byte) 0xDD, appAddress.getApplicationData().getProtocol().byteValue());
        assertEquals((short) 0xA6A1, appAddress.getApplicationData().getLocalPortLow().getValue().shortValue());
        assertEquals((short) 0xA6A2, appAddress.getApplicationData().getLocalPortHigh().getValue().shortValue());
        assertEquals((short) 0xFFDD, appAddress.getApplicationData().getRemotePortLow().getValue().shortValue());
        assertEquals((short) 0xFFDE, appAddress.getApplicationData().getRemotePortHigh().getValue().shortValue());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "04 20 00 0A " + //
                "AA BB "), null);
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__UnknownLCAFType() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "AA 20 00 12 " + // Type AA is unknown
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 A6 A2 " + // local port range
                "FF DD FF DE " + // remote port range
                "00 01 11 22 33 44"), null); // AFI=1, IP=0x11223344
    }

    @Test
    public void deserialize__Ipv6() throws Exception {
        Eid appAddress = LispAddressSerializer.getInstance().deserializeEid(
                hexToByteBuffer("40 03 00 00 " + //
                        "04 20 00 1E " + //
                        "AA BB CC DD " + // IPTOS & protocol
                        "A6 A1 A6 A2 " + // local port range
                        "FF DD FF DE " + // remote port range
                        "00 02 11 22 33 44 55 66 77 88 99 AA BB CC AA BB CC DD"), null); // AFI=2,
        // IPv6

        assertEquals("1122:3344:5566:7788:99aa:bbcc:aabb:ccdd", String.valueOf(
                ((ApplicationData) appAddress.getAddress()).getApplicationData().getAddress().getValue()));
    }

    @Test
    public void serialize__Simple() throws Exception {
        ApplicationDataBuilder addressBuilder = new ApplicationDataBuilder();
        addressBuilder.setIpTos(ByteUtil.getPartialInt(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC }));
        addressBuilder.setProtocol((short) 0xDD);
        addressBuilder.setLocalPortLow(new PortNumber(0xA6A1));
        addressBuilder.setLocalPortHigh(new PortNumber(0xA6A2));
        addressBuilder.setRemotePortLow(new PortNumber(0xFFDD));
        addressBuilder.setRemotePortHigh(new PortNumber(0xFFDE));
        addressBuilder.setAddress(new SimpleAddress(new IpAddress(new Ipv4Address("17.34.51.68"))));

        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ApplicationDataLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress((Address)
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ApplicationDataBuilder()
                .setApplicationData(addressBuilder.build()).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(eb.build()));
        LispAddressSerializer.getInstance().serialize(buf, eb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "04 00 00 12 " + //
                "AA BB CC DD " + // IPTOS & protocol
                "A6 A1 A6 A2 " + // local port range
                "FF DD FF DE " + // remote port range
                "00 01 11 22 33 44"); // AFI=1, IP=0x11223344
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
