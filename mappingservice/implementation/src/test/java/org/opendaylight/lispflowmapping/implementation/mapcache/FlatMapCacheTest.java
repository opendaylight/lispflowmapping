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

    private static ILispDAO daoMock = mock(ILispDAO.class);
    private static Eid eidMock = mock(Eid.class);
    private static final FlatMapCache FLAT_MAP_CACHE = new FlatMapCache(daoMock);
    private static final Object DUMMY_OBJECT = "dummy_object";

    private static final Eid EID_TEST = new EidBuilder().setAddress(new Ipv4Builder().setIpv4(new Ipv4Address("127.0.0.1")).build()).build();
    private static final MappingAuthkey authKey = new MappingAuthkeyBuilder().setKeyString("auth_string_test").build();
    private static final Eid NORMALIZED_EID = MaskUtil.normalize(EID_TEST);

    /**
     * Tests {@link FlatMapCache#addMapping}.
     */
    @Test
    public void addMappingTest() {
        FLAT_MAP_CACHE.addMapping(EID_TEST, DUMMY_OBJECT, false);
        verify(daoMock, atMost(2)).put(NORMALIZED_EID, new MappingEntry<>(anyString(), any(Date.class)));
        verify(daoMock).put(NORMALIZED_EID, new MappingEntry<>(SubKeys.RECORD, DUMMY_OBJECT));
    }

    /**
     * Tests {@link FlatMapCache#getMapping}.
     */
    @Test
    public void getMappingTest() {
        when(daoMock.getSpecific(NORMALIZED_EID, SubKeys.RECORD)).thenReturn(DUMMY_OBJECT);
        assertEquals(DUMMY_OBJECT, FLAT_MAP_CACHE.getMapping(eidMock, EID_TEST));
    }

    /**
     * Tests {@link FlatMapCache#removeMapping}.
     */
    @Test
    public void removeMappingTest() {
        FLAT_MAP_CACHE.removeMapping(EID_TEST, true);
        verify(daoMock).removeSpecific(NORMALIZED_EID, SubKeys.RECORD);
    }

    /**
     * Tests {@link FlatMapCache#addAuthenticationKey}.
     */
    @Test
    public void addAuthenticationKeyTest() {
        FLAT_MAP_CACHE.addAuthenticationKey(EID_TEST, authKey);
        verify(daoMock, atMost(1)).put(NORMALIZED_EID, new MappingEntry<>(SubKeys.AUTH_KEY, authKey));
    }

    /**
     * Tests {@link FlatMapCache#getAuthenticationKey}.
     */
    @Test
    public void getAuthenticationKeyTest() {
        when(daoMock.getSpecific(NORMALIZED_EID, SubKeys.AUTH_KEY)).thenReturn(authKey);
        assertEquals(authKey, FLAT_MAP_CACHE.getAuthenticationKey(EID_TEST));
    }

    /**
     * Tests {@link FlatMapCache#addAuthenticationKey} with other than MappingAuthkey object.
     */
    @Test
    public void getAuthenticationKeyTest_withNull() {
        when(daoMock.getSpecific(NORMALIZED_EID, SubKeys.AUTH_KEY)).thenReturn(new Object());
        assertNull(FLAT_MAP_CACHE.getAuthenticationKey(EID_TEST));
    }

    /**
     * Tests {@link FlatMapCache#removeAuthenticationKey}.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        FLAT_MAP_CACHE.removeAuthenticationKey(EID_TEST);
        verify(daoMock).removeSpecific(NORMALIZED_EID, SubKeys.AUTH_KEY);
    }


}
