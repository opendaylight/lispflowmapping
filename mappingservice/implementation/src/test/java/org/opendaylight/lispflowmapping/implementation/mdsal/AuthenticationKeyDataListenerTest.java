/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModified;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.VniUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifierKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;

public class AuthenticationKeyDataListenerTest {
    private static final VirtualNetworkIdentifierKey VNI_KEY = new VirtualNetworkIdentifierKey(new VniUri("urn:foo"));
    private static final String IPV4_STRING_1 = "192.168.0.1";
    private static final String IPV4_STRING_2 = "192.168.0.2";
    private static final String IPV4_STRING_3 = "192.168.0.3";
    private static final Eid IPV4_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid IPV4_EID_3 = LispAddressUtil.asIpv4Eid(IPV4_STRING_3);
    private static final AuthenticationKey AUTHENTICATION_KEY_1 = getAuthenticationKey(IPV4_EID_1, "pass1");
    private static final AuthenticationKey AUTHENTICATION_KEY_2 = getAuthenticationKey(IPV4_EID_2, "pass2");
    private static final AuthenticationKey AUTHENTICATION_KEY_3 = getAuthenticationKey(IPV4_EID_3, "pass3");

    private IMappingSystem mappingSystemMock;
    private AuthenticationKeyDataListener authenticationKeyDataListener;

    private DataTreeModification<AuthenticationKey> changeDel;
    private DataTreeModification<AuthenticationKey> changeSubtreeModified;
    private DataTreeModification<AuthenticationKey> changeWrite;
    private DataObjectDeleted<AuthenticationKey> modDel;
    private DataObjectModified<AuthenticationKey> modSubtreeModified;
    private DataObjectWritten<AuthenticationKey> modWrite;

    @Before
    public void init() {
        DataBroker dataBrokerMock = mock(DataBroker.class);
        mappingSystemMock = mock(IMappingSystem.class);
        authenticationKeyDataListener = new AuthenticationKeyDataListener(dataBrokerMock, mappingSystemMock);

        changeDel = mock(DataTreeModification.class);
        modDel = spy(DataObjectDeleted.class);
        when(changeDel.path()).thenReturn(DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, VNI_KEY)
            .child(AuthenticationKey.class, AUTHENTICATION_KEY_1.key())
            .build());
        when(changeDel.getRootNode()).thenReturn(modDel);

        changeSubtreeModified = mock(DataTreeModification.class);
        modSubtreeModified = spy(DataObjectModified.class);
        when(changeSubtreeModified.path()).thenReturn(DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, VNI_KEY)
            .child(AuthenticationKey.class, AUTHENTICATION_KEY_3.key())
            .build());
        when(changeSubtreeModified.getRootNode()).thenReturn(modSubtreeModified);

        changeWrite = mock(DataTreeModification.class);
        modWrite = spy(DataObjectWritten.class);
        when(changeWrite.path()).thenReturn(DataObjectIdentifier.builder(MappingDatabase.class)
            .child(VirtualNetworkIdentifier.class, VNI_KEY)
            .child(AuthenticationKey.class, AUTHENTICATION_KEY_2.key())
            .build());
        when(changeWrite.getRootNode()).thenReturn(modWrite);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with DELETE modification type.
     */
    @Test
    public void onDataTreeChangedTest_delete() {
        when(modDel.dataBefore()).thenReturn(AUTHENTICATION_KEY_1);

        authenticationKeyDataListener.onDataTreeChanged(List.of(changeDel));
        verify(mappingSystemMock).removeAuthenticationKey(IPV4_EID_1);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with WRITE modification type.
     */
    @Test
    public void onDataTreeChangedTest_write() {
        when(modWrite.dataAfter()).thenReturn(AUTHENTICATION_KEY_2);

        authenticationKeyDataListener.onDataTreeChanged(List.of(changeWrite));
        verify(mappingSystemMock).addAuthenticationKey(IPV4_EID_2, AUTHENTICATION_KEY_2.getMappingAuthkey());
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type.
     */
    @Test
    public void onDataTreeChangedTest_subtreeModified() {
        when(modSubtreeModified.dataAfter()).thenReturn(AUTHENTICATION_KEY_3);

        authenticationKeyDataListener.onDataTreeChanged(List.of(changeSubtreeModified));
        verify(mappingSystemMock).addAuthenticationKey(IPV4_EID_3, AUTHENTICATION_KEY_3.getMappingAuthkey());
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with multiple modification types.
     */
    @Test
    public void onDataTreeChangedTest_multipleModTypes() {
        when(modDel.dataBefore()).thenReturn(AUTHENTICATION_KEY_1);
        when(modWrite.dataAfter()).thenReturn(AUTHENTICATION_KEY_2);
        when(modSubtreeModified.dataAfter()).thenReturn(AUTHENTICATION_KEY_3);

        authenticationKeyDataListener.onDataTreeChanged(List.of(changeDel, changeWrite, changeSubtreeModified));
        verify(mappingSystemMock).removeAuthenticationKey(IPV4_EID_1);
        verify(mappingSystemMock).addAuthenticationKey(IPV4_EID_2, AUTHENTICATION_KEY_2.getMappingAuthkey());
        verify(mappingSystemMock).addAuthenticationKey(IPV4_EID_3, AUTHENTICATION_KEY_3.getMappingAuthkey());
    }

    private static AuthenticationKey getAuthenticationKey(final Eid eid, final String password) {
        return new AuthenticationKeyBuilder()
                .withKey(new AuthenticationKeyKey(new EidUri("uri-1")))
                .setEid(eid)
                .setMappingAuthkey(new MappingAuthkeyBuilder()
                        .setKeyString(password)
                        .setKeyType(Uint16.valueOf(1)).build())
                .build();
    }
}
