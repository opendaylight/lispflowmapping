/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.inmemorydb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOCGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashMapDb implements ILispDAO, AutoCloseable {

    protected static final Logger LOG = LoggerFactory.getLogger(HashMapDb.class);
    private ConcurrentMap<Object, ConcurrentMap<String, Object>> data = new ConcurrentHashMap<Object, ConcurrentMap<String, Object>>();
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private int recordTimeOut = 240;

    @Override
    public void put(Object key, MappingEntry<?>... values) {
        if (!data.containsKey(key)) {
            data.put(key, new ConcurrentHashMap<String, Object>());
        }
        for (MappingEntry<?> entry : values) {
            data.get(key).put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object getSpecific(Object key, String valueKey) {
        Map<String, Object> keyToValues = data.get(key);
        if (keyToValues == null) {
            return null;
        }
        return keyToValues.get(valueKey);
    }

    @Override
    public Map<String, Object> get(Object key) {
        return data.get(key);
    }

    @Override
    public void getAll(IRowVisitor visitor) {
        for (ConcurrentMap.Entry<Object, ConcurrentMap<String, Object>> keyEntry : data.entrySet()) {
            for (Map.Entry<String, Object> valueEntry : keyEntry.getValue().entrySet()) {
                visitor.visitRow(keyEntry.getKey(), valueEntry.getKey(), valueEntry.getValue());
            }
        }
    }

    @Override
    public void remove(Object key) {
        data.remove(key);
    }

    @Override
    public void removeSpecific(Object key, String valueKey) {
        if (data.containsKey(key) && data.get(key).containsKey(valueKey)) {
            data.get(key).remove(valueKey);
        }
    }

    @Override
    public void removeAll() {
        data.clear();
    }

    public void cleanOld() {
        getAll(new IRowVisitor() {
            public void visitRow(Object keyId, String valueKey, Object value) {
                if (value instanceof MappingServiceRLOCGroup) {
                    MappingServiceRLOCGroup rloc = (MappingServiceRLOCGroup) value;
                    if (isExpired(rloc)) {
                        removeSpecific(keyId, valueKey);
                    }
                }
            }

            private boolean isExpired(MappingServiceRLOCGroup rloc) {
                return System.currentTimeMillis() - rloc.getRegisterdDate().getTime() > TimeUnit.MILLISECONDS.convert(recordTimeOut, timeUnit);
            }
        });
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

    public void close() throws Exception {
        data.clear();
    }

}
