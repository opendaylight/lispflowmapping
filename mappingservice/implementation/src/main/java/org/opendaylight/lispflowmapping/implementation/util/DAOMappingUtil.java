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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.opendaylight.lispflowmapping.implementation.dao.MappingKeyUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.RLOCGroup;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;
import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOMappingUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DAOMappingUtil.class);

    public static List<RLOCGroup> getLocatorsByEidRecord(EidRecord eid, ILispDAO dao, boolean iterateMask) {
        IMappingKey key = MappingKeyUtil.generateMappingKey(eid.getLispAddressContainer(), eid.getMask());
        return getLocators(key, dao, iterateMask);
    }

    public static Object getLocatorsSpecificByEidRecord(EidRecord eid, ILispDAO dao, String subkey,
            boolean iterateMask) {
        IMappingKey key = MappingKeyUtil.generateMappingKey(eid.getLispAddressContainer(), eid.getMask());
        return getLocatorsSpecific(key, dao, subkey, iterateMask);
    }

    public static List<RLOCGroup> getLocatorsByEidToLocatorRecord(EidToLocatorRecord eid, ILispDAO dao,
            boolean iterateMask) {
        IMappingKey key = MappingKeyUtil.generateMappingKey(eid.getLispAddressContainer(), eid.getMaskLength());
        return getLocators(key, dao, iterateMask);
    }

    public static Object getLocatorsSpecificByEidtoLocatorRecord(EidToLocatorRecord eid, ILispDAO dao, String subkey,
            boolean iterateMask) {
        IMappingKey key = MappingKeyUtil.generateMappingKey(eid.getLispAddressContainer(), eid.getMaskLength());
        return getLocatorsSpecific(key, dao, subkey, iterateMask);
    }

    public static List<RLOCGroup> getLocators(IMappingKey key, ILispDAO dao, boolean iterateMask) {
        Map<String, ?> locators = dao.get(key);
        List<RLOCGroup> result = aggregateLocators(locators);
        LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
        if (iterateMask && result.isEmpty() && MaskUtil.isMaskable(eid)) {
            result = findMaskLocators(dao, key);
        }
        return result;
    }

    private static List<RLOCGroup> aggregateLocators(Map<String, ?> locators) {
        List<RLOCGroup> result = new ArrayList<RLOCGroup>();
        if (locators != null) {
            for (Object value : locators.values()) {
                if (value != null && value instanceof RLOCGroup) {
                    result.add((RLOCGroup) value);
                }
            }
        }
        return result;
    }

    public static Object getLocatorsSpecific(IMappingKey key, ILispDAO dao, String subkey, boolean iterateMask) {
        Object locators = dao.getSpecific(key, subkey);
        LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
        if (iterateMask && locators == null && MaskUtil.isMaskable(eid)) {
            locators = findMaskLocatorsSpecific(key, dao, subkey);
        }
        return locators;
    }

    private static Object findMaskLocatorsSpecific(IMappingKey key, ILispDAO dao, String subkey) {
        int mask = key.getMask();
        while (mask > 0) {
            LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
            key = MappingKeyUtil.generateMappingKey(LispAFIConvertor.toContainer(MaskUtil.normalize(eid, mask)), mask);
            mask--;
            Object locators = dao.getSpecific(key, subkey);
            if (locators != null) {
                return locators;
            }
        }
        return null;
    }

    private static List<RLOCGroup> findMaskLocators(ILispDAO dao, IMappingKey key) {
        int mask = key.getMask();
        while (mask > 0) {
            LispAFIAddress eid = LispAFIConvertor.toAFI(key.getEID());
            key = MappingKeyUtil.generateMappingKey(LispAFIConvertor.toContainer(MaskUtil.normalize(eid, mask)), mask);
            mask--;
            Map<String, ?> locators = dao.get(key);
            if (locators != null) {
                List<RLOCGroup> result = aggregateLocators(locators);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }
        return null;
    }

    public static Entry<IMappingKey, List<RLOCGroup>> getMapping(LispAFIAddress srcEid, EidRecord eidRecord,
            ILispDAO dao) {
        // a map-request for an actual SrcDst LCAF, ignore src eid
        if (eidRecord.getLispAddressContainer().getAddress() instanceof LcafSourceDest) {
            LcafSourceDestAddr eid = ((LcafSourceDest) eidRecord.getLispAddressContainer().getAddress())
                    .getLcafSourceDestAddr();
            LispAFIAddress srcAddr = LispAFIConvertor.toAFIfromPrimitive(eid.getSrcAddress().getPrimitiveAddress());
            LispAFIAddress dstAddr = LispAFIConvertor.toAFIfromPrimitive(eid.getDstAddress().getPrimitiveAddress());
            return getMapping(srcAddr, dstAddr, eid.getSrcMaskLength(), eid.getDstMaskLength(), dao);
        }

        // potential map-request for SrcDst LCAF from non SrcDst capable devices
        Entry<IMappingKey, List<RLOCGroup>> mapping = getMapping(srcEid,
                LispAFIConvertor.toAFI(eidRecord.getLispAddressContainer()), (short) MaskUtil.getMaxMask(srcEid),
                eidRecord.getMask(), dao);

        // if indeed SrcDst LCAF change the key to matched dst eid
        if (mapping.getKey().getEID().getAddress() instanceof LcafSourceDest) {
            LcafSourceDestAddr srcDst = ((LcafSourceDest) mapping.getKey().getEID().getAddress())
                    .getLcafSourceDestAddr();
            IMappingKey newKey = MappingKeyUtil.generateMappingKey(
                    LispAFIConvertor.toAFIfromPrimitive(srcDst.getDstAddress().getPrimitiveAddress()),
                    srcDst.getDstMaskLength());
            return new AbstractMap.SimpleImmutableEntry<>(newKey, mapping.getValue());
        }

        return mapping;
    }

    public static Entry<IMappingKey, List<RLOCGroup>> getMapping(LispAFIAddress srcEid, LispAFIAddress dstEid,
            short srcMask, short dstMask, ILispDAO dao) {
        IMappingKey key = MappingKeyUtil.generateMappingKey(dstEid, dstMask);
        Entry<IMappingKey, Map<String, ?>> daoEntry = getDaoEntry(key, dao);
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.getValue().get(DAOSubKeys.LCAF_SRCDST_SUBKEY.toString());
            if (srcDstDao != null) {
                Entry<IMappingKey, List<RLOCGroup>> mapping = getMappingForEid(srcEid, srcMask, srcDstDao);
                // if lookup fails, return whatever is found for dst eid
                if (mapping.getValue() != null) {
                    LispAFIAddress newDst = LispAFIConvertor.toAFI(daoEntry.getKey().getEID());
                    LispAFIAddress newSrc = LispAFIConvertor.toAFI(mapping.getKey().getEID());

                    LispAFIAddress newEid = new LcafSourceDestAddrBuilder()
                            .setDstAddress(
                                    new DstAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(newDst))
                                            .build())
                            .setSrcAddress(
                                    new SrcAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(newSrc))
                                            .build()).setDstMaskLength((short) daoEntry.getKey().getMask())
                            .setSrcMaskLength((short) mapping.getKey().getMask()).setAfi((short) 16387)
                            .setLcafType((short) 12).build();
                    IMappingKey newKey = MappingKeyUtil.generateMappingKey(newEid, dstMask);
                    return new AbstractMap.SimpleImmutableEntry<>(newKey, mapping.getValue());
                }
            }

            // dst eid lookup
            return makeMappingEntry(daoEntry.getKey(), daoEntry.getValue());
        }
        return new AbstractMap.SimpleImmutableEntry<>(key, null);
    }

    public static Entry<IMappingKey, List<RLOCGroup>> getMappingExact(LispAFIAddress srcEid, LispAFIAddress dstEid,
            short srcMask, short dstMask, ILispDAO dao) {
        IMappingKey dstKey = MappingKeyUtil.generateMappingKey(dstEid, dstMask);
        Map<String, ?> daoEntry = dao.get(dstKey);
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.get(DAOSubKeys.LCAF_SRCDST_SUBKEY.toString());
            // if lookup fails, return whatever is found for dst eid
            if (srcDstDao != null) {
                IMappingKey srcKey = MappingKeyUtil.generateMappingKey(srcEid, srcMask);
                Map<String, ?> mapping = srcDstDao.get(srcKey);
                LispAFIAddress newEid = new LcafSourceDestAddrBuilder()
                        .setDstAddress(
                                new DstAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(dstEid))
                                        .build())
                        .setSrcAddress(
                                new SrcAddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(srcEid))
                                        .build()).setDstMaskLength((short) dstMask).setSrcMaskLength((short) srcMask)
                        .setAfi((short) 16387).setLcafType((short) 12).build();
                IMappingKey newKey = MappingKeyUtil.generateMappingKey(newEid, dstMask);
                return makeMappingEntry(newKey, mapping);
            }

            // dst eid lookup
            return makeMappingEntry(dstKey, daoEntry);
        }
        return new AbstractMap.SimpleImmutableEntry<>(dstKey, null);
    }

    public static Entry<IMappingKey, List<RLOCGroup>> getMappingForEidRecord(EidRecord eid, ILispDAO dao) {
        return getMappingForEid(LispAFIConvertor.toAFI(eid.getLispAddressContainer()), eid.getMask(), dao);
    }

    private static Entry<IMappingKey, List<RLOCGroup>> makeMappingEntry(IMappingKey key, Map<String, ?> locators) {
        if (locators != null) {
            List<RLOCGroup> locatorsList = aggregateLocators(locators);
            if (locatorsList != null && !locatorsList.isEmpty()) {
                return new AbstractMap.SimpleImmutableEntry<>(key, locatorsList);
            }
        }
        return new AbstractMap.SimpleImmutableEntry<>(key, null);
    }

    public static Entry<IMappingKey, List<RLOCGroup>> getMappingForEid(LispAFIAddress eid, int maskLen, ILispDAO dao) {
        if (eid instanceof LcafSourceDestAddr) {
            LispAFIAddress srcAddr = LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddr) eid).getSrcAddress()
                    .getPrimitiveAddress());
            LispAFIAddress dstAddr = LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDestAddr) eid).getDstAddress()
                    .getPrimitiveAddress());

            return getMapping(srcAddr, dstAddr, ((LcafSourceDestAddr) eid).getSrcMaskLength(),
                    ((LcafSourceDestAddr) eid).getDstMaskLength(), dao);
        }
        IMappingKey key = MappingKeyUtil.generateMappingKey(eid, maskLen);
        Entry<IMappingKey, Map<String, ?>> entry = getDaoEntry(key, dao);
        if (entry == null) {
            return makeMappingEntry(key, null);
        } else {
            return makeMappingEntry(entry.getKey(), entry.getValue());
        }
    }

    public static Entry<IMappingKey, Map<String, ?>> getDaoEntry(IMappingKey lookupKey, ILispDAO dao) {
        LispAFIAddress eidAddress = LispAFIConvertor.toAFI(lookupKey.getEID());
        if (MaskUtil.isMaskable(eidAddress)) {
            IMappingKey key;
            int mask = lookupKey.getMask();
            while (mask > 0) {
                key = MappingKeyUtil.generateMappingKey(eidAddress, mask);
                mask--;
                Map<String, ?> entry = dao.get(key);
                if (entry != null) {
                    return new AbstractMap.SimpleImmutableEntry<IMappingKey, Map<String, ?>>(key, entry);
                }
            }
            return null;
        } else {
            Map<String, ?> entry = dao.get(lookupKey);
            if (entry != null) {
                return new AbstractMap.SimpleImmutableEntry<IMappingKey, Map<String, ?>>(lookupKey,
                        dao.get(lookupKey));
            } else {
                return null;
            }
        }
    }

    public static void addSubscribers(LispAddressContainer eid, int mask, Set<SubscriberRLOC> subscribers,
            ILispDAO dao) {
        IMappingKey key = MappingKeyUtil.generateMappingKey(eid, mask);
        dao.put(key, new MappingEntry<Set<SubscriberRLOC>>(DAOSubKeys.SUBSCRIBERS_SUBKEY.toString(), subscribers));
    }

    public static void addAuthenticationKey(LispAddressContainer address, int maskLen, String key, ILispDAO dao) {
        IMappingKey mappingServiceKey = MappingKeyUtil.generateMappingKey(address, maskLen);
        if (address.getAddress() instanceof LcafSourceDest) {
            IMappingKey srcKey = MappingKeyUtil.generateMappingKey(getSrcForLcafSrcDst(address),
                    getSrcMaskForLcafSrcDst(address));
            ILispDAO srcDstDao = getOrInstantiateSrcDstInnerDao(address, maskLen, dao);
            srcDstDao.put(srcKey, new MappingEntry<String>(DAOSubKeys.PASSWORD_SUBKEY.toString(), key));
        } else {
            dao.put(mappingServiceKey, new MappingEntry<String>(DAOSubKeys.PASSWORD_SUBKEY.toString(), key));
        }
    }

    public static String getPasswordForMaskable(LispAddressContainer prefix, int maskLength, ILispDAO db,
            boolean shouldIterate) {
        while (maskLength >= 0) {
            IMappingKey key = MappingKeyUtil.generateMappingKey(prefix, maskLength);
            Object password = db.getSpecific(key, DAOSubKeys.PASSWORD_SUBKEY.toString());
            if (password != null && password instanceof String) {
                return (String) password;
            } else if (shouldIterate) {
                maskLength -= 1;
            } else {
                LOG.warn("Failed to find password!");
                return null;
            }
        }
        return null;
    }

    public static String getPassword(LispAddressContainer prefix, int maskLength, ILispDAO dao, boolean iterate) {
        if (MaskUtil.isMaskable(LispAFIConvertor.toAFI(prefix))) {
            return getPasswordForMaskable(prefix, maskLength, dao, iterate);
        } else if (prefix.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSrcDstInnerDao(prefix, maskLength, dao);
            if (srcDstDao != null) {
                return getPasswordForMaskable(LispAFIConvertor.toContainer(getSrcForLcafSrcDst(prefix)),
                        getSrcMaskForLcafSrcDst(prefix), srcDstDao, iterate);
            }
            return null;
        } else {
            IMappingKey key = MappingKeyUtil.generateMappingKey(prefix, maskLength);
            Object password = dao.getSpecific(key, DAOSubKeys.PASSWORD_SUBKEY.toString());
            if (password != null && password instanceof String) {
                return (String) password;
            } else {
                LOG.warn("Failed to find password!");
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<SubscriberRLOC> getSubscribers(LispAddressContainer prefix, int maskLength, ILispDAO dao) {
        Object subscribers;
        if (prefix.getAddress() instanceof LcafSourceDest) {
            IMappingKey srcKey = MappingKeyUtil.generateMappingKey(getSrcForLcafSrcDst(prefix),
                    getSrcMaskForLcafSrcDst(prefix));
            ILispDAO srcDstDao = getSrcDstInnerDao(prefix, maskLength, dao);
            subscribers = srcDstDao.getSpecific(srcKey, DAOSubKeys.SUBSCRIBERS_SUBKEY.toString());
        } else {
            IMappingKey key = MappingKeyUtil.generateMappingKey(prefix, maskLength);
            subscribers = dao.getSpecific(key, DAOSubKeys.SUBSCRIBERS_SUBKEY.toString());
        }

        if (subscribers != null && subscribers instanceof Set<?>) {
            return (Set<SubscriberRLOC>) subscribers;
        }
        return null;
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over
    // src. This method returns the DAO
    // associated to a dst or creates it if it doesn't exist.
    public static ILispDAO getOrInstantiateSrcDstInnerDao(LispAddressContainer address, int maskLen, ILispDAO dao) {
        IMappingKey dstKey = MappingKeyUtil.generateMappingKey(getDstForLcafSrcDst(address),
                getDstMaskForLcafSrcDst(address));
        ILispDAO srcDstDao = (ILispDAO) dao.getSpecific(dstKey, DAOSubKeys.LCAF_SRCDST_SUBKEY.toString());
        if (srcDstDao == null) {
            srcDstDao = new HashMapDb();
            dao.put(dstKey, new MappingEntry<>(DAOSubKeys.LCAF_SRCDST_SUBKEY.toString(), srcDstDao));
        }
        return srcDstDao;
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over
    // src. This method returns the DAO
    // associated to a dst or null if it doesn't exist.
    public static ILispDAO getSrcDstInnerDao(LispAddressContainer address, int maskLen, ILispDAO dao) {
        IMappingKey dstKey = MappingKeyUtil.generateMappingKey(getDstForLcafSrcDst(address),
                getDstMaskForLcafSrcDst(address));
        return (ILispDAO) dao.getSpecific(dstKey, DAOSubKeys.LCAF_SRCDST_SUBKEY.toString());
    }

    public static boolean saveRlocs(EidToLocatorRecord eidRecord, boolean checkForChanges, ILispDAO dao,
            boolean shouldIterate, boolean shouldOverwrite) {
        Map<String, RLOCGroup> rlocGroups = new HashMap<String, RLOCGroup>();
        if (eidRecord.getLocatorRecord() != null) {
            for (LocatorRecord locatorRecord : eidRecord.getLocatorRecord()) {
                String subkey = getAddressKey(locatorRecord.getLispAddressContainer().getAddress(), shouldOverwrite);
                if (!rlocGroups.containsKey(subkey)) {
                    rlocGroups
                            .put(subkey,
                                    new RLOCGroup(eidRecord.getRecordTtl(), eidRecord.getAction(), eidRecord
                                            .isAuthoritative()));
                }
                rlocGroups.get(subkey).addRecord(locatorRecord);
            }
        } else {
            rlocGroups.put(DAOSubKeys.ADDRESS_SUBKEY.toString(),
                    new RLOCGroup(eidRecord.getRecordTtl(), eidRecord.getAction(), eidRecord.isAuthoritative()));
        }
        List<MappingEntry<RLOCGroup>> entries = new ArrayList<>();
        for (String subkey : rlocGroups.keySet()) {
            entries.add(new MappingEntry<>(subkey, rlocGroups.get(subkey)));
        }

        if (eidRecord.getLispAddressContainer().getAddress() instanceof LcafSourceDest) {
            Entry<IMappingKey, List<RLOCGroup>> oldMapping = null, newMapping = null;
            LispAFIAddress srcAddr = getSrcForLcafSrcDst(eidRecord.getLispAddressContainer());
            LispAFIAddress dstAddr = getDstForLcafSrcDst(eidRecord.getLispAddressContainer());
            short srcMask = getSrcMaskForLcafSrcDst(eidRecord.getLispAddressContainer());
            short dstMask = getDstMaskForLcafSrcDst(eidRecord.getLispAddressContainer());

            if (checkForChanges) {
                oldMapping = DAOMappingUtil.getMappingExact(srcAddr, dstAddr, srcMask, dstMask, dao);
            }
            IMappingKey dstKey = MappingKeyUtil.generateMappingKey(dstAddr, dstMask);
            ILispDAO srcDstDao = (ILispDAO) dao.getSpecific(dstKey, DAOSubKeys.LCAF_SRCDST_SUBKEY.toString());
            if (srcDstDao == null) {
                srcDstDao = new HashMapDb();
                dao.put(dstKey, new MappingEntry<>(DAOSubKeys.LCAF_SRCDST_SUBKEY.toString(), srcDstDao));
            }
            IMappingKey srcKey = MappingKeyUtil.generateMappingKey(srcAddr, srcMask);
            srcDstDao.put(srcKey, entries.toArray(new MappingEntry[entries.size()]));
            if (checkForChanges) {
                newMapping = DAOMappingUtil.getMappingExact(srcAddr, dstAddr, srcMask, dstMask, dao);
                return (newMapping.getValue() == null) ? oldMapping.getValue() != null : !newMapping.getValue()
                        .equals(oldMapping.getValue());
            }
        } else {
            List<RLOCGroup> oldLocators = null, newLocators = null;
            if (checkForChanges) {
                oldLocators = DAOMappingUtil.getLocatorsByEidToLocatorRecord(eidRecord, dao, shouldIterate);
            }
            IMappingKey key = MappingKeyUtil.generateMappingKey(eidRecord.getLispAddressContainer(),
                    eidRecord.getMaskLength());
            dao.put(key, entries.toArray(new MappingEntry[entries.size()]));
            if (checkForChanges) {
                newLocators = DAOMappingUtil.getLocatorsByEidToLocatorRecord(eidRecord, dao, shouldIterate);
                return (newLocators == null) ? oldLocators != null : !newLocators.equals(oldLocators);
            }
        }
        return false;
    }

    public static String getAddressKey(Address address, boolean shouldOverwrite) {
        if (address instanceof LcafKeyValue) {
            LcafKeyValue keyVal = (LcafKeyValue) address;
            if (keyVal.getLcafKeyValueAddressAddr().getKey().getPrimitiveAddress() instanceof DistinguishedName) {
                return ((DistinguishedName) keyVal.getLcafKeyValueAddressAddr().getKey().getPrimitiveAddress())
                        .getDistinguishedNameAddress().getDistinguishedName();
            }
        }
        if (shouldOverwrite) {
            return DAOSubKeys.ADDRESS_SUBKEY.toString();
        } else {
            return String.valueOf(address.hashCode());
        }
    }

    public static void removeAuthenticationKey(LispAddressContainer address, int maskLen, ILispDAO dao) {
        if (address.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSrcDstInnerDao(address, maskLen, dao);
            if (srcDstDao != null) {
                IMappingKey srcKey = MappingKeyUtil.generateMappingKey(getSrcForLcafSrcDst(address),
                        getSrcMaskForLcafSrcDst(address));
                srcDstDao.removeSpecific(srcKey, DAOSubKeys.PASSWORD_SUBKEY.toString());
            }
        } else {
            IMappingKey key = MappingKeyUtil.generateMappingKey(address, maskLen);
            dao.removeSpecific(key, DAOSubKeys.PASSWORD_SUBKEY.toString());
        }
    }

    private static void removeMappingRlocs(Entry<IMappingKey, List<RLOCGroup>> mapping, ILispDAO db,
            boolean shouldOverwrite) {
        if (mapping == null || mapping.getValue() == null) {
            return;
        }
        for (RLOCGroup group : mapping.getValue()) {
            for (LocatorRecord record : group.getRecords()) {
                db.removeSpecific(mapping.getKey(),
                        DAOMappingUtil.getAddressKey(record.getLispAddressContainer().getAddress(), shouldOverwrite));
            }
        }
    }

    public static void removeMapping(LispAddressContainer address, int maskLen, ILispDAO dao, boolean oerwrite) {
        Entry<IMappingKey, List<RLOCGroup>> mapping;
        ILispDAO db;
        if (address.getAddress() instanceof LcafSourceDest) {
            db = getSrcDstInnerDao(address, maskLen, dao);
            LispAFIAddress srcAddr = getSrcForLcafSrcDst(address);
            short srcMask = getSrcMaskForLcafSrcDst(address);
            mapping = DAOMappingUtil.getMappingForEid(srcAddr, srcMask, db);
        } else {
            db = dao;
            mapping = DAOMappingUtil.getMappingForEid(LispAFIConvertor.toAFI(address), maskLen, db);
        }
        removeMappingRlocs(mapping, db, oerwrite);
    }

    public static void removeSubscribers(LispAddressContainer address, int maskLen, ILispDAO dao,
            boolean shouldOverwrite) {
        Entry<IMappingKey, List<RLOCGroup>> mapping;
        ILispDAO db;
        if (address.getAddress() instanceof LcafSourceDest) {
            db = getSrcDstInnerDao(address, maskLen, dao);
            LispAFIAddress srcAddr = getSrcForLcafSrcDst(address);
            short srcMask = getSrcMaskForLcafSrcDst(address);
            mapping = DAOMappingUtil.getMappingForEid(srcAddr, srcMask, db);
        } else {
            db = dao;
            mapping = DAOMappingUtil.getMappingForEid(LispAFIConvertor.toAFI(address), maskLen, db);
        }
        db.removeSpecific(mapping.getKey(), DAOSubKeys.SUBSCRIBERS_SUBKEY.toString());
    }

    public static void removeEntry(LispAddressContainer address, int maskLen, ILispDAO dao, boolean shouldOverwrite) {
        Entry<IMappingKey, List<RLOCGroup>> mapping;
        ILispDAO db;
        if (address.getAddress() instanceof LcafSourceDest) {
            db = getSrcDstInnerDao(address, maskLen, dao);
            LispAFIAddress srcAddr = getSrcForLcafSrcDst(address);
            short srcMask = getSrcMaskForLcafSrcDst(address);
            mapping = DAOMappingUtil.getMappingForEid(srcAddr, srcMask, db);
        } else {
            db = dao;
            mapping = DAOMappingUtil.getMappingForEid(LispAFIConvertor.toAFI(address), maskLen, db);
        }

        db.remove(mapping.getKey());
    }

    public static LispAFIAddress getSrcForLcafSrcDst(LispAddressContainer container) {
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr()
                .getSrcAddress().getPrimitiveAddress());
    }

    public static LispAFIAddress getDstForLcafSrcDst(LispAddressContainer container) {
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr()
                .getDstAddress().getPrimitiveAddress());
    }

    public static short getSrcMaskForLcafSrcDst(LispAddressContainer container) {
        return ((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr().getSrcMaskLength();
    }

    public static short getDstMaskForLcafSrcDst(LispAddressContainer container) {
        return ((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr().getDstMaskLength();
    }

}
