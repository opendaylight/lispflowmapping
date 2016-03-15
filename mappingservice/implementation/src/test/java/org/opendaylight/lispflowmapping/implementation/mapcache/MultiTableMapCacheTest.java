/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mapcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.lisp.util.SourceDestKeyHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;

public class MultiTableMapCacheTest {

    private static MultiTableMapCache MULTI_TABLE_MAP_CACHE;
    private static ILispDAO DAO_TEST;
    private static final Ipv4Address IPV_4_ADDRESS_SRC = new Ipv4Address("127.0.0.1");
    private static final Ipv4Address IPV_4_ADDRESS_DST = new Ipv4Address("192.168.0.1");
    private static final Eid EID_TEST = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100L))
            .setAddress(new Ipv4Builder().setIpv4(IPV_4_ADDRESS_SRC).build()).build();
    private static final Eid EID_SOURCE_DEST_KEY_TYPE = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100L))
            .setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder()
                    .setSourceDestKey(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder()
                            .setSource(new SimpleAddress(new IpAddress(IPV_4_ADDRESS_SRC)))
                            .setDest(new SimpleAddress(new IpAddress(IPV_4_ADDRESS_DST))).build()).build()).build();
    private static final Object DUMMY_OBJECT = "dummy_object";

    @Before
    public void init() {
        DAO_TEST = mock(ILispDAO.class);
        MULTI_TABLE_MAP_CACHE = new MultiTableMapCache(DAO_TEST);
    }

    /**
     * Tests {@link MultiTableMapCache#addMapping} with SourceDestKey address type.
     */
    @Test
    public void addMappingTest_withSourceDestKey() {

        final Eid normalized_Eid = MaskUtil.normalize(EID_SOURCE_DEST_KEY_TYPE);
        final Eid dstKey = SourceDestKeyHelper.getDst(normalized_Eid);
        final Eid srcKey = SourceDestKeyHelper.getSrc(normalized_Eid);

        ILispDAO tableMock = mock(ILispDAO.class);
        ILispDAO srcDstDaoMock = mock(ILispDAO.class);
        when(DAO_TEST.getSpecific(100L, SubKeys.VNI)).thenReturn(tableMock);
        when(tableMock.getSpecific(dstKey, SubKeys.LCAF_SRCDST)).thenReturn(srcDstDaoMock);

        MULTI_TABLE_MAP_CACHE.addMapping(EID_SOURCE_DEST_KEY_TYPE, DUMMY_OBJECT, true);
        verify(srcDstDaoMock, times(2)).put(srcKey, new MappingEntry<>(anyString(), any(Date.class)));
        verify(srcDstDaoMock).put(srcKey, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#addMapping} with other than SourceDestKey address type.
     */
    @Test
    public void addMappingTest_withoutSourceDestKey() {
        final Eid eid_ipv4Type = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100L))
                .setAddress(new Ipv4Builder().setIpv4(IPV_4_ADDRESS_SRC).build()).build();
        final Eid normalized_Eid = MaskUtil.normalize(eid_ipv4Type);

        ILispDAO tableMock = mock(ILispDAO.class);
        when(DAO_TEST.getSpecific(100L, SubKeys.VNI)).thenReturn(tableMock);

        MULTI_TABLE_MAP_CACHE.addMapping(eid_ipv4Type, DUMMY_OBJECT, true);
        verify(tableMock, times(2)).put(normalized_Eid, new MappingEntry<>(anyString(), any(Date.class)));
        verify(tableMock).put(normalized_Eid, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#getMapping} with SourceDestKey address type.
     */
    @Test
    public void getMappingTest() {
        final ILispDAO tableMock = mock(ILispDAO.class);
        final ILispDAO srcDstDao = mock(ILispDAO.class);
        when(DAO_TEST.getSpecific(100L, SubKeys.VNI)).thenReturn(tableMock);

        final Eid dstAddr = SourceDestKeyHelper.getDst(EID_SOURCE_DEST_KEY_TYPE);
        final Eid normalizedDstAddr = MaskUtil.normalize(dstAddr);
        final short dstMask = MaskUtil.getMaskForAddress(dstAddr.getAddress());
        final Eid dstKey = MaskUtil.normalize(dstAddr, dstMask);

        final Eid srcAddr = SourceDestKeyHelper.getSrc(EID_SOURCE_DEST_KEY_TYPE);
        final Eid normalizedSrcAddr = MaskUtil.normalize(srcAddr);
        final short srcMask = MaskUtil.getMaskForAddress(srcAddr.getAddress());
        final Eid srcKey = MaskUtil.normalize(normalizedSrcAddr, srcMask);

        final Map<String, Object> entry = Maps.newConcurrentMap();
        entry.put(SubKeys.RECORD, DUMMY_OBJECT);
        entry.put(SubKeys.LCAF_SRCDST, srcDstDao);

        final Object obj = "mapping_lpm_eid";
        final Map<String, Object> entry2 = Maps.newConcurrentMap();
        entry2.put(SubKeys.RECORD, obj);

        when(tableMock.get(normalizedDstAddr)).thenReturn(entry);
        when(srcDstDao.get(normalizedSrcAddr)).thenReturn(entry2);

        assertEquals(obj, MULTI_TABLE_MAP_CACHE.getMapping(EID_TEST, EID_SOURCE_DEST_KEY_TYPE));
        assertNull(MULTI_TABLE_MAP_CACHE.getMapping(null, null));
        assertNull(MULTI_TABLE_MAP_CACHE.getMapping(EID_TEST, null));
    }
}
