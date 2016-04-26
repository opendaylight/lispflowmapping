/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.serializer.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class MapRegisterCacheValueBuilder implements Builder <MapRegisterCacheValue> {

    private byte[] _val;

    public MapRegisterCacheValueBuilder() {
    }

    public MapRegisterCacheValueBuilder(MapRegisterCacheValue base) {
        this._val = base.getVal();
    }


    public byte[] getVal() {
        return _val == null ? null : _val.clone();
    }

    public MapRegisterCacheValueBuilder setVal(final byte[] value) {
        this._val = value;
        return this;
    }

    public MapRegisterCacheValue build() {
        return new MapRegisterCacheValueImpl(this);
    }

    private static final class MapRegisterCacheValueImpl implements MapRegisterCacheValue {

        private final byte[] _val;

        private Map<Class<? extends Augmentation<MapRegisterCacheValue>>, Augmentation<MapRegisterCacheValue>> augmentation = Collections.emptyMap();

        private MapRegisterCacheValueImpl(MapRegisterCacheValueBuilder base) {
            this._val = base.getVal();
        }

        @Override
        public byte[] getVal() {
            return _val == null ? null : _val.clone();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(_val);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!(obj instanceof MapRegisterCacheValue)) {
                return false;
            }
            MapRegisterCacheValue other = (MapRegisterCacheValue)obj;
            if (!Arrays.equals(_val, other.getVal())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder ("MapRegisterCacheValue [");
            boolean first = true;

            if (_val != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_val=");
                builder.append(Arrays.toString(_val));
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
