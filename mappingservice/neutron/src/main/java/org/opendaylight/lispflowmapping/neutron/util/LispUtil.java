/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.util;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInputBuilder;

/**
 * LispUtil class has util functions for building inputs for LISP service RPCs.
 */
public final class LispUtil {
    // Utility class, should not be instantiated
    private LispUtil() {
    }

  //  private static final Logger LOG = LoggerFactory.getLogger(LispUtil.class);

    public static AddMappingInput buildAddMappingInput(Eid eid, List<LocatorRecord> locators) {
        MappingRecordBuilder mrb = new MappingRecordBuilder();

        mrb.setAction(Action.NoAction).setAuthoritative(true).setEid(eid)
                .setLocatorRecord(locators).setMapVersion((short) 0)
                .setRecordTtl(1440);

        AddMappingInputBuilder mib = new AddMappingInputBuilder();
        mib.setMappingRecord(mrb.build());

        return mib.build();
    }

    public static AddKeyInput buildAddKeyInput(Eid eid, String net) {
        AddKeyInputBuilder kib = new AddKeyInputBuilder();

        kib.setEid(eid).setMappingAuthkey(new MappingAuthkeyBuilder().setKeyString(net).setKeyType(1).build());
        return kib.build();
    }

    public static GetMappingInput buildGetMappingInput(Eid eid) {
        return new GetMappingInputBuilder().setEid(eid).build();
    }

    public static RemoveMappingInput buildRemoveMappingInput(Eid eid) {
        RemoveMappingInputBuilder rmib = new RemoveMappingInputBuilder();
        rmib.setEid(eid);
        return rmib.build();
    }

    public static RemoveKeyInput buildRemoveKeyInput(Eid eid) {
        RemoveKeyInputBuilder kib = new RemoveKeyInputBuilder();

        kib.setEid(eid);
        return kib.build();
    }
}
