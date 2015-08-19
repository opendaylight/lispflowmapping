/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.serializer.address;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address;

public class LispIpv6AddressTest extends BaseTestCase {

    @Test
    public void constructor__Name() throws Exception {
        Ipv6Address ip = (Ipv6Address) LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:0");

        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ip.getAfi().shortValue());
        assertEquals(18, LispAddressSerializer.getInstance().getAddressSize(ip));
        assertEquals("0:0:0:0:0:0:0:0", ip.getIpv6Address().getValue());
    }

    @Test
    public void constructor__Buffer() throws Exception {
        Ipv6Address ip = (Ipv6Address) LispAFIConvertor.asIPv6AfiAddress("0:0:0:0:0:0:0:1");

        assertEquals(AddressFamilyNumberEnum.IP6.getIanaCode(), ip.getAfi().shortValue());
        assertEquals(18, LispAddressSerializer.getInstance().getAddressSize(ip));
        assertEquals("0:0:0:0:0:0:0:1", ip.getIpv6Address().getValue());
    }
}
