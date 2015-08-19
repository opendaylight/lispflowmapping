/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.IidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.AuthenticationKeyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.MappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.mapping.database.InstanceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.mapping.database.InstanceIdKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Preconditions;

/**
 * Utility class to create InstanceIdentifier path objects based on EID.
 * Used for storing RPC data to the config datastore.
 *
 * @author Lorand Jakab
 *
 */
public class InstanceIdentifierUtil {
    public static InstanceIdentifier<AuthenticationKey> createAuthenticationKeyIid(LispAddressContainer eid, int mask) {
        Preconditions.checkNotNull(eid, "Key needs and EID entry!");

        InstanceIdKey iidKey = new InstanceIdKey(new IidUri(Long.toString(getLispInstanceId(eid))));
        AuthenticationKeyKey authKeyKey = new AuthenticationKeyKey(new EidUri(getURIAddressString(eid, mask)));
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(InstanceId.class, iidKey).child(AuthenticationKey.class, authKeyKey);
    }

    public static InstanceIdentifier<Mapping> createMappingIid(LispAddressContainer eid, int mask, MappingOrigin orig) {
        Preconditions.checkNotNull(eid, "Mapping needs an EID entry!");

        InstanceIdKey iidKey = new InstanceIdKey(new IidUri(Long.toString(getLispInstanceId(eid))));
        MappingKey eidKey = new MappingKey(new EidUri(getURIAddressString(eid, mask)), orig);
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(InstanceId.class, iidKey).child(Mapping.class, eidKey);
    }

    private static long getLispInstanceId(LispAddressContainer container) {
        Address eid = container.getAddress();
        if (eid instanceof LcafSegment) {
            return ((LcafSegment) eid).getLcafSegmentAddr().getInstanceId();
        }
        return 0L;
    }

    private static String getURIAddressString(LispAddressContainer container, int mask) {
        return LispAddressStringifier.getURIString(container, mask);
    }
}
