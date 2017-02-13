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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.config.ConfigIni;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.util.MappingMergeUtil;
import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IAuthKeyDb;
import org.opendaylight.lispflowmapping.interfaces.mapcache.ILispMapCache;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMapCache;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(MappingMergeUtil.class)
public class MappingSystemTest {

    private static ILispDAO daoMock = Mockito.mock(ILispDAO.class);
    @Mock private static ILispMapCache smcMock;
    @Mock private static IMapCache pmcMock;
    @Mock private static IAuthKeyDb akdbMock;
    @Mock private static DataStoreBackEnd dsbeMock;
    @Mock private static EnumMap<MappingOrigin, IMapCache> tableMapMock;
    @InjectMocks private static MappingSystem mappingSystem = new MappingSystem(daoMock, false, true, true);

    private static final String IPV4_SRC =      "127.0.0.1";
    private static final String IPV4_DST =      "192.168.0.1";
    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";
    private static final String IPV4_STRING_3 = "1.2.5.0";
    private static final String MASK =          "/20";

    private static final Eid EID_IPV4_SRC = LispAddressUtil.asIpv4Eid(IPV4_SRC);
    private static final Eid EID_IPV4_DST = LispAddressUtil.asIpv4Eid(IPV4_DST);
    private static final Eid EID_IPV4_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid EID_IPV4_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid EID_SERVICE_PATH = new EidBuilder().setAddress(getDefaultServicePath((short) 253)).build();
    private static final Eid EID_SERVICE_PATH_INDEX_OOB = new EidBuilder()
            .setAddress(getDefaultServicePath((short) 200)).build(); // with index out of bounds

    public static final SimpleAddress SIMPLE_ADDR_1 = new SimpleAddress(new IpAddress(new Ipv4Address(IPV4_STRING_1)));
    public static final SimpleAddress SIMPLE_ADDR_2 = new SimpleAddress(new IpAddress(new Ipv4Address(IPV4_STRING_2)));
    public static final SimpleAddress SIMPLE_ADDR_3 = new SimpleAddress(new IpAddress(new Ipv4Address(IPV4_STRING_3)));

    private static final Hop HOP_1 = new HopBuilder().setHopId("hop-id-1").setAddress(SIMPLE_ADDR_1).build();
    private static final Hop HOP_2 = new HopBuilder().setHopId("hop-id-2").setAddress(SIMPLE_ADDR_2).build();
    private static final Hop HOP_3 = new HopBuilder().setHopId("hop-id-3").setAddress(SIMPLE_ADDR_3).build();

