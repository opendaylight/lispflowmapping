/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.VniUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.XtrIdUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifierKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Preconditions;

/**
 * Utility class to create InstanceIdentifier path objects based on EID.
 * Used for storing RPC data to the config datastore.
 *
 * @author Lorand Jakab
 *
 */
public final class InstanceIdentifierUtil {
    // Utility class, should not be instantiated
    private InstanceIdentifierUtil() {
    }

    public static InstanceIdentifier<AuthenticationKey> createAuthenticationKeyIid(Eid eid) {
        Preconditions.checkNotNull(eid, "Key needs and EID entry!");

        VirtualNetworkIdentifierKey vniKey = new VirtualNetworkIdentifierKey(new VniUri(
                Long.toString(getLispInstanceId(eid))));
        AuthenticationKeyKey authKeyKey = new AuthenticationKeyKey(new EidUri(
                LispAddressStringifier.getURIString(eid)));
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(VirtualNetworkIdentifier.class, vniKey).child(AuthenticationKey.class, authKeyKey);
    }

    public static InstanceIdentifier<Mapping> createMappingIid(Eid eid, MappingOrigin orig) {
        Preconditions.checkNotNull(eid, "Mapping needs an EID entry!");

        VirtualNetworkIdentifierKey vniKey = new VirtualNetworkIdentifierKey(new VniUri(
                Long.toString(getLispInstanceId(eid))));
        MappingKey eidKey = new MappingKey(new EidUri(LispAddressStringifier.getURIString(eid)), orig);
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(VirtualNetworkIdentifier.class, vniKey).child(Mapping.class, eidKey);
    }

    public static InstanceIdentifier<XtrIdMapping> createXtrIdMappingIid(Eid eid, MappingOrigin orig, XtrId xtrId) {
        Preconditions.checkNotNull(eid, "Mapping needs an EID entry!");
        Preconditions.checkNotNull(xtrId, "Mapping needs an xTR-ID entry!");

        VirtualNetworkIdentifierKey vniKey = new VirtualNetworkIdentifierKey(new VniUri(
                Long.toString(getLispInstanceId(eid))));
        MappingKey eidKey = new MappingKey(new EidUri(LispAddressStringifier.getURIString(eid)), orig);
        XtrIdMappingKey xtrIdKey = new XtrIdMappingKey(new XtrIdUri(LispAddressStringifier.getURIString(xtrId)));
        return InstanceIdentifier.create(MappingDatabase.class)
                .child(VirtualNetworkIdentifier.class, vniKey).child(Mapping.class, eidKey)
                .child(XtrIdMapping.class, xtrIdKey);
    }

    private static long getLispInstanceId(Eid eid) {
        Address address = eid.getAddress();
        if (address instanceof InstanceId) {
            return ((InstanceId) address).getInstanceId().getIid().getValue();
        } else if (eid.getVirtualNetworkId() != null) {
            return eid.getVirtualNetworkId().getValue();
        }
        return 0L;
    }
}
