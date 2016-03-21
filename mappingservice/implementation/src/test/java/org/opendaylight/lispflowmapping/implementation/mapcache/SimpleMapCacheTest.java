/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mapcache;

import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SimpleMapCache.class)
public class SimpleMapCacheTest {

    private static ILispDAO tableMock;
    private static ILispDAO xtrIdDaoMock;
    private static MappingRecord mappingRecordMock;
    private static ILispDAO daoMock;
    private static SimpleMapCache simpleMapCache;

    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";
    private static final String IPV4_STRING_SRC = "127.0.0.1";
    private static final String IPV4_STRING_DST = "192.168.0.1";
    private static final String IPV4_PREFIX_STRING = "/24";
    private static final short MASK = 24;
    private static final Eid IPV4_EID = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final long VNI = 100L;
    private static final byte[] XTR_ID = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private static final Eid EID_IPV4_PREFIX_1_VNI = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING, new InstanceIdType(VNI));
    private static final Eid EID_IPV4_PREFIX_2 = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final Eid EID_IPV4_PREFIX_SRC = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_SRC + IPV4_PREFIX_STRING);
    private static final Eid EID_IPV4_PREFIX_DST = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_DST + IPV4_PREFIX_STRING);
    private static final Eid EID_IPV4 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid NORMALIZED_EID_1 = MaskUtil.normalize(EID_IPV4_PREFIX_1_VNI);
    private static final Eid NORMALIZED_EID_2 = MaskUtil.normalize(EID_IPV4_PREFIX_2);
    private static final IpAddress IP_ADDRESS = new IpAddress(new Ipv4Address(IPV4_STRING_2));

    private static final ConfigIni CONFIG_INI = ConfigIni.getInstance();

    @Before
    public void init() {
        daoMock = Mockito.mock(ILispDAO.class, "dao");
        tableMock = Mockito.mock(ILispDAO.class);
        xtrIdDaoMock = Mockito.mock(ILispDAO.class);
        mappingRecordMock = Mockito.mock(MappingRecord.class);
        simpleMapCache = PowerMockito.spy(new SimpleMapCache(daoMock));
    }

    /**
     * Tests {@link SimpleMapCache#addMapping} method where no interaction is expected with the dao.
     */
    @Test
    public void addMappingTest_noDaoInteraction() throws Exception {
        setConfigIniMappingMergeField(true);

        Mockito.when(mappingRecordMock.getXtrId()).thenReturn(null);
        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, null, true);
        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, new Object(), true);
        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, mappingRecordMock, false);

        Mockito.verifyZeroInteractions(daoMock);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with dstEid == null.
     */
    @Test
    public void getMapping_withNullDstEid() {
        assertNull(simpleMapCache.getMapping(null, null, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with VNI table == null.
     */
    @Test
    public void getMapping_withNullVniTable() {
        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(null);
        assertNull(simpleMapCache.getMapping(null, EID_IPV4_PREFIX_1_VNI, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getMappingLpmEid} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getMappingLpmEidTest() throws Exception {
        // TODO
        final AbstractMap.SimpleImmutableEntry<Eid, Map<String, Object>> daoEntryMock =
                Mockito.mock(AbstractMap.SimpleImmutableEntry.class);
        final Map<String, Object> mapMock = Mockito.mock(Map.class);

        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(tableMock);
        PowerMockito.doReturn(daoEntryMock)
                .when(simpleMapCache,
                        PowerMockito.method(SimpleMapCache.class, "getDaoPairEntryBest", Eid.class, ILispDAO.class))
                .withArguments(EID_IPV4_PREFIX_DST, tableMock);
        Mockito.when(daoEntryMock.getValue()).thenReturn(mapMock);
        Mockito.when(mapMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        // getMappingLpmEid(Eid dstEid, byte[] xtrId, ILispDAO table)

        simpleMapCache.getMapping(null, EID_IPV4_PREFIX_DST, XTR_ID);
    }

    /**
     * Tests {@link SimpleMapCache#getDaoPairEntryBest} method with maskable eid.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getDaoPairEntryBest_withMaskableEid() {
        final Eid normalizedKey = MaskUtil.normalize(EID_IPV4_PREFIX_DST, MASK);
        final Map<String, Object> entryMock = Mockito.mock(Map.class);
        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.get(normalizedKey)).thenReturn(entryMock);
        Mockito.when(entryMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);

        simpleMapCache.getMapping(EID_IPV4_PREFIX_SRC, EID_IPV4_PREFIX_DST, XTR_ID);
        Mockito.verify(entryMock).get(SubKeys.XTRID_RECORDS);
    }

    /**
     * Tests {@link SimpleMapCache#getDaoPairEntryBest} method with maskable eid and entry not found.
     */
    @Test
    public void getDaoPairEntryBest_withMaskableEid_noEntry() {
        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.get(Mockito.any(Eid.class))).thenReturn(null);

        assertNull(simpleMapCache.getMapping(EID_IPV4_PREFIX_SRC, EID_IPV4_PREFIX_DST, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getDaoPairEntryBest} method with non-maskable eid.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getDaoPairEntryBest_withNonMaskableEid() {
        final Eid normalizedKey = MaskUtil.normalize(EID_IPV4);
        final Map<String, Object> entryMock = Mockito.mock(Map.class);
        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.get(normalizedKey)).thenReturn(entryMock);
        Mockito.when(entryMock.get(SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);

        simpleMapCache.getMapping(EID_IPV4_PREFIX_SRC, EID_IPV4, XTR_ID);
        Mockito.verify(entryMock).get(SubKeys.XTRID_RECORDS);
    }

    /**
     * Tests {@link SimpleMapCache#getDaoPairEntryBest} method with non-maskable eid and entry not found.
     */
    @Test
    public void getDaoPairEntryBest_withNonMaskableEid_noEntry() {
        final Eid normalizedKey = MaskUtil.normalize(EID_IPV4);
        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.get(normalizedKey)).thenReturn(null);

        assertNull(simpleMapCache.getMapping(EID_IPV4_PREFIX_SRC, EID_IPV4, XTR_ID));
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with Eid VNI == null.
     */
    @Test
    public void getVniTable_withVniNull() {
        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.getMapping(null, EID_IPV4_PREFIX_2, XTR_ID);
        Mockito.verify(daoMock).getSpecific(0L, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping} method with Eid VNI == 100L.
     */
    @Test
    public void getVniTable_withVniNotNull() {
        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(null);

        simpleMapCache.getMapping(null, EID_IPV4_PREFIX_1_VNI, XTR_ID);
        Mockito.verify(daoMock).getSpecific(VNI, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#addMapping} method with mappin merge allowed.
     */
    @Test
    public void addMappingTest_mappingMergeTrue() throws Exception {
        setConfigIniMappingMergeField(true);

        Mockito.when(mappingRecordMock.getTimestamp()).thenReturn(System.currentTimeMillis());
        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_1, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(mappingRecordMock.getXtrId()).thenReturn(new XtrId(XTR_ID));

        Set<IpAddress> ipAddresses = Sets.newHashSet(IP_ADDRESS);
        List<Object> records = Lists.newArrayList(getDefaultMappingRecordBuilder().build());
        PowerMockito.doReturn(records).when(simpleMapCache,         // stubs private getXtrIdMappingList method
                PowerMockito.method(SimpleMapCache.class, "getXtrIdMappingList", ILispDAO.class))
                .withArguments(xtrIdDaoMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, mappingRecordMock, false);
        Mockito.verify(xtrIdDaoMock).put(new XtrId(XTR_ID), new MappingEntry<>(SubKeys.RECORD, mappingRecordMock));

        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_1, new MappingEntry<>(SubKeys.REGDATE, new Date(Long.MAX_VALUE)));
        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_1, new MappingEntry<>(SubKeys.RECORD, getDefaultMappingRecordBuilder().build()));
        Mockito.verify(tableMock).put(NORMALIZED_EID_1, new MappingEntry<>(SubKeys.SRC_RLOCS, ipAddresses));
    }

    /**
     * Tests {@link SimpleMapCache#addMapping} method with mappin merge allowed.
     */
    @Test
    public void addMappingTest_mappingMergeFalse() throws Exception {
        setConfigIniMappingMergeField(false);

        Mockito.when(mappingRecordMock.getTimestamp()).thenReturn(Long.MAX_VALUE);
        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_1, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(mappingRecordMock.getXtrId()).thenReturn(new XtrId(XTR_ID));

        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, mappingRecordMock, false);
        Mockito.verify(xtrIdDaoMock).put(new XtrId(XTR_ID), new MappingEntry<>(SubKeys.RECORD, mappingRecordMock));

        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_1, new MappingEntry<>(SubKeys.REGDATE, new Date(Long.MAX_VALUE)));
        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_1, new MappingEntry<>(SubKeys.RECORD, mappingRecordMock));
    }

    /**
     * Tests {@link SimpleMapCache#getOrInstantiateVniTable} method with vni == null.
     */
    @Test
    public void getOrInstantiateVniTableTest_withNullVni() throws Exception {
        setConfigIniMappingMergeField(false);

        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_2, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_2, mappingRecordMock, false); // Eid VNI == null
        Mockito.verify(daoMock).getSpecific(0L, SubKeys.VNI);
        Mockito.verify(daoMock, Mockito.never()).putNestedTable(0L, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#getOrInstantiateVniTable} method with vni == null, table == null.
     */
    @Test
    public void getOrInstantiateVniTableTest_withNullVniAndTable() throws Exception {
        setConfigIniMappingMergeField(false);

        Mockito.when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(null);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_2, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(daoMock.putNestedTable(0L, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_2, mappingRecordMock, true); // Eid VNI == null
        Mockito.verify(daoMock).putNestedTable(0L, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#getOrInstantiateVniTable} method with vni == 100L, table == null.
     */
    @Test
    public void getOrInstantiateVniTableTest_withNullTable() throws Exception {
        setConfigIniMappingMergeField(false);

        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(null);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_1, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(daoMock.putNestedTable(VNI, SubKeys.VNI)).thenReturn(tableMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_1_VNI, mappingRecordMock, false); // Eid VNI == null
        Mockito.verify(daoMock).putNestedTable(VNI, SubKeys.VNI);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping}.
     */
    @Test
    @Ignore
    public void getMappingTest() {
        assertNull(simpleMapCache.getMapping(EID_IPV4_PREFIX_1_VNI, null, XTR_ID));
        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableMock);
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setEid(IPV4_EID)
                .setLocatorRecord(Lists.newArrayList())
                .setTimestamp(Long.MAX_VALUE)
                .setRecordTtl(10)
                .setAction(MappingRecord.Action.NativelyForward)
                .setSourceRloc(IP_ADDRESS);
    }

    private static void setConfigIniMappingMergeField(boolean value) throws NoSuchFieldException,
            IllegalAccessException {
        final Field mappingMergeField = CONFIG_INI.getClass().getDeclaredField("mappingMerge");
        mappingMergeField.setAccessible(true);
        mappingMergeField.setBoolean(CONFIG_INI, value);
    }
}
