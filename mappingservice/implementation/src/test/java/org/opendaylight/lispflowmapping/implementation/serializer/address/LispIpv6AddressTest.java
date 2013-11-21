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
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6;

public class LispIpv6AddressTest extends BaseTestCase {

    @Test
    public void constructor__Name() throws Exception {
        Ipv6 ip = LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:0");

        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ip.getAfi().shortValue());
        assertEquals(18, LispAddressSerializer.getInstance().getAddressSize(ip));
        assertEquals("0:0:0:0:0:0:0:0", ip.getIpv6Address().getValue());
    }

    @Test
    public void constructor__Buffer() throws Exception {
        Ipv6 ip = LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1");

        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ip.getAfi().shortValue());
        assertEquals(18, LispAddressSerializer.getInstance().getAddressSize(ip));
        assertEquals("0:0:0:0:0:0:0:1", ip.getIpv6Address().getValue());
    }
}
