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
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.AddressTypeMap;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;

/**
 * Class to (de)serialize addresses that can be used in an LCAF.
 *
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
        return Length.AFI + serializer.getAddressSize(address);
    }

    public void serialize(ByteBuffer buffer, SimpleAddress address) {
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(
                AddressTypeMap.getSimpleAddressInnerType(address));
        buffer.putShort(serializer.getAfi());
        serializer.serializeData(buffer, address);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public SimpleAddress deserialize(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        short afi = buffer.getShort();
        // AddressTypeMap indexes IPv4 and IPv6 prefixes (vs simple addresses) with the negative AFI values -1 and -2
        if ((afi == 1 || afi == 2) && ctx != null
                && ctx.getMaskLen() != LispAddressSerializerContext.MASK_LEN_MISSING) {
            afi *= -1;
        }
        Class<? extends LispAddressFamily> addressType = AddressTypeMap.getAddressType(afi);
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(addressType);
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI: " + afi);
        }
        try {
            return serializer.deserializeSimpleAddressData(buffer, ctx);
        } catch (RuntimeException e) {
            throw new LispSerializationException("Problem deserializing AFI " + afi + " in SimpleAddress context", e);
        }
    }

    private interface Length {
        int AFI = 2;
    }
}
