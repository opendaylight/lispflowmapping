/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.backends.mdsal;

import org.opendaylight.lispflowmapping.implementation.backends.IStorageBackend;
import org.opendaylight.lispflowmapping.implementation.backends.StorageIndex;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingKey;

/**
 * Implements the MD-SAL storage back-end
 *
 * @author Lorand Jakab
 *
 */
public class MdSalStorageBackend implements IStorageBackend {

    @Override
    public StorageIndex storeMapping(EidToLocatorRecord mapping, MappingOrigin orig) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EidToLocatorRecord retrieveMapping(StorageIndex index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StorageIndex storeKey(MappingKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String retrieveKey(StorageIndex index) {
        // TODO Auto-generated method stub
        return null;
    }
}
