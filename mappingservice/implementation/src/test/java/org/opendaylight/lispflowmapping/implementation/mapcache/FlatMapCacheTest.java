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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;

public class FlatMapCacheTest {

    private static ILispDAO DAO_MOCK;
    private static FlatMapCache FLAT_MAP_CACHE;
    private static final Eid EID_MOCK = mock(Eid.class);
    private static final Object DUMMY_OBJECT = "dummy_object";

    private static final Eid EID_TEST = new EidBuilder().setAddress(new Ipv4Builder().setIpv4(new Ipv4Address("127.0.0.1")).build()).build();
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder().setKeyString("auth_string_test").build();
    private static final Eid NORMALIZED_EID = MaskUtil.normalize(EID_TEST);

    @Before
    public void init() {
        DAO_MOCK = mock(ILispDAO.class);
        FLAT_MAP_CACHE = new FlatMapCache(DAO_MOCK);
    }

    /**
     * Tests {@link FlatMapCache#addMapping}.
     */
    @Test
    public void addMappingTest() {
        FLAT_MAP_CACHE.addMapping(EID_TEST, DUMMY_OBJECT, false);
        verify(DAO_MOCK, atMost(2)).put(NORMALIZED_EID, new MappingEntry<>(anyString(), any(Date.class)));
        verify(DAO_MOCK).put(NORMALIZED_EID, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link FlatMapCache#getMapping}.
     */
    @Test
    public void getMappingTest() {
        when(DAO_MOCK.getSpecific(NORMALIZED_EID, SubKeys.RECORD)).thenReturn(DUMMY_OBJECT);
        assertEquals(DUMMY_OBJECT, FLAT_MAP_CACHE.getMapping(EID_MOCK, EID_TEST));
    }

    /**
     * Tests {@link FlatMapCache#removeMapping}.
     */
    @Test
    public void removeMappingTest() {
        FLAT_MAP_CACHE.removeMapping(EID_TEST, true);
        verify(DAO_MOCK).removeSpecific(NORMALIZED_EID, SubKeys.RECORD);
    }

    /**
     * Tests {@link FlatMapCache#addAuthenticationKey}.
     */
    @Test
    public void addAuthenticationKeyTest() {
        FLAT_MAP_CACHE.addAuthenticationKey(EID_TEST, MAPPING_AUTHKEY);
        verify(DAO_MOCK, atMost(1)).put(NORMALIZED_EID, new MappingEntry<>(SubKeys.AUTH_KEY, MAPPING_AUTHKEY));
    }

    /**
     * Tests {@link FlatMapCache#getAuthenticationKey}.
     */
    @Test
    public void getAuthenticationKeyTest() {
        when(DAO_MOCK.getSpecific(NORMALIZED_EID, SubKeys.AUTH_KEY)).thenReturn(MAPPING_AUTHKEY);
        assertEquals(MAPPING_AUTHKEY, FLAT_MAP_CACHE.getAuthenticationKey(EID_TEST));
    }

    /**
     * Tests {@link FlatMapCache#getAuthenticationKey} with other than MappingAuthkey object.
     */
    @Test
    public void getAuthenticationKeyTest_withNull() {
        when(DAO_MOCK.getSpecific(NORMALIZED_EID, SubKeys.AUTH_KEY)).thenReturn(new Object());
        assertNull(FLAT_MAP_CACHE.getAuthenticationKey(EID_TEST));
    }

    /**
     * Tests {@link FlatMapCache#removeAuthenticationKey}.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        FLAT_MAP_CACHE.removeAuthenticationKey(EID_TEST);
        verify(DAO_MOCK).removeSpecific(NORMALIZED_EID, SubKeys.AUTH_KEY);
    }

    /**
     * Tests {@link FlatMapCache#updateMappingRegistration}.
     */
    @Test
    public void updateMappingRegistrationTest() {
        FLAT_MAP_CACHE.updateMappingRegistration(EID_TEST);
        verify(DAO_MOCK).put(NORMALIZED_EID, new MappingEntry<>(anyString(), any(Date.class)));
    }

    /**
     * Tests {@link FlatMapCache#addData}.
     */
    @Test
    public void addDataTest() {
        FLAT_MAP_CACHE.addData(EID_TEST, SubKeys.RECORD, DUMMY_OBJECT);
        verify(DAO_MOCK).put(NORMALIZED_EID, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link FlatMapCache#getData}.
     */
    @Test
    public void getDataTest() {
        when(DAO_MOCK.getSpecific(NORMALIZED_EID, SubKeys.RECORD)).thenReturn(DUMMY_OBJECT);
        assertEquals(DUMMY_OBJECT, FLAT_MAP_CACHE.getData(EID_TEST, SubKeys.RECORD));
    }

    /**
     * Tests {@link FlatMapCache#removeData}.
     */
    @Test
    public void removeDataTest() {
        FLAT_MAP_CACHE.removeData(EID_TEST, SubKeys.RECORD);
        verify(DAO_MOCK).removeSpecific(NORMALIZED_EID, SubKeys.RECORD);
    }
}
