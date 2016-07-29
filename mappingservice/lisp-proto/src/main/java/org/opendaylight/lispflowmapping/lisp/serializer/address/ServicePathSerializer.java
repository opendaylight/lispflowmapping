/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

/**
 * Class to (de)serialize Service Path data type.
 *
 * @author Lorand Jakab
 *
 */
public class ServicePathSerializer extends LcafSerializer {

    private static final ServicePathSerializer INSTANCE = new ServicePathSerializer();

    // Private constructor prevents instantiation from other classes
    private ServicePathSerializer() {
    }

    public static ServicePathSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected byte getLcafType() {
        return LispCanonicalAddressFormatEnum.SERVICE_PATH.getLispCode();
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        return (short) (Length.SPI + Length.SI);
    }

    @Override
    protected short getAfi() {
        return (short) AddressFamily.LispCanonicalAddressFormat.getIntValue();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        ServicePath sp = (ServicePath) lispAddress.getAddress();
        buffer.put(ByteUtil.partialIntToByteArray(NumberUtil.asInt(
                sp.getServicePath().getServicePathId().getValue().intValue()), Length.SPI));
        buffer.put((byte) NumberUtil.asShort(sp.getServicePath().getServiceIndex()));
    }

    @Override
    protected Eid deserializeLcafEidData(ByteBuffer buffer, byte res2, short length, LispAddressSerializerContext ctx) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ServicePathLcaf.class);
        eb.setVirtualNetworkId(getVni(ctx));
        eb.setAddress(deserializeData(buffer));
        return eb.build();
    }

    private Address deserializeData(ByteBuffer buffer) {
        ServicePathBuilder spb = new ServicePathBuilder();
        byte[] spi = new byte[3];
        buffer.get(spi);
        spb.setServicePathId(new ServicePathIdType((long) ByteUtil.getPartialInt(spi)));
        spb.setServiceIndex((short) ByteUtil.getUnsignedByte(buffer));
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.ServicePathBuilder().setServicePath(spb.build()).build();
    }

    private interface Length {
        int SPI = 3;
        int SI = 1;
    }
}
