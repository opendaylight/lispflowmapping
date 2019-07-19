/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.timebucket.implementation;

import java.util.Date;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.implementation.timebucket.containers.TimeBucketWheel;
import org.opendaylight.lispflowmapping.implementation.timebucket.interfaces.ISouthBoundMappingTimeoutService;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Created by Shakib Ahmed on 12/1/16.
 */
public class TimeBucketMappingTimeoutService implements ISouthBoundMappingTimeoutService {
    private final TimeBucketWheel timeBucketWheel;

    public TimeBucketMappingTimeoutService(int numberOfBucket, long mappingRecordValidityInMillis,
                                           MappingSystem mappingSystem) {
        timeBucketWheel = new TimeBucketWheel(numberOfBucket, mappingRecordValidityInMillis, mappingSystem);
    }

    @Override
    public int addMapping(Eid key, MappingData mappingData) {
        return timeBucketWheel.add(key, mappingData, getTimestamp(mappingData));
    }

    @Override
    public int refreshMapping(Eid key, MappingData newMappingData, int presentBucketId) {
        return timeBucketWheel.refreshMappping(key, newMappingData, getTimestamp(newMappingData), presentBucketId);
    }

    @Override
    public void removeMappingFromTimeoutService(Eid key, int presentBucketId) {
        timeBucketWheel.removeMapping(key, presentBucketId);
    }

    @Override
    public void removeExpiredMappings() {
        timeBucketWheel.clearExpiredMappingAndRotate();
    }

    private static long getTimestamp(MappingData mappingData) {
        final Date stamp = mappingData.getTimestamp();
        return stamp != null ? stamp.getTime() : System.currentTimeMillis();
    }
}
