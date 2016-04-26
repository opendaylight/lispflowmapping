/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.cache;

import java.util.HashMap;
import java.util.Map;

public class MapRegisterCache {
    private final Map<MapRegisterCacheKey, MapRegisterCacheValue> cache;


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


}
