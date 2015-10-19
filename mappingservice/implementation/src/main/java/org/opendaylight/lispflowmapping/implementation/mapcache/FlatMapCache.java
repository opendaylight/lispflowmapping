/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.mapcache;

import java.util.Date;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.Eid;

/**
 * Flat key implementation of a map-cache. As the name suggests, no longest prefix matching is done for IP addresses
 * or their derivatives.
 *
 * @author Florin Coras
 *
 */

public class FlatMapCache implements IMapCache {
    private ILispDAO dao;

    public FlatMapCache(ILispDAO dao) {
        this.dao = dao;
    }

    @Override
    public void addMapping(Eid key, Object data, boolean shouldOverwrite) {
        dao.put(key, new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
        dao.put(key, new MappingEntry<>(SubKeys.RECORD, data));
    }

    @Override
    public Object getMapping(Eid srcKey, Eid dstKey) {
        return dao.getSpecific(dstKey, SubKeys.RECORD);
    }

    @Override
    public void removeMapping(Eid key, boolean overwrite) {
        dao.removeSpecific(key, SubKeys.RECORD);
    }

    @Override
    public void addAuthenticationKey(Eid key, String authKey) {
        dao.put(key, new MappingEntry<>(SubKeys.AUTH_KEY, authKey));
    }

    @Override
    public String getAuthenticationKey(Eid key) {
        Object data = dao.getSpecific(key, SubKeys.AUTH_KEY);
        if (data instanceof String) {
            return (String) data;
        } else {
            return null;
        }
    }

    @Override
    public void removeAuthenticationKey(Eid key) {
        dao.removeSpecific(key, SubKeys.AUTH_KEY);
    }

    @Override
    public void updateMappingRegistration(Eid key) {
        if (dao.get(key) != null) {
            dao.put(key, new MappingEntry<>(SubKeys.REGDATE, new Date(System.currentTimeMillis())));
        }
    }

    @Override
    public void addData(Eid key, String subKey, Object data) {
        dao.put(key, new MappingEntry<>(subKey, data));
    }

    @Override
    public Object getData(Eid key, String subKey) {
        return dao.getSpecific(key, subKey);
    }

    @Override
    public void removeData(Eid key, String subKey) {
        dao.removeSpecific(key, subKey);
    }

    @Override
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
}
