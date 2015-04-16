/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.index;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author Lorand Jakab
 *
 */
public class MdSalStorageIndex extends AbstractStorageIndex{

    private InstanceIdentifier<EidToLocatorRecord>[] mappingPointer;

    @Override
    public void storeMapping(EidToLocatorRecord mapping, MappingOrigin orig) {
        // TODO Auto-generated method stub
    }

    @Override
    public void storeKey(MappingKey key) {
        // TODO Auto-generated method stub
    }

    @Override
    public EidToLocatorRecord retrieveMapping(MappingOrigin orig) {
        // TODO Auto-generated method stub
        return null;
    }

    public InstanceIdentifier<EidToLocatorRecord> getPointer(MappingOrigin orig) {
        return mappingPointer[orig.getIntValue()];
    }
}
