/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.serializer.address;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;

public class LispIpv6AddressTest extends BaseTestCase {

    @Test
    public void constructor__Name() throws Exception {
        LispIpv6Address lispIpv6Address = new LispIpv6Address("::0");

        assertEquals(AddressFamilyNumberEnum.IP6, lispIpv6Address.getAfi());
        assertEquals(18, LispIpv6AddressSerializer.getInstance().getAddressSize(lispIpv6Address));
        assertEquals("0:0:0:0:0:0:0:0", lispIpv6Address.toString());
    }

    @Test
    public void constructor__Buffer() throws Exception {
        LispIpv6Address lispIpv6Address = new LispIpv6Address(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });

        assertEquals(AddressFamilyNumberEnum.IP6, lispIpv6Address.getAfi());
        assertEquals(18, LispIpv6AddressSerializer.getInstance().getAddressSize(lispIpv6Address));
        assertEquals("0:0:0:0:0:0:0:1", lispIpv6Address.toString());
    }
}
