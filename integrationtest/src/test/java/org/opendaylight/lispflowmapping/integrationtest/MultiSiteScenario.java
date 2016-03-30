/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.lispflowmapping.integrationtest.MappingServiceIntegrationTest.ourAddress;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_A;
import static org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.SITE_D5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
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

    private void verifyIpv4Address(final MapReply mapReply, final MappingRecord.Action action) {
        final MappingRecord mappingRecord = verifyMappingRecord(mapReply);
        assertEquals(action, mappingRecord.getAction());
    }

    private Ipv4Address verifyIpv4Address(final LocatorRecord locatorRecord) {
        assertNotNull(locatorRecord);
        final Rloc rloc = locatorRecord.getRloc();
        assertNotNull(rloc);
        final Address address = rloc.getAddress();
        assertTrue(address instanceof Ipv4);
        return ((Ipv4) address).getIpv4();
    }

    private List<LocatorRecord> verifyLocatorRecord(final MappingRecord mappingRecord) {
        final List<LocatorRecord> locatorRecords = mappingRecord.getLocatorRecord();
        assertNotNull(locatorRecords);
        return locatorRecords;
    }

    private MappingRecord verifyMappingRecord(MapReply mapReply) {
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

        final MappingRecordBuilder mrb = prepareMappingRecord(EidType.EID_WITH_PREFIX, null, dstSite);
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

        final MappingRecordBuilder mrbNegative = prepareMappingRecord(EidType.EID_WITH_PREFIX, null, dstSite);
        mrbNegative.setEid(eidAsIpv4Prefix);
        mrbNegative.setAction(action);

        mapService.addMapping(MappingOrigin.Northbound, eidAsIpv4Prefix, dstSite.getSiteId(), mrbNegative.build());
    }

    void deleteNorthMappingNegative(final Site dstSite) {
        final Ipv4Prefix ipv4Prefix = new Ipv4Prefix(dstSite.getEidPrefix() + "/" + DEFAULT_NETWORK_MASK);
        final Eid eidAsIpv4Prefix = LispAddressUtil.toEid(ipv4Prefix, dstSite.getVNI());

        mapService.removeMapping(MappingOrigin.Northbound, eidAsIpv4Prefix);
    }

    void storeNorthMappingSrcDst(final Site srcSite, final Site ... dstSite) {
        final MappingRecordBuilder mrb = prepareMappingRecord(EidType.EID_SRC_DST, srcSite,
                dstSite);
        mapService.addMapping(MappingOrigin.Northbound, mrb.getEid(), srcSite.getSiteId(), mrb.build());
    }

    void storeNorthMappingIpPrefix(boolean defaultWeightPriority, final Site ... dstSite) {
        final MappingRecordBuilder mrb = prepareMappingRecord(EidType.EID_WITH_PREFIX, null, dstSite);
        mapService.addMapping(MappingOrigin.Northbound, mrb.getEid(), dstSite[0].getSiteId(), mrb.build());
    }

    private void storeDestinationSiteMappingViaSouthbound(final Site dstSite) {
        emitMapRegisterMessage(dstSite);
    }

    private MappingRecordBuilder prepareMappingRecordGeneral(final EidType eidType,
                                                             final Site srcSite, final Site dstSite) {
        final MappingRecordBuilder mrb = provideCommonMapRecordBuilder();
        mrb.setXtrId(new XtrId(ArrayUtils.addAll(dstSite.getSiteId().getValue(), dstSite.getSiteId().getValue())));

        Eid eid = null;
        if (EidType.EID_SRC_DST.equals(eidType)) {
            if (srcSite != null && dstSite != null && srcSite.getEidPrefix() != null && dstSite.getEidPrefix() !=
                    null) {
                eid = LispAddressUtil.asSrcDstEid(srcSite.getEidPrefix(), dstSite.getEidPrefix(), DEFAULT_NETWORK_MASK,
                        DEFAULT_NETWORK_MASK, dstSite.getVNI().getValue().intValue());
            }
        }

        mrb.setEid(eid == null ? toEid(dstSite.getEidPrefix(), dstSite.getVNI(), DEFAULT_NETWORK_MASK) : eid);
        return mrb;
    }

    private MappingRecordBuilder prepareMappingRecord(final EidType eidType, final Site srcSite, final Site...
            dstSites) {
        final MappingRecordBuilder mrb = prepareMappingRecordGeneral(eidType, srcSite, dstSites[0]);
        final List<LocatorRecord> locatorRecords = new ArrayList<>();
        for (Site dstSite : dstSites) {
            if (dstSite.getRloc() != null) {
                locatorRecords.add(provideLocatorRecord(LispAddressUtil.toRloc(new Ipv4Address(dstSite.getRloc())),
                        dstSite.getRloc(), dstSite.getWeight(), dstSite.getPriority()));
            }
        }
        mrb.setLocatorRecord(locatorRecords);

        return mrb;
    }

    private LocatorRecord provideLocatorRecord(final Rloc rloc, final String rlocStr, final short weight, final short
            priority) {
        final LocatorRecordBuilder locatorRecordBuilder = new LocatorRecordBuilder();
        locatorRecordBuilder.setRloc(rloc);
        locatorRecordBuilder.setLocatorId(rlocStr);
        locatorRecordBuilder.setPriority(priority);
        locatorRecordBuilder.setWeight(weight);
        locatorRecordBuilder.setMulticastPriority(DEFAULT_MULTICAST_PRIORITY);
        locatorRecordBuilder.setMulticastWeight(DEFAULT_MULTICAST_WEIGHT);
        locatorRecordBuilder.setLocalLocator(DEFAULT_LOCAL_LOCATOR);
        locatorRecordBuilder.setRlocProbed(DEFAULT_RLOC_PROBED);
        locatorRecordBuilder.setRouted(DEFAULT_ROUTED);
        return locatorRecordBuilder.build();
    }

    private MappingRecordBuilder provideCommonMapRecordBuilder() {
        final MappingRecordBuilder mappingRecordBuilder = new MappingRecordBuilder();
        mappingRecordBuilder.setRecordTtl(TTL);
        mappingRecordBuilder.setAction(MappingRecord.Action.NoAction);
        mappingRecordBuilder.setAuthoritative(true);
        mappingRecordBuilder.setTimestamp(System.currentTimeMillis());
        return mappingRecordBuilder;
    }

    void deleteNorthMapingSrcDst(final Site srcSite, final Site dstSite) {
        final Eid eid = LispAddressUtil.asSrcDstEid(srcSite.getEidPrefix(), dstSite.getEidPrefix(),
                DEFAULT_NETWORK_MASK, DEFAULT_NETWORK_MASK, dstSite.getVNI().getValue().intValue());
        mapService.removeMapping(MappingOrigin.Northbound, eid);
    }

    void storeSouthboundMappings(final Site ... sites) {
        for (Site site : sites) {
            storeDestinationSiteMappingViaSouthbound(site);
        }
    }

    boolean isPossibleAssertPingResultImmediately(final boolean expectedPingWorks, final boolean isPartialyWorking,
                                                  final String  msg) {
        //ping fail is unwanted. ping definitely failed
        if (expectedPingWorks && !isPartialyWorking) {
            fail(msg);
        }

        //ping fail is wanted. still can fail later
        if (!expectedPingWorks && isPartialyWorking) {
            return false;
        }

        //ping fail is unwanted. still can fail later
        if (expectedPingWorks && isPartialyWorking) {
            return false;
        }

        //ping fail is wanted. ping definitely failed.
        if (!expectedPingWorks && !isPartialyWorking) {
            return true;
        }
        return false;
    }

    boolean checkActionAndRloc(final Site dstSite, boolean expectedPingWorks, MapReply mapReplyFromSrcToDst, final
                                Site  ... additionalSitesFromMapping) {
        final MappingRecord mappingRecord = verifyMappingRecord(mapReplyFromSrcToDst);
        final boolean isNotDroppendSrcDst = !MappingRecord.Action.Drop.equals(mappingRecord.getAction());

        if (isPossibleAssertPingResultImmediately(expectedPingWorks, isNotDroppendSrcDst, "Drop action has appeared " +
                "during ping")) {
            return true;
        }

        final List<LocatorRecord> locatorRecords = verifyLocatorRecord(mappingRecord);
        for (Site expectedTargetSite : concateSites(dstSite, additionalSitesFromMapping)) {
            boolean expectedTargetFound = false;
            for (LocatorRecord locatorRecord : locatorRecords) {
                if (expectedTargetSite.getRloc().equals(rlocToString(locatorRecord))) {
                    final Ipv4Address ipv4AddressSrcDst = verifyIpv4Address(locatorRecord);
                    final boolean isRlocSrcDstEqual = ipv4AddressSrcDst.getValue().equals(expectedTargetSite.getRloc());
                    if (isPossibleAssertPingResultImmediately(expectedPingWorks, isRlocSrcDstEqual, "Unexpected RLOC." +
                            "Expected value " + dstSite.getRloc() + ". Real value " + ipv4AddressSrcDst.getValue() +
                            ".")) {
                        return true;
                    }

                    final boolean isWeightEquals = expectedTargetSite.getWeight() == locatorRecord.getWeight();
                    if (isPossibleAssertPingResultImmediately(expectedPingWorks, isWeightEquals, "Weight isn't equal." +
                            "Expected value " + expectedTargetSite.getWeight() + ". Value from mapping" +
                            locatorRecord.getWeight() + ".")) {
                        return true;
                    }

                    final boolean isPriorityEquals = expectedTargetSite.getPriority() == locatorRecord.getPriority();
                    if (isPossibleAssertPingResultImmediately(expectedPingWorks, isPriorityEquals, "Priority isn't " +
                            "equal. Expected value " + expectedTargetSite.getPriority() + ". Value from mapping" +
                            locatorRecord.getPriority() + ".")) {
                        return true;
                    }

                    expectedTargetFound = true;
                    break;
                }
            }
            if (isPossibleAssertPingResultImmediately(expectedPingWorks, expectedTargetFound, "Mapping for " +
                expectedTargetSite.getRloc() + " was expected but wasn't returned from mapping service." +
                expectedTargetFound)) {
                return true;
            }

        }

        return false;
    }

    private String rlocToString(final LocatorRecord locatorRecord) {
        final Address address = locatorRecord.getRloc().getAddress();
        if (address instanceof Ipv4) {
            Ipv4Address ipv4 = ((Ipv4) address).getIpv4();
            return ipv4.getValue();
        }
        return null;
    }

    private Iterable<Site> concateSites(final Site dstSite, final Site... additionalSitesFromMapping) {
        final List<Site> sites = new ArrayList<>();
        sites.add(dstSite);
        for (Site additionalSite : additionalSitesFromMapping) {
            sites.add(additionalSite);
        }
        return sites;
    }

    private void assertPing(final Site srcSite, final int srcHostIndex, final Site dstSite, final int dstHostIndex,
                         boolean expectedPingWorks, final Site ... additionalSitesFromMapping) {
        final MapReply mapReplyFromSrcToDst = emitMapRequestMessage(srcSite.getHost(srcHostIndex), dstSite.getHost
                (dstHostIndex), dstSite.getVNI());
        if (checkActionAndRloc(dstSite, expectedPingWorks, mapReplyFromSrcToDst,
                additionalSitesFromMapping)) {
            return;
        }

        final MapReply mapReplyFromDstToSrc = emitMapRequestMessage(dstSite.getHost(dstHostIndex), srcSite.getHost
                (srcHostIndex), srcSite.getVNI());
        if (checkActionAndRloc(srcSite, expectedPingWorks, mapReplyFromDstToSrc)) {
            return;
        }

        final InstanceIdType iidDst = mapReplyFromSrcToDst.getMappingRecordItem().get(0).getMappingRecord().getEid().
                getVirtualNetworkId();
        final InstanceIdType iidSrc = mapReplyFromDstToSrc.getMappingRecordItem().get(0).getMappingRecord().getEid().
                getVirtualNetworkId();

        final boolean isIIDEqual = iidDst.equals(iidSrc);

        if (expectedPingWorks != isIIDEqual) {
            fail("IID problem. Dst value " + iidDst.getValue() + ". Src value " + iidSrc.getValue() + ".");
        }
    }

    void assertPingWorks(final Site srcSite, final int srcHostIndex, final Site dstSite, final int dstHostIndex,
                         final Site ... additionalSitesFromMapping) {
        assertPing(srcSite, srcHostIndex, dstSite, dstHostIndex, true, additionalSitesFromMapping);
    }

    void assertPingFails(final Site srcSite, final int srcHostIndex, final Site dstSite, final int dstHostIndex) {
        assertPing(srcSite, srcHostIndex, dstSite, dstHostIndex, false);
    }

    private void sleepForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            LOG.trace("Interrupted while sleeping", e);
        }
    }

}
