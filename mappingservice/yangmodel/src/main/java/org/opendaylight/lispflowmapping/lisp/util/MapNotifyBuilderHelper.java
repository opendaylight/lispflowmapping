/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapnotifymessage.MapNotifyBuilder;

public class MapNotifyBuilderHelper {

    public static void setFromMapRegister(MapNotifyBuilder builder, MapRegister mapRegister) {
        if (builder.getEidToLocatorRecord() == null) {
            builder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        }
        builder.setNonce(mapRegister.getNonce());
        builder.setKeyId(mapRegister.getKeyId());
        byte[] authenticationData = mapRegister.getAuthenticationData();
        if (authenticationData != null) {
            authenticationData = authenticationData.clone();
            Arrays.fill(authenticationData, (byte) 0);
        }
        builder.setAuthenticationData(authenticationData);

        for (EidToLocatorRecord eidToLocator : mapRegister.getEidToLocatorRecord()) {
            builder.getEidToLocatorRecord().add(eidToLocator);
        }
    }
}
