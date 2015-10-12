/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.LispAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecord.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;

/**
 * Converts RPC *Input object to other object types
 *
 * @author Lorand Jakab
 *
 */
public class RPCInputConvertorUtil {
    public static AuthenticationKey toAuthenticationKey(AddKeyInput input) {
        return toAuthenticationKey(input.getEid(), input.getKey());
    }

    public static AuthenticationKey toAuthenticationKey(UpdateKeyInput input) {
        return toAuthenticationKey(input.getEid(), input.getKey());
    }

    public static AuthenticationKey toAuthenticationKey(RemoveKeyInput input) {
        return toAuthenticationKey(input.getEid(), null);
    }

    public static Mapping toMapping(AddMappingInput input) {
        return toMapping(input.getMappingEntry());
    }

    public static Mapping toMapping(UpdateMappingInput input) {
        return toMapping(input.getMappingEntry());
    }

    public static Mapping toMapping(RemoveMappingInput input) {
        return toMapping(input.getEid());
    }

    private static AuthenticationKey toAuthenticationKey(LispAddress address, MappingAuthkey key) {
        AuthenticationKeyBuilder akb = new AuthenticationKeyBuilder(address);
        akb.setEidUri(new EidUri(LispAddressStringifier.getURIString(address)));
        if (key != null) {
            akb.setKeyType(key.getKeyType());
            akb.setAuthkey(key.getAuthkey());
        }
        return akb.build();
    }

    private static Mapping toMapping(EidToLocatorRecord mapping) {
        MappingBuilder mb = new MappingBuilder(mapping);
        mb.setEidUri(new EidUri(LispAddressStringifier.getURIString(mapping.getEid())));
        mb.setOrigin(MappingOrigin.Northbound);
        return mb.build();
    }

    private static Mapping toMapping(LispAddress address) {
        MappingBuilder mb = new MappingBuilder();
        mb.setEidUri(new EidUri(LispAddressStringifier.getURIString(address)));
        mb.setOrigin(MappingOrigin.Northbound);
        mb.setEid((Eid)address);
        return mb.build();
    }
}
