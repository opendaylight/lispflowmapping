/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;

public class MacSerializer extends LispAddressSerializer {

    private static final MacSerializer INSTANCE = new MacSerializer();

    // Private constructor prevents instantiation from other classes
    private MacSerializer() {
    }

    public static MacSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAddress lispAddress) {
        return Length.MAC;
    }

    @Override
    protected LispAddress deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        byte[] macBuffer = new byte[6];
        buffer.get(macBuffer);
        StringBuilder sb = new StringBuilder(17);
        for (byte b : macBuffer) {
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        LispAddressContainerBuilder lab = new LispAddressContainerBuilder();
        lab.setAddressType(MacAfi.class);
        lab.setVirtualNetworkId(ctx.getVni());
        lab.setAddress(new MacBuilder().setMac(new MacAddress(sb.toString())).build());
        return lab.build();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Mac mac = (Mac) lispAddress.getAddress();
        String macString = mac.getMac().getValue();
        macString = macString.replaceAll(":", "");
        buffer.put(DatatypeConverter.parseHexBinary(macString));
    }

    private interface Length {
        int MAC = 6;
    }

}
