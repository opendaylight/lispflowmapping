/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingServiceShell;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement Karaf commands to interact with the Mapping Service.
 *
 * @author Lorand Jakab
 *
 */
public class MappingServiceShell implements IMappingServiceShell {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingServiceShell.class);

    private final IMappingService mappingService;

    public MappingServiceShell(final IMappingService mappingService) {
        this.mappingService = mappingService;
    }

    @Override
    public String printMappings() {
        return mappingService.printMappings();
    }

    @Override
    public String prettyPrintMappings() {
        return mappingService.prettyPrintMappings();
    }

    @Override
    public String printKeys() {
        return mappingService.printKeys();
    }

    @Override
    public void addDefaultKeyIPv4() {
        Eid eid = LispAddressUtil.toEid(new Ipv4Prefix("0.0.0.0/0"), null);
        MappingAuthkey key = new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build();
        mappingService.addAuthenticationKey(eid, key);
    }
}
