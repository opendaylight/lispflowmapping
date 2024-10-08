/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The superclass for the different MD-SAL data change event listeners.
 *
 */
public abstract class AbstractDataListener<T extends DataObject> implements DataTreeChangeListener<T> {
    private DataBroker broker;
    private InstanceIdentifier<T> path;
    private Registration configRegistration;
    private Registration operRegistration;

    void registerDataChangeListener() {
        configRegistration = broker.registerTreeChangeListener(
            DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, path), this);
        operRegistration = broker.registerTreeChangeListener(
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, path), this);
    }

    public void closeDataChangeListener() {
        configRegistration.close();
        operRegistration.close();
    }

    void setBroker(DataBroker broker) {
        this.broker = broker;
    }

    void setPath(InstanceIdentifier<T> path) {
        this.path = path;
    }
}
