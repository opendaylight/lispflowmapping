/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.dsbackend;

import static org.junit.Assert.assertEquals;

import com.google.common.util.concurrent.FluentFuture;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.bind.DatatypeConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.EidUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.VniUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.XtrIdUri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMappingKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifierBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifierKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PowerMockIgnore({"javax.xml.datatype.*"}) // for Java 9+ compatibility, see https://github.com/powermock/powermock/issues/864
@PrepareForTest({LoggerFactory.class})
public class DataStoreBackEndTest {

    @Captor private static ArgumentCaptor<DataObjectIdentifier<AuthenticationKey>> iidCaptorAuthKey;
    @Captor private static ArgumentCaptor<DataObjectIdentifier<Mapping>> iidCaptorMapping;
    @Captor private static ArgumentCaptor<DataObjectIdentifier<XtrIdMapping>> iidCaptorXtrIdMapping;
    @Mock private static TransactionChain txChainMock;
    @Mock private static WriteTransaction wTxMock;
    private static DataStoreBackEnd dataStoreBackEnd;

    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_STRING_2 = "1.2.4.0";
    private static final String IPV4_STRING_3 = "1.2.5.0";
    private static final String IPV4_STRING_4 = "1.2.6.0";
    private static final String DUMMY_URI = "dummy/uri";
    private static final Eid EID_IPV4_1 = LispAddressUtil.asIpv4Eid(IPV4_STRING_1);
    private static final Eid EID_IPV4_2 = LispAddressUtil.asIpv4Eid(IPV4_STRING_2);
    private static final Eid EID_IPV4_3 = LispAddressUtil.asIpv4Eid(IPV4_STRING_3);
    private static final Eid EID_IPV4_4 = LispAddressUtil.asIpv4Eid(IPV4_STRING_4);
    private static final byte[] XTR_ID = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final DataObjectIdentifier<MappingDatabase> DATABASE_ROOT =
        DataObjectIdentifier.builder(MappingDatabase.class).build();

    @Before
    public void init() {
        final DataBroker brokerMock = Mockito.mock(DataBroker.class);
        final Logger logMock = Mockito.mock(Logger.class, "LOG");

        PowerMockito.mockStatic(LoggerFactory.class);
        PowerMockito.when(LoggerFactory.getLogger(DataStoreBackEnd.class))
                .thenReturn(logMock);
        Mockito.when(logMock.isDebugEnabled()).thenReturn(true);
        Mockito.when(brokerMock.createMergingTransactionChain())
                .thenReturn(txChainMock);
        dataStoreBackEnd = PowerMockito.spy(new DataStoreBackEnd(brokerMock));

        Mockito.when(txChainMock.newWriteOnlyTransaction()).thenReturn(wTxMock);
        Mockito.doReturn(CommitInfo.emptyFluentFuture()).when(wTxMock).commit();
    }

    /**
     * Tests {@link DataStoreBackEnd#addAuthenticationKey} method.
     */
    @Test
    public void addAuthenticationKeyTest() {
        final AuthenticationKey authenticationKey = getDefaultAuthenticationKeyBuilder().build();

        dataStoreBackEnd.addAuthenticationKey(authenticationKey);
        Mockito.verify(wTxMock).mergeParentStructurePut(Mockito.eq(LogicalDatastoreType.CONFIGURATION),
                iidCaptorAuthKey.capture(), Mockito.eq(authenticationKey));

        // result
        AuthenticationKeyKey result = iidCaptorAuthKey.getValue().firstKeyOf(AuthenticationKey.class);
        assertEquals("ipv4:" + IPV4_STRING_1, result.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#addMapping} method.
     */
    @Test
    public void addMappingTest() {
        final Mapping mapping = new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(EID_IPV4_1)),
                        MappingOrigin.Northbound))
                .setMappingRecord(getDefaultMappingRecordBuilder().build())
                .setOrigin(MappingOrigin.Northbound).build();

        dataStoreBackEnd.addMapping(mapping);
        Mockito.verify(wTxMock).mergeParentStructurePut(Mockito.eq(LogicalDatastoreType.CONFIGURATION),
                iidCaptorMapping.capture(), Mockito.eq(mapping));

