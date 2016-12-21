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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class MultiTableMapCacheTest {

    private static MultiTableMapCache multiTableMapCache;
    private static ILispDAO daoMock;
    private static ILispDAO srcDstDaoMock;
    private static ILispDAO tableDaoMock;
    private static ILispDAO dbMock;

    private static final String IPV4_STRING_1 = "127.0.0.1/24";
    private static final String IPV4_STRING_2 = "127.0.0.1/24";
    private static final Ipv4Prefix IPV_4_PREFIX_SRC = new Ipv4Prefix(IPV4_STRING_1);
    private static final Ipv4Prefix IPV_4_PREFIX_DST = new Ipv4Prefix(IPV4_STRING_2);
    private static final long VNI = 100L;

    private static final Eid EID_TEST = new EidBuilder().setVirtualNetworkId(new InstanceIdType(VNI))
            .setAddress(new Ipv4Builder().setIpv4(new Ipv4Address("10.0.0.1")).build()).build();
    private static final Eid EID_SOURCE_DEST_KEY_TYPE = new EidBuilder().setVirtualNetworkId(new InstanceIdType(VNI))
            .setAddress(new SourceDestKeyBuilder()
                    .setSourceDestKey(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp
                            .address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder()
                            .setSource(new SimpleAddress(new IpPrefix(IPV_4_PREFIX_SRC)))
                            .setDest(new SimpleAddress(new IpPrefix(IPV_4_PREFIX_DST))).build()).build()).build();
    private static final Eid EID_IPV4_PREFIX_SRC = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1);
    private static final Eid EID_IPV4_PREFIX_DST = new EidBuilder().setVirtualNetworkId(new InstanceIdType(VNI))
            .setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(IPV_4_PREFIX_DST).build()).build();
    private static final Eid NORMALIZED_SRCDST_EID = MaskUtil.normalize(EID_SOURCE_DEST_KEY_TYPE);
    private static final Eid NORMALIZED_PREFIX_SRC_EID = MaskUtil.normalize(EID_IPV4_PREFIX_SRC);
    private static final Eid NORMALIZED_EID = MaskUtil.normalize(EID_TEST);
    private static final Object DUMMY_OBJECT = "dummy_object";
    private static final Object DUMMY_OBJECT_2 = "mapping_lpm_eid";

    @Before
    public void init() {
        daoMock = mock(ILispDAO.class);
        tableDaoMock = mock(ILispDAO.class);
        srcDstDaoMock = mock(ILispDAO.class);
        multiTableMapCache = new MultiTableMapCache(daoMock);
        dbMock = mock(ILispDAO.class);
    }

    /**
     * Tests {@link MultiTableMapCache#addMapping} with SourceDestKey address type.
     */
    @Test
    public void addMappingTest_withSourceDestKey() {

        final Eid normalized_Eid = MaskUtil.normalize(EID_SOURCE_DEST_KEY_TYPE);
        final Eid dstKey = SourceDestKeyHelper.getDstBinary(normalized_Eid);
        final Eid srcKey = SourceDestKeyHelper.getSrcBinary(normalized_Eid);

        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.getSpecific(dstKey, SubKeys.LCAF_SRCDST)).thenReturn(srcDstDaoMock);

        multiTableMapCache.addMapping(EID_SOURCE_DEST_KEY_TYPE, DUMMY_OBJECT);
        verify(srcDstDaoMock).put(srcKey, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#addMapping} with other than SourceDestKey address type.
     */
    @Test
    public void addMappingTest_withoutSourceDestKey() {
        when(daoMock.getSpecific(0L, SubKeys.VNI)).thenReturn(tableDaoMock);

        multiTableMapCache.addMapping(EID_IPV4_PREFIX_SRC, DUMMY_OBJECT);
        verify(tableDaoMock).put(NORMALIZED_PREFIX_SRC_EID, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#getMapping} with SourceDestKey address type.
     */
    @Test
    public void getMappingTest_withSourceDestKey() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);

        final Eid dstAddr = SourceDestKeyHelper.getDstBinary(EID_SOURCE_DEST_KEY_TYPE);
        final Eid normalizedDstAddr = MaskUtil.normalize(dstAddr);

        final Eid srcAddr = SourceDestKeyHelper.getSrcBinary(EID_SOURCE_DEST_KEY_TYPE);
        final Eid normalizedSrcAddr = MaskUtil.normalize(srcAddr);

        final Map<String, Object> entry = getEntry1();
        final Map<String, Object> entry2 = getEntry2();

        when(tableDaoMock.getBest(normalizedDstAddr)).thenReturn(entry);
        when(srcDstDaoMock.getBest(normalizedSrcAddr)).thenReturn(entry2);

        assertEquals(DUMMY_OBJECT_2, multiTableMapCache.getMapping(null, EID_SOURCE_DEST_KEY_TYPE));
        assertNull(multiTableMapCache.getMapping(null, null));
        assertNull(multiTableMapCache.getMapping(EID_TEST, null));
    }

    /**
     * Tests {@link MultiTableMapCache#getMapping} with Ipv4 address type.
     */
    @Test
    public void getMappingTest_withIpv4() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.get(NORMALIZED_EID)).thenReturn(null);

        assertNull(multiTableMapCache.getMapping(null, EID_TEST));
    }

    /**
     * Tests {@link MultiTableMapCache#getMapping} with table == null.
     */
    @Test
    public void getMappingTest_withNullTable() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(null);
        assertNull(multiTableMapCache.getMapping(null, EID_TEST));
    }

    /**
     * Tests {@link MultiTableMapCache#getMapping} with Ipv4Prefix address type.
     */
    @Test
    public void getMappingTest_withIpv4Prefix() {
        final Eid key = MaskUtil.normalize(EID_IPV4_PREFIX_DST, (short) 24);
        final Eid key2 = MaskUtil.normalize(MaskUtil.normalize(EID_IPV4_PREFIX_SRC), (short) 24);

        final Map<String, Object> entry = getEntry1();
        final Map<String, Object> entry2 = getEntry2();

        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.getBest(key)).thenReturn(entry);
        when(srcDstDaoMock.getBest(LispAddressUtil.asIpPrefixBinaryEid(key2))).thenReturn(entry2);

        assertEquals(DUMMY_OBJECT_2, multiTableMapCache.getMapping(EID_IPV4_PREFIX_SRC, EID_IPV4_PREFIX_DST));
        assertEquals(DUMMY_OBJECT, multiTableMapCache.getMapping(null, EID_IPV4_PREFIX_DST));
    }

    /**
     * Tests {@link MultiTableMapCache#removeMapping} method with SourceDestKey type address.
     */
    @Test
    public void removeMappingTest() {

        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.getSpecific(SourceDestKeyHelper
                .getDstBinary(NORMALIZED_SRCDST_EID), SubKeys.LCAF_SRCDST)).thenReturn(dbMock);

        multiTableMapCache.removeMapping(EID_SOURCE_DEST_KEY_TYPE);
        verify(dbMock).remove(SourceDestKeyHelper.getSrcBinary(NORMALIZED_SRCDST_EID));
    }

    /**
     * Tests {@link MultiTableMapCache#removeMapping} method with Ipv4 type address.
     */
    @Test
    public void removeMappingTest_with() {

        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);

        multiTableMapCache.removeMapping(EID_TEST);
        verify(tableDaoMock).remove(NORMALIZED_EID);
    }

    /**
     * Tests {@link MultiTableMapCache#removeMapping} method with table == null.
     */
    @Test
    public void removeMappingTest_withNUllTable() {

        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(null);

        multiTableMapCache.removeMapping(EID_SOURCE_DEST_KEY_TYPE);
        verifyZeroInteractions(tableDaoMock);
        verifyZeroInteractions(dbMock);
    }

    /**
     * Tests {@link MultiTableMapCache#addData} with SourceDestKey address type.
     */
    @Test
    public void addDataTest_withSourceDestKey() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.getSpecific(SourceDestKeyHelper.getDstBinary(NORMALIZED_SRCDST_EID), SubKeys.LCAF_SRCDST))
                .thenReturn(srcDstDaoMock);

        multiTableMapCache.addData(EID_SOURCE_DEST_KEY_TYPE, SubKeys.RECORD, DUMMY_OBJECT);
        verify(srcDstDaoMock).put(SourceDestKeyHelper.getSrcBinary(NORMALIZED_SRCDST_EID),
                new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#addData} with other than SourceDestKey address type.
     */
    @Test
    public void addDataTest_withIpv4() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);

        multiTableMapCache.addData(EID_TEST, SubKeys.RECORD, DUMMY_OBJECT);
        verify(tableDaoMock).put(NORMALIZED_EID, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#getData} with SourceDestKey address type.
     */
    @Test
    public void getDataTest_withSourceDestKey() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.getSpecific(SourceDestKeyHelper.getDstBinary(NORMALIZED_SRCDST_EID), SubKeys.LCAF_SRCDST))
                .thenReturn(srcDstDaoMock);
        when(srcDstDaoMock.getSpecific(SourceDestKeyHelper.getSrcBinary(NORMALIZED_SRCDST_EID), SubKeys.RECORD))
                .thenReturn(DUMMY_OBJECT);

        assertEquals(DUMMY_OBJECT, multiTableMapCache.getData(EID_SOURCE_DEST_KEY_TYPE, SubKeys.RECORD));
    }

    /**
     * Tests {@link MultiTableMapCache#getData} with other than SourceDestKey address type.
     */
    @Test
    public void getDataTest_withIpv4() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.getSpecific(NORMALIZED_EID, SubKeys.RECORD)).thenReturn(DUMMY_OBJECT);

        assertEquals(DUMMY_OBJECT, multiTableMapCache.getData(EID_TEST, SubKeys.RECORD));
    }

    /**
     * Tests {@link MultiTableMapCache#getData} with table == null.
     */
    @Test
    public void getDataTest_withNullTable() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(null);
        assertNull(multiTableMapCache.getData(EID_TEST, SubKeys.RECORD));
    }

    /**
     * Tests {@link MultiTableMapCache#removeData} with SourceDestKey address type.
     */
    @Test
    public void removeDataTest_withSourceDestKey() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);
        when(tableDaoMock.getSpecific(SourceDestKeyHelper.getDstBinary(NORMALIZED_SRCDST_EID), SubKeys.LCAF_SRCDST))
                .thenReturn(srcDstDaoMock);

        multiTableMapCache.removeData(EID_SOURCE_DEST_KEY_TYPE, SubKeys.RECORD);
        verify(srcDstDaoMock).removeSpecific(SourceDestKeyHelper.getSrcBinary(NORMALIZED_SRCDST_EID), SubKeys.RECORD);
    }

    /**
     * Tests {@link MultiTableMapCache#removeData} with other than SourceDestKey address type.
     */
    @Test
    public void removeDataTest_withIpv4() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(tableDaoMock);

        multiTableMapCache.removeData(EID_TEST, SubKeys.RECORD);
        verify(tableDaoMock).removeSpecific(NORMALIZED_EID, SubKeys.RECORD);
    }

    /**
     * Tests {@link MultiTableMapCache#removeData} with table == null.
     */
    @Test
    public void removeDataTest_withNullTable() {
        when(daoMock.getSpecific(VNI, SubKeys.VNI)).thenReturn(null);
        multiTableMapCache.removeData(EID_TEST, SubKeys.RECORD);

        Mockito.verifyZeroInteractions(tableDaoMock);
    }

    /**
     * The following test is for coverage-increase purpose only.
     */
    @Test
    public void otherTest() {
        multiTableMapCache.printMappings();
    }

    private Map<String, Object> getEntry1() {
        Map<String, Object> entry = Maps.newConcurrentMap();
        entry.put(SubKeys.RECORD, DUMMY_OBJECT);
        entry.put(SubKeys.LCAF_SRCDST, srcDstDaoMock);

        return entry;
    }

    private Map<String, Object> getEntry2() {
        Map<String, Object> entry = Maps.newConcurrentMap();
        entry.put(SubKeys.RECORD, DUMMY_OBJECT_2);

        return entry;
    }
}