    private static final MappingAuthkeyBuilder MAPPING_AUTHKEY_BUILDER = new MappingAuthkeyBuilder()
            .setKeyType(1).setKeyString("pass-1");
    private static final Rloc RLOC = LispAddressUtil.toRloc(new Ipv4Address(IPV4_STRING_2));
    private static final ConfigIni CONFIG_INI = ConfigIni.getInstance();
    private static final long REGISTRATION_VALIDITY = CONFIG_INI.getRegistrationValiditySb();
    private static final Date EXPIRED_DATE = new Date(System.currentTimeMillis() - (REGISTRATION_VALIDITY + 1L));
    private static final Object DUMMY_OBJECT = "dummy-object";
    private static final SiteId SITE_ID = new SiteId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
    private static final XtrId XTR_ID = new XtrId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7,
                                                                8, 9, 10, 11, 12, 13, 14, 15});

    @Before
    public void init() throws Exception {
        Mockito.when(daoMock.putTable(MappingOrigin.Southbound.toString())).thenReturn(Mockito.mock(HashMapDb.class));
        Mockito.when(daoMock.putTable(MappingOrigin.Northbound.toString())).thenReturn(Mockito.mock(HashMapDb.class));

        Mockito.when(tableMapMock.get(MappingOrigin.Southbound)).thenReturn(smcMock);
        Mockito.when(tableMapMock.get(MappingOrigin.Northbound)).thenReturn(pmcMock);

        injectMocks();
        setLookupPolicy(IMappingService.LookupPolicy.NB_FIRST);
    }

    /**
     * This method injects mocks which can not be injected the usual way using Mockito.
     */
    private static void injectMocks() throws NoSuchFieldException, IllegalAccessException {
        final Field smcField = MappingSystem.class.getDeclaredField("smc");
        smcField.setAccessible(true);
        smcField.set(mappingSystem, smcMock);

        final Field pmcField = MappingSystem.class.getDeclaredField("pmc");
        pmcField.setAccessible(true);
        pmcField.set(mappingSystem, pmcMock);

        final Field tableMapField = MappingSystem.class.getDeclaredField("tableMap");
        tableMapField.setAccessible(true);
        tableMapField.set(mappingSystem, tableMapMock);
    }

    /**
     * This method changes the lookup policy.
     */
    private static void setLookupPolicy(IMappingService.LookupPolicy policy) throws NoSuchFieldException,
            IllegalAccessException {
        final Field lookupPolicy = ConfigIni.class.getDeclaredField("lookupPolicy");
        lookupPolicy.setAccessible(true);
        lookupPolicy.set(CONFIG_INI, policy);
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with NB mapping == null, expired mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withNullExpiredNbMapping() {
        final MappingData mappingData = getDefaultMappingData();
        mappingData.setTimestamp(EXPIRED_DATE);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(null);
        Mockito.when(smcMock.getMapping(EID_IPV4_DST, (XtrId) null)).thenReturn(mappingData);

        final Mapping mapping = new MappingBuilder()
                .setEidUri(new EidUri("ipv4:" + IPV4_DST))
                .setOrigin(MappingOrigin.Southbound)
                .setSiteId(Lists.newArrayList(SITE_ID))
                .setMappingRecord(mappingData.getRecord()).build();

        assertNull(mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
        Mockito.verify(dsbeMock).removeMapping(mapping);
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with NB mapping == null, not expired mapping.
     * Returns the original mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withNullNbMapping() {
        final MappingData mappingData = getDefaultMappingData();
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(null);
        Mockito.when(smcMock.getMapping(EID_IPV4_DST, (XtrId) null)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with ServicePath type dst address and multiple locator record.
     * Returns the original mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_multipleLocatorRecords() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build(), // set two locators
                        getDefaultLocatorRecordBuilder().build())).build();
        final MappingData mappingData = getDefaultMappingData(mappingRecord);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with ServicePath type dst address and single Ipv4 type locator
     * record. Returns the original mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_singleIpv4LocatorRecord() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build())).build(); // Ipv4 type Rloc
        final MappingData mappingData = getDefaultMappingData(mappingRecord);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with ServicePath type dst address and single ExplicitLocatorPath
     * type locator record. Service index out of bounds. Returns the original mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_singleELPLocatorRecord_IndexOutOfBounds() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder()
                                .setRloc(getELPTypeRloc()).build())).build();
        final MappingData mappingData = getDefaultMappingData(mappingRecord);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH_INDEX_OOB)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH_INDEX_OOB));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with ServicePath type dst address and single ExplicitLocatorPath
     * type locator record.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_singleELPLocatorRecord()
            throws UnknownHostException {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder()
                                .setRloc(getELPTypeRloc()).build())).build();
        final MappingData mappingData = getDefaultMappingData(mappingRecord);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingData);

        final MappingData result = (MappingData) mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH);
        final Ipv4Binary ipv4Result = (Ipv4Binary) result.getRecord().getLocatorRecord().get(0).getRloc().getAddress();
        assertTrue(Arrays
                .equals(InetAddress.getByName(IPV4_STRING_3).getAddress(), ipv4Result.getIpv4Binary().getValue()));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with ServicePath type dst address and single IpPrefix type locator
     * record. Returns the original mapping.
     */
    @Test
    public void getMappingTest_NbFirst_withServicePathDestinationAddress_IpPrefixLocatorRecord() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder()
                                .setRloc(getIpPrefixTypeRloc()).build())).build();
        final MappingData mappingData = getDefaultMappingData(mappingRecord);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method with Ipv4 type dst address.
     */
    @Test
    public void getMappingTest_NbFirst() {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build())).build();
        final MappingData mappingData = getDefaultMappingData(mappingRecord);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method, northbound and southbound intersection.
     */
    @Test
    public void getMappingTest_NbSbIntersection_withNullNbMapping() throws NoSuchFieldException,
            IllegalAccessException {
        setLookupPolicy(IMappingService.LookupPolicy.NB_AND_SB);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(null);

        assertNull(mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method, northbound and southbound intersection with ServicePath type dst
     * address and single Ipv4 type locator record. Returns the original mapping.
     */
    @Test
    public void getMappingTest_NbSbIntersection_withServicePathDestinationAddress() throws NoSuchFieldException,
            IllegalAccessException {
        setLookupPolicy(IMappingService.LookupPolicy.NB_AND_SB);
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build())).build(); // Ipv4 type Rloc
        final MappingData mappingData = getDefaultMappingData(mappingRecord);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_SERVICE_PATH));
    }

    /**
     * Tests {@link MappingSystem#getMapping} method, northbound and southbound intersection with single Ipv4 type
     * locator, southbound null. Returns the original mapping.
     */
    @Test
    public void getMappingTest_NbSbIntersection_withSbNull() throws NoSuchFieldException,
            IllegalAccessException {
        setLookupPolicy(IMappingService.LookupPolicy.NB_AND_SB);

        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build())).build(); // Ipv4 type Rloc
        MappingData nbMappingData = getDefaultMappingData(mappingRecord);
        MappingData sbMappingData = getDefaultMappingData(mappingRecord);
        sbMappingData.setTimestamp(EXPIRED_DATE);

        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(nbMappingData);
        Mockito.when(smcMock.getMapping(EID_IPV4_DST, (XtrId) null)).thenReturn(sbMappingData);

        final Mapping mapping = new MappingBuilder()
                .setEidUri(new EidUri("ipv4:" + IPV4_DST))
                .setOrigin(MappingOrigin.Southbound)
                .setSiteId(Lists.newArrayList(SITE_ID))
                .setMappingRecord(mappingRecord).build();

        assertEquals(nbMappingData, mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
        Mockito.verify(dsbeMock).removeMapping(mapping);
    }

    /**
     * Tests {@link MappingSystem#getMapping} method, northbound and southbound intersection with single Ipv4 type
     * locator, southbound non-null. Returns the merged mapping.
     */
    @Test
    public void getMappingTest_NbSbIntersection_mergeMappings() throws NoSuchFieldException,
            IllegalAccessException {
        setLookupPolicy(IMappingService.LookupPolicy.NB_AND_SB);

        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder()
                .setLocatorRecord(Lists.newArrayList(
                        getDefaultLocatorRecordBuilder().build())).build();
        final MappingData nbMappingData = getDefaultMappingData(mappingRecord);
        final MappingData sbMappingData = getDefaultMappingData(mappingRecord);

        // this mock will be ultimately returned when MappingMergeUtil.computeNbSbIntersection is called
        final MappingData resultMock = Mockito.mock(MappingData.class);

        PowerMockito.mockStatic(MappingMergeUtil.class);
        Mockito.when(pmcMock.getMapping(EID_IPV4_SRC, EID_IPV4_DST)).thenReturn(nbMappingData);
        Mockito.when(smcMock.getMapping(EID_IPV4_DST, (XtrId) null)).thenReturn(sbMappingData);
        PowerMockito.when(MappingMergeUtil.computeNbSbIntersection(nbMappingData, sbMappingData))
                .thenReturn(resultMock);

        assertEquals(resultMock, mappingSystem.getMapping(EID_IPV4_SRC, EID_IPV4_DST));
    }

    /**
     * Tests {@link MappingSystem#getMapping(Eid dst)} method.
     */
    @Test
    public void getMappingTest_withSrcNull() {
        final MappingData mappingData = getDefaultMappingData();
        Mockito.when(pmcMock.getMapping(null, EID_IPV4_DST)).thenReturn(null);
        Mockito.when(smcMock.getMapping(EID_IPV4_DST, (XtrId) null)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(EID_IPV4_DST));
    }

    /**
     * Tests {@link MappingSystem#getMapping(Eid src, Eid dst, XtrId xtrId)} method.
     */
    @Test
    public void getMappingTest_withXtrId() {
        final MappingData mappingData = getDefaultMappingData();
        mappingData.setXtrId(XTR_ID);

        Mockito.when(smcMock.getMapping(EID_IPV4_DST, XTR_ID)).thenReturn(mappingData);

        assertEquals(mappingData, mappingSystem.getMapping(null, EID_IPV4_DST, XTR_ID));
    }

    /**
     * Tests {@link MappingSystem#getMapping(MappingOrigin, Eid)} method.
     */
    @Test
    public void getMappingTest_withMappingOrigin() {
        final MappingData mappingData = getDefaultMappingData();
        Mockito.when(smcMock.getMapping(EID_IPV4_SRC, (XtrId) null)).thenReturn(mappingData);

        mappingSystem.getMapping(MappingOrigin.Southbound, EID_IPV4_SRC);
        Mockito.verify(smcMock).getMapping(EID_IPV4_SRC, (XtrId) null);

        mappingSystem.getMapping(MappingOrigin.Northbound, EID_IPV4_SRC);
        Mockito.verify(pmcMock).getMapping(null, EID_IPV4_SRC);
    }

    /**
     * Tests {@link MappingSystem#removeMapping} method.
     */
    @Test
    public void removeMappingTest_nb() {
        Mockito.when(tableMapMock.get(MappingOrigin.Northbound)).thenReturn(pmcMock);

        mappingSystem.removeMapping(MappingOrigin.Northbound, EID_IPV4_1);
        Mockito.verify(pmcMock).removeMapping(EID_IPV4_1);
    }

    /**
     * Tests {@link MappingSystem#removeMapping} method.
     */
    @Test
    public void removeMappingTest_sb() throws NoSuchFieldException, IllegalAccessException {
        mappingSystem = new MappingSystem(daoMock, false, true, false);
        injectMocks();
        Mockito.when(tableMapMock.get(MappingOrigin.Southbound)).thenReturn(smcMock);

        mappingSystem.removeMapping(MappingOrigin.Southbound, EID_IPV4_1);
        Mockito.verify(smcMock).removeMapping(EID_IPV4_1);
    }

    /**
     * Tests {@link MappingSystem#restoreDaoFromDatastore} method.
     */
    @Test
    public void restoreDaoFromDatastoreTest() {
        final Mapping mapping_1 = new MappingBuilder()
                .setOrigin(MappingOrigin.Northbound)
                .setMappingRecord(getDefaultMappingRecordBuilder()
                        .setEid(EID_IPV4_1).build()).build();
        final Mapping mapping_2 = new MappingBuilder()
                .setOrigin(MappingOrigin.Northbound)
                .setMappingRecord(getDefaultMappingRecordBuilder()
                        .setEid(EID_IPV4_2).build()).build();

        final MappingAuthkey mappingAuthkey_1 = MAPPING_AUTHKEY_BUILDER.build();
        final AuthenticationKey authenticationKey_1 = new AuthenticationKeyBuilder()
                .setMappingAuthkey(mappingAuthkey_1)
                .setEid(EID_IPV4_1).build();

        final MappingAuthkey mappingAuthkey_2 = MAPPING_AUTHKEY_BUILDER.setKeyString("pass-2").build();
        final AuthenticationKey authenticationKey_2 = new AuthenticationKeyBuilder()
                .setMappingAuthkey(mappingAuthkey_2)
                .setEid(EID_IPV4_2).build();

        final List<Mapping> mappings = Lists.newArrayList(mapping_1, mapping_2);
        final List<AuthenticationKey> authenticationKeys = Lists.newArrayList(authenticationKey_1, authenticationKey_2);

        Mockito.when(dsbeMock.getLastUpdateTimestamp()).thenReturn(System.currentTimeMillis());
        Mockito.when(dsbeMock.getAllMappings(LogicalDatastoreType.CONFIGURATION)).thenReturn(mappings);
        Mockito.when(dsbeMock.getAllAuthenticationKeys()).thenReturn(authenticationKeys);
        Mockito.when(tableMapMock.get(MappingOrigin.Northbound)).thenReturn(pmcMock);

        mappingSystem.initialize();

        ArgumentCaptor<MappingData> captor = ArgumentCaptor.forClass(MappingData.class);
        Mockito.verify(pmcMock).addMapping(Mockito.eq(EID_IPV4_1), captor.capture());
        assertEquals(captor.getValue().getRecord(), mapping_1.getMappingRecord());
        Mockito.verify(pmcMock).addMapping(Mockito.eq(EID_IPV4_2), captor.capture());
        assertEquals(captor.getValue().getRecord(), mapping_2.getMappingRecord());
        Mockito.verify(akdbMock).addAuthenticationKey(EID_IPV4_1, mappingAuthkey_1);
        Mockito.verify(akdbMock).addAuthenticationKey(EID_IPV4_2, mappingAuthkey_2);
    }

    /**
     * Tests {@link MappingSystem#addAuthenticationKey} method.
     */
    @Test
    public void addAuthenticationKeyTest() {
        mappingSystem.addAuthenticationKey(EID_IPV4_1, MAPPING_AUTHKEY_BUILDER.build());
        Mockito.verify(akdbMock).addAuthenticationKey(EID_IPV4_1, MAPPING_AUTHKEY_BUILDER.build());
    }

    /**
     * Tests {@link MappingSystem#getAuthenticationKey} method.
     */
    @Test
    public void getAuthenticationKeyTest() {
        Mockito.when(akdbMock.getAuthenticationKey(EID_IPV4_1)).thenReturn(MAPPING_AUTHKEY_BUILDER.build());
        assertEquals(MAPPING_AUTHKEY_BUILDER.build(), mappingSystem.getAuthenticationKey(EID_IPV4_1));
    }

    /**
     * Tests {@link MappingSystem#removeAuthenticationKey} method.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        mappingSystem.removeAuthenticationKey(EID_IPV4_1);
        Mockito.verify(akdbMock).removeAuthenticationKey(EID_IPV4_1);
    }

    /**
     * Tests {@link MappingSystem#addData} method.
     */
    @Test
    public void addDataTest() {
        Mockito.when(tableMapMock.get(MappingOrigin.Northbound)).thenReturn(pmcMock);

        mappingSystem.addData(MappingOrigin.Northbound, EID_IPV4_1, SubKeys.RECORD, DUMMY_OBJECT);
        Mockito.verify(pmcMock).addData(EID_IPV4_1, SubKeys.RECORD, DUMMY_OBJECT);
    }

    /**
     * Tests {@link MappingSystem#getData} method.
     */
    @Test
    public void getDataTest() {
        Mockito.when(tableMapMock.get(MappingOrigin.Northbound)).thenReturn(pmcMock);
        Mockito.when(pmcMock.getData(EID_IPV4_1, SubKeys.RECORD)).thenReturn(DUMMY_OBJECT);

        assertEquals(DUMMY_OBJECT, mappingSystem.getData(MappingOrigin.Northbound, EID_IPV4_1, SubKeys.RECORD));
    }

    /**
     * Tests {@link MappingSystem#removeData} method.
     */
    @Test
    public void removeDataTest() {
        Mockito.when(tableMapMock.get(MappingOrigin.Northbound)).thenReturn(pmcMock);

        mappingSystem.removeData(MappingOrigin.Northbound, EID_IPV4_1, SubKeys.RECORD);
        Mockito.verify(pmcMock).removeData(EID_IPV4_1, SubKeys.RECORD);
    }

    /**
     * Tests {@link MappingSystem#cleanCaches} method.
     */
    @Test
    public void cleanCachesTest() {
        mappingSystem.cleanCaches();
        Mockito.verify(daoMock).removeAll();
    }

    /**
     * Following test are executed for coverage-increase purpose.
     */
    @Test
    public void otherTests() throws NoSuchFieldException, IllegalAccessException {
        mappingSystem.printMappings();
        Mockito.verify(pmcMock).printMappings();
        Mockito.verify(smcMock).printMappings();

        mappingSystem.destroy();
        mappingSystem = new MappingSystem(daoMock, true, true, true);
        mappingSystem.setDataStoreBackEnd(dsbeMock);
        mappingSystem.setMappingMerge(false);
        mappingSystem.setIterateMask(true);
    }

    private static MappingData getDefaultMappingData() {
        return getDefaultMappingData(null);
    }

    private static MappingData getDefaultMappingData(MappingRecord mappingRecord) {
        if (mappingRecord == null) {
            mappingRecord = getDefaultMappingRecordBuilder().build();
        }
        return new MappingData(mappingRecord, System.currentTimeMillis());
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setSiteId(SITE_ID)
                .setEid(EID_IPV4_DST);
    }

    private static ServicePath getDefaultServicePath(short index) {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
                .service.path.ServicePathBuilder servicePathBuilder = new org.opendaylight.yang.gen.v1.urn.ietf.params
                .xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePathBuilder();

        return new ServicePathBuilder().setServicePath(servicePathBuilder
                .setServiceIndex(index)
                .setServicePathId(new ServicePathIdType(1L)).build())
                .build();
    }

    private static LocatorRecordBuilder getDefaultLocatorRecordBuilder() {
        return new LocatorRecordBuilder()
                .setLocatorId("locator-id")
                .setRloc(RLOC);
    }

    private static Rloc getELPTypeRloc() {

        final ExplicitLocatorPath explicitLocatorPath = new ExplicitLocatorPathBuilder().setExplicitLocatorPath(new org
                .opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address
                .address.explicit.locator.path.ExplicitLocatorPathBuilder()
                .setHop(Lists.newArrayList(HOP_1, HOP_2, HOP_3)).build()).build();

        return new RlocBuilder()
                .setAddress(explicitLocatorPath)
                .setAddressType(ExplicitLocatorPathLcaf.class).build();
    }

    private static Rloc getIpPrefixTypeRloc() {

        final Address address = new Ipv4PrefixBuilder().setIpv4Prefix(new org.opendaylight.yang.gen.v1.urn.ietf.params
                .xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix(IPV4_STRING_1 + MASK)).build();

        return new RlocBuilder()
                .setAddress(address)
                .setAddressType(Ipv4PrefixAfi.class).build();
    }
}
