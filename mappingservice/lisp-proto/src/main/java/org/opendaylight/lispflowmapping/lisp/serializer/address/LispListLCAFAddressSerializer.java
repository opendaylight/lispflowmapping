/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.LcafListAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lcaflistaddress.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lcaflistaddress.AddressesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispaddress.lispaddresscontainer.address.lcaflist.LcafListAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispsimpleaddress.PrimitiveAddress;

public class LispListLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispListLCAFAddressSerializer INSTANCE = new LispListLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispListLCAFAddressSerializer() {
    }

    public static LispListLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        short totalSize = 0;
        for (Addresses address : ((LcafListAddress) lispAddress).getAddresses()) {
            totalSize += LispAddressSerializer.getInstance().getAddressSize(LispAFIConvertor.toAFIfromPrimitive(address.getPrimitiveAddress()));
        }
        return totalSize;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        for (Addresses address : ((LcafListAddress) lispAddress).getAddresses()) {
            LispAddressSerializer.getInstance().serialize(buffer, LispAFIConvertor.toAFIfromPrimitive(address.getPrimitiveAddress()));
        }
    }

    @Override
    public LcafListAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        List<Addresses> addresses = new ArrayList<Addresses>();
        while (length > 0) {
            PrimitiveAddress address = LispAFIConvertor.toPrimitive(LispAddressSerializer.getInstance().deserialize(buffer));
            length -= LispAddressSerializer.getInstance().getAddressSize(LispAFIConvertor.toAFIfromPrimitive(address));
            addresses.add(new AddressesBuilder().setName("Address " + (addresses.size()+1))
                    .setPrimitiveAddress((PrimitiveAddress) address).build());
        }
        return new LcafListAddrBuilder().setAddresses(addresses).setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.LIST.getLispCode()).build();
    }
}
