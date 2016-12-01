/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util.timebucket.containers;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 12/1/16.
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
            throw new IllegalArgumentException("Expected number of bucket "
                    + "in TimeBucketMappingContainer to be more 1");
        }

        this.numberOfBuckets = numberOfBuckets;

        initializeBucketList(numberOfBuckets, mappingSystem);
        timeFrame = (long) Math.ceil(1.0 * mappingRecordValidityInMilis / (numberOfBuckets - 1));
        lastRotationTimestamp = System.currentTimeMillis();
        currentBucketId = 0;
    }

    private void initializeBucketList(int numberOfBucket, MappingSystem mappingSystem) {
        bucketList = new ArrayList<>();
        for (int i = 0; i < numberOfBucket; i++) {
            bucketList.add(new TimeBucket(mappingSystem));
        }
    }

    public int add(Eid key, MappingData mappingData, long timestamp) {

        if (wheelRotationNeeded(timestamp)) {
            clearExpiredMappingAndRotate(timestamp);
        }

        int timeBucketId;
        if (lastRotationTimestamp > timestamp) {
            //already expired mapping, add in the last bucket and wait for clearance
            timeBucketId = getLastBucketId();
        } else {
            timeBucketId = getProperBucketId(timestamp);
        }

        TimeBucket properTimeBucket = getBucket(timeBucketId);
        properTimeBucket.add(key, mappingData);
        return timeBucketId;
    }

    public int refreshMappping(Eid key, MappingData newMappingData, long timestamp, int bucketId) {
        TimeBucket timeBucket = getBucket(bucketId);
        timeBucket.removeFromBucketOnly(key);
        return add(key, newMappingData, timestamp);
    }

    private int getLastBucketId() {
        return (currentBucketId - 1 + numberOfBuckets) % numberOfBuckets;
    }

    private int getProperBucketId(long timestamp) {
        int relativeBucketId = ( ((int)(timestamp - lastRotationTimestamp)) / numberOfBuckets );
        if (relativeBucketId >= numberOfBuckets) {
            LOG.warn("Bucket index limit out of bound. Miscalculation of proper bucket or wrong"
                    + " timestamp? Ignoring and placing in current bucket.");
            relativeBucketId = numberOfBuckets - 1;
        }

        int lastBucketId = getLastBucketId();

        return (lastBucketId - relativeBucketId + numberOfBuckets) % numberOfBuckets;
    }

    private TimeBucket getBucket(int bucketId) {
        return bucketList.get(bucketId);
    }

    public void clearExpiredMappingAndRotate() {
        clearExpiredMappingAndRotate(System.currentTimeMillis());
    }

    public void clearExpiredMappingAndRotate(long currentStamp) {
        int numberOfRotationToPerform = getNumberOfRotationToPerform(currentStamp);

        long timeForwaded = 0;

        while (numberOfRotationToPerform > 0) {
            clearExpiredBucket();
            rotate();
            numberOfRotationToPerform--;
            timeForwaded += timeFrame;
        }

        lastRotationTimestamp = currentStamp + timeForwaded;
    }

    private int getNumberOfRotationToPerform(long currentStamp) {
        if (currentStamp < lastRotationTimestamp) {
            return 0;
        }

        long durationInMili = (currentStamp - lastRotationTimestamp);

        int numberOfRotationToPerform = (int) (1.0 * durationInMili / timeFrame);
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

    /*
     * This function handles a potential error that's going to be introduced
     * if time bucket wheel is not rotated for a long time. We continue adding
     * to the current buckets for a long time without rotating, then we run
     * into the risk of adding mapping to the current bucket that are outside
     * the time frame. And then if we suddenly the wheel and we cast newer
     * mapping record to a narrower time frame and potentially run into the
     * issue of timing out a mapping record before they should.
     */
    private boolean wheelRotationNeeded(long timestamp) {
        return getProperBucketId(timestamp) > 0;
    }

    public long getLastRotationTimestamp() {
        return lastRotationTimestamp;
    }
}
