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
public class GroupBasedPolicyNeutronIntentHandlerBean implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(GroupBasedPolicyNeutronIntentHandlerBean.class);

    private final DataBroker dataBroker;
    private final MountPointService mountService;

    private VbridgeTopologyListenerService vbridgeTopologyListenerService;

    private final IntentHandlerAsyncExecutorProvider intentHandlerAsyncExecutorProvider =
            IntentHandlerAsyncExecutorProvider.getInstace();

    public GroupBasedPolicyNeutronIntentHandlerBean(DataBroker dataBroker, MountPointService mountService) {
        this.dataBroker = dataBroker;
        this.mountService = mountService;
    }


    @Override
    public void close() throws Exception {
        LOG.info("Clustering provider closed for {}", this.getClass().getSimpleName());
        intentHandlerAsyncExecutorProvider.close();
    }

    public void init() {
        LOG.info("LFM neutron handler service registered", this.getClass().getSimpleName());
        vbridgeTopologyListenerService = VbridgeTopologyListenerService.initialize(dataBroker, mountService);
    }
}
