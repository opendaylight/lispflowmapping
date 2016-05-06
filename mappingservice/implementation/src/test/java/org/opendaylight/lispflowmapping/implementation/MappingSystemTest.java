/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
        .ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.ServicePathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
        .address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(LoggerFactory.class)
public class MappingSystemTest {

    @Mock private static IMapCache smcMock;
    @Mock private static IMapCache pmcMock;
    @Spy private static ILispDAO daoSpy = Mockito.spy(new HashMapDb());
    @Mock private static DataStoreBackEnd dsbeMock;
    @InjectMocks private static MappingSystem mappingSystem = new MappingSystem(daoSpy, false, true, true);

    private static final String IPV4_SRC = "127.0.0.1";
    private static final String IPV4_DST = "192.168.0.1";
    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";

    private static final Eid EID_IPV4_SRC = LispAddressUtil.asIpv4Eid(IPV4_SRC);
    private static final Eid EID_IPV4_DST = LispAddressUtil.asIpv4Eid(IPV4_DST);
    private static final Eid EID_IPV4 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid EID_SERVICE_PATH = new EidBuilder().setAddress(getDefaultServicePath()).build();

    private static final Rloc RLOC = LispAddressUtil.asIpv4Rloc(IPV4_STRING_2);

    private static final ConfigIni CONFIG_INI = ConfigIni.getInstance();
    private static final long REGISTRATION_VALIDITY = CONFIG_INI.getRegistrationValiditySb();
    private static final Date EXPIRED_DATE = new Date(System.currentTimeMillis() - (REGISTRATION_VALIDITY + 1L));

    private static final SiteId SITE_ID = new SiteId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});

    @Before
    public void init() throws Exception {
        Mockito.when(daoSpy.putTable(MappingOrigin.Southbound.toString())).thenReturn(Mockito.mock(HashMapDb.class));
        Mockito.when(daoSpy.putTable(MappingOrigin.Northbound.toString())).thenReturn(Mockito.mock(HashMapDb.class));
        injectMocks();
    }

    /**
     * This method injects mocks which can not be injected the usual way using Mockito.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void injectMocks() throws NoSuchFieldException, IllegalAccessException {
        final Field smcField = MappingSystem.class.getDeclaredField("smc");
        smcField.setAccessible(true);
        smcField.set(mappingSystem, smcMock);

        final Field pmcField = MappingSystem.class.getDeclaredField("pmc");
        pmcField.setAccessible(true);
        pmcField.set(mappingSystem, pmcMock);
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with NB mapping == null, expired mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withNullExpiredNbMapping() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setTimestamp(EXPIRED_DATE.getTime()).build();
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(null);
        Mockito.when(smcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(mappingRecord);

        // expected result
        final Mapping mapping = new MappingBuilder()
                .setEidUri(new EidUri("ipv4:1.2.3.0"))
                .setOrigin(MappingOrigin.Southbound)
                .setSiteId(Lists.newArrayList(SITE_ID))
                .setMappingRecord(mappingRecord).build();

        assertNull(mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
        Mockito.verify(dsbeMock).removeMapping(mapping);
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with NB mapping == null, not expired mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withNullNbMapping() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder().build();
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(null);
        Mockito.when(smcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(mappingRecord);

        assertEquals(mappingRecord, mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with dst address of type ServicePath and multiple locator record.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_multipleLocatorRecords() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build(), // set two locators
                        getDefaultLocatorRecordBuilder().build())).build();
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingRecord);

        assertEquals(mappingRecord, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with dst address of type ServicePath and single locator record of
     * type Ipv4.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_singleIpv4LocatorRecord() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build())).build(); // Ipv4 type Rloc
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingRecord);

        assertEquals(mappingRecord, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with dst address of type ServicePath and single locator record of
     * type ExplicitLocatorPath.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_singleExplicitLocPathLocatorRecord() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder()
                                .setRloc(getExplicitLocatorPathTypeRloc()).build())).build();
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingRecord);

        assertEquals(mappingRecord, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH));
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setSiteId(SITE_ID)
                .setEid(EID_IPV4);
    }

    private static ServicePath getDefaultServicePath() {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
                .service.path.ServicePathBuilder servicePathBuilder = new org.opendaylight.yang.gen.v1.urn.ietf.params
                .xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePathBuilder();

        return new ServicePathBuilder().setServicePath(servicePathBuilder
                .setServiceIndex((short) 253)
                .setServicePathId(new ServicePathIdType(1L)).build())
                .build();
    }

    private static LocatorRecordBuilder getDefaultLocatorRecordBuilder() {
        return new LocatorRecordBuilder()
                .setLocatorId("locator-id")
                .setRloc(RLOC);
    }

    private static Rloc getExplicitLocatorPathTypeRloc() {
        final Hop hop = new HopBuilder().setHopId("hop-id").build();
        final ExplicitLocatorPath explicitLocatorPath = new ExplicitLocatorPathBuilder().setExplicitLocatorPath(new org
                .opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.explicit.locator.path.ExplicitLocatorPathBuilder().setHop(Lists.newArrayList(hop)).build())
                .build();

        return new RlocBuilder()
                .setAddress(explicitLocatorPath)
                .setAddressType(ExplicitLocatorPathLcaf.class).build();
    }
}
