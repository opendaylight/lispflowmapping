package org.opendaylight.lispflowmapping.implementation.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifymessage.MapNotifyBuilder;

public class MapNotifyBuilderHelper {

    public static void setFromMapRegister(MapNotifyBuilder builder, MapRegister mapRegister) {
        if (builder.getEidToLocatorRecord() == null) {
            builder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        }
        builder.setNounce(mapRegister.getNounce());
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
