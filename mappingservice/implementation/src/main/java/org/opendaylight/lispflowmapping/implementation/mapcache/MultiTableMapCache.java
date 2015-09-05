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

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.lispflowmapping.lisp.util.LcafSourceDestHelper;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multi table map-cache that works with 'simple' and SourceDest LCAF addresses (see lisp-proto.yang). It can do longest
 * prefix matching for IP and SourceDest LCAF addresses. In case of the latter, it uses two tables, one for dst and
 * another for source, queried and populated in this exact order.
 *
 * @author Florin Coras
 *
 */
public class MultiTableMapCache implements IMapCache {
    private static final Logger LOG = LoggerFactory.getLogger(MultiTableMapCache.class);
    private ILispDAO dao;

    public MultiTableMapCache(ILispDAO dao) {
        this.dao = dao;
    }

    public void addMapping(LispAddressContainer key, Object value, boolean shouldOverwrite) {
        LispAddressContainer eid = MaskUtil.normalize(key);
        if (eid.getAddress() instanceof LcafSourceDest) {
            LispAddressContainer srcKey = LcafSourceDestHelper.getSrc(eid);
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(eid, dao);
            srcDstDao.put(srcKey, new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
            srcDstDao.put(srcKey, new MappingEntry<>(SubKeys.RECORD, value));
        } else {
            dao.put(eid, new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
            dao.put(eid, new MappingEntry<>(SubKeys.RECORD, value));
        }
    }

    // Method returns the DAO entry (hash) corresponding to either the longest prefix match of eid, if eid is maskable,
    // or the exact match otherwise. eid must be a 'simple' address
    private Map<String, ?> getDaoEntryBest(LispAddressContainer eid, ILispDAO dao) {
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

    private Object getMappingExactSD(LispAddressContainer srcEid, LispAddressContainer dstEid,
            ILispDAO dao) {
        Map<String, ?> daoEntry = dao.get(dstEid);
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.get(SubKeys.LCAF_SRCDST);
            if (srcEid != null && srcDstDao != null) {
                return srcDstDao.getSpecific(srcEid, SubKeys.RECORD);
            }
            // if lookup fails, return whatever is found for dst eid
            return daoEntry.get(SubKeys.RECORD);
        }
        return null;
    }

    // Returns the mapping corresponding to the longest prefix match for eid. eid must be a simple (maskable or not) address
    private Object getMappingLpmEid(LispAddressContainer eid, ILispDAO dao) {
        Map<String, ?> daoEntry = getDaoEntryBest(eid, dao);
        if (daoEntry != null) {
            return daoEntry.get(SubKeys.RECORD);
        } else {
            return null;
        }
    }

    // Returns a mapping corresponding to either the longest prefix match for both dstEid and srcEid,
    // if a SourceDest mapping exists, or to dstEid
    private Object getMappingLpmSD(LispAddressContainer srcEid, LispAddressContainer dstEid, ILispDAO dao) {
        Map<String, ?> daoEntry = getDaoEntryBest(dstEid, dao);
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.get(SubKeys.LCAF_SRCDST);
            if (srcDstDao != null) {
                Object mapping = getMappingLpmEid(srcEid, srcDstDao);
                if (mapping!= null) {
                    return mapping;
                }
            }

            // if lookup fails, return whatever is found for dst eid
            return daoEntry.get(SubKeys.RECORD);
        }
        return null;
    }

    public Object getMapping(LispAddressContainer srcEid, LispAddressContainer dstEid) {
        // a map-request for an actual SrcDst LCAF, ignore src eid
        if (dstEid.getAddress() instanceof LcafSourceDest) {
            LispAddressContainer srcAddr = LcafSourceDestHelper.getSrc(dstEid);
            LispAddressContainer dstAddr = LcafSourceDestHelper.getDst(dstEid);
            return getMappingLpmSD(srcAddr, dstAddr, dao);
        }

        // potential map-request for SrcDst LCAF from non SrcDst capable devices
        return getMappingLpmSD(srcEid, dstEid, dao);
    }

    public void removeMapping(LispAddressContainer eid, boolean overwrite) {
        eid = MaskUtil.normalize(eid);
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO db = getSDInnerDao(eid, dao);
            if (db != null) {
                db.removeSpecific(LcafSourceDestHelper.getSrc(eid),
                        SubKeys.RECORD);
            }
        } else {
            dao.removeSpecific(eid, SubKeys.RECORD);
        }
    }

