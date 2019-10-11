/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.mapcache;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.ILispMapCache;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.mapcache.lisp.LispMapCacheStringifier;
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

    private long getVni(Eid eid) {
        if (eid.getVirtualNetworkId() == null) {
            return 0;
        } else {
            return eid.getVirtualNetworkId().getValue().toJava();
        }
    }

    private ILispDAO getVniTable(Eid eid) {
        return (ILispDAO) dao.getSpecific(getVni(eid), SubKeys.VNI);
    }

    private void removeVniTable(Eid eid) {
        dao.removeSpecific(getVni(eid), SubKeys.VNI);
    }

    private ILispDAO getOrInstantiateVniTable(Eid eid) {
        long vni = getVni(eid);
        ILispDAO table = (ILispDAO) dao.getSpecific(vni, SubKeys.VNI);
        if (table == null) {
            table = dao.putNestedTable(vni, SubKeys.VNI);
        }
        return table;
    }

    private ILispDAO getOrInstantiateXtrIdTable(Eid eid, ILispDAO lispDAO) {
        ILispDAO table = (ILispDAO) lispDAO.getSpecific(eid, SubKeys.XTRID_RECORDS);
        if (table == null) {
            table = lispDAO.putNestedTable(eid, SubKeys.XTRID_RECORDS);
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
    private Object getMappingLpmEid(Eid eid, XtrId xtrId, ILispDAO lispDAO) {
        SimpleImmutableEntry<Eid, Map<String, ?>> daoEntry = lispDAO.getBestPair(MaskUtil.normalize(eid));
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
        return getMapping(dstEid, (XtrId) null);
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
    private List<Object> getXtrIdMappingList(ILispDAO lispDAO) {
        if (lispDAO != null) {
            final List<Object> records = new ArrayList<>();
            lispDAO.getAll(new IRowVisitor() {
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

    // Returns null for positive mappings, and 0/0 for empty cache.
    @Override
    public Eid getWidestNegativeMapping(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return MaskUtil.normalize(eid, (short) 0);
        }
        return table.getWidestNegativePrefix(MaskUtil.normalize(eid));
    }

    @Override
    public Eid getCoveringLessSpecific(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        return table.getCoveringLessSpecific(MaskUtil.normalize(eid));
    }

    @Override
    public Eid getParentPrefix(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        return table.getParentPrefix(MaskUtil.normalize(eid));
    }

    @Override
    public Eid getSiblingPrefix(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        return table.getSiblingPrefix(MaskUtil.normalize(eid));
    }

    @Override
    public Eid getVirtualParentSiblingPrefix(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        return table.getVirtualParentSiblingPrefix(MaskUtil.normalize(eid));
    }

    @Override
    public Set<Eid> getSubtree(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return Collections.emptySet();
        }
        return table.getSubtree(eid);
    }

    @Override
    public void removeMapping(Eid eid) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return;
        }

        Eid key = MaskUtil.normalize(eid);
        table.remove(key);
        if (table.isEmpty()) {
            removeVniTable(eid);
        }
    }

    @Override
    public void removeMapping(Eid eid, XtrId xtrId) {
        List<XtrId> xtrIds = Arrays.asList(xtrId);
        removeXtrIdMappings(eid, xtrIds);
    }

    @Override
    public void removeXtrIdMappings(Eid eid, List<XtrId> xtrIds) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return;
        }
        Eid key = MaskUtil.normalize(eid);
        ILispDAO xtrIdTable = (ILispDAO) table.getSpecific(key, SubKeys.XTRID_RECORDS);
        if (xtrIdTable == null) {
            return;
        }
        for (XtrId xtrId : xtrIds) {
            xtrIdTable.removeSpecific(xtrId, SubKeys.RECORD);
        }
        if (xtrIdTable.isEmpty()) {
            table.removeSpecific(key, SubKeys.XTRID_RECORDS);
            if (table.isEmpty()) {
                removeVniTable(eid);
            }
        }
    }

    @Override
    public void addData(Eid eid, String subKey, Object data) {
        ILispDAO table = getOrInstantiateVniTable(eid);
        Eid key = MaskUtil.normalize(eid);
        table.put(key, new MappingEntry<>(subKey, data));
    }

    @Override
    public Object getData(Eid eid, String subKey) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return null;
        }
        Eid key = MaskUtil.normalize(eid);
        return table.getSpecific(key, subKey);
    }

    @Override
    public void removeData(Eid eid, String subKey) {
        ILispDAO table = getVniTable(eid);
        if (table == null) {
            return;
        }
        Eid key = MaskUtil.normalize(eid);
        table.removeSpecific(key, subKey);
        if (table.isEmpty()) {
            removeVniTable(eid);
        }
    }

    @Override
    public String printMappings() {
        return LispMapCacheStringifier.printSMCMappings(dao);
    }

    @Override
    public String prettyPrintMappings() {
        return LispMapCacheStringifier.prettyPrintSMCMappings(dao);
    }
}
