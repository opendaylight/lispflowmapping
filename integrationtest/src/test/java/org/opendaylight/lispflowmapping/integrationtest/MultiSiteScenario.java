/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.lispflowmapping.integrationtest.MappingServiceIntegrationTest.ourAddress;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_A;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_B;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_C;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D4;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.Site;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class MultiSiteScenario {

    private final int DEFAULT_NETWORK_MASK = 24;
    private final int IP_MASK = 32;

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

    private static final Logger LOG = LoggerFactory.getLogger(MultiSiteScenario.class);

    MultiSiteScenario(final IMappingService mapService, final IFlowMapping lms) {
        this.mapService = mapService;
        this.lms = lms;
    }

    private void verifySingleIpv4RlocMapping(final MapReply mapReply, final MappingRecord.Action action) {
        final MappingRecord mappingRecord = verifySingleIpv4RlocMappingCommon(mapReply);
        assertEquals(action, mappingRecord.getAction());
    }

    private void verifySingleIpv4RlocMapping(final MapReply mapReply, final String siteRloc, boolean pingWork) {
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
        if (pingWork) {
            assertEquals(siteRloc, ((Ipv4) address).getIpv4().getValue());
        } else {
            assertNotEquals(siteRloc, ((Ipv4) address).getIpv4().getValue());
        }
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

    private void emitMapRegisterMessage(final Site dstSite) {
        final MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        final MappingRecordItemBuilder mappingRecordItemBuilder = new MappingRecordItemBuilder();
        mappingRecordItemBuilder.setMappingRecordItemId(MAP_RECORD_A);

        final MappingRecordBuilder mrb = prepareMappingRecord(MappingOrigin.Southbound, null, dstSite);
        mappingRecordItemBuilder.setMappingRecord(mrb.build());
        mapRegisterBuilder.setMappingRecordItem(Collections.singletonList(mappingRecordItemBuilder.build()));

        lms.handleMapRegister(mapRegisterBuilder.build());
    }

    void setCommonAuthentication() {
        Eid eid = LispAddressUtil.toEid(new Ipv4Prefix("0.0.0.0/0"), SITE_A.getVNI());
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);

        eid = LispAddressUtil.toEid(new Ipv4Prefix("0.0.0.0/0"), SITE_D5.getVNI());
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);
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
        mapRequestBuilder.setItrRloc(prepareDummyItrRloc());
        return lms.handleMapRequest(mapRequestBuilder.build());
    }

    private List<ItrRloc> prepareDummyItrRloc() {
        List<ItrRloc> itrRlocs = new ArrayList<>();
        final ItrRlocBuilder itrRlocBuilder = new ItrRlocBuilder();
        itrRlocBuilder.setItrRlocId(ourAddress);
        itrRlocBuilder.setRloc(LispAddressUtil.asIpv4Rloc(ourAddress));
        itrRlocs.add(itrRlocBuilder.build());
        return itrRlocs;
    }

    void storeNorthMappingNegative(final Site dstSite, final MappingRecord.Action action) {
        final Ipv4Prefix ipv4Prefix = new Ipv4Prefix(dstSite.getEidPrefix() + "/" + DEFAULT_NETWORK_MASK);
        final Eid eidAsIpv4Prefix = LispAddressUtil.toEid(ipv4Prefix, dstSite.getVNI());

        final MappingRecordBuilder mrbNegative = prepareMappingRecord(MappingOrigin.Northbound, null, dstSite);
        mrbNegative.setEid(eidAsIpv4Prefix);
        mrbNegative.setAction(action);

        mapService.addMapping(MappingOrigin.Northbound, eidAsIpv4Prefix, dstSite.getSiteId(), mrbNegative.build());
    }

    void deleteNorthMappingNegative(final Site dstSite) {
        final Ipv4Prefix ipv4Prefix = new Ipv4Prefix(dstSite.getEidPrefix() + "/" + DEFAULT_NETWORK_MASK);
        final Eid eidAsIpv4Prefix = LispAddressUtil.toEid(ipv4Prefix, dstSite.getVNI());

        mapService.removeMapping(MappingOrigin.Northbound, eidAsIpv4Prefix);
    }

    void storeNorthMappingSrcDst(final Site srcSite, final Site dstSite) {
        final MappingRecordBuilder mrb = prepareMappingRecord(MappingOrigin.Northbound, srcSite, dstSite);
        mapService.addMapping(MappingOrigin.Northbound, mrb.getEid(), srcSite.getSiteId(), mrb.build());
    }

    private void storeDestinationSiteMappingViaSouthbound(final Site dstSite) {
        emitMapRegisterMessage(dstSite);
    }

    private MappingRecordBuilder prepareMappingRecord(final MappingOrigin mappingOrigin, final Site srcSite, final
                                                      Site dstSite) {
        final MappingRecordBuilder mrb = provideCommonMapRecordBuilder();
        Eid eid = null;
        if (MappingOrigin.Northbound.equals(mappingOrigin)) {
            if (srcSite != null && dstSite != null && srcSite.getEidPrefix() != null && dstSite.getEidPrefix() !=
                    null) {
                eid = LispAddressUtil.asSrcDstEid(srcSite.getEidPrefix(), dstSite.getEidPrefix(), DEFAULT_NETWORK_MASK,
                        DEFAULT_NETWORK_MASK, dstSite.getVNI().getValue().intValue());
            }
        } else if (MappingOrigin.Southbound.equals(mappingOrigin)) {
            if (dstSite != null && dstSite.getEidPrefix() != null) {
                eid = toEid(dstSite.getEidPrefix(), dstSite.getVNI(), DEFAULT_NETWORK_MASK);
            }
        }

        mrb.setEid(eid);

        if (dstSite.getRloc() != null) {
            mrb.setLocatorRecord(provideLocatorRecord(LispAddressUtil.toRloc(new Ipv4Address(dstSite.getRloc())),
                    dstSite.getRloc()));
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
        mappingRecordBuilder.setRecordTtl(TTL);
        mappingRecordBuilder.setAction(MappingRecord.Action.NoAction);
        mappingRecordBuilder.setAuthoritative(true);
        return mappingRecordBuilder;
    }

    void deleteNorthMapingSrcDst(final Site srcSite, final Site dstSite) {
        final Eid eid = LispAddressUtil.asSrcDstEid(srcSite.getEidPrefix(), dstSite.getEidPrefix(),
                DEFAULT_NETWORK_MASK, DEFAULT_NETWORK_MASK, dstSite.getVNI().getValue().intValue());
        mapService.removeMapping(MappingOrigin.Northbound, eid);
    }

//    void storeNorthMappingBidirect(final Site srcSite, final Site dstSite) {
//        storeNorthMappingSrcDst(srcSite, dstSite);
//        storeNorthMappingSrcDst(dstSite, srcSite);
//    }

    void storeSouthboundMappings() {
        storeDestinationSiteMappingViaSouthbound(SITE_A);
        storeDestinationSiteMappingViaSouthbound(SITE_B);
        storeDestinationSiteMappingViaSouthbound(SITE_C);
        storeDestinationSiteMappingViaSouthbound(SITE_D4);
        storeDestinationSiteMappingViaSouthbound(SITE_D5);
    }

    void pingSimulation(final Site srcSite, final int srcHostIndex, final Site dstSite, final int dstHostIndex) {
        pingSimulation(srcSite, srcHostIndex, dstSite, dstHostIndex, true, true);
    }

    void pingSimulation(final Site srcSite, final int srcHostIndex, final Site dstSite, final int dstHostIndex, final
    boolean pingWorkVniValue, final boolean pingWorkReturnedRloc) {
        final InstanceIdType instanceIdTypeDst = oneWayReachability(srcSite, srcHostIndex, dstSite, dstHostIndex, true);
        final InstanceIdType instanceIdTypeSrc = oneWayReachability(dstSite, dstHostIndex, srcSite, srcHostIndex,
                pingWorkReturnedRloc);
        if (pingWorkVniValue) {
            assertEquals("Ping doesn't work. Vni is different.", instanceIdTypeDst, instanceIdTypeSrc);
        } else {
            assertNotEquals("Ping work. Vni are equals.", instanceIdTypeDst, instanceIdTypeSrc);
        }
    }

    InstanceIdType oneWayReachability(final Site srcSite, final int srcHostIndex, final Site dstSite, final int
            dstHostIndex, final boolean pingWork) {
        final MapReply mapReplyFromSrcToDst = emitMapRequestMessage(srcSite.getHost(srcHostIndex), dstSite.getHost
                (dstHostIndex), dstSite.getVNI());
        verifySingleIpv4RlocMapping(mapReplyFromSrcToDst, dstSite.getRloc(), pingWork);
        return mapReplyFromSrcToDst.getMappingRecordItem().get(0).getMappingRecord().getEid().getVirtualNetworkId();
    }

    void oneWayReachability(final Site srcSite, final int srcHostIndex, final Site dstSite, final int dstHostIndex,
                            final MappingRecord.Action action) {
        final MapReply mapReplyFromSrcToDst = emitMapRequestMessage(srcSite.getHost(srcHostIndex), dstSite.getHost
                        (dstHostIndex), dstSite.getVNI());
        verifySingleIpv4RlocMapping(mapReplyFromSrcToDst, action);
    }

    private void sleepForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            LOG.trace("Interrupted while sleeping", e);
        }
    }

}
