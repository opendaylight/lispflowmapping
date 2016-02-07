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
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.lispflowmapping.implementation.mapcache.FlatMapCache;
import org.opendaylight.lispflowmapping.implementation.mapcache.MultiTableMapCache;
import org.opendaylight.lispflowmapping.implementation.mapcache.SimpleMapCache;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.MappingMergeUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Mapping System coordinates caching of md-sal stored mappings and if so configured enables longest prefix match
 * mapping lookups
 *
 * @author Florin Coras
 *
 */
public class MappingSystem implements IMappingSystem {
    private final static Logger LOG = LoggerFactory.getLogger(MappingSystem.class);
    private boolean iterateMask;
    private boolean notificationService;
    private boolean overwrite;
    private ILispDAO dao;
    private IMapCache smc;
    private IMapCache pmc;
    private final EnumMap<MappingOrigin, IMapCache> tableMap = new EnumMap<>(MappingOrigin.class);
    private DataStoreBackEnd dsbe;
    private BindingAwareBroker bindingAwareBroker;

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
        this.iterateMask = iterate;
        if (smc != null || pmc != null) {
            buildMapCaches();
        }
    }

    public void initialize() {
        restoreDaoFromDatastore();
    }

    private void buildMapCaches() {
        /*
         * There exists a direct relationship between MappingOrigins and the tables that are part of the MappingSystem.
         * Therefore, if a new origin is added, probably a new table should be instantiate here as well.
         */
        if (iterateMask) {
            smc = new SimpleMapCache(dao.putTable(MappingOrigin.Southbound.toString()));
            pmc = new MultiTableMapCache(dao.putTable(MappingOrigin.Northbound.toString()));
        } else {
            smc = new FlatMapCache(dao.putTable(MappingOrigin.Southbound.toString()));
            pmc = new FlatMapCache(dao.putTable(MappingOrigin.Northbound.toString()));
        }
        tableMap.put(MappingOrigin.Northbound, pmc);
        tableMap.put(MappingOrigin.Southbound, smc);
    }

    public void addMapping(MappingOrigin origin, SiteId siteId, Eid key, Object data) {
        // Store data first in MapCache and only afterwards persist to datastore. This should be used only for SB
        // registrations
        addMapping(origin, key, data);
        dsbe.addMapping(DSBEInputUtil.toMapping(origin, key, siteId, (MappingRecord) data));
    }

    public void addMapping(MappingOrigin origin, Eid key, Object value) {
        tableMap.get(origin).addMapping(key, value, origin == MappingOrigin.Southbound ? overwrite : true);
    }

    public void updateMappingRegistration(MappingOrigin origin, Eid key) {
        tableMap.get(origin).updateMappingRegistration(key);
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
    public Object getMapping(Eid src, Eid dst) {
        // NOTE: Currently we have two lookup algorithms implemented, which are configurable
        // using lookupPolicy in ConfigIni.java

        if (ConfigIni.getInstance().getLookupPolicy() == ConfigIni.NB_AND_SB) {
            return getMappingNbSbIntersection(src, dst);
        } else {
            return getMappingNbFirst(src, dst);
        }
    }

    private Object getMappingNbFirst(Eid src, Eid dst) {

        // Default lookup policy is northboundFirst
        //lookupPolicy == NB_FIRST

        Object nbMapping = pmc.getMapping(src, dst);

        if (nbMapping == null) {
            return smc.getMapping(src, dst);
        }
        if (dst.getAddress() instanceof ServicePath) {
            return updateServicePathMappingRecord((MappingRecord) nbMapping, dst);
        }
        return nbMapping;
    }

    private Object getMappingNbSbIntersection(Eid src, Eid dst) {
        //lookupPolicy == NB_AND_SB, we return intersection
        //of NB and SB mappings, or NB mapping if intersection is empty.

        Object nbMapping = pmc.getMapping(src, dst);
        if (nbMapping == null) {
            return nbMapping;
        }
        // no intersection for Service Path mappings
        if (dst.getAddress() instanceof ServicePath) {
            return updateServicePathMappingRecord((MappingRecord)nbMapping, dst);
        }
        Object sbMapping = smc.getMapping(src, dst);
        if (sbMapping == null) {
            return nbMapping;
        }
        // both NB and SB mappings exist. Compute intersection of the mappings
        return MappingMergeUtil.computeNbSbIntersection((MappingRecord)nbMapping, (MappingRecord)sbMapping);
    }

    @Override
    public Object getMapping(Eid dst) {
        return getMapping((Eid)null, dst);
    }

    @Override
    public Object getMapping(MappingOrigin origin, Eid key) {
        return tableMap.get(origin).getMapping(null, key);
    }

    @Override
    public void removeMapping(MappingOrigin origin, Eid key) {
        tableMap.get(origin).removeMapping(key, origin == MappingOrigin.Southbound ? overwrite : true);
        if (notificationService) {
            // TODO
        }
    }

    @Override
    public void addAuthenticationKey(Eid key, MappingAuthkey authKey) {
        LOG.debug("Adding authentication key '{}' for {}", authKey.getKeyString(),
                LispAddressStringifier.getString(key));
        smc.addAuthenticationKey(key, authKey);
    }

    @Override
    public MappingAuthkey getAuthenticationKey(Eid key) {
        LOG.debug("Retrieving authentication key for {}", LispAddressStringifier.getString(key));
        return smc.getAuthenticationKey(key);
    }

    @Override
    public void removeAuthenticationKey(Eid key) {
        LOG.debug("Removing authentication key for {}", LispAddressStringifier.getString(key));
        smc.removeAuthenticationKey(key);
    }

    @Override
    public void addData(MappingOrigin origin, Eid key, String subKey, Object data) {
        LOG.debug("Add data of class {} for key {} and subkey {}", data.getClass(),
                LispAddressStringifier.getString(key), subKey);
        tableMap.get(origin).addData(key, subKey, data);
    }

    @Override
    public Object getData(MappingOrigin origin, Eid key, String subKey) {
        LOG.debug("Retrieving data for key {} and subkey {}", LispAddressStringifier.getString(key), subKey);
        return tableMap.get(origin).getData(key, subKey);
    }

    @Override
    public void removeData(MappingOrigin origin, Eid key, String subKey) {
        LOG.debug("Removing data for key {} and subkey {}", LispAddressStringifier.getString(key), subKey);
        tableMap.get(origin).removeData(key, subKey);
    }


    /**
     * Restore all mappings and keys from mdsal datastore
     */
    private void restoreDaoFromDatastore() {
        List<Mapping> mappings = dsbe.getAllMappings();
        List<AuthenticationKey> authKeys = dsbe.getAllAuthenticationKeys();

        LOG.info("Restoring {} mappings and {} keys from datastore into DAO", mappings.size(), authKeys.size());

        for (Mapping mapping : mappings) {
            addMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid(), mapping.getMappingRecord());
        }

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
}
