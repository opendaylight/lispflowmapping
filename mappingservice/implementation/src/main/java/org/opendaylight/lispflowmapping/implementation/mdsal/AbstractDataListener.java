/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The superclass for the different MD-SAL data change event listeners.
 *
 */
public abstract class AbstractDataListener<T extends DataObject> implements ClusteredDataTreeChangeListener<T> {
    private DataBroker broker;
    private InstanceIdentifier<T> path;
    private ListenerRegistration<ClusteredDataTreeChangeListener<T>> registration;

    public void registerDataChangeListener() {
        final DataTreeIdentifier<T> dataTreeIdentifier = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
                path);

        registration = broker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    public void closeDataChangeListener() {
        registration.close();
    }

    public void setBroker(DataBroker broker) {
        this.broker = broker;
    }

    void setPath(InstanceIdentifier<T> path) {
        this.path = path;
    }

}
