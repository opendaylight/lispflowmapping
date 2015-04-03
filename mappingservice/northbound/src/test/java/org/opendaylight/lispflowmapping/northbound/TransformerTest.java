/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.northbound;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.DstAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.SrcAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;




public class TransformerTest extends BaseTestCase{

    @Override
    @Before
    public void before() throws Exception {
        super.before();
    }

    @Test
    public void convertToLispAddress__ipv4() throws Exception {

    	LispAddress lispAddress = new LispIpv4Address("10.0.0.1");

    	LispAddressGeneric lispAddressGeneric = new LispAddressGeneric(lispAddress);

    	//assertEquals(lispAddress, new LispIpv4Address("10.0.0.1"));

    	assertEquals(lispAddress, LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric));

    }

    @Test
    public void transformLispAddressGeneric__ipv4() throws Exception {

    	LispAddressGeneric lispAddressGenericIn
    		= new LispAddressGeneric(AddressFamilyNumberEnum.IP.getIanaCode(),"10.0.0.1");

    	LispAddressContainer lispAddressContainer =
    			YangTransformerNB.transformLispAddress(
    					LispAddressConvertorNB.convertToLispAddress(lispAddressGenericIn));

    	LispAddressGeneric lispAddressGenericOut
    		= new LispAddressGeneric(YangTransformerNB.transformToLispAddress(lispAddressContainer));


    	assertEquals(lispAddressGenericIn.getIpAddress(),lispAddressGenericOut.getIpAddress());

    }

    @Test
    public void transformLispAddressGeneric__srcdst() throws Exception {

    	LispAddressGeneric lispAddressGenericIn = new LispAddressGeneric();

    	LispAddressGeneric lispAddressGenericSrc
			= new LispAddressGeneric(AddressFamilyNumberEnum.IP.getIanaCode(),"10.0.0.1");

    	LispAddressGeneric lispAddressGenericDst
			= new LispAddressGeneric(AddressFamilyNumberEnum.IP.getIanaCode(),"20.0.0.2");

    	lispAddressGenericIn.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
    	lispAddressGenericIn.setLcafType(LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());

    	lispAddressGenericIn.setSrcAddress(lispAddressGenericSrc);
    	lispAddressGenericIn.setDstAddress(lispAddressGenericDst);

    	lispAddressGenericIn.setSrcMaskLength((byte) 32);
    	lispAddressGenericIn.setDstMaskLength((byte) 32);


    	LispAddressContainer lispAddressContainer =
    			YangTransformerNB.transformLispAddress(
    					LispAddressConvertorNB.convertToLispAddress(lispAddressGenericIn));

    	LispAddressGeneric lispAddressGenericOut
    		= new LispAddressGeneric(YangTransformerNB.transformToLispAddress(lispAddressContainer));


    	assertEquals(lispAddressGenericIn.getSrcAddress().getIpAddress(),
    				 lispAddressGenericOut.getSrcAddress().getIpAddress());

    	assertEquals(lispAddressGenericIn.getDstAddress().getIpAddress(),
				     lispAddressGenericOut.getDstAddress().getIpAddress());

    }


    @Test
    public void transformLispAddressContainer__ipv4() throws Exception {

    	Ipv4Address ipv4AddressIn = new Ipv4Address("10.0.0.1");
        LispAFIAddress lispAFIAddressIn = new Ipv4AddressBuilder()
    		.setIpv4Address(ipv4AddressIn)
    		.setAfi(AddressFamilyNumberEnum.IP.getIanaCode())
    		.build();

        LispAddressContainer lispAddressContainerIn = LispAFIConvertor.toContainer(lispAFIAddressIn);

    	LispAddressGeneric lispAddressGeneric
    		= new LispAddressGeneric(YangTransformerNB.transformToLispAddress(lispAddressContainerIn));

    	LispAddressContainer lispAddressContainerOut
    		= YangTransformerNB.transformLispAddress(LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric));

        LispAFIAddress lispAFIAddressOut = LispAFIConvertor.toAFI(lispAddressContainerOut);

        org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv4Address lispIpv4AddressOut;
        lispIpv4AddressOut = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispIpv4Address) lispAFIAddressOut;


    	assertEquals(ipv4AddressIn.getValue(),
    				 lispIpv4AddressOut.getIpv4Address().getValue());

    }


    @Test
    public void transformLispAddressContainer__srcdst() throws Exception {

    	Ipv4Address ipv4AddressSrcIn = new Ipv4Address("10.0.0.1");
        LispAFIAddress lispAFIAddressSrc = new Ipv4AddressBuilder()
    		.setIpv4Address(ipv4AddressSrcIn)
    		.setAfi(AddressFamilyNumberEnum.IP.getIanaCode())
    		.build();

    	Ipv4Address ipv4AddressDstIn = new Ipv4Address("20.0.0.2");
        LispAFIAddress lispAFIAddressDst = new Ipv4AddressBuilder()
    		.setIpv4Address(ipv4AddressDstIn)
    		.setAfi(AddressFamilyNumberEnum.IP.getIanaCode())
    		.build();

    	SrcAddress srcAddress = new SrcAddressBuilder()
			.setPrimitiveAddress(YangTransformerNB.toPrimitive(lispAFIAddressSrc))
			.build();

    	DstAddress dstAddress = new DstAddressBuilder()
			.setPrimitiveAddress(YangTransformerNB.toPrimitive(lispAFIAddressDst))
			.build();

        LispAFIAddress lispAFIAddressIn = new LcafSourceDestAddrBuilder()
			.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode())
			.setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode())
			.setSrcMaskLength((short) 32)
			.setDstMaskLength((short) 32)
			.setSrcAddress(srcAddress)
			.setDstAddress(dstAddress)
			.build();

        LispAddressContainer lispAddressContainerIn = LispAFIConvertor.toContainer(lispAFIAddressIn);

    	LispAddressGeneric lispAddressGeneric
    		= new LispAddressGeneric(YangTransformerNB.transformToLispAddress(lispAddressContainerIn));

    	LispAddressContainer lispAddressContainerOut
    		= YangTransformerNB.transformLispAddress(LispAddressConvertorNB.convertToLispAddress(lispAddressGeneric));

        LispAFIAddress lispAFIAddressOut = LispAFIConvertor.toAFI(lispAddressContainerOut);


    	org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSourceDestAddress lcafSourceDestAddressOut;
		lcafSourceDestAddressOut = (org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LcafSourceDestAddress) lispAFIAddressOut;

		Ipv4Address ipv4AddressSrcOut
			= ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4)
					lcafSourceDestAddressOut.getSrcAddress().getPrimitiveAddress())
					.getIpv4Address().getIpv4Address();

		Ipv4Address ipv4AddressDstOut
		= ((org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.Ipv4)
				lcafSourceDestAddressOut.getDstAddress().getPrimitiveAddress())
				.getIpv4Address().getIpv4Address();


    	assertEquals(ipv4AddressSrcIn.getValue(),ipv4AddressSrcOut.getValue());

    	assertEquals(ipv4AddressDstIn.getValue(),ipv4AddressDstOut.getValue());

    }


}
