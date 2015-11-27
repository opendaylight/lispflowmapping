/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingServiceShell;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lorand Jakab
 *
 */
public class MappingServiceShell implements IMappingServiceShell, BindingAwareProvider, AutoCloseable {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingServiceShell.class);

    private BindingAwareBroker bindingAwareBroker;
    private IMappingService mappingService;

    public void setBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {
        this.bindingAwareBroker = bindingAwareBroker;
    }

    public void setMappingService(IMappingService mappingService) {
        this.mappingService = mappingService;
    }

    public void initialize() {
        bindingAwareBroker.registerProvider(this);
    }

    @Override
    public String printMappings() {
        return mappingService.printMappings();
    }

    @Override
    public void addDefaultKeyIPv4() {
        Eid eid = LispAddressUtil.toEid(new Ipv4Prefix("0.0.0.0/0"), null);
        MappingAuthkey key = new MappingAuthkeyBuilder().setKeyType(1).setKeyString("password").build();
        mappingService.addAuthenticationKey(eid, key);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void onSessionInitiated(ProviderContext arg0) {
        // TODO Auto-generated method stub

    }
}
