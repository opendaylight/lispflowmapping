/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address.factory;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.lisp.serializer.address.AfiListSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.ApplicationDataSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.DistinguishedNameSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.ExplicitLocatorPathSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.InstanceIdSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4PrefixSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv6PrefixSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv6Serializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.KeyValueAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LcafSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4Serializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.MacSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.NoAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.ServicePathSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.SourceDestKeySerializer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AfiListLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ApplicationDataLcaf;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;

/**
 * Factory for LispAddress (de)serializers
 *
 * @author Lorand Jakab
 *
 */
public final class LispAddressSerializerFactory {
    private static Map<Class<? extends LispAddressFamily>, LispAddressSerializer> addressTypeToSerializerMap;

    // Class should not be instantiated
    private LispAddressSerializerFactory() {
    }

    private static void initializeMap() {
        addressTypeToSerializerMap = new HashMap<Class<? extends LispAddressFamily>, LispAddressSerializer>();
        addressTypeToSerializerMap.put(NoAddressAfi.class, NoAddressSerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv4Afi.class, Ipv4Serializer.getInstance());
        addressTypeToSerializerMap.put(Ipv4PrefixAfi.class, Ipv4PrefixSerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv6Afi.class, Ipv6Serializer.getInstance());
        addressTypeToSerializerMap.put(Ipv6PrefixAfi.class, Ipv6PrefixSerializer.getInstance());
        addressTypeToSerializerMap.put(MacAfi.class, MacSerializer.getInstance());
        addressTypeToSerializerMap.put(DistinguishedNameAfi.class, DistinguishedNameSerializer.getInstance());
        addressTypeToSerializerMap.put(Lcaf.class, LcafSerializer.getInstance());
        addressTypeToSerializerMap.put(AfiListLcaf.class, AfiListSerializer.getInstance());
        addressTypeToSerializerMap.put(InstanceIdLcaf.class, InstanceIdSerializer.getInstance());
        addressTypeToSerializerMap.put(ApplicationDataLcaf.class, ApplicationDataSerializer.getInstance());
        addressTypeToSerializerMap.put(ExplicitLocatorPathLcaf.class, ExplicitLocatorPathSerializer.getInstance());
        addressTypeToSerializerMap.put(SourceDestKeyLcaf.class, SourceDestKeySerializer.getInstance());
        addressTypeToSerializerMap.put(KeyValueAddressLcaf.class, KeyValueAddressSerializer.getInstance());
        addressTypeToSerializerMap.put(ServicePathLcaf.class, ServicePathSerializer.getInstance());
    }

    public static LispAddressSerializer getSerializer(Class<? extends LispAddressFamily> clazz) {
        if (addressTypeToSerializerMap == null) {
            initializeMap();
        }
        return addressTypeToSerializerMap.get(clazz);
    }
}
