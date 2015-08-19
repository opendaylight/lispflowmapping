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
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafKeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafkeyvalueaddress.KeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafkeyvalueaddress.ValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafkeyvalue.LcafKeyValueAddressAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.PrimitiveAddress;

public class LispKeyValueLCAFSerializer extends LispLCAFAddressSerializer {

    private static final LispKeyValueLCAFSerializer INSTANCE = new LispKeyValueLCAFSerializer();

    // Private constructor prevents instantiation from other classes
    private LispKeyValueLCAFSerializer() {
    }

    public static LispKeyValueLCAFSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        return (short) (LispAddressSerializer.getInstance().getAddressSize(
                LispAFIConvertor.toAFIfromPrimitive(((LcafKeyValueAddress) lispAddress).getKey().getPrimitiveAddress())) + LispAddressSerializer.getInstance()
                .getAddressSize(LispAFIConvertor.toAFIfromPrimitive(((LcafKeyValueAddress) lispAddress).getValue().getPrimitiveAddress())));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LcafKeyValueAddress lispKeyValueLCAFAddress = ((LcafKeyValueAddress) lispAddress);
        LispAddressSerializer.getInstance().serialize(buffer, LispAFIConvertor.toAFIfromPrimitive(lispKeyValueLCAFAddress.getKey().getPrimitiveAddress()));
        LispAddressSerializer.getInstance().serialize(buffer, LispAFIConvertor.toAFIfromPrimitive(lispKeyValueLCAFAddress.getValue().getPrimitiveAddress()));
    }

    @Override
    protected LcafKeyValueAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        LispAFIAddress keyAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LispAFIAddress valueAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LcafKeyValueAddressAddrBuilder builder = new LcafKeyValueAddressAddrBuilder();
        builder.setKey(new KeyBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(keyAddress)).build());
        builder.setValue(new ValueBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(valueAddress)).build());
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode());

        return builder.build();
    }

}
