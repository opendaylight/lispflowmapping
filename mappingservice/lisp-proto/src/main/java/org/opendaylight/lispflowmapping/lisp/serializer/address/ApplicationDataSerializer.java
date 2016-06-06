/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ApplicationDataLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.application.data.ApplicationData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.application.data.ApplicationDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public final class ApplicationDataSerializer extends LcafSerializer {

    private static final ApplicationDataSerializer INSTANCE = new ApplicationDataSerializer();

    // Private constructor prevents instantiation from other classes
    private ApplicationDataSerializer() {
    }

    public static ApplicationDataSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected byte getLcafType() {
        return LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode();
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        ApplicationData appData = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                .rev151105.lisp.address.address.ApplicationData) lispAddress.getAddress()).getApplicationData();
        return (short) (Length.ALL_FIELDS + SimpleAddressSerializer.getInstance().getAddressSize(appData.getAddress()));
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.LispCanonicalAddressFormat.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        ApplicationData appData = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                .rev151105.lisp.address.address.ApplicationData) lispAddress.getAddress()).getApplicationData();
        buffer.put(ByteUtil.partialIntToByteArray(NumberUtil.asInt(appData.getIpTos()), Length.TOC));
        buffer.put((byte) NumberUtil.asShort(appData.getProtocol()));
        if (appData.getLocalPortLow() != null) {
            buffer.putShort(NumberUtil.asShort(appData.getLocalPortLow().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        if (appData.getLocalPortHigh() != null) {
            buffer.putShort(NumberUtil.asShort(appData.getLocalPortHigh().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        if (appData.getRemotePortLow() != null) {
            buffer.putShort(NumberUtil.asShort(appData.getRemotePortLow().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        if (appData.getRemotePortHigh() != null) {
            buffer.putShort(NumberUtil.asShort(appData.getRemotePortHigh().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        SimpleAddressSerializer.getInstance().serialize(buffer, appData.getAddress());
    }

    @Override
    protected Eid deserializeLcafEidData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ApplicationDataLcaf.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(deserializeData(buffer, ctx));
        return eb.build();
    }

    @Override
    protected Rloc deserializeLcafRlocData(ByteBuffer buffer, byte res2, short length,
            LispAddressSerializerContext ctx) {
        RlocBuilder eb = new RlocBuilder();
        eb.setAddressType(ApplicationDataLcaf.class);
        eb.setVirtualNetworkId(null);
        eb.setAddress(deserializeData(buffer, ctx));
        return eb.build();
    }

    private Address deserializeData(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        ApplicationDataBuilder builder = new ApplicationDataBuilder();
        byte[] rawIPTos = new byte[3];
        buffer.get(rawIPTos);
        builder.setIpTos(ByteUtil.getPartialInt(rawIPTos));
        builder.setProtocol((short) ByteUtil.getUnsignedByte(buffer));
        builder.setLocalPortLow(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        builder.setLocalPortHigh(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        builder.setRemotePortLow(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        builder.setRemotePortHigh(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        SimpleAddress address = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
        builder.setAddress(address);
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ApplicationDataBuilder().setApplicationData(builder.build()).build();
    }

    private interface Length {
        int LOCAL_PORT_LOW = 2;
        int LOCAL_PORT_HIGH = 2;
        int REMOTE_PORT_LOW = 2;
        int REMOTE_PORT_HIGH = 2;
        int TOC = 3;
        int PROTOCOL = 1;
        int ALL_FIELDS = LOCAL_PORT_LOW + LOCAL_PORT_HIGH + REMOTE_PORT_LOW + REMOTE_PORT_HIGH + TOC + PROTOCOL;
    }

}
