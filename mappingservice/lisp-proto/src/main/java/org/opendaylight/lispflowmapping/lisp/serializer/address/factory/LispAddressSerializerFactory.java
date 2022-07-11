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
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4BinarySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4PrefixBinarySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4PrefixSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv4Serializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv6BinarySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv6PrefixBinarySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv6PrefixSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.Ipv6Serializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.KeyValueAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LcafSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;

/**
 * Factory for LispAddress (de)serializers.
 *
 * @author Lorand Jakab
 */
public final class LispAddressSerializerFactory {
    private static Map<LispAddressFamily, LispAddressSerializer> addressTypeToSerializerMap;

    private LispAddressSerializerFactory() {
        // Class should not be instantiated
    }

    private static void initializeMap() {
        // FIXME: use ImmutabmeMap and static initialization
        addressTypeToSerializerMap = new HashMap<>();
        addressTypeToSerializerMap.put(NoAddressAfi.VALUE, NoAddressSerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv4Afi.VALUE, Ipv4Serializer.getInstance());
        addressTypeToSerializerMap.put(Ipv4BinaryAfi.VALUE, Ipv4BinarySerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv4PrefixAfi.VALUE, Ipv4PrefixSerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv4PrefixBinaryAfi.VALUE, Ipv4PrefixBinarySerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv6Afi.VALUE, Ipv6Serializer.getInstance());
        addressTypeToSerializerMap.put(Ipv6BinaryAfi.VALUE, Ipv6BinarySerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv6PrefixAfi.VALUE, Ipv6PrefixSerializer.getInstance());
        addressTypeToSerializerMap.put(Ipv6PrefixBinaryAfi.VALUE, Ipv6PrefixBinarySerializer.getInstance());
        addressTypeToSerializerMap.put(MacAfi.VALUE, MacSerializer.getInstance());
        addressTypeToSerializerMap.put(DistinguishedNameAfi.VALUE, DistinguishedNameSerializer.getInstance());
        addressTypeToSerializerMap.put(Lcaf.VALUE, LcafSerializer.getInstance());
        addressTypeToSerializerMap.put(AfiListLcaf.VALUE, AfiListSerializer.getInstance());
        addressTypeToSerializerMap.put(InstanceIdLcaf.VALUE, InstanceIdSerializer.getInstance());
        addressTypeToSerializerMap.put(ApplicationDataLcaf.VALUE, ApplicationDataSerializer.getInstance());
        addressTypeToSerializerMap.put(ExplicitLocatorPathLcaf.VALUE, ExplicitLocatorPathSerializer.getInstance());
        addressTypeToSerializerMap.put(SourceDestKeyLcaf.VALUE, SourceDestKeySerializer.getInstance());
        addressTypeToSerializerMap.put(KeyValueAddressLcaf.VALUE, KeyValueAddressSerializer.getInstance());
        addressTypeToSerializerMap.put(ServicePathLcaf.VALUE, ServicePathSerializer.getInstance());
    }

    public static LispAddressSerializer getSerializer(LispAddressFamily clazz) {
        if (addressTypeToSerializerMap == null) {
            initializeMap();
        }
        return addressTypeToSerializerMap.get(clazz);
    }
}
