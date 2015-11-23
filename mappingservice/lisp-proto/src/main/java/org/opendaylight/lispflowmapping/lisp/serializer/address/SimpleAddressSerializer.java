/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.address.factory.LispAddressSerializerFactory;
import org.opendaylight.lispflowmapping.lisp.util.AddressTypeMap;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;

/**
 * @author Lorand Jakab
 *
 */
public class SimpleAddressSerializer {

    private static final SimpleAddressSerializer INSTANCE = new SimpleAddressSerializer();

    // Private constructor prevents instantiation from other classes
    protected SimpleAddressSerializer() {
    }

    public static SimpleAddressSerializer getInstance() {
        return INSTANCE;
    }

    public int getAddressSize(SimpleAddress address) {
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(
                AddressTypeMap.getSimpleAddressInnerType(address));
        return Length.AFI + serializer.getSimpleAddressSize(address);
    }

    public void serialize(ByteBuffer buffer, SimpleAddress address) {
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(
                AddressTypeMap.getSimpleAddressInnerType(address));
        serializer.serializeData(buffer, address);
    }

    public SimpleAddress deserialize(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        // TODO
        return null;
    }

    private interface Length {
        int AFI = 2;
    }
}
