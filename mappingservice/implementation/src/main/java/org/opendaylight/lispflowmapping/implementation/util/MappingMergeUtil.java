/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to implement merging of locator sets
 *
 * @author Lorand Jakab
 *
 */
public final class MappingMergeUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingMergeUtil.class);
    private static final short DO_NOT_USE_RLOC = 255;

    // Utility class, should not be instantiated
    private MappingMergeUtil() {
    }

    private static void mergeCommonMappingRecordFields(MappingRecordBuilder mrb, MappingRecord record) {
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
        // For performance reasons we only check if the unicast priority of any of the locators is 255, in which case
        // we use the new (current, last registered) locator. We don't need to build a new LocatorRecord this way.
        if (newLocator.getPriority() == DO_NOT_USE_RLOC || existingLocator.getPriority() == DO_NOT_USE_RLOC) {
            return newLocator;
        }
        // TODO figure out what to do with the "local" bit, which is the only one that might matter for merging(?)
        return existingLocator;
    }

    private static void mergeLocatorRecords(MappingRecordBuilder mrb, MappingRecord record) {
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
        for (LocatorRecord locator : record.getLocatorRecord()) {
            Rloc rloc = locator.getRloc();
            if (locatorMap.containsKey(rloc)) {
                if (locatorMap.get(rloc).equals(locator)) {
                    continue;
                } else {
                    LocatorRecord mergedLocator = mergeLocators(locatorMap.get(rloc), locator);
                    locatorMap.put(rloc, mergedLocator);
                    mergeHappened = true;
                }
            } else {
                // We add both the LinkedHanshMap and the List, in case we can return the original list plus new
                // elements and need not generate a new list with merged locators (which should be the most common
                // scenario).
                locatorMap.put(rloc, locator);
                locators.add(locator);
            }
        }

        if (mergeHappened) {
            List<LocatorRecord> mergedLocators = new ArrayList<LocatorRecord>();
            for (Map.Entry<Rloc, LocatorRecord> entry : locatorMap.entrySet()) {
                mergedLocators.add(entry.getValue());
            }
            mrb.setLocatorRecord(mergedLocators);
        } else {
            mrb.setLocatorRecord(locators);
        }
    }

    public static MappingRecord mergeMappings(MappingRecord currentMergedMapping, MappingRecord newMapping) {
        if (currentMergedMapping == null) {
            return newMapping;
        }

        MappingRecordBuilder mrb = new MappingRecordBuilder(currentMergedMapping);
        mergeCommonMappingRecordFields(mrb, newMapping);
        mergeLocatorRecords(mrb, newMapping);
        return mrb.build();
    }

    public static MappingRecord mergeXtrIdMappings(List<Object> records) {
        MappingRecordBuilder mrb = new MappingRecordBuilder((MappingRecord) records.get(0));
        for (int i = 1; i < records.size(); i++) {
            MappingRecord record = (MappingRecord) records.get(i);
            mergeCommonMappingRecordFields(mrb, record);
            mergeLocatorRecords(mrb, record);
        }
        return mrb.build();
    }
}
