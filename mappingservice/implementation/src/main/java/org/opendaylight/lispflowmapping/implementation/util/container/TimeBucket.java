/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util.container;

import java.util.HashMap;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;


/**
 * Created by sheikahm on 9/26/16.
 */
public class TimeBucket {
    private HashMap<Eid, BucketElement> bucket;

    public TimeBucket() {
        bucket = new HashMap<>();
    }

    public void add(Eid key, String subKey, ILispDAO dao) {
        BucketElement bucketElement = bucket.get(key);
        if (bucketElement == null) {
            bucketElement = new BucketElement();
        }

        bucketElement.addNewElement(subKey, dao);
    }

    public void remove(Eid key, String subKey) {
        BucketElement bucketElement = bucket.get(key);
        if (bucketElement == null) {
            return;
        }
        removeObjectFromContainerAndBucketElement(key, subKey, bucketElement);
    }

    private void removeObjectFromContainerAndBucketElement(Eid key, String subKey, BucketElement bucketElement) {
        ILispDAO container = bucketElement.getAnElement(subKey);
        container.removeSpecific(key, subKey);
        bucketElement.deleteAnElement(subKey);
    }

    public void clearBucket() {
        bucket.forEach((key, bucketElement) -> {
            bucketElement.getElement().forEach((subkey, dao) -> {
                dao.removeSpecific(key, subkey);
            });
            bucketElement.getElement().clear();
        });
        bucket.clear();
    }
}
