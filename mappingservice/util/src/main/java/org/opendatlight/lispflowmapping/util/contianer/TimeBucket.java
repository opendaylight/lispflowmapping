/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendatlight.lispflowmapping.util.contianer;

import java.util.HashMap;

import org.opendatlight.lispflowmapping.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;


/**
 * Created by sheikahm on 9/26/16.
 */
public class TimeBucket {
    private HashMap<Eid, BucketElement> bucket;

    public TimeBucket() {
        bucket = new HashMap<>();
    }

    public void add(Eid key, String subKey, MappingAllInfo mappingAllInfo) {
        BucketElement bucketElement = bucket.get(key);
        if (bucketElement == null) {
            bucketElement = new BucketElement();
        }

        bucketElement.addNewElement(subKey, mappingAllInfo);
    }

    public void remove(Eid key, String subKey) {
        BucketElement bucketElement = bucket.get(key);
        if (bucketElement == null) {
            return;
        }
        removeObjectFromContainerAndBucketElement(key, subKey, bucketElement);
    }

    private void removeObjectFromContainerAndBucketElement(Eid key, String subKey, BucketElement bucketElement) {
        MappingAllInfo mappingAllInfo = bucketElement.getSpecificElement(subKey);

        deleteSingleTimeoutRecordFromMapCacheAndDSBE(key, subKey, mappingAllInfo);

        bucketElement.deleteSpecificElement(subKey);
    }

    private void deleteSingleTimeoutRecordFromMapCacheAndDSBE(Eid key, String subKey, MappingAllInfo mappingAllInfo) {
        ILispDAO dao = mappingAllInfo.getDao();

        DataStoreBackEnd dsbe = mappingAllInfo.getDsbe();
        MappingRecord mappingRecord = mappingAllInfo.getMappingRecord();
        SiteId siteId = mappingRecord.getSiteId();

        //remove from map cache
        mappingAllInfo.getDao().removeSpecific(key, subKey);

        if (dsbe != null) {
            //delete the entry from datastore backend too
            dsbe.removeMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, key, siteId, mappingRecord));
            if (mappingRecord.getXtrId() != null) {
                dsbe.removeXtrIdMapping(DSBEInputUtil.toXtrIdMapping(mappingRecord));
            }
        }
    }

    public void clearBucket() {
        bucket.forEach((key, bucketElement) -> {
            bucketElement.getElement().forEach((subkey, mappingAllInfo) -> {
                deleteSingleTimeoutRecordFromMapCacheAndDSBE(key, subkey, mappingAllInfo);
            });
            bucketElement.getElement().clear();
        });
        bucket.clear();
    }
}
