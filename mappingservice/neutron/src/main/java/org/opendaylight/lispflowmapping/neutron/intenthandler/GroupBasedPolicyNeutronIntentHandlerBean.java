/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.lispflowmapping.neutron.intenthandler.listener.service.VbridgeTopologyListenerService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 1/19/17.
 */

/*
 * This is the bean instantiated when lisp neutron service is installed. This bean
 * registers and maintain listeners for getting updates of Neutron Topology
 * maintained by groupbasedpolicy and honeycomb vbd.
 */
public class GroupBasedPolicyNeutronIntentHandlerBean implements AutoCloseable, ClusterSingletonService {
    private static final Logger LOG = LoggerFactory.getLogger(GroupBasedPolicyNeutronIntentHandlerBean.class);

    private static final ServiceGroupIdentifier IDENTIFIER =
            ServiceGroupIdentifier.create("lfm-neutron-service-group-identifier");

    private final DataBroker dataBroker;
    private final ClusterSingletonServiceProvider clusterProvider;
    private final MountPointService mountService;
    private ClusterSingletonServiceRegistration singletonServiceRegistration;

    private VbridgeTopologyListenerService vbridgeTopologyListenerService;

    private IntentHandlerAsyncExecutorProvider intentHandlerAsyncExecutorProvider =
            IntentHandlerAsyncExecutorProvider.getInstace();

    public GroupBasedPolicyNeutronIntentHandlerBean(final DataBroker dataBroker,
                                                    final BindingAwareBroker bindingAwareBroker,
                                                    final ClusterSingletonServiceProvider
                                                            clusterSingletonServiceProvider) {
        final BindingAwareBroker.ProviderContext session = Preconditions.checkNotNull(bindingAwareBroker)
                .registerProvider(new BindingAwareProvider() {
                    @Override
                    public void onSessionInitiated(BindingAwareBroker.ProviderContext providerContext) {
                        //NOP
                    }
                });
        this.dataBroker = dataBroker;
        this.mountService = session.getSALService(MountPointService.class);
        this.clusterProvider = clusterSingletonServiceProvider;
    }

    public void initialize() {
        LOG.info("Clustering session initiated for {}", this.getClass().getSimpleName());
        singletonServiceRegistration = clusterProvider.registerClusterSingletonService(this);
    }


    @Override
    public void close() throws Exception {
        LOG.info("Clustering provider closed for {}", this.getClass().getSimpleName());
        if (singletonServiceRegistration != null) {
            singletonServiceRegistration.close();
            singletonServiceRegistration = null;
        }
        intentHandlerAsyncExecutorProvider.close();
    }

    @Override
    public void instantiateServiceInstance() {
        vbridgeTopologyListenerService = VbridgeTopologyListenerService.initialize(dataBroker, mountService);
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        vbridgeTopologyListenerService.close();
        return Futures.immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return IDENTIFIER;
    }
}
