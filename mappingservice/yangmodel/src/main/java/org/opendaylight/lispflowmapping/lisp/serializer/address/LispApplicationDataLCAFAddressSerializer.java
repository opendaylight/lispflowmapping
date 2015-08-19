/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafapplicationdataaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafapplicationdata.LcafApplicationDataAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;

public class LispApplicationDataLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispApplicationDataLCAFAddressSerializer INSTANCE = new LispApplicationDataLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispApplicationDataLCAFAddressSerializer() {
    }

    public static LispApplicationDataLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        return (short) (Length.ALL_FIELDS + LispAddressSerializer.getInstance().getAddressSize(
                LispAFIConvertor.toAFIfromPrimitive(((LcafApplicationDataAddress) lispAddress).getAddress().getPrimitiveAddress())));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LcafApplicationDataAddress applicationDataAddress = ((LcafApplicationDataAddress) lispAddress);
        buffer.put(ByteUtil.partialIntToByteArray(NumberUtil.asInt(applicationDataAddress.getIpTos()), Length.TOC));
        buffer.put((byte) NumberUtil.asShort(applicationDataAddress.getProtocol()));
        if (applicationDataAddress.getLocalPortLow() != null) {
            buffer.putShort(NumberUtil.asShort(applicationDataAddress.getLocalPortLow().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        if (applicationDataAddress.getLocalPortHigh() != null) {
            buffer.putShort(NumberUtil.asShort(applicationDataAddress.getLocalPortHigh().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        if (applicationDataAddress.getRemotePortLow() != null) {
            buffer.putShort(NumberUtil.asShort(applicationDataAddress.getRemotePortLow().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        if (applicationDataAddress.getRemotePortHigh() != null) {
            buffer.putShort(NumberUtil.asShort(applicationDataAddress.getRemotePortHigh().getValue().shortValue()));
        } else {
            buffer.putShort((short) 0);
        }
        LispAddressSerializer.getInstance().serialize(buffer,
                LispAFIConvertor.toAFIfromPrimitive(((LcafApplicationDataAddress) lispAddress).getAddress().getPrimitiveAddress()));
    }

    @Override
    protected LcafApplicationDataAddress deserializeData(ByteBuffer buffer, byte res2, short length) {

        LcafApplicationDataAddrBuilder builder = new LcafApplicationDataAddrBuilder();
        byte[] rawIPTos = new byte[3];
        buffer.get(rawIPTos);
        builder.setIpTos(ByteUtil.getPartialInt(rawIPTos));
        builder.setProtocol((short) ByteUtil.getUnsignedByte(buffer));
        builder.setLocalPortLow(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        builder.setLocalPortHigh(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        builder.setRemotePortLow(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        builder.setRemotePortHigh(new PortNumber(ByteUtil.asUnsignedShort(buffer.getShort())));
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode())
                .setAddress(new AddressBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(address)).build());

        return builder.build();
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
