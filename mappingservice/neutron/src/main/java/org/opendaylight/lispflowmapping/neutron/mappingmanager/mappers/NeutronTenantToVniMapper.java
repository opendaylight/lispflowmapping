/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers;

import java.util.HashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Created by Shakib Ahmed on 2/17/17.
 */
public class NeutronTenantToVniMapper {
    private final HashMap<String, Uint32> neutronTenantToVniMapper = new HashMap<>();
    private long id = 1;

    public synchronized @NonNull Uint32 getVni(String tenantUuid) {
        final Uint32 existing = neutronTenantToVniMapper.get(tenantUuid);
        if (existing != null) {
            return existing;
        }
        final Uint32 allocated = Uint32.valueOf(id++);
        neutronTenantToVniMapper.put(tenantUuid, allocated);
        return allocated;
    }
}
