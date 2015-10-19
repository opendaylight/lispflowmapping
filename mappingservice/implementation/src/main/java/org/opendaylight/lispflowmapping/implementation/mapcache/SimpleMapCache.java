/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.mapcache;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Date;
import java.util.Map;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple map-cache that works with 'simple' addresses (see lisp-proto.yang). It can do longest prefix matching for IP
 * addresses.
 *
 * @author Florin Coras
 *
 */
public class SimpleMapCache implements IMapCache {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleMapCache.class);
    private ILispDAO dao;

    public SimpleMapCache(ILispDAO dao) {
        this.dao = dao;
    }

    public void addMapping(Eid key, Object value, boolean shouldOverwrite) {
        Eid eid = MaskUtil.normalize(key);
        dao.put(eid, new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
        dao.put(eid, new MappingEntry<>(SubKeys.RECORD, value));
    }

    // Method returns the DAO entry (hash) corresponding to either the longest prefix match of eid, if eid is maskable,
    // or the exact match otherwise. eid must be a 'simple' address
    private  Map<String, ?> getDaoEntryBest(Eid key, ILispDAO dao) {
        if (MaskUtil.isMaskable(key.getAddress())) {
            Eid lookupKey;
            short mask = MaskUtil.getMaskForAddress(key.getAddress());
            while (mask > 0) {
                lookupKey = MaskUtil.normalize(key);
                mask--;
                Map<String, ?> entry = dao.get(lookupKey);
                if (entry != null) {
                    return entry;
                }
            }
            return null;
        } else {
            return dao.get(key);
        }
    }

    // Method returns the DAO entry (hash) corresponding to either the longest prefix match of eid, if eid is maskable,
    // or the exact match otherwise. eid must be a 'simple' address
    private SimpleImmutableEntry<Eid, Map<String, ?>> getDaoPairEntryBest(Eid key, ILispDAO dao) {
        if (MaskUtil.isMaskable(key.getAddress())) {
            Eid lookupKey;
            short mask = MaskUtil.getMaskForAddress(key.getAddress());
            while (mask > 0) {
                lookupKey = MaskUtil.normalize(key);
                mask--;
                Map<String, ?> entry = dao.get(lookupKey);
                if (entry != null) {
                    return new SimpleImmutableEntry<Eid, Map<String, ?>>(lookupKey, entry);
                }
            }
            return null;
        } else {
            Map<String, ?> entry = dao.get(key);
            if (entry != null) {
                return new SimpleImmutableEntry<Eid, Map<String, ?>>(key, entry);
            } else {
                return null;
            }
        }
    }

    // Returns the mapping corresponding to the longest prefix match for eid. eid must be a simple (maskable or not)
    // address
    private Object getMappingLpmEid(Eid eid, ILispDAO dao) {
        Map<String, ?> daoEntry = getDaoEntryBest(eid, dao);
        if (daoEntry != null) {
            return daoEntry.get(SubKeys.RECORD);
        } else {
            return null;
        }
    }

    // Returns the matched key and mapping corresponding to the longest prefix match for eid. eid must be a simple
    // (maskable or not) address
    private SimpleImmutableEntry<Eid, Object> getMappingPairLpmEid(Eid eid, ILispDAO dao) {
        SimpleImmutableEntry<Eid, Map<String, ?>> daoEntry = getDaoPairEntryBest(eid, dao);
        if (daoEntry != null) {
            return new SimpleImmutableEntry<Eid, Object>(daoEntry.getKey(), daoEntry.getValue().get(
                    SubKeys.RECORD));
        } else {
            return null;
        }
    }

    public Object getMapping(Eid srcEid, Eid dstEid) {
        return getMappingLpmEid(dstEid, dao);
    }

    public void removeMapping(Eid eid, boolean overwrite) {
        eid = MaskUtil.normalize(eid);
        dao.removeSpecific(eid, SubKeys.RECORD);
    }

    public void addAuthenticationKey(Eid eid, String key) {
        eid = MaskUtil.normalize(eid);
        dao.put(eid, new MappingEntry<String>(SubKeys.AUTH_KEY, key));
    }

    private String getAuthKeyLpm(Eid prefix, ILispDAO db) {
        short maskLength = MaskUtil.getMaskForAddress(prefix.getAddress());
        while (maskLength >= 0) {
            Eid key = MaskUtil.normalize(prefix);
            Object password = db.getSpecific(key, SubKeys.AUTH_KEY);
            if (password != null && password instanceof String) {
                return (String) password;
            }
            maskLength -= 1;
        }
        return null;
    }

    public String getAuthenticationKey(Eid eid) {
        if (MaskUtil.isMaskable(eid.getAddress())) {
            return getAuthKeyLpm(eid, dao);
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

    public void removeAuthenticationKey(Eid eid) {
        eid = MaskUtil.normalize(eid);
        dao.removeSpecific(eid, SubKeys.AUTH_KEY);
    }

    public String printMappings() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Keys\tValues\n");
        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                sb.append(valueKey + "=" + value + "\t");
                lastKey = key;
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public void updateMappingRegistration(Eid key) {
        Map<String, ?> daoEntry = getDaoEntryBest(key, dao);
        if (daoEntry != null) {
            dao.put(key, new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
        }
    }

    @Override
    public void addData(Eid key, String subKey, Object data) {
        Eid normKey = MaskUtil.normalize(key);
        dao.put(normKey, new MappingEntry<>(subKey, data));
    }

    @Override
    public Object getData(Eid key, String subKey) {
        return dao.getSpecific(key, subKey);
    }

    @Override
    public void removeData(Eid key, String subKey) {
        dao.removeSpecific(key, subKey);
    }
}
