/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util;

import java.util.Arrays;
import java.util.List;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;

/**
 * @author Florin Coras
 *
 */
public class DSBEInputUtil {
    public static Mapping toMapping(MappingOrigin origin, Eid key, SiteId siteId,
            EidToLocatorRecord record) {
        List<SiteId> siteIds = (siteId != null) ? Arrays.asList(siteId) : null;
        return new MappingBuilder(record).setEidUri(new EidUri(LispAddressStringifier.getURIString(key)))
                .setOrigin(origin).setSiteId(siteIds).build();
    }

    public static Mapping toMapping(MappingOrigin origin, Eid key) {
        MappingBuilder mb = new MappingBuilder();
        mb.setEidUri(new EidUri(LispAddressStringifier.getURIString(key)));
        mb.setOrigin(origin);
        return mb.build();
    }

    public static AuthenticationKey toAuthenticationKey(Eid key, String authKey) {
        AuthenticationKeyBuilder akb = new AuthenticationKeyBuilder();
        akb.setEidUri(new EidUri(LispAddressStringifier.getURIString(key)));
        akb.setEid(key);
        akb.setMappingAuthkey(new MappingAuthkeyBuilder().setKeyString(authKey).build());
        return akb.build();
    }
}
