/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.serializer.address;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.HopsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcaftrafficengineering.LcafTrafficEngineeringAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.reencaphop.HopBuilder;

public class LispTrafficEngineeringLCAFAddressTest extends BaseTestCase {

    @Test
    public void deserialize__Simple() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 00 00 01 AA BB CC DD " + // IPv4
                "00 00 00 01 11 22 33 44")); // IPv4

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafTrafficEngineeringAddress trafficEngineering = (LcafTrafficEngineeringAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode(), trafficEngineering.getLcafType().byteValue());

        List<Hops> hops = trafficEngineering.getHops();
        assertEquals(2, hops.size());

        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("170.187.204.221"), hops.get(0).getHop().getPrimitiveAddress());
        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68"), hops.get(1).getHop().getPrimitiveAddress());
    }

    @Test
    public void deserialize__Bits() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 05 00 01 AA BB CC DD " + // IPv4
                "00 02 00 01 11 22 33 44")); // IPv4

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafTrafficEngineeringAddress trafficEngineering = (LcafTrafficEngineeringAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode(), trafficEngineering.getLcafType().byteValue());

        List<Hops> hops = trafficEngineering.getHops();
        assertEquals(2, hops.size());

        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("170.187.204.221"), hops.get(0).getHop().getPrimitiveAddress());
        assertEquals(true, hops.get(0).isLookup().booleanValue());
        assertEquals(false, hops.get(0).isRLOCProbe().booleanValue());
        assertEquals(true, hops.get(0).isStrict().booleanValue());
        assertEquals(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68"), hops.get(1).getHop().getPrimitiveAddress());
        assertEquals(false, hops.get(1).isLookup().booleanValue());
        assertEquals(true, hops.get(1).isRLOCProbe().booleanValue());
        assertEquals(false, hops.get(1).isStrict().booleanValue());
    }

    @Test
    public void deserialize__NoAddresses() throws Exception {
        LispAFIAddress address = LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 00 "));

        assertEquals(AddressFamilyNumberEnum.LCAF.getIanaCode(), address.getAfi().shortValue());
        LcafTrafficEngineeringAddress trafficEngineering = (LcafTrafficEngineeringAddress) address;

        assertEquals(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode(), trafficEngineering.getLcafType().byteValue());

        List<Hops> hops = trafficEngineering.getHops();
        assertEquals(0, hops.size());
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 18 " + //
                "00 01 AA BB CC DD " + // IPv4
                "00 02 11 22 33 44 11 22 33 44 11 22 33 44"));
    }

    @Test(expected = LispSerializationException.class)
    public void deserialize__ShorterBuffer2() throws Exception {
        LispAddressSerializer.getInstance().deserialize(hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 18 "));
    }

    @Test
    public void serialize__Simple() throws Exception {
        List<Hops> hops = new ArrayList<Hops>();
        hops.add(new HopsBuilder().setHop(new HopBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("170.187.204.221")).build())
                .build());
        hops.add(new HopsBuilder().setHop(new HopBuilder().setPrimitiveAddress(LispAFIConvertor.asPrimitiveIPAfiAddress("17.34.51.68")).build())
                .build());
        LcafTrafficEngineeringAddrBuilder trafficBuilder = new LcafTrafficEngineeringAddrBuilder();
        trafficBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType(
                (short) LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode());
        trafficBuilder.setHops(hops);

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(trafficBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, trafficBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 10 " + //
                "00 00 00 01 AA BB CC DD " + // IPv4
                "00 00 00 01 11 22 33 44"); // IPv4
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }

    @Test
    public void serialize__NoAddresses() throws Exception {
        LcafTrafficEngineeringAddrBuilder addressBuilder = new LcafTrafficEngineeringAddrBuilder();
        addressBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType(
                (short) LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode());

        ByteBuffer buf = ByteBuffer.allocate(LispAddressSerializer.getInstance().getAddressSize(addressBuilder.build()));
        LispAddressSerializer.getInstance().serialize(buf, addressBuilder.build());
        ByteBuffer expectedBuf = hexToByteBuffer("40 03 00 00 " + //
                "0A 00 00 00");
        ArrayAssert.assertEquals(expectedBuf.array(), buf.array());
    }
}
