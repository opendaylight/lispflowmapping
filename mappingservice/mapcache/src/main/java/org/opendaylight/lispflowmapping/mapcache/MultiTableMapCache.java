/*
 * Copyright (c) 2014, 2016 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.mapcache;

import java.util.Map;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Multi table map-cache that works with 'simple' and SourceDest LCAF addresses (see lisp-proto.yang). It can do longest
 * prefix matching for IP and SourceDest LCAF addresses. In case of the latter, it uses two tables, one for dst and
 * another for source, queried and populated in this exact order.
 *
 * @author Florin Coras
 */
public class MultiTableMapCache implements IMapCache {
    private ILispDAO dao;

    public MultiTableMapCache(ILispDAO dao) {
        this.dao = dao;
    }

    private long getVni(Eid eid) {
        if (eid.getVirtualNetworkId() == null) {
            return 0;
        } else {
            return eid.getVirtualNetworkId().getValue();
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

    public void addMapping(Eid key, Object value) {
        Eid eid = MaskUtil.normalize(key);
        ILispDAO table = getOrInstantiateVniTable(key);

        if (eid.getAddress() instanceof SourceDestKey) {
            Eid srcKey = SourceDestKeyHelper.getSrcBinary(eid);
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(eid, table);
            srcDstDao.put(srcKey, new MappingEntry<>(SubKeys.RECORD, value));
        } else {
            table.put(eid, new MappingEntry<>(SubKeys.RECORD, value));
        }
    }

    // Returns the mapping corresponding to the longest prefix match for eid.
    // eid must be a simple (maskable or not) address
    private Object getMappingLpmEid(Eid eid, ILispDAO mappingsDb) {
        if (eid == null) {
            return null;
        }
        Map<String, ?> daoEntry = mappingsDb.getBest(MaskUtil.normalize(eid));
        if (daoEntry != null) {
            return daoEntry.get(SubKeys.RECORD);
        } else {
            return null;
        }
    }

    // Returns a mapping corresponding to either the longest prefix match for both dstEid and srcEid,
    // if a SourceDest mapping exists, or to dstEid
    private Object getMappingLpmSD(Eid srcEid, Eid dstEid, ILispDAO mappingsDb) {
        Map<String, ?> daoEntry = mappingsDb.getBest(MaskUtil.normalize(dstEid));
        if (daoEntry != null) {
            // try SrcDst eid lookup
            ILispDAO srcDstDao = (ILispDAO) daoEntry.get(SubKeys.LCAF_SRCDST);
            if (srcEid != null && srcDstDao != null) {
                // make sure that srcEid is a prefix, not an IP and binary
                Object mapping = getMappingLpmEid(LispAddressUtil.asIpPrefixBinaryEid(srcEid), srcDstDao);
                if (mapping !=  null) {
                    return mapping;
                }
            }

            // if lookup fails, return whatever is found for dst eid
            return daoEntry.get(SubKeys.RECORD);
        }
        return null;
    }

    public Object getMapping(Eid srcEid, Eid dstEid) {
        if (dstEid == null) {
            return null;
        }

        ILispDAO table = getVniTable(dstEid);
        if (table == null) {
            return null;
        }

        // a map-request for an actual SrcDst LCAF, ignore src eid
        if (dstEid.getAddress() instanceof SourceDestKey) {
            Eid srcAddr = SourceDestKeyHelper.getSrcBinary(dstEid);
            Eid dstAddr = SourceDestKeyHelper.getDstBinary(dstEid);
            return getMappingLpmSD(srcAddr, dstAddr, table);
        }

        // potential map-request for SrcDst LCAF from non SrcDst capable devices
        return getMappingLpmSD(srcEid, dstEid, table);
    }

    // Returns null for positive mappings, and 0/0 for empty cache.
    @Override
    public Eid getWidestNegativeMapping(Eid key) {
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return MaskUtil.normalize(key, (short) 0);
        }
        return table.getWidestNegativePrefix(key);
    }

    public void removeMapping(Eid eid) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }

        if (key.getAddress() instanceof SourceDestKey) {
            ILispDAO db = getSDInnerDao(key, table);
            if (db != null) {
                db.remove(SourceDestKeyHelper.getSrcBinary(key));
                if (db.isEmpty()) {
                    removeSDInnerDao(key, table);
                }
            }
        } else {
            table.remove(key);
        }
        if (table.isEmpty()) {
            removeVniTable(eid);
        }
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src.
    // This method returns the DAO associated to a dst or creates it if it doesn't exist.
    private ILispDAO getOrInstantiateSDInnerDao(Eid address, ILispDAO mappingsDb) {
        Eid dstKey = SourceDestKeyHelper.getDstBinary(address);
        ILispDAO srcDstDao = (ILispDAO) mappingsDb.getSpecific(dstKey, SubKeys.LCAF_SRCDST);
        if (srcDstDao == null) {
            // inserts nested table for source
            srcDstDao = mappingsDb.putNestedTable(dstKey, SubKeys.LCAF_SRCDST);
        }
        return srcDstDao;
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src.
    // This method returns the DAO associated to dst or null if it doesn't exist.
    private ILispDAO getSDInnerDao(Eid address, ILispDAO mappingsDb) {
        return (ILispDAO) mappingsDb.getSpecific(SourceDestKeyHelper.getDstBinary(address), SubKeys.LCAF_SRCDST);
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
        final IRowVisitor vniVisitor = (new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append(key + "\t");
                }
                if ((valueKey.equals(SubKeys.LCAF_SRCDST))) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(innerVisitor);
                    sb.append("}\t");
                } else {
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
                if (valueKey.equals(SubKeys.VNI)) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(vniVisitor);
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

    private void removeSDInnerDao(Eid address, ILispDAO mappingsDb) {
        mappingsDb.removeSpecific(SourceDestKeyHelper.getDstBinary(address), SubKeys.LCAF_SRCDST);
    }

    @Override
    public void addData(Eid eid, String subKey, Object data) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getOrInstantiateVniTable(key);

        if (key.getAddress() instanceof SourceDestKey) {
            ILispDAO srcDstDao = getOrInstantiateSDInnerDao(key, table);
            srcDstDao.put(SourceDestKeyHelper.getSrcBinary(key), new MappingEntry<>(subKey, data));
        } else {
            table.put(key, new MappingEntry<>(subKey, data));
        }
    }

    @Override
    public Object getData(Eid eid, String subKey) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return null;
        }

        if (key.getAddress() instanceof SourceDestKey) {
            ILispDAO srcDstDao = getSDInnerDao(key, table);
            return srcDstDao.getSpecific(SourceDestKeyHelper.getSrcBinary(key), subKey);
        } else {
            return table.getSpecific(key, subKey);
        }
    }

    @Override
    public void removeData(Eid eid, String subKey) {
        Eid key = MaskUtil.normalize(eid);
        ILispDAO table = getVniTable(key);
        if (table == null) {
            return;
        }
        if (key.getAddress() instanceof SourceDestKey) {
            ILispDAO db = getSDInnerDao(key, table);
            if (db != null) {
                db.removeSpecific(SourceDestKeyHelper.getSrcBinary(key), subKey);
                if (db.isEmpty()) {
                    removeSDInnerDao(key, table);
                }
            }
        } else {
            table.removeSpecific(key, subKey);
        }
        if (table.isEmpty()) {
            removeVniTable(eid);
        }
    }
}
