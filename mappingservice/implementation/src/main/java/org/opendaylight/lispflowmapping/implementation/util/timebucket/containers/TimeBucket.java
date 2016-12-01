/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util.timebucket.containers;

import java.util.HashMap;

import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 12/1/16.
 */
public class TimeBucket {
    private static final Logger LOG = LoggerFactory.getLogger(TimeBucket.class);

    private HashMap<Eid, MappingData> bucketElements;

    private MappingSystem mappingSystem;

    public TimeBucket(MappingSystem mappingSystem) {
        bucketElements = new HashMap<>();
        this.mappingSystem = mappingSystem;
    }

    public void add(Eid key, MappingData mappingData) {
        bucketElements.put(key, mappingData);
    }

    public void removeFromBucketOnly(Eid key) {
        MappingData mappingData = bucketElements.get(key);

        if (mappingData != null) {
            bucketElements.remove(key);
        }
    }

    public void clearBucket() {
        bucketElements.forEach((key, mappingData) -> {
            mappingSystem.handleSbMappingExpiration(key, mappingData);
        });
        bucketElements.clear();
    }
}