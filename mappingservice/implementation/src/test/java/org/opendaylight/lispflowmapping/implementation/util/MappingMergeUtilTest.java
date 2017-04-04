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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.junit.Test;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

public class MappingMergeUtilTest {

    private static final String IPV4_STRING_1 =         "1.2.3.0";
    private static final String IPV4_STRING_2 =         "1.2.4.0";
    private static final String IPV4_STRING_3 =         "1.2.5.0";
    private static final String IPV4_STRING_4 =         "1.2.6.0";
    private static final String IPV6_STRING =           "1111:2222:3333:4444:5555:6666:7777:8888";
    private static final String IPV4_RLOC_STRING_1 =    "101.101.101.101";
    private static final String IPV4_RLOC_STRING_2 =    "102.102.102.102";
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
    private static final IpAddressBinary IPV4_SOURCE_RLOC_1 = new IpAddressBinary(
            new Ipv4AddressBinary(new byte[] {1, 1, 1, 1}));

    private static final XtrId XTR_ID_1 = new XtrId(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

    private static final SiteId SITE_ID_1 = new SiteId(new byte[]{1, 1, 1, 1, 1, 1, 1, 1});

    private static final long REGISTRATION_VALIDITY = ConfigIni.getInstance().getRegistrationValiditySb();

    /**
     * Tests {@link MappingMergeUtil#mappingIsExpired} method.
     */
    @Test
    public void mappingIsExpiredTest() {
        long timestamp = new Date().getTime();
        MappingData mappingData = getDefaultMappingData();
        mappingData.setTimestamp(new Date(timestamp - (REGISTRATION_VALIDITY + 1L)));
        assertTrue(MappingMergeUtil.mappingIsExpired(mappingData));
        mappingData.setTimestamp(new Date(timestamp));
        assertFalse(MappingMergeUtil.mappingIsExpired(mappingData));
        mappingData.setTimestamp(null);
        assertFalse(MappingMergeUtil.mappingIsExpired(mappingData));
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
        MappingRecord nbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_1).build();
        MappingRecord sbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2).build();
        MappingData nbMappingData = getDefaultMappingData(nbMappingRecord);
        MappingData sbMappingData = getDefaultMappingData(sbMappingRecord);

        // result
        MappingData result = MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);

        assertEquals(IPV4_PREFIX_EID_1, result.getRecord().getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#computeNbSbIntersection} method, Nb mask less than SB mask.
     */
    @Test
    public void computeNbSbIntersectionTest_withMaskableIpv4PrefixEIDs() {
        MappingRecord nbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2).build();
        MappingRecord sbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_1).build();
        MappingData nbMappingData = getDefaultMappingData(nbMappingRecord);
        MappingData sbMappingData = getDefaultMappingData(sbMappingRecord);

        // result
        MappingData result = MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);

        assertEquals(IPV4_PREFIX_EID_1, result.getRecord().getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#computeNbSbIntersection} method with SourceDestKey Eid address type, Ipv4Prefix
     * SB Eid, Nb mask less than SB mask.
     */
    @Test
    public void computeNbSbIntersectionTest_withMaskableSourceDestKeyEIDs_Ipv4SB() {
        MappingRecord nbMappingRecord = getDefaultMappingRecordBuilder().setEid(SOURCE_DEST_KEY_EID_1).build();
        MappingRecord sbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_1).build();
        MappingData nbMappingData = getDefaultMappingData(nbMappingRecord);
        MappingData sbMappingData = getDefaultMappingData(sbMappingRecord);

        // result
        final MappingData result = MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);
        final Eid eid = LispAddressUtil.asSrcDstEid(IPV4_STRING_3 ,IPV4_STRING_1 ,24 , 24, 1);

        assertEquals(eid, result.getRecord().getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#computeNbSbIntersection} method with SourceDestKey Eid address type, Ipv6Prefix
     * SB Eid, Nb mask less than SB mask.
     */
    @Test
    public void computeNbSbIntersectionTest_withMaskableSourceDestKeyEIDs_Ipv6SB() {
        MappingRecord nbMappingRecord = getDefaultMappingRecordBuilder().setEid(SOURCE_DEST_KEY_EID_1).build();
        MappingRecord sbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV6_PREFIX_EID).build();
        MappingData nbMappingData = getDefaultMappingData(nbMappingRecord);
        MappingData sbMappingData = getDefaultMappingData(sbMappingRecord);

        // result
        final MappingData result = MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);
        final Eid eid = LispAddressUtil.asSrcDstEid(IPV4_STRING_3 ,IPV6_STRING ,24 , 96, 1);

        assertEquals(eid, result.getRecord().getEid());
    }

    /**
     * Tests {@link MappingMergeUtil#getCommonLocatorRecords} method with empty list of locator records.
     */
    @Test
    public void getCommonLocatorRecords_withEmptyLocatorRecords() {
        MappingRecord nbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_EID_2).build();
        MappingRecord sbMappingRecord = getDefaultMappingRecordBuilder().setEid(IPV4_EID_1).build();
        MappingData nbMappingData = getDefaultMappingData(nbMappingRecord);
        MappingData sbMappingData = getDefaultMappingData(sbMappingRecord);

        // result
        MappingData result = MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);
        assertEquals(0, result.getRecord().getLocatorRecord().size());
    }

    /**
     * Tests {@link MappingMergeUtil#getCommonLocatorRecords} method with list of locator records == null.
     */
    @Test
    public void getCommonLocatorRecords_withNullLocatorRecords() {
        MappingRecord nbMappingRecord = getDefaultMappingRecordBuilder().setLocatorRecord(null).build();
        MappingRecord sbMappingRecord = getDefaultMappingRecordBuilder().setLocatorRecord(null).build();
        MappingData nbMappingData = getDefaultMappingData(nbMappingRecord);
        MappingData sbMappingData = getDefaultMappingData(sbMappingRecord);

        // result
        MappingData result = MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);
        assertNull(result.getRecord().getLocatorRecord());
    }

    /**
     * Tests {@link MappingMergeUtil#getCommonLocatorRecords} method, verifies that NB common locator's priority is set
     * to correct value (based on SB locator's priority).
     */
    @Test
    public void getCommonLocatorRecords_priorityCheck() {
        LocatorRecordBuilder nbLocatorRecordBuilder1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setPriority((short) 1)
                .setLocatorId("NB-locator-id");
        LocatorRecordBuilder sbLocatorRecordBuilder1 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_1)
                .setPriority((short) 255)
                .setLocatorId("SB-locator-id");

        LocatorRecordBuilder nbLocatorRecordBuilder2 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_2)
                .setPriority((short) 1)
                .setLocatorId("NB-locator-id");
        LocatorRecordBuilder sbLocatorRecordBuilder2 = new LocatorRecordBuilder()
                .setRloc(IPV4_RLOC_2)
                .setPriority((short) 254)
                .setLocatorId("SB-locator-id");

        final MappingRecordBuilder nbMappingRecordBuilder = getDefaultMappingRecordBuilder();
        nbMappingRecordBuilder.getLocatorRecord().add(nbLocatorRecordBuilder1.build());
        nbMappingRecordBuilder.getLocatorRecord().add(nbLocatorRecordBuilder2.build());

        final MappingRecordBuilder sbMappingRecordBuilder = getDefaultMappingRecordBuilder();
        sbMappingRecordBuilder.getLocatorRecord().add(sbLocatorRecordBuilder1.build());
        sbMappingRecordBuilder.getLocatorRecord().add(sbLocatorRecordBuilder2.build());

        MappingData nbMappingData = getDefaultMappingData(nbMappingRecordBuilder.build());
        MappingData sbMappingData = getDefaultMappingData(sbMappingRecordBuilder.build());

        // result
        final MappingData result = MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);
        final Iterator<LocatorRecord> iterator = result.getRecord().getLocatorRecord().iterator();
        final LocatorRecord resultLocator_1 = iterator.next();
        final LocatorRecord resultLocator_2 = iterator.next();

        assertEquals(2, result.getRecord().getLocatorRecord().size());

        assertEquals("NB-locator-id", resultLocator_1.getLocatorId());
        assertEquals(255, (short) resultLocator_1.getPriority()); // priority changed to 255

        assertEquals("NB-locator-id", resultLocator_2.getLocatorId());
        assertEquals(1, (short) resultLocator_2.getPriority());   // priority remains original
    }

    private static MappingData getDefaultMappingData() {
        return getDefaultMappingData(null);
    }

    private static MappingData getDefaultMappingData(MappingRecord mappingRecord) {
        if (mappingRecord == null) {
            mappingRecord = getDefaultMappingRecordBuilder().build();
        }
        return new MappingData(MappingOrigin.Southbound, mappingRecord, System.currentTimeMillis());
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
