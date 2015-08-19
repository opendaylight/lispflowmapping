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
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafListAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispDistinguishedNameAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.distinguishedname.DistinguishedNameBuilder;

public class LispDistinguishedNameAddressSerializerTest extends BaseTestCase {

    @Test
    public void deserialize__EmptyString() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("00 11 00"));

        assertEquals(AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode(), address.getAfi().shortValue());
        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) address;

        assertEquals("", distinguishedNameAddress.getDistinguishedName());

    }

    @Test
    public void deserialize__DavidString() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("00 11 64 61 76 69 64 00"));

        assertEquals(AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode(), address.getAfi().shortValue());
        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) address;

        assertEquals("david", distinguishedNameAddress.getDistinguishedName());

    }

    @Test
    public void deserialize__inList() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 8 " + //
                "00 11 64 61 76 69 64 00"));

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafListAddress lispLCAFAddress = (LcafListAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.LIST.getLispCode(), lispLCAFAddress.getLcafType().byteValue());

        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) LispAFIConvertor.toAFIfromPrimitive(lispLCAFAddress.getAddresses().get(0)
                .getPrimitiveAddress());

        assertEquals("david", distinguishedNameAddress.getDistinguishedName());

    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 10 " + //
                "00 11 64 61 76 69 64 00"));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 "));
    }

    @Test
    public void deserialize__ReadUntilZero() throws Exception {
        LispDistinguishedNameAddress address = (LispDistinguishedNameAddress) LispAddressSerializer.getInstance().deserialize(
                hexToByteBuffer("00 11 64 61 76 00 69 64"));
        assertEquals("dav", address.getDistinguishedName());
    }

    @Test
    public void serialize__Simple() throws Exception {
        DistinguishedNameBuilder distinguishedName = new DistinguishedNameBuilder().setDistinguishedName("david").setAfi(
                AddressFamilyNumberEnum.DISTINGUISHED_NAME.getIanaCode());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(distinguishedName.build()));
        LispAddressSerializer.getInstance().serialize(buf, distinguishedName.build());
        ByteBuffer expectedBuf = hexToByteBuffer("00 11 64 61 76 69 64 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

}
