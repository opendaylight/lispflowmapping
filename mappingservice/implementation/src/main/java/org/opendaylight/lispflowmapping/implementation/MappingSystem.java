/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.MappingMergeUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.mapcache.ILispMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.mapcache.ExtendedMappingRecord;
import org.opendaylight.lispflowmapping.mapcache.MultiTableMapCache;
import org.opendaylight.lispflowmapping.mapcache.SimpleMapCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
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
    private boolean iterateMask;
    private boolean notificationService;
    private boolean overwrite;
    private ILispDAO dao;
    private ILispMapCache smc;
    private IMapCache pmc;
    private final EnumMap<MappingOrigin, IMapCache> tableMap = new EnumMap<>(MappingOrigin.class);
    private DataStoreBackEnd dsbe;
    private boolean isMaster = false;

    public MappingSystem(ILispDAO dao, boolean iterateMask, boolean notifications, boolean overwrite) {
        this.dao = dao;
        this.iterateMask = iterateMask;
        this.notificationService = notifications;
        this.overwrite = overwrite;
        buildMapCaches();
    }

    public void setDataStoreBackEnd(DataStoreBackEnd dsbe) {
        this.dsbe = dsbe;
    }

    @Override
    public void setOverwritePolicy(boolean overwrite) {
        this.overwrite = overwrite;
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
         * Therefore, if a new origin is added, probably a new table should be instantiate here as well.
         */
        smc = new SimpleMapCache(dao.putTable(MappingOrigin.Southbound.toString()));
        pmc = new MultiTableMapCache(dao.putTable(MappingOrigin.Northbound.toString()));
        tableMap.put(MappingOrigin.Northbound, pmc);
        tableMap.put(MappingOrigin.Southbound, smc);
    }

    public void addMapping(MappingOrigin origin, Eid key, MappingRecord mapping, boolean merge) {
        if (mapping == null) {
            LOG.warn("addMapping() called with null mapping, ignoring");
            return;
        }

        Long timestamp = System.currentTimeMillis();

        if (origin == MappingOrigin.Southbound) {
            XtrId xtrId = mapping.getXtrId();
            if (xtrId == null && merge) {
                LOG.warn("addMapping() called will null xTR-ID in MappingRecord, while merge is set, ignoring");
                return;
            }
            if (xtrId != null && !overwrite) {
                smc.addMapping(key, xtrId, mapping, timestamp);
            }
            if (xtrId != null && merge) {
                List<XtrId> expiredMappings = new ArrayList<>();
                Set<IpAddressBinary> sourceRlocs = new HashSet<>();
                ExtendedMappingRecord mergedEntry = MappingMergeUtil.mergeXtrIdMappings(smc.getAllXtrIdMappings(key),
                        expiredMappings, sourceRlocs);
                smc.removeXtrIdMappings(key, expiredMappings);
                if (mergedEntry == null) {
                    return;
                }
                timestamp = mergedEntry.getTimestamp().getTime();
                smc.addMapping(key, mergedEntry.getRecord(), timestamp, sourceRlocs);
            }
        }

        tableMap.get(origin).addMapping(key, mapping);
    }

    public void refreshMappingRegistration(Eid key, XtrId xtrId, Long timestamp) {
        smc.refreshMappingRegistration(key, xtrId, timestamp);
    }

    private MappingRecord updateServicePathMappingRecord(MappingRecord mapping, Eid eid) {
        // keep properties of original record
        MappingRecordBuilder recordBuilder = new MappingRecordBuilder(mapping);
        recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());

        // there should only be one locator record
        if (mapping.getLocatorRecord().size() != 1) {
            LOG.warn("MappingRecord associated to ServicePath EID has more than one locator!");
            return mapping;
        }

        LocatorRecord locatorRecord = mapping.getLocatorRecord().get(0);
        long serviceIndex = ((ServicePath) eid.getAddress()).getServicePath().getServiceIndex();
        int index = LispAddressUtil.STARTING_SERVICE_INDEX - (int) serviceIndex;
        Rloc rloc = locatorRecord.getRloc();
        if (rloc.getAddress() instanceof Ipv4 || rloc.getAddress() instanceof Ipv6) {
            if (index != 0) {
                LOG.warn("Service Index should be 255 for simple IP RLOCs!");
            }
            return mapping;
        } else if (rloc.getAddress() instanceof ExplicitLocatorPath) {
            ExplicitLocatorPath elp = (ExplicitLocatorPath) rloc.getAddress();
            List<Hop> hops = elp.getExplicitLocatorPath().getHop();

            if (index < 0 || index > hops.size())  {
                LOG.warn("Service Index out of bounds!");
                return mapping;
            }

            SimpleAddress nextHop = hops.get(index).getAddress();
            LocatorRecordBuilder lrb = new LocatorRecordBuilder(locatorRecord);
            lrb.setRloc(LispAddressUtil.toRloc(nextHop));
            recordBuilder.getLocatorRecord().add(lrb.build());
            return recordBuilder.build();
        } else {
            LOG.warn("Nothing to do with ServicePath mapping record");
            return mapping;
        }
    }

    @Override
    public MappingRecord getMapping(Eid src, Eid dst) {
        // NOTE: Currently we have two lookup algorithms implemented, which are configurable

        if (ConfigIni.getInstance().getLookupPolicy() == IMappingService.LookupPolicy.NB_AND_SB) {
            return getMappingNbSbIntersection(src, dst);
        } else {
            return getMappingNbFirst(src, dst);
        }
    }

    @Override
    public MappingRecord getMapping(Eid dst) {
        return getMapping((Eid)null, dst);
    }

    @Override
    public MappingRecord getMapping(Eid src, Eid dst, XtrId xtrId) {
        // Note: If xtrId is null, we need to go through regular policy checking else Policy doesn't matter

        if (xtrId == null) {
            return getMapping(src, dst);
        }

        return smc.getMapping(dst, xtrId);
    }

    @Override
    public MappingRecord getMapping(MappingOrigin origin, Eid key) {
        if (origin.equals(MappingOrigin.Southbound)) {
            return getSbMappingWithExpiration(null, key);
        }
        return tableMap.get(origin).getMapping(null, key);
    }

    private MappingRecord getMappingNbFirst(Eid src, Eid dst) {

        // Default lookup policy is northboundFirst
        //lookupPolicy == NB_FIRST

        MappingRecord nbMapping = pmc.getMapping(src, dst);

        if (nbMapping == null) {
            return getSbMappingWithExpiration(src, dst);
        }
        if (dst.getAddress() instanceof ServicePath) {
            return updateServicePathMappingRecord(nbMapping, dst);
        }
        return nbMapping;
    }

    private MappingRecord getMappingNbSbIntersection(Eid src, Eid dst) {
        //lookupPolicy == NB_AND_SB, we return intersection
        //of NB and SB mappings, or NB mapping if intersection is empty.

        MappingRecord nbMapping = pmc.getMapping(src, dst);
        if (nbMapping == null) {
            return nbMapping;
        }
        // no intersection for Service Path mappings
        if (dst.getAddress() instanceof ServicePath) {
            return updateServicePathMappingRecord(nbMapping, dst);
        }
        MappingRecord sbMapping = getSbMappingWithExpiration(src, dst);
        if (sbMapping == null) {
            return nbMapping;
        }
        // both NB and SB mappings exist. Compute intersection of the mappings
        return MappingMergeUtil.computeNbSbIntersection(nbMapping, sbMapping);
    }

    private MappingRecord getSbMappingWithExpiration(Eid src, Eid dst) {
        MappingRecord mapping = smc.getMapping(src, dst);
        if (MappingMergeUtil.mappingIsExpired(mapping)) {
            dsbe.removeMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, mapping.getEid(),
                    new SiteId(mapping.getSiteId()), mapping));
            return null;
        } else {
            return mapping;
        }
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
        tableMap.get(origin).removeMapping(key);
        if (notificationService) {
            // TODO
        }
    }

    @Override
    public void addAuthenticationKey(Eid key, MappingAuthkey authKey) {
        LOG.debug("Adding authentication key '{}' with key-ID {} for {}", authKey.getKeyString(), authKey.getKeyType(),
                LispAddressStringifier.getString(key));
        smc.addAuthenticationKey(key, authKey);
    }

    @Override
    public MappingAuthkey getAuthenticationKey(Eid key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving authentication key for {}", LispAddressStringifier.getString(key));
        }
        return smc.getAuthenticationKey(key);
    }

    @Override
    public void removeAuthenticationKey(Eid key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing authentication key for {}", LispAddressStringifier.getString(key));
        }
        smc.removeAuthenticationKey(key);
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


    /**
     * Restore all mappings and keys from mdsal datastore.
     */
    private void restoreDaoFromDatastore() {
        List<Mapping> mappings = dsbe.getAllMappings();
        List<AuthenticationKey> authKeys = dsbe.getAllAuthenticationKeys();

        LOG.info("Restoring {} mappings and {} keys from datastore into DAO", mappings.size(), authKeys.size());

        int expiredMappings = 0;
        for (Mapping mapping : mappings) {
            if (MappingMergeUtil.mappingIsExpired(mapping.getMappingRecord())) {
                dsbe.removeMapping(mapping);
                expiredMappings++;
                continue;
            }
            addMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid(), mapping.getMappingRecord(), false);
        }
        LOG.info("{} mappings were expired and were not restored", expiredMappings);

        for (AuthenticationKey authKey : authKeys) {
            addAuthenticationKey(authKey.getEid(), authKey.getMappingAuthkey());
        }
    }

    public void destroy() {
        LOG.info("Mapping System is being destroyed!");
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

    public void cleanCaches() {
        dao.removeAll();
        buildMapCaches();
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
