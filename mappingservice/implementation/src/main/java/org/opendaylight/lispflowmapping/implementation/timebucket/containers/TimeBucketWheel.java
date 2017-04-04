/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.timebucket.containers;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 12/1/16.
 */
public class TimeBucketWheel {
    private static final Logger LOG = LoggerFactory.getLogger(TimeBucketWheel.class);

    private int currentBucketId;
    private int numberOfBuckets;
    private long lastRotationTimestamp;

    private List<TimeBucket> bucketList;

    private long timeFrame;

    public TimeBucketWheel(int numberOfBuckets, long mappingRecordValidityInMilis, MappingSystem mappingSystem) {

        if (numberOfBuckets <= 1) {
            throw new IllegalArgumentException("Expected number of buckets "
                    + "in TimeBucketMappingContainer to be more 1");
        }

        this.numberOfBuckets = numberOfBuckets;

        initializeBucketList(mappingSystem);
        timeFrame = (long) Math.ceil(1.0 * mappingRecordValidityInMilis / (numberOfBuckets - 1));
        lastRotationTimestamp = System.currentTimeMillis();
        currentBucketId = 0;
    }

    private void initializeBucketList(MappingSystem mappingSystem) {
        bucketList = new ArrayList<>();
        for (int i = 0; i < numberOfBuckets; i++) {
            bucketList.add(new TimeBucket(mappingSystem));
        }
    }

    public int add(Eid key, MappingData mappingData, long timestamp) {
        clearExpiredMappingAndRotate(timestamp);
        int timeBucketId = getProperBucketId(timestamp);

        TimeBucket properTimeBucket = getBucket(timeBucketId);
        properTimeBucket.add(key, mappingData);
        return timeBucketId;
    }

    public int refreshMappping(Eid key, MappingData newMappingData, long timestamp, int bucketId) {
        TimeBucket timeBucket = getBucket(bucketId);
        timeBucket.removeFromBucketOnly(key);
        return add(key, newMappingData, timestamp);
    }

    public void removeMapping(Eid key, int bucketId) {
        TimeBucket timeBucket = getBucket(bucketId);
        timeBucket.removeFromBucketOnly(key);
    }

    private int getLastBucketId() {
        return (currentBucketId - 1 + numberOfBuckets) % numberOfBuckets;
    }

    private int getProperBucketId(long timestamp) {
        if (timestamp > lastRotationTimestamp) {
            //after rotation we are at current
            return currentBucketId;
        }

        int relativeBucketId = (int) ((lastRotationTimestamp - timestamp) / timeFrame);
        if (relativeBucketId >= numberOfBuckets) {
            //too old scenario
            LOG.error("The mapping that is being added is too old! This should not happen.");
            return getLastBucketId();
        }

        return (this.currentBucketId + relativeBucketId) % numberOfBuckets;
    }

    private TimeBucket getBucket(int bucketId) {
        return bucketList.get(bucketId);
    }

    public void clearExpiredMappingAndRotate() {
        clearExpiredMappingAndRotate(System.currentTimeMillis());
    }

    public void clearExpiredMappingAndRotate(long currentStamp) {
        int numberOfRotationToPerform = getNumberOfRotationsToPerform(currentStamp);

        long timeForwarded = 0;

        while (numberOfRotationToPerform > 0) {
            clearExpiredBucket();
            rotate();
            numberOfRotationToPerform--;
            timeForwarded += timeFrame;
        }

        lastRotationTimestamp = lastRotationTimestamp + timeForwarded;
    }

    private int getNumberOfRotationsToPerform(long currentStamp) {
        if (currentStamp < lastRotationTimestamp) {
            return 0;
        }

        long durationInMillis = (currentStamp - lastRotationTimestamp);

        int numberOfRotationToPerform = (int) (1.0 * durationInMillis / timeFrame);
        numberOfRotationToPerform = Math.min(numberOfRotationToPerform, numberOfBuckets);

        return numberOfRotationToPerform;
    }

    private void clearExpiredBucket() {
        int timeoutBucketId = getLastBucketId();
        clearSpecificBucket(timeoutBucketId);
    }

    private void clearSpecificBucket(int bucketId) {
        TimeBucket bucket = getBucket(bucketId);
        bucket.clearBucket();
    }

    private void rotate() {
        currentBucketId = getLastBucketId();
    }
}
