/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.neutron;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.AddMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.GetMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.RemoveMappingInputBuilder;

/**
 * LispUtil class has util functions for building inputs for LISP service RPCs.
 *
 *
 *
 */


public class LispUtil {

  //  private static final Logger LOG = LoggerFactory.getLogger(LispUtil.class);

    public static AddMappingInput buildAddMappingInput(LispAddressContainer eid, List<LocatorRecord> locators, int mask) {
        AddMappingInputBuilder mib = new AddMappingInputBuilder();

        mib.setAction(Action.NoAction).setAuthoritative(true).setLispAddressContainer(eid)
                .setLocatorRecord(locators).setMapVersion((short) 0).setMaskLength((short) mask)
                .setRecordTtl(1440);
        return mib.build();
    }

    public static AddKeyInput buildAddKeyInput(LispAddressContainer eid, String net, int mask) {
        AddKeyInputBuilder kib = new AddKeyInputBuilder();

        kib.setLispAddressContainer(eid).setAuthkey(net).setMaskLength((short)mask).setKeyType(1);
        return kib.build();
    }

    public static GetMappingInput buildGetMappingInput(LispAddressContainer eid, short mask) {
        return new GetMappingInputBuilder().setLispAddressContainer(eid).setMaskLength(mask).build();
    }

    public static RemoveMappingInput buildRemoveMappingInput(LispAddressContainer eid, int mask) {
        RemoveMappingInputBuilder rmib = new RemoveMappingInputBuilder();
        rmib.setLispAddressContainer(eid).setMaskLength((short)mask);
        return rmib.build();
    }
    public static RemoveKeyInput buildRemoveKeyInput(LispAddressContainer eid, int mask) {
        RemoveKeyInputBuilder kib = new RemoveKeyInputBuilder();

        kib.setLispAddressContainer(eid).setMaskLength((short)mask);
        return kib.build();
    }
}