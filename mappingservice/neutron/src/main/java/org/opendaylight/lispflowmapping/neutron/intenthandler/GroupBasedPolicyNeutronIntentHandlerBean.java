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
import org.opendaylight.lispflowmapping.neutron.intenthandler.listener.service.VbridgeTopologyListenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 1/19/17.
 */

/*
 * This is the bean instantiated when lisp neutron service is installed. This bean
 * registers and maintain listeners for getting updates of Neutron Topology
 * maintained by groupbasedpolicy and honeycomb vbd.
 */
public class GroupBasedPolicyNeutronIntentHandlerBean implements AutoCloseable, BindingAwareProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GroupBasedPolicyNeutronIntentHandlerBean.class);

    private DataBroker dataBroker;
    private MountPointService mountService;

    private VbridgeTopologyListenerService vbridgeTopologyListenerService;

    private IntentHandlerAsyncExecutorProvider intentHandlerAsyncExecutorProvider =
            IntentHandlerAsyncExecutorProvider.getInstace();

    public GroupBasedPolicyNeutronIntentHandlerBean(final BindingAwareBroker bindingAwareBroker) {
        bindingAwareBroker.registerProvider(this);
    }


    @Override
    public void close() throws Exception {
        LOG.info("Clustering provider closed for {}", this.getClass().getSimpleName());
        intentHandlerAsyncExecutorProvider.close();
    }

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext session) {
        LOG.info("LFM neutron handler service registered", this.getClass().getSimpleName());
        dataBroker = session.getSALService(DataBroker.class);
        mountService = session.getSALService(MountPointService.class);
        vbridgeTopologyListenerService = VbridgeTopologyListenerService.initialize(dataBroker, mountService);
    }
}
