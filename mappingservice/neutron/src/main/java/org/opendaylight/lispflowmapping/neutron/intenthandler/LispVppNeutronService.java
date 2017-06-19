/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.lispflowmapping.neutron.intenthandler.listener.VppNodeListener;
import org.opendaylight.lispflowmapping.neutron.intenthandler.manager.VppNodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 6/19/17.
 */
public class LispVppNeutronService implements AutoCloseable, BindingAwareProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LispVppNeutronService.class);
    private DataBroker dataBroker;
    private MountPointService mountService;

    private VppNodeManager vppNodeManager;
    private VppNodeListener vppNodeListener;

    public LispVppNeutronService(final DataBroker dataBroker, final BindingAwareBroker bindingAwareBroker) {
        this.dataBroker = dataBroker;
        bindingAwareBroker.registerProvider(this);
    }

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext session) {
        LOG.info("LFM neutron handler service registered", this.getClass().getSimpleName());
        this.mountService = session.getSALService(MountPointService.class);
        this.vppNodeManager = new VppNodeManager(dataBroker, mountService);
        this.vppNodeListener = new VppNodeListener(dataBroker, vppNodeManager);
    }

    @Override
    public void close() throws Exception {
        vppNodeListener.close();
    }
}
