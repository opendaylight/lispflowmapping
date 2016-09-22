/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.util.manager;


import org.opendaylight.lispflowmapping.util.contianer.MappingAllInfo;
import org.opendaylight.lispflowmapping.util.contianer.TimeBucketMappingContainer;
import org.opendaylight.lispflowmapping.util.interfaces.ISouthboundMappingTimeoutManager;

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
    public int addOrRefreshMapping(Object key, String subKey, MappingAllInfo mappingAllInfo,
                                    Integer previousContainerId) {
        if (previousContainerId != null) {
            return timeBucketMappingContainer.refreshMappping(key, subKey, mappingAllInfo,
                    previousContainerId.intValue());
        } else {
            return timeBucketMappingContainer.add(key, subKey, mappingAllInfo);
        }
    }

    @Override
    public void cleanTimeoutMapping() {
        timeBucketMappingContainer.clearTimeoutMappingAndRotate(System.currentTimeMillis());
    }
}