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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AfiListLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AfiList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class LispDistinguishedNameAddressSerializerTest extends BaseTestCase {

    @Test
    public void deserialize__EmptyString() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("00 11 00"), null);

        assertEquals(DistinguishedNameAfi.class, address.getAddressType());
        DistinguishedName distinguishedName = (DistinguishedName) address.getAddress();

        assertEquals("", distinguishedName.getDistinguishedName().getValue());

    }

    @Test
    public void deserialize__DavidString() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("00 11 64 61 76 69 64 00"),
                null);

        assertEquals(DistinguishedNameAfi.class, address.getAddressType());
        DistinguishedName distinguishedName = (DistinguishedName) address.getAddress();

        assertEquals("david", distinguishedName.getDistinguishedName().getValue());

    }

    @Test
    public void deserialize__inList() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 8 " + //
                "00 11 64 61 76 69 64 00"), null);

        assertEquals(AfiListLcaf.class, address.getAddressType());
        assertEquals("david", ((AfiList) address.getAddress()).getAfiList().getAddressList().get(0)
                .getDistinguishedNameType().getValue());

    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 10 " + //
                "00 11 64 61 76 69 64 00"), null);
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("40 03 00 00 " + //
                "01 00 00 18 "), null);
    }

    @Test
    public void deserialize__ReadUntilZero() throws Exception {
        Eid address = LispAddressSerializer.getInstance().deserializeEid(hexToByteBuffer("00 11 64 61 76 00 69 64"),
                null);

        DistinguishedName distinguishedName = (DistinguishedName) address.getAddress();

        assertEquals("dav", distinguishedName.getDistinguishedName().getValue());
    }

    @Test
    public void serialize__Simple() throws Exception {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(DistinguishedNameAfi.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress(new DistinguishedNameBuilder().setDistinguishedName(new DistinguishedNameType("david")).build());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(eb.build()));
        LispAddressSerializer.getInstance().serialize(buf, eb.build());
        ByteBuffer expectedBuf = hexToByteBuffer("00 11 64 61 76 69 64 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

}
