/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.mapcache;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.ILispMapCache;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Simple map-cache that works with 'simple' addresses (see lisp-proto.yang). It can do longest prefix matching for IP
 * addresses.
 *
 * @author Florin Coras
 * @author Lorand Jakab
 *
 */
public class SimpleMapCache implements ILispMapCache {
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

    private ILispDAO getOrInstantiateXtrIdTable(Eid eid, ILispDAO dao) {
        ILispDAO table = (ILispDAO) dao.getSpecific(eid, SubKeys.XTRID_RECORDS);
        if (table == null) {
            table = dao.putNestedTable(eid, SubKeys.XTRID_RECORDS);
        }
        return table;
    }

    @Override
    public void addMapping(Eid key, Object value) {
        addMapping(key, value, null);
    }

    @Override
    public void addMapping(Eid key, Object value, Set<IpAddressBinary> sourceRlocs) {
        Eid eid = MaskUtil.normalize(key);
        ILispDAO table = getOrInstantiateVniTable(key);
        table.put(eid, new MappingEntry<>(SubKeys.RECORD, value));
        if (sourceRlocs != null) {
            table.put(eid, new MappingEntry<>(SubKeys.SRC_RLOCS, sourceRlocs));
        }
    }

    @Override
    public void addMapping(Eid key, XtrId xtrId, Object value) {
        Eid eid = MaskUtil.normalize(key);
        ILispDAO table = getOrInstantiateVniTable(key);
        ILispDAO xtrIdDao = getOrInstantiateXtrIdTable(eid, table);
        xtrIdDao.put(xtrId, new MappingEntry<>(SubKeys.RECORD, value));
    }

    // Returns the mapping corresponding to the longest prefix match for eid. eid must be a simple (maskable or not)
    // address
    private Object getMappingLpmEid(Eid eid, XtrId xtrId, ILispDAO dao) {
        SimpleImmutableEntry<Eid, Map<String, ?>> daoEntry = dao.getBestPair(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            if (xtrId != null) {
                ILispDAO xtrIdTable = (ILispDAO) daoEntry.getValue().get(SubKeys.XTRID_RECORDS);
                if (xtrIdTable != null) {
                    return xtrIdTable.getSpecific(xtrId, SubKeys.RECORD);
                }
            } else {
                return daoEntry.getValue().get(SubKeys.RECORD);
            }
        }
        return null;
    }

    @Override
    public Object getMapping(Eid srcEid, Eid dstEid) {
        final XtrId xtrId = null;
        return getMapping(dstEid, xtrId);
    }

    @Override
    public Object getMapping(Eid eid, XtrId xtrId) {
        if (eid == null) {
            return null;
        }

        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        return getMappingLpmEid(eid, xtrId, table);
    }

    // Returns the list of mappings stored in an xTR-ID DAO
    private List<Object> getXtrIdMappingList(ILispDAO dao) {
        if (dao != null) {
            final List<Object> records = new ArrayList<>();
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

    @Override
    public List<Object> getAllXtrIdMappings(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        Map<String, ?> daoEntry = table.getBest(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            ILispDAO xtrIdTable = (ILispDAO) daoEntry.get(SubKeys.XTRID_RECORDS);
            if (xtrIdTable != null) {
                return getXtrIdMappingList(xtrIdTable);
            }
        }
        return null;
    }

    public Eid getWidestNegativeMapping(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        return table.getWidestNegativePrefix(MaskUtil.normalize(eid));
    }

    @Override
    public Eid getParentPrefix(Eid eid) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return null;
        }
        return table.getParentPrefix(key);
    }

    @Override
    public void removeMapping(Eid eid) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }

        // We intentionally don't remove subscribers, so in case a mapping is re-added, they get notified
        table.removeSpecific(key, SubKeys.RECORD);
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
        ILispDAO xtrIdTable = (ILispDAO) table.getSpecific(key, SubKeys.XTRID_RECORDS);
        if (xtrIdTable == null) {
            return;
        }
        xtrIdTable.removeSpecific(xtrId, SubKeys.RECORD);
    }

    @Override
    public void removeXtrIdMappings(Eid eid, List<XtrId> xtrIds) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }
        ILispDAO xtrIdTable = (ILispDAO) table.getSpecific(key, SubKeys.XTRID_RECORDS);
        if (xtrIdTable == null) {
            return;
        }
        for (XtrId xtrId : xtrIds) {
            xtrIdTable.removeSpecific(xtrId, SubKeys.RECORD);
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
