/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container
        .MappingAuthkeyBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AuthenticationKeyDataListenerTest {

    private static IMappingSystem iMappingSystemMock;
    private static AuthenticationKeyDataListener authenticationKeyDataListener;

    private static DataTreeModification<AuthenticationKey> change_del;
    private static DataTreeModification<AuthenticationKey> change_subtreeModified;
    private static DataTreeModification<AuthenticationKey> change_write;
    private static DataObjectModification<AuthenticationKey> mod_del;
    private static DataObjectModification<AuthenticationKey> mod_subtreeModified;
    private static DataObjectModification<AuthenticationKey> mod_write;

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
    @SuppressWarnings("unchecked")
    public void init() {
        DataBroker dataBrokerMock = Mockito.mock(DataBroker.class);
        iMappingSystemMock = Mockito.mock(IMappingSystem.class);
        authenticationKeyDataListener = new AuthenticationKeyDataListener(dataBrokerMock, iMappingSystemMock);

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
        Mockito.when(mod_del.getModificationType()).thenReturn(ModificationType.DELETE);
        Mockito.when(mod_subtreeModified.getModificationType()).thenReturn(ModificationType.SUBTREE_MODIFIED);
        Mockito.when(mod_write.getModificationType()).thenReturn(ModificationType.WRITE);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with DELETE modification type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_delete() {
        final List<DataTreeModification<AuthenticationKey>> changes = Lists.newArrayList(change_del);
        Mockito.when(mod_del.getDataBefore()).thenReturn(AUTHENTICATION_KEY_1);

        authenticationKeyDataListener.onDataTreeChanged(changes);
        Mockito.verify(iMappingSystemMock).removeAuthenticationKey(IPV4_EID_1);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with WRITE modification type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_write() {
        final List<DataTreeModification<AuthenticationKey>> changes = Lists.newArrayList(change_write);
        Mockito.when(mod_write.getDataAfter()).thenReturn(AUTHENTICATION_KEY_2);

        authenticationKeyDataListener.onDataTreeChanged(changes);
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_2, AUTHENTICATION_KEY_2.getMappingAuthkey());
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with SUBTREE_MODIFIED modification type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_subtreeModified() {
        final List<DataTreeModification<AuthenticationKey>> changes = Lists.newArrayList(change_subtreeModified);
        Mockito.when(mod_subtreeModified.getDataAfter()).thenReturn(AUTHENTICATION_KEY_3);

        authenticationKeyDataListener.onDataTreeChanged(changes);
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_3, AUTHENTICATION_KEY_3.getMappingAuthkey());
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with multiple modification types.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_multipleModTypes() {
        final List<DataTreeModification<AuthenticationKey>> changes =
                Lists.newArrayList(change_del, change_write, change_subtreeModified);

        Mockito.when(mod_del.getDataBefore()).thenReturn(AUTHENTICATION_KEY_1);
        Mockito.when(mod_write.getDataAfter()).thenReturn(AUTHENTICATION_KEY_2);
        Mockito.when(mod_subtreeModified.getDataAfter()).thenReturn(AUTHENTICATION_KEY_3);

        authenticationKeyDataListener.onDataTreeChanged(changes);
        Mockito.verify(iMappingSystemMock).removeAuthenticationKey(IPV4_EID_1);
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_2, AUTHENTICATION_KEY_2.getMappingAuthkey());
        Mockito.verify(iMappingSystemMock).addAuthenticationKey(IPV4_EID_3, AUTHENTICATION_KEY_3.getMappingAuthkey());
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with no modification type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_noModType() {
        final DataTreeModification<AuthenticationKey> changeNoModType = Mockito.mock(DataTreeModification.class);
        final DataObjectModification<AuthenticationKey> modNoType = Mockito.mock(DataObjectModification.class);
        final List<DataTreeModification<AuthenticationKey>> changes = Lists.newArrayList(changeNoModType);

        Mockito.when(changeNoModType.getRootNode()).thenReturn(modNoType);
        Mockito.when(modNoType.getModificationType()).thenReturn(null);

        authenticationKeyDataListener.onDataTreeChanged(changes);
        Mockito.verifyZeroInteractions(iMappingSystemMock);
    }

    private static AuthenticationKey getAuthenticationKey(Eid eid, String password) {
        return new AuthenticationKeyBuilder()
                .setEid(eid)
                .setMappingAuthkey(new MappingAuthkeyBuilder()
                        .setKeyString(password)
                        .setKeyType(1).build())
                .build();
    }
}
