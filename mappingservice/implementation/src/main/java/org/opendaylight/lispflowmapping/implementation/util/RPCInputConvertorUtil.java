/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;

/**
 * Converts RPC *Input object to other object types
 *
 * @author Lorand Jakab
 *
 */
public final class RPCInputConvertorUtil {
    // Utility class, should not be instantiated
    private RPCInputConvertorUtil() {
    }

    public static AuthenticationKey toAuthenticationKey(AddKeyInput input) {
        return toAuthenticationKey(input.getEid(), input.getMappingAuthkey());
    }

    public static AuthenticationKey toAuthenticationKey(UpdateKeyInput input) {
        return toAuthenticationKey(input.getEid(), input.getMappingAuthkey());
    }

    public static AuthenticationKey toAuthenticationKey(RemoveKeyInput input) {
        return toAuthenticationKey(input.getEid(), null);
    }

    public static Mapping toMapping(AddMappingInput input) {
        return toMapping(input.getMappingRecord());
    }

    public static Mapping toMapping(UpdateMappingInput input) {
        return toMapping(input.getMappingRecord());
    }

    public static Mapping toMapping(RemoveMappingInput input) {
        return toMapping(input.getEid());
    }

    private static AuthenticationKey toAuthenticationKey(Eid address, MappingAuthkey key) {
        AuthenticationKeyBuilder akb = new AuthenticationKeyBuilder();
        akb.setEidUri(new EidUri(LispAddressStringifier.getURIString(address)));
        akb.setEid(address);
        if (key != null) {
            akb.setMappingAuthkey(key);
        }
        return akb.build();
    }

    private static Mapping toMapping(MappingRecord mapping) {
        MappingBuilder mb = new MappingBuilder();
        mb.setEidUri(new EidUri(LispAddressStringifier.getURIString(mapping.getEid())));
        mb.setOrigin(MappingOrigin.Northbound);
        mb.setMappingRecord(mapping);
        return mb.build();
    }

    private static Mapping toMapping(Eid eid) {
        MappingBuilder mb = new MappingBuilder();
        mb.setEidUri(new EidUri(LispAddressStringifier.getURIString(eid)));
        mb.setOrigin(MappingOrigin.Northbound);
        mb.setMappingRecord(new MappingRecordBuilder().setEid(eid).build());
        return mb.build();
    }
}
