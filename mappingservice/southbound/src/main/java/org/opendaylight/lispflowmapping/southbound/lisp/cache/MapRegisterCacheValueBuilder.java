/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class MapRegisterCacheValueBuilder implements Builder <MapRegisterCacheValue> {
    private byte[] _val;
    private Boolean _wantMapNotify;
    private Boolean _mergeBit;


    public MapRegisterCacheValueBuilder() {
    }

    public MapRegisterCacheValueBuilder(MapRegisterCacheValue base) {
        this._val = base.getVal();
        this._wantMapNotify = base.isWantMapNotifyBitSet();
        this._mergeBit = base.isMergeBitSet();
    }


    public byte[] getVal() {
        return _val == null ? null : _val.clone();
    }

    private boolean getWantMapNotify() {
        return _wantMapNotify;
    }


    public MapRegisterCacheValueBuilder setVal(final byte[] value) {
        this._val = value;
        return this;
    }

    public MapRegisterCacheValueBuilder setWantMapNotify(final boolean _wantMapNotify) {
        this._wantMapNotify = _wantMapNotify;
        return this;
    }

    public MapRegisterCacheValueBuilder setMerge(final boolean _mergeBit) {
        this._mergeBit = _mergeBit;
        return this;
    }

    public MapRegisterCacheValue build() {
        return new MapRegisterCacheValueImpl(this);
    }

    public boolean getMergeBit() {
        return _mergeBit;
    }

    private static final class MapRegisterCacheValueImpl implements MapRegisterCacheValue {

        private final byte[] _val;
        private final Boolean _wantMapNotify;
        private final Boolean _mergeBit;
        private List<Eid> _eids;

        private MapRegisterCacheValueImpl(MapRegisterCacheValueBuilder base) {
            this._val = base.getVal();
            this._wantMapNotify = base.getWantMapNotify();
            this._mergeBit = base.getMergeBit();
        }

        @Override
        public byte[] getVal() {
            return _val == null ? null : _val.clone();
        }

        @Override
        public boolean isWantMapNotifyBitSet() {
            return _wantMapNotify;
        }

        @Override
        public boolean isMergeBitSet() {
            return _mergeBit;
        }

        @Override
        public List<Eid> getEids() {
            return _eids;
        }

        @Override
        public void setEids(List<Eid> eids) {
            this._eids = eids;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(_val);
            result = prime * result + Objects.hashCode(_eids);
            result = prime * result + Objects.hashCode(_mergeBit);
            result = prime * result + Objects.hashCode(_wantMapNotify);

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

            if (!Objects.equals(_eids, other.getEids())) {
                return false;
            }

            if (!Objects.equals(_mergeBit, other.isMergeBitSet())) {
                return false;
            }
            if (!Objects.equals(_wantMapNotify, other.isWantMapNotifyBitSet())) {
                return false;
            }

            return true;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder ("MapRegisterCacheValue [");
            boolean first = true;
            if (_eids != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_eids=");
                builder.append(_eids);
            }

            if (_val != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_val=");
                builder.append(Arrays.toString(_val));
             }
            if (_mergeBit != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mergeBit=");
                builder.append(_mergeBit);
            }
            if (_wantMapNotify != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_wantMapNotify=");
                builder.append(_wantMapNotify);
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
