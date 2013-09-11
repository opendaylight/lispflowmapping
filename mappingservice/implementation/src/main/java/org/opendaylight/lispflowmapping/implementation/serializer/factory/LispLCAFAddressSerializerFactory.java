package org.opendaylight.lispflowmapping.implementation.serializer.factory;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.implementation.serializer.address.LispASAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispApplicationDataLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispIpv4AddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispIpv6AddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispListLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispMACAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispNoAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispSegmentLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispSourceDestLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispTrafficEngineeringLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispLCAFAddressSerializerFactory {
	
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
		lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING, LispTrafficEngineeringLCAFAddressSerializer.getInstance());
		lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.SOURCE_DEST, LispSourceDestLCAFAddressSerializer.getInstance());
	}
	
	protected static LispAddressSerializer getSerializer(AddressFamilyNumberEnum afi) {
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
