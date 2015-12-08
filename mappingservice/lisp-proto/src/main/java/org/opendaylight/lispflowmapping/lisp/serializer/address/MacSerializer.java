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

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public final class MacSerializer extends LispAddressSerializer {

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
    public int getAddressSize(SimpleAddress simpleAddress) {
        return Length.MAC;
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily._48BitMac.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        Mac mac = (Mac) lispAddress.getAddress();
        String macString = mac.getMac().getValue();
        macString = macString.replaceAll(":", "");
        buffer.put(DatatypeConverter.parseHexBinary(macString));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, SimpleAddress simpleAddress) {
        String macString = simpleAddress.getMacAddress().getValue();
        macString = macString.replaceAll(":", "");
        buffer.put(DatatypeConverter.parseHexBinary(macString));
    }

    @Override
    protected Eid deserializeEidData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(MacAfi.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(new MacBuilder().setMac(deserializeData(buffer)).build());
        return eb.build();
    }

    @Override
    protected Rloc deserializeRlocData(ByteBuffer buffer) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(MacAfi.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(new MacBuilder().setMac(deserializeData(buffer)).build());
        return rb.build();
    }

    @Override
    protected SimpleAddress deserializeSimpleAddressData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        return new SimpleAddress(deserializeData(buffer));
    }

    private MacAddress deserializeData(ByteBuffer buffer) {
        byte[] macBuffer = new byte[6];
        buffer.get(macBuffer);
        StringBuilder sb = new StringBuilder(17);
        for (byte b : macBuffer) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", b));
        }
        return new MacAddress(sb.toString());
    }

    private interface Length {
        int MAC = 6;
    }

}
