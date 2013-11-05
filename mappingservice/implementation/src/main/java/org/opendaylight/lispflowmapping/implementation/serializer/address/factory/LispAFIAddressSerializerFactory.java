package org.opendaylight.lispflowmapping.implementation.serializer.address.factory;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.implementation.serializer.address.LispASAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispDistinguishedNameAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispIpv4AddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispIpv6AddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispMACAddressSerializer;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

public class LispAFIAddressSerializerFactory {

    private static Map<AddressFamilyNumberEnum, LispAddressSerializer> afiToSearializerMap;

    private static void initializeMap() {
        afiToSearializerMap = new HashMap<AddressFamilyNumberEnum, LispAddressSerializer>();
        afiToSearializerMap.put(AddressFamilyNumberEnum.AS, LispASAddressSerializer.getInstance());
        afiToSearializerMap.put(AddressFamilyNumberEnum.IP, LispIpv4AddressSerializer.getInstance());
        afiToSearializerMap.put(AddressFamilyNumberEnum.IP6, LispIpv6AddressSerializer.getInstance());
        afiToSearializerMap.put(AddressFamilyNumberEnum.DISTINGUISHED_NAME, LispDistinguishedNameAddressSerializer.getInstance());
        afiToSearializerMap.put(AddressFamilyNumberEnum.LCAF, LispLCAFAddressSerializer.getInstance());
        afiToSearializerMap.put(AddressFamilyNumberEnum.MAC, LispMACAddressSerializer.getInstance());

    }

    public static LispAddressSerializer getSerializer(AddressFamilyNumberEnum afi) {
        if (afiToSearializerMap == null) {
            initializeMap();
        }
        return afiToSearializerMap.get(afi);
    }

}
