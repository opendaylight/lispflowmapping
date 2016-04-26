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

    protected final Map<MapRegisterCacheKey, MapRegisterCacheValue> cache;

    public MapRegisterCache() {
        cache = new HashMap<>();
    }

    public void addEntry(final MapRegisterCacheKey mapRegisterCacheKey, final MapRegisterCacheValue
            mapRegisterCacheValue) {
        cache.put(mapRegisterCacheKey, mapRegisterCacheValue);
    }

    public void addEntry(final Map.Entry<MapRegisterCacheKey, MapRegisterCacheValue>
                                 mapRegisterCacheEntry) {
        cache.put(mapRegisterCacheEntry.getKey(), mapRegisterCacheEntry.getValue());
    }

    public MapRegisterCacheValue getEntry(final MapRegisterCacheKey mapRegisterCacheKey) {
        if (mapRegisterCacheKey != null) {
            return cache.get(mapRegisterCacheKey);
        }
        return null;
    }


    public void removeEntry(final MapRegisterCacheKey mapRegisterCacheKey) {
        if (mapRegisterCacheKey != null) {
            cache.remove(mapRegisterCacheKey);
        }
    }

    public void refreshEntry(final MapRegisterCacheKey mapRegisterCacheKey) {
        final MapRegisterCacheValue mapRegisterCacheValue = cache.get(mapRegisterCacheKey);
        mapRegisterCacheValue.setTimestamp(System.currentTimeMillis());
    }
}
