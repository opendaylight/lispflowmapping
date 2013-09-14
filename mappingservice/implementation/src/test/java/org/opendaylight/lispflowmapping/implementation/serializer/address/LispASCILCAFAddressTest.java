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
import org.opendaylight.lispflowmapping.type.lisp.address.LispASCILCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispDistinguishedNameAddress;

public class LispASCILCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 8 " + //
                "00 11 64 61 76 69 64 00"));

        assertEquals(AddressFamilyNumberEnum.LCAF, address.getAfi());
        LispASCILCAFAddress asciAddress = (LispASCILCAFAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.BASEONE, asciAddress.getType());

        assertEquals("david", asciAddress.getdistinguishedName().getDistinguishedName());

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
    public void serialize__Simple() throws Exception {
        LispDistinguishedNameAddress distinguishedName = new LispDistinguishedNameAddress("david");
        LispASCILCAFAddress address = new LispASCILCAFAddress((byte) 0, distinguishedName);

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(address));
        LispAddressSerializer.getInstance().serialize(buf, address);
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 8 " + //
                "00 11 64 61 76 69 64 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
