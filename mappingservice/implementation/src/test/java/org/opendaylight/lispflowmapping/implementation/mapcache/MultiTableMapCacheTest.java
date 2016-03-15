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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;

public class MultiTableMapCacheTest {

    private static MultiTableMapCache MULTI_TABLE_MAP_CACHE;
    private static ILispDAO DAO_MOCK;
    private static ILispDAO SRC_DST_DAO_MOCK;
    private static ILispDAO TABLE_DAO_MOCK;
    private static ILispDAO DB_MOCK;
    private static final Ipv4Address IPV_4_ADDRESS_SRC = new Ipv4Address("127.0.0.1");
    private static final Ipv4Address IPV_4_ADDRESS_DST = new Ipv4Address("192.168.0.1");

    private static final Eid EID_TEST = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100L))
            .setAddress(new Ipv4Builder().setIpv4(IPV_4_ADDRESS_SRC).build()).build();
    private static final Eid EID_SOURCE_DEST_KEY_TYPE = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100L))
            .setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder()
                    .setSourceDestKey(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder()
                            .setSource(new SimpleAddress(new IpAddress(IPV_4_ADDRESS_SRC)))
                            .setDest(new SimpleAddress(new IpAddress(IPV_4_ADDRESS_DST))).build()).build()).build();
    private static final Eid EID_IPV4PREFIX = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100L))
            .setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(new Ipv4Prefix(IPV_4_ADDRESS_DST.getValue() + "/24")).build()).build();
    private static final Eid NORMALIZED_SRCDST_EID = MaskUtil.normalize(EID_SOURCE_DEST_KEY_TYPE);
    private static final Eid EID_MAC = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100l))
            .setAddress(new MacBuilder().setMac(new MacAddress("aa:bb:cc:dd:ee:ff")).build()).build();
    private static final Eid NORMALIZED_EID = MaskUtil.normalize(EID_TEST);

    private static final Object DUMMY_OBJECT = "dummy_object";
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder().setKeyString("mapping_authkey_test").build();

    @Before
    public void init() {
        DAO_MOCK = mock(ILispDAO.class);
        TABLE_DAO_MOCK = mock(ILispDAO.class);
        SRC_DST_DAO_MOCK = mock(ILispDAO.class);
        MULTI_TABLE_MAP_CACHE = new MultiTableMapCache(DAO_MOCK);
        DB_MOCK= mock(ILispDAO.class);
    }

    /**
     * Tests {@link MultiTableMapCache#addMapping} with SourceDestKey address type.
     */
    @Test
    public void addMappingTest_withSourceDestKey() {

        final Eid normalized_Eid = MaskUtil.normalize(EID_SOURCE_DEST_KEY_TYPE);
        final Eid dstKey = SourceDestKeyHelper.getDst(normalized_Eid);
        final Eid srcKey = SourceDestKeyHelper.getSrc(normalized_Eid);

        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);
        when(TABLE_DAO_MOCK.getSpecific(dstKey, SubKeys.LCAF_SRCDST)).thenReturn(SRC_DST_DAO_MOCK);

        MULTI_TABLE_MAP_CACHE.addMapping(EID_SOURCE_DEST_KEY_TYPE, DUMMY_OBJECT, true);
        verify(SRC_DST_DAO_MOCK, times(2)).put(srcKey, new MappingEntry<>(anyString(), any(Date.class)));
        verify(SRC_DST_DAO_MOCK).put(srcKey, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#addMapping} with other than SourceDestKey address type.
     */
    @Test
    public void addMappingTest_withoutSourceDestKey() {
        final Eid eid_ipv4Type = new EidBuilder().setVirtualNetworkId(new InstanceIdType(100L))
                .setAddress(new Ipv4Builder().setIpv4(IPV_4_ADDRESS_SRC).build()).build();
        final Eid normalized_Eid = MaskUtil.normalize(eid_ipv4Type);

        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);

        MULTI_TABLE_MAP_CACHE.addMapping(eid_ipv4Type, DUMMY_OBJECT, true);
        verify(TABLE_DAO_MOCK, times(2)).put(normalized_Eid, new MappingEntry<>(anyString(), any(Date.class)));
        verify(TABLE_DAO_MOCK).put(normalized_Eid, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link MultiTableMapCache#getMapping} with SourceDestKey address type.
     */
    @Test
    public void getMappingTest() {
        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);

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
        entry.put(SubKeys.LCAF_SRCDST, SRC_DST_DAO_MOCK);

        final Object obj = "mapping_lpm_eid";
        final Map<String, Object> entry2 = Maps.newConcurrentMap();
        entry2.put(SubKeys.RECORD, obj);

        when(TABLE_DAO_MOCK.get(normalizedDstAddr)).thenReturn(entry);
        when(SRC_DST_DAO_MOCK.get(normalizedSrcAddr)).thenReturn(entry2);

        assertEquals(obj, MULTI_TABLE_MAP_CACHE.getMapping(EID_TEST, EID_SOURCE_DEST_KEY_TYPE));
        assertNull(MULTI_TABLE_MAP_CACHE.getMapping(null, null));
        assertNull(MULTI_TABLE_MAP_CACHE.getMapping(EID_TEST, null));
    }

    /**
     * Tests {@link MultiTableMapCache#removeMapping}.
     */
    @Test
    public void removeMappingTest() {

        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);
        when(TABLE_DAO_MOCK.getSpecific(SourceDestKeyHelper
                .getDst(NORMALIZED_SRCDST_EID), SubKeys.LCAF_SRCDST)).thenReturn(DB_MOCK);

        MULTI_TABLE_MAP_CACHE.removeMapping(EID_SOURCE_DEST_KEY_TYPE, true);
        verify(DB_MOCK).removeSpecific(SourceDestKeyHelper.getSrc(NORMALIZED_SRCDST_EID),
                SubKeys.RECORD);
    }

    /**
     * Tests {@link MultiTableMapCache#addAuthenticationKey}.
     */
    @Test
    public void addAuthenticationKeyTest() {
        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);
        when(TABLE_DAO_MOCK.putNestedTable(SourceDestKeyHelper.getDst(NORMALIZED_SRCDST_EID), SubKeys.LCAF_SRCDST))
                .thenReturn(SRC_DST_DAO_MOCK);

        MULTI_TABLE_MAP_CACHE.addAuthenticationKey(EID_SOURCE_DEST_KEY_TYPE, MAPPING_AUTHKEY);
        verify(SRC_DST_DAO_MOCK).put(SourceDestKeyHelper.getSrc(NORMALIZED_SRCDST_EID),
                new MappingEntry<>(SubKeys.AUTH_KEY, MAPPING_AUTHKEY));

        MULTI_TABLE_MAP_CACHE.addAuthenticationKey(EID_TEST, MAPPING_AUTHKEY);
        verify(TABLE_DAO_MOCK).put(NORMALIZED_EID, new MappingEntry<>(SubKeys.AUTH_KEY, MAPPING_AUTHKEY));
    }

    /**
     * Tests {@link MultiTableMapCache#getAuthenticationKey} with Ipv4 address.
     */
    @Test
    public void getAuthenticationKeyTest_withIpv4Prefix() {
        final short maskLength = MaskUtil.getMaskForAddress(EID_IPV4PREFIX.getAddress());
        final Eid key = MaskUtil.normalize(EID_IPV4PREFIX, maskLength);

        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);
        when(TABLE_DAO_MOCK.getSpecific(key, SubKeys.AUTH_KEY)).thenReturn(MAPPING_AUTHKEY);

        assertEquals(MAPPING_AUTHKEY, MULTI_TABLE_MAP_CACHE.getAuthenticationKey(EID_IPV4PREFIX));
    }

    /**
     * Tests {@link MultiTableMapCache#getAuthenticationKey} with SourceDestKey address.
     */
    @Test
    public void getAuthenticationKeyTest_withSourceDestKey() {
        final Eid eidSrc = SourceDestKeyHelper.getSrc(EID_SOURCE_DEST_KEY_TYPE);
        final Eid eidDst = SourceDestKeyHelper.getDst(EID_SOURCE_DEST_KEY_TYPE);
        final short maskLength = MaskUtil.getMaskForAddress(eidSrc.getAddress());
        final Eid key = MaskUtil.normalize(eidSrc, maskLength);

        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);
        when(TABLE_DAO_MOCK.getSpecific(eidDst, SubKeys.LCAF_SRCDST))
                .thenReturn(SRC_DST_DAO_MOCK);
        when(SRC_DST_DAO_MOCK.getSpecific(key, SubKeys.AUTH_KEY)).thenReturn(MAPPING_AUTHKEY);

        assertNull(MULTI_TABLE_MAP_CACHE.getAuthenticationKey(EID_SOURCE_DEST_KEY_TYPE));
    }

    /**
     * Tests {@link MultiTableMapCache#getAuthenticationKey} with Mac address.
     */
    @Test
    public void getAuthenticationKeyTest_withMac() {
        final Eid key = MaskUtil.normalize(EID_MAC);

        when(DAO_MOCK.getSpecific(100L, SubKeys.VNI)).thenReturn(TABLE_DAO_MOCK);
        when(TABLE_DAO_MOCK.getSpecific(key, SubKeys.AUTH_KEY)).thenReturn(MAPPING_AUTHKEY);

        assertEquals(MAPPING_AUTHKEY, MULTI_TABLE_MAP_CACHE.getAuthenticationKey(EID_MAC));
    }
}
