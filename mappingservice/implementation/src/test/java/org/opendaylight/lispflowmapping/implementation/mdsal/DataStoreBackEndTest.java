/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container
        .MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.XtrIdUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping
        .XtrIdMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container
        .MappingAuthkeyBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerFactory.class})
public class DataStoreBackEndTest {

    private static BindingTransactionChain txChainMock;
    private static DataBroker brokerMock;
    private static WriteTransaction wTxMock;
    private static Logger logMock;
    private static DataStoreBackEnd dataStoreBackEnd;

    private static final String IPV4_STRING = "1.2.3.0";
    private static final String DUMMY_URI = "dummy/uri";
    private static final Eid EID_IPV4 = LispAddressUtil.asIpv4Eid(IPV4_STRING);
    private static final byte[] XTR_ID = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final InstanceIdentifier<MappingDatabase> DATABASE_ROOT =
            InstanceIdentifier.create(MappingDatabase.class);

    @Before
    public void init() {
        txChainMock = Mockito.mock(BindingTransactionChain.class);
        brokerMock = Mockito.mock(DataBroker.class);
        wTxMock = Mockito.mock(WriteTransaction.class);
        logMock = Mockito.mock(Logger.class, "LOG");

        PowerMockito.mockStatic(LoggerFactory.class);
        PowerMockito.when(LoggerFactory.getLogger(DataStoreBackEnd.class)).
                thenReturn(logMock);
        Mockito.when(logMock.isDebugEnabled()).thenReturn(true);
        Mockito.when(brokerMock.createTransactionChain(Mockito.any(DataStoreBackEnd.class))).thenReturn(txChainMock);
        dataStoreBackEnd = Mockito.spy(new DataStoreBackEnd(brokerMock));

        Mockito.when(txChainMock.newWriteOnlyTransaction()).thenReturn(wTxMock);
        Mockito.when(wTxMock.submit()).
                thenReturn(Futures.<Void, TransactionCommitFailedException>immediateCheckedFuture(null));
    }

    /**
     * Tests {@link DataStoreBackEnd#addAuthenticationKey} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void addAuthenticationKeyTest() {
        final AuthenticationKey authenticationKey = getDefaultAuthenticationKeyBuilder().build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.addAuthenticationKey(authenticationKey);
        Mockito.verify(wTxMock).put(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptor.capture(),
                Mockito.eq(authenticationKey), Mockito.eq(true));

        // result
        AuthenticationKeyKey result = (AuthenticationKeyKey) iidCaptor.getValue().firstKeyOf(AuthenticationKey.class);
        assertEquals("ipv4:" + IPV4_STRING, result.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#addMapping} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void addMappingTest() {
        final Mapping mapping = new MappingBuilder()
                .setMappingRecord(getDefaultMappingRecordBuilder().build())
                .setOrigin(MappingOrigin.Northbound).build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.addMapping(mapping);
        Mockito.verify(wTxMock).put(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptor.capture(),
                Mockito.eq(mapping), Mockito.eq(true));

        // result
        MappingKey result = (MappingKey) iidCaptor.getValue().firstKeyOf(Mapping.class);
        assertEquals("ipv4:" + IPV4_STRING, result.getEidUri().getValue());
        assertEquals(MappingOrigin.Northbound, result.getOrigin());
    }

    /**
     * Tests {@link DataStoreBackEnd#addXtrIdMapping} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void addXtrIdMappingTest() {
        XtrIdMapping xtrIdMapping = new XtrIdMappingBuilder()
                .setXtrIdUri(new XtrIdUri(DUMMY_URI))
                .setMappingRecord(getDefaultMappingRecordBuilder().build()).build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.addXtrIdMapping(xtrIdMapping);
        Mockito.verify(wTxMock).put(Mockito.eq(LogicalDatastoreType.OPERATIONAL), iidCaptor.capture(),
                Mockito.eq(xtrIdMapping), Mockito.eq(true));

        // result
        XtrIdMappingKey xtrIdResult = (XtrIdMappingKey) iidCaptor.getValue().firstKeyOf(XtrIdMapping.class);
        MappingKey mappingResult = (MappingKey) iidCaptor.getValue().firstKeyOf(Mapping.class);

        assertEquals(DatatypeConverter.printHexBinary(XTR_ID), xtrIdResult.getXtrIdUri().getValue());
        assertEquals(MappingOrigin.Southbound, mappingResult.getOrigin());
        assertEquals("ipv4:" + IPV4_STRING, mappingResult.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#removeAuthenticationKey} method.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        final AuthenticationKey authenticationKey = getDefaultAuthenticationKeyBuilder().build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.removeAuthenticationKey(authenticationKey);
        Mockito.verify(wTxMock).delete(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptor.capture());

        // result
        AuthenticationKeyKey result = (AuthenticationKeyKey) iidCaptor.getValue().firstKeyOf(AuthenticationKey.class);
        assertEquals("ipv4:" + IPV4_STRING, result.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#removeMapping} method.
     */
    @Test
    public void removeMapping() {
        final Mapping mapping = new MappingBuilder()
                .setMappingRecord(getDefaultMappingRecordBuilder().build())
                .setOrigin(MappingOrigin.Northbound).build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.removeMapping(mapping);
        Mockito.verify(wTxMock).delete(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptor.capture());

        // result
        MappingKey result = (MappingKey) iidCaptor.getValue().firstKeyOf(Mapping.class);
        assertEquals("ipv4:" + IPV4_STRING, result.getEidUri().getValue());
        assertEquals(MappingOrigin.Northbound, result.getOrigin());
    }

