/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;

/**
 * @author Lorand Jakab
 *
 */
public class IpPrefixSerializer {

    private static final IpPrefixSerializer INSTANCE = new IpPrefixSerializer();

    // Private constructor prevents instantiation from other classes
    protected IpPrefixSerializer() {
    }

    public static IpPrefixSerializer getInstance() {
        return INSTANCE;
    }

    public int getAddressSize(IpPrefix prefix) {
        // TODO
        return 0;
    }

    public void serialize(ByteBuffer buffer, IpPrefix prefix) {
        // TODO
    }
    public SimpleAddress deserialize(ByteBuffer buffer, LispAddressSerializerContext ctx) {
        // TODO
        return null;
    }
}
