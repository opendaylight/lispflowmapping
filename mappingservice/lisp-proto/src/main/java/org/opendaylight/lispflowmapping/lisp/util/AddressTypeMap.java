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

import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializerContext;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;

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
        afiToAddressTypeMap.put((short)1, Ipv4Afi.class);
        // TODO
    }

    private static void initializeLcafMap() {
        lcafToAddressTypeMap = new HashMap<Short, Class<? extends LispAddressFamily>>();
        lcafToAddressTypeMap.put((short)1, Ipv4Afi.class);
        // TODO
    }

    public static Class<? extends LispAddressFamily> getAddressType(short afi,
            LispAddressSerializerContext.AddressContext ctx) {
        if (afiToAddressTypeMap == null) {
            initializeAfiMap();
        }
        // We denote prefix types in our private Map with their negative AFI value
        if (ctx == LispAddressSerializerContext.AddressContext.EID) {
            afi *= -1;
        }
        return afiToAddressTypeMap.get(afi);
    }

    public static Class<? extends LispAddressFamily> getLcafType(short lcafType) {
        if (lcafToAddressTypeMap == null) {
            initializeLcafMap();
        }
        return afiToAddressTypeMap.get(lcafType);
    }
}
