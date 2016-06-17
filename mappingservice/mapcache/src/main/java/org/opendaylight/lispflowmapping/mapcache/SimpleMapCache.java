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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
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

    private void removeExpiredXtrIdTableEntries(ILispDAO xtrIdDao, List<XtrId> expiredMappings) {
        for (XtrId xtrId : expiredMappings) {
            xtrIdDao.removeSpecific(xtrId, SubKeys.RECORD);
        }
    }

    public void addMapping(Eid key, Object value, boolean shouldOverwrite, boolean shouldMerge) {
        if (value == null) {
            LOG.warn("addMapping() called with null 'value', ignoring");
            return;
        }

        if (!(value instanceof MappingRecord)) {
            LOG.warn("addMapping() called with a 'value' that is not a 'MappingRecord', ignoring");
            return;
        }

        MappingRecord record = (MappingRecord) value;
        if (record.getXtrId() == null && !shouldOverwrite && shouldMerge) {
            LOG.warn("addMapping() called will null xTR-ID in MappingRecord, while merge is set, ignoring");
            return;
        }

        Date regdate = new Date(record.getTimestamp());     // The serializer always sets it
        Eid eid = MaskUtil.normalize(key);
        ILispDAO table = getOrInstantiateVniTable(key);

        ILispDAO xtrIdDao = null;
        if (!shouldOverwrite) {
            xtrIdDao = getOrInstantiateXtrIdTable(eid, table);
            xtrIdDao.put(record.getXtrId(), new MappingEntry<>(SubKeys.RECORD, value));
        }

        if (shouldMerge) {
            List<XtrId> expiredMappings = new ArrayList<XtrId>();
            Set<IpAddressBinary> sourceRlocs = new HashSet<IpAddressBinary>();
            MappingRecord mergedEntry = MappingMergeUtil.mergeXtrIdMappings(getXtrIdMappingList(xtrIdDao),
                    expiredMappings, sourceRlocs);
            removeExpiredXtrIdTableEntries(xtrIdDao, expiredMappings);
            if (mergedEntry == null) {
                return;
            }
            regdate = new Date(mergedEntry.getTimestamp());
            table.put(eid, new MappingEntry<>(SubKeys.REGDATE, regdate));
            table.put(eid, new MappingEntry<>(SubKeys.RECORD, mergedEntry));
            table.put(eid, new MappingEntry<>(SubKeys.SRC_RLOCS, sourceRlocs));
        } else {
            table.put(eid, new MappingEntry<>(SubKeys.REGDATE, regdate));
            table.put(eid, new MappingEntry<>(SubKeys.RECORD, value));
        }
    }

    // Returns the list of mappings stored in an xTR-ID DAO
    private List<Object> getXtrIdMappingList(ILispDAO dao) {
        if (dao != null) {
            final List<Object> records = new ArrayList<Object>();
            dao.getAll(new IRowVisitor() {
                public void visitRow(Object keyId, String valueKey, Object value) {
                    if (valueKey.equals(SubKeys.RECORD)) {
                        records.add(value);
                    }
                }
            });
            return records;
        }
        return null;
    }
