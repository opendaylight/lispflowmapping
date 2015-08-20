/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispIpv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;

public class LispIpv6AddressSerializer extends LispAddressSerializer {

    private static final LispIpv6AddressSerializer INSTANCE = new LispIpv6AddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispIpv6AddressSerializer() {
    }

    public static LispIpv6AddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return Length.IPV6;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispIpv6Address lispIpvAddress = (LispIpv6Address) lispAddress;
        try {
            buffer.put(Inet6Address.getByName(lispIpvAddress.getIpv6Address().getValue()).getAddress());
        } catch (UnknownHostException e) {
        }
    }

    @Override
    protected LispIpv6Address deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[16];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
        }
        return new Ipv6AddressBuilder().setIpv6Address(new Ipv6Address(address.getHostAddress())).setAfi((short) AddressFamilyNumberEnum.IP6.getIanaCode())
                .build();
    }

    private interface Length {
        int IPV6 = 16;
    }

}
