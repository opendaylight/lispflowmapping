/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.LispAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequestnotification.MapRequestBuilder;

/**
 * Utilities to prepare MapRegister and MapRequest object for interaction with
 * the IFlowMapping API.
 *
 * @author Lorand Jakab
 *
 */
public class MapServerMapResolverUtil {

    public static MapRegister getMapRegister(EidToLocatorRecord mapping) {
        MapRegisterBuilder mrb = new MapRegisterBuilder();
        mrb.setEidToLocatorRecord(getEidToLocatorRecord(mapping));
        return mrb.build();
    }

    public static MapRequest getMapRequest(LispAddress address) {
        MapRequestBuilder mrb = new MapRequestBuilder();
        mrb.setPitr(false);
        mrb.setEidRecord(getEidRecord(address));
        return mrb.build();
    }

    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord> getEidToLocatorRecord(EidToLocatorRecord mapping) {
        EidToLocatorRecordBuilder etlrb = new EidToLocatorRecordBuilder();
        etlrb.setRecordTtl(mapping.getRecordTtl());
        etlrb.setMaskLength(mapping.getMaskLength());
        etlrb.setMapVersion(mapping.getMapVersion());
        etlrb.setAction(mapping.getAction());
        etlrb.setAuthoritative(mapping.isAuthoritative());
        etlrb.setEid(mapping.getEid());
        etlrb.setLocatorRecord(mapping.getLocatorRecord());

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord> mappings =
                new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord>();
        mappings.add(etlrb.build());
        return mappings;
    }

    public static List<EidRecord> getEidRecord(LispAddress address) {
        EidRecordBuilder erb = new EidRecordBuilder();
        erb.setAddress(address.getAddress());
        erb.setAfi(address.getAfi());
        erb.setInstanceId(address.getInstanceId());

        List<EidRecord> records = new ArrayList<EidRecord>();
        records.add(erb.build());
        return records;
    }
}
