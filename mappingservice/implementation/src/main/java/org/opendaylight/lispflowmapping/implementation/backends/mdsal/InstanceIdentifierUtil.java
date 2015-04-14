/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.backends.mdsal;

import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.IidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.db.instance.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.db.instance.EidKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapping.database.InstanceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapping.database.InstanceIdKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Preconditions;

/**
 * @author Lorand Jakab
 *
 */
public class InstanceIdentifierUtil {
    public static InstanceIdentifier<Eid> createKey(MappingKey key) {
        LispAddressContainer eid = key.getLispAddressContainer();
        return createKey(eid);
    }

    public static InstanceIdentifier<Eid> createKey(LispAddressContainer eid) {
        Preconditions.checkNotNull(eid, "Key needs and EID entry!");

        InstanceIdKey iidKey = new InstanceIdKey(new IidUri(Long.toString(getLispInstanceId(eid))));
        EidKey eidKey = new EidKey(new EidUri(getAddressString(eid)),MappingOrigin.Northbound);
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(InstanceId.class, iidKey).child(Eid.class, eidKey);
    }

    public static InstanceIdentifier<Eid> createEid(EidToLocatorRecord mapping) {
        LispAddressContainer eid = mapping.getLispAddressContainer();
        Preconditions.checkNotNull(eid, "Mapping needs an EID entry!");

        InstanceIdKey iidKey = new InstanceIdKey(new IidUri(Long.toString(getLispInstanceId(eid))));
        EidKey eidKey = new EidKey(new EidUri(getAddressString(eid)),MappingOrigin.Northbound);
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(InstanceId.class, iidKey).child(Eid.class, eidKey);
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
