/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.implementation.mdsal.AuthenticationKeyDataListener;
import org.opendaylight.lispflowmapping.implementation.mdsal.MappingDataListener;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.RPCInputConvertorUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingsInput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class MappingServiceTest {

    @Mock(name = "mappingSystem") private static MappingSystem mappingSystem;
    @Mock(name = "dsbe") private static DataStoreBackEnd dsbe;
    @Mock(name = "keyListener") private static AuthenticationKeyDataListener keyListener;
    @Mock(name = "mappingListener") private static MappingDataListener mappingListener;

    private final DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private final NotificationPublishService notificationPublishService = Mockito
            .mock(NotificationPublishService.class);
    private final ILispDAO lispDAO = Mockito.mock(ILispDAO.class);

    @InjectMocks
    MappingService mappingService = new MappingService(dataBroker,
            notificationPublishService, lispDAO);

    private static final String IPV4_STRING = "1.2.3.0";
    private static final Eid IPV4_EID = LispAddressUtil.asIpv4Eid(IPV4_STRING);

    private static final RpcResult<Object> RPC_RESULT_SUCCESS = RpcResultBuilder.success().build();
    private static final MappingAuthkey MAPPING_AUTHKEY = new MappingAuthkeyBuilder()
            .setKeyString("password")
            .setKeyType(1).build();
    private static final SiteId SITE_ID = new SiteId(new byte[] {0, 1, 2, 3, 4, 5, 6, 7});
    private static final XtrId XTR_ID = new XtrId(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
    private static final MappingData DUMMY_MAPPING = new MappingData(null);

    /**
     * Tests {@link MappingService#addKey} method.
     */
    @Test
    public void addKeyTest() throws ExecutionException, InterruptedException {
        final MappingAuthkey mappingAuthkey = new MappingAuthkeyBuilder()
                .setKeyString("dummy-password")
                .setKeyType(2).build();
        final AddKeyInput addKeyInput = new AddKeyInputBuilder()
                .setMappingAuthkey(mappingAuthkey)
                .setEid(IPV4_EID).build();
        Mockito.when(mappingSystem.getAuthenticationKey(IPV4_EID)).thenReturn(MAPPING_AUTHKEY);

        // input
        final RpcResult<Object> rpc = RpcResultBuilder.failed().withError(RpcError.ErrorType.PROTOCOL, "data-exists",
                "Key already exists! Please use update-key if you want to change it.").build();
        final RpcError error = rpc.getErrors().iterator().next(); // equals() not implemented int RpcError

        // result
        final Future<RpcResult<Void>> result = mappingService.addKey(addKeyInput);
        final RpcError errorResult = result.get().getErrors().iterator().next();

        assertEquals(1, result.get().getErrors().size());
        assertEquals(error.getMessage(), errorResult.getMessage());
        assertEquals(error.getApplicationTag(), errorResult.getApplicationTag());
        assertEquals(error.getCause(), errorResult.getCause());
        assertEquals(error.getErrorType(), errorResult.getErrorType());
        assertEquals(error.getInfo(), errorResult.getInfo());
        assertEquals(error.getTag(), errorResult.getTag());
        assertEquals(error.getSeverity(), errorResult.getSeverity());
        assertEquals(rpc.getResult(), result.get().getResult());
        assertEquals(rpc.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#addKey} method with null MappingAuthkey.
     */
    @Test
    public void addKeyTest_withNullMapAuthkey() throws ExecutionException, InterruptedException {
        final AddKeyInput addKeyInput = new AddKeyInputBuilder().setEid(IPV4_EID).build();
        Mockito.when(mappingSystem.getAuthenticationKey(IPV4_EID)).thenReturn(null);

        final Future<RpcResult<Void>> result = mappingService.addKey(addKeyInput);
        Mockito.verify(dsbe).addAuthenticationKey(Mockito.any());

        assertEquals(RPC_RESULT_SUCCESS.getErrors(), result.get().getErrors());
        assertEquals(RPC_RESULT_SUCCESS.getResult(), result.get().getResult());
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#addMapping} method.
     */
    @Test
    public void addMappingTest() throws ExecutionException, InterruptedException {
        final MappingRecordBuilder mappingRecordBuilder = getDefaultMappingRecordBuilder();
        final AddMappingInput addMappingInput = new AddMappingInputBuilder()
                .setMappingRecord(mappingRecordBuilder.build()).build();

        final Future<RpcResult<Void>> result = mappingService.addMapping(addMappingInput);
        Mockito.verify(dsbe).addMapping(RPCInputConvertorUtil.toMapping(addMappingInput));

        assertEquals(RPC_RESULT_SUCCESS.getErrors(), result.get().getErrors());
        assertEquals(RPC_RESULT_SUCCESS.getResult(), result.get().getResult());
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#getKey} method.
     */
    @Test
    public void getKeyTest() throws ExecutionException, InterruptedException {
        final GetKeyInput getKeyInput = new GetKeyInputBuilder().setEid(IPV4_EID).build();
        final RpcResult<GetKeyOutput> rpcResult = RpcResultBuilder
                .success(new GetKeyOutputBuilder().setMappingAuthkey(MAPPING_AUTHKEY).build()).build();

        Mockito.when(mappingSystem.getAuthenticationKey(getKeyInput.getEid())).thenReturn(MAPPING_AUTHKEY);

        final Future<RpcResult<GetKeyOutput>> result = mappingService.getKey(getKeyInput);
        assertEquals(rpcResult.getErrors(), result.get().getErrors());
        assertEquals(rpcResult.getResult(), result.get().getResult());
        assertEquals(rpcResult.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#getKey} method with null MappingAuthkey.
     */
    @Test
    public void getKeyTest_withNullMappingAuthkey() throws ExecutionException, InterruptedException {
        // input
        final GetKeyInput getKeyInput = new GetKeyInputBuilder().setEid(IPV4_EID).build();
        Mockito.when(mappingSystem.getAuthenticationKey(getKeyInput.getEid())).thenReturn(null);

        final RpcResult<Object> rpc = RpcResultBuilder.failed()
                .withError(RpcError.ErrorType.APPLICATION, "data-missing", "Key was not found in the mapping database")
                .build();
        final RpcError error = rpc.getErrors().iterator().next();

        // result
        final Future<RpcResult<GetKeyOutput>> result = mappingService.getKey(getKeyInput);
        final RpcError errorResult = result.get().getErrors().iterator().next();

        assertEquals(1, result.get().getErrors().size());
        assertEquals(error.getMessage(), errorResult.getMessage());
        assertEquals(error.getApplicationTag(), errorResult.getApplicationTag());
        assertEquals(error.getCause(), errorResult.getCause());
        assertEquals(error.getErrorType(), errorResult.getErrorType());
        assertEquals(error.getInfo(), errorResult.getInfo());
        assertEquals(error.getTag(), errorResult.getTag());
        assertEquals(error.getSeverity(), errorResult.getSeverity());
        assertEquals(rpc.getResult(), result.get().getResult());
        assertEquals(rpc.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#getMapping} method.
     */
    @Test
    public void getMappingTest() throws ExecutionException, InterruptedException {
        // input
        final GetMappingInput getMappingInput = new GetMappingInputBuilder().setEid(IPV4_EID).build();
        final MappingData mappingData = getDefaultMappingData();
        final MappingRecord nonBinaryMappingRecord = getDefaultMappingRecordBuilder()
                .setEid(LispAddressUtil.toEid(new Ipv4Address(IPV4_STRING), null)).build();

        Mockito.when(mappingSystem.getMapping(getMappingInput.getEid())).thenReturn(mappingData);

        final RpcResult<GetMappingOutput> rpc = RpcResultBuilder
                .success(new GetMappingOutputBuilder().setMappingRecord(nonBinaryMappingRecord)).build();

        //result
        final Future<RpcResult<GetMappingOutput>> result = mappingService.getMapping(getMappingInput);
        final RpcResult<GetMappingOutput> rpcResult = result.get();

        assertEquals(rpc.getResult(), rpcResult.getResult());
        assertEquals(rpc.getErrors(), rpcResult.getErrors());
        assertEquals(rpc.isSuccessful(), rpcResult.isSuccessful());
    }

    /**
     * Tests {@link MappingService#getMapping} method with null MappingRecord.
     */
    @Test
    public void getMappingTest_withNullMapRecord() throws ExecutionException, InterruptedException {
        // input
        final GetMappingInput getMappingInput = new GetMappingInputBuilder().setEid(IPV4_EID).build();
        Mockito.when(mappingSystem.getMapping(getMappingInput.getEid())).thenReturn(null);

        final RpcResult<Object> rpc = RpcResultBuilder.failed().withError(RpcError.ErrorType.APPLICATION,
                "data-missing", "No mapping was found in the mapping database").build();
        final RpcError error = rpc.getErrors().iterator().next();

        //result
        final Future<RpcResult<GetMappingOutput>> result = (mappingService.getMapping(getMappingInput));
        final RpcError errorResult = result.get().getErrors().iterator().next();

        assertEquals(1, result.get().getErrors().size());
        assertEquals(error.getMessage(), errorResult.getMessage());
        assertEquals(error.getApplicationTag(), errorResult.getApplicationTag());
        assertEquals(error.getCause(), errorResult.getCause());
        assertEquals(error.getErrorType(), errorResult.getErrorType());
        assertEquals(error.getInfo(), errorResult.getInfo());
        assertEquals(error.getTag(), errorResult.getTag());
        assertEquals(error.getSeverity(), errorResult.getSeverity());
        assertEquals(rpc.getResult(), result.get().getResult());
        assertEquals(rpc.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#removeKey} method.
     */
    @Test
    public void removeKeyTest() throws ExecutionException, InterruptedException {
        final RemoveKeyInput removeKeyInput = new RemoveKeyInputBuilder().setEid(IPV4_EID).build();

        final Future<RpcResult<Void>> result = mappingService.removeKey(removeKeyInput);
        Mockito.verify(dsbe).removeAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(removeKeyInput));

        assertEquals(RPC_RESULT_SUCCESS.getErrors(), result.get().getErrors());
        assertEquals(RPC_RESULT_SUCCESS.getResult(), result.get().getResult());
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#removeMapping} method.
     */
    @Test
    public void removeMappingTest() throws ExecutionException, InterruptedException {
        final RemoveMappingInput removeMappingInput = new RemoveMappingInputBuilder().setEid(IPV4_EID).build();

        final Future<RpcResult<Void>> result = mappingService.removeMapping(removeMappingInput);
        Mockito.verify(dsbe).removeMapping(RPCInputConvertorUtil.toMapping(removeMappingInput));

        assertEquals(RPC_RESULT_SUCCESS.getErrors(), result.get().getErrors());
        assertEquals(RPC_RESULT_SUCCESS.getResult(), result.get().getResult());
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#updateKey} method.
     */
    @Test
    public void updateKeyTest() throws ExecutionException, InterruptedException {
        final UpdateKeyInput updateKeyInput = new UpdateKeyInputBuilder().setEid(IPV4_EID).build();
        Mockito.when(mappingSystem.getAuthenticationKey(IPV4_EID)).thenReturn(MAPPING_AUTHKEY);

        final Future<RpcResult<Void>> result = mappingService.updateKey(updateKeyInput);
        Mockito.verify(dsbe).updateAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(updateKeyInput));

        assertEquals(RPC_RESULT_SUCCESS.getErrors(), result.get().getErrors());
        assertEquals(RPC_RESULT_SUCCESS.getResult(), result.get().getResult());
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#updateKey} method with null MappingAuthkey.
     */
    @Test
    public void updateKeyTest_withNullMapAuthkey() throws ExecutionException, InterruptedException {
        final UpdateKeyInput updateKeyInput = new UpdateKeyInputBuilder().setEid(IPV4_EID).build();
        Mockito.when(mappingSystem.getAuthenticationKey(IPV4_EID)).thenReturn(null);

        // input
        final RpcResult<Object> rpc = RpcResultBuilder.failed().withError(RpcError.ErrorType.PROTOCOL, "data-missing",
                "Key doesn't exist! Please use add-key if you want to create a new authentication key.").build();
        final RpcError error = rpc.getErrors().iterator().next();

        // result
        final Future<RpcResult<Void>> result = mappingService.updateKey(updateKeyInput);
        final RpcError errorResult = result.get().getErrors().iterator().next();

        assertEquals(1, result.get().getErrors().size());
        assertEquals(error.getMessage(), errorResult.getMessage());
        assertEquals(error.getApplicationTag(), errorResult.getApplicationTag());
        assertEquals(error.getCause(), errorResult.getCause());
        assertEquals(error.getErrorType(), errorResult.getErrorType());
        assertEquals(error.getInfo(), errorResult.getInfo());
        assertEquals(error.getTag(), errorResult.getTag());
        assertEquals(error.getSeverity(), errorResult.getSeverity());
        assertEquals(rpc.getResult(), result.get().getResult());
        assertEquals(rpc.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#updateMapping} method.
     */
    @Test
    public void updateMappingTest() throws ExecutionException, InterruptedException {
        final MappingRecord mappingRecord = getDefaultMappingRecordBuilder().build();
        final UpdateMappingInput updateMappingInput = new UpdateMappingInputBuilder()
                .setMappingRecord(mappingRecord).build();

        final Future<RpcResult<Void>> result = mappingService.updateMapping(updateMappingInput);
        Mockito.verify(dsbe).updateMapping(RPCInputConvertorUtil.toMapping(updateMappingInput));

        assertEquals(RPC_RESULT_SUCCESS.getErrors(), result.get().getErrors());
        assertEquals(RPC_RESULT_SUCCESS.getResult(), result.get().getResult());
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(), result.get().isSuccessful());
    }

    /**
     * Tests {@link MappingService#addMapping} method from southbound.
     */
    @Test
    public void addMappingTest_fromSouthbound() throws ExecutionException, InterruptedException {
        // input
        final MappingRecord record = getDefaultMappingRecordBuilder()
                .setXtrId(XTR_ID).build();
        final MappingData data = getDefaultMappingData(record);

        mappingService.addMapping(MappingOrigin.Southbound, IPV4_EID, SITE_ID, data);

        Mockito.verify(mappingSystem).addMapping(MappingOrigin.Southbound, IPV4_EID, data);
        Mockito.verify(dsbe).addMapping(DSBEInputUtil.toMapping(MappingOrigin.Southbound, IPV4_EID, SITE_ID, data));
    }

    /**
     * Tests {@link MappingService#addMapping} method from northbound.
     */
    @Test
    public void addMappingTest_fromNorthbound() throws ExecutionException, InterruptedException {
        // input
        final MappingOrigin origin = MappingOrigin.Northbound;
        final MappingRecord record = getDefaultMappingRecordBuilder()
                .setXtrId(XTR_ID).build();
        final MappingData data = getDefaultMappingData(record);

        mappingService.addMapping(origin, IPV4_EID, SITE_ID, data);
        Mockito.verify(dsbe).addMapping(DSBEInputUtil.toMapping(origin, IPV4_EID, SITE_ID, data));
        Mockito.verifyZeroInteractions(mappingSystem);
        Mockito.verifyNoMoreInteractions(dsbe);
    }

    /**
     * Tests {@link MappingService#getMapping(MappingOrigin, Eid)} method.
     */
    @Test
    public void getMappingTest_withOriginAndEid() {
        Mockito.when(mappingSystem.getMapping(Mockito.any(MappingOrigin.class), Mockito.any(Eid.class)))
                .thenReturn(DUMMY_MAPPING);
        assertEquals(DUMMY_MAPPING, mappingService.getMapping(MappingOrigin.Northbound, IPV4_EID));
    }

    /**
     * Tests {@link MappingService#getMapping(Eid)} method.
     */
    @Test
    public void getMappingTest_withEid() {
        Mockito.when(mappingSystem.getMapping(Mockito.any(Eid.class))).thenReturn(DUMMY_MAPPING);
        assertEquals(DUMMY_MAPPING, mappingService.getMapping(IPV4_EID));
    }

    /**
     * Tests {@link MappingService#getMapping(Eid)} method.
     */
    @Test
    public void getMappingTest_withSrcAndDstEid() {
        Mockito.when(mappingSystem.getMapping(Mockito.any(Eid.class), Mockito.any(Eid.class)))
            .thenReturn(DUMMY_MAPPING);
        assertEquals(DUMMY_MAPPING, mappingService.getMapping(IPV4_EID, IPV4_EID));
    }

    /**
     * Tests {@link MappingService#removeMapping} method.
     */
    @Test
    public void removeMappingTest_withMapOrginAndEid() {
        mappingService.removeMapping(MappingOrigin.Northbound, IPV4_EID);
        Mockito.verify(dsbe).removeMapping(DSBEInputUtil.toMapping(MappingOrigin.Northbound, IPV4_EID));
    }

    /**
     * Tests {@link MappingService#addAuthenticationKey} method.
     */
    @Test
    public void addAuthenticationKeyTest() {
        mappingService.addAuthenticationKey(IPV4_EID, MAPPING_AUTHKEY);
        Mockito.verify(dsbe).addAuthenticationKey(DSBEInputUtil.toAuthenticationKey(IPV4_EID, MAPPING_AUTHKEY));
    }

    /**
     * Tests {@link MappingService#getAuthenticationKey(Eid)} method.
     */
    @Test
    public void getAuthenticationKeyTest() {
        Mockito.when(mappingSystem.getAuthenticationKey(IPV4_EID)).thenReturn(MAPPING_AUTHKEY);
        assertEquals(MAPPING_AUTHKEY, mappingService.getAuthenticationKey(IPV4_EID));
    }

    /**
     * Tests {@link MappingService#removeAuthenticationKey} method.
     */
    @Test
    public void removeAuthenticationKeyTest() {
        mappingService.removeAuthenticationKey(IPV4_EID);
        Mockito.verify(dsbe).removeAuthenticationKey(DSBEInputUtil.toAuthenticationKey(IPV4_EID, null));
    }

    /**
     * Tests {@link MappingService#addData} method.
     */
    @Test
    public void addDataTest() {
        mappingService.addData(MappingOrigin.Northbound, IPV4_EID, SubKeys.RECORD, DUMMY_MAPPING);
        Mockito.verify(mappingSystem).addData(MappingOrigin.Northbound, IPV4_EID, SubKeys.RECORD, DUMMY_MAPPING);
    }

    /**
     * Tests {@link MappingService#getData} method.
     */
    @Test
    public void getDataTest() {
        Mockito.when(mappingSystem.getData(MappingOrigin.Northbound, IPV4_EID, SubKeys.RECORD))
                .thenReturn(DUMMY_MAPPING);
        assertEquals(DUMMY_MAPPING, mappingService.getData(MappingOrigin.Northbound, IPV4_EID, SubKeys.RECORD));
    }

    /**
     * Tests {@link MappingService#removeData} method.
     */
    @Test
    public void removeDataTest() {
        mappingService.removeData(MappingOrigin.Northbound, IPV4_EID, SubKeys.RECORD);
        Mockito.verify(mappingSystem).removeData(MappingOrigin.Northbound, IPV4_EID, SubKeys.RECORD);
    }

    /**
     * Tests {@link MappingService#printMappings} method.
     */
    @Test
    public void printMappingsTest() {
        mappingService.printMappings();
        Mockito.verify(mappingSystem).printMappings();
    }

    /**
     * Tests {@link MappingService#close} method.
     */
    @Test
    public void close() throws Exception {
        mappingService.close();
        Mockito.verify(keyListener).closeDataChangeListener();
        Mockito.verify(mappingListener).closeDataChangeListener();
    }

    /**
     * Tests {@link MappingService#cleanCachedMappings} method.
     */
    @Test
    public void cleanCachedMappingsTest() throws Exception {
        mappingService.cleanCachedMappings();
        Mockito.verify(mappingSystem).cleanCaches();
        Mockito.verify(dsbe).removeAllDatastoreContent();
    }

    /**
     * Tests {@link MappingService} not implemented methods.
     */
    @Test
    public void nullReturnMethodTest() throws ExecutionException, InterruptedException {
        assertNull(mappingService.removeKeys(Mockito.mock(RemoveKeysInput.class)));
        assertNull(mappingService.removeMappings(Mockito.mock(RemoveMappingsInput.class)));
        assertNull(mappingService.getKeys(Mockito.mock(GetKeysInput.class)));
        assertNull(mappingService.addMappings(Mockito.mock(AddMappingsInput.class)));
        assertNull(mappingService.updateKeys(Mockito.mock(UpdateKeysInput.class)));
        assertNull(mappingService.removeAllMappings());
        assertNull(mappingService.getAllKeys());
        assertNull(mappingService.updateMappings(Mockito.mock(UpdateMappingsInput.class)));
        assertNull(mappingService.addKeys(Mockito.mock(AddKeysInput.class)));
        assertNull(mappingService.getAllMappings());
    }

    private static MappingData getDefaultMappingData() {
        return getDefaultMappingData(null);
    }

    private static MappingData getDefaultMappingData(MappingRecord mappingRecord) {
        if (mappingRecord == null) {
            mappingRecord = getDefaultMappingRecordBuilder().build();
        }
        return new MappingData(mappingRecord, System.currentTimeMillis());
    }

    private static MappingRecordBuilder getDefaultMappingRecordBuilder() {
        return new MappingRecordBuilder()
                .setAction(MappingRecord.Action.NoAction)
                .setAuthoritative(false)
                .setLocatorRecord(new ArrayList<>())
                .setMapVersion((short) 0)
                .setRecordTtl(60)
                .setEid(IPV4_EID);
    }
}