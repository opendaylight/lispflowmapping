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
import org.opendaylight.lispflowmapping.implementation.serializer.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafListAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaflistaddress.Addresses;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaflistaddress.AddressesBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafList;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafListBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv6;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public class LispListLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44")); // IPv6

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafListAddress lcafList = (LcafListAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.LIST.getLispCode(), lcafList.getLcafType().byteValue());

        List<Addresses> addressList = lcafList.getAddresses();
        assertEquals(2, addressList.size());

        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPAfiAddress("170.187.204.221")), addressList.get(0).getPrimitiveAddress());
        assertEquals(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("1122:3344:1122:3344:1122:3344:1122:3344")), addressList.get(1)
                .getPrimitiveAddress());
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 00 "));

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafListAddress lcafList = (LcafListAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.LIST.getLispCode(), lcafList.getLcafType().byteValue());

        List<Addresses> addressList = lcafList.getAddresses();
        assertEquals(0, addressList.size());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44"));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        LcafListBuilder listBuilder = new LcafListBuilder();
        listBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        listBuilder.setLcafType((short) LispCanonicalAddressFormatEnum.LIST.getLispCode());
        List<Addresses> addressList = new ArrayList<Addresses>();
        addressList.add(new AddressesBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPAfiAddress("170.187.204.221")))
                .build());
        addressList.add(new AddressesBuilder().setPrimitiveAddress(
                LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("1122:3344:1122:3344:1122:3344:1122:3344"))).build());
        listBuilder.setAddresses(addressList);
        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(listBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, listBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44 11 22 33 44");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        LcafListBuilder listBuilder = new LcafListBuilder();
        listBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        listBuilder.setLcafType((short) LispCanonicalAddressFormatEnum.LIST.getLispCode());
        List<Addresses> addressList = new ArrayList<Addresses>();
        listBuilder.setAddresses(addressList);
        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(listBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, listBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void equals__Simple() throws Exception {
        Ipv6 ip1 = (Ipv6) LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1"));
        Ipv6 ip2 = (Ipv6) LispAFIConvertor.toPrimitive(LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:2"));
        LcafListBuilder listBuilder = new LcafListBuilder().setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.LIST.getLispCode()).setAddresses(new ArrayList<Addresses>());
        listBuilder.getAddresses().add(new AddressesBuilder().setPrimitiveAddress(ip1).build());
        LcafList address1 = listBuilder.build();
        listBuilder.setAddresses(new ArrayList<Addresses>());
        listBuilder.getAddresses().add(new AddressesBuilder().setPrimitiveAddress(ip1).build());
        LcafList address2 = listBuilder.build();
        listBuilder.setAddresses(new ArrayList<Addresses>());
        listBuilder.getAddresses().add(new AddressesBuilder().setPrimitiveAddress(ip2).build());
        LcafList address3 = listBuilder.build();

        assertEquals(address1, address2);
        Assert.assertNotEquals(address2, address3);
        Assert.assertNotEquals(address1, address3);
    }
}