/*
    private SimpleImmutableEntry<byte[], Long> getXtrIdMinTimeStamp(ILispDAO dao) {
        if (dao != null) {
            // We don't actually need a list here, but the external variable we modify inside the IRowVisitor
            // implementation has to be final. So we use a List, and it's methods, to modify contents
            final List<SimpleImmutableEntry<byte[], Long>> entries =
                    new ArrayList<SimpleImmutableEntry<byte[], Long>>();
            entries.set(0, new SimpleImmutableEntry<byte[], Long>(null, System.currentTimeMillis()));

            dao.getAll(new IRowVisitor() {
                public void visitRow(Object keyId, String valueKey, Object value) {
                    if (valueKey.equals(SubKeys.RECORD)) {
                        byte[] currentXtrId = ((MappingRecord) value).getXtrId();
                        long currentTimestamp = ((MappingRecord) value).getTimestamp();
                        // If one of the timestamps is expired, we signal it to the caller by setting the returned
                        // timestamp to null. The caller will then have to remove that xTR-ID, and do a full merge
                        // (or decrement)
                        if (MappingMergeUtil.timestampIsExpired(currentTimestamp)) {
                            SimpleImmutableEntry<byte[], Long> entry =
                                    new SimpleImmutableEntry<byte[], Long>(currentXtrId, null);
                            entries.set(0, entry);
                            return;
                        }
                        if (entries.get(0).getValue() > currentTimestamp) {
                            SimpleImmutableEntry<byte[], Long> entry =
                                    new SimpleImmutableEntry<byte[], Long>(currentXtrId, currentTimestamp);
                            entries.set(0, entry);
                        }
                    }
                }
            });
            return entries.get(0);
        }
        return null;
    }
*/
    // Returns the mapping corresponding to the longest prefix match for eid. eid must be a simple (maskable or not)
    // address
    private Object getMappingLpmEid(Eid eid, byte[] xtrId, ILispDAO dao) {
        SimpleImmutableEntry<Eid, Map<String, ?>> daoEntry = dao.getBestPair(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            if (xtrId != null) {
                ILispDAO xtrIdTable = getXtrIdTable(eid, (ILispDAO) daoEntry.getValue().get(SubKeys.XTRID_RECORDS));
                if (xtrIdTable != null) {
                    MappingRecord xtrIdRecord = (MappingRecord) xtrIdTable.getSpecific(xtrId, SubKeys.RECORD);
                    if (xtrIdRecord.getTimestamp() != null &&
                            MappingMergeUtil.timestampIsExpired(xtrIdRecord.getTimestamp())) {
                        xtrIdTable.removeSpecific(xtrId, SubKeys.RECORD);
                        return null;
                    } else {
                        return xtrIdRecord;
                    }
                } else {
                    return null;
                }
            } else {
                Date timestamp = (Date) daoEntry.getValue().get(SubKeys.REGDATE);
                if (timestamp != null && MappingMergeUtil.timestampIsExpired(timestamp)) {
                    dao.removeSpecific(daoEntry.getKey(), SubKeys.REGDATE);
                    dao.removeSpecific(daoEntry.getKey(), SubKeys.RECORD);
                }
                return daoEntry.getValue().get(SubKeys.RECORD);
            }
        } else {
            return null;
        }
    }

    public Object getMapping(Eid srcEid, Eid dstEid, byte[] xtrId) {
        if (dstEid == null) {
            return null;
        }

        ILispDAO table = getVniTable(dstEid);
        if (table == null) {
            return null;
        }
        return getMappingLpmEid(dstEid, xtrId, table);
    }

    public Object getMapping(Eid srcEid, Eid dstEid) {
        return getMapping(srcEid, dstEid, null);
    }

    public List<Object> getAllXtrIdMappings(Eid eid) {
        Map<String, ?> daoEntry = dao.getBest(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            ILispDAO xtrIdTable = getXtrIdTable(eid, (ILispDAO) daoEntry.get(SubKeys.XTRID_RECORDS));
            if (xtrIdTable != null) {
                return getXtrIdMappingList(xtrIdTable);
            }
        }
        return null;
    }

    public Eid getWidestNegativeMapping(Eid key) {
        return dao.getWidestNegativePrefix(MaskUtil.normalize(key));
    }

    public void removeMapping(Eid eid, boolean overwrite) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }

        table.removeSpecific(key, SubKeys.RECORD);

        if (!overwrite) {
            ILispDAO xtrIdTable = getXtrIdTable(key, table);
            if (xtrIdTable != null) {
                xtrIdTable.removeSpecific(key, SubKeys.RECORD);
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
    public void updateMappingRegistration(Eid eid, Long timestamp) {
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
