/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Test;
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

    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";
    private static final String IPV4_RLOC_STRING_1 = "101.101.101.101";
    private static final String IPV4_RLOC_STRING_2 = "102.102.102.102";
    private static final String IPV4_RLOC_STRING_3 = "103.103.103.103";
    private static final String IPV4_RLOC_STRING_4 = "104.104.104.104";
    private static final String IPV4_STRING_PREFIX = "/24";
    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_STRING_PREFIX);
    private static final Eid IPV4_PREFIX_EID_2 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_2 + IPV4_STRING_PREFIX);
    private static final Rloc IPV4_RLOC_1 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1);
    private static final Rloc IPV4_RLOC_2 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_2);
    private static final Rloc IPV4_RLOC_3 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_3);
    private static final Rloc IPV4_RLOC_4 = LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_4);

    private static final String LOCATOR_ID_STRING = "locator-id";

    private static final XtrId XTR_ID_1 = new XtrId(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    private static final XtrId XTR_ID_2 = new XtrId(new byte[] {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
    private static final XtrId XTR_ID_3 = new XtrId(new byte[] {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3});

    private static final SiteId SITE_ID_1 = new SiteId(new byte[]{1, 1, 1, 1, 1, 1, 1, 1});
    private static final SiteId SITE_ID_2 = new SiteId(new byte[]{2, 2, 2, 2, 2, 2, 2, 2});

    private static final LocatorRecordBuilder LOCATOR_RECORD = new LocatorRecordBuilder();

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with a new LocatorRecord equal to current LocatorRecord
     * and null XtrId.
     */
    @Test
    public void mergeMappingsTest_withSingleEqualLocator() {
        // current Locator records
        final LocatorRecord currentLocatorRecord_1 = LOCATOR_RECORD             // same locator
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator records
        final LocatorRecord newLocatorRecord_1 = LOCATOR_RECORD                 // same locator
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // result
        MappingRecordBuilder mrb = new MappingRecordBuilder(currentMergedMappingBuilder.build())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with a new LocatorRecord not equal to current LocatorRecord
     * but same Rloc, where current locator is local. XtrId is null.
     */
    @Test
    public void mergeMappingsTest_withSingleSameRloc_currentIsLocal() {
        // current Locator records
        final LocatorRecord currentLocatorRecord_1 = LOCATOR_RECORD             // same Rloc
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING)
                .setLocalLocator(true).build();                                 // local locator set to true

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator records
        final LocatorRecord newLocatorRecord_1 = LOCATOR_RECORD                 // same Rloc
                .setRloc(IPV4_RLOC_1)
                .setLocatorId("locator-id-1").build();                          // different locator id

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // result
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
     * Tests {@link MappingMergeUtil#mergeMappings} method with a new LocatorRecord not equal to current LocatorRecord
     * but same Rloc, where current locator is not local. XtrId is null.
     */
    @Test
    public void mergeMappingsTest_withSingleSameRloc_currentIsNotLocal() {
        // current Locator records
        final LocatorRecord currentLocatorRecord_1 = LOCATOR_RECORD             // same Rloc
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING)
                .setLocalLocator(false).build();                                // local locator set to false

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator records
        final LocatorRecord newLocatorRecord_1 = LOCATOR_RECORD                 // same Rloc
                .setRloc(IPV4_RLOC_1)
                .setLocatorId("locator-id-1").build();                          // different locator id

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // result
        MappingRecordBuilder mrb = new MappingRecordBuilder(getDefaultMappingRecordBuilder().build())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        mrb.getLocatorRecord().add(newLocatorRecord_1);

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), null, null);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with different Rlocs. XtrId is null.
     */
    @Test
    public void mergeMappingsTest_withSingleDifferentRloc() {
        // current Locator records
        final LocatorRecord currentLocatorRecord_1 = LOCATOR_RECORD             // different locator record
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);

        // new locator records
        final LocatorRecord newLocatorRecord_1 = LOCATOR_RECORD                 // different locator record
                .setRloc(IPV4_RLOC_2)
                .setLocatorId("locator-id-1").build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);

        // result
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
     * Tests {@link MappingMergeUtil#mergeMappings} method.
     */
    //@Test
    public void mergeMappingsTest() {
        // new locator records
        final LocatorRecord newLocatorRecord_1 = LOCATOR_RECORD                 // same locator
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final LocatorRecord newLocatorRecord_2 = LOCATOR_RECORD                 // same locator Rloc
                .setRloc(IPV4_RLOC_2)
                .setLocatorId(LOCATOR_ID_STRING + "-1").build();

        final LocatorRecord newLocatorRecord_3 = LOCATOR_RECORD                 // new Rloc
                .setRloc(IPV4_RLOC_3)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_1);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord_3);

        // current Locator records
        final LocatorRecord currentLocatorRecord_1 = LOCATOR_RECORD             // same locator
                .setRloc(IPV4_RLOC_1)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final LocatorRecord currentLocatorRecord_2 = LOCATOR_RECORD             // same locator Rloc
                .setRloc(IPV4_RLOC_2)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final LocatorRecord currentLocatorRecord_3 = LOCATOR_RECORD             // locator record to exclude
                .setRloc(IPV4_RLOC_4)
                .setLocatorId(LOCATOR_ID_STRING).build();

        final MappingRecordBuilder currentMergedMappingBuilder = getDefaultMappingRecordBuilder();
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_1);
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_2);
        currentMergedMappingBuilder.getLocatorRecord().add(currentLocatorRecord_3);
        final Date timestamp = new Date();

        // result
        MappingRecordBuilder mrb = new MappingRecordBuilder(currentMergedMappingBuilder.build())
                .setXtrId(XTR_ID_3)
                .setTimestamp(timestamp.getTime())
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);

        MappingRecord result = MappingMergeUtil.mergeMappings
                (currentMergedMappingBuilder.build(), newMappingRecordBuilder.build(), XTR_ID_3, timestamp);
        assertEquals(mrb.build(),result);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with currentMergedMapping == null.
     */
    @Test
    public void mergeMappingsTest_withNullCurrentMergedMapping() {
        assertNull(MappingMergeUtil.mergeMappings(null, null, null, null));
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setEid(IPV4_PREFIX_EID_1)
                .setLocatorRecord(new ArrayList<>())
                .setRecordTtl(2)
                .setAction(MappingRecord.Action.NativelyForward)
                .setAuthoritative(true)
                .setMapVersion((short) 1)
                .setMaskLength((short) 1)
                .setSiteId(SITE_ID_1)
                .setSourceRloc(new IpAddress(new Ipv4Address("192.168.0.1")))
                .setTimestamp(new Date().getTime())
                .setXtrId(XTR_ID_1);
    }
}
