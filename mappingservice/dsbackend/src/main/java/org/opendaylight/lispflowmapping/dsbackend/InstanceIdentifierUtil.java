/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.dsbackend;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.VniUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.XtrIdUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifierKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

/**
 * Utility class to create InstanceIdentifier path objects based on EID.
 * Used for storing RPC data to the config datastore.
 *
 * @author Lorand Jakab
 */
public final class InstanceIdentifierUtil {
    // Utility class, should not be instantiated
    private InstanceIdentifierUtil() {
    }

    public static @NonNull WithKey<AuthenticationKey, AuthenticationKeyKey> createAuthenticationKeyIid(Eid eid) {
        requireNonNull(eid, "Key needs and EID entry!");

        return DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, new VirtualNetworkIdentifierKey(new VniUri(
                Long.toString(getLispInstanceId(eid)))))
            .child(AuthenticationKey.class, new AuthenticationKeyKey(new EidUri(
                LispAddressStringifier.getURIString(eid))))
            .build();
    }

    public static @NonNull WithKey<Mapping, MappingKey> createMappingIid(Eid eid, MappingOrigin orig) {
        requireNonNull(eid, "Mapping needs an EID entry!");

        return DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, new VirtualNetworkIdentifierKey(new VniUri(
                Long.toString(getLispInstanceId(eid)))))
            .child(Mapping.class, new MappingKey(new EidUri(LispAddressStringifier.getURIString(eid)), orig))
            .build();
    }

    public static @NonNull WithKey<XtrIdMapping, XtrIdMappingKey> createXtrIdMappingIid(Eid eid, MappingOrigin orig,
            XtrId xtrId) {
        requireNonNull(eid, "Mapping needs an EID entry!");
        requireNonNull(xtrId, "Mapping needs an xTR-ID entry!");

        return DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, new VirtualNetworkIdentifierKey(new VniUri(
                Long.toString(getLispInstanceId(eid)))))
            .child(Mapping.class, new MappingKey(new EidUri(LispAddressStringifier.getURIString(eid)), orig))
            .child(XtrIdMapping.class, new XtrIdMappingKey(new XtrIdUri(LispAddressStringifier.getURIString(xtrId))))
            .build();
    }

    private static long getLispInstanceId(Eid eid) {
        Address address = eid.getAddress();
        if (address instanceof InstanceId ii) {
            return ii.getInstanceId().getIid().getValue().toJava();
        } else if (eid.getVirtualNetworkId() != null) {
            return eid.getVirtualNetworkId().getValue().toJava();
        }
        return 0L;
    }
}
