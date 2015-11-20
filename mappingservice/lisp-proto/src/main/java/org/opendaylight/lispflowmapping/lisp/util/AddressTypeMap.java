/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AfiListLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ApplicationDataLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AsNumberAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.KeyValueAddressLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Lcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.NoAddressAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;

/**
 * This class contains static HashMaps of AFIs and LCAF types to LispAddressFamily identities
 *
 * @author Lorand Jakab
 *
 */
public final class AddressTypeMap {
    private static Map<Short, Class<? extends LispAddressFamily>> afiToAddressTypeMap;
    private static Map<Short, Class<? extends LispAddressFamily>> lcafToAddressTypeMap;

    private static void initializeAfiMap() {
        afiToAddressTypeMap = new HashMap<Short, Class<? extends LispAddressFamily>>();
        afiToAddressTypeMap.put((short) 0, NoAddressAfi.class);
        afiToAddressTypeMap.put((short) AddressFamily.IpV4.getIntValue(), Ipv4Afi.class);
        afiToAddressTypeMap.put((short) (AddressFamily.IpV4.getIntValue() * -1), Ipv4PrefixAfi.class);
        afiToAddressTypeMap.put((short) AddressFamily.IpV6.getIntValue(), Ipv6Afi.class);
        afiToAddressTypeMap.put((short) (AddressFamily.IpV6.getIntValue() * -1), Ipv6PrefixAfi.class);
        afiToAddressTypeMap.put((short) AddressFamily.DistinguishedName.getIntValue(), DistinguishedNameAfi.class);
        afiToAddressTypeMap.put((short) AddressFamily.AsNumber.getIntValue(), AsNumberAfi.class);
        afiToAddressTypeMap.put((short) AddressFamily.LispCanonicalAddressFormat.getIntValue(), Lcaf.class);
        afiToAddressTypeMap.put((short) AddressFamily._48BitMac.getIntValue(), MacAfi.class);
    }

    private static void initializeLcafMap() {
        lcafToAddressTypeMap = new HashMap<Short, Class<? extends LispAddressFamily>>();
        lcafToAddressTypeMap.put((short) LispCanonicalAddressFormatEnum.LIST.getLispCode(), AfiListLcaf.class);
        lcafToAddressTypeMap.put((short) LispCanonicalAddressFormatEnum.SEGMENT.getLispCode(), InstanceIdLcaf.class);
        lcafToAddressTypeMap.put((short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode(),
                ApplicationDataLcaf.class);
        lcafToAddressTypeMap.put((short) LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode(),
                ExplicitLocatorPathLcaf.class);
        lcafToAddressTypeMap.put((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode(),
                SourceDestKeyLcaf.class);
        lcafToAddressTypeMap.put((short) LispCanonicalAddressFormatEnum.KEY_VALUE.getLispCode(),
                KeyValueAddressLcaf.class);
        // TODO
    }

    public static Class<? extends LispAddressFamily> getAddressType(short afi) {
        if (afiToAddressTypeMap == null) {
            initializeAfiMap();
        }
        return afiToAddressTypeMap.get(afi);
    }

    public static Class<? extends LispAddressFamily> getLcafType(short lcafType) {
        if (lcafToAddressTypeMap == null) {
            initializeLcafMap();
        }
        return lcafToAddressTypeMap.get(lcafType);
    }
}
