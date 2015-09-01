/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.mapcache;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.lispflowmapping.lisp.util.LcafSourceDestHelper;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyMapCache {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyMapCache.class);

    public static boolean addMapping(EidToLocatorRecord record, boolean checkForChanges, ILispDAO dao,
            boolean shouldIterate, boolean shouldOverwrite) {
        LispAddressContainer eid = MaskUtil.normalize(record.getLispAddressContainer());
        EidToLocatorRecord oldMapping = null;
        if (eid.getAddress() instanceof LcafSourceDest) {
            LispAddressContainer srcAddr = LcafSourceDestHelper.getSrc(eid);
            LispAddressContainer dstAddr = LcafSourceDestHelper.getDst(eid);

            if (checkForChanges) {
                oldMapping = getMappingExactSD(srcAddr, dstAddr, dao);
            }
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(eid, dao);
            srcDstDao.put(LcafSourceDestHelper.getSrc(eid),
                    new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
            srcDstDao.put(LcafSourceDestHelper.getSrc(eid), new MappingEntry<>(SubKeys.RECORD, record));
            if (checkForChanges && oldMapping != null && eid.equals(oldMapping.getLispAddressContainer())) {
                return true;
            }
        } else {
            if (checkForChanges) {
                oldMapping = getMappingExactSD(null, eid, dao);
            }
            dao.put(eid, new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
            dao.put(eid, new MappingEntry<>(SubKeys.RECORD, record));
            if (checkForChanges && oldMapping != null && eid.equals(oldMapping.getLispAddressContainer())) {
                return true;
            }
        }
        return false;
    }

    // Method returns the DAO entry (hash) corresponding to either the longest prefix match of eid, if eid is maskable,
    // or the exact match otherwise. eid must be a 'simple' address
    private static Map<String, ?> getDaoEntryBest(LispAddressContainer eid, ILispDAO dao) {
        if (MaskUtil.isMaskable(eid)) {
            LispAddressContainer key;
            short mask = MaskUtil.getMaskForAddress(eid);
            while (mask > 0) {
                key = MaskUtil.normalize(eid, mask);
                mask--;
                Map<String, ?> entry = dao.get(key);
                if (entry != null) {
                    return entry;
                }
            }
            return null;
        } else {
            Map<String, ?> entry = dao.get(eid);
            if (entry != null) {
                return dao.get(eid);
            } else {
                return null;
            }
        }
    }

    private static EidToLocatorRecord getMappingExactSD(LispAddressContainer srcEid, LispAddressContainer dstEid,
            ILispDAO dao) {
        Map<String, ?> daoEntry = dao.get(dstEid);
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.get(SubKeys.LCAF_SRCDST);
            if (srcEid != null && srcDstDao != null) {
                EidToLocatorRecord mapping = (EidToLocatorRecord) srcDstDao.getSpecific(srcEid, SubKeys.RECORD);
                return mapping;
            }
            // if lookup fails, return whatever is found for dst eid
            return (EidToLocatorRecord) daoEntry.get(SubKeys.RECORD);
        }
        return null;
    }

    // Returns the mapping corresponding to the longest prefix match for eid. eid must be a simple (maskable or not) address
    private static EidToLocatorRecord getMappingLpmEid(LispAddressContainer eid, ILispDAO dao) {
        Map<String, ?> daoEntry = getDaoEntryBest(eid, dao);
        if (daoEntry != null) {
            return (EidToLocatorRecord) daoEntry.get(SubKeys.RECORD);
        } else {
            return null;
        }
    }

    // Returns a mapping corresponding to either the longest prefix match for both dstEid and srcEid,
    // if a SourceDest mapping exists, or to dstEid
    private static EidToLocatorRecord getMappingLpmSD(LispAddressContainer srcEid, LispAddressContainer dstEid, ILispDAO dao) {
        Map<String, ?> daoEntry = getDaoEntryBest(dstEid, dao);
        if (daoEntry != null) {

            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.get(SubKeys.LCAF_SRCDST);
            if (srcDstDao != null) {
                EidToLocatorRecord mapping = getMappingLpmEid(srcEid, srcDstDao);
                if (mapping!= null) {
                    return mapping;
                }
            }

            // if lookup fails, return whatever is found for dst eid
            return (EidToLocatorRecord) daoEntry.get(SubKeys.RECORD);
        }
        return null;
    }

    public static EidToLocatorRecord getMapping(LispAddressContainer srcEid, LispAddressContainer dstEid,
            ILispDAO dao) {
        // a map-request for an actual SrcDst LCAF, ignore src eid
        if (dstEid.getAddress() instanceof LcafSourceDest) {
            LispAddressContainer srcAddr = LcafSourceDestHelper.getSrc(dstEid);
            LispAddressContainer dstAddr = LcafSourceDestHelper.getDst(dstEid);
            return getMappingLpmSD(srcAddr, dstAddr, dao);
        }

        // potential map-request for SrcDst LCAF from non SrcDst capable devices
        EidToLocatorRecord mapping = getMappingLpmSD(srcEid, dstEid, dao);

        if (mapping == null) {
            return null;
        }

        // if indeed SrcDst LCAF change the mapping's eid to matched dstEid
        if (mapping.getLispAddressContainer().getAddress() instanceof LcafSourceDest) {
            EidToLocatorRecord newMapping = new EidToLocatorRecordBuilder(mapping).setLispAddressContainer(
                    LcafSourceDestHelper.getDst(mapping.getLispAddressContainer())).build();
            return newMapping;
        }

        return mapping;
    }

    public static void removeMapping(LispAddressContainer address, ILispDAO dao, boolean oerwrite) {
        if (address.getAddress() instanceof LcafSourceDest) {
            ILispDAO db = getSDInnerDao(address, dao);
            if (db != null) {
                db.removeSpecific(LcafSourceDestHelper.getSrc(address),
                        SubKeys.RECORD);
            }
        } else {
            dao.removeSpecific(address, SubKeys.RECORD);
        }
    }

    public static void addAuthenticationKey(LispAddressContainer eid, String key, ILispDAO dao) {
        eid = MaskUtil.normalize(eid);
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(eid, dao);
            srcDstDao.put(LcafSourceDestHelper.getSrc(eid), new MappingEntry<String>(SubKeys.PASSWORD, key));
        } else {
            dao.put(eid, new MappingEntry<String>(SubKeys.PASSWORD, key));
        }
    }

    private static String getAuthKeyLpm(LispAddressContainer prefix, ILispDAO db) {
        short maskLength = MaskUtil.getMaskForAddress(prefix);
        while (maskLength >= 0) {
            LispAddressContainer key = MaskUtil.normalize(prefix, maskLength);
            Object password = db.getSpecific(key, SubKeys.PASSWORD);
            if (password != null && password instanceof String) {
                return (String) password;
            }
            maskLength -= 1;
        }
        return null;
    }

    public static String getAuthenticationKey(LispAddressContainer eid, ILispDAO dao, boolean iterate) {
        if (MaskUtil.isMaskable(LispAFIConvertor.toAFI(eid)) && iterate) {
            return getAuthKeyLpm(eid, dao);
        } else if (eid.getAddress() instanceof LcafSourceDest && iterate) {
            // NOTE: this is an exact match, not a longest prefix match
            ILispDAO srcDstDao = getSDInnerDao(eid, dao);
            if (srcDstDao != null) {
                return getAuthKeyLpm(LcafSourceDestHelper.getSrc(eid), srcDstDao);
            }
            return null;
        } else {
            Object password = dao.getSpecific(eid, SubKeys.PASSWORD);
            if (password != null && password instanceof String) {
                return (String) password;
            } else {
                LOG.warn("Failed to find password!");
                return null;
            }
        }
    }

    public static void removeAuthenticationKey(LispAddressContainer eid, ILispDAO dao) {
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSDInnerDao(eid, dao);
            if (srcDstDao != null) {
                srcDstDao.removeSpecific(eid, SubKeys.PASSWORD);
            }
        } else {
            dao.removeSpecific(eid, SubKeys.PASSWORD);
        }
    }

    public static void addSubscribers(LispAddressContainer eid, Set<SubscriberRLOC> subscribers,
            ILispDAO dao) {
        eid = MaskUtil.normalize(eid);
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(eid, dao);
            srcDstDao.put(LcafSourceDestHelper.getSrc(eid), new MappingEntry<Set<SubscriberRLOC>>(
                    SubKeys.SUBSCRIBERS, subscribers));
        } else {
            dao.put(eid, new MappingEntry<Set<SubscriberRLOC>>(SubKeys.SUBSCRIBERS, subscribers));
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<SubscriberRLOC> getSubscribers(LispAddressContainer eid, ILispDAO dao) {
        Object subscribers;
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSDInnerDao(eid, dao);
            subscribers = srcDstDao.getSpecific(LcafSourceDestHelper.getSrc(eid), SubKeys.SUBSCRIBERS);
        } else {
            subscribers = dao.getSpecific(eid, SubKeys.SUBSCRIBERS);
        }

        if (subscribers != null && subscribers instanceof Set<?>) {
            return (Set<SubscriberRLOC>) subscribers;
        }
        return null;
    }

    public static void removeSubscribers(LispAddressContainer address, ILispDAO dao, boolean shouldOverwrite) {
        if (address.getAddress() instanceof LcafSourceDest) {
            ILispDAO db = getSDInnerDao(address, dao);
            if (db != null) {
                db.removeSpecific(LcafSourceDestHelper.getSrc(address), SubKeys.SUBSCRIBERS);
            }
        } else {
            dao.removeSpecific(address, SubKeys.SUBSCRIBERS);
        }
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src.
    // This method returns the DAO associated to a dst or creates it if it doesn't exist.
    public static ILispDAO getOrInstantiateSDInnerDao(LispAddressContainer address, ILispDAO dao) {
        ILispDAO srcDstDao = (ILispDAO) dao.getSpecific(LcafSourceDestHelper.getDst(address),
                SubKeys.LCAF_SRCDST);
        if (srcDstDao == null) {
            // inserts nested table for source
            srcDstDao = dao.putNestedTable(LcafSourceDestHelper.getDst(address), SubKeys.LCAF_SRCDST);
        }
        return srcDstDao;
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src.
    // This method returns the DAO associated to dst or null if it doesn't exist.
    public static ILispDAO getSDInnerDao(LispAddressContainer address, ILispDAO dao) {
        return (ILispDAO) dao.getSpecific(LcafSourceDestHelper.getDst(address), SubKeys.LCAF_SRCDST);
    }
}
