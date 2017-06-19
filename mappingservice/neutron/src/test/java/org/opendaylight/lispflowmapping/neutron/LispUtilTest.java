/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.util.LispUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
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

public class LispUtilTest {
    private static final int RECORD_TTL = 1440;
    private static final String IPV4_1 = "192.168.0.1";
    private static final String IPV4_2 = "192.168.0.2";
    private static final String KEY = "key";
    private static final Eid EID = LispAddressUtil.asIpv4Eid(IPV4_1);
    private static final LocatorRecord LOCATOR_RECORD = getDefaultLocatorRecord();

    /**
     * Tests {@link LispUtil#buildAddMappingInput} method.
     */
    @Test
    public void buildAddMappingInputTest() {
        final MappingRecord mappingRecord = new MappingRecordBuilder()
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(true).setEid(EID)
                .setLocatorRecord(Lists.newArrayList(getDefaultLocatorRecord()))
                .setMapVersion((short) 0)
                .setRecordTtl(RECORD_TTL).build();

        final AddMappingInput expectedResult = new AddMappingInputBuilder().setMappingRecord(mappingRecord).build();
        final AddMappingInput result = LispUtil.buildAddMappingInput(EID, Lists.newArrayList(LOCATOR_RECORD));

        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link LispUtil#buildAddKeyInput} method.
     */
    @Test
    public void buildAddKeyInputTest() {
        final AddKeyInput expectedResult = new AddKeyInputBuilder()
                .setEid(EID)
                .setMappingAuthkey(new MappingAuthkeyBuilder()
                        .setKeyString(KEY)
                        .setKeyType(1).build()).build();
        final AddKeyInput result = LispUtil.buildAddKeyInput(EID, KEY);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link LispUtil#buildGetMappingInput} method.
     */
    @Test
    public void buildGetMappingInputTest() {
        final GetMappingInput expectedResult = new GetMappingInputBuilder().setEid(EID).build();
        final GetMappingInput result = LispUtil.buildGetMappingInput(EID);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link LispUtil#buildRemoveMappingInput} method.
     */
    @Test
    public void buildRemoveMappingInputTest() {
        final RemoveMappingInput expectedResult = new RemoveMappingInputBuilder().setEid(EID).build();
        final RemoveMappingInput result = LispUtil.buildRemoveMappingInput(EID);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests {@link LispUtil#buildRemoveKeyInput} method.
     */
    @Test
    public void buildRemoveKeyInputTest() {
        final RemoveKeyInput expectedResult = new RemoveKeyInputBuilder().setEid(EID).build();
        final RemoveKeyInput result = LispUtil.buildRemoveKeyInput(EID);

        assertEquals(expectedResult, result);
    }

    private static LocatorRecord getDefaultLocatorRecord() {
        return new LocatorRecordBuilder()
                .setRloc(new RlocBuilder()
                        .setAddress(new Ipv4Builder()
                                .setIpv4(new Ipv4Address(IPV4_2)).build()).build()).build();
    }
}
