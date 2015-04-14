/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

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
 * @author Lorand Jakab
 *
 */
public class InstanceIdentifierUtil {
    public static InstanceIdentifier<AuthenticationKey> createKey(LispAddressContainer eid) {
        Preconditions.checkNotNull(eid, "Key needs and EID entry!");

        InstanceIdKey iidKey = new InstanceIdKey(new IidUri(Long.toString(getLispInstanceId(eid))));
        AuthenticationKeyKey authKeyKey = new AuthenticationKeyKey(new EidUri(getAddressString(eid)));
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(InstanceId.class, iidKey).child(AuthenticationKey.class, authKeyKey);
    }

    public static InstanceIdentifier<Mapping> createMapping(LispAddressContainer eid) {
        Preconditions.checkNotNull(eid, "Mapping needs an EID entry!");

        InstanceIdKey iidKey = new InstanceIdKey(new IidUri(Long.toString(getLispInstanceId(eid))));
        MappingKey eidKey = new MappingKey(new EidUri(getAddressString(eid)),MappingOrigin.Northbound);
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

    private static String getAddressString(LispAddressContainer container) {
        return LispAFIConvertor.toString(container);
    }
}
