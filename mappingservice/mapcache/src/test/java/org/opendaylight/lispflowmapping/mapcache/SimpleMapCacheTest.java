/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.mapcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Date;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;

public class SimpleMapCacheTest {

    private static ILispDAO tableMock;
    private static ILispDAO xtrIdDaoMock;
    private static MappingData mappingDataMock;
    private static ILispDAO daoMock;
    private static SimpleMapCache simpleMapCache;

    private static final String IPV4_STRING_1 =      "1.2.3.0";
    private static final String IPV4_STRING_DST =    "192.168.0.1";
    private static final String IPV4_PREFIX_STRING = "/24";
    private static final short MASK = 24;
    private static final long VNI_0 = 0L;
    private static final long VNI_100 = 100L;
    private static final byte[] IPV4_RLOC_BINARY = new byte[] {0, 1, 4, 0};
    private static final XtrId XTR_ID = new XtrId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

    private static final Eid EID_IPV4_PREFIX_1_VNI = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING, new InstanceIdType(VNI_100));
    private static final Eid EID_IPV4_PREFIX_2 = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final Eid EID_IPV4_PREFIX_DST = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_DST + IPV4_PREFIX_STRING);
    private static final Eid EID_IPV4 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid NORMALIZED_EID_1 = MaskUtil.normalize(EID_IPV4_PREFIX_1_VNI);
    private static final Eid NORMALIZED_EID_2 = MaskUtil.normalize(EID_IPV4_PREFIX_2);
    private static final Eid NORMALIZED_EID_IPV4 = MaskUtil.normalize(EID_IPV4);
    private static final Eid NORMALIZED_EID_IPV4_PREFIX_DST = MaskUtil.normalize(EID_IPV4_PREFIX_DST, (short) 24);

    private static final IpAddressBinary IP_ADDRESS = new IpAddressBinary(new Ipv4AddressBinary(IPV4_RLOC_BINARY));
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder()
            .setKeyString("pass")
            .setKeyType(1).build();

    @Before
    public void init() {
        daoMock = Mockito.mock(ILispDAO.class, "dao");
        tableMock = Mockito.mock(ILispDAO.class);
        xtrIdDaoMock = Mockito.mock(ILispDAO.class);
        mappingDataMock = Mockito.mock(MappingData.class);
        simpleMapCache = new SimpleMapCache(daoMock);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with dstEid == null.
     */
    @Test
    public void getMappingTest_withNullDstEid() {
        assertNull(simpleMapCache.getMapping(null, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with VNI_100 table == null.
     */
    @Test
    public void getMappingTest_withNullVniTable() {
        Mockito.when(daoMock.getSpecific(VNI_100, SubKeys.VNI)).thenReturn(null);
        assertNull(simpleMapCache.getMapping(EID_IPV4_PREFIX_1_VNI, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#removeMapping} method with xTR-ID.
     */
    @Test
    public void removeMappingTest_withXtrId() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_IPV4, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);

        simpleMapCache.removeMapping(EID_IPV4, XTR_ID);
        Mockito.verify(xtrIdDaoMock).removeSpecific(XTR_ID, SubKeys.RECORD);
    }

    /**
     * Tests {@link SimpleMapCache#removeMapping} method.
     */
    @Test
    public void removeMappingTest() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.removeMapping(EID_IPV4);
        Mockito.verify(tableMock).removeSpecific(MaskUtil.normalize(EID_IPV4), SubKeys.RECORD);
        Mockito.verify(tableMock).removeSpecific(MaskUtil.normalize(EID_IPV4), SubKeys.SRC_RLOCS);
        Mockito.verify(tableMock).removeSpecific(MaskUtil.normalize(EID_IPV4), SubKeys.XTRID_RECORDS);
        Mockito.verifyNoMoreInteractions(tableMock);
    }

    /**
     * Tests {@link SimpleMapCache#removeMapping} method with null VNI_100 table.
     */
    @Test
    public void removeMappingTest_withNullVniTable() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.removeMapping(EID_IPV4);
        Mockito.verifyNoMoreInteractions(tableMock);
    }

    /**
     * Tests {@link SimpleMapCache#addAuthenticationKey} method.
     */
    @Test
    public void addAuthenticationKeyTest() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.addAuthenticationKey(EID_IPV4, MAPPING_AUTHKEY);
        Mockito.verify(tableMock)
                .put(MaskUtil.normalize(EID_IPV4), new MappingEntry<>(SubKeys.AUTH_KEY, MAPPING_AUTHKEY));
    }

    /**
     * Tests {@link SimpleMapCache#getAuthenticationKey} method with maskable address.
     */
    @Test
    public void getAuthenticationKeyTest_withMaskableAddress() {
        Mockito.when(daoMock.getSpecific(VNI_100, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(MaskUtil.normalize(EID_IPV4_PREFIX_1_VNI, MASK), SubKeys.AUTH_KEY))
                .thenReturn(MAPPING_AUTHKEY);

        assertEquals(MAPPING_AUTHKEY, simpleMapCache.getAuthenticationKey(EID_IPV4_PREFIX_1_VNI));
    }

    /**
     * Tests {@link SimpleMapCache#getAuthenticationKey} method with non maskable address.
     */
    @Test
    public void addAuthenticationKeyTest_withNonMaskableAddress() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY)).thenReturn(MAPPING_AUTHKEY);

        assertEquals(MAPPING_AUTHKEY, simpleMapCache.getAuthenticationKey(EID_IPV4));
        Mockito.verify(tableMock).getSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY);
    }

    /**
     * Tests {@link SimpleMapCache#getAuthenticationKey} method with no MappingAuthkey.
     */
    @Test
    public void addAuthenticationKeyTest_withNoMappingAuthkey() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY)).thenReturn(null);

        assertNull(simpleMapCache.getAuthenticationKey(EID_IPV4));
    }

    /**
     * Tests {@link SimpleMapCache#getAuthenticationKey} method with no VNI_100 table.
     */
    @Test
    public void addAuthenticationKeyTest_withNullVniTable() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        assertNull(simpleMapCache.getAuthenticationKey(EID_IPV4));
    }

    /**
     * Tests {@link SimpleMapCache#removeAuthenticationKey} method.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.removeAuthenticationKey(EID_IPV4);
        Mockito.verify(tableMock).removeSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY);
    }

    /**
     * Tests {@link SimpleMapCache#removeAuthenticationKey} method with no VNI_100 table.
     */
    @Test
    public void removeAuthenticationKeyTest_withNoVniTable() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.removeAuthenticationKey(EID_IPV4);
        Mockito.verify(tableMock, Mockito.never()).removeSpecific(Mockito.any(Eid.class), Mockito.anyString());
    }

    /**
     * Tests {@link SimpleMapCache#getAllXtrIdMappings} method with maskable address.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getAllXtrIdMappings_withMaskableAddress() {
        final Eid normalizedKey = MaskUtil.normalize(EID_IPV4_PREFIX_1_VNI, MASK);
        final Map<String, Object> entryMock = Mockito.mock(Map.class);
        final ILispDAO xtrIdRecordsMock = Mockito.mock(ILispDAO.class);

        Mockito.when(daoMock.getBest(normalizedKey)).thenReturn(entryMock);
        Mockito.when(entryMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdRecordsMock);
        Mockito.when(xtrIdRecordsMock.getSpecific(EID_IPV4_PREFIX_1_VNI, SubKeys.XTRID_RECORDS))
                .thenReturn(xtrIdDaoMock);
        simpleMapCache.getAllXtrIdMappings(EID_IPV4_PREFIX_1_VNI);

        Mockito.verify(daoMock).getBest(Mockito.any(Eid.class));
    }

    /**
     * Tests {@link SimpleMapCache#getAllXtrIdMappings} method with non maskable address.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getAllXtrIdMappings_withNonMaskableAddress() {
        final Map<String, Object> entryMock = Mockito.mock(Map.class);
        final ILispDAO xtrIdRecordsMock = Mockito.mock(ILispDAO.class);

        Mockito.when(daoMock.getBest(NORMALIZED_EID_IPV4)).thenReturn(entryMock);
        Mockito.when(entryMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdRecordsMock);
        Mockito.when(xtrIdRecordsMock.getSpecific(EID_IPV4, SubKeys.XTRID_RECORDS))
                .thenReturn(xtrIdDaoMock);
        simpleMapCache.getAllXtrIdMappings(EID_IPV4);

        Mockito.verify(daoMock).getBest(Mockito.any(Eid.class));
    }

    /**
     * Tests {@link SimpleMapCache#getAllXtrIdMappings} method with null daoEntry.
     */
    @Test
    public void getAllXtrIdMappings_withNullEntry() {
        Mockito.when(daoMock.getBest(Mockito.any(Eid.class))).thenReturn(null);

        assertNull(simpleMapCache.getAllXtrIdMappings(EID_IPV4_PREFIX_1_VNI));
        Mockito.verify(daoMock, Mockito.times(1)).getBest(Mockito.any(Eid.class));
    }

    /**
     * Tests {@link SimpleMapCache#getMappingLpmEid} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getMappingLpmEidTest() throws Exception {
        final Map<String, Object> mapMock = Mockito.mock(Map.class);
        final SimpleImmutableEntry<Eid, Map<String, ?>> mapPair = new SimpleImmutableEntry<>(
                NORMALIZED_EID_IPV4_PREFIX_DST, mapMock);
        final ILispDAO xtrIdRecordsMock = Mockito.mock(ILispDAO.class);
        final MappingData mappingData = new MappingData(getDefaultMappingRecordBuilder().build());

        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getBestPair(NORMALIZED_EID_IPV4_PREFIX_DST)).thenReturn(mapPair);
        Mockito.when(mapMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdRecordsMock);
        Mockito.when(xtrIdRecordsMock.getSpecific(EID_IPV4_PREFIX_DST, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(xtrIdDaoMock.getSpecific(XTR_ID, SubKeys.RECORD))
                .thenReturn(mappingData);       // second invocation

        // with non-expired mapping record
        assertEquals(mappingData, simpleMapCache.getMapping(EID_IPV4_PREFIX_DST, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getMappingLpmEid} method with null XtrId.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getMappingLpmEidTest_withNullXtrId() throws Exception {
        final Map<String, Object> mapMock = Mockito.mock(Map.class);
        final SimpleImmutableEntry<Eid, Map<String, ?>> mapPair = new SimpleImmutableEntry<>(
                NORMALIZED_EID_IPV4_PREFIX_DST, mapMock);
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getBestPair(MaskUtil.normalize(EID_IPV4_PREFIX_DST, (short) 24))).thenReturn(mapPair);

        simpleMapCache.getMapping(EID_IPV4_PREFIX_DST, (XtrId) null);
        Mockito.verify(tableMock).getBestPair(NORMALIZED_EID_IPV4_PREFIX_DST);
        Mockito.verify(mapMock).get(SubKeys.RECORD);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with maskable eid.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getMappingTest_withMaskableEid() {
        final Eid ipv4PrefixEid = LispAddressUtil.asIpv4PrefixEid("192.168.0.225" + "/32");
        final Map<String, Object> entryMock = Mockito.mock(Map.class);
        final SimpleImmutableEntry<Eid, Map<String, ?>> mapPair = new SimpleImmutableEntry<>(
                NORMALIZED_EID_IPV4_PREFIX_DST, entryMock);

        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getBestPair(ipv4PrefixEid)).thenReturn(mapPair);
        Mockito.when(entryMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(xtrIdDaoMock.getSpecific(NORMALIZED_EID_IPV4_PREFIX_DST, SubKeys.XTRID_RECORDS)).thenReturn(null);

        simpleMapCache.getMapping(ipv4PrefixEid, XTR_ID);
        Mockito.verify(entryMock).get(SubKeys.XTRID_RECORDS);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with maskable eid and entry not found.
     */
    @Test
    public void getMappingTest_withMaskableEid_noEntry() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.get(Mockito.any(Eid.class))).thenReturn(null);

        assertNull(simpleMapCache.getMapping(EID_IPV4_PREFIX_DST, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with non-maskable eid.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getMappingTest_withNonMaskableEid() {
        final Map<String, Object> entryMock = Mockito.mock(Map.class);
        final SimpleImmutableEntry<Eid, Map<String, ?>> mapPair = new SimpleImmutableEntry<>(
                NORMALIZED_EID_IPV4_PREFIX_DST, entryMock);

        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getBestPair(NORMALIZED_EID_IPV4)).thenReturn(mapPair);
        Mockito.when(entryMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);

        simpleMapCache.getMapping(EID_IPV4, XTR_ID);
        Mockito.verify(entryMock).get(SubKeys.XTRID_RECORDS);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with non-maskable eid and entry not found.
     */
    @Test
    public void getMappingTest_withNonMaskableEid_noEntry() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.get(NORMALIZED_EID_IPV4)).thenReturn(null);

        assertNull(simpleMapCache.getMapping(EID_IPV4, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with Eid VNI_100 == null.
     */
    @Test
    public void getVniTableTest_withVniNull() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.getMapping(EID_IPV4_PREFIX_2, XTR_ID);
        Mockito.verify(daoMock).getSpecific(VNI_0, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#getVniTable} method with Eid VNI_100 == 100L.
     */
    @Test
    public void getVniTableTest_withVniNotNull() {
        Mockito.when(daoMock.getSpecific(VNI_100, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.getMapping(EID_IPV4_PREFIX_1_VNI, XTR_ID);
        Mockito.verify(daoMock).getSpecific(VNI_100, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#addData} method.
     */
    @Test
    public void addDataTest() {
        final Object dummyData = "dummy-data";
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.addData(EID_IPV4, SubKeys.RECORD, dummyData);
        Mockito.verify(tableMock).put(NORMALIZED_EID_IPV4, new MappingEntry<>(SubKeys.RECORD, dummyData));
    }

    /**
     * Tests {@link SimpleMapCache#getData} method.
     */
    @Test
    public void getDataTest() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.getData(EID_IPV4, SubKeys.RECORD);
        Mockito.verify(tableMock).getSpecific(NORMALIZED_EID_IPV4, SubKeys.RECORD);
    }

    /**
     * Tests {@link SimpleMapCache#getData} method with no VNI_100 table.
     */
    @Test
    public void getDataTest_withNullVniTable() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.getData(EID_IPV4, SubKeys.RECORD);
        Mockito.verifyNoMoreInteractions(tableMock);
    }

    /**
     * Tests {@link SimpleMapCache#removeData} method.
     */
    @Test
    public void removeDataTest() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.removeData(EID_IPV4, SubKeys.RECORD);
        Mockito.verify(tableMock).removeSpecific(NORMALIZED_EID_IPV4, SubKeys.RECORD);
    }

    /**
     * Tests {@link SimpleMapCache#removeData} method with no VNI_100 table.
     */
    @Test
    public void removeDataTest_withNullTable() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.removeData(EID_IPV4, SubKeys.RECORD);
        Mockito.verifyNoMoreInteractions(tableMock);
    }

    /**
     * Tests {@link SimpleMapCache#addMapping} method.
     */
    @Test
    public void addMappingTest() throws Exception {
        final Date timestamp = new Date(System.currentTimeMillis());
        Mockito.when(mappingDataMock.getTimestamp()).thenReturn(timestamp);
        Mockito.when(daoMock.getSpecific(VNI_100, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_1, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(mappingDataMock.getXtrId()).thenReturn(XTR_ID);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, mappingDataMock);
        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_1, new MappingEntry<>(SubKeys.RECORD, mappingDataMock));
    }

    /**
     * Tests {@link SimpleMapCache#getOrInstantiateVniTable} method with vni == null.
     */
    @Test
    public void getOrInstantiateVniTableTest_withNullVni() throws Exception {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_2, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_2, mappingDataMock); // Eid VNI_100 == null
        Mockito.verify(daoMock).getSpecific(VNI_0, SubKeys.VNI);
        Mockito.verify(daoMock, Mockito.never()).putNestedTable(VNI_0, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#getOrInstantiateVniTable} method with vni == null, table == null.
     */
    @Test
    public void getOrInstantiateVniTableTest_withNullVniAndTable() throws Exception {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_2, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(daoMock.putNestedTable(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_2, mappingDataMock); // Eid VNI_100 == null
        Mockito.verify(daoMock).putNestedTable(VNI_0, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#getOrInstantiateVniTable} method with vni == 100L, table == null.
     */
    @Test
    public void getOrInstantiateVniTableTest_withNullTable() throws Exception {
        Mockito.when(daoMock.getSpecific(VNI_100, SubKeys.VNI)).thenReturn(null);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_1, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(daoMock.putNestedTable(VNI_100, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, mappingDataMock); // Eid VNI_100 == null
        Mockito.verify(daoMock).putNestedTable(VNI_100, SubKeys.VNI);
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setEid(EID_IPV4)
                .setLocatorRecord(Lists.newArrayList())
                .setTimestamp(Long.MAX_VALUE)
                .setRecordTtl(10)
                .setAction(MappingRecord.Action.NativelyForward)
                .setSourceRloc(IP_ADDRESS);
    }
}
