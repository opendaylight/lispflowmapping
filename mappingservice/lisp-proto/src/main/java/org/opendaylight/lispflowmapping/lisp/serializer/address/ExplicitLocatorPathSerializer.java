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
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop.LrsBits;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class ExplicitLocatorPathSerializer extends LcafSerializer {

    private static final ExplicitLocatorPathSerializer INSTANCE = new ExplicitLocatorPathSerializer();

    // Private constructor prevents instantiation from other classes
    private ExplicitLocatorPathSerializer() {
    }

    public static ExplicitLocatorPathSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected byte getLcafType() {
        return LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode();
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        short totalSize = 0;
        ExplicitLocatorPath elp = (ExplicitLocatorPath) lispAddress.getAddress();
        if (elp != null) {
            for (Hop hop : elp.getExplicitLocatorPath().getHop()) {
                totalSize += SimpleAddressSerializer.getInstance().getAddressSize(hop.getAddress()) + 2;
            }
        }
        return totalSize;
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.LispCanonicalAddressFormat.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        ExplicitLocatorPath elp = (ExplicitLocatorPath) lispAddress.getAddress();
        if (elp.getExplicitLocatorPath().getHop() != null) {
            for (Hop hop : elp.getExplicitLocatorPath().getHop()) {
                buffer.put((byte) 0);
                buffer.put((byte) (ByteUtil.boolToBit(BooleanUtils.isTrue(hop.getLrsBits().isLookup()), Flags.LOOKUP) | //
                        ByteUtil.boolToBit(BooleanUtils.isTrue(hop.getLrsBits().isRlocProbe()), Flags.RLOC_PROBE) | //
                ByteUtil.boolToBit(BooleanUtils.isTrue(hop.getLrsBits().isStrict()), Flags.STRICT)));
                SimpleAddressSerializer.getInstance().serialize(buffer, hop.getAddress());
            }
        }
    }

    @Override
    protected Eid deserializeLcafEidData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ExplicitLocatorPathLcaf.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(deserializeData(buffer, length, ctx));
        return eb.build();
    }

    @Override
    protected Rloc deserializeLcafRlocData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        RlocBuilder rb = new RlocBuilder();
        rb.setAddressType(ExplicitLocatorPathLcaf.class);
        rb.setVirtualNetworkId(null);
        rb.setAddress(deserializeData(buffer, length, ctx));
        return rb.build();
    }

    private Address deserializeData(ByteBuffer buffer, short length, LispAddressSerializerContext ctx) {
        List<Hop> hops = new ArrayList<Hop>();
        while (length > 0) {
            byte flags = (byte) buffer.getShort();
            boolean lookup = ByteUtil.extractBit(flags, Flags.LOOKUP);
            boolean rlocProbe = ByteUtil.extractBit(flags, Flags.RLOC_PROBE);
            boolean strict = ByteUtil.extractBit(flags, Flags.STRICT);
            SimpleAddress address = SimpleAddressSerializer.getInstance().deserialize(buffer, ctx);
            HopBuilder builder = new HopBuilder();
            builder.setLrsBits(new LrsBits(lookup, rlocProbe, strict));
            builder.setAddress(address);
            builder.setHopId("Hop " + (hops.size()+1));
            length -= SimpleAddressSerializer.getInstance().getAddressSize(address) + 2;
            hops.add(builder.build());
        }
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPathBuilder()
                .setExplicitLocatorPath(new ExplicitLocatorPathBuilder().setHop(hops).build()).build();
    }

    private interface Flags {
        int LOOKUP = 0x04;
        int RLOC_PROBE = 0x02;
        int STRICT = 0x01;
    }

}
