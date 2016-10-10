/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.mapcache;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.ILispMapCache;
import org.opendaylight.lispflowmapping.lisp.type.ExtendedMappingRecord;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple map-cache that works with 'simple' addresses (see lisp-proto.yang). It can do longest prefix matching for IP
 * addresses.
 *
 * @author Florin Coras
 * @author Lorand Jakab
 *
 */
public class SimpleMapCache implements ILispMapCache {
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

    private ILispDAO getXtrIdTable(Eid eid, ILispDAO dao) {
        return (ILispDAO) dao.getSpecific(eid, SubKeys.XTRID_RECORDS);
    }

    private ILispDAO getOrInstantiateXtrIdTable(Eid eid, ILispDAO dao) {
        ILispDAO table = (ILispDAO) dao.getSpecific(eid, SubKeys.XTRID_RECORDS);
        if (table == null) {
            table = dao.putNestedTable(eid, SubKeys.XTRID_RECORDS);
        }
        return table;
    }

    @Override
    public void addMapping(Eid key, ExtendedMappingRecord mapping) {
        addMapping(key, mapping, null);
    }

    @Override
    public void addMapping(Eid key, ExtendedMappingRecord mapping, Set<IpAddressBinary> sourceRlocs) {
        Eid eid = MaskUtil.normalize(key);
        ILispDAO table = getOrInstantiateVniTable(key);
        table.put(eid, new MappingEntry<>(SubKeys.REGDATE, mapping.getTimestamp()));
        table.put(eid, new MappingEntry<>(SubKeys.RECORD, mapping));
        if (sourceRlocs != null) {
            table.put(eid, new MappingEntry<>(SubKeys.SRC_RLOCS, sourceRlocs));
        }
    }

    @Override
    public void addMapping(Eid key, XtrId xtrId, ExtendedMappingRecord mapping) {
        Eid eid = MaskUtil.normalize(key);
        ILispDAO table = getOrInstantiateVniTable(key);
        ILispDAO xtrIdDao = getOrInstantiateXtrIdTable(eid, table);
        xtrIdDao.put(xtrId, new MappingEntry<>(SubKeys.EXT_RECORD, mapping));
    }

    // Returns the mapping corresponding to the longest prefix match for eid. eid must be a simple (maskable or not)
    // address
    private ExtendedMappingRecord getMappingLpmEid(Eid eid, XtrId xtrId, ILispDAO dao) {
        SimpleImmutableEntry<Eid, Map<String, ?>> daoEntry = dao.getBestPair(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            if (xtrId != null) {
                ILispDAO xtrIdTable = getXtrIdTable(eid, (ILispDAO) daoEntry.getValue().get(SubKeys.XTRID_RECORDS));
                if (xtrIdTable != null) {
                    ExtendedMappingRecord xtrIdRecord = (ExtendedMappingRecord) xtrIdTable.getSpecific(xtrId,
                            SubKeys.EXT_RECORD);
                    if (xtrIdRecord.getTimestamp() != null
                            && MappingMergeUtil.timestampIsExpired(xtrIdRecord.getTimestamp())) {
                        xtrIdTable.removeSpecific(xtrId, SubKeys.EXT_RECORD);
                        return null;
                    } else {
                        return xtrIdRecord;
                    }
                } else {
                    return null;
                }
            } else {
                Date timestamp = (Date) daoEntry.getValue().get(SubKeys.REGDATE);
                ExtendedMappingRecord record = (ExtendedMappingRecord) daoEntry.getValue().get(SubKeys.RECORD);
                if (timestamp != null && MappingMergeUtil.timestampIsExpired(timestamp)) {
                    dao.removeSpecific(daoEntry.getKey(), SubKeys.REGDATE);
                    dao.removeSpecific(daoEntry.getKey(), SubKeys.RECORD);
                }
                return record;
            }
        } else {
            return null;
        }
    }

    @Override
    public ExtendedMappingRecord getMapping(Eid srcEid, Eid dstEid) {
        final XtrId xtrId = null;
        return getMapping(dstEid, xtrId);
    }

    @Override
    public ExtendedMappingRecord getMapping(Eid key, XtrId xtrId) {
        if (key == null) {
            return null;
        }

        ILispDAO table = getVniTable(key);
        if (table == null) {
            return null;
        }
        return getMappingLpmEid(key, xtrId, table);
    }

    // Returns the list of mappings stored in an xTR-ID DAO
    private List<Object> getXtrIdMappingList(ILispDAO dao) {
        if (dao != null) {
            final List<Object> records = new ArrayList<>();
            dao.getAll(new IRowVisitor() {
                public void visitRow(Object keyId, String valueKey, Object value) {
                    if (valueKey.equals(SubKeys.EXT_RECORD)) {
                        records.add(value);
                    }
                }
            });
            return records;
        }
        return null;
    }

    @Override
    public List<Object> getAllXtrIdMappings(Eid eid) {
        Map<String, ?> daoEntry = dao.getBest(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            ILispDAO xtrIdTable = (ILispDAO) daoEntry.get(SubKeys.XTRID_RECORDS);
            if (xtrIdTable != null) {
                return getXtrIdMappingList(xtrIdTable);
            }
        }
        return null;
    }

    public Eid getWidestNegativeMapping(Eid key) {
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return null;
        }
        return table.getWidestNegativePrefix(MaskUtil.normalize(key));
    }

    @Override
    public void removeMapping(Eid eid) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }

        table.removeSpecific(key, SubKeys.RECORD);
        table.removeSpecific(key, SubKeys.REGDATE);
        table.removeSpecific(key, SubKeys.SRC_RLOCS);
        table.removeSpecific(key, SubKeys.XTRID_RECORDS);
    }

    @Override
    public void removeMapping(Eid eid, XtrId xtrId) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }
        ILispDAO xtrIdTable = getXtrIdTable(key, table);
        if (xtrIdTable == null) {
            return;
        }
        xtrIdTable.removeSpecific(xtrId, SubKeys.EXT_RECORD);
    }

    @Override
    public void removeXtrIdMappings(Eid eid, List<XtrId> xtrIds) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }
        ILispDAO xtrIdTable = getXtrIdTable(key, table);
        if (xtrIdTable == null) {
            return;
        }
        for (XtrId xtrId : xtrIds) {
            xtrIdTable.removeSpecific(xtrId, SubKeys.EXT_RECORD);
        }
    }

    @Override
    public void refreshMappingRegistration(Eid eid, XtrId xtrId, Long timestamp) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return;
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Map<String, Object> daoEntry = table.getBest(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            daoEntry.put(SubKeys.REGDATE, new Date(timestamp));
        }

        ILispDAO xtrIdTable = (ILispDAO) daoEntry.get(SubKeys.XTRID_RECORDS);
        if (xtrIdTable != null && xtrId != null) {
            ExtendedMappingRecord extRecord = (ExtendedMappingRecord) xtrIdTable.getSpecific(xtrId,
                    SubKeys.EXT_RECORD);
            if (extRecord != null) {
                extRecord.setTimestamp(new Date(timestamp));
            }
        }
    }

    @Override
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

    @Override
    public MappingAuthkey getAuthenticationKey(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        if (MaskUtil.isMaskable(eid.getAddress()) && !(eid.getAddress() instanceof SourceDestKey)) {
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

    @Override
    public void removeAuthenticationKey(Eid eid) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }
        table.removeSpecific(key, SubKeys.AUTH_KEY);
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

    @Override
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
}
