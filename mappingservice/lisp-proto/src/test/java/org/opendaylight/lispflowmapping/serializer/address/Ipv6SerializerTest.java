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

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class Ipv6SerializerTest extends BaseTestCase {

    @Test
    public void constructor__Name() throws Exception {
        Rloc rloc = LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:0");

        assertEquals(Ipv6BinaryAfi.class, rloc.getAddressType());
        assertEquals(18, LispAddressSerializer.getInstance().getAddressSize(rloc));
        assertArrayEquals(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                ((Ipv6Binary) rloc.getAddress()).getIpv6Binary().getValue());
    }

    @Test
    public void constructor__Buffer() throws Exception {
        Rloc rloc = LispAddressUtil.asIpv6Rloc("0:0:0:0:0:0:0:1");

        assertEquals(Ipv6BinaryAfi.class, rloc.getAddressType());
        assertEquals(18, LispAddressSerializer.getInstance().getAddressSize(rloc));
        assertArrayEquals(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                ((Ipv6Binary) rloc.getAddress()).getIpv6Binary().getValue());
    }
}
