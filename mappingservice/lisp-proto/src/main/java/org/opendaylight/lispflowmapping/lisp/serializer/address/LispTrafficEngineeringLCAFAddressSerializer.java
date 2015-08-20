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

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.HopsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcaftrafficengineering.LcafTrafficEngineeringAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispsimpleaddress.PrimitiveAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.reencaphop.HopBuilder;

public class LispTrafficEngineeringLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispTrafficEngineeringLCAFAddressSerializer INSTANCE = new LispTrafficEngineeringLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispTrafficEngineeringLCAFAddressSerializer() {
    }

    public static LispTrafficEngineeringLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAFIAddress lispAddress) {
        short totalSize = 0;
        if (((LcafTrafficEngineeringAddress) lispAddress).getHops() != null) {
            for (Hops hop : ((LcafTrafficEngineeringAddress) lispAddress).getHops()) {
                totalSize += LispAddressSerializer.getInstance().getAddressSize(LispAFIConvertor.toAFIfromPrimitive(hop.getHop().getPrimitiveAddress())) + 2;
            }
        }
        return totalSize;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        if (((LcafTrafficEngineeringAddress) lispAddress).getHops() != null) {
            for (Hops hop : ((LcafTrafficEngineeringAddress) lispAddress).getHops()) {
                buffer.put((byte) 0);
                buffer.put((byte) (ByteUtil.boolToBit(BooleanUtils.isTrue(hop.isLookup()), Flags.LOOKUP) | //
                        ByteUtil.boolToBit(BooleanUtils.isTrue(hop.isRLOCProbe()), Flags.RLOC_PROBE) | //
                ByteUtil.boolToBit(BooleanUtils.isTrue(hop.isStrict()), Flags.STRICT)));
                LispAddressSerializer.getInstance().serialize(buffer, LispAFIConvertor.toAFIfromPrimitive(hop.getHop().getPrimitiveAddress()));
            }
        }
    }

    @Override
    protected LcafTrafficEngineeringAddress deserializeData(ByteBuffer buffer, byte res2, short length) {
        List<Hops> hops = new ArrayList<Hops>();
        while (length > 0) {
            byte flags = (byte) buffer.getShort();
            boolean lookup = ByteUtil.extractBit(flags, Flags.LOOKUP);
            boolean RLOCProbe = ByteUtil.extractBit(flags, Flags.RLOC_PROBE);
            boolean strict = ByteUtil.extractBit(flags, Flags.STRICT);
            PrimitiveAddress address = LispAFIConvertor.toPrimitive(LispAddressSerializer.getInstance().deserialize(buffer));
            HopsBuilder builder = new HopsBuilder();
            builder.setLookup(lookup);
            builder.setRLOCProbe(RLOCProbe);
            builder.setStrict(strict);
            builder.setHop(new HopBuilder().setPrimitiveAddress(address).build());
            builder.setName("Hop " + (hops.size()+1));
            length -= LispAddressSerializer.getInstance().getAddressSize(LispAFIConvertor.toAFIfromPrimitive(address)) + 2;
            hops.add(builder.build());
        }
        return new LcafTrafficEngineeringAddrBuilder().setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
                .setLcafType((short) LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode()).setHops(hops).build();
    }

    private interface Flags {
        int LOOKUP = 0x04;
        int RLOC_PROBE = 0x02;
        int STRICT = 0x01;
    }

}
