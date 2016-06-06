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
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.mapcache.SimpleMapCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationKeyDataListenerTest {

    @Mock(name = "smc") private static SimpleMapCache smcMock;
    @Mock(name = "broker") private static DataBroker brokerMock;
    @Mock(name = "registration") private static ListenerRegistration<DataTreeChangeListener<AuthenticationKey>>
            registrationMock;
    @InjectMocks private static AuthenticationKeyDataListener authenticationKeyDataListener;

    private static DataTreeModification<AuthenticationKey> change_del;
    private static DataTreeModification<AuthenticationKey> change_subtreeModified;
    private static DataTreeModification<AuthenticationKey> change_write;
    private static DataObjectModification<AuthenticationKey> mod_del;
    private static DataObjectModification<AuthenticationKey> mod_subtreeModified;
    private static DataObjectModification<AuthenticationKey> mod_write;

    private static final String IPV4_STRING = "192.168.0.1";
    private static final String MASK = "/24";

    private static final Eid IPV4_BINARY_EID = LispAddressUtil.asIpv4Eid(IPV4_STRING);
    private static final Eid IPV4_PREFIX_EID = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING + MASK);
    private static final Eid IPV4_PREFIX_BINARY_EID = LispAddressUtil.asIpv4PrefixBinaryEid(IPV4_STRING + MASK);

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
        Mockito.when(mod_subtreeModified.getModificationType()).
                thenReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED);
        Mockito.when(mod_write.getModificationType()).thenReturn(DataObjectModification.ModificationType.WRITE);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with binary eid.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_BinaryEid() {
        final AuthenticationKey authenticationKey = new AuthenticationKeyBuilder()
                .setEid(IPV4_BINARY_EID)
                .setKey(new AuthenticationKeyKey(new EidUri("URI"))).build();
        Mockito.when(mod_del.getDataBefore()).thenReturn(authenticationKey);

        authenticationKeyDataListener.onDataTreeChanged(Lists.newArrayList(change_del));
        Mockito.verify(smcMock).removeAuthenticationKey(IPV4_BINARY_EID);
    }

    /**
     * Tests {@link AuthenticationKeyDataListener#onDataTreeChanged} method with Ipv4Prefix eid.
     */
    //@Test
    @SuppressWarnings("unchecked")
    public void onDataTreeChangedTest_Ipv4PrefixEid() {
        final AuthenticationKey authenticationKey = new AuthenticationKeyBuilder()
                .setEid(IPV4_PREFIX_EID)
                .setKey(new AuthenticationKeyKey(new EidUri("URI"))).build();
        Mockito.when(mod_del.getDataBefore()).thenReturn(authenticationKey);

        authenticationKeyDataListener.onDataTreeChanged(Lists.newArrayList(change_del));
        Mockito.verify(smcMock).removeAuthenticationKey(IPV4_PREFIX_BINARY_EID);
    }
}
