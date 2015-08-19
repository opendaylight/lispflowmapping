/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;

public class LispIpv4AddressSerializer extends LispAddressSerializer {

    private static final LispIpv4AddressSerializer INSTANCE = new LispIpv4AddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispIpv4AddressSerializer() {
    }

    public static LispIpv4AddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return Length.IPV4;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispIpv4Address lispIpvAddress = (LispIpv4Address) lispAddress;
        try {
            buffer.put(Inet4Address.getByName(lispIpvAddress.getIpv4Address().getValue()).getAddress());
        } catch (UnknownHostException e) {
        }
    }

    @Override
    protected LispIpv4Address deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[4];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
        }
        return new Ipv4AddressBuilder().setIpv4Address(new Ipv4Address(address.getHostAddress())).setAfi(AddressFamilyNumberEnum.IP.getIanaCode()).build();
    }

    private interface Length {
        int IPV4 = 4;
    }

}
