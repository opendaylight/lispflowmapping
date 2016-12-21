/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.mapcache.AuthKeyDb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationKeyDataListenerTest {

    @Mock(name = "akdb") private static AuthKeyDb akdbMock;
    @Mock(name = "broker") private static DataBroker brokerMock;
    @Mock(name = "registration") private static ListenerRegistration<AuthenticationKeyDataListener>
            registrationMock;
    @InjectMocks private static AuthenticationKeyDataListener authenticationKeyDataListener;

    private static DataTreeModification<AuthenticationKey> change_del;
    private static DataTreeModification<AuthenticationKey> change_subtreeModified;
    private static DataTreeModification<AuthenticationKey> change_write;
    private static DataObjectModification<AuthenticationKey> mod_del;
    private static DataObjectModification<AuthenticationKey> mod_subtreeModified;
    private static DataObjectModification<AuthenticationKey> mod_write;

    private static final String IPV4_STRING_1 = "192.168.0.1";
    private static final String IPV4_STRING_2 = "192.168.0.2";
    private static final String IPV4_STRING_3 = "192.168.0.3";
    private static final String MASK = "/24";

    private static final Eid IPV4_BINARY_EID_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid IPV4_BINARY_EID_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid IPV4_BINARY_EID_3 = LispAddressUtil.asIpv4Eid(IPV4_STRING_3);
    private static final Eid IPV4_PREFIX_EID = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + MASK);
    private static final Eid IPV4_PREFIX_BINARY_EID = LispAddressUtil.asIpv4PrefixBinaryEid(IPV4_STRING_1 + MASK);

    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder().setKeyType(1)
            .setKeyString("key").build();

    private static final AuthenticationKey AUTHENTICATION_KEY_1 = getAuthenticationKey(IPV4_BINARY_EID_1);
    private static final AuthenticationKey AUTHENTICATION_KEY_2 = getAuthenticationKey(IPV4_BINARY_EID_2);
    private static final AuthenticationKey AUTHENTICATION_KEY_3 = getAuthenticationKey(IPV4_BINARY_EID_3);
    private static final AuthenticationKey AUTHENTICATION_KEY_PREFIX = getAuthenticationKey(IPV4_PREFIX_EID);

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        final InstanceIdentifier<AuthenticationKey> instanceIdentifierMock = Mockito.mock(InstanceIdentifier.class);
        final DataTreeIdentifier<AuthenticationKey> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, instanceIdentifierMock);

        change_del = Mockito.mock(DataTreeModification.class);
        change_subtreeModified = Mockito.mock(DataTreeModification.class);
        change_write = Mockito.mock(DataTreeModification.class);
        mod_del = Mockito.mock(DataObjectModification.class);
        mod_subtreeModified = Mockito.mock(DataObjectModification.class);
        mod_write = Mockito.mock(DataObjectModification.class);

        Mockito.when(change_del.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_del.getRootNode()).thenReturn(mod_del);
        Mockito.when(change_subtreeModified.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_subtreeModified.getRootNode()).thenReturn(mod_subtreeModified);
        Mockito.when(change_write.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(change_write.getRootNode()).thenReturn(mod_write);
        Mockito.when(mod_del.getModificationType()).thenReturn(DataObjectModification.ModificationType.DELETE);
        Mockito.when(mod_subtreeModified.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED);
        Mockito.when(mod_write.getModificationType()).thenReturn(DataObjectModification.ModificationType.WRITE);

        Mockito.when(brokerMock.registerDataTreeChangeListener(Mockito.any(DataTreeIdentifier.class),
                Mockito.any(AuthenticationKeyDataListener.class))).thenReturn(registrationMock);
        authenticationKeyDataListener = new AuthenticationKeyDataListener(brokerMock, akdbMock);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with DELETE mod type, binary eid.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_delete_BinaryEid() {
        Mockito.when(mod_del.getDataBefore()).thenReturn(AUTHENTICATION_KEY_1);

        authenticationKeyDataListener.onDataTreeChanged(Lists.newArrayList(change_del));
        Mockito.verify(akdbMock).removeAuthenticationKey(IPV4_BINARY_EID_1);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with DELETE mod type, Ipv4Prefix eid.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_delete_Ipv4PrefixEid() {
        Mockito.when(mod_del.getDataBefore()).thenReturn(AUTHENTICATION_KEY_PREFIX);

        authenticationKeyDataListener.onDataTreeChanged(Lists.newArrayList(change_del));
        Mockito.verify(akdbMock).removeAuthenticationKey(IPV4_PREFIX_BINARY_EID);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED mod type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_subtreeModified() {
        Mockito.when(mod_subtreeModified.getDataAfter()).thenReturn(AUTHENTICATION_KEY_2);

        authenticationKeyDataListener.onDataTreeChanged(Lists.newArrayList(change_subtreeModified));
        Mockito.verify(akdbMock).addAuthenticationKey(IPV4_BINARY_EID_2, MAPPING_AUTHKEY);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with WRITE mod type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_write() {
        Mockito.when(mod_write.getDataAfter()).thenReturn(AUTHENTICATION_KEY_3);

        authenticationKeyDataListener.onDataTreeChanged(Lists.newArrayList(change_write));
        Mockito.verify(akdbMock).addAuthenticationKey(IPV4_BINARY_EID_3, MAPPING_AUTHKEY);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with null mod type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_nullModType() {
        final DataTreeModification<AuthenticationKey> change_nullModType = Mockito.mock(DataTreeModification.class);
        final DataObjectModification mod_nullModType = Mockito.mock(DataObjectModification.class);
        Mockito.when(change_nullModType.getRootNode()).thenReturn(mod_nullModType);
        Mockito.when(mod_nullModType.getModificationType()).thenReturn(null);

        authenticationKeyDataListener.onDataTreeChanged(Lists.newArrayList(change_nullModType));
        Mockito.verifyZeroInteractions(akdbMock);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#closeDataChangeListener} method.
     */
    @Test
    public void closeDataChangeListenerTest() {
        authenticationKeyDataListener.closeDataChangeListener();
        Mockito.verify(registrationMock).close();
    }

    private static AuthenticationKey getAuthenticationKey(Eid eid) {
        return new AuthenticationKeyBuilder()
                .setEid(eid)
                .setMappingAuthkey(MAPPING_AUTHKEY)
                .build();
    }
}
