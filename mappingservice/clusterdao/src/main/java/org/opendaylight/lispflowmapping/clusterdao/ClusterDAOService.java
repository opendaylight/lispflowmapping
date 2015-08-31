/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.clusterdao;

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.clustering.services.CacheConfigException;
import org.opendaylight.controller.clustering.services.CacheExistException;
import org.opendaylight.controller.clustering.services.IClusterContainerServices;
import org.opendaylight.controller.clustering.services.IClusterServices;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterDAOService implements ILispDAO {

    protected static final Logger LOG = LoggerFactory.getLogger(ClusterDAOService.class);
    private IClusterContainerServices clusterContainerService = null;
    private ConcurrentMap<Object, ConcurrentMap<String, Object>> data;
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
        LOG.trace("Cluster Service unset");
        if (this.clusterContainerService == s) {
            this.clusterContainerService = null;
        }
        scheduler.shutdownNow();
    }

    private void allocateCache() {
        if (this.clusterContainerService == null) {
            LOG.warn("un-initialized clusterContainerService, can't create cache");
            return;
        }
        LOG.trace("Creating Cache for ClusterDAOService");
        try {
            this.clusterContainerService.createCache(CACHE_NAME, EnumSet.of(IClusterServices.cacheMode.NON_TRANSACTIONAL));
        } catch (CacheConfigException cce) {
            LOG.warn("Cache couldn't be created for ClusterDAOService -  check cache mode");
        } catch (CacheExistException cce) {
            LOG.warn("Cache for ClusterDAOService already exists, destroy and recreate");
        }
        LOG.trace("Cache successfully created for ClusterDAOService");
    }

    @SuppressWarnings({ "unchecked" })
    private void retrieveCache() {
        if (this.clusterContainerService == null) {
            LOG.warn("un-initialized clusterContainerService, can't retrieve cache");
            return;
        }
        LOG.trace("Retrieving cache for ClusterDAOService");
        data = (ConcurrentMap<Object, ConcurrentMap<String, Object>>) this.clusterContainerService.getCache(CACHE_NAME);
        if (data == null) {
            LOG.warn("Cache couldn't be retrieved for ClusterDAOService");
        }
        LOG.trace("Cache was successfully retrieved for ClusterDAOService");
    }

    public void getAll(IRowVisitor visitor) {
        for (ConcurrentMap.Entry<Object, ConcurrentMap<String, Object>> keyEntry : data.entrySet()) {
            for (Map.Entry<String, Object> valueEntry : keyEntry.getValue().entrySet()) {
                visitor.visitRow(keyEntry.getKey(), valueEntry.getKey(), valueEntry.getValue());
            }
        }
    }

    public void put(Object key, MappingEntry<?>... values) {
        if (!data.containsKey(key)) {
            data.put(key, new ConcurrentHashMap<String, Object>());
        }
        for (MappingEntry<?> entry : values) {
            data.get(key).put(entry.getKey(), entry.getValue());
        }
    }

    // TODO: this should be moved outside of DAO implementation
    public void cleanOld() {
        getAll(new IRowVisitor() {
            public void visitRow(Object keyId, String valueKey, Object value) {
                if (value != null && valueKey instanceof String && ((String) valueKey).equals(SubKeys.REGDATE)) {
                    Date date = (Date) value;
                    if (isExpired(date)) {
                        removeSpecific(keyId, SubKeys.RECORD);
                    }
                }
            }

            private boolean isExpired(Date date) {
                return System.currentTimeMillis() - date.getTime() > TimeUnit.MILLISECONDS.convert(recordTimeOut, timeUnit);
            }
        });
    }

    public Object getSpecific(Object key, String valueKey) {
        Map<String, Object> keyToValues = data.get(key);
        if (keyToValues == null) {
            return null;
        }
        return keyToValues.get(valueKey);
    }

    public Map<String, Object> get(Object key) {
        return data.get(key);
    }

    public void remove(Object key) {
        data.remove(key);
    }

    public void removeSpecific(Object key, String valueKey) {
        if (data.containsKey(key) && data.get(key).containsKey(valueKey)) {
            data.get(key).remove(valueKey);
        }
    }

    public void removeAll() {
        data.clear();
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

    @Override
    public ILispDAO putNestedTable(Object key, String valueKey) {
        // TODO Auto-generated method stub
        return null;
    }
}
