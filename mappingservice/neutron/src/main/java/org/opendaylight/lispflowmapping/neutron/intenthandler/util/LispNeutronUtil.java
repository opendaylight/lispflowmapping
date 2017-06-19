/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.lispflowmapping.neutron.intenthandler.LispNeutronAsyncExecutorProvider;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 1/12/17.
 */
public class LispNeutronUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(LispNeutronUtil.class);

    private static final short DURATION = 3000;

    private static LispNeutronAsyncExecutorProvider asyncExecutorProvider = LispNeutronAsyncExecutorProvider
            .getInstace();

    private LispNeutronUtil(){}

    public static ListenableFuture<DataBroker> resolveDataBrokerForMountPointAsync(
                                                    InstanceIdentifier<Node> iiToMountPoint,
                                                    MountPointService mountService, int retryCount) {
        return asyncExecutorProvider.getExecutor().submit(()
            -> resolveDataBrokerForMountPoint(iiToMountPoint, mountService, retryCount));
    }

    public static DataBroker resolveDataBrokerForMountPoint(InstanceIdentifier<Node> iiToMountPoint,
                                                            MountPointService mountService, int retryCount) {

        if (retryCount == 0) {
            return null;
        }

        final Optional<MountPoint> vppMountPointOpt = mountService.getMountPoint(iiToMountPoint);

        if (vppMountPointOpt.isPresent()) {
            final MountPoint vppMountPoint = vppMountPointOpt.get();
            final Optional<DataBroker> dataBrokerOpt = vppMountPoint.getService(DataBroker.class);
            if (dataBrokerOpt.isPresent()) {
                return dataBrokerOpt.get();
            }
        }
        try {
            Thread.sleep(DURATION);
        } catch (InterruptedException e) {
            LOG.warn("Thread interrupted to ", e);
        }
        return resolveDataBrokerForMountPoint(iiToMountPoint, mountService, --retryCount);
    }
}
