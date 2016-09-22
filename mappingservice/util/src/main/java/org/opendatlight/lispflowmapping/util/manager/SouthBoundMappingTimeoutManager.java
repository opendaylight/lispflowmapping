/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendatlight.lispflowmapping.util.manager;


import org.opendatlight.lispflowmapping.util.contianer.MappingAllInfo;
import org.opendatlight.lispflowmapping.util.contianer.TimeBucketMappingContainer;
import org.opendatlight.lispflowmapping.util.interfaces.ISouthboundMappingTimeoutManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Created by sheikahm on 9/21/16.
 */
public class SouthBoundMappingTimeoutManager implements ISouthboundMappingTimeoutManager {

    private TimeBucketMappingContainer timeBucketMappingContainer;
    public static final int NUMBER_OF_BUCKET = 4;
    public static final int SOUTHBOUND_MAPPING_GLOBAL_TIMEOUT = 10;

    private static SouthBoundMappingTimeoutManager instance = null;

    private SouthBoundMappingTimeoutManager() {
        timeBucketMappingContainer = new TimeBucketMappingContainer(NUMBER_OF_BUCKET,
                SOUTHBOUND_MAPPING_GLOBAL_TIMEOUT);
    }

    public static SouthBoundMappingTimeoutManager getInstance() {
        if (instance == null) {
            synchronized (SouthBoundMappingTimeoutManager.class) {
                if (instance == null) {
                    instance = new SouthBoundMappingTimeoutManager();
                }
            }
        }
        return instance;
    }

    @Override
    public int addMapping(Eid key, String subKey, MappingAllInfo mappingAllInfo) {
        return timeBucketMappingContainer.add(key, subKey, mappingAllInfo);
    }

    @Override
    public int refreshMapping(Eid key, String subKey, MappingAllInfo mappingAllInfo, int containerId) {
        return timeBucketMappingContainer.refreshMappping(key, subKey, mappingAllInfo, containerId);

    }

    @Override
    public void cleanTimeoutMapping() {
        timeBucketMappingContainer.clearTimeoutMappingAndRotate(System.currentTimeMillis());
    }
}