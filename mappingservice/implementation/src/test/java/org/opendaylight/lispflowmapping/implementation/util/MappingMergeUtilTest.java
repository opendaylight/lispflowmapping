/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

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

public class MappingMergeUtilTest {

    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";
    private static final String IPV4_RLOC_STRING_1 = "100.100.100.100";
    private static final String IPV4_RLOC_STRING_2 = "200.200.200.200";
    private static final String IPV4_STRING_PREFIX = "/24";
    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_STRING_PREFIX);
    private static final Eid IPV4_PREFIX_EID_2 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_2 + IPV4_STRING_PREFIX);

    private static final XtrId XTR_ID_1 = new XtrId(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    private static final XtrId XTR_ID_2 = new XtrId(new byte[] {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
    private static final XtrId XTR_ID_3 = new XtrId(new byte[] {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3});

    private static final SiteId SITE_ID_1 = new SiteId(new byte[]{1, 1, 1, 1, 1, 1, 1, 1});
    private static final SiteId SITE_ID_2 = new SiteId(new byte[]{2, 2, 2, 2, 2, 2, 2, 2});

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method.
     */
    @Test
    public void mergeMappings() {
        final MappingRecord currentMergedMapping = getDefaultMappingRecordBuilder().build();
        final LocatorRecord newLocatorRecord = getDefaultLocatorRecordBuilder()
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_2)).build(); // change the locator Rloc
        final MappingRecordBuilder newMappingRecordBuilder = getDefaultMappingRecordBuilder().setEid(IPV4_PREFIX_EID_2)
                .setLocatorRecord(Lists.newArrayList())
                .setXtrId(XTR_ID_2)
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);
        newMappingRecordBuilder.getLocatorRecord().add(newLocatorRecord);
        final Date timestamp = new Date();

        // result
        MappingRecordBuilder mrb = new MappingRecordBuilder(currentMergedMapping)
                .setXtrId(XTR_ID_3)
                .setTimestamp(timestamp.getTime())
                .setRecordTtl(1)
                .setSiteId(SITE_ID_2);

        MappingRecord result = MappingMergeUtil.
                mergeMappings(currentMergedMapping, newMappingRecordBuilder.build(), XTR_ID_3, timestamp);
    }

    /**
     * Tests {@link MappingMergeUtil#mergeMappings} method with currentMergedMapping == null.
     */
    @Test
    public void mergeMappings_withNullCurrentMergedMapping() {
        assertNull(MappingMergeUtil.mergeMappings(null, null, null, null));
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        MappingRecordBuilder mappingRecordBuilder = new MappingRecordBuilder()
                .setEid(IPV4_PREFIX_EID_1)
                .setLocatorRecord(new ArrayList<>())
                .setRecordTtl(2)
                .setAction(MappingRecord.Action.NativelyForward)
                .setAuthoritative(true)
                .setMapVersion((short) 1)
                .setMaskLength((short) 1)
                .setSiteId(SITE_ID_1)
                .setSourceRloc(new IpAddress(new Ipv4Address("192.168.0.1")))
                .setTimestamp(1L)
                .setXtrId(XTR_ID_1);
        mappingRecordBuilder.getLocatorRecord().add(getDefaultLocatorRecordBuilder().build());

        return mappingRecordBuilder;
    }

    private static LocatorRecordBuilder getDefaultLocatorRecordBuilder() {
        return new LocatorRecordBuilder()
                .setRloc(LispAddressUtil.asIpv4Rloc(IPV4_RLOC_STRING_1))
                .setLocatorId("locator-id");
    }
}
