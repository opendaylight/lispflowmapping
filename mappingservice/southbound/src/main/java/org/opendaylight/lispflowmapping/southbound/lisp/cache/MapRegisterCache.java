/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.MapRegisterCacheMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.MapRegisterCacheMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValueBuilder;

public class MapRegisterCache {

    protected final Map<MapRegisterCacheKey, MapRegisterCacheValue> cache;

    public MapRegisterCache() {
        cache = new ConcurrentHashMap<>();
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

    public synchronized MapRegisterCacheValue refreshEntry(final MapRegisterCacheKey mapRegisterCacheKey) {
        final MapRegisterCacheValue mapRegisterCacheValueOld = getEntry(mapRegisterCacheKey);
        final MapRegisterCacheMetadata mapRegisterCacheMetadataOld = mapRegisterCacheValueOld
                .getMapRegisterCacheMetadata();

        final MapRegisterCacheMetadataBuilder mapRegisterCacheMetadataBuilderNew = new MapRegisterCacheMetadataBuilder(
                mapRegisterCacheMetadataOld);
        mapRegisterCacheMetadataBuilderNew.setTimestamp(System.currentTimeMillis());

        final MapRegisterCacheValueBuilder mapRegisterCacheValueBuilderNew = new MapRegisterCacheValueBuilder();
        mapRegisterCacheValueBuilderNew.setPacketData(mapRegisterCacheValueOld.getPacketData());
        mapRegisterCacheValueBuilderNew.setMappingAuthkey(mapRegisterCacheValueOld.getMappingAuthkey());
        mapRegisterCacheValueBuilderNew.setMapRegisterCacheMetadata(mapRegisterCacheMetadataBuilderNew.build());

        return mapRegisterCacheValueBuilderNew.build();
    }

    public int cacheSize() {
        return cache.size();
    }
}
