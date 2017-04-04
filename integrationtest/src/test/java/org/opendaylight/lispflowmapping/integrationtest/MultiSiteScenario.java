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

import com.google.common.net.InetAddresses;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.lispflowmapping.integrationtest.MultiSiteScenarioUtil.Site;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains methods for:
 * - checking sites visibility (availability) - simulation of ping command
 * - for preparing and processing data structures necessary for creating map request, map register LISP messages.
 *
 */
class MultiSiteScenario {

    private final int DEFAULT_NETWORK_MASK = 24;
    private final int IP_MASK = 32;

    private final Integer TTL = 1440;
    private final String MAP_RECORD_A = "MAP_RECORD_1";

    private final Short DEFAULT_MULTICAST_PRIORITY = 255;
    private final Short DEFAULT_MULTICAST_WEIGHT = 0;
    private final Boolean DEFAULT_LOCAL_LOCATOR = true;
    private final Boolean DEFAULT_RLOC_PROBED = false;
    private final Boolean DEFAULT_ROUTED = true;
    private final byte[] DEFAULT_SITE_ID = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

    private final MappingAuthkey NULL_AUTH_KEY = new MappingAuthkeyBuilder().setKeyType(0).build();
    private final IMappingService mapService;
    private final IFlowMapping lms;

    private static final Logger LOG = LoggerFactory.getLogger(MultiSiteScenario.class);

    enum EidType {
        EID_SRC_DST, EID_WITH_PREFIX
    }

    MultiSiteScenario(final IMappingService mapService, final IFlowMapping lms) {
        this.mapService = mapService;
        this.lms = lms;
    }

    private Ipv4AddressBinary verifyIpv4Address(final LocatorRecord locatorRecord) {
        assertNotNull(locatorRecord);
        final Rloc rloc = locatorRecord.getRloc();
        assertNotNull(rloc);
        final Address address = rloc.getAddress();
        assertTrue(address instanceof Ipv4Binary);
        return ((Ipv4Binary) address).getIpv4Binary();
    }

    private List<LocatorRecord> verifyLocatorRecordExists(final MappingRecord mappingRecord) {
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

    private void emitMapRegisterMessage(final Site dstSite, final boolean merge) {
        final MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setXtrSiteIdPresent(true);
        mapRegisterBuilder.setXtrId(dstSite.getXtrId());
        mapRegisterBuilder.setSiteId(new SiteId(DEFAULT_SITE_ID));
        mapRegisterBuilder.setMergeEnabled(merge);
        final MappingRecordItemBuilder mappingRecordItemBuilder = new MappingRecordItemBuilder();
        mappingRecordItemBuilder.setMappingRecordItemId(MAP_RECORD_A);

        final MappingRecordBuilder mrb = prepareMappingRecord(EidType.EID_WITH_PREFIX, null, dstSite);
        mappingRecordItemBuilder.setMappingRecord(mrb.build());
        mapRegisterBuilder.setMappingRecordItem(Collections.singletonList(mappingRecordItemBuilder.build()));

        lms.handleMapRegister(mapRegisterBuilder.build());
    }

    void setCommonAuthentication() {
        Eid eid = LispAddressUtil.asIpv4PrefixBinaryEid("0.0.0.0/0", SITE_A.getVNI());
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);

        eid = LispAddressUtil.asIpv4PrefixBinaryEid("0.0.0.0/0", SITE_D5.getVNI());
        mapService.addAuthenticationKey(eid, NULL_AUTH_KEY);
        sleepForSeconds(1);
    }

    private Eid toEid(final String destSiteEidPrefix, final InstanceIdType vniValue, final int mask) {
        return LispAddressUtil.asIpv4PrefixBinaryEid(destSiteEidPrefix + "/" + mask, vniValue);

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
        mapRequestBuilder.setSmrInvoked(false);
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
        final Eid eidAsIpv4Prefix = LispAddressUtil.asIpv4PrefixBinaryEid(
                dstSite.getEidPrefix() + "/" + DEFAULT_NETWORK_MASK, dstSite.getVNI());

        final MappingRecordBuilder mrbNegative = prepareMappingRecord(EidType.EID_WITH_PREFIX, null, dstSite);
        mrbNegative.setEid(eidAsIpv4Prefix);
        mrbNegative.setAction(action);

        mapService.addMapping(MappingOrigin.Northbound, eidAsIpv4Prefix, new SiteId(DEFAULT_SITE_ID),
                new MappingData(MappingOrigin.Northbound, mrbNegative.build()));
    }

    void deleteNorthMappingNegative(final Site dstSite) {
        final Eid eidAsIpv4Prefix = LispAddressUtil.asIpv4PrefixBinaryEid(
                dstSite.getEidPrefix() + "/" + DEFAULT_NETWORK_MASK, dstSite.getVNI());

        mapService.removeMapping(MappingOrigin.Northbound, eidAsIpv4Prefix);
    }

    void storeNorthMappingSrcDst(final Site srcSite, final Site ... dstSite) {
        final MappingRecordBuilder mrb = prepareMappingRecord(EidType.EID_SRC_DST, srcSite,
                dstSite);
        mapService.addMapping(MappingOrigin.Northbound, mrb.getEid(), new SiteId(DEFAULT_SITE_ID),
                new MappingData(MappingOrigin.Northbound, mrb.build()));
    }

    void storeNorthMappingIpPrefix(final Site... dstSite) {
        final MappingRecordBuilder mrb = prepareMappingRecord(EidType.EID_WITH_PREFIX, null, dstSite);
        mapService.addMapping(MappingOrigin.Northbound, mrb.getEid(),  new SiteId(DEFAULT_SITE_ID),
                new MappingData(MappingOrigin.Northbound, mrb.build()));
    }

    private void storeDestinationSiteMappingViaSouthbound(final Site dstSite, final boolean merge) {
        emitMapRegisterMessage(dstSite, merge);
    }

    private MappingRecordBuilder prepareMappingRecordGeneral(final EidType eidType,
                                                             final Site srcSite, final Site dstSite) {
        final MappingRecordBuilder mrb = provideCommonMapRecordBuilder();
        mrb.setXtrId(dstSite.getXtrId());

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
        assertTrue(isEidEquals(dstSites));
        final MappingRecordBuilder mrb = prepareMappingRecordGeneral(eidType, srcSite, dstSites[0]);
        final List<LocatorRecord> locatorRecords = new ArrayList<>();
        if (!dstSites[0].isForDeletion) {
            for (Site dstSite : dstSites) {
                if (dstSite.getRloc() != null) {
                    locatorRecords.add(provideLocatorRecord(LispAddressUtil.asIpv4Rloc(dstSite.getRloc()),
                            dstSite.getRloc(), dstSite.getWeight(), dstSite.getPriority()));
                }
            }
        }
        mrb.setLocatorRecord(locatorRecords);

        return mrb;
    }

