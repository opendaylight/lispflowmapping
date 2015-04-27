/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The superclass for the different MD-SAL data change event listeners.
 *
 * @author Lorand Jakab
 *
 */
public abstract class AbstractDataListener implements DataChangeListener {
    private DataBroker broker;
    private InstanceIdentifier<?> path;
    private ListenerRegistration<DataChangeListener> registration;

    public void registerDataChangeListener() {
        registration = broker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                path, this, DataBroker.DataChangeScope.SUBTREE);
    }

    public void closeDataChangeListener() {
        registration.close();
    }

    public void setBroker(DataBroker broker) {
        this.broker = broker;
    }

    void setPath(InstanceIdentifier<?> path) {
        this.path = path;
    }
}
