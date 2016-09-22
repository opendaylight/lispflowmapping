/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.util.contianer;

import java.util.HashMap;

import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.util.DSBEInputUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;



/**
 * Created by sheikahm on 9/26/16.
 */
public class TimeBucket {
    private HashMap<Object, BucketElement> bucket;

    public TimeBucket() {
        bucket = new HashMap<>();
    }

    public void add(Object key, String subKey, MappingAllInfo mappingAllInfo) {
        BucketElement bucketElement = bucket.get(key);
        if (bucketElement == null) {
            bucketElement = new BucketElement();
        }

        bucketElement.addNewElement(subKey, mappingAllInfo);
    }

    public void remove(Object key, String subKey) {
        BucketElement bucketElement = bucket.get(key);
        if (bucketElement == null) {
            return;
        }
        removeObjectFromContainerAndBucketElement(key, subKey, bucketElement);
    }

    private void removeObjectFromContainerAndBucketElement(Object key, String subKey, BucketElement bucketElement) {

        MappingAllInfo mappingAllInfo = null;
        if (bucketElement != null) {
            mappingAllInfo = bucketElement.getSpecificElement(subKey);
        }

        deleteSingleTimeoutRecordFromMapCacheAndDSBE(key, subKey, mappingAllInfo);

        bucketElement.deleteSpecificElement(subKey);
    }

    private void deleteSingleTimeoutRecordFromMapCacheAndDSBE(Object key, String subKey,
                                                              MappingAllInfo mappingAllInfo) {
        /*
        //Possible challenges:
        1) mappingAllInfo is null, something basic went wrong.
        2) mappingAllInfo.dsbe is null, meta data, no need to delete it form DataStoreBackEnd.
        3) We may be deleting XtrIdSpecific record only. We particularly don't save any information
           that detects that one bucket entry is xtrId specific, and that is, key is not of type Eid.
         */

        ILispDAO dao = mappingAllInfo.getDao();

        DataStoreBackEnd dsbe = mappingAllInfo.getDsbe();
        MappingRecord mappingRecord = mappingAllInfo.getMappingRecord();
        SiteId siteId = mappingRecord.getSiteId();

        //remove from map cache
        mappingAllInfo.getDao().removeSpecific(key, subKey);

        if (dsbe != null && mappingRecord != null) {
            //delete the entry from datastore backend too
            if (mappingRecord.getXtrId() != null) {
                dsbe.removeXtrIdMapping(DSBEInputUtil.toXtrIdMapping(mappingRecord));
            }
            if (key instanceof  Eid) {
                dsbe.removeMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, (Eid) key, siteId, mappingRecord));
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
