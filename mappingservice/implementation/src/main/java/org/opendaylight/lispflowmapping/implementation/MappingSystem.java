/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.timebucket.implementation.TimeBucketMappingTimeoutService;
import org.opendaylight.lispflowmapping.implementation.timebucket.interfaces.ISouthBoundMappingTimeoutService;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.LoggingUtil;
import org.opendaylight.lispflowmapping.implementation.util.MSNotificationInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.MappingMergeUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IAuthKeyDb;
import org.opendaylight.lispflowmapping.interfaces.mapcache.ILispMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.lispflowmapping.mapcache.AuthKeyDb;
import org.opendaylight.lispflowmapping.mapcache.MultiTableMapCache;
import org.opendaylight.lispflowmapping.mapcache.SimpleMapCache;
import org.opendaylight.lispflowmapping.mapcache.lisp.LispMapCacheStringifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Mapping System coordinates caching of md-sal stored mappings and if so configured enables longest prefix match
 * mapping lookups.
 *
 * @author Florin Coras
 * @author Lorand Jakab
 *
 */
public class MappingSystem implements IMappingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(MappingSystem.class);
    private static final String AUTH_KEY_TABLE = "authentication";
    //private static final int TTL_RLOC_TIMED_OUT = 1;
    private static final int TTL_NO_RLOC_KNOWN = ConfigIni.getInstance().getNegativeMappingTTL();
    private NotificationPublishService notificationPublishService;
    private boolean mappingMerge;
    private ILispDAO dao;
    private ILispDAO sdao;
    private ILispMapCache smc;
    private IMapCache pmc;
    private ConcurrentHashMap<Eid, Set<Subscriber>> subscriberdb = new ConcurrentHashMap<>();
    private IAuthKeyDb akdb;
    private final EnumMap<MappingOrigin, IMapCache> tableMap = new EnumMap<>(MappingOrigin.class);
    private DataStoreBackEnd dsbe;
    private boolean isMaster = false;

    private ISouthBoundMappingTimeoutService sbMappingTimeoutService;

    public MappingSystem(ILispDAO dao, boolean iterateMask, NotificationPublishService nps, boolean mappingMerge) {
        this.dao = dao;
        this.notificationPublishService = nps;
        this.mappingMerge = mappingMerge;
        buildMapCaches();

        sbMappingTimeoutService = new TimeBucketMappingTimeoutService(ConfigIni.getInstance()
                .getNumberOfBucketsInTimeBucketWheel(), ConfigIni.getInstance().getRegistrationValiditySb(),
                this);
    }

    public void setDataStoreBackEnd(DataStoreBackEnd dataStoreBackEnd) {
        this.dsbe = dataStoreBackEnd;
    }

    @Override
    public void setMappingMerge(boolean mappingMerge) {
        this.mappingMerge = mappingMerge;
    }

    @Override
    public void setIterateMask(boolean iterate) {
        LOG.error("Non-longest prefix match lookups are not properly supported, variable is set to true");
    }

    public void initialize() {
        restoreDaoFromDatastore();
    }

    private void buildMapCaches() {
        /*
         * There exists a direct relationship between MappingOrigins and the tables that are part of the MappingSystem.
         * Therefore, if a new origin is added, probably a new table should be instantiated here as well. Here we
         * instantiate a SimpleMapCache for southbound originated LISP mappings and a MultiTableMapCache for northbound
         * originated mappings. Use of FlatMapCache would be possible when no longest prefix match is needed at all,
         * but that option is no longer supported in the code, since it was never tested and may lead to unexpected
         * results.
         */
        sdao = dao.putTable(MappingOrigin.Southbound.toString());
        pmc = new MultiTableMapCache(dao.putTable(MappingOrigin.Northbound.toString()));
        smc = new SimpleMapCache(sdao);
        akdb = new AuthKeyDb(dao.putTable(AUTH_KEY_TABLE));
        tableMap.put(MappingOrigin.Northbound, pmc);
        tableMap.put(MappingOrigin.Southbound, smc);
    }

    @Override
    public void updateMapping(MappingOrigin origin, Eid key, MappingData mappingData) {
        addMapping(origin, key, mappingData, MappingChange.Updated);
    }

    @Override
    public void addMapping(MappingOrigin origin, Eid key, MappingData mappingData) {
        addMapping(origin, key, mappingData, MappingChange.Created);
    }

    private void addMapping(MappingOrigin origin, Eid key, MappingData mappingData, MappingChange changeType) {

        sbMappingTimeoutService.removeExpiredMappings();

        if (mappingData == null) {
            LOG.warn("addMapping() called with null mapping, ignoring");
            return;
        }

        if (origin == MappingOrigin.Southbound) {
            XtrId xtrId = mappingData.getXtrId();
            if (xtrId == null && mappingMerge && mappingData.isMergeEnabled()) {
                LOG.warn("addMapping() called will null xTR-ID in MappingRecord, while merge is set, ignoring");
                return;
            }
            if (xtrId != null && mappingMerge) {
                if (mappingData.isMergeEnabled()) {
                    smc.addMapping(key, xtrId, mappingData);
                    handleMergedMapping(key);
                    return;
                } else {
                    clearPresentXtrIdMappings(key);
                    smc.addMapping(key, xtrId, mappingData);
                }
            }
            addOrRefreshMappingInTimeoutService(key, mappingData);
        }

        tableMap.get(origin).addMapping(key, mappingData);

        if (origin != MappingOrigin.Southbound) {
            // If a NB mapping is added, we need to check if it's covering negative mappings in SB, and remove those
            handleSbNegativeMappings(key);
            // Notifications for SB changes are sent in the SB code itself, so this is called for non-SB additions only
            notifyChange(key, mappingData.getRecord(), changeType);
        }
    }

    @SuppressWarnings("unchecked")
    private void clearPresentXtrIdMappings(Eid key) {
        List<MappingData> allXtrMappingList = (List<MappingData>) (List<?>) smc.getAllXtrIdMappings(key);

        if (((MappingData) smc.getMapping(key, (XtrId) null)).isMergeEnabled()) {
            LOG.trace("Different xTRs have different merge configuration!");
        }

        for (MappingData mappingData : allXtrMappingList) {
            removeSbXtrIdSpecificMapping(key, mappingData.getXtrId(), mappingData);
        }
    }

    private void addOrRefreshMappingInTimeoutService(Eid key, MappingData mappingData) {
        Integer oldBucketId = (Integer) smc.getData(key, SubKeys.TIME_BUCKET_ID);
        Integer updatedBucketId;

        if (oldBucketId != null) {
            //refresh mapping
            updatedBucketId = sbMappingTimeoutService.refreshMapping(key, mappingData, oldBucketId);
        } else {
            updatedBucketId = sbMappingTimeoutService.addMapping(key, mappingData);
        }

        smc.addData(key, SubKeys.TIME_BUCKET_ID, updatedBucketId);
    }

    private void handleSbNegativeMappings(Eid key) {
        Set<Eid> childPrefixes = getSubtree(MappingOrigin.Southbound, key);

        // We don't want to remove a newly added negative mapping, so remove it from the child set
        childPrefixes.remove(key);

        LOG.trace("handleSbNegativeMappings(): subtree prefix set for EID {} (excluding the EID itself): {}",
                LispAddressStringifier.getString(key),
                LispAddressStringifier.getString(childPrefixes));
        if (childPrefixes == null || childPrefixes.isEmpty()) {
            // The assumption here is that negative prefixes are well maintained and never overlapping.
            // If we have children for the EID, no parent lookup should thus be necessary.
            Eid parentPrefix = smc.getCoveringLessSpecific(key);
            LOG.trace("handleSbNegativeMappings(): parent prefix for EID {}: {}",
                    LispAddressStringifier.getString(key),
                    LispAddressStringifier.getString(parentPrefix));
            handleSbNegativeMapping(parentPrefix);
            return;
        }

        for (Eid prefix : childPrefixes) {
            handleSbNegativeMapping(prefix);
        }
    }

    private void handleSbNegativeMapping(Eid key) {
        MappingData mappingData = getSbMappingWithExpiration(null, key, null);
        if (mappingData != null && mappingData.isNegative().or(false)) {
            removeSbMapping(mappingData.getRecord().getEid(), mappingData);
        }
    }

    @Override
    public MappingData addNegativeMapping(Eid key) {
        MappingRecord mapping = buildNegativeMapping(key);
        MappingData mappingData = new MappingData(mapping);
        LOG.debug("Adding negative mapping for EID {}", LispAddressStringifier.getString(mapping.getEid()));
        LOG.trace(mappingData.getString());
        smc.addMapping(mapping.getEid(), mappingData);
        dsbe.addMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, mapping.getEid(), null, mappingData));
        return mappingData;
    }

    private MappingRecord buildNegativeMapping(Eid eid) {
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder();
        recordBuilder.setAuthoritative(false);
        recordBuilder.setMapVersion((short) 0);
        recordBuilder.setEid(eid);
        if (eid.getAddressType().equals(Ipv4PrefixBinaryAfi.class)
                || eid.getAddressType().equals(Ipv6PrefixBinaryAfi.class)) {
            Eid widestNegativePrefix = getWidestNegativePrefix(eid);
            if (widestNegativePrefix != null) {
                recordBuilder.setEid(widestNegativePrefix);
            }
        }
        recordBuilder.setAction(LispMessage.NEGATIVE_MAPPING_ACTION);
        //if (getAuthenticationKey(eid) != null) {
        //    recordBuilder.setRecordTtl(TTL_RLOC_TIMED_OUT);
        //} else {
        recordBuilder.setRecordTtl(TTL_NO_RLOC_KNOWN);
        //}
        return recordBuilder.build();
    }

    /*
     * Since this method is only called when there is a hit in the southbound Map-Register cache, and that cache is
     * not used when merge is on, it's OK to ignore the effects of timestamp changes on merging for now.
     */
    public void refreshMappingRegistration(Eid key, XtrId xtrId, Long timestamp) {

        sbMappingTimeoutService.removeExpiredMappings();

        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        MappingData mappingData = (MappingData) smc.getMapping(null, key);
        if (mappingData != null) {
            mappingData.setTimestamp(new Date(timestamp));
            addOrRefreshMappingInTimeoutService(key, mappingData);
        } else {
            LOG.warn("Could not update timestamp for EID {}, no mapping found", LispAddressStringifier.getString(key));
        }
        if (mappingMerge && xtrId != null) {
            MappingData xtrIdMappingData = (MappingData) smc.getMapping(key, xtrId);
            if (xtrIdMappingData != null) {
                xtrIdMappingData.setTimestamp(new Date(timestamp));
            } else {
                LOG.warn("Could not update timestamp for EID {} xTR-ID {}, no mapping found",
                        LispAddressStringifier.getString(key), LispAddressStringifier.getString(xtrId));
            }
        }
    }

    private MappingData updateServicePathMappingRecord(MappingData mappingData, Eid eid) {
        // keep properties of original record
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder(mappingData.getRecord());
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());

        // there should only be one locator record
        if (mappingData.getRecord().getLocatorRecord().size() != 1) {
            LOG.warn("MappingRecord associated to ServicePath EID has more than one locator!");
            return mappingData;
        }

        LocatorRecord locatorRecord = mappingData.getRecord().getLocatorRecord().get(0);
        long serviceIndex = ((ServicePath) eid.getAddress()).getServicePath().getServiceIndex();
        int index = LispAddressUtil.STARTING_SERVICE_INDEX - (int) serviceIndex;
        Rloc rloc = locatorRecord.getRloc();
        if (rloc.getAddress() instanceof Ipv4 || rloc.getAddress() instanceof Ipv6) {
            if (index != 0) {
                LOG.warn("Service Index should be 255 for simple IP RLOCs!");
            }
            return mappingData;
        } else if (rloc.getAddress() instanceof ExplicitLocatorPath) {
            ExplicitLocatorPath elp = (ExplicitLocatorPath) rloc.getAddress();
            List<Hop> hops = elp.getExplicitLocatorPath().getHop();

            if (index < 0 || index > hops.size())  {
                LOG.warn("Service Index out of bounds!");
                return mappingData;
            }

            SimpleAddress nextHop = hops.get(index).getAddress();
            LocatorRecordBuilder lrb = new LocatorRecordBuilder(locatorRecord);
            lrb.setRloc(LispAddressUtil.toRloc(nextHop));
            recordBuilder.getLocatorRecord().add(lrb.build());
            return new MappingData(recordBuilder.build());
        } else {
            LOG.warn("Nothing to do with ServicePath mapping record");
            return mappingData;
        }
    }

    private MappingData handleMergedMapping(Eid key) {
        List<MappingData> expiredMappingDataList = new ArrayList<>();
        Set<IpAddressBinary> sourceRlocs = new HashSet<>();

        MappingData mergedMappingData = MappingMergeUtil.mergeXtrIdMappings(smc.getAllXtrIdMappings(key),
                expiredMappingDataList, sourceRlocs);

        for (MappingData mappingData : expiredMappingDataList) {
            removeSbXtrIdSpecificMapping(key, mappingData.getXtrId(), mappingData);
        }

        if (mergedMappingData != null) {
            smc.addMapping(key, mergedMappingData, sourceRlocs);
            dsbe.addMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, key, mergedMappingData));
            addOrRefreshMappingInTimeoutService(key, mergedMappingData);
        } else {
            removeSbMapping(key, mergedMappingData);
        }
        return mergedMappingData;
    }

    @Override
    public MappingData getMapping(Eid src, Eid dst) {
        // NOTE: Currently we have two lookup algorithms implemented, which are configurable

        if (ConfigIni.getInstance().getLookupPolicy() == IMappingService.LookupPolicy.NB_AND_SB) {
            return getMappingNbSbIntersection(src, dst);
        } else {
            return getMappingNbFirst(src, dst);
        }
    }

    @Override
    public MappingData getMapping(Eid dst) {
        return getMapping((Eid) null, dst);
    }

    @Override
    public MappingData getMapping(Eid src, Eid dst, XtrId xtrId) {
        // Note: If xtrId is null, we need to go through regular policy checking else Policy doesn't matter

        if (xtrId == null) {
            return getMapping(src, dst);
        }

        return getSbMappingWithExpiration(src, dst, xtrId);
    }

    @Override
    public MappingData getMapping(MappingOrigin origin, Eid key) {
        if (origin.equals(MappingOrigin.Southbound)) {
            return getSbMappingWithExpiration(null, key, null);
        }
        return (MappingData) tableMap.get(origin).getMapping(null, key);
    }

    private MappingData getMappingNbFirst(Eid src, Eid dst) {

        // Default lookup policy is northboundFirst
        //lookupPolicy == NB_FIRST

        MappingData nbMappingData = (MappingData) pmc.getMapping(src, dst);

        if (nbMappingData == null) {
            return getSbMappingWithExpiration(src, dst, null);
        }
        if (dst.getAddress() instanceof ServicePath) {
            return updateServicePathMappingRecord(nbMappingData, dst);
        }
        return nbMappingData;
    }

    private MappingData getMappingNbSbIntersection(Eid src, Eid dst) {
        //lookupPolicy == NB_AND_SB, we return intersection
        //of NB and SB mappings, or NB mapping if intersection is empty.

        MappingData nbMappingData = (MappingData) pmc.getMapping(src, dst);
        if (nbMappingData == null) {
            return nbMappingData;
        }
        // no intersection for Service Path mappings
        if (dst.getAddress() instanceof ServicePath) {
            return updateServicePathMappingRecord(nbMappingData, dst);
        }
        MappingData sbMappingData = getSbMappingWithExpiration(src, dst, null);
        if (sbMappingData == null) {
            return nbMappingData;
        }
        // both NB and SB mappings exist. Compute intersection of the mappings
        return MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData);
    }

    private MappingData getSbMappingWithExpiration(Eid src, Eid dst, XtrId xtrId) {
        MappingData mappingData = (MappingData) smc.getMapping(dst, xtrId);
        if (mappingData != null && MappingMergeUtil.mappingIsExpired(mappingData)) {
            return handleSbExpiredMapping(dst, xtrId, mappingData);
        } else {
            return mappingData;
        }
    }

    public MappingData handleSbExpiredMapping(Eid key, XtrId xtrId, MappingData mappingData) {
        if (mappingMerge && mappingData.isMergeEnabled()) {
            return handleMergedMapping(key);
        }

        if (xtrId != null) {
            removeSbXtrIdSpecificMapping(key, xtrId, mappingData);
        } else {
            removeSbMapping(key, mappingData);
        }
        return null;
    }

    private void removeSbXtrIdSpecificMapping(Eid key, XtrId xtrId, MappingData mappingData) {
        smc.removeMapping(key, xtrId);
        dsbe.removeXtrIdMapping(DSBEInputUtil.toXtrIdMapping(mappingData));
    }

    private void removeSbMapping(Eid key, MappingData mappingData) {
        if (mappingData != null && mappingData.getXtrId() != null) {
            removeSbXtrIdSpecificMapping(key, mappingData.getXtrId(), mappingData);
        }

        removeFromSbTimeoutService(key);
        Set<Subscriber> subscribers = getSubscribers(key);
        smc.removeMapping(key);
        dsbe.removeMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, key, mappingData));
        publishNotification(mappingData.getRecord(), null, subscribers, null, MappingChange.Removed);
        removeSubscribersConditionally(MappingOrigin.Southbound, key);
    }

    private void removeFromSbTimeoutService(Eid key) {
        Integer bucketId = (Integer) smc.getData(key, SubKeys.TIME_BUCKET_ID);
        if (bucketId != null) {
            sbMappingTimeoutService.removeMappingFromTimeoutService(key, bucketId);
        }
    }

    @Override
    public Eid getWidestNegativePrefix(Eid key) {
        if (!MaskUtil.isMaskable(key.getAddress())) {
            LOG.warn("Widest negative prefix only makes sense for maskable addresses!");
            return null;
        }

        // We assume that ILispMapCache#getWidestNegativeMapping() returns null for positive mappings, and 0/0
        // for empty cache.
        Eid nbPrefix = pmc.getWidestNegativeMapping(key);
        if (nbPrefix == null) {
            LOG.trace("getWidestNegativePrefix NB: positive mapping, returning null");
            return null;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("getWidestNegativePrefix NB: {}", LispAddressStringifier.getString(nbPrefix));
        }

        Eid sbPrefix = smc.getWidestNegativeMapping(key);
        if (sbPrefix == null) {
            LOG.trace("getWidestNegativePrefix SB: positive mapping, returning null");
            return null;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("getWidestNegativePrefix SB: {}", LispAddressStringifier.getString(sbPrefix));
        }

        // since prefixes overlap, just return the more specific (larger mask)
        if (LispAddressUtil.getIpPrefixMask(nbPrefix) < LispAddressUtil.getIpPrefixMask(sbPrefix)) {
            return sbPrefix;
        } else {
            return nbPrefix;
        }
    }

    @Override
    public Set<Eid> getSubtree(MappingOrigin origin, Eid key) {
        if (!MaskUtil.isMaskable(key.getAddress())) {
            LOG.warn("Child prefixes only make sense for maskable addresses!");
            return Collections.emptySet();
        }

        return tableMap.get(origin).getSubtree(key);
    }

    @Override
    public void removeMapping(MappingOrigin origin, Eid key) {
        Eid dstAddr = null;
        Set<Subscriber> subscribers = null;
        Set<Subscriber> dstSubscribers = null;
        MappingData mapping = (MappingData) tableMap.get(origin).getMapping(null, key);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing mapping for EID {} from {}",
                    LispAddressStringifier.getString(key), origin);
        }
        if (LOG.isTraceEnabled() && mapping != null) {
            LOG.trace(mapping.getString());
        }

        MappingRecord notificationMapping = null;

        if (mapping != null) {
            notificationMapping = mapping.getRecord();
            subscribers = getSubscribers(key);
            // For SrcDst LCAF also send SMRs to Dst prefix
            if (key.getAddress() instanceof SourceDestKey) {
                dstAddr = SourceDestKeyHelper.getDstBinary(key);
                dstSubscribers = getSubscribers(dstAddr);
            }
        }

        removeSubscribersConditionally(origin, key);

        if (origin == MappingOrigin.Southbound) {
            removeFromSbTimeoutService(key);
        }

        if (origin == MappingOrigin.Southbound && mapping != null && mapping.isPositive().or(false)) {
            mergeNegativePrefixes(key);
        } else {
            // mergeNegativePrefixes() above removes the mapping, so addNegativeMapping() will work correctly
            tableMap.get(origin).removeMapping(key);
        }

        if (notificationMapping != null) {
            publishNotification(notificationMapping, key, subscribers, dstSubscribers, MappingChange.Removed);
            notifyChildren(key, notificationMapping, MappingChange.Removed);
            if (dstAddr != null) {
                notifyChildren(dstAddr, notificationMapping, MappingChange.Removed);
            }
        }
    }

    public void notifyChange(Eid eid, MappingRecord mapping, MappingChange mappingChange) {
        Set<Subscriber> subscribers = getSubscribers(eid);

        Set<Subscriber> dstSubscribers = null;
        // For SrcDst LCAF also send SMRs to Dst prefix
        if (eid.getAddress() instanceof SourceDestKey) {
            Eid dstAddr = SourceDestKeyHelper.getDstBinary(eid);
            dstSubscribers = getSubscribers(dstAddr);
            notifyChildren(dstAddr, mapping, mappingChange);
        }
        publishNotification(mapping, eid, subscribers, dstSubscribers, mappingChange);

        notifyChildren(eid, mapping, mappingChange);
    }

    private void notifyChildren(Eid eid, MappingRecord mapping, MappingChange mappingChange) {
        // Update/created only happens for NB mappings. We assume no overlapping prefix support for NB mappings - so
        // this NB mapping should not have any children. Both for NB first and NB&SB cases all subscribers for SB
        // prefixes that are equal or more specific to this NB prefix have to be notified of the potential change.
        // Each subscriber is notified for the prefix that it is currently subscribed to, and MS should return to them
        // a Map-Reply with the same prefix and update mapping in cases of EID_INTERSECTION_RLOC_NB_FIRST which is a
        // new option we are creating TODO
        Set<Eid> childPrefixes = getSubtree(MappingOrigin.Southbound, eid);
        if (childPrefixes == null || childPrefixes.isEmpty()) {
            return;
        }

        childPrefixes.remove(eid);
        for (Eid prefix : childPrefixes) {
            Set<Subscriber> subscribers = getSubscribers(prefix);
            publishNotification(mapping, prefix, subscribers, null, mappingChange);
        }
    }

    private void publishNotification(MappingRecord mapping, Eid eid, Set<Subscriber> subscribers,
                                     Set<Subscriber> dstSubscribers, MappingChange mappingChange) {
        try {
            // The notifications are used for sending SMR.
            notificationPublishService.putNotification(MSNotificationInputUtil.toMappingChanged(
                    mapping, eid, subscribers, dstSubscribers, mappingChange));
        } catch (InterruptedException e) {
            LOG.warn("Notification publication interrupted!");
        }
    }

    /*
     * Merges adjacent negative prefixes and notifies their subscribers.
     */
    private void mergeNegativePrefixes(Eid eid) {
        LOG.debug("Merging negative prefixes starting from EID {}", LispAddressStringifier.getString(eid));

        // If we delete nodes while we walk up the radix trie the algorithm will give incorrect results, because
        // removals rearrange relationships in the trie. So we save prefixes to be removed into a HashMap.
        Map<Eid, MappingData> mergedMappings = new HashMap<>();

        Eid currentNode = smc.getSiblingPrefix(eid);
        MappingData mapping = (MappingData) smc.getMapping(null, currentNode);
        if (mapping != null && mapping.isNegative().or(false)) {
            mergedMappings.put(currentNode, mapping);
        } else {
            return;
        }

        Eid previousNode = currentNode;
        currentNode = smc.getVirtualParentSiblingPrefix(currentNode);
        while (currentNode != null) {
            mapping = (MappingData) smc.getMapping(null, currentNode);
            if (mapping != null && mapping.isNegative().or(false)) {
                mergedMappings.put(currentNode, mapping);
            } else {
                break;
            }
            previousNode = currentNode;
            currentNode = smc.getVirtualParentSiblingPrefix(previousNode);
        }

        for (Eid key : mergedMappings.keySet()) {
            removeSbMapping(key, mergedMappings.get(key));
        }
        smc.removeMapping(eid);

        addNegativeMapping(getVirtualParent(previousNode));
    }

    private static Eid getVirtualParent(Eid eid) {
        if (eid.getAddress() instanceof Ipv4PrefixBinary) {
            Ipv4PrefixBinary prefix = (Ipv4PrefixBinary) eid.getAddress();
            short parentPrefixLength = (short) (prefix.getIpv4MaskLength() - 1);
            byte[] parentPrefix = MaskUtil.normalizeByteArray(prefix.getIpv4AddressBinary().getValue(),
                    parentPrefixLength);
            return LispAddressUtil.asIpv4PrefixBinaryEid(eid, parentPrefix, parentPrefixLength);
        } else if (eid.getAddress() instanceof Ipv6PrefixBinary) {
            Ipv6PrefixBinary prefix = (Ipv6PrefixBinary) eid.getAddress();
            short parentPrefixLength = (short) (prefix.getIpv6MaskLength() - 1);
            byte[] parentPrefix = MaskUtil.normalizeByteArray(prefix.getIpv6AddressBinary().getValue(),
                    parentPrefixLength);
            return LispAddressUtil.asIpv6PrefixBinaryEid(eid, parentPrefix, parentPrefixLength);
        }
        return null;
    }

    @Override
    public synchronized void subscribe(Subscriber subscriber, Eid subscribedEid) {
        Set<Subscriber> subscribers = getSubscribers(subscribedEid);
        if (subscribers == null) {
            subscribers = Sets.newConcurrentHashSet();
        } else if (subscribers.contains(subscriber)) {
            // If there is an entry already for this subscriber, remove it, so that it gets the new timestamp
            subscribers.remove(subscriber);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding new subscriber {} for EID {}", subscriber.getString(),
                    LispAddressStringifier.getString(subscribedEid));
        }
        subscribers.add(subscriber);
        addSubscribers(subscribedEid, subscribers);
    }

    private void addSubscribers(Eid address, Set<Subscriber> subscribers) {
        LoggingUtil.logSubscribers(LOG, address, subscribers);
        subscriberdb.put(address, subscribers);
    }

    @Override
    public Set<Subscriber> getSubscribers(Eid address) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving subscribers for EID {}", LispAddressStringifier.getString(address));
        }

        Set<Subscriber> subscribers = subscriberdb.get(address);
        LoggingUtil.logSubscribers(LOG, address, subscribers);
        return subscribers;
    }

    /*
     * Only remove subscribers if there is no exact match mapping in the map-cache other than the one specified by
     * origin. Right now we only have two origins, but in case we will have more, we only need to update this method,
     * not the callers. We use getData() instead of getMapping to do exact match instead of longest prefix match.
     */
    private void removeSubscribersConditionally(MappingOrigin origin, Eid address) {
        if (origin == MappingOrigin.Southbound) {
            if (pmc.getData(address, SubKeys.RECORD) == null) {
                removeSubscribers(address);
            }
        } else if (origin == MappingOrigin.Northbound) {
            if (smc.getData(address, SubKeys.RECORD) == null) {
                removeSubscribers(address);
            }
        }
    }

    private void removeSubscribers(Eid address) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing subscribers for EID {}", LispAddressStringifier.getString(address));
        }
        subscriberdb.remove(address);
    }

    @Override
    public void addAuthenticationKey(Eid key, MappingAuthkey authKey) {
        LOG.debug("Adding authentication key '{}' with key-ID {} for {}", authKey.getKeyString(), authKey.getKeyType(),
                LispAddressStringifier.getString(key));
        akdb.addAuthenticationKey(key, authKey);
    }

    @Override
    public MappingAuthkey getAuthenticationKey(Eid key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving authentication key for {}", LispAddressStringifier.getString(key));
        }
        return akdb.getAuthenticationKey(key);
    }

    @Override
    public void removeAuthenticationKey(Eid key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing authentication key for {}", LispAddressStringifier.getString(key));
        }
        akdb.removeAuthenticationKey(key);
    }

    @Override
    public void addData(MappingOrigin origin, Eid key, String subKey, Object data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add data of {} for key {} and subkey {}", data.getClass(),
                    LispAddressStringifier.getString(key), subKey);
        }
        tableMap.get(origin).addData(key, subKey, data);
    }

    @Override
    public Object getData(MappingOrigin origin, Eid key, String subKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving data for key {} and subkey {}", LispAddressStringifier.getString(key), subKey);
        }
        return tableMap.get(origin).getData(key, subKey);
    }

    @Override
    public void removeData(MappingOrigin origin, Eid key, String subKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing data for key {} and subkey {}", LispAddressStringifier.getString(key), subKey);
        }
        tableMap.get(origin).removeData(key, subKey);
    }

    @Override
    public Eid getParentPrefix(Eid key) {
        return smc.getParentPrefix(key);
    }


    /**
     * Restore all mappings and keys from mdsal datastore.
     */
    private void restoreDaoFromDatastore() {
        List<AuthenticationKey> authKeys = dsbe.getAllAuthenticationKeys();
        List<Mapping> mappings = dsbe.getAllMappings(LogicalDatastoreType.CONFIGURATION);

        /*
         * XXX By default, the operational datastore is not persisted to disk, either at run-time, or on shutdown,
         * so the following will have no effect (getLastUpdateTimestamp() will fail, since it's reading from
         * the operational datastore, and even if it didn't getAllMappings() will fail anyway). According to rovarga it
         * should be possible to turn on persistence for the operational datastore editing
         * etc/opendaylight/karaf/05-clustering.xml, by setting <persistence>true</persistence>. At the time of writing
         * the below code block that didn't seem to work though.
         */
        Long lastUpdateTimestamp = dsbe.getLastUpdateTimestamp();
        if (lastUpdateTimestamp != null && System.currentTimeMillis() - lastUpdateTimestamp
                > ConfigIni.getInstance().getRegistrationValiditySb()) {
            LOG.warn("Restore threshold passed, not restoring operational datastore into DAO");
        } else {
            mappings.addAll(dsbe.getAllMappings(LogicalDatastoreType.OPERATIONAL));
        }
        dsbe.removeLastUpdateTimestamp();

        LOG.info("Restoring {} mappings and {} keys from datastore into DAO", mappings.size(), authKeys.size());

        for (Mapping mapping : mappings) {
            addMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid(),
                    new MappingData(mapping.getMappingRecord()));
        }

        for (AuthenticationKey authKey : authKeys) {
            addAuthenticationKey(authKey.getEid(), authKey.getMappingAuthkey());
        }
    }

    public void destroy() {
        LOG.info("Mapping System is being destroyed!");
        dsbe.saveLastUpdateTimestamp();
    }

    @Override
    public String printMappings() {
        sbMappingTimeoutService.removeExpiredMappings();

        final StringBuilder sb = new StringBuilder();
        sb.append("Policy map-cache\n----------------\n");
        sb.append(pmc.printMappings());
        sb.append("\nSouthbound map-cache\n--------------------\n");
        sb.append(smc.printMappings());
        return sb.toString();
    }

    @Override
    public String prettyPrintMappings() {
        sbMappingTimeoutService.removeExpiredMappings();

        final StringBuilder sb = new StringBuilder();
        sb.append("Policy map-cache\n----------------\n");
        sb.append(pmc.prettyPrintMappings());
        sb.append("\nSouthbound map-cache\n--------------------\n");
        sb.append(smc.prettyPrintMappings());
        sb.append("\nSubscribers\n-----------\n");
        sb.append(prettyPrintSubscribers(subscriberdb));
        return sb.toString();
    }

    private static String prettyPrintSubscribers(Map<Eid, Set<Subscriber>> subscribers) {
        final StringBuilder sb = new StringBuilder();
        for (Eid eid: subscribers.keySet()) {
            sb.append("\n  ");
            sb.append(LispAddressStringifier.getString(eid));
            sb.append("\n");
            sb.append(LispMapCacheStringifier.prettyPrintSubscriberSet(subscribers.get(eid), 4));
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String printKeys() {
        return akdb.printKeys();
    }

    @Override
    public String prettyPrintKeys() {
        return akdb.prettyPrintKeys();
    }

    public void cleanCaches() {
        dao.removeAll();
        subscriberdb.clear();
        buildMapCaches();
    }

    public void cleanSBMappings() {
        smc = new SimpleMapCache(sdao);
    }

    @Override
    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }
}
