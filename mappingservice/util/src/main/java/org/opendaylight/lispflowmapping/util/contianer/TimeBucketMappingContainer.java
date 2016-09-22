/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.util.contianer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 9/21/16.
 */
public class TimeBucketMappingContainer {
    private static final Logger LOG = LoggerFactory.getLogger(TimeBucketMappingContainer.class);

    private int current;
    private int numberOfBucket;
    private int mappingRecordTimeout;
    private long lastCleanTimestamp;

    private List<TimeBucket> bucketList;

    private long timeFrame;

    public TimeBucketMappingContainer(int numberOfBucket, int mappingRecordTimeout) {

        if (numberOfBucket <= 1) {
            throw new IllegalArgumentException("Expected number of bucket "
                    + "in TimeBucketMappingContainer to be more 1");
        }

        this.numberOfBucket = numberOfBucket;
        long mappingRecordTimeoutInMillis = TimeUnit.MINUTES.toMillis(mappingRecordTimeout);

        initializeBucketList(numberOfBucket);
        timeFrame = (long) Math.ceil(1.0 * mappingRecordTimeoutInMillis / (numberOfBucket - 1));
        lastCleanTimestamp = System.currentTimeMillis();
        current = 0;
    }

    private void initializeBucketList(int numberOfBucket) {
        bucketList = new ArrayList<>();
        for (int i = 0; i < numberOfBucket; i++) {
            bucketList.add(new TimeBucket());
        }
    }

    public void clearTimeoutMappingAndRotate() {
        clearTimeoutMappingAndRotate(System.currentTimeMillis());
    }

    public void clearTimeoutMappingAndRotate(long currentStamp) {
        long durationInMili = (currentStamp - lastCleanTimestamp);

        int rotationToPerform = (int) (1.0 * durationInMili / timeFrame);

        rotationToPerform = Math.min(rotationToPerform, numberOfBucket);

        long timeForwaded = 0;

        while (rotationToPerform > 0) {
            clearTimeoutBucket();
            rotate();
            rotationToPerform--;
            timeForwaded += timeFrame;
        }

        lastCleanTimestamp = currentStamp + timeForwaded;
    }

    private void clearTimeoutBucket() {
        int indexOfTimeoutBucket = (current - 1 + numberOfBucket) % numberOfBucket;
        clearSpecificBucket(indexOfTimeoutBucket);
    }

    private void clearSpecificBucket(int bucketIndex) {
        TimeBucket bucket = bucketList.get(bucketIndex);
        bucket.clearBucket();
    }

    private void rotate() {
        current = (current + 1 + numberOfBucket) % numberOfBucket;
    }

    public int add(Object key, Runnable removalMethod) {
        bucketList.get(current).add(key, removalMethod);
        return current;
    }

    public int refreshMappping(Object key, Runnable removalMethod, int containerId) {
        TimeBucket timeBucket = bucketList.get(containerId);
        timeBucket.remove(key);

        return add(key, removalMethod);
    }

//    //anything after this is garbage
//
//    public static void main(String[] args){
//        TimeBucketMappingContainer timeBucketMappingContainer = new TimeBucketMappingContainer(4, 3);
//        int indx1 = timeBucketMappingContainer.add(new Integer(1), new String("1"));
//        timeBucketMappingContainer.clearTimeoutMappingAndRotate(System.currentTimeMillis() + 120000 + 1000);
//        int indx2 = timeBucketMappingContainer.add(new Integer(2), new String("2"));
//        timeBucketMappingContainer.clearTimeoutMappingAndRotate(System.currentTimeMillis() + 240000 + 1000);
//        int indx3 = timeBucketMappingContainer.add(new Integer(3), new String("3"));
//        timeBucketMappingContainer.clearTimeoutMappingAndRotate(System.currentTimeMillis() + 360000 + 1000);
//        int indx4 = timeBucketMappingContainer.add(new Integer(4), new String("4"));
//        timeBucketMappingContainer.clearTimeoutMappingAndRotate(System.currentTimeMillis() + 480000 + 1000);
//        int indx5 = timeBucketMappingContainer.add(new Integer(5), new String("5"));
//    }
}