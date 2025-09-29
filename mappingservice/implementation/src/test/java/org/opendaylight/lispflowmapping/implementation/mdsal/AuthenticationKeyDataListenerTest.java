/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModified;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;

public class AuthenticationKeyDataListenerTest {

    private static IMappingSystem iMappingSystemMock;
    private static AuthenticationKeyDataListener authenticationKeyDataListener;

    private static DataTreeModification<AuthenticationKey> change_del;
    private static DataTreeModification<AuthenticationKey> change_subtreeModified;
    private static DataTreeModification<AuthenticationKey> change_write;
    private static DataObjectDeleted<AuthenticationKey> mod_del;
    private static DataObjectModified<AuthenticationKey> mod_subtreeModified;
    private static DataObjectWritten<AuthenticationKey> mod_write;

    private static final String IPV4_STRING_1 = "192.168.0.1";
    private static final String IPV4_STRING_2 = "192.168.0.2";
    private static final String IPV4_STRING_3 = "192.168.0.3";
    private static final Eid IPV4_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid IPV4_EID_3 = LispAddressUtil.asIpv4Eid(IPV4_STRING_3);
    private static final AuthenticationKey AUTHENTICATION_KEY_1 = getAuthenticationKey(IPV4_EID_1, "pass1");
    private static final AuthenticationKey AUTHENTICATION_KEY_2 = getAuthenticationKey(IPV4_EID_2, "pass2");
    private static final AuthenticationKey AUTHENTICATION_KEY_3 = getAuthenticationKey(IPV4_EID_3, "pass3");

    @Before
    public void init() {
        DataBroker dataBrokerMock = Mockito.mock(DataBroker.class);
        iMappingSystemMock = Mockito.mock(IMappingSystem.class);
        authenticationKeyDataListener = new AuthenticationKeyDataListener(dataBrokerMock, iMappingSystemMock);

        final DataTreeIdentifier<AuthenticationKey> dataTreeIdentifier = DataTreeIdentifier.of(
            LogicalDatastoreType.CONFIGURATION,
            InstanceIdentifier.create(MappingDatabase.class).child(VirtualNetworkIdentifier.class)
                .child(AuthenticationKey.class));

        change_del = Mockito.mock(DataTreeModification.class);
        change_subtreeModified = Mockito.mock(DataTreeModification.class);
        change_write = Mockito.mock(DataTreeModification.class);
        mod_del = Mockito.spy(DataObjectDeleted.class);
        mod_subtreeModified = Mockito.spy(DataObjectModified.class);
        mod_write = Mockito.spy(DataObjectWritten.class);

        Mockito.when(change_del.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_del.getRootNode()).thenReturn(mod_del);
        Mockito.when(change_subtreeModified.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_subtreeModified.getRootNode()).thenReturn(mod_subtreeModified);
        Mockito.when(change_write.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_write.getRootNode()).thenReturn(mod_write);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with DELETE modification type.
     */
    @Test
    public void onDataTreeChangedTest_delete() {
        Mockito.when(mod_del.dataBefore()).thenReturn(AUTHENTICATION_KEY_1);

        authenticationKeyDataListener.onDataTreeChanged(List.of(change_del));
        Mockito.verify(iMappingSystemMock).removeAuthenticationKey(IPV4_EID_1);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with WRITE modification type.
     */
    @Test
    public void onDataTreeChangedTest_write() {
        Mockito.when(mod_write.dataAfter()).thenReturn(AUTHENTICATION_KEY_2);

        authenticationKeyDataListener.onDataTreeChanged(List.of(change_write));
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_2, AUTHENTICATION_KEY_2.getMappingAuthkey());
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type.
     */
    @Test
    public void onDataTreeChangedTest_subtreeModified() {
        Mockito.when(mod_subtreeModified.dataAfter()).thenReturn(AUTHENTICATION_KEY_3);

        authenticationKeyDataListener.onDataTreeChanged(List.of(change_subtreeModified));
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_3, AUTHENTICATION_KEY_3.getMappingAuthkey());
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with multiple modification types.
     */
    @Test
    public void onDataTreeChangedTest_multipleModTypes() {
        Mockito.when(mod_del.dataBefore()).thenReturn(AUTHENTICATION_KEY_1);
        Mockito.when(mod_write.dataAfter()).thenReturn(AUTHENTICATION_KEY_2);
        Mockito.when(mod_subtreeModified.dataAfter()).thenReturn(AUTHENTICATION_KEY_3);

        authenticationKeyDataListener.onDataTreeChanged(List.of(change_del, change_write, change_subtreeModified));
        Mockito.verify(iMappingSystemMock).removeAuthenticationKey(IPV4_EID_1);
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_2, AUTHENTICATION_KEY_2.getMappingAuthkey());
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_3, AUTHENTICATION_KEY_3.getMappingAuthkey());
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
