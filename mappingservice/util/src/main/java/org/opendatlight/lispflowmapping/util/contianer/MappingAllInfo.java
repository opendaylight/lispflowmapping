/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendatlight.lispflowmapping.util.contianer;

import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;

/**
 * Created by sheikahm on 9/27/16.
 */
public class MappingAllInfo {
    private ILispDAO dao;
    private DataStoreBackEnd dsbe;
    private MappingRecord mappingRecord;

    private SiteId siteId;

    public ILispDAO getDao() {
        return dao;
    }

    public void setDao(ILispDAO dao) {
        this.dao = dao;
    }

    public DataStoreBackEnd getDsbe() {
        return dsbe;
    }

    public void setDsbe(DataStoreBackEnd dsbe) {
        this.dsbe = dsbe;
    }

    public MappingRecord getMappingRecord() {
        return mappingRecord;
    }

    public void setMappingRecord(MappingRecord mappingRecord) {
        this.mappingRecord = mappingRecord;
    }

    public SiteId getSiteId() {
        return siteId;
    }

    public void setSiteId(SiteId siteId) {
        this.siteId = siteId;
    }
}
