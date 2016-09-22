/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util;


import org.opendaylight.lispflowmapping.implementation.util.container.TimeBucketMappingContainer;
import org.opendaylight.lispflowmapping.implementation.util.interfaces.ISouthboundMappingTimeoutManager;

/**
 * Created by sheikahm on 9/21/16.
 */
public class SountBoundMappingTimeoutManager implements ISouthboundMappingTimeoutManager {

    private TimeBucketMappingContainer timeBucketMappingContainer;
    public static final int NUMBER_OF_BUCKET = 4;
    public static final int SOUTHBOUND_MAPPING_GLOBAL_TIMEOUT = 10;

    public SountBoundMappingTimeoutManager() {
        timeBucketMappingContainer = new TimeBucketMappingContainer(NUMBER_OF_BUCKET,
                SOUTHBOUND_MAPPING_GLOBAL_TIMEOUT);
    }

    @Override
    public int addMapping(Object mappingRecord) {
        //TODO

        return -1;
    }

    @Override
    public int refreshMapping(Object mappingRecrdReference) {
        //TODO

        return -1;

    }

    @Override
    public void cleanTimeoutMapping() {
        timeBucketMappingContainer.clearTimeoutMappingAndRotate(System.currentTimeMillis());
    }
}