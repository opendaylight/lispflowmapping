/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.MappingMergeUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IAuthKeyDb;
import org.opendaylight.lispflowmapping.interfaces.mapcache.ILispMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.mapcache.AuthKeyDb;
import org.opendaylight.lispflowmapping.mapcache.MultiTableMapCache;
import org.opendaylight.lispflowmapping.mapcache.SimpleMapCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
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
 *
 */
public class MappingSystem implements IMappingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(MappingSystem.class);
    private static final String AUTH_KEY_TABLE = "authentication";
    private static final int TTL_RLOC_TIMED_OUT = 1;
    private static final int TTL_NO_RLOC_KNOWN = 15;
    private boolean notificationService;
    private boolean mappingMerge;
    private ILispDAO dao;
    private ILispDAO sdao;
    private ILispMapCache smc;
    private IMapCache pmc;
    private IAuthKeyDb akdb;
    private final EnumMap<MappingOrigin, IMapCache> tableMap = new EnumMap<>(MappingOrigin.class);
    private DataStoreBackEnd dsbe;
    private boolean isMaster = false;

    public MappingSystem(ILispDAO dao, boolean iterateMask, boolean notifications, boolean mappingMerge) {
        this.dao = dao;
        this.notificationService = notifications;
        this.mappingMerge = mappingMerge;
        buildMapCaches();
    }

    public void setDataStoreBackEnd(DataStoreBackEnd dsbe) {
        this.dsbe = dsbe;
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

    public void addMapping(MappingOrigin origin, Eid key, MappingData mappingData) {
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
                    mergeMappings(key);
                    return;
                } else {
                    clearPresentXtrIdMappings(key);
                    smc.addMapping(key, xtrId, mappingData);
                }
            }
        }

        tableMap.get(origin).addMapping(key, mappingData);
    }

    private void clearPresentXtrIdMappings(Eid key) {
        List<MappingData> allXtrMappingList = (List<MappingData>) (List<?>) smc.getAllXtrIdMappings(key);

        if (((MappingData) smc.getMapping(key, (XtrId) null)).isMergeEnabled()) {
            LOG.trace("Different xTRs have different merge configuration!");
        }

        for (MappingData mappingData : allXtrMappingList) {
            smc.removeMapping(key, mappingData.getXtrId());
            dsbe.removeXtrIdMapping(DSBEInputUtil.toXtrIdMapping(mappingData));
        }
    }

    @Override
    public MappingData addNegativeMapping(Eid key) {
        MappingRecord mapping = getNegativeMapping(key);
        MappingData mappingData = new MappingData(mapping);
        smc.addMapping(mapping.getEid(), mappingData);
        dsbe.addMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, mapping.getEid(), null, mappingData));
        return mappingData;
    }

    private MappingRecord getNegativeMapping(Eid eid) {
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
        recordBuilder.setAction(Action.NativelyForward);
        if (getAuthenticationKey(eid) != null) {
            recordBuilder.setRecordTtl(TTL_RLOC_TIMED_OUT);
        } else {
            recordBuilder.setRecordTtl(TTL_NO_RLOC_KNOWN);
        }
        return recordBuilder.build();
    }

    /*
     * Since this method is only called when there is a hit in the southbound Map-Register cache, and that cache is
     * not used when merge is on, it's OK to ignore the effects of timestamp changes on merging for now.
     */
    public void refreshMappingRegistration(Eid key, XtrId xtrId, Long timestamp) {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        MappingData mappingData = (MappingData) smc.getMapping(null, key);
        if (mappingData != null) {
            mappingData.setTimestamp(new Date(timestamp));
        } else {
            LOG.warn("Could not update timestamp for EID {}, no mapping found", LispAddressStringifier.getString(key));
        }
        if (xtrId != null) {
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

    private MappingData mergeMappings(Eid key) {
        List<XtrId> expiredMappings = new ArrayList<>();
        Set<IpAddressBinary> sourceRlocs = new HashSet<>();
        MappingData mergedMappingData = MappingMergeUtil.mergeXtrIdMappings(smc.getAllXtrIdMappings(key),
                expiredMappings, sourceRlocs);
        smc.removeXtrIdMappings(key, expiredMappings);
        for (XtrId xtrId : expiredMappings) {
            dsbe.removeXtrIdMapping(DSBEInputUtil.toXtrIdMapping(xtrId));
        }
        if (mergedMappingData != null) {
            smc.addMapping(key, mergedMappingData, sourceRlocs);
            dsbe.addMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, key,
                    mergedMappingData.getRecord().getSiteId(), mergedMappingData));
        } else {
            smc.removeMapping(key);
            dsbe.removeMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, key));
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
            return removeExpiredMapping(dst, xtrId, mappingData);
        } else {
            return mappingData;
        }
    }

    /*
     * This private method either removes the main mapping ONLY, or the xTR-ID mapping ONLY, based on xtrId being
     * null or non-null. Caller functions should take care of removing both when necessary.
     */
    private MappingData removeExpiredMapping(Eid key, XtrId xtrId, MappingData mappingData) {
        if (mappingMerge && mappingData.isMergeEnabled()) {
            return mergeMappings(key);
        }
        if (xtrId == null) {
            smc.removeMapping(key);
            dsbe.removeMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, mappingData.getRecord().getEid(),
                    new SiteId(mappingData.getRecord().getSiteId()), mappingData));
        } else {
            smc.removeMapping(key, xtrId);
            dsbe.removeXtrIdMapping(DSBEInputUtil.toXtrIdMapping(mappingData));
        }
        return null;
    }

    @Override
    public Eid getWidestNegativePrefix(Eid key) {
        Eid nbPrefix = pmc.getWidestNegativeMapping(key);
        if (nbPrefix == null) {
            return null;
        }

        Eid sbPrefix = smc.getWidestNegativeMapping(key);
        if (sbPrefix == null) {
            return null;
        }

        // since prefixes overlap, just return the more specific (larger mask)
        if (LispAddressUtil.getIpPrefixMask(nbPrefix) < LispAddressUtil.getIpPrefixMask(sbPrefix)) {
            return sbPrefix;
        } else {
            return nbPrefix;
        }
    }

    @Override
    public void removeMapping(MappingOrigin origin, Eid key) {
        Set<SubscriberRLOC> subscribers = null;
        if (origin == MappingOrigin.Southbound) {
            MappingData mapping = (MappingData) smc.getMapping(null, key);
            if (mapping != null && mapping.isNegative()) {
                SimpleImmutableEntry<Eid, Set<SubscriberRLOC>> mergedNegativePrefix = computeMergedNegativePrefix(key);
                if (mergedNegativePrefix != null) {
                    addNegativeMapping(mergedNegativePrefix.getKey());
                    subscribers = mergedNegativePrefix.getValue();
                }
            }
        }
        tableMap.get(origin).removeMapping(key);
        if (notificationService) {
            // TODO
        }
    }

    @SuppressWarnings("unchecked")
    /*
     * Returns the "merged" prefix and the subscribers of the prefixes that were merged.
     */
    private SimpleImmutableEntry<Eid, Set<SubscriberRLOC>> computeMergedNegativePrefix(Eid eid) {
        // Variable to hold subscribers we collect along the way
        Set<SubscriberRLOC> subscribers = null;

        // If prefix sibling has a negative mapping, save its subscribers
        Eid sibling = smc.getSiblingPrefix(eid);
        MappingData mapping = (MappingData) smc.getMapping(null, sibling);
        if (mapping != null && mapping.isNegative()) {
            subscribers = (Set<SubscriberRLOC>) getData(MappingOrigin.Southbound, eid, SubKeys.SUBSCRIBERS);
        } else {
            return null;
        }

        Eid currentNode = sibling;
        Eid previousNode = sibling;
        while ((currentNode = smc.getVirtualParentSiblingPrefix(currentNode)) != null) {
            mapping = (MappingData) smc.getMapping(null, currentNode);
            if (mapping != null && mapping.isNegative()) {
                subscribers.addAll((Set<SubscriberRLOC>)
                        getData(MappingOrigin.Southbound, currentNode, SubKeys.SUBSCRIBERS));
                smc.removeMapping(currentNode);
            } else {
                break;
            }
            previousNode = currentNode;
        }
        return new SimpleImmutableEntry<>(getVirtualParent(previousNode), subscribers);
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
            LOG.debug("Add data of class {} for key {} and subkey {}", data.getClass(),
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
        final StringBuffer sb = new StringBuffer();
        sb.append("PolicyMapCache\n--------------\n");
        sb.append(pmc.printMappings());
        sb.append("SbMapCache\n----------\n");
        sb.append(smc.printMappings());
        return sb.toString();
    }

    @Override
    public String printKeys() {
        return akdb.printKeys();
    }

    public void cleanCaches() {
        dao.removeAll();
        buildMapCaches();
    }

    /*
     * XXX  Mappings and keys should be separated for this to work properly, as is it will remove northbound originated
     * authentication keys too, since they are currently stored in smc.
     */
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
