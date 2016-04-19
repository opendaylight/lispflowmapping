/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

public class MappingMergeUtilTest {

    private static final String IPV4_STRING_1 =         "1.2.3.0";
    private static final String IPV4_STRING_2 =         "1.2.4.0";
    private static final String IPV4_STRING_3 =         "1.2.5.0";
    private static final String IPV4_STRING_4 =         "1.2.6.0";
    private static final String IPV6_STRING =           "1111:2222:3333:4444:5555:6666:7777:8888";
    private static final String IPV4_RLOC_STRING_1 =    "101.101.101.101";
    private static final String IPV4_RLOC_STRING_2 =    "102.102.102.102";
    private static final String IPV4_RLOC_STRING_3 =    "103.103.103.103";
    private static final String IPV4_RLOC_STRING_4 =    "104.104.104.104";
    private static final String IPV4_STRING_PREFIX_24 = "/24";
    private static final String IPV4_STRING_PREFIX_16 = "/16";
    private static final String IPV6_STRING_PREFIX =    "/96";
    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_STRING_PREFIX_24);
    private static final Eid IPV4_PREFIX_EID_2 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_2 + IPV4_STRING_PREFIX_16);
    private static final Eid IPV6_PREFIX_EID = LispAddressUtil.asIpv6PrefixEid(IPV6_STRING + IPV6_STRING_PREFIX);
    private static final Eid IPV4_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid SOURCE_DEST_KEY_EID_1 = LispAddressUtil
            .asSrcDstEid(IPV4_STRING_3, IPV4_STRING_4, 24, 16, 1);
    private static final Rloc IPV4_RLOC_1 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1);
    private static final Rloc IPV4_RLOC_2 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_2);
    private static final Rloc IPV4_RLOC_3 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_3);
    private static final Rloc IPV4_RLOC_4 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_4);
    private static final IpAddress IPV4_SOURCE_RLOC_1 = new IpAddress(new Ipv4Address("192.168.0.1"));
    private static final IpAddress IPV4_SOURCE_RLOC_2 = new IpAddress(new Ipv4Address("192.168.0.2"));
    private static final IpAddress IPV4_SOURCE_RLOC_3 = new IpAddress(new Ipv4Address("192.168.0.3"));

    private static final String LOCATOR_ID_STRING = "locator-id";

    private static final XtrId XTR_ID_1 = new XtrId(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    private static final XtrId XTR_ID_2 = new XtrId(new byte[] {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
    private static final XtrId XTR_ID_3 = new XtrId(new byte[] {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3});

    private static final SiteId SITE_ID_1 = new SiteId(new byte[]{1, 1, 1, 1, 1, 1, 1, 1});
    private static final SiteId SITE_ID_2 = new SiteId(new byte[]{2, 2, 2, 2, 2, 2, 2, 2});

    private static final long REGISTRATION_VALIDITY = ConfigIni.getInstance().getRegistrationValiditySb();

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

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
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

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
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

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
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

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
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

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), XTR_ID_3, timestamp);
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

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), XTR_ID_3, timestamp);
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
        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), XTR_ID_3, new Date(100L));
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
        MappingRecord expiredMappingRecord_1 = getDefaultMappingRecordBuilder().setTimestamp(1L).build();
        MappingRecord expiredMappingRecord_2 = getDefaultMappingRecordBuilder()
                .setXtrId(XTR_ID_2)
                .setTimestamp(1L).build();
        List<XtrId> expiredMappings = Lists.newArrayList();
        List<Object> mappingRecords =
                Lists.newArrayList(expiredMappingRecord_1, expiredMappingRecord_2);

        assertNull(MappingMergeUtil.mergeXtrIdMappings(mappingRecords, expiredMappings, null));
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

        final MappingRecord expiredMappingRecord_1 = getDefaultMappingRecordBuilder()
                .setTimestamp(timestamp_1).build();
        final MappingRecord expiredMappingRecord_2 = getDefaultMappingRecordBuilder()
                .setSourceRloc(IPV4_SOURCE_RLOC_2)
                .setXtrId(XTR_ID_2)
                .setTimestamp(timestamp_2).build();
        final MappingRecord expiredMappingRecord_3 = getDefaultMappingRecordBuilder()
                .setSourceRloc(IPV4_SOURCE_RLOC_3)
                .setXtrId(XTR_ID_3)
                .setTimestamp(timestamp_3).build();

        Set<IpAddress> sourceRlocs = Sets.newHashSet();
        final List<XtrId> expiredMappings = Lists.newArrayList();
        final List<Object> mappingRecords =
                Lists.newArrayList(expiredMappingRecord_1, expiredMappingRecord_2, expiredMappingRecord_3);

        // result
        MappingRecord result = MappingMergeUtil.mergeXtrIdMappings(mappingRecords, expiredMappings, sourceRlocs);
        assertEquals(timestamp_2, (long) result.getTimestamp());
        assertEquals(XTR_ID_2, result.getXtrId());

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
        MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        assertTrue(MappingMergeUtil.mappingIsExpired(mappingRecordBuilder.setTimestamp(timestamp - (REGISTRATION_VALIDITY + 1L)).build()));
        assertFalse(MappingMergeUtil.mappingIsExpired(mappingRecordBuilder.setTimestamp(timestamp).build()));
        assertFalse(MappingMergeUtil.mappingIsExpired(mappingRecordBuilder.setTimestamp(null).build()));
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

    /**
     * Tests {@link MappingMergeUtil#computeNbSbIntersection} method, Nb mask greater than SB mask.
     */
    @Test
    public void computeNbSbIntersectionTest_withMaskableIpv4PrefixEIDs_() {
        MappingRecord NBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_1).build();
        MappingRecord SBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2).build();

        // result
        MappingRecord result = (MappingRecord) MappingMergeUtil
                .computeNbSbIntersection(NBmappingRecord, SBmappingRecord);

        assertEquals(IPV4_PREFIX_EID_1, result.getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#computeNbSbIntersection} method, Nb mask less than SB mask.
     */
    @Test
    public void computeNbSbIntersectionTest_withMaskableIpv4PrefixEIDs() {
        MappingRecord NBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2).build();
        MappingRecord SBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_1).build();

        // result
        MappingRecord result = (MappingRecord) MappingMergeUtil
                .computeNbSbIntersection(NBmappingRecord, SBmappingRecord);

        assertEquals(IPV4_PREFIX_EID_1, result.getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#computeNbSbIntersection} method with SourceDestKey Eid address type, Ipv4Prefix
     * SB Eid, Nb mask less than SB mask.
     */
    @Test
    public void computeNbSbIntersectionTest_withMaskableSourceDestKeyEIDs_Ipv4SB() {
        MappingRecord NBmappingRecord = getDefaultMappingRecordBuilder().setEid(SOURCE_DEST_KEY_EID_1).build();
        MappingRecord SBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_1).build();

        // result
        final MappingRecord result = (MappingRecord) MappingMergeUtil
                .computeNbSbIntersection(NBmappingRecord, SBmappingRecord);
        final Eid eid = LispAddressUtil.asSrcDstEid(IPV4_STRING_3 ,IPV4_STRING_1 ,24 , 24, 1);

        assertEquals(eid, result.getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#computeNbSbIntersection} method with SourceDestKey Eid address type, Ipv6Prefix
     * SB Eid, Nb mask less than SB mask.
     */
    @Test
    public void computeNbSbIntersectionTest_withMaskableSourceDestKeyEIDs_Ipv6SB() {
        MappingRecord NBmappingRecord = getDefaultMappingRecordBuilder().setEid(SOURCE_DEST_KEY_EID_1).build();
        MappingRecord SBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV6_PREFIX_EID).build();

        // result
        final MappingRecord result = (MappingRecord) MappingMergeUtil
                .computeNbSbIntersection(NBmappingRecord, SBmappingRecord);
        final Eid eid = LispAddressUtil.asSrcDstEid(IPV4_STRING_3 ,IPV6_STRING ,24 , 96, 1);

        assertEquals(eid, result.getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#getCommonLocatorRecords} method with empty list of locator records.
     */
    @Test
    public void getCommonLocatorRecords_withEmptyLocatorRecords() {
        MappingRecord NBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_EID_2).build();
        MappingRecord SBmappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_EID_1).build();

        // result
        MappingRecord result = (MappingRecord) MappingMergeUtil
                .computeNbSbIntersection(NBmappingRecord, SBmappingRecord);
        assertEquals(0, result.getLocatorRecord().size());
    }

    /**
     * Tests {@link MappingMergeUtil#getCommonLocatorRecords} method with list of locator records == null.
     */
    @Test
    public void getCommonLocatorRecords_withNullLocatorRecords() {
        MappingRecord NBmappingRecord = getDefaultMappingRecordBuilder().setLocatorRecord(null).build();
        MappingRecord SBmappingRecord = getDefaultMappingRecordBuilder().setLocatorRecord(null).build();

        // result
        MappingRecord result = (MappingRecord) MappingMergeUtil
                .computeNbSbIntersection(NBmappingRecord, SBmappingRecord);
        assertNull(result.getLocatorRecord());
    }

    /**
     * Tests {@link MappingMergeUtil#getCommonLocatorRecords} method, verifies that NB common locator's priority is set
     * to correct value (based on SB locator's priority).
     */
    @Test
    public void getCommonLocatorRecords_priorityCheck() {
        LocatorRecordBuilder NBlocatorRecordBuilder_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setPriority((short) 1)
                .setLocatorId("NB-locator-id");
        LocatorRecordBuilder SBlocatorRecordBuilder_1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setPriority((short) 255)
                .setLocatorId("SB-locator-id");

        LocatorRecordBuilder NBlocatorRecordBuilder_2 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_2)
                .setPriority((short) 1)
                .setLocatorId("NB-locator-id");
        LocatorRecordBuilder SBlocatorRecordBuilder_2 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_2)
                .setPriority((short) 254)
                .setLocatorId("SB-locator-id");

        final MappingRecordBuilder NBmappingRecordBuilder = getDefaultMappingRecordBuilder();
        NBmappingRecordBuilder.getLocatorRecord().add(NBlocatorRecordBuilder_1.build());
        NBmappingRecordBuilder.getLocatorRecord().add(NBlocatorRecordBuilder_2.build());

        final MappingRecordBuilder SBmappingRecordBuilder = getDefaultMappingRecordBuilder();
        SBmappingRecordBuilder.getLocatorRecord().add(SBlocatorRecordBuilder_1.build());
        SBmappingRecordBuilder.getLocatorRecord().add(SBlocatorRecordBuilder_2.build());

        // result
        final MappingRecord result = (MappingRecord) MappingMergeUtil
                .computeNbSbIntersection(NBmappingRecordBuilder.build(), SBmappingRecordBuilder.build());
        final Iterator<LocatorRecord> iterator = result.getLocatorRecord().iterator();
        final LocatorRecord resultLocator_1 = iterator.next();
        final LocatorRecord resultLocator_2 = iterator.next();

        assertEquals(2, result.getLocatorRecord().size());

        assertEquals("NB-locator-id", resultLocator_1.getLocatorId());
        assertEquals(255, (short) resultLocator_1.getPriority()); // priority changed to 255

        assertEquals("NB-locator-id", resultLocator_2.getLocatorId());
        assertEquals(1, (short) resultLocator_2.getPriority());   // priority remains original
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
