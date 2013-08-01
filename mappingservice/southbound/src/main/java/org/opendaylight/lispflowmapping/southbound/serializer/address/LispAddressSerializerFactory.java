package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.type.lisp.address.LispASAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispMACAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;

public class LispAddressSerializerFactory {
	
	private static Map<Class<? extends LispAddress>, LispAddressSerializer> addressToSearializerMap;
	
	private static void initializeMap() {
		addressToSearializerMap = new HashMap<Class<? extends LispAddress>, LispAddressSerializer>();
		addressToSearializerMap.put(LispASAddress.class, LispASAddressSerializer.getInstance());
		addressToSearializerMap.put(LispIpv4Address.class, LispIpv4AddressSerializer.getInstance());
		addressToSearializerMap.put(LispIpv6Address.class, LispIpv6AddressSerializer.getInstance());
		addressToSearializerMap.put(LispListLCAFAddress.class, LispListLCAFAddressSerializer.getInstance());
		addressToSearializerMap.put(LispMACAddress.class, LispMACAddressSerializer.getInstance());
		addressToSearializerMap.put(LispNoAddress.class, LispNoAddressSerializer.getInstance());
		addressToSearializerMap.put(LispSegmentLCAFAddress.class, LispSegmentLCAFAddressSerializer.getInstance());
	}
	
	public static LispAddressSerializer getSerializer(LispAddress lispAddress) {
		if (addressToSearializerMap == null) {
			initializeMap();
		}
		return addressToSearializerMap.get(lispAddress.getClass());
	}
	
	

}
