/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.lispflowmapping.mapcache.ExtendedMappingRecord;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to implement merging of locator sets.
 *
 * @author Lorand Jakab
 *
 */
public final class MappingMergeUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingMergeUtil.class);

    // Utility class, should not be instantiated
    private MappingMergeUtil() {
    }

    private static void mergeCommonMappingRecordFields(MappingRecordBuilder mrb, MappingRecord record) {
        // Set xTR-ID and site-ID from the current mapping, it help with determining the timestamp
        mrb.setXtrId(record.getXtrId());
        mrb.setSiteId(record.getSiteId());
        // For the TTL value we take the minimum of all records
        mrb.setRecordTtl(Math.min(mrb.getRecordTtl(), record.getRecordTtl()));
        if (!mrb.getAction().equals(record.getAction())) {
            LOG.warn("Mapping merge operation: actions are different, which one is used is undefined");
        }
        if (mrb.isAuthoritative() != record.isAuthoritative()) {
            LOG.warn("Mapping merge operation: authoritative status is different, which one is used is undefined");
        }
        if (!mrb.getEid().equals(record.getEid())) {
            LOG.warn("Mapping merge operation: EID records are different, which one is used is undefined");
        }
    }

    private static LocatorRecord mergeLocators(LocatorRecord existingLocator, LocatorRecord newLocator) {
        if (existingLocator.isLocalLocator()) {
            return existingLocator;
        }
        return newLocator;
    }

    private static int compareLocators(LocatorRecord one, LocatorRecord two) {
        byte[] oneIp = LispAddressUtil.ipAddressToByteArray(one.getRloc().getAddress());
        byte[] twoIp = LispAddressUtil.ipAddressToByteArray(two.getRloc().getAddress());
        return LispAddressUtil.compareIpAddressByteArrays(oneIp, twoIp);
    }

    private static void mergeLocatorRecords(MappingRecordBuilder mrb, MappingRecord newRecord) {
        List<LocatorRecord> locators = mrb.getLocatorRecord();

        // We assume locators are unique and sorted and don't show up several times (with different or identical
        // p/w/mp/mw), so we create a LinkedHashMap (which preserves order) of the locators from the existing merged
        // record, keyed by the Rloc
        Map<Rloc, LocatorRecord> locatorMap = new LinkedHashMap<Rloc, LocatorRecord>();

        // All locators to be added to the merge set are first stored in this list
        List<LocatorRecord> newLocatorList = new ArrayList<LocatorRecord>();

        for (LocatorRecord locator : locators) {
            locatorMap.put(locator.getRloc(), locator);
        }
        for (LocatorRecord newLocator : newRecord.getLocatorRecord()) {
            Rloc newRloc = newLocator.getRloc();
            if (locatorMap.containsKey(newRloc)) {
                // overlapping locator
                if (locatorMap.get(newRloc).equals(newLocator)) {
                    continue;
                } else {
                    LocatorRecord mergedLocator = mergeLocators(locatorMap.get(newRloc), newLocator);
                    newLocatorList.add(mergedLocator);
                }
            } else {
                // new locator
                newLocatorList.add(newLocator);
            }
        }

        // Build new merged and sorted locator set if need be
        if (newLocatorList.size() != 0) {
            List<LocatorRecord> mergedLocators = new ArrayList<LocatorRecord>();

            int mlocIt = 0;
            int locIt = 0;
            while (mlocIt < newLocatorList.size() && locIt < locators.size()) {
                int cmp = compareLocators(locators.get(locIt), newLocatorList.get(mlocIt));
                if (cmp < 0) {
                    mergedLocators.add(locators.get(locIt));
                    locIt++;
                } else if (cmp > 0) {
                    mergedLocators.add(newLocatorList.get(mlocIt));
                    mlocIt++;
                } else {
                    // when a locator appears in both lists, keep the new (merged) one and skip the old
                    mergedLocators.add(newLocatorList.get(mlocIt));
                    mlocIt++;
                    locIt++;
                }
            }
            while (locIt < locators.size()) {
                mergedLocators.add(locators.get(locIt));
                locIt++;
            }
            while (mlocIt < newLocatorList.size()) {
                mergedLocators.add(newLocatorList.get(mlocIt));
                mlocIt++;
            }
            mrb.setLocatorRecord(mergedLocators);
        }
    }

    public static MappingRecord mergeMappings(MappingRecord currentMergedMapping, MappingRecord newMapping,
            XtrId xtrId, Date regdate) {
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

    public static ExtendedMappingRecord mergeXtrIdMappings(List<Object> extendedRecords, List<XtrId> expiredMappings,
            Set<IpAddressBinary> sourceRlocs) {
        MappingRecordBuilder mrb = null;
        XtrId xtrId = null;
        Long timestamp = Long.MAX_VALUE;

        for (int i = 0; i < extendedRecords.size(); i++) {
            ExtendedMappingRecord extendedRecord = (ExtendedMappingRecord) extendedRecords.get(i);
            MappingRecord record = extendedRecord.getRecord();

            // Skip expired mappings and add them to a list to be returned to the caller
            if (timestampIsExpired(extendedRecord.getTimestamp())) {
                expiredMappings.add(record.getXtrId());
                continue;
            }

            if (mrb == null) {
                mrb = new MappingRecordBuilder(record);
            }

            // Save the oldest valid timestamp
            if (extendedRecord.getTimestamp().getTime() < timestamp) {
                timestamp = extendedRecord.getTimestamp().getTime();
                xtrId = record.getXtrId();
            }

            // Merge record fields and locators
            mergeCommonMappingRecordFields(mrb, record);
            mergeLocatorRecords(mrb, record);

            // Save source locator for use in Map-Notify
            sourceRlocs.add(record.getSourceRloc());
        }

        if (mrb == null) {
            LOG.warn("All mappings expired when merging! Unexpected!");
            return null;
        }
        mrb.setXtrId(xtrId);

        return new ExtendedMappingRecord(mrb.build(), new Date(timestamp));
    }

    public static boolean mappingIsExpired(MappingRecord mapping) {
        Preconditions.checkNotNull(mapping, "mapping should not be null!");
        if (mapping.getTimestamp() != null) {
            return timestampIsExpired(mapping.getTimestamp());
        }
        return false;
    }

    public static boolean timestampIsExpired(Date timestamp) {
        Preconditions.checkNotNull(timestamp, "timestamp should not be null!");
        return timestampIsExpired(timestamp.getTime());
    }

    public static boolean timestampIsExpired(Long timestamp) {
        Preconditions.checkNotNull(timestamp, "timestamp should not be null!");
        if ((System.currentTimeMillis() - timestamp) > ConfigIni.getInstance().getRegistrationValiditySb() ) {
            return true;
        }
        return false;
    }

    public static MappingRecord computeNbSbIntersection(MappingRecord nbMapping, MappingRecord sbMapping) {
        // returns a MappingRecord which has the more specific EID, and intersection of locator records.
        // If locators intersection is empty, original NB mapping is returned.
        // The intersection is only computed for mappings with maskable EIDs.
        // Supports both maskable and non-maskable EIDs

        MappingRecordBuilder mrb = new MappingRecordBuilder(nbMapping);

        if (MaskUtil.isMaskable(sbMapping.getEid().getAddress())
                && MaskUtil.isMaskable(nbMapping.getEid().getAddress())) {

            short sbMask = MaskUtil.getMaskForAddress(sbMapping.getEid().getAddress());
            short nbMask = MaskUtil.getMaskForAddress(nbMapping.getEid().getAddress());

            if (nbMapping.getEid().getAddress() instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang
                    .ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey) {
                nbMask = SourceDestKeyHelper.getDstMask(nbMapping.getEid());
                if ( nbMask < sbMask) {
                    // We have to create a new SourceDest EID, where the source is same as the
                    // one in NB record, and dest EID is the more specific from SB mapping record.

                    SourceDestKey srcDstKey = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                            .address.types.rev151105.lisp.address.address.SourceDestKey) nbMapping.getEid()
                            .getAddress()).getSourceDestKey();
                    SourceDestKeyBuilder sdb = new SourceDestKeyBuilder(srcDstKey);
                    sdb.setDest(new SimpleAddress(getIpPrefix(sbMapping.getEid().getAddress())));
                    mrb.setEid(LispAddressUtil.asSrcDstEid(sdb.build(), nbMapping.getEid().getVirtualNetworkId()));
                }
            } else if (nbMask < sbMask) {
                // Both EIDs are IP prefixes. SB mapping is a subprefix so we have to update EID intersection
                mrb.setEid(sbMapping.getEid());
            }
        }
        // find and update locators intersection if not empty
        List<LocatorRecord> commonLocators = getCommonLocatorRecords(nbMapping, sbMapping);
        if (commonLocators != null && !commonLocators.isEmpty()) {
            mrb.setLocatorRecord(commonLocators);
        }

        return mrb.build();
    }

    private static List<LocatorRecord> getCommonLocatorRecords(MappingRecord nbMapping, MappingRecord sbMapping) {
        // This method updates the MappingRecord builder with the intersection of the locator records
        // from the two mappings. NB mapping records fields have precedence, only Priority is updated
        // from SB mapping if p is 255.

        // Return null when NB is a negative mapping
        if (nbMapping.getLocatorRecord() == null || nbMapping.getLocatorRecord().isEmpty()) {
            return null;
        }

        List<LocatorRecord> sbLocators = sbMapping.getLocatorRecord();

        // We assume locators are unique and don't show up several times (with different or identical p/w/mp/mw),
        // so we create a HashMap of the locators from the SB mapping record, keyed by the Rloc
        Map<Rloc, LocatorRecord> sbLocatorMap = new HashMap<Rloc, LocatorRecord>();
        for (LocatorRecord locator : sbLocators) {
            sbLocatorMap.put(locator.getRloc(), locator);
        }

        // Gradually building final list of common locators, in order that they appear in NB Mapping
        List<LocatorRecord> commonLocators = new ArrayList<LocatorRecord>();

        for (LocatorRecord nbLocator : nbMapping.getLocatorRecord()) {
            Rloc nbRloc = nbLocator.getRloc();
            if (sbLocatorMap.containsKey(nbRloc)) {
                // common locator found. use the NB record as the common locator.

                if (sbLocatorMap.get(nbRloc).getPriority() == (short) 255) {
                    // if SB locator has p == 255 then common locator takes all NB fields except for p
                    // which must be set to 255
                    LocatorRecordBuilder lrb = new LocatorRecordBuilder(nbLocator);
                    lrb.setPriority((short) 255);
                    commonLocators.add(lrb.build());
                } else {
                    commonLocators.add(nbLocator);
                }
            }
        }
        return commonLocators;
    }

    private static IpPrefix getIpPrefix(Address address) {
        IpPrefix ipPrefix = null;

        if (address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns
                .yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix) {
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp
                    .address.address.Ipv4Prefix lispPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params
                    .xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix) address;

            Ipv4Prefix inetPrefix = new Ipv4Prefix(lispPrefix.getIpv4Prefix());
            ipPrefix = new IpPrefix(inetPrefix);
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params
                .xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) {
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp
                    .address.address.Ipv6Prefix lispPrefix = (org.opendaylight.yang.gen.v1.urn.ietf.params
                    .xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) address;

            Ipv6Prefix inetPrefix = new Ipv6Prefix(lispPrefix.getIpv6Prefix());
            ipPrefix = new IpPrefix(inetPrefix);
        } else {
            LOG.warn("Southbound mapping address is not an IpPrefix");
        }
        return ipPrefix;
    }
}
