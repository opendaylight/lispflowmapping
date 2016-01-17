/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Utility class to implement merging of locator sets
 *
 * @author Lorand Jakab
 *
 */
public final class MappingMergeUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingMergeUtil.class);
    // A mapping registration is valid for this many milliseconds
    private static final long REGISTRATION_VALIDITY = 200000L;

    // Utility class, should not be instantiated
    private MappingMergeUtil() {
    }

    private static void mergeCommonMappingRecordFields(MappingRecordBuilder mrb, MappingRecord record) {
        // Set xTR-ID and site-ID from the current mapping, it help with determining the timestamp
        mrb.setXtrId(record.getXtrId());
        mrb.setSiteId(record.getSiteId());
        // For the TTL value we take the minimum of all records
        mrb.setRecordTtl(Math.min(mrb.getRecordTtl(), record.getRecordTtl()));
        if  (!mrb.getAction().equals(record.getAction())) {
            LOG.warn("Mapping merge operation: actions are different, which one is used is undefined");
        }
        if  (mrb.isAuthoritative() != record.isAuthoritative()) {
            LOG.warn("Mapping merge operation: authoritative status is different, which one is used is undefined");
        }
        if  (!mrb.getEid().equals(record.getEid())) {
            LOG.warn("Mapping merge operation: EID records are different, which one is used is undefined");
        }
    }

    private static LocatorRecord mergeLocators(LocatorRecord existingLocator, LocatorRecord newLocator) {
        if (existingLocator.isLocalLocator()) {
            return existingLocator;
        }
        return newLocator;
    }

    private static void mergeLocatorRecords(MappingRecordBuilder mrb, MappingRecord newRecord) {
        List<LocatorRecord> locators = mrb.getLocatorRecord();

        // Optimization: unless we had to merge any of the locators due to differences in weights, etc, we return
        // the original 'locators' List with any new locators added
        boolean mergeHappened = false;

        // We assume locators are unique and don't show up several times (with different or identical p/w/mp/mw),
        // so we create a LinkedHashMap (which preserves order) of the locators from the existing merged record,
        // keyed by the Rloc
        Map<Rloc, LocatorRecord> locatorMap = new LinkedHashMap<Rloc, LocatorRecord>();
        for (LocatorRecord locator : locators) {
            locatorMap.put(locator.getRloc(), locator);
        }
        for (LocatorRecord newLocator : newRecord.getLocatorRecord()) {
            Rloc newRloc = newLocator.getRloc();
            if (locatorMap.containsKey(newRloc)) {
                // XXX  LocatorRecord YANG generated class doesn't override equals() so I'm not sure of the behavior
                // here, need to verify if it works as expected
                if (locatorMap.get(newRloc).equals(newLocator)) {
                    continue;
                } else {
                    LocatorRecord mergedLocator = mergeLocators(locatorMap.get(newRloc), newLocator);
                    locatorMap.put(newRloc, mergedLocator);
                    mergeHappened = true;
                }
            } else {
                // We add both the LinkedHanshMap and the List, in case we can return the original list plus new
                // elements and need not generate a new list with merged locators (which should be the most common
                // scenario).
                locatorMap.put(newRloc, newLocator);
                locators.add(newLocator);
            }
        }

        if (mergeHappened) {
            List<LocatorRecord> mergedLocators = new ArrayList<LocatorRecord>();
            for (Map.Entry<Rloc, LocatorRecord> entry : locatorMap.entrySet()) {
                mergedLocators.add(entry.getValue());
            }
            mrb.setLocatorRecord(mergedLocators);
        } else {
            // TODO  Check if this is necessary after and .add() was called on locators
            mrb.setLocatorRecord(locators);
        }
    }

    public static MappingRecord mergeMappings(MappingRecord currentMergedMapping, MappingRecord newMapping,
            byte[] xtrId, Date regdate) {
        if (currentMergedMapping == null) {
            return newMapping;
        }

        MappingRecordBuilder mrb = new MappingRecordBuilder(currentMergedMapping);
        mergeCommonMappingRecordFields(mrb, newMapping);
        mergeLocatorRecords(mrb, newMapping);

        if (xtrId != null) {
            mrb.setXtrId(xtrId);
            mrb.setTimestamp(regdate.getTime());
        }

        return mrb.build();
    }

    public static SimpleImmutableEntry<MappingRecord, List<byte[]>> mergeXtrIdMappings(List<Object> records) {
        List<byte[]> expiredMappings = new ArrayList<byte[]>();
        MappingRecordBuilder mrb = new MappingRecordBuilder((MappingRecord) records.get(0));
        byte[] xtrId = mrb.getXtrId();
        Long timestamp = mrb.getTimestamp();

        for (int i = 1; i < records.size(); i++) {
            MappingRecord record = (MappingRecord) records.get(i);

            // Skip expired mappings and add them to a list to be returned to the caller
            if (timestampIsExpired(record.getTimestamp())) {
                expiredMappings.add(record.getXtrId());
                continue;
            }

            // Save the oldest valid timestamp
            if (record.getTimestamp() < timestamp) {
                timestamp = record.getTimestamp();
                xtrId = record.getXtrId();
            }

            // Merge record fields and locators
            mergeCommonMappingRecordFields(mrb, record);
            mergeLocatorRecords(mrb, record);
        }
        mrb.setXtrId(xtrId);
        mrb.setTimestamp(timestamp);

        return new SimpleImmutableEntry<MappingRecord, List<byte[]>>(mrb.build(), expiredMappings);
    }

    public static boolean timestampIsExpired(Date timestamp) {
        Preconditions.checkNotNull(timestamp, "timestamp should not be null!");
        return timestampIsExpired(timestamp.getTime());
    }

    public static boolean timestampIsExpired(Long timestamp) {
        Preconditions.checkNotNull(timestamp, "timestamp should not be null!");
        if ((System.currentTimeMillis() - timestamp) > REGISTRATION_VALIDITY) {
            return true;
        }
        return false;
    }
}
