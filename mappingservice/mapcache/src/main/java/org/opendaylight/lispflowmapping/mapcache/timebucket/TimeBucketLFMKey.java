/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.mapcache.timebucket;

import java.util.Objects;

/**
 * Created by sheikahm on 10/4/16.
 */
public class TimeBucketLFMKey {
    private Object key;
    private String subKey;

    private int hash = 0;
    private volatile boolean hashValid = false;

    public TimeBucketLFMKey(Object key, String subKey) {
        this.key = key;
        this.subKey = subKey;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public String getSubKey() {
        return subKey;
    }

    public void setSubKey(String subKey) {
        this.subKey = subKey;
    }

    @Override
    public int hashCode() {
        if (hashValid) {
            return hash;
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(key);
        result = prime * result + Objects.hashCode(subKey);
        hash = result;
        hashValid = true;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TimeBucketLFMKey)) {
            return false;
        }
        TimeBucketLFMKey other = (TimeBucketLFMKey) obj;

        if (!(key.equals(other.getKey()))) {
            return false;
        }

        if (!(subKey.equals(other.getSubKey()))) {
            return false;
        }

        return true;
    }
}
