/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.northbound;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispASAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;
import org.opendaylight.lispflowmapping.type.lisp.address.LispApplicationDataLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispDistinguishedNameAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispMACAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSourceDestLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispTrafficEngineeringLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.ReencapHop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispAddressConvertorNB {

	protected static final Logger logger = LoggerFactory.getLogger(LispAddressConvertorNB.class);


public static LispAddress convertToLispAddress(LispAddressGeneric generic){

	if (generic == null){
		logger.warn("Couldn't convert address, generic address is null");
		return null;
	}

    	LispAddress lispAddress = null;

    	AddressFamilyNumberEnum afiEnum  = AddressFamilyNumberEnum.valueOf((short) generic.getAfi());

    	switch (afiEnum) {

    	case IP:
    		lispAddress = new LispIpv4Address(generic.getIpAddress());
        	if (((LispIpv4Address)lispAddress).getAddress() == null){
        		throw new IllegalArgumentException("Convertor was unable to convert address");
        	}
    		break;
		case AS:
			lispAddress = new LispASAddress(generic.getAsNum());
			break;
		case IP6:
			lispAddress = new LispIpv6Address(generic.getIpAddress());
        	if (((LispIpv6Address)lispAddress).getAddress() == null){
        		throw new IllegalArgumentException("Convertor was unable to convert address");
        	}
			break;
		case MAC:
			lispAddress = new LispMACAddress(generic.getMac());
			break;
		case DISTINGUISHED_NAME:
			lispAddress = new LispDistinguishedNameAddress(generic.getDistinguishedName());
			break;
		case LCAF:
			LispCanonicalAddressFormatEnum lcafEnum =
				LispCanonicalAddressFormatEnum.valueOf(generic.getLcafType());
			switch (lcafEnum){
			case APPLICATION_DATA:
				lispAddress = convertToApplicationData(generic);
				break;
			case LIST:
				lispAddress = convertToList(generic);
				break;
			case SEGMENT:
				lispAddress = convertToSegment(generic);
				break;
			case SOURCE_DEST:
				lispAddress = convertToSrcDst(generic);
				break;
			case TRAFFIC_ENGINEERING:
				lispAddress = convertToTE(generic);
				break;
			default:
				throw new IllegalArgumentException("LCAF type " + lcafEnum +
						" not supported by this convertor: convertToLispAddress(LispAddressGeneric generic)");
			}
			break;
		default:
			throw new IllegalArgumentException("AFI " + afiEnum +
					" not supported by this convertor: convertToLispAddress(LispAddressGeneric generic)");
    	}



    	return lispAddress;
    }

private static LispApplicationDataLCAFAddress convertToApplicationData(LispAddressGeneric generic){
	return new LispApplicationDataLCAFAddress(
			(byte) 0, generic.getProtocol(), generic.getIpTos(), generic.getLocalPort(),
			generic.getRemotePort(), convertToLispAddress(generic.getAddress()));
}

private static LispListLCAFAddress convertToList(LispAddressGeneric generic){
	List<LispAddress> list = new ArrayList<LispAddress>();

	for (int i=0;i<generic.getAddresses().size();i++){
		list.add(convertToLispAddress(generic.getAddresses().get(i)));
	}

	return new LispListLCAFAddress((byte) 0, list);
}

private static LispSegmentLCAFAddress convertToSegment(LispAddressGeneric generic){
	byte idMaskLen = 0; //Not used here
	return new LispSegmentLCAFAddress(idMaskLen, generic.getInstanceId(),
			convertToLispAddress(generic.getAddress()));
}

private static LispSourceDestLCAFAddress convertToSrcDst(LispAddressGeneric generic){
	return new LispSourceDestLCAFAddress((byte)0, (short)0, generic.getSrcMaskLength(),
			generic.getDstMaskLength(),	convertToLispAddress(generic.getSrcAddress()),
			convertToLispAddress(generic.getDstAddress()));
}



private static LispTrafficEngineeringLCAFAddress convertToTE(LispAddressGeneric generic){
	List<ReencapHop> listHops = new ArrayList<ReencapHop>();
	ReencapHop hop;

	for (int i=0;i<generic.getHops().size();i++){
		hop = new ReencapHop(
				convertToLispAddress(generic.getHops().get(i).getAddress()), (short) 0,
				generic.getHops().get(i).isLookup(), generic.getHops().get(i).isRLOCProbe(), generic.getHops().get(i).isStrict());
		listHops.add(hop);
	}
	return new LispTrafficEngineeringLCAFAddress((byte)0, listHops);
}





public static void convertGenericToLispAddresses(MapRegister mapRegister){
	LispAddress EID;
	LispAddressGeneric EIDGeneric;

	List<LocatorRecord> locRecordList;
	LispAddress RLOC;
	LispAddressGeneric RLOCGeneric;

	if (mapRegister == null){
		logger.warn("Couldn't convert addresses, mapRegister is null");
		return;
	}

	int EIDtoLocatorRecordCount = mapRegister.getEidToLocatorRecords().size();

	for (int i=0;i<EIDtoLocatorRecordCount;i++){
    	EIDGeneric = mapRegister.getEidToLocatorRecords().get(i).getPrefixGeneric();

    	EID = convertToLispAddress(EIDGeneric);

    	mapRegister.getEidToLocatorRecords().get(i).setPrefix(EID);
    	locRecordList = mapRegister.getEidToLocatorRecords().get(i).getLocators();

    	for(int j=0;j<locRecordList.size();j++){

    		RLOCGeneric = locRecordList.get(j).getLocatorGeneric();

    		RLOC = convertToLispAddress(RLOCGeneric);
    		locRecordList.get(j).setLocator(RLOC);
    	}

	}
}


public static void convertRecordToGenericAddress(EidToLocatorRecord record){

	LispAddress EID;
	List<LocatorRecord> locRecordList;
	LispAddress RLOC;

	if (record == null){
		logger.warn("Couldn't convert addresses, record is null");
		return;
	}

	EID = record.getPrefix();
    record.setPrefixGeneric(new LispAddressGeneric(EID));

    locRecordList = record.getLocators();
    for(int j=0;j<locRecordList.size();j++){

    	RLOC = locRecordList.get(j).getLocator();
    	locRecordList.get(j).setLocatorGeneric(new LispAddressGeneric(RLOC));
    }
}



}