        // result
        MappingKey result = iidCaptorMapping.getValue().firstKeyOf(Mapping.class);
        assertEquals("ipv4:" + IPV4_STRING_1, result.getEidUri().getValue());
        assertEquals(MappingOrigin.Northbound, result.getOrigin());
    }

    /**
     * Tests {@link DataStoreBackEnd#addXtrIdMapping} method.
     */
    @Test
    public void addXtrIdMappingTest() {
        XtrIdMapping xtrIdMapping = new XtrIdMappingBuilder()
                .setXtrIdUri(new XtrIdUri(DUMMY_URI))
                .setMappingRecord(getDefaultMappingRecordBuilder().build()).build();

        dataStoreBackEnd.addXtrIdMapping(xtrIdMapping);
        Mockito.verify(wTxMock).mergeParentStructurePut(Mockito.eq(LogicalDatastoreType.OPERATIONAL),
                iidCaptorXtrIdMapping.capture(), Mockito.eq(xtrIdMapping));

        // result
        XtrIdMappingKey xtrIdResult = iidCaptorXtrIdMapping.getValue().firstKeyOf(XtrIdMapping.class);
        MappingKey mappingResult = iidCaptorXtrIdMapping.getValue().firstKeyOf(Mapping.class);

        assertEquals(DatatypeConverter.printHexBinary(XTR_ID), xtrIdResult.getXtrIdUri().getValue());
        assertEquals(MappingOrigin.Southbound, mappingResult.getOrigin());
        assertEquals("ipv4:" + IPV4_STRING_1, mappingResult.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#removeAuthenticationKey} method.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        final AuthenticationKey authenticationKey = getDefaultAuthenticationKeyBuilder().build();

        dataStoreBackEnd.removeAuthenticationKey(authenticationKey);
        Mockito.verify(wTxMock).delete(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptorAuthKey.capture());

        // result
        AuthenticationKeyKey result = iidCaptorAuthKey.getValue().firstKeyOf(AuthenticationKey.class);
        assertEquals("ipv4:" + IPV4_STRING_1, result.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#removeMapping} method.
     */
    @Test
    public void removeMapping() {
        final Mapping mapping = new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(EID_IPV4_1)),
                        MappingOrigin.Northbound))
                .setMappingRecord(getDefaultMappingRecordBuilder().build())
                .setOrigin(MappingOrigin.Northbound).build();

        dataStoreBackEnd.removeMapping(mapping);
        Mockito.verify(wTxMock).delete(Mockito.eq(LogicalDatastoreType.CONFIGURATION), iidCaptorMapping.capture());

        // result
        MappingKey result = iidCaptorMapping.getValue().firstKeyOf(Mapping.class);
        assertEquals("ipv4:" + IPV4_STRING_1, result.getEidUri().getValue());
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

        dataStoreBackEnd.removeXtrIdMapping(xtrIdMapping);
        Mockito.verify(wTxMock).delete(Mockito.eq(LogicalDatastoreType.OPERATIONAL), iidCaptorXtrIdMapping.capture());

        // result
        XtrIdMappingKey xtrIdResult = iidCaptorXtrIdMapping.getValue().firstKeyOf(XtrIdMapping.class);
        MappingKey mappingResult = iidCaptorXtrIdMapping.getValue().firstKeyOf(Mapping.class);

        assertEquals(DatatypeConverter.printHexBinary(XTR_ID), xtrIdResult.getXtrIdUri().getValue());
        assertEquals(MappingOrigin.Southbound, mappingResult.getOrigin());
        assertEquals("ipv4:" + IPV4_STRING_1, mappingResult.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#updateAuthenticationKey} method.
     */
    @Test
    public void updateAuthenticationKeyTest() {
        final AuthenticationKey authenticationKey = getDefaultAuthenticationKeyBuilder().build();

        dataStoreBackEnd.updateAuthenticationKey(authenticationKey);
        Mockito.verify(wTxMock).mergeParentStructurePut(Mockito.eq(LogicalDatastoreType.CONFIGURATION),
                iidCaptorAuthKey.capture(), Mockito.eq(authenticationKey));

        // result
        AuthenticationKeyKey result = iidCaptorAuthKey.getValue().firstKeyOf(AuthenticationKey.class);
        assertEquals("ipv4:" + IPV4_STRING_1, result.getEidUri().getValue());
    }

    /**
     * Tests {@link DataStoreBackEnd#updateMapping} method.
     */
    @Test
    public void updateMappingTest() {
        final Mapping mapping = new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(EID_IPV4_1)),
                        MappingOrigin.Northbound))
                .setMappingRecord(getDefaultMappingRecordBuilder().build())
                .setOrigin(MappingOrigin.Northbound).build();

        dataStoreBackEnd.updateMapping(mapping);
        Mockito.verify(wTxMock).mergeParentStructurePut(Mockito.eq(LogicalDatastoreType.CONFIGURATION),
                iidCaptorMapping.capture(), Mockito.eq(mapping));

        // result
        MappingKey result = iidCaptorMapping.getValue().firstKeyOf(Mapping.class);
        assertEquals("ipv4:" + IPV4_STRING_1, result.getEidUri().getValue());
        assertEquals(MappingOrigin.Northbound, result.getOrigin());
    }

    /**
     * Tests {@link DataStoreBackEnd#getAllMappings} method.
     */
    @Test
    public void getAllMappingsTest() {
        final ReadTransaction rTxMock = Mockito.mock(ReadTransaction.class);
        final FluentFuture<Optional<MappingDatabase>> readFutureMock = FluentFutures.immediateFluentFuture(
            Optional.of(getDefaultMappingDatabase().build()));

        Mockito.when(txChainMock.newReadOnlyTransaction()).thenReturn(rTxMock);
        Mockito.when(rTxMock.read(LogicalDatastoreType.CONFIGURATION, DATABASE_ROOT)).thenReturn(readFutureMock);
        Mockito.when(rTxMock.read(LogicalDatastoreType.OPERATIONAL, DATABASE_ROOT)).thenReturn(readFutureMock);

        assertEquals(8, dataStoreBackEnd.getAllMappings().size());
    }

    /**
     * Tests {@link DataStoreBackEnd#getAllAuthenticationKeys} method.
     */
    @Test
    public void getAllAuthenticationKeysTest() {
        final ReadTransaction rTxMock = Mockito.mock(ReadTransaction.class);
        final FluentFuture<Optional<MappingDatabase>> readFutureMock = FluentFutures.immediateFluentFuture(
            Optional.of(getDefaultMappingDatabase().build()));

        Mockito.when(txChainMock.newReadOnlyTransaction()).thenReturn(rTxMock);
        Mockito.when(rTxMock.read(LogicalDatastoreType.CONFIGURATION, DATABASE_ROOT)).thenReturn(readFutureMock);

        assertEquals(4, dataStoreBackEnd.getAllAuthenticationKeys().size());
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

    /**
     * Increases {@link DataStoreBackEnd} code coverage.
     */
    @Test
    public void onTransactionChainFailedTest() {
        dataStoreBackEnd.onTransactionChainFailed(Mockito.mock(TransactionChain.class), Mockito.mock(Throwable.class));
    }

    /**
     * Increases {@link DataStoreBackEnd} code coverage.
     */
    @Test
    public void onTransactionChainSuccessfulTest() {
        dataStoreBackEnd.onTransactionChainSuccessful(Mockito.mock(TransactionChain.class));
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setEid(EID_IPV4_1)
                .setLocatorRecord(new ArrayList<>())
                .setXtrId(new XtrId(XTR_ID));
    }

    private static AuthenticationKeyBuilder getDefaultAuthenticationKeyBuilder() {
        return new AuthenticationKeyBuilder()
                .withKey(new AuthenticationKeyKey(new EidUri(DUMMY_URI)))
                .setEid(EID_IPV4_1)
                .setMappingAuthkey(new MappingAuthkeyBuilder().setKeyString("password").setKeyType(Uint16.valueOf(0))
                .build());
    }

    private static MappingDatabaseBuilder getDefaultMappingDatabase() {
        final Mapping mapping_1 = new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(EID_IPV4_1)),
                        MappingOrigin.Northbound))
                .setMappingRecord(getDefaultMappingRecordBuilder().build()).build();
        final Mapping mapping_2 = new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(EID_IPV4_2)),
                        MappingOrigin.Northbound))
                .setMappingRecord(getDefaultMappingRecordBuilder().setEid(EID_IPV4_2).build()).build();
        final Mapping mapping_3 = new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(EID_IPV4_3)),
                        MappingOrigin.Northbound))
                .setMappingRecord(getDefaultMappingRecordBuilder().setEid(EID_IPV4_3).build()).build();
        final Mapping mapping_4 = new MappingBuilder()
                .withKey(new MappingKey(new EidUri(LispAddressStringifier.getURIString(EID_IPV4_4)),
                        MappingOrigin.Northbound))
                .setMappingRecord(getDefaultMappingRecordBuilder().setEid(EID_IPV4_4).build()).build();

        final Map<MappingKey, Mapping> mappingEntries_1 = new LinkedHashMap<>();
        mappingEntries_1.put(new MappingKey(new EidUri("uri-1"), MappingOrigin.Northbound), mapping_1);
        mappingEntries_1.put(new MappingKey(new EidUri("uri-2"), MappingOrigin.Northbound), mapping_2);
        final Map<MappingKey, Mapping> mappingEntries_2 = new LinkedHashMap<>();
        mappingEntries_2.put(new MappingKey(new EidUri("uri-3"), MappingOrigin.Northbound), mapping_3);
        mappingEntries_2.put(new MappingKey(new EidUri("uri-4"), MappingOrigin.Northbound), mapping_4);

        final AuthenticationKey authenticationKey_1 = new AuthenticationKeyBuilder()
                .withKey(new AuthenticationKeyKey(new EidUri("uri-1"))).build();
        final AuthenticationKey authenticationKey_2 = new AuthenticationKeyBuilder()
                .withKey(new AuthenticationKeyKey(new EidUri("uri-2"))).build();
        final AuthenticationKey authenticationKey_3 = new AuthenticationKeyBuilder()
                .withKey(new AuthenticationKeyKey(new EidUri("uri-3"))).build();
        final AuthenticationKey authenticationKey_4 = new AuthenticationKeyBuilder()
                .withKey(new AuthenticationKeyKey(new EidUri("uri-4"))).build();

        final Map<AuthenticationKeyKey, AuthenticationKey> authKey_1 = new LinkedHashMap<>();
        authKey_1.put(new AuthenticationKeyKey(new EidUri("uri-1")), authenticationKey_1);
        authKey_1.put(new AuthenticationKeyKey(new EidUri("uri-2")), authenticationKey_2);
        final Map<AuthenticationKeyKey, AuthenticationKey> authKey_2 = new LinkedHashMap<>();
        authKey_2.put(new AuthenticationKeyKey(new EidUri("uri-3")), authenticationKey_3);
        authKey_2.put(new AuthenticationKeyKey(new EidUri("uri-4")), authenticationKey_4);

        final VirtualNetworkIdentifier vni_1 = new VirtualNetworkIdentifierBuilder()
                .setVni(new VniUri("vni/uri/1"))
                .setMapping(mappingEntries_1)
                .setAuthenticationKey(authKey_1).build();
        final VirtualNetworkIdentifier vni_2 = new VirtualNetworkIdentifierBuilder()
                .setVni(new VniUri("vni/uri/2"))
                .setMapping(mappingEntries_2)
                .setAuthenticationKey(authKey_2).build();

        final Map<VirtualNetworkIdentifierKey, VirtualNetworkIdentifier> vniEntries = new LinkedHashMap<>();
        vniEntries.put(new VirtualNetworkIdentifierKey(new VniUri("vni/uri/1")), vni_1);
        vniEntries.put(new VirtualNetworkIdentifierKey(new VniUri("vni/uri/2")), vni_2);

        return new MappingDatabaseBuilder().setVirtualNetworkIdentifier(vniEntries);
    }
}