    private boolean isEidEquals(final Site... sites) {
        String commonEid = null;
        for (Site site : sites) {
            final String concreteEidPrefix = site.getEidPrefix();
            if (!concreteEidPrefix.equals(commonEid)) {
                if (commonEid == null) {
                    commonEid = concreteEidPrefix;
                } else {
                    return false;
                }
            }
        }
        return true;
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

    void deleteSouthboundMappings(final Site dstSite) {
        emitMapRegisterMessage(dstSite, false);

    }

    void storeSouthboundMappings(final boolean merge, final Site ... sites) {
        for (Site site : sites) {
            storeDestinationSiteMappingViaSouthbound(site, merge);
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

        final List<LocatorRecord> locatorRecords = verifyLocatorRecordExists(mappingRecord);
        for (Site expectedTargetSite : concatenateSites(dstSite, additionalSitesFromMapping)) {
            boolean expectedTargetFound = false;
            for (LocatorRecord locatorRecord : locatorRecords) {
                if (expectedTargetSite.getRloc().equals(rlocToString(locatorRecord))) {
                    final byte[] ipv4AddressSrcDst = verifyIpv4Address(locatorRecord).getValue();
                    final byte[] rloc = InetAddresses.forString((expectedTargetSite.getRloc())).getAddress();
                    final boolean isRlocSrcDstEqual = Arrays.equals(ipv4AddressSrcDst, rloc);
                    if (isPossibleAssertPingResultImmediately(expectedPingWorks, isRlocSrcDstEqual, "Unexpected RLOC." +
                            "Expected value " + rloc + ". Real value " + ipv4AddressSrcDst +
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
        return LispAddressStringifier.getString(locatorRecord.getRloc());
    }

    private Iterable<Site> concatenateSites(final Site dstSite, final Site... additionalSitesFromMapping) {
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
        if (checkActionAndRloc(dstSite, expectedPingWorks, mapReplyFromSrcToDst, additionalSitesFromMapping)) {
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


    private List<MapRequest> translateBuffersToMapRequest(byte[][] buffers) {
        final List<MapRequest> mapRequests = new ArrayList<>();
        for (byte[] buffer : buffers) {
            final MapRequest mapRequest = MapRequestSerializer.getInstance().deserialize(ByteBuffer.wrap(buffer), null);
            assertNotNull(mapRequest);
            mapRequests.add(mapRequest);
        }
        return mapRequests;
    }

    private Set<Eid> prepareExpectedEid(final String ... hosts) {
        final Set<Eid> eids = new HashSet<>();
        for (String host : hosts) {
            eids.add(LispAddressUtil.asIpv4PrefixBinaryEid(host + "/" + IP_MASK,
                    new InstanceIdType(MultiSiteScenarioUtil.VNI2)));
        }
        return eids;
    }

    private SourceEid prepareSourceEid(final String eidPrefix) {
        final SourceEidBuilder sourceEidBuilder = new SourceEidBuilder();
        final Eid eid = LispAddressUtil.asIpv4Eid(eidPrefix, MultiSiteScenarioUtil.VNI2);
        return sourceEidBuilder.setEid(eid).build();
    }

    void checkSMR(final SocketReader socketReader, final String site, final String ... hosts) {
        byte[][] buffers = socketReader.getBuffers(hosts.length);
        if (areBuffersEmpty(buffers)) {
            fail("No SMR received!");
        }
        List<MapRequest> mapRequests = translateBuffersToMapRequest(buffers);
        final Set<Eid> eids = prepareExpectedEid(hosts);
        final SourceEid expectedSourceEid = prepareSourceEid(site);
        for(MapRequest mapRequest : mapRequests) {
            assertTrue(mapRequest.isSmr());
            final SourceEid receivedSourceEid = mapRequest.getSourceEid();
            assertEquals(expectedSourceEid, receivedSourceEid);
            final List<EidItem> currentEidItems = mapRequest.getEidItem();
            assertNotNull(currentEidItems);
            assertTrue(SMRContainsExpectedEid(eids, currentEidItems));
        }
        //all expected eids should be after looping via mapRequests matched.
        assertTrue("Expected eids wasn't/weren't found " + eids, eids.isEmpty());
    }

    private boolean SMRContainsExpectedEid(Set<Eid> eids, List<EidItem> currentEidItems) {
        for (EidItem eidItem : currentEidItems) {
            //if eid from map request is matched then it is removed from set of expected eids
            if (!eids.remove(eidItem.getEid())) {
                 fail("SMR contained " + eidItem.getEid() + " which wasn't expected.");
            }
        }
        return true;
    }

    protected static boolean areBuffersEmpty(byte[][] buffers) {
        for (byte[] buffer : buffers) {
            for (byte b : buffer) {
                if (b != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
