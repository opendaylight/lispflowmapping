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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
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

    private ILispDAO getVniTable(Eid eid) {
        long vni = 0;
        if (eid.getVirtualNetworkId() == null) {
            vni = 0;
        } else {
            vni = eid.getVirtualNetworkId().getValue();
        }
        return (ILispDAO) dao.getSpecific(vni, SubKeys.VNI);
    }

    private ILispDAO getOrInstantiateVniTable(Eid eid) {
        long vni = 0;
        if (eid.getVirtualNetworkId() == null) {
            vni = 0;
        } else {
            vni = eid.getVirtualNetworkId().getValue();
        }
        ILispDAO table = (ILispDAO) dao.getSpecific(vni, SubKeys.VNI);
        if (table == null) {
            table = dao.putNestedTable(vni, SubKeys.VNI);
        }
        return table;
    }

    private ILispDAO getGenericSubTable(Eid eid, ILispDAO dao) {
        return (ILispDAO) dao.getSpecific(eid, SubKeys.SUB_RECORDS);
    }

    private ILispDAO getOrInstantiateGenericSubTable(Eid eid, ILispDAO dao) {
        ILispDAO table = (ILispDAO) dao.getSpecific(eid, SubKeys.SUB_RECORDS);
        if (table == null) {
            table = dao.putNestedTable(eid, SubKeys.SUB_RECORDS);
        }
        return table;
    }

    public void addMapping(Eid key, Object subkey, Object value, boolean shouldOverwrite) {
        Date timestamp = new Date(System.currentTimeMillis());
        Eid eid = MaskUtil.normalize(key);
        ILispDAO table = getOrInstantiateVniTable(key);

        table.put(eid, new MappingEntry<>(SubKeys.REGDATE, timestamp));
        table.put(eid, new MappingEntry<>(SubKeys.RECORD, value));

        if (!shouldOverwrite) {
            ILispDAO subDao = getOrInstantiateGenericSubTable(eid, table);
            subDao.put(subkey, new MappingEntry<>(SubKeys.REGDATE, timestamp));
            subDao.put(subkey, new MappingEntry<>(SubKeys.RECORD, value));
        }
    }

    // Method returns the DAO entry (hash) corresponding to either the longest prefix match of eid, if eid is maskable,
    // or the exact match otherwise. eid must be a 'simple' address
    private  Map<String, Object> getDaoEntryBest(Eid eid, ILispDAO dao) {
        Eid key;
        if (MaskUtil.isMaskable(eid.getAddress())) {
            short mask = MaskUtil.getMaskForAddress(eid.getAddress());
            while (mask > 0) {
                key = MaskUtil.normalize(eid, mask);
                mask--;
                Map<String, Object> entry = dao.get(key);
                if (entry != null) {
                    return entry;
                }
            }
            return null;
        } else {
            key = MaskUtil.normalize(eid);
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
                lookupKey = MaskUtil.normalize(key, mask);
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
        if (dstEid == null) {
            return null;
        }

        ILispDAO table = getVniTable(dstEid);
        if (table == null) {
            return null;
        }
        return getMappingLpmEid(dstEid, table);
    }

    public void removeMapping(Eid eid, Object subkey, boolean overwrite) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }

        table.removeSpecific(key, SubKeys.RECORD);

        if (!overwrite) {
            ILispDAO subTable = getGenericSubTable(key, table);
            if (subTable != null) {
                subTable.removeSpecific(subkey, SubKeys.RECORD);
            }
        }
    }

    public void addAuthenticationKey(Eid eid, MappingAuthkey authKey) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getOrInstantiateVniTable(key);
        table.put(key, new MappingEntry<>(SubKeys.AUTH_KEY, authKey));
    }

    private MappingAuthkey getAuthKeyLpm(Eid prefix, ILispDAO db) {
        short maskLength = MaskUtil.getMaskForAddress(prefix.getAddress());
        while (maskLength >= 0) {
            Eid key = MaskUtil.normalize(prefix, maskLength);
            Object password = db.getSpecific(key, SubKeys.AUTH_KEY);
            if (password != null && password instanceof MappingAuthkey) {
                return (MappingAuthkey) password;
            }
            maskLength -= 1;
        }
        return null;
    }

    public MappingAuthkey getAuthenticationKey(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        if (MaskUtil.isMaskable(eid.getAddress())) {
            return getAuthKeyLpm(eid, table);
        } else {
            Eid key = MaskUtil.normalize(eid);
            Object password = table.getSpecific(key, SubKeys.AUTH_KEY);
            if (password != null && password instanceof MappingAuthkey) {
                return (MappingAuthkey) password;
            } else {
                LOG.warn("Failed to find password!");
                return null;
            }
        }
    }

    public void removeAuthenticationKey(Eid eid) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }
        table.removeSpecific(key, SubKeys.AUTH_KEY);
    }

    public String printMappings() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Keys\tValues\n");

        final IRowVisitor innerVisitor = (new IRowVisitor() {
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

        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                if (valueKey.equals(SubKeys.VNI)) {
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
    public void updateMappingRegistration(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return;
        }
        Map<String, Object> daoEntry = getDaoEntryBest(eid, table);
        if (daoEntry != null) {
            daoEntry.put(SubKeys.REGDATE, new Date(System.currentTimeMillis()));
        }
    }

    @Override
    public void addData(Eid eid, String subKey, Object data) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getOrInstantiateVniTable(key);
        table.put(key, new MappingEntry<>(subKey, data));
    }

    @Override
    public Object getData(Eid eid, String subKey) {
        ILispDAO table = getOrInstantiateVniTable(eid);
        if (table == null) {
            return null;
        }
        Eid key = MaskUtil.normalize(eid);
        return table.getSpecific(key, subKey);
    }

    @Override
    public void removeData(Eid eid, String subKey) {
        ILispDAO table = getOrInstantiateVniTable(eid);
        if (table == null) {
            return;
        }
        Eid key = MaskUtil.normalize(eid);
        table.removeSpecific(key, subKey);
    }
}
