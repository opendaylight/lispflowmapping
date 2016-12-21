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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;

public class AuthKeyDbTest {

    private static ILispDAO tableMock;
    private static ILispDAO daoMock;
    private static AuthKeyDb authKeyDb;

    private static final String IPV4_STRING_1 =      "1.2.3.0";
    private static final String IPV4_PREFIX_STRING = "/24";
    private static final short MASK = 24;
    private static final long VNI_0 = 0L;
    private static final long VNI_100 = 100L;

    private static final Eid EID_IPV4_PREFIX_1_VNI = LispAddressUtil
            .asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING, new InstanceIdType(VNI_100));
    private static final Eid EID_IPV4 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid NORMALIZED_EID_IPV4 = MaskUtil.normalize(EID_IPV4);
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder()
            .setKeyString("pass")
            .setKeyType(1).build();

    @Before
    public void init() {
        daoMock = Mockito.mock(ILispDAO.class, "dao");
        tableMock = Mockito.mock(ILispDAO.class);
        authKeyDb = new AuthKeyDb(daoMock);
    }

    /**
     * Tests {@link AuthKeyDb#addAuthenticationKey} method.
     */
    @Test
    public void addAuthenticationKeyTest() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        authKeyDb.addAuthenticationKey(EID_IPV4, MAPPING_AUTHKEY);
        Mockito.verify(tableMock)
                .put(MaskUtil.normalize(EID_IPV4), new MappingEntry<>(SubKeys.AUTH_KEY, MAPPING_AUTHKEY));
    }

    /**
     * Tests {@link AuthKeyDb#getAuthenticationKey} method with maskable address.
     */
    @Test
    public void getAuthenticationKeyTest_withMaskableAddress() {
        Mockito.when(daoMock.getSpecific(VNI_100, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(MaskUtil.normalize(EID_IPV4_PREFIX_1_VNI, MASK), SubKeys.AUTH_KEY))
                .thenReturn(MAPPING_AUTHKEY);

        assertEquals(MAPPING_AUTHKEY, authKeyDb.getAuthenticationKey(EID_IPV4_PREFIX_1_VNI));
    }

    /**
     * Tests {@link AuthKeyDb#getAuthenticationKey} method with non maskable address.
     */
    @Test
    public void addAuthenticationKeyTest_withNonMaskableAddress() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY)).thenReturn(MAPPING_AUTHKEY);

        assertEquals(MAPPING_AUTHKEY, authKeyDb.getAuthenticationKey(EID_IPV4));
        Mockito.verify(tableMock).getSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY);
    }

    /**
     * Tests {@link AuthKeyDb#getAuthenticationKey} method with no MappingAuthkey.
     */
    @Test
    public void addAuthenticationKeyTest_withNoMappingAuthkey() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);
        Mockito.when(tableMock.getSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY)).thenReturn(null);

        assertNull(authKeyDb.getAuthenticationKey(EID_IPV4));
    }

    /**
     * Tests {@link AuthKeyDb#getAuthenticationKey} method with no VNI_100 table.
     */
    @Test
    public void addAuthenticationKeyTest_withNullVniTable() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        assertNull(authKeyDb.getAuthenticationKey(EID_IPV4));
    }

    /**
     * Tests {@link AuthKeyDb#removeAuthenticationKey} method.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(tableMock);

        authKeyDb.removeAuthenticationKey(EID_IPV4);
        Mockito.verify(tableMock).removeSpecific(NORMALIZED_EID_IPV4, SubKeys.AUTH_KEY);
    }

    /**
     * Tests {@link AuthKeyDb#removeAuthenticationKey} method with no VNI_100 table.
     */
    @Test
    public void removeAuthenticationKeyTest_withNoVniTable() {
        Mockito.when(daoMock.getSpecific(VNI_0, SubKeys.VNI)).thenReturn(null);

        authKeyDb.removeAuthenticationKey(EID_IPV4);
        Mockito.verify(tableMock, Mockito.never()).removeSpecific(Mockito.any(Eid.class), Mockito.anyString());
    }
}
