/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.backends.mdsal;

import org.opendaylight.lispflowmapping.implementation.backends.StorageIndex;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * This class is the index object into the MD-SAL storage back-end, where the
 * pointers used are InstanceIdentifier<T> objects
 *
 * @author Lorand Jakab
 *
 */
class MdSalStorageIndex<T extends DataObject> extends StorageIndex {

    private InstanceIdentifier<T> identifier;

    /**
     * @return the identifier
     */
    InstanceIdentifier<T> getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    void setIdentifier(InstanceIdentifier<T> identifier) {
        this.identifier = identifier;
    }
}
