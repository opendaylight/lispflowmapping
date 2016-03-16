/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;


public class MultiSiteScenario {

    final String SITE_A_EID_PREFIX = "192.0.1.0";
    final String SITE_B_EID_PREFIX = "192.0.2.0";
    final String SITE_C_EID_PREFIX = "192.0.3.0";
    final String SITE_D_EID_PREFIX = "192.0.4.0";
    final String SITE_E_EID_PREFIX = "192.0.5.0";

    final String SITE_A_IP = "192.0.1.5";
    final String SITE_B_4_IP = "192.0.2.4";
    final String SITE_B_5_IP = "192.0.2.5";
    final String SITE_C_IP = "192.0.3.4";

    final String SITE_A_RLOC = "1.1.1.1";
    final String SITE_B_RLOC = "2.2.2.2";
    final String SITE_C_RLOC = "3.3.3.3";
    final String SITE_D_RLOC = "4.4.4.4";
    final String SITE_E_RLOC = "5.5.5.5";

    private final int VNI_2_VALUE = 2;
    private final InstanceIdType VNI_2 = new InstanceIdType((long) VNI_2_VALUE);

    private final int DEFAULT_NETWORK_MASK = 24;
    private final int IP_MASK = 32;

    private final byte[] SITE_ID_A_VALUE = new byte[]{'A', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    final SiteId SITE_ID_A = new SiteId(SITE_ID_A_VALUE);
    private final byte[] SITE_ID_B_VALUE = new byte[]{'B', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    final SiteId SITE_ID_B = new SiteId(SITE_ID_B_VALUE);
    private final byte[] SITE_ID_C_VALUE = new byte[]{'C', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    final SiteId SITE_ID_C = new SiteId(SITE_ID_C_VALUE);
    private final byte[] SITE_ID_D_VALUE = new byte[]{'D', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    final SiteId SITE_ID_D = new SiteId(SITE_ID_D_VALUE);
    private final byte[] SITE_ID_E_VALUE = new byte[]{'E', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    final SiteId SITE_ID_E = new SiteId(SITE_ID_E_VALUE);

    private final Integer TTL = 1440;
    private final String MAP_RECORD_A = "MAP_RECORD_1";

    private final Short DEFAULT_PRIORITY = 1;
    private final Short DEFAULT_WEIGHT = 1;
    private final Short DEFAULT_MULTICAST_PRIORITY = 255;
    private final Short DEFAULT_MULTICAST_WEIGHT = 0;
    private final Boolean DEFAULT_LOCAL_LOCATOR = true;
    private final Boolean DEFAULT_RLOC_PROBED = false;
    private final Boolean DEFAULT_ROUTED = true;

    private final MappingAuthkey NULL_AUTH_KEY = new MappingAuthkeyBuilder().setKeyType(0).build();
    private final IMappingService mapService;
    private final IFlowMapping lms;

    MultiSiteScenario(final IMappingService mapService, final IFlowMapping lms) {
        this.mapService = mapService;
        this.lms = lms;
    }

    private void verifySingleIpv4RlocMapping(final MapReply mapReply, final MappingRecord.Action action) {
        final MappingRecord mappingRecord = verifySingleIpv4RlocMappingCommon(mapReply);
        assertEquals(action, mappingRecord.getAction());
    }

    private void verifySingleIpv4RlocMapping(final MapReply mapReply, final String siteRloc) {
        final MappingRecord mappingRecord = verifySingleIpv4RlocMappingCommon(mapReply);
        final List<LocatorRecord> locatorRecords = mappingRecord.getLocatorRecord();
        assertNotNull(locatorRecords);
        assertEquals(1, locatorRecords.size());
        final LocatorRecord locatorRecord = locatorRecords.get(0);
        assertNotNull(locatorRecord);
        final Rloc rloc = locatorRecord.getRloc();
        assertNotNull(rloc);
        final Address address = rloc.getAddress();
        assertTrue(address instanceof Ipv4);
        assertEquals(siteRloc, ((Ipv4) address).getIpv4().getValue());
    }

    private MappingRecord verifySingleIpv4RlocMappingCommon(MapReply mapReply) {
        assertNotNull(mapReply);
        final List<MappingRecordItem> mappingRecordItems = mapReply.getMappingRecordItem();
        assertNotNull(mappingRecordItems);
        assertEquals(1, mappingRecordItems.size());
        final MappingRecordItem mappingRecordItem = mappingRecordItems.get(0);
        assertNotNull(mappingRecordItem);
        final MappingRecord mappingRecord = mappingRecordItem.getMappingRecord();
        assertNotNull(mappingRecord);
        return mappingRecord;
    }

    private void emitMapRegisterMessage(final IMappingService mapService, final IFlowMapping lms, final
    MappingOrigin mappingOrigin, final String destSiteEidPrefix, final String siteRloc, final int vniValue) {
        final MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        final MappingRecordItemBuilder mappingRecordItemBuilder = new MappingRecordItemBuilder();
        mappingRecordItemBuilder.setMappingRecordItemId(MAP_RECORD_A);

        final MappingRecordBuilder mrb = prepareMappingRecord(mappingOrigin, siteRloc, null,
                destSiteEidPrefix, vniValue);
        mappingRecordItemBuilder.setMappingRecord(mrb.build());
        mapRegisterBuilder.setMappingRecordItem(Collections.singletonList(mappingRecordItemBuilder.build()));


        Eid eid = toEid(destSiteEidPrefix, new InstanceIdType((long) vniValue), DEFAULT_NETWORK_MASK);
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);
        lms.handleMapRegister(mapRegisterBuilder.build());
    }

    private Eid toEid(final String destSiteEidPrefix, final InstanceIdType vniValue, final int mask) {
        return LispAddressUtil.toEid(new Ipv4Prefix(destSiteEidPrefix + "/" + mask), vniValue);

    }

    private MapReply emitMapRequestMessage(final String siteFromEidPrefix, final String siteToEidPrefix, final
    InstanceIdType vniValue) {
        final MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        final EidItemBuilder eidItemBuilder = new EidItemBuilder();
        Eid dstEid = toEid(siteToEidPrefix, vniValue, IP_MASK);

        eidItemBuilder.setEid(dstEid);
        eidItemBuilder.setEidItemId(siteFromEidPrefix + siteToEidPrefix);
        final List<EidItem> eidItem = Collections.singletonList(eidItemBuilder.build());
        final Eid srcEid = toEid(siteFromEidPrefix, vniValue, IP_MASK);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setEid(srcEid).build());
        mapRequestBuilder.setEidItem(eidItem);
        return lms.handleMapRequest(mapRequestBuilder.build());
    }

    private void createMappingNegative(final MappingOrigin mapOrigin, final String
            siteEidPrefix, final SiteId siteId, final MappingRecord.Action action) {
        final Ipv4Prefix ipv4Prefix = new Ipv4Prefix(siteEidPrefix + "/" + DEFAULT_NETWORK_MASK);
        final Eid eidAsIpv4Prefix = LispAddressUtil.toEid(ipv4Prefix, VNI_2);

        final MappingRecordBuilder mrbNegative = prepareMappingRecord(mapOrigin, null, null, null, VNI_2_VALUE);
        mrbNegative.setEid(eidAsIpv4Prefix);
        mrbNegative.setAction(action);

        mapService.addMapping(mapOrigin, eidAsIpv4Prefix, SITE_ID_B, mrbNegative.build());
    }

    private void storeSourceDestinationSiteMappingViaNorthbound(final IMappingService mapService, final int vniValue,
                                                                final SiteId siteId, final String siteToEidPrefix,
                                                                final String siteFromEidPrefix, final String siteRloc) {
        final MappingRecordBuilder mrb = prepareMappingRecord(MappingOrigin.Northbound, siteRloc, siteFromEidPrefix,
                siteToEidPrefix, vniValue);
        mapService.addMapping(MappingOrigin.Northbound, mrb.getEid(), siteId, mrb.build());
    }

    private void storeDestinationSiteMappingViaSouthbound(final int vniValue, final SiteId siteId, final String
            destSiteEidPrefix, final String siteRloc) {
        emitMapRegisterMessage(mapService, lms, MappingOrigin.Southbound, destSiteEidPrefix, siteRloc, vniValue);
    }

    private MappingRecordBuilder prepareMappingRecord(final MappingOrigin mappingOrigin, final String siteRloc,
                                                      final String sourceSiteEidPrefix, final String
                                                              destSiteEidPrefix, final int vniValue) {
        final MappingRecordBuilder mrb = provideCommonMapRecordBuilder();
        Eid eid = null;
        if (MappingOrigin.Northbound.equals(mappingOrigin)) {
            if (sourceSiteEidPrefix != null && destSiteEidPrefix != null) {
                eid = LispAddressUtil.asSrcDstEid(sourceSiteEidPrefix, destSiteEidPrefix, DEFAULT_NETWORK_MASK,
                        DEFAULT_NETWORK_MASK, vniValue);
            }
        } else if (MappingOrigin.Southbound.equals(mappingOrigin)) {
            if (destSiteEidPrefix != null) {
                eid = toEid(destSiteEidPrefix, new InstanceIdType((long) vniValue), DEFAULT_NETWORK_MASK);
            }
        }
        mrb.setEid(eid);

        if (siteRloc != null) {
            mrb.setLocatorRecord(provideLocatorRecord(LispAddressUtil.toRloc(new Ipv4Address(siteRloc)), siteRloc));
        } else {
            mrb.setLocatorRecord(null);
        }

        mrb.setTimestamp(System.currentTimeMillis());
        mrb.setAction(MappingRecord.Action.NoAction);
        mrb.setRecordTtl(TTL);
        return mrb;
    }

    private List<LocatorRecord> provideLocatorRecord(final Rloc rloc, final String rlocStr) {
        final LocatorRecordBuilder locatorRecordBuilder = new LocatorRecordBuilder();
        locatorRecordBuilder.setRloc(rloc);
        locatorRecordBuilder.setLocatorId(rlocStr);
        locatorRecordBuilder.setPriority(DEFAULT_PRIORITY);
        locatorRecordBuilder.setWeight(DEFAULT_WEIGHT);
        locatorRecordBuilder.setMulticastPriority(DEFAULT_MULTICAST_PRIORITY);
        locatorRecordBuilder.setMulticastWeight(DEFAULT_MULTICAST_WEIGHT);
        locatorRecordBuilder.setLocalLocator(DEFAULT_LOCAL_LOCATOR);
        locatorRecordBuilder.setRlocProbed(DEFAULT_RLOC_PROBED);
        locatorRecordBuilder.setRouted(DEFAULT_ROUTED);

        final List<LocatorRecord> locatorRecords = new ArrayList<>();
        locatorRecords.add(locatorRecordBuilder.build());
        return locatorRecords;
    }

    private MappingRecordBuilder provideCommonMapRecordBuilder() {
        final MappingRecordBuilder mappingRecordBuilder = new MappingRecordBuilder();
        mappingRecordBuilder.setRecordTtl(1440);
        mappingRecordBuilder.setAction(MappingRecord.Action.NoAction);
        mappingRecordBuilder.setAuthoritative(true);
        return mappingRecordBuilder;
    }

    void storeMapingForMultiSiteScenario() {
        storeDestinationSiteMappingViaSouthbound(VNI_2_VALUE, SITE_ID_A, SITE_A_EID_PREFIX, SITE_A_RLOC);
        storeDestinationSiteMappingViaSouthbound(VNI_2_VALUE, SITE_ID_B, SITE_B_EID_PREFIX, SITE_B_RLOC);
        storeDestinationSiteMappingViaSouthbound(VNI_2_VALUE, SITE_ID_C, SITE_C_EID_PREFIX, SITE_C_RLOC);
        storeDestinationSiteMappingViaSouthbound(VNI_2_VALUE, SITE_ID_D, SITE_D_EID_PREFIX, SITE_D_RLOC);
        storeDestinationSiteMappingViaSouthbound(VNI_2_VALUE, SITE_ID_E, SITE_E_EID_PREFIX, SITE_E_RLOC);

        storeSourceDestinationSiteMappingViaNorthbound(mapService, VNI_2_VALUE, SITE_ID_B, SITE_C_EID_PREFIX,
                SITE_B_EID_PREFIX, SITE_C_RLOC);
        storeSourceDestinationSiteMappingViaNorthbound(mapService, VNI_2_VALUE, SITE_ID_C, SITE_B_EID_PREFIX,
                SITE_C_EID_PREFIX, SITE_B_RLOC);
        createMappingNegative(MappingOrigin.Northbound, SITE_C_EID_PREFIX, SITE_ID_C, MappingRecord.Action
                .Drop);
    }

    void emitMapRequestMessages() {
        final MapReply mapReplyFromAToB = emitMapRequestMessage(SITE_A_IP, SITE_B_4_IP, VNI_2);
        verifySingleIpv4RlocMapping(mapReplyFromAToB, SITE_B_RLOC);

        final MapReply mapReplyFromBToA = emitMapRequestMessage(SITE_B_4_IP, SITE_A_IP, VNI_2);
        verifySingleIpv4RlocMapping(mapReplyFromBToA, SITE_A_RLOC);

        final MapReply mapReplyFromBToC = emitMapRequestMessage(SITE_B_5_IP, SITE_C_IP, VNI_2);
        verifySingleIpv4RlocMapping(mapReplyFromBToC, SITE_C_RLOC);

        final MapReply mapReplyFromCToB = emitMapRequestMessage(SITE_C_IP, SITE_B_5_IP, VNI_2);
        verifySingleIpv4RlocMapping(mapReplyFromCToB, SITE_B_RLOC);

        final MapReply mapReplyFromAToC = emitMapRequestMessage(SITE_A_IP, SITE_C_IP, VNI_2);
        verifySingleIpv4RlocMapping(mapReplyFromAToC, MappingRecord.Action.Drop);
    }

    private void sleepForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {

        }
    }


}
