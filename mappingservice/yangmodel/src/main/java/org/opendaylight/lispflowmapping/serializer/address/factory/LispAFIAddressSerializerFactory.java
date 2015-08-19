/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.serializer.address.factory;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.lisp.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.serializer.address.LispDistinguishedNameAddressSerializer;
import org.opendaylight.lispflowmapping.serializer.address.LispIpv4AddressSerializer;
import org.opendaylight.lispflowmapping.serializer.address.LispIpv6AddressSerializer;
import org.opendaylight.lispflowmapping.serializer.address.LispLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.serializer.address.LispMACAddressSerializer;
import org.opendaylight.lispflowmapping.serializer.address.LispNoAddressSerializer;

public class LispAFIAddressSerializerFactory {

    private static Map<AddressFamilyNumberEnum, LispAddressSerializer> afiToSearializerMap;

    private static void initializeMap() {
        afiToSearializerMap = new HashMap<AddressFamilyNumberEnum, LispAddressSerializer>();
        afiToSearializerMap.put(AddressFamilyNumberEnum.IP, LispIpv4AddressSerializer.getInstance());
        afiToSearializerMap.put(AddressFamilyNumberEnum.NO_ADDRESS, LispNoAddressSerializer.getInstance());
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
