/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.util.EnumMap;
import java.util.List;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.lispflowmapping.implementation.mapcache.FlatMapCache;
import org.opendaylight.lispflowmapping.implementation.mapcache.MultiTableMapCache;
import org.opendaylight.lispflowmapping.implementation.mapcache.SimpleMapCache;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
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

    public void addMapping(MappingOrigin origin, LispAddressContainer key, Object value) {
        tableMap.get(origin).addMapping(key, value, overwrite);
    }

    public void updateMappingRegistration(MappingOrigin origin, LispAddressContainer key) {
        tableMap.get(origin).updateMappingRegistration(key);
    }

    @Override
    public Object getMapping(LispAddressContainer src, LispAddressContainer dst) {
        // NOTE: what follows is just a simple implementation of a lookup logic, it SHOULD be subject to future
        // improvements

        // first lookup src/dst in policy table
        Object mapping = pmc.getMapping(src, dst);

        // if nothing is found, lookup src/dst in sb table
        if (mapping == null) {
            return smc.getMapping(src, dst);
        }

        return mapping;
    }

    @Override
    public Object getMapping(LispAddressContainer dst) {
        return getMapping((LispAddressContainer)null, dst);
    }

    @Override
    public Object getMapping(MappingOrigin origin, LispAddressContainer key) {
        return tableMap.get(origin).getMapping(null, key);
    }

    @Override
    public void removeMapping(MappingOrigin origin, LispAddressContainer key) {
        tableMap.get(origin).removeMapping(key, overwrite);
        if (notificationService) {
            // TODO
        }
    }

    @Override
    public void addAuthenticationKey(LispAddressContainer key, String authKey) {
        LOG.debug("Adding authentication key '{}' for {}", key,
                LispAddressStringifier.getString(key));
        smc.addAuthenticationKey(key, authKey);
    }

    @Override
    public String getAuthenticationKey(LispAddressContainer key) {
        LOG.debug("Retrieving authentication key for {}", LispAddressStringifier.getString(key));
        return smc.getAuthenticationKey(key);
    }

    @Override
    public void removeAuthenticationKey(LispAddressContainer key) {
        LOG.debug("Removing authentication key for {}", LispAddressStringifier.getString(key));
        smc.removeAuthenticationKey(key);
    }

    @Override
    public void addData(MappingOrigin origin, LispAddressContainer key, String subKey, Object data) {
        LOG.debug("Add data of class {} for key {} and subkey {}", data.getClass(),
                LispAddressStringifier.getString(key), subKey);
        tableMap.get(origin).addData(key, subKey, data);
    }

    @Override
    public Object getData(MappingOrigin origin, LispAddressContainer key, String subKey) {
        LOG.debug("Retrieving data for key {} and subkey {}", LispAddressStringifier.getString(key), subKey);
        return tableMap.get(origin).getData(key, subKey);
    }

    @Override
    public void removeData(MappingOrigin origin, LispAddressContainer key, String subKey) {
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
            addMapping(mapping.getOrigin(), mapping.getLispAddressContainer(),
                    new EidToLocatorRecordBuilder(mapping).build());
        }

        for (AuthenticationKey authKey : authKeys) {
            addAuthenticationKey(authKey.getLispAddressContainer(), authKey.getAuthkey());
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
