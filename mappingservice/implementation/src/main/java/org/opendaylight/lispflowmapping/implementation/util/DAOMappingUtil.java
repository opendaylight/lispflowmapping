/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOCGroup;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOMappingUtil {
    protected static final Logger logger = LoggerFactory.getLogger(DAOMappingUtil.class);

    public static List<MappingServiceRLOCGroup> getLocatorsByEidRecord(EidRecord eid, ILispDAO dao, boolean iterateMask) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        return getLocators(key, dao, iterateMask);
    }

    public static Object getLocatorsSpecificByEidRecord(EidRecord eid,  ILispDAO dao, String subkey, boolean iterateMask) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        return getLocatorsSpecific(key, dao, subkey, iterateMask);
    }

    public static List<MappingServiceRLOCGroup> getLocatorsByEidToLocatorRecord(EidToLocatorRecord eid, ILispDAO dao, boolean iterateMask) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMaskLength());
        return getLocators(key, dao, iterateMask);
    }

    public static Object getLocatorsSpecificByEidtoLocatorRecord(EidToLocatorRecord eid,  ILispDAO dao, String subkey, boolean iterateMask) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMaskLength());
        return getLocatorsSpecific(key, dao, subkey, iterateMask);
    }

    public static List<MappingServiceRLOCGroup> getLocators(IMappingServiceKey key, ILispDAO dao, boolean iterateMask) {
        Map<String, ?> locators = dao.get(key);
        List<MappingServiceRLOCGroup> result = aggregateLocators(locators);
        if (iterateMask && result.isEmpty() && MaskUtil.isMaskable(key.getEID().getAddress())) {
            result = findMaskLocators(dao, key);
        }
        return result;
    }

    private static List<MappingServiceRLOCGroup> aggregateLocators(Map<String, ?> locators) {
        List<MappingServiceRLOCGroup> result = new ArrayList<MappingServiceRLOCGroup>();
        if (locators != null) {
            for (Object value : locators.values()) {
                if (value != null && value instanceof MappingServiceRLOCGroup) {
                    if (!((MappingServiceRLOCGroup) value).getRecords().isEmpty()) {
                        result.add((MappingServiceRLOCGroup) value);
                    }
                }
            }
        }
        return result;
    }

    public static Object getLocatorsSpecific(IMappingServiceKey key, ILispDAO dao, String subkey, boolean iterateMask) {
        Object locators = dao.getSpecific(key, subkey);
        if (iterateMask && locators == null && MaskUtil.isMaskable(key.getEID().getAddress())) {
            locators = findMaskLocatorsSpecific(key, dao, subkey);
        }
        return locators;
    }

    private static Object findMaskLocatorsSpecific(IMappingServiceKey key, ILispDAO dao, String subkey) {
        int mask = key.getMask();
        while (mask > 0) {
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    new LispAddressContainerBuilder().setAddress(MaskUtil.normalize(key.getEID().getAddress(), mask)).build(), mask);
            mask--;
            Object locators = dao.getSpecific(key, subkey);
            if (locators != null) {
                return locators;
            }
        }
        return null;
    }

    private static List<MappingServiceRLOCGroup> findMaskLocators(ILispDAO dao, IMappingServiceKey key) {
        int mask = key.getMask();
        while (mask > 0) {
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    new LispAddressContainerBuilder().setAddress(MaskUtil.normalize(key.getEID().getAddress(), mask)).build(), mask);
            mask--;
            Map<String, ?> locators = dao.get(key);
            if (locators != null) {
                List<MappingServiceRLOCGroup> result = aggregateLocators(locators);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }
        return null;
    }

    public static Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> getMappingForEidRecord(EidRecord eid, ILispDAO dao) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        int mask = eid.getMask();
        while (mask > 0) {
            Address eidAddress = MaskUtil.normalize(key.getEID().getAddress(), mask);
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    new LispAddressContainerBuilder().setAddress(eidAddress).build(), mask);
            mask--;
            Map<String, ?> locators = dao.get(key);
            if (locators != null) {
                List<MappingServiceRLOCGroup> locatorsList = aggregateLocators(locators);
                if (locatorsList != null && !locatorsList.isEmpty()) {
                    Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> result = new AbstractMap.SimpleImmutableEntry<>(key, locatorsList);
                    return result;
                }
            }
        }
        // empty mapping
        key = MappingServiceKeyUtil.generateMappingServiceKey(eid.getLispAddressContainer(), eid.getMask());
        return new AbstractMap.SimpleImmutableEntry<>(key, null);
    }
}