    public void addAuthenticationKey(LispAddressContainer eid, String key) {
        eid = MaskUtil.normalize(eid);
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(eid, dao);
            srcDstDao.put(LcafSourceDestHelper.getSrc(eid), new MappingEntry<String>(SubKeys.AUTH_KEY, key));
        } else {
            dao.put(eid, new MappingEntry<String>(SubKeys.AUTH_KEY, key));
        }
    }

    private String getAuthKeyLpm(LispAddressContainer prefix, ILispDAO db) {
        short maskLength = MaskUtil.getMaskForAddress(prefix);
        while (maskLength >= 0) {
            LispAddressContainer key = MaskUtil.normalize(prefix, maskLength);
            Object password = db.getSpecific(key, SubKeys.AUTH_KEY);
            if (password != null && password instanceof String) {
                return (String) password;
            }
            maskLength -= 1;
        }
        return null;
    }

    public String getAuthenticationKey(LispAddressContainer eid) {
        if (MaskUtil.isMaskable(LispAFIConvertor.toAFI(eid))) {
            return getAuthKeyLpm(eid, dao);
        } else if (eid.getAddress() instanceof LcafSourceDest) {
            // NOTE: this is an exact match, not a longest prefix match
            ILispDAO srcDstDao = getSDInnerDao(eid, dao);
            if (srcDstDao != null) {
                return getAuthKeyLpm(LcafSourceDestHelper.getSrc(eid), srcDstDao);
            }
            return null;
        } else {
            Object password = dao.getSpecific(eid, SubKeys.AUTH_KEY);
            if (password != null && password instanceof String) {
                return (String) password;
            } else {
                LOG.warn("Failed to find password!");
                return null;
            }
        }
    }

    public void removeAuthenticationKey(LispAddressContainer eid) {
        eid = MaskUtil.normalize(eid);
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSDInnerDao(eid, dao);
            if (srcDstDao != null) {
                srcDstDao.removeSpecific(eid, SubKeys.AUTH_KEY);
            }
        } else {
            dao.removeSpecific(eid, SubKeys.AUTH_KEY);
        }
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src.
    // This method returns the DAO associated to a dst or creates it if it doesn't exist.
    private ILispDAO getOrInstantiateSDInnerDao(LispAddressContainer address, ILispDAO dao) {
        LispAddressContainer dstKey = LcafSourceDestHelper.getDst(address);
        ILispDAO srcDstDao = (ILispDAO) dao.getSpecific(dstKey, SubKeys.LCAF_SRCDST);
        if (srcDstDao == null) {
            // inserts nested table for source
            srcDstDao = dao.putNestedTable(dstKey, SubKeys.LCAF_SRCDST);
        }
        return srcDstDao;
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src.
    // This method returns the DAO associated to dst or null if it doesn't exist.
    private ILispDAO getSDInnerDao(LispAddressContainer address, ILispDAO dao) {
        return (ILispDAO) dao.getSpecific(LcafSourceDestHelper.getDst(address), SubKeys.LCAF_SRCDST);
    }

    public String printMappings() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Keys\tValues\n");
        final IRowVisitor innerVisitor = (new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append(key + "\t");
                }
                if (!(valueKey.equals(SubKeys.LCAF_SRCDST))) {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                if (valueKey.equals(SubKeys.LCAF_SRCDST)) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(innerVisitor);
                    sb.append("}\t");
                } else {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public void updateMappingRegistration(LispAddressContainer key) {

    }

    @Override
    public void addData(LispAddressContainer key, String subKey, Object data) {
        key = MaskUtil.normalize(key);
        if (key.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(key, dao);
            srcDstDao.put(LcafSourceDestHelper.getSrc(key), new MappingEntry<Object>(subKey, data));
        } else {
            dao.put(key, new MappingEntry<Object>(subKey, data));
        }
    }

    @Override
    public Object getData(LispAddressContainer eid, String subKey) {
        if (eid.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSDInnerDao(eid, dao);
            return srcDstDao.getSpecific(LcafSourceDestHelper.getSrc(eid), subKey);
        } else {
            return dao.getSpecific(eid, subKey);
        }
    }

    @Override
    public void removeData(LispAddressContainer key, String subKey) {
        key = MaskUtil.normalize(key);
        if (key.getAddress() instanceof LcafSourceDest) {
            ILispDAO db = getSDInnerDao(key, dao);
            if (db != null) {
                db.removeSpecific(LcafSourceDestHelper.getSrc(key), subKey);
            }
        } else {
            dao.removeSpecific(key, subKey);
        }
    }
}
