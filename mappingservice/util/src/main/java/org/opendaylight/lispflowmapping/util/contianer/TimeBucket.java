/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.util.contianer;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by sheikahm on 9/26/16.
 */
public class TimeBucket {
    private static final Logger LOG = LoggerFactory.getLogger(TimeBucket.class);

    private HashMap<Object, Runnable> bucket;

    public TimeBucket() {
        bucket = new HashMap<>();
    }

    public void add(Object key, Runnable removalFunction) {
        bucket.put(key, removalFunction);
    }

    public void remove(Object key) {
        bucket.remove(key);
    }

    public void clearBucket() {
        bucket.forEach((key, removalFunction) -> {
            removalFunction.run();
        });
    }
}
