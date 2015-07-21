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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;
import org.opendaylight.lispflowmapping.implementation.lisp.AbstractLispComponent;

public class DAOMappingUtil {

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
        LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
        if (iterateMask && result.isEmpty() && MaskUtil.isMaskable(eid)) {
            result = findMaskLocators(dao, key);
        }
        return result;
    }

    private static List<MappingServiceRLOCGroup> aggregateLocators(Map<String, ?> locators) {
        List<MappingServiceRLOCGroup> result = new ArrayList<MappingServiceRLOCGroup>();
        if (locators != null) {
            for (Object value : locators.values()) {
                if (value != null && value instanceof MappingServiceRLOCGroup) {
                    result.add((MappingServiceRLOCGroup) value);
                }
            }
        }
        return result;
    }

    public static Object getLocatorsSpecific(IMappingServiceKey key, ILispDAO dao, String subkey, boolean iterateMask) {
        Object locators = dao.getSpecific(key, subkey);
        LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
        if (iterateMask && locators == null && MaskUtil.isMaskable(eid)) {
            locators = findMaskLocatorsSpecific(key, dao, subkey);
        }
        return locators;
    }

    private static Object findMaskLocatorsSpecific(IMappingServiceKey key, ILispDAO dao, String subkey) {
        int mask = key.getMask();
        while (mask > 0) {
            LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    LispAFIConvertor.toContainer(MaskUtil.normalize(eid, mask)), mask);
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
            LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
            key = MappingServiceKeyUtil.generateMappingServiceKey(
                    LispAFIConvertor.toContainer(MaskUtil.normalize(eid, mask)), mask);
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

    public static Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> getMapping(LispAFIAddress srcEid, EidRecord eid,
            ILispDAO dao) {
        // a map-request for an actual SrcDst LCAF, ignore src eid
        if (eid.getLispAddressContainer().getAddress() instanceof LcafSourceDest) {
            LcafSourceDestAddr eidAddr = ((LcafSourceDest) eid.getLispAddressContainer().getAddress()).getLcafSourceDestAddr();
            LispAFIAddress srcAddr = LispAFIConvertor.toAFIfromPrimitive(eidAddr.getSrcAddress().getPrimitiveAddress());
            LispAFIAddress dstAddr = LispAFIConvertor.toAFIfromPrimitive(eidAddr.getDstAddress().getPrimitiveAddress());
            return getMapping(srcAddr, dstAddr, eidAddr.getSrcMaskLength(), eidAddr.getDstMaskLength(), dao);
        }

        // potential map-request for SrcDst LCAF from non SrcDst capable devices
        Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> mapping = getMapping(srcEid,
                LispAFIConvertor.toAFI(eid.getLispAddressContainer()), (short) MaskUtil.getMaxMask(srcEid), eid.getMask(), dao);

        // if indeed SrcDst LCAF change the key to matched dst eid
        if (mapping.getKey().getEID().getAddress() instanceof LcafSourceDest) {
            LcafSourceDestAddr srcDst = ((LcafSourceDest)mapping.getKey().getEID().getAddress()).getLcafSourceDestAddr();
            IMappingServiceKey newKey = MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.
                    toAFIfromPrimitive(srcDst.getDstAddress().getPrimitiveAddress()), srcDst.getDstMaskLength());
            return new AbstractMap.SimpleImmutableEntry<>(newKey, mapping.getValue());
        }

        return mapping;
    }

    public static Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> getMapping(LispAFIAddress srcEid,
            LispAFIAddress dstEid, short srcMask, short dstMask, ILispDAO dao) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(dstEid, dstMask);
        Entry<IMappingServiceKey, Map<String, ?>> daoEntry = getDaoEntry(key, dao);
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.getValue().get(AbstractLispComponent.LCAF_SRCDST_SUBKEY);
            if (srcDstDao != null) {
                Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> mapping = getMappingForEid(srcEid, srcMask, srcDstDao);
                // if lookup fails, return whatever is found for dst eid
                if (mapping.getValue() != null) {
                    LispAFIAddress newDst = LispAFIConvertor.toAFI(daoEntry.getKey().getEID());
                    LispAFIAddress newSrc = LispAFIConvertor.toAFI(mapping.getKey().getEID());

                    LispAFIAddress newEid = new LcafSourceDestAddrBuilder()
                            .setDstAddress(new DstAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(newDst)).build())
                            .setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(newSrc)).build())
                            .setDstMaskLength((short)daoEntry.getKey().getMask()).setSrcMaskLength((short)mapping.getKey().getMask())
                            .setAfi((short)16387).setLcafType((short)12).build();
                    IMappingServiceKey newKey = MappingServiceKeyUtil.generateMappingServiceKey(newEid, dstMask);
                    return new AbstractMap.SimpleImmutableEntry<>(newKey, mapping.getValue());
                }
            }

            // dst eid lookup
            return makeMappingEntry(daoEntry.getKey(), daoEntry.getValue());
        }
        return new AbstractMap.SimpleImmutableEntry<>(key, null);
    }

    public static Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> getMappingExact(LispAFIAddress srcEid,
            LispAFIAddress dstEid, short srcMask, short dstMask, ILispDAO dao) {
        IMappingServiceKey dstKey = MappingServiceKeyUtil.generateMappingServiceKey(dstEid, dstMask);
        Map<String, ?> daoEntry = dao.get(dstKey);
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.get(AbstractLispComponent.LCAF_SRCDST_SUBKEY);
            // if lookup fails, return whatever is found for dst eid
            if (srcDstDao != null) {
                IMappingServiceKey srcKey = MappingServiceKeyUtil.generateMappingServiceKey(srcEid, srcMask);
                Map<String, ?> mapping = srcDstDao.get(srcKey);
                LispAFIAddress newEid = new LcafSourceDestAddrBuilder()
                        .setDstAddress(new DstAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(dstEid)).build())
                        .setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(srcEid)).build())
                        .setDstMaskLength((short)dstMask).setSrcMaskLength((short)srcMask)
                        .setAfi((short)16387).setLcafType((short)12).build();
                IMappingServiceKey newKey = MappingServiceKeyUtil.generateMappingServiceKey(newEid, dstMask);
                return makeMappingEntry(newKey, mapping);
            }

            // dst eid lookup
            return makeMappingEntry(dstKey, daoEntry);
        }
        return new AbstractMap.SimpleImmutableEntry<>(dstKey, null);
    }

    public static Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> getMappingForEidRecord(EidRecord eid, ILispDAO dao) {
        return getMappingForEid(LispAFIConvertor.toAFI(eid.getLispAddressContainer()), eid.getMask(), dao);
    }

    private static Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> makeMappingEntry(IMappingServiceKey key, Map<String, ?> locators) {
        if (locators != null) {
            List<MappingServiceRLOCGroup> locatorsList = aggregateLocators(locators);
            if (locatorsList != null && !locatorsList.isEmpty()) {
                return new AbstractMap.SimpleImmutableEntry<>(key, locatorsList);
            }
        }
        return new AbstractMap.SimpleImmutableEntry<>(key, null);
    }

    public static Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> getMappingForEid(LispAFIAddress eid,
            int maskLen, ILispDAO dao) {
        if (eid instanceof LcafSourceDestAddr) {
            LispAFIAddress srcAddr = LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddr) eid).getSrcAddress().getPrimitiveAddress());
            LispAFIAddress dstAddr = LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddr) eid).getDstAddress().getPrimitiveAddress());

            return getMapping(srcAddr, dstAddr, ((LcafSourceDestAddr) eid).getSrcMaskLength(),
                    ((LcafSourceDestAddr) eid).getDstMaskLength(), dao);
        }
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eid, maskLen);
        Entry<IMappingServiceKey, Map<String, ?>> entry = getDaoEntry(key, dao);
        if (entry == null) {
            return makeMappingEntry(key, null);
        } else {
            return makeMappingEntry(entry.getKey(), entry.getValue());
        }
    }

    public static Entry<IMappingServiceKey, Map<String, ?>> getDaoEntry(IMappingServiceKey lookupKey, ILispDAO dao) {
        LispAFIAddress eidAddress = LispAFIConvertor.toAFI(lookupKey.getEID());
        if (MaskUtil.isMaskable(eidAddress)) {
            IMappingServiceKey key;
            int mask = lookupKey.getMask();
            while (mask > 0) {
                key = MappingServiceKeyUtil.generateMappingServiceKey(eidAddress, mask);
                mask--;
                Map<String, ?> entry = dao.get(key);
                if (entry != null) {
                    return new AbstractMap.SimpleImmutableEntry<IMappingServiceKey, Map<String, ?>>(key, entry);
                }
            }
            return null;
        } else {
            Map<String, ?> entry = dao.get(lookupKey);
            if (entry != null) {
                return new AbstractMap.SimpleImmutableEntry<IMappingServiceKey, Map<String, ?>>(lookupKey,
                        dao.get(lookupKey));
            } else {
                return null;
            }
        }
    }
}
