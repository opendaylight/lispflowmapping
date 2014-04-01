/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafKeyValueAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafkeyvalueaddress.KeyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafkeyvalueaddress.ValueBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafKeyValueBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.PrimitiveAddress;

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
                (LispAFIAddress) ((LcafKeyValueAddress) lispAddress).getKey().getPrimitiveAddress()) + LispAddressSerializer.getInstance()
                .getAddressSize((LispAFIAddress) ((LcafKeyValueAddress) lispAddress).getValue().getPrimitiveAddress()));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LcafKeyValueAddress lispKeyValueLCAFAddress = ((LcafKeyValueAddress) lispAddress);
        LispAddressSerializer.getInstance().serialize(buffer, (LispAFIAddress) lispKeyValueLCAFAddress.getKey().getPrimitiveAddress());
        LispAddressSerializer.getInstance().serialize(buffer, (LispAFIAddress) lispKeyValueLCAFAddress.getValue().getPrimitiveAddress());
    }

    @Override
    protected LcafKeyValueAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        LispAFIAddress keyAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LispAFIAddress valueAddress = LispAddressSerializer.getInstance().deserialize(buffer);
        LcafKeyValueBuilder builder = new LcafKeyValueBuilder();
        builder.setKey(new KeyBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(keyAddress)).build());
        builder.setValue(new ValueBuilder().setPrimitiveAddress((PrimitiveAddress) LispAFIConvertor.toPrimitive(valueAddress)).build());
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode());

        return builder.build();
    }

}
