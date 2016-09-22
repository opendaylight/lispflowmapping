/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendatlight.lispflowmapping.util.contianer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Created by sheikahm on 9/21/16.
 */
public class TimeBucketMappingContainer {

    private int current;
    private int numberOfBucket;
    private int mappingRecordTimeout;
    private long lastCleanTimestamp;

    private List<TimeBucket> bucketList;

    private double timeFrame;

    //EPS is needed to ensure floating point precision never affects timeout
    //Generally it's better to timeout later but not before.
    private static final double EPS = 1e-8;

    public TimeBucketMappingContainer(int numberOfBucket, int mappingRecordTimeout) {

        if (numberOfBucket <= 1) {
            throw new IllegalArgumentException("Expected number of bucket "
                    + "in TimeBucketMappingContainer to be more 1");
        }

        this.numberOfBucket = numberOfBucket;
        this.mappingRecordTimeout = mappingRecordTimeout;

        initializeBucketList(numberOfBucket);
        timeFrame = (1.0 * mappingRecordTimeout / (numberOfBucket - 1)) + EPS;
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

        long durationInMin = TimeUnit.MILLISECONDS.toMinutes(durationInMili);

        int rotationToPerform = (int) (1.0 * durationInMin / timeFrame);

        rotationToPerform = Math.min(rotationToPerform, numberOfBucket);

        while (rotationToPerform > 0) {
            clearTimeoutBucket();
            rotate();
            rotationToPerform--;
        }

        lastCleanTimestamp = currentStamp;
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

    public int add(Eid key, String subKey, MappingAllInfo mappingAllInfo) {
        bucketList.get(current).add(key, subKey, mappingAllInfo);
        return current;
    }

    public int refreshMappping(Eid key, String subKey, MappingAllInfo mappingAllInfo, int containerId) {
        bucketList.get(containerId).remove(key, subKey);
        return add(key, subKey, mappingAllInfo);
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