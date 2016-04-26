/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp.cache;

import java.util.HashMap;
import java.util.Map;

public class MapRegisterCache {
    /**
     * How long is record supposed to be relevant. After this time record isn't valid.
     */
    private static final long CACHE_RECORD_TIMEOUT = 90000;
    protected final Map<MapRegisterCacheKey, MapRegisterCacheValue> cache;


    public MapRegisterCache() {
        cache = new HashMap<>();
    }

    public void addMapRegisterEntryToCache(final MapRegisterCacheKey mapRegisterCacheKey, final MapRegisterCacheValue
            mapRegisterCacheValue) {
        cache.put(mapRegisterCacheKey, mapRegisterCacheValue);
    }

    public void addMapRegisterEntryToCache(final Map.Entry<MapRegisterCacheKey,MapRegisterCacheValue>
                                                   mapRegisterCacheEntry) {
        cache.put(mapRegisterCacheEntry.getKey(), mapRegisterCacheEntry.getValue());
    }

    public boolean isMapRegisterKeyInCache(final MapRegisterCacheKey mapRegisterCacheKey) {
        return cache.get(mapRegisterCacheKey) != null ? true : false;
    }

    public boolean isMapRegisterMsgCurrent(final MapRegisterCacheKey mapRegisterCacheKey) {
        final MapRegisterCacheValue mapRegisterCacheValue = cache.get(mapRegisterCacheKey);
        if (mapRegisterCacheValue != null) {
            final long creationTime = mapRegisterCacheValue.getTimestamp();
            final long currentTime = System.currentTimeMillis();
            if (currentTime - creationTime <= CACHE_RECORD_TIMEOUT) {
                return true;
            }
        }
        return false;
    }

    public MapRegisterCacheValue getMapRegisterKeyInCache(final MapRegisterCacheKey mapRegisterCacheKey) {
        return cache.get(mapRegisterCacheKey);
    }


    public void refreshRecord(final MapRegisterCacheKey mapRegisterCacheKey) {
        final MapRegisterCacheValue mapRegisterCacheValue = cache.get(mapRegisterCacheKey);
        mapRegisterCacheValue.setTimestamp(System.currentTimeMillis());
    }
}
