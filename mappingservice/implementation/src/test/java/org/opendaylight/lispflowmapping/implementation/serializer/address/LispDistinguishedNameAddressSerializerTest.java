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

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispDistinguishedNameAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;

public class LispDistinguishedNameAddressSerializerTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("00 11 00"));

        assertEquals(AddressFamilyNumberEnum.DISTINGUISHED_NAME, address.getAfi());
        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) address;

        assertEquals("", distinguishedNameAddress.getDistinguishedName());

    }

    @Test
    public void deserialize__DavidString() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("00 11 64 61 76 69 64 00"));

        assertEquals(AddressFamilyNumberEnum.DISTINGUISHED_NAME, address.getAfi());
        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) address;

        assertEquals("david", distinguishedNameAddress.getDistinguishedName());

    }

    @Test
    public void deserialize__inList() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 8 " + //
                "00 11 64 61 76 69 64 00"));

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispListLCAFAddress lispLCAFAddress = (LispListLCAFAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.LIST, lispLCAFAddress.getType());

        LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) lispLCAFAddress.getAddresses().get(0);

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
        LispDistinguishedNameAddress distinguishedName = new LispDistinguishedNameAddress("david");

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(distinguishedName));
        LispAddressSerializer.getInstance().serialize(buf, distinguishedName);
        ByteBuffer expectedBuf = hexToByteBuffer("00 11 64 61 76 69 64 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

}
