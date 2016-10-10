/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.mapcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.lisp.type.ExtendedMappingRecord;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class MappingMergeUtilTest {

    private static final String IPV4_STRING_1 =         "1.2.3.0";
    private static final String IPV4_STRING_2 =         "1.2.4.0";
    private static final String IPV4_RLOC_STRING_1 =    "101.101.101.101";
    private static final String IPV4_RLOC_STRING_2 =    "102.102.102.102";
    private static final String IPV4_RLOC_STRING_3 =    "103.103.103.103";
    private static final String IPV4_RLOC_STRING_4 =    "104.104.104.104";
    private static final String IPV4_STRING_PREFIX_24 = "/24";
    private static final String IPV4_STRING_PREFIX_16 = "/16";
    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_STRING_PREFIX_24);
    private static final Eid IPV4_PREFIX_EID_2 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_2 + IPV4_STRING_PREFIX_16);
    private static final Rloc IPV4_RLOC_1 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1);
    private static final Rloc IPV4_RLOC_2 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_2);
    private static final Rloc IPV4_RLOC_3 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_3);
    private static final Rloc IPV4_RLOC_4 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_4);
    private static final IpAddressBinary IPV4_SOURCE_RLOC_1 = new IpAddressBinary(
            new Ipv4AddressBinary(new byte[] {1, 1, 1, 1}));
    private static final IpAddressBinary IPV4_SOURCE_RLOC_2 = new IpAddressBinary(
            new Ipv4AddressBinary(new byte[] {2, 2, 2, 2}));
    private static final IpAddressBinary IPV4_SOURCE_RLOC_3 = new IpAddressBinary(
            new Ipv4AddressBinary(new byte[] {3, 3, 3, 3}));

    private static final String LOCATOR_ID_STRING = "locator-id";

    private static final XtrId XTR_ID_1 = new XtrId(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    private static final XtrId XTR_ID_2 = new XtrId(new byte[] {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
    private static final XtrId XTR_ID_3 = new XtrId(new byte[] {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3});

    private static final SiteId SITE_ID_1 = new SiteId(new byte[]{1, 1, 1, 1, 1, 1, 1, 1});
    private static final SiteId SITE_ID_2 = new SiteId(new byte[]{2, 2, 2, 2, 2, 2, 2, 2});

    // TODO Change when configuration infra is ready
    private static final long REGISTRATION_VALIDITY = 200000L;

    /**
     * Tests {@link MappingMergeUtil#mergeLocatorRecords} method with a new LocatorRecord equal to current
     * LocatorRecord and null XtrId.
     */
    @Test
    public void mergeLocatorRecordsTest_withSingleEqualLocator() {
        // current locator record
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder() // locator equals to newLocatorRecord_1
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator record
        final LocatorRecord newLocatorRecord_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // expected result
        MappingRecordBuilder mrb = new MappingRecordBuilder(currentMergedMappingBuilder.build())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);

        MappingRecord result = MappingMergeUtil.mergeMappings(
                currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeLocatorRecords} method with a new LocatorRecord not equal to current
     * LocatorRecord but same Rloc, where current locator is local. XtrId is null.
     */
    @Test
    public void mergeLocatorRecordsTest_withSingleSameRloc_currentIsLocal() {
        // current locator record
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder() // Rloc equals to Rloc in
                .setRloc(IPV4_RLOC_1)                                           // newLocatorRecord_1
                .setLocatorId(LOCATOR_ID_STRING)
                .setLocalLocator(true).build();                                 // local locator set to true

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator record
        final LocatorRecord newLocatorRecord_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setLocatorId("locator-id-1").build();                          // different locator id

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // expected result
        MappingRecordBuilder mrb = new MappingRecordBuilder(getDefaultMappingRecordBuilder()
                    .setTimestamp(currentMergedMappingBuilder.getTimestamp()).build())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        mrb.getLocatorRecord().add(currentLocatorRecord_1);

        MappingRecord result = MappingMergeUtil.mergeMappings(
                currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeLocatorRecords} method with a new LocatorRecord not equal to current
     * LocatorRecord but same Rloc, where current locator is not local. XtrId is null.
     */
    @Test
    public void mergeLocatorRecordsTest_withSingleSameRloc_currentIsNotLocal() {
        // current locator record
        final long timestamp = 1L;
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder() // Rloc equals to Rloc in
                .setRloc(IPV4_RLOC_1)                                           // newLocatorRecord_1
                .setLocatorId(LOCATOR_ID_STRING)
                .setLocalLocator(false).build();                                // local locator set to false

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder()
                .setTimestamp(timestamp);
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator record
        final LocatorRecord newLocatorRecord_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setLocatorId("locator-id-1").build();                          // different locator id

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // expected result
        MappingRecordBuilder mrb = new MappingRecordBuilder(getDefaultMappingRecordBuilder().build())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2).setTimestamp(timestamp);
        mrb.getLocatorRecord().add(newLocatorRecord_1);

        MappingRecord result = MappingMergeUtil.mergeMappings(
                currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeLocatorRecords} method with different Rlocs. XtrId is null.
     */
    @Test
    public void mergeLocatorRecordsTest_withSingleDifferentRloc() {
        // current locator record
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder() // different locator record
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator record
        final LocatorRecord newLocatorRecord_1 = new LocatorRecordBuilder() // different locator record
                .setRloc(IPV4_RLOC_2)
                .setLocatorId("locator-id-1").build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // expected result
        MappingRecordBuilder mrb = new MappingRecordBuilder(getDefaultMappingRecordBuilder().build())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2)
                .setTimestamp(currentMergedMappingBuilder.getTimestamp());
        mrb.getLocatorRecord().add(currentLocatorRecord_1);
        mrb.getLocatorRecord().add(newLocatorRecord_1);

        MappingRecord result = MappingMergeUtil.mergeMappings(
                currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with multiple locator records. We assume that
     * provided lists of locator records are pre-ordered.
     */
    @Test
    public void mergeMappingsTest_withMultipleSortedLocatorRecords() {
        // current locator records
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final LocatorRecord currentLocatorRecord_3 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_3)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_3);

        // new locator records
        final LocatorRecord newLocatorRecord_2 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_2)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final LocatorRecord newLocatorRecord_4 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_4)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_4);
        final Date timestamp = new Date();

        // expected result
        MappingRecordBuilder mrb = new MappingRecordBuilder(getDefaultMappingRecordBuilder().build())
                .setXtrId(XTR_ID_3)
                .setTimestamp(timestamp.getTime())
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        mrb.getLocatorRecord().add(currentLocatorRecord_1);
        mrb.getLocatorRecord().add(newLocatorRecord_2);
        mrb.getLocatorRecord().add(currentLocatorRecord_3);
        mrb.getLocatorRecord().add(newLocatorRecord_4);

        MappingRecord result = MappingMergeUtil.mergeMappings(
                currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), XTR_ID_3, timestamp);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with multiple locator records. We assume that
     * provided lists of locator records are not sorted. Test returns inconsistent results based on the order of
     * the items.
     */
    @Test
    public void mergeLocatorRecordsTest_withMultipleUnsortedLocatorRecords() {
        // current locator records
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder() // locator equals to newLocatorRecord_1
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final LocatorRecord currentLocatorRecord_2 = new LocatorRecordBuilder() // Rloc equals to Rloc in
                .setRloc(IPV4_RLOC_2)                                           // newLocatorRecord_2
                .setLocatorId(LOCATOR_ID_STRING)
                .setLocalLocator(false).build();                                // local locator set to false

        final LocatorRecord currentLocatorRecord_3 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_4)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_3);
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_2);
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator records
        final LocatorRecord newLocatorRecord_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final LocatorRecord newLocatorRecord_2 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_2)
                .setLocatorId(LOCATOR_ID_STRING + "-1").build();

        final LocatorRecord newLocatorRecord_3 = new LocatorRecordBuilder() // new Rloc
                .setRloc(IPV4_RLOC_3)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_3);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);
        final Date timestamp = new Date();

        // expected result
        MappingRecordBuilder mrb = new MappingRecordBuilder(getDefaultMappingRecordBuilder().build())
                .setXtrId(XTR_ID_3)
                .setTimestamp(timestamp.getTime())
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        mrb.getLocatorRecord().add(currentLocatorRecord_1);
        mrb.getLocatorRecord().add(newLocatorRecord_2);
        mrb.getLocatorRecord().add(newLocatorRecord_3);
        mrb.getLocatorRecord().add(currentLocatorRecord_3);

        MappingRecord result = MappingMergeUtil.mergeMappings(
                currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), XTR_ID_3, timestamp);
        assertNotEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method for setting the correct XtrId.
     */
    @Test
    public void mergeMappingsTest_withSingleEqualLocator_XtrIdNotNull() {
        // current locator record
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder() // locator equals to newLocatorRecord_1
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator record
        final LocatorRecord newLocatorRecord_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // result
        MappingRecord result = MappingMergeUtil.mergeMappings(
                currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), XTR_ID_3, new Date(100L));
        assertEquals(XTR_ID_3, result.getXtrId());
        assertEquals((Long) 100L, result.getTimestamp());
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with newMapping == null. Should throw an NPE.
     */
    @Test(expected = NullPointerException.class)
    public void mergeMappingsTest_withNewMappingNull() {
        // current locator record
        final LocatorRecord currentLocatorRecord_1 = new LocatorRecordBuilder()             // same locator
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        MappingMergeUtil.mergeMappings(currentMergedMappingBuilder.build(), null, null, null);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with currentMergedMapping == null.
     */
    @Test
    public void mergeMappingsTest_withNullCurrentMergedMapping() {
        MappingRecord newMapping = Mockito.mock(MappingRecord.class);

        assertEquals(newMapping, MappingMergeUtil.mergeMappings(null, newMapping, null, null));
    }

    /**
     * Tests {@link MappingMergeUtil#mergeXtrIdMappings} method with two expired mappings.
     */
    @Test
    public void mergeXtrIdMappingsTest_verifyExpiredMappings() {
        ExtendedMappingRecord expiredRecord1 = new ExtendedMappingRecord(getDefaultMappingRecordBuilder().build());
        expiredRecord1.setTimestamp(new Date(1L));
        ExtendedMappingRecord expiredRecord2 = new ExtendedMappingRecord(getDefaultMappingRecordBuilder()
                .setXtrId(XTR_ID_2).build());
        expiredRecord2.setTimestamp(new Date(1L));
        List<XtrId> expiredMappings = Lists.newArrayList();
        List<Object> records =
                Lists.newArrayList(expiredRecord1, expiredRecord2);

        assertNull(MappingMergeUtil.mergeXtrIdMappings(records, expiredMappings, null));
        assertEquals(2, expiredMappings.size());
        assertTrue(expiredMappings.contains(XTR_ID_1));
        assertTrue(expiredMappings.contains(XTR_ID_2));
    }

    /**
     * Tests {@link MappingMergeUtil#mergeXtrIdMappings} method, verifies the oldest timestamp.
     */
    @Test
    public void mergeXtrIdMappingsTest_verifyOldestTimestamp() {
        final long timestamp = new Date().getTime();
        final long timestamp_1 = timestamp - 200L;
        final long timestamp_2 = timestamp - 300L; // oldest mapping
        final long timestamp_3 = timestamp - 100L;

        ExtendedMappingRecord expiredRecord1 = new ExtendedMappingRecord(getDefaultMappingRecordBuilder().build());
        expiredRecord1.setTimestamp(new Date(timestamp_1));
        ExtendedMappingRecord expiredRecord2 = new ExtendedMappingRecord(getDefaultMappingRecordBuilder()
                .setSourceRloc(IPV4_SOURCE_RLOC_2)
                .setXtrId(XTR_ID_2).build());
        expiredRecord2.setTimestamp(new Date(timestamp_2));
        ExtendedMappingRecord expiredRecord3 = new ExtendedMappingRecord(getDefaultMappingRecordBuilder()
                .setSourceRloc(IPV4_SOURCE_RLOC_3)
                .setXtrId(XTR_ID_3).build());
        expiredRecord3.setTimestamp(new Date(timestamp_3));

        Set<IpAddressBinary> sourceRlocs = Sets.newHashSet();
        final List<XtrId> expiredMappings = Lists.newArrayList();
        final List<Object> mappingRecords =
                Lists.newArrayList(expiredRecord1, expiredRecord2, expiredRecord3);

        // result
        ExtendedMappingRecord result = MappingMergeUtil.mergeXtrIdMappings(
                mappingRecords, expiredMappings, sourceRlocs);
        assertEquals(timestamp_2, (long) result.getTimestamp().getTime());
        assertEquals(XTR_ID_2, result.getRecord().getXtrId());

        assertTrue(sourceRlocs.size() == 3);
        assertTrue(sourceRlocs.contains(IPV4_SOURCE_RLOC_1));
        assertTrue(sourceRlocs.contains(IPV4_SOURCE_RLOC_2));
        assertTrue(sourceRlocs.contains(IPV4_SOURCE_RLOC_3));

        assertTrue(expiredMappings.isEmpty());
    }

    /**
     * Tests {@link MappingMergeUtil#mappingIsExpired} method.
     */
    @Test
    public void mappingIsExpiredTest() {
        long timestamp = new Date().getTime();
        ExtendedMappingRecord emr = new ExtendedMappingRecord(getDefaultMappingRecordBuilder().build());
        emr.setTimestamp(new Date(timestamp - (REGISTRATION_VALIDITY + 1L)));
        assertTrue(MappingMergeUtil.mappingIsExpired(emr));
        emr.setTimestamp(new Date(timestamp));
        assertFalse(MappingMergeUtil.mappingIsExpired(emr));
        emr.setTimestamp(null);
        assertFalse(MappingMergeUtil.mappingIsExpired(emr));
    }

    /**
     * Tests {@link MappingMergeUtil#mappingIsExpired} method, throws NPE.
     */
    @Test(expected = NullPointerException.class)
    public void mappingIsExpiredTest_throwsNPE() {
        MappingMergeUtil.mappingIsExpired(null);
    }

    /**
     * Tests {@link MappingMergeUtil#timestampIsExpired} method, throws NPE.
     */
    @Test(expected = NullPointerException.class)
    public void timestampIsExpiredTest_withDate_throwsNPE() {
        Date date = null;
        MappingMergeUtil.timestampIsExpired(date);
    }

    /**
     * Tests {@link MappingMergeUtil#timestampIsExpired} method, throws NPE.
     */
    @Test
    public void timestampIsExpiredTest_withDate() {
        long timestamp = new Date().getTime();
        assertTrue(MappingMergeUtil.timestampIsExpired(new Date(timestamp - (REGISTRATION_VALIDITY + 1L))));
        assertFalse(MappingMergeUtil.timestampIsExpired(new Date(timestamp)));
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setEid(IPV4_PREFIX_EID_1)
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
