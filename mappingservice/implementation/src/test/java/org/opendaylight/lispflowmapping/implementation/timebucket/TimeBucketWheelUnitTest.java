/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.timebucket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.implementation.timebucket.containers.TimeBucket;
import org.opendaylight.lispflowmapping.implementation.timebucket.containers.TimeBucketWheel;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

/**
 * Created by Shakib Ahmed on 12/13/16.
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(TimeBucketWheel.class)
public class TimeBucketWheelUnitTest {

    private static final String IPV4_STRING_1 =         "1.2.3.0";
    private static final String IPV4_STRING_2 =         "1.2.4.0";
    private static final String IPV4_STRING_3 =         "1.2.5.0";
    private static final String IPV4_STRING_4 =         "1.2.6.0";
    private static final String IPV4_STRING_5 =         "1.2.7.0";
    private static final Eid IPV4_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid IPV4_EID_3 = LispAddressUtil.asIpv4Eid(IPV4_STRING_3);
    private static final Eid IPV4_EID_4 = LispAddressUtil.asIpv4Eid(IPV4_STRING_4);
    private static final Eid IPV4_EID_5 = LispAddressUtil.asIpv4Eid(IPV4_STRING_5);
    private static final IpAddressBinary IPV4_SOURCE_RLOC_1 = new IpAddressBinary(
            new Ipv4AddressBinary(new byte[] {1, 1, 1, 1}));

    private static final int NUMBER_OF_BUCKETS = 4;

    private static final XtrId XTR_ID_1 = new XtrId(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

    private static final SiteId SITE_ID_1 = new SiteId(new byte[]{1, 1, 1, 1, 1, 1, 1, 1});

    private static MappingSystem mappingSystem = Mockito.mock(MappingSystem.class);

    /**
     * Tests {@link TimeBucketWheel#add(Eid, MappingData, long)} method for general case.
     */
    @Test
    public void mappingAddedInTheProperBucketGeneralTest() {
        PowerMockito.mockStatic(System.class);

        long frozenTimeStamp = System.currentTimeMillis();
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        TimeBucketWheel timeBucketWheel = getDefaultTimeBucketWheel();

        final int bucketId1 = timeBucketWheel.add(IPV4_EID_1, getDefaultMappingData(IPV4_EID_1),
                System.currentTimeMillis());

        frozenTimeStamp += 1000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        final int bucketId2 = timeBucketWheel.add(IPV4_EID_2, getDefaultMappingData(IPV4_EID_2),
                System.currentTimeMillis());

        frozenTimeStamp += 1000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        final int bucketId3 = timeBucketWheel.add(IPV4_EID_3, getDefaultMappingData(IPV4_EID_3),
                System.currentTimeMillis());

        frozenTimeStamp += 1000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        final int bucketId4 = timeBucketWheel.add(IPV4_EID_4, getDefaultMappingData(IPV4_EID_4),
                System.currentTimeMillis());

        frozenTimeStamp += 1000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        final int bucketId5 = timeBucketWheel.add(IPV4_EID_5, getDefaultMappingData(IPV4_EID_5),
                System.currentTimeMillis());

        Assert.assertEquals((bucketId1 - 1 + NUMBER_OF_BUCKETS) % NUMBER_OF_BUCKETS, bucketId2);
        Assert.assertEquals((bucketId2 - 1 + NUMBER_OF_BUCKETS) % NUMBER_OF_BUCKETS, bucketId3);
        Assert.assertEquals((bucketId3 - 1 + NUMBER_OF_BUCKETS) % NUMBER_OF_BUCKETS, bucketId4);
        Assert.assertEquals((bucketId4 - 1 + NUMBER_OF_BUCKETS) % NUMBER_OF_BUCKETS, bucketId5);
    }

    /**
     * Tests {@link TimeBucketWheel#add(Eid, MappingData, long)} method for add in some bucket in the middle.
     */
    @Test
    public void mappingAddedInTheProperBucketAddInMiddleTest() throws Exception {
        PowerMockito.mockStatic(System.class);

        long frozenTimeStamp = System.currentTimeMillis();
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        TimeBucketWheel timeBucketWheel = getDefaultTimeBucketWheel();

        checkTooOldMappingCase(timeBucketWheel);

        checkOlderThanCurrentCase(timeBucketWheel);

    }

    private void checkTooOldMappingCase(TimeBucketWheel timeBucketWheel) throws Exception {
        long frozenTimeStamp = System.currentTimeMillis() - 10000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        int bucketId = timeBucketWheel.add(IPV4_EID_1, getDefaultMappingData(IPV4_EID_1),
                System.currentTimeMillis());

        int idOfLastBucketInBucketWheel = Whitebox.invokeMethod(timeBucketWheel,
                "getLastBucketId");

        Assert.assertEquals(idOfLastBucketInBucketWheel, bucketId);

        //Time resetting to old frozen time
        frozenTimeStamp = System.currentTimeMillis() + 10000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);
    }

    private void checkOlderThanCurrentCase(TimeBucketWheel timeBucketWheel) {
        long frozenTimeStamp = System.currentTimeMillis();

        int bucketId1 = timeBucketWheel.add(IPV4_EID_1, getDefaultMappingData(IPV4_EID_1),
                frozenTimeStamp);

        MappingData toBeExpiredMappingData = getDefaultMappingData(IPV4_EID_2);

        int bucketId2 = timeBucketWheel.add(IPV4_EID_2, toBeExpiredMappingData,
                frozenTimeStamp - 1000);

        Assert.assertEquals((bucketId1 + 1) % NUMBER_OF_BUCKETS, bucketId2);

        //expired at proper time
        frozenTimeStamp = frozenTimeStamp + 3000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        timeBucketWheel.clearExpiredMappingAndRotate();

        Mockito.verify(mappingSystem).handleSbExpiredMapping(IPV4_EID_2, null, toBeExpiredMappingData);
    }


    /**
     * Tests {@link TimeBucketWheel#refreshMappping(Eid, MappingData, long, int)} method.
     * {@link ClassCastException} can be thrown.
     */
    @Test
    public void mappingRefreshedProperlyTest() {
        PowerMockito.mockStatic(System.class);

        long frozenTimeStamp = System.currentTimeMillis();
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        TimeBucketWheel timeBucketWheel = getDefaultTimeBucketWheel();

        final int bucketId1 = timeBucketWheel.add(IPV4_EID_1, getDefaultMappingData(IPV4_EID_1),
                System.currentTimeMillis());

        frozenTimeStamp += 2000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        MappingData newMappingData = getDefaultMappingData(IPV4_EID_1);

        int currentBucketId = timeBucketWheel.refreshMappping(IPV4_EID_1, newMappingData,
                System.currentTimeMillis(), bucketId1);

        List<TimeBucket> bucketList = extractBucketList(timeBucketWheel);

        TimeBucket pastTimeBucket = bucketList.get(bucketId1);

        MappingData oldStoredMappingData = getMappingDataFromTimeBucket(pastTimeBucket, IPV4_EID_1);

        Assert.assertNull(oldStoredMappingData);

        TimeBucket presentTimeBucket = bucketList.get(currentBucketId);

        MappingData newStoredMappingData = getMappingDataFromTimeBucket(presentTimeBucket, IPV4_EID_1);

        Assert.assertEquals(newMappingData, newStoredMappingData);
    }

    private List<TimeBucket> extractBucketList(TimeBucketWheel timeBucketWheel) {
        List<TimeBucket> bucketList;
        try {
            bucketList = (List<TimeBucket>) Whitebox.getInternalState(timeBucketWheel, "bucketList");
        } catch (ClassCastException e) {
            throw e;
        }
        return bucketList;
    }

    private MappingData getMappingDataFromTimeBucket(TimeBucket timeBucket, Eid eid) {
        ConcurrentHashMap<Eid, MappingData> bucketElements;

        try {
            bucketElements = (ConcurrentHashMap<Eid, MappingData>) Whitebox.getInternalState(timeBucket,
                    "bucketElements");
        } catch (ClassCastException e) {
            throw e;
        }

        return bucketElements.get(eid);
    }

    /**
     * Tests {@link TimeBucketWheel#clearExpiredMappingAndRotate(long)} method.
     * {@link ClassCastException} can be thrown.
     */
    @Test
    public void expiredMappingClearedProperlyTest() {
        PowerMockito.mockStatic(System.class);

        long frozenTimeStamp = System.currentTimeMillis();
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        TimeBucketWheel timeBucketWheel = getDefaultTimeBucketWheel();

        MappingData mappingData = getDefaultMappingData(IPV4_EID_1);
        timeBucketWheel.add(IPV4_EID_1, mappingData,
                System.currentTimeMillis());

        frozenTimeStamp = System.currentTimeMillis() + 4000;
        PowerMockito.when(System.currentTimeMillis()).thenReturn(frozenTimeStamp);

        timeBucketWheel.clearExpiredMappingAndRotate(frozenTimeStamp);

        Mockito.verify(mappingSystem).handleSbExpiredMapping(IPV4_EID_1, null, mappingData);
    }

    private static TimeBucketWheel getDefaultTimeBucketWheel() {
        return new TimeBucketWheel(4, 3000, mappingSystem);
    }

    private static MappingData getDefaultMappingData(Eid eid) {
        return getDefaultMappingData(eid,null);
    }

    private static MappingData getDefaultMappingData(Eid eid, MappingRecord mappingRecord) {
        if (mappingRecord == null) {
            mappingRecord = getDefaultMappingRecordBuilder(eid).build();
        }
        return new MappingData(MappingOrigin.Southbound, mappingRecord, System.currentTimeMillis());
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder(Eid eid) {
        return new MappingRecordBuilder()
                .setEid(eid)
                .setLocatorRecord(new ArrayList<>())
                .setRecordTtl(2)
                .setAction(MappingRecord.Action.NativelyForward)
                .setAuthoritative(true)
                .setMapVersion((short) 1)
                .setSiteId(SITE_ID_1)
                .setSourceRloc(IPV4_SOURCE_RLOC_1)
                .setTimestamp(new Date().getTime())
                .setXtrId(XTR_ID_1);
    }
}