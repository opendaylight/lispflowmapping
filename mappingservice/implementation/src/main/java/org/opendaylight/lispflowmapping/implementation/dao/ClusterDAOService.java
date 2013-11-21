/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.dao;

import java.lang.reflect.ParameterizedType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.clustering.services.CacheConfigException;
import org.opendaylight.controller.clustering.services.CacheExistException;
import org.opendaylight.controller.clustering.services.IClusterContainerServices;
import org.opendaylight.controller.clustering.services.IClusterServices;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.interfaces.dao.IQueryAll;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingValueKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterDAOService implements ILispDAO, IQueryAll {

    protected static final Logger logger = LoggerFactory.getLogger(ClusterDAOService.class);
    private IClusterContainerServices clusterContainerService = null;
    private ConcurrentMap<Class<?>, Map<Object, Map<String, Object>>> typeToKeysToValues;
    private final String CACHE_NAME = "mappingServiceCache";
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private int recordTimeOut = 240;
    private int cleanInterval = 10;
    private ScheduledExecutorService scheduler;

    void setClusterContainerService(IClusterContainerServices s) {
        this.clusterContainerService = s;
        allocateCache();
        retrieveCache();
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                cleanOld();
            }
        }, 0, cleanInterval, timeUnit);
    }

    void unsetClusterContainerService(IClusterContainerServices s) {
        logger.debug("Cluster Service unset");
        if (this.clusterContainerService == s) {
            this.clusterContainerService = null;
        }
        scheduler.shutdownNow();
    }

    @SuppressWarnings("deprecation")
    private void allocateCache() {
        if (this.clusterContainerService == null) {
            logger.error("un-initialized clusterContainerService, can't create cache");
            return;
        }
        logger.debug("Creating Cache for ClusterDAOService");
        try {
            this.clusterContainerService.createCache(CACHE_NAME, EnumSet.of(IClusterServices.cacheMode.NON_TRANSACTIONAL));
        } catch (CacheConfigException cce) {
            logger.error("Cache couldn't be created for ClusterDAOService -  check cache mode");
        } catch (CacheExistException cce) {
            logger.error("Cache for ClusterDAOService already exists, destroy and recreate");
        }
        logger.debug("Cache successfully created for ClusterDAOService");
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private void retrieveCache() {
        if (this.clusterContainerService == null) {
            logger.error("un-initialized clusterContainerService, can't retrieve cache");
            return;
        }
        logger.debug("Retrieving cache for ClusterDAOService");
        typeToKeysToValues = (ConcurrentMap<Class<?>, Map<Object, Map<String, Object>>>) this.clusterContainerService.getCache(CACHE_NAME);
        if (typeToKeysToValues == null) {
            logger.error("Cache couldn't be retrieved for ClusterDAOService");
        }
        logger.debug("Cache was successfully retrieved for ClusterDAOService");
    }

    public void getAll(IRowVisitor visitor) {
        for (Map.Entry<Class<?>, Map<Object, Map<String, Object>>> typeEntry : typeToKeysToValues.entrySet()) {
            for (Map.Entry<Object, Map<String, Object>> keyEntry : typeEntry.getValue().entrySet()) {
                for (Map.Entry<String, Object> valueEntry : keyEntry.getValue().entrySet()) {
                    visitor.visitRow(typeEntry.getKey(), keyEntry.getKey(), valueEntry.getKey(), valueEntry.getValue());
                }
            }
        }
    }

    public <K> void put(K key, MappingEntry<?>... values) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        Map<String, Object> keyToValues = new HashMap<String, Object>();
        for (MappingEntry<?> entry : values) {
            keyToValues.put(entry.getKey(), entry.getValue());
        }
        keysToValues.put(key, keyToValues);
    }

    private <K> Map<Object, Map<String, Object>> getTypeMap(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Illegal null key.");
        }
        Map<Object, Map<String, Object>> keysToValues = typeToKeysToValues.get(key.getClass());
        if (keysToValues == null) {
            throw new IllegalArgumentException("Unknown key type " + key.getClass() + ". must register with IConverter first.");
        }
        return keysToValues;
    }

    @SuppressWarnings("unchecked")
    public <K, V> V getSpecific(K key, MappingValueKey<V> valueKey) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        Map<String, Object> keyToValues = keysToValues.get(key);
        if (keyToValues == null) {
            return null;
        }
        return (V) keyToValues.get(valueKey.getKey());
    }

    public void cleanOld() {
        getAll(new IRowVisitor() {
            public void visitRow(Class<?> keyType, Object keyId, String valueKey, Object value) {
                if (value instanceof MappingServiceValue) {
                    MappingServiceValue msv = (MappingServiceValue) value;
                    for (Iterator<MappingServiceRLOC> it = msv.getRlocs().iterator(); it.hasNext();) {
                        MappingServiceRLOC rloc = it.next();
                        if (isExpired(rloc)) {
                            it.remove();
                        }
                    }
                }
            }

            private boolean isExpired(MappingServiceRLOC rloc) {
                return System.currentTimeMillis() - rloc.getRegisterdDate().getTime() > TimeUnit.MILLISECONDS.convert(recordTimeOut, timeUnit);
            }
        });
    }

    public <K> Object getSpecific(K key, String valueKey) {
        return getSpecific(key, new MappingValueKey<Object>(valueKey));
    }

    public <K> Map<String, ?> get(K key) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        return keysToValues.get(key);
    }

    public <K> boolean remove(K key) {
        Map<Object, Map<String, Object>> keysToValues = getTypeMap(key);
        return keysToValues.remove(key) != null;
    }

    public <UserType, DbType> void register(Class<? extends ILispTypeConverter<UserType, DbType>> userType) {
        Class<?> eidType = (Class<?>) ((ParameterizedType) userType.getGenericInterfaces()[0]).getActualTypeArguments()[0];
        typeToKeysToValues.put(eidType, new HashMap<Object, Map<String, Object>>());
    }

    public void clearAll() {
        typeToKeysToValues.clear();
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setRecordTimeOut(int recordTimeOut) {
        this.recordTimeOut = recordTimeOut;
    }

    public int getRecordTimeOut() {
        return recordTimeOut;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

}
