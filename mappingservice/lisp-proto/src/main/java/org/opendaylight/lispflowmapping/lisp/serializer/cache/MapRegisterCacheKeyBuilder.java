/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.serializer.cache;

import java.util.Arrays;
import org.opendaylight.yangtools.concepts.Builder;

public class MapRegisterCacheKeyBuilder implements Builder <MapRegisterCacheKey> {

    private byte[] _eidPrefix;
    private byte[] _siteId;
    private byte[] _xTRId;

    public MapRegisterCacheKeyBuilder() {
    }

    public MapRegisterCacheKeyBuilder(MapRegisterCacheKey base) {
        this._eidPrefix = base.getEidPrefix();
        this._siteId = base.getSiteId();
        this._xTRId = base.getXTRId();
    }


    public byte[] getEidPrefix() {
        return _eidPrefix == null ? null : _eidPrefix.clone();
    }

    public byte[] getSiteId() {
        return _siteId == null ? null : _siteId.clone();
    }

    public byte[] getXTRId() {
        return _xTRId == null ? null : _xTRId.clone();
    }

    public MapRegisterCacheKeyBuilder setEidPrefix(final byte[] value) {
        this._eidPrefix = value;
        return this;
    }


    public MapRegisterCacheKeyBuilder setSiteId(final byte[] value) {
        this._siteId = value;
        return this;
    }


    public MapRegisterCacheKeyBuilder setXTRId(final byte[] value) {
        this._xTRId = value;
        return this;
    }

    public MapRegisterCacheKey build() {
        return new MapRegisterCacheKeyImpl(this);
    }

    private static final class MapRegisterCacheKeyImpl implements MapRegisterCacheKey {

        public Class<MapRegisterCacheKey> getImplementedInterface() {
            return MapRegisterCacheKey.class;
        }

        private final byte[] _eidPrefix;
        private final byte[] _siteId;
        private final byte[] _xTRId;

        private MapRegisterCacheKeyImpl(MapRegisterCacheKeyBuilder base) {
            this._eidPrefix = base.getEidPrefix();
            this._siteId = base.getSiteId();
            this._xTRId = base.getXTRId();
        }

        @Override
        public byte[] getEidPrefix() {
            return _eidPrefix == null ? null : _eidPrefix.clone();
        }

        @Override
        public byte[] getSiteId() {
            return _siteId == null ? null : _siteId.clone();
        }

        @Override
        public byte[] getXTRId() {
            return _xTRId == null ? null : _xTRId.clone();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(_eidPrefix);
            result = prime * result + Arrays.hashCode(_siteId);
            result = prime * result + Arrays.hashCode(_xTRId);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MapRegisterCacheKey)) {
                return false;
            }
            MapRegisterCacheKey other = (MapRegisterCacheKey)obj;
            if (!Arrays.equals(_eidPrefix, other.getEidPrefix())) {
                return false;
            }
            if (!Arrays.equals(_siteId, other.getSiteId())) {
                return false;
            }
            if (!Arrays.equals(_xTRId, other.getXTRId())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder ("MapRegisterCacheKey [");
            boolean first = true;

            if (_eidPrefix != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_eidPrefix=");
                builder.append(Arrays.toString(_eidPrefix));
             }
            if (_siteId != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_siteId=");
                builder.append(Arrays.toString(_siteId));
             }
            if (_xTRId != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_xTRId=");
                builder.append(Arrays.toString(_xTRId));
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            return builder.append(']').toString();
        }
    }

}
