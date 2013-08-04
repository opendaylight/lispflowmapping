package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispAddressSerializerFactory {
	
	private static Map<AddressFamilyNumberEnum, LispAddressSerializer> afiToSearializerMap;
	private static Map<LispCanonicalAddressFormatEnum, LispLCAFAddressSerializer> lcafToSearializerMap;
	
	private static void initializeMap() {
		afiToSearializerMap = new HashMap<AddressFamilyNumberEnum, LispAddressSerializer>();
		afiToSearializerMap.put(AddressFamilyNumberEnum.AS, LispASAddressSerializer.getInstance());
		afiToSearializerMap.put(AddressFamilyNumberEnum.IP, LispIpv4AddressSerializer.getInstance());
		afiToSearializerMap.put(AddressFamilyNumberEnum.IP6, LispIpv6AddressSerializer.getInstance());
		afiToSearializerMap.put(AddressFamilyNumberEnum.LCAF, LispLCAFAddressSerializer.getInstance());
		afiToSearializerMap.put(AddressFamilyNumberEnum.MAC, LispMACAddressSerializer.getInstance());
		afiToSearializerMap.put(AddressFamilyNumberEnum.RESERVED, LispNoAddressSerializer.getInstance());
		
		lcafToSearializerMap = new HashMap<LispCanonicalAddressFormatEnum, LispLCAFAddressSerializer>();
		lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.LIST, LispListLCAFAddressSerializer.getInstance());
		lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.SEGMENT, LispSegmentLCAFAddressSerializer.getInstance());
		lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.APPLICATION_DATA, LispApplicationDataLCAFAddressSerializer.getInstance());
		lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.SOURCE_DEST, LispSourceDestLCAFAddressSerializer.getInstance());
	}
	
	public static LispAddressSerializer getSerializer(AddressFamilyNumberEnum afi) {
		if (afiToSearializerMap == null) {
			initializeMap();
		}
		return afiToSearializerMap.get(afi);
	}
	
	public static LispLCAFAddressSerializer getLCAFSerializer(LispCanonicalAddressFormatEnum lcaf) {
		if (lcafToSearializerMap == null) {
			initializeMap();
		}
		return lcafToSearializerMap.get(lcaf);
	}
	

}