    /**
     * Tests {@link DataStoreBackEnd#addXtrIdMapping} method.
     */
    @Test
    public void removeXtrIdMappingTest() {
        XtrIdMapping xtrIdMapping = new XtrIdMappingBuilder()
                .setXtrIdUri(new XtrIdUri(DUMMY_URI))
                .setMappingRecord(getDefaultMappingRecordBuilder().build()).build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.removeXtrIdMapping(xtrIdMapping);
        Mockito.verify(wTxMock).delete(Mockito.eq(LogicalDatastoreType.OPERATIONAL), iidCaptor.capture());

        // result
        XtrIdMappingKey xtrIdResult = (XtrIdMappingKey) iidCaptor.getValue().firstKeyOf(XtrIdMapping.class);
        MappingKey mappingResult = (MappingKey) iidCaptor.getValue().firstKeyOf(Mapping.class);

        assertEquals(DatatypeConverter.printHexBinary(XTR_ID), xtrIdResult.getXtrIdUri().getValue());
        assertEquals(MappingOrigin.Southbound, mappingResult.getOrigin());
        assertEquals("ipv4:" + IPV4_STRING, mappingResult.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#updateAuthenticationKey} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void updateAuthenticationKeyTest() {
        final AuthenticationKey authenticationKey = getDefaultAuthenticationKeyBuilder().build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.updateAuthenticationKey(authenticationKey);
        Mockito.verify(wTxMock).put(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptor.capture(),
                Mockito.eq(authenticationKey), Mockito.eq(true));

        // result
        AuthenticationKeyKey result = (AuthenticationKeyKey) iidCaptor.getValue().firstKeyOf(AuthenticationKey.class);
        assertEquals("ipv4:" + IPV4_STRING, result.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#updateMapping} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void updateMappingTest() {
        final Mapping mapping = new MappingBuilder()
                .setMappingRecord(getDefaultMappingRecordBuilder().build())
                .setOrigin(MappingOrigin.Northbound).build();
        final ArgumentCaptor<InstanceIdentifier> iidCaptor = ArgumentCaptor.forClass(InstanceIdentifier.class);

        dataStoreBackEnd.updateMapping(mapping);
        Mockito.verify(wTxMock).put(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptor.capture(),
                Mockito.eq(mapping), Mockito.eq(true));

        // result
        MappingKey result = (MappingKey) iidCaptor.getValue().firstKeyOf(Mapping.class);
        assertEquals("ipv4:" + IPV4_STRING, result.getEidUri().getValue());
        assertEquals(MappingOrigin.Northbound, result.getOrigin());
    }

    /**
     * Tests {@link DataStoreBackEnd#getAllMappings} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getAllMappingsTest() throws ReadFailedException {
        //final List<Mapping> mappings = new ArrayList<>();

        final ReadOnlyTransaction rTxMock = Mockito.mock(ReadOnlyTransaction.class);
        final CheckedFuture<Optional<MappingDatabase>, ReadFailedException> readFutureMock
                = Mockito.mock(CheckedFuture.class);
        final Optional<MappingDatabase> optionalMock = Mockito.mock(Optional.class);

        Mockito.when(txChainMock.newReadOnlyTransaction()).thenReturn(rTxMock);
        Mockito.when(rTxMock.read(LogicalDatastoreType.CONFIGURATION, DATABASE_ROOT)).thenReturn(readFutureMock);
        Mockito.when(rTxMock.read(LogicalDatastoreType.OPERATIONAL, DATABASE_ROOT)).thenReturn(readFutureMock);
        Mockito.when(readFutureMock.checkedGet()).thenReturn(optionalMock);
        Mockito.when(optionalMock.isPresent()).thenReturn(true);

        dataStoreBackEnd.getAllMappings();
        Mockito.verify(optionalMock, Mockito.times(2)).get();

        //TODO
    }

    /**
     * Tests {@link DataStoreBackEnd#removeAllDatastoreContent} method.
     */
    @Test
    public void removeAllDatastoreContentTest() {
        dataStoreBackEnd.removeAllDatastoreContent();
        Mockito.verify(wTxMock).delete(LogicalDatastoreType.CONFIGURATION, DATABASE_ROOT);
        Mockito.verify(wTxMock).delete(LogicalDatastoreType.OPERATIONAL, DATABASE_ROOT);
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setEid(EID_IPV4)
                .setLocatorRecord(new ArrayList<>())
                .setXtrId(new XtrId(XTR_ID));
    }

    private static AuthenticationKeyBuilder getDefaultAuthenticationKeyBuilder() {
        return new AuthenticationKeyBuilder()
                .setKey(new AuthenticationKeyKey(new EidUri(DUMMY_URI)))
                .setEid(EID_IPV4)
                .setMappingAuthkey(new MappingAuthkeyBuilder().setKeyString("pswd").setKeyType(0).build());
    }
}
