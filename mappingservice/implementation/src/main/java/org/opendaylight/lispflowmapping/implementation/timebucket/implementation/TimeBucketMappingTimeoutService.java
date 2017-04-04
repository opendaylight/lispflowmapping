/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.timebucket.implementation;

import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.implementation.timebucket.containers.TimeBucketWheel;
import org.opendaylight.lispflowmapping.implementation.timebucket.interfaces.ISouthBoundMappingTimeoutService;
import org.opendaylight.lispflowmapping.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 12/1/16.
 */
public class TimeBucketMappingTimeoutService implements ISouthBoundMappingTimeoutService {
    private static final Logger LOG = LoggerFactory.getLogger(TimeBucketWheel.class);

    TimeBucketWheel timeBucketWheel;

    public TimeBucketMappingTimeoutService(int numberOfBucket, long mappingRecordValidityInMillis,
                                           MappingSystem mappingSystem) {
        timeBucketWheel = new TimeBucketWheel(numberOfBucket, mappingRecordValidityInMillis, mappingSystem);
    }

    @Override
    public int addMapping(Eid key, MappingData mappingData) {
        long timestamp = System.currentTimeMillis();
        if (mappingData.getTimestamp() != null) {
            timestamp = mappingData.getTimestamp().getTime();
        }
        return timeBucketWheel.add(key, mappingData, timestamp);
    }

    @Override
    public int refreshMapping(Eid key, MappingData newMappingData, int presentBucketId) {
        long timestamp = System.currentTimeMillis();
        if (newMappingData.getTimestamp() != null) {
            timestamp = newMappingData.getTimestamp().getTime();
        }
        return timeBucketWheel.refreshMappping(key, newMappingData, timestamp, presentBucketId);
    }

    @Override
    public void removeMappingFromTimeoutService(Eid key, int presentBucketId) {
        timeBucketWheel.removeMapping(key, presentBucketId);
    }

    @Override
    public void removeExpiredMappings() {
        timeBucketWheel.clearExpiredMappingAndRotate();
    }
}
