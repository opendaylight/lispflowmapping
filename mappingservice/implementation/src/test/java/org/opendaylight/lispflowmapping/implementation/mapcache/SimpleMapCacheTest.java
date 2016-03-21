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
import java.util.Date;
import java.util.List;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address
        .Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
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
    private static MappingRecord mergedEntryMock;
    private static ILispDAO daoMock;
    private static SimpleMapCache simpleMapCache;

    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";
    private static final String IPV4_STRING_3 = "127.0.0.1";
    private static final String IPV4_STRING_4 = "192.168.0.1";
    private static final String IPV4_PREFIX_STRING = "/24";
    private static final Eid IPV4_EID = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final Ipv4Prefix IPV_4_PREFIX_SRC = new Ipv4Prefix(IPV4_STRING_3 + IPV4_PREFIX_STRING);
    private static final Ipv4Prefix IPV_4_PREFIX_DST = new Ipv4Prefix(IPV4_STRING_4 + IPV4_PREFIX_STRING);
    private static final long VNI = 100L;
    private static final byte[] XTR_ID = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private static final Eid EID_IPV4_PREFIX_SRC = new EidBuilder().setVirtualNetworkId(new InstanceIdType(VNI))
            .setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(IPV_4_PREFIX_SRC).build()).build();
    private static final Eid EID_IPV4_PREFIX_DST = new EidBuilder().setVirtualNetworkId(new InstanceIdType(VNI))
            .setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(IPV_4_PREFIX_DST).build()).build();
    private static final Eid NORMALIZED_EID_SRC = MaskUtil.normalize(EID_IPV4_PREFIX_SRC);
    private static final IpAddress IP_ADDRESS = new IpAddress(new Ipv4Address(IPV4_STRING_2));

    private static final ConfigIni CONFIG_INI = ConfigIni.getInstance();

    @Before
    public void init() {
        daoMock = Mockito.mock(ILispDAO.class);
        tableMock = Mockito.mock(ILispDAO.class);
        xtrIdDaoMock = Mockito.mock(ILispDAO.class);
        mappingRecordMock = Mockito.mock(MappingRecord.class);
        mergedEntryMock = Mockito.mock(MappingRecord.class);
        simpleMapCache = PowerMockito.spy(new SimpleMapCache(daoMock));
    }

    /**
     * Tests {@link SimpleMapCache#addMapping} with (value = null), (!(value instanceof MappingRecord)).
     */
    @Test
    public void addMappingTest_withFalseValues() {
        simpleMapCache.addMapping(EID_IPV4_PREFIX_SRC, null, true);
        simpleMapCache.addMapping(EID_IPV4_PREFIX_SRC, new Object(), true);
        Mockito.verifyZeroInteractions(daoMock);
    }

    /**
     * Tests {@link SimpleMapCache#addMapping} method with mappin merge allowed.
     */
    @Test
    public void addMappingTest_mappingMergeTrue() throws Exception {
        setConfigIniMappingMergeField(true);

        Mockito.when(mappingRecordMock.getTimestamp()).thenReturn(System.currentTimeMillis());
        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_SRC, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(mappingRecordMock.getXtrId()).thenReturn(new XtrId(XTR_ID));

        Set<IpAddress> ipAddresses = Sets.newHashSet(IP_ADDRESS);
        List<Object> records = Lists.newArrayList(getDefaultMappingRecordBuilder().build());
        PowerMockito.doReturn(records).when(simpleMapCache,         // stubs private getXtrIdMappingList method
                PowerMockito.method(SimpleMapCache.class, "getXtrIdMappingList", ILispDAO.class))
                .withArguments(xtrIdDaoMock);

        simpleMapCache.addMapping(EID_IPV4_PREFIX_SRC, mappingRecordMock, false);
        Mockito.verify(xtrIdDaoMock).put(new XtrId(XTR_ID), new MappingEntry<>(SubKeys.RECORD, mappingRecordMock));

        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_SRC, new MappingEntry<>(SubKeys.REGDATE, new Date(Long.MAX_VALUE)));
        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_SRC, new MappingEntry<>(SubKeys.RECORD, getDefaultMappingRecordBuilder().build()));
        Mockito.verify(tableMock).put(NORMALIZED_EID_SRC, new MappingEntry<>(SubKeys.SRC_RLOCS, ipAddresses));
    }

    /**
     * Tests {@link SimpleMapCache#addMapping} method with mappin merge allowed.
     */
    @Test
    public void addMappingTest_mappingMergeFalse() throws Exception {
        setConfigIniMappingMergeField(false);

        Mockito.when(mappingRecordMock.getTimestamp()).thenReturn(Long.MAX_VALUE);
        Mockito.when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_SRC, SubKeys.XTRID_RECORDS)).thenReturn(xtrIdDaoMock);
        Mockito.when(mappingRecordMock.getXtrId()).thenReturn(new XtrId(XTR_ID));

        simpleMapCache.addMapping(EID_IPV4_PREFIX_SRC, mappingRecordMock, false);
        Mockito.verify(xtrIdDaoMock).put(new XtrId(XTR_ID), new MappingEntry<>(SubKeys.RECORD, mappingRecordMock));

        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_SRC, new MappingEntry<>(SubKeys.REGDATE, new Date(Long.MAX_VALUE)));
        Mockito.verify(tableMock)
                .put(NORMALIZED_EID_SRC, new MappingEntry<>(SubKeys.RECORD, mappingRecordMock));
        Mockito.verifyNoMoreInteractions(tableMock);
    }

    /**
     * Tests {@link SimpleMapCache#getMapping}.
     */
    @Test
    @Ignore
    public void getMappingTest() {
        assertNull(simpleMapCache.getMapping(EID_IPV4_PREFIX_SRC, null, XTR_ID));

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
