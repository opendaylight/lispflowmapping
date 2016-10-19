/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRequestSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregistermessage.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregistermessage.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.GetStatsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapNotifyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRegisterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.get.stats.output.ControlMessageStats;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class LispSouthboundRpcTest {

    @Mock LispSouthboundPlugin lispSouthboundPlugin;
    @InjectMocks LispSouthboundRPC lispSouthboundRPC;

    private static final RpcResult<Void> RPC_RESULT_FAILURE = RpcResultBuilder.<Void>failed().build();
    private static final RpcResult<Void> RPC_RESULT_SUCCESS = RpcResultBuilder.<Void>success().build();
    private static final RpcResult<GetStatsOutput> RPC_RESULT_ERROR = RpcResultBuilder.<GetStatsOutput>failed()
            .withError(RpcError.ErrorType.APPLICATION, "data-missing", "No stats found").build();

    /**
     * Tests {@link LispSouthboundRPC#sendMapNotify} method.
     */
    @Test
    public void sendMapNotifyTest_inputNotNull() throws ExecutionException, InterruptedException {

        final MapNotify mapNotify = getDefaultMapNotifyBuilder().build();
        final TransportAddress transportAddress = new TransportAddressBuilder().build();
        final SendMapNotifyInput sendMapNotifyInputMock = Mockito.mock(SendMapNotifyInput.class);

        Mockito.when(sendMapNotifyInputMock.getTransportAddress()).thenReturn(transportAddress);
        Mockito.when(sendMapNotifyInputMock.getMapNotify()).thenReturn(mapNotify);
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(),
                lispSouthboundRPC.sendMapNotify(sendMapNotifyInputMock).get().isSuccessful());

        Mockito.verify(lispSouthboundPlugin).handleSerializedLispBuffer(transportAddress,
                MapNotifySerializer.getInstance().serialize(mapNotify), MessageType.MapNotify);
    }

    /**
     * Tests {@link LispSouthboundRPC#sendMapNotify} method with null input.
     */
    @Test
    public void sendMapNotifyTest_nullInput() throws ExecutionException, InterruptedException {
        assertEquals(RPC_RESULT_FAILURE.isSuccessful(),
                lispSouthboundRPC.sendMapNotify(null).get().isSuccessful());

        Mockito.verifyZeroInteractions(lispSouthboundPlugin);
    }

    /**
     * Tests {@link LispSouthboundRPC#sendMapReply} method.
     */
    @Test
    public void sendMapReplyTest_inputNotNull() throws ExecutionException, InterruptedException {

        final MapReply mapReply = getDefaultMapReplyBuilder().build();
        final TransportAddress transportAddress = new TransportAddressBuilder().build();
        final SendMapReplyInput sendMapReplyInputMock = Mockito.mock(SendMapReplyInput.class);

        Mockito.when(sendMapReplyInputMock.getTransportAddress()).thenReturn(transportAddress);
        Mockito.when(sendMapReplyInputMock.getMapReply()).thenReturn(mapReply);
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(),
                lispSouthboundRPC.sendMapReply(sendMapReplyInputMock).get().isSuccessful());

        Mockito.verify(lispSouthboundPlugin).handleSerializedLispBuffer(transportAddress,
                MapReplySerializer.getInstance().serialize(mapReply), MessageType.MapReply);
    }

    /**
     * Tests {@link LispSouthboundRPC#sendMapReply} method with null input.
     */
    @Test
    public void sendMapReplyTest_nullInput() throws ExecutionException, InterruptedException {
        assertEquals(RPC_RESULT_FAILURE.isSuccessful(),
                lispSouthboundRPC.sendMapReply(null).get().isSuccessful());

        Mockito.verifyZeroInteractions(lispSouthboundPlugin);
    }

    /**
     * Tests {@link LispSouthboundRPC#sendMapRequest} method.
     */
    @Test
    public void sendMapRequestTest_inputNotNull() throws ExecutionException, InterruptedException {

        final MapRequest mapRequest = new MapRequestBuilder().build();
        final TransportAddress transportAddress = new TransportAddressBuilder().build();
        final SendMapRequestInput sendMapRequestInputMock = Mockito.mock(SendMapRequestInput.class);

        Mockito.when(sendMapRequestInputMock.getTransportAddress()).thenReturn(transportAddress);
        Mockito.when(sendMapRequestInputMock.getMapRequest()).thenReturn(mapRequest);
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(),
                lispSouthboundRPC.sendMapRequest(sendMapRequestInputMock).get().isSuccessful());

        Mockito.verify(lispSouthboundPlugin).handleSerializedLispBuffer(transportAddress,
                MapRequestSerializer.getInstance().serialize(mapRequest), MessageType.MapRequest);
    }

    /**
     * Tests {@link LispSouthboundRPC#sendMapRequest} method with null input.
     */
    @Test
    public void sendMapRequestTest_nullInput() throws ExecutionException, InterruptedException {
        assertEquals(RPC_RESULT_FAILURE.isSuccessful(),
                lispSouthboundRPC.sendMapRequest(null).get().isSuccessful());

        Mockito.verifyZeroInteractions(lispSouthboundPlugin);
    }

    /**
     * Tests {@link LispSouthboundRPC#sendMapRegister} method.
     */
    @Test
    public void sendMapRegisterTest_inputNotNull() throws ExecutionException, InterruptedException {

        final MapRegister mapRegister = getDefaultMapRegisterBuilder().build();
        final TransportAddress transportAddress = new TransportAddressBuilder().build();
        final SendMapRegisterInput sendMapRegisterInputMock = Mockito.mock(SendMapRegisterInput.class);

        Mockito.when(sendMapRegisterInputMock.getTransportAddress()).thenReturn(transportAddress);
        Mockito.when(sendMapRegisterInputMock.getMapRegister()).thenReturn(mapRegister);
        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(),
                lispSouthboundRPC.sendMapRegister(sendMapRegisterInputMock).get().isSuccessful());

        Mockito.verify(lispSouthboundPlugin).handleSerializedLispBuffer(transportAddress,
                MapRegisterSerializer.getInstance().serialize(mapRegister), MessageType.MapRegister);
    }

    /**
     * Tests {@link LispSouthboundRPC#sendMapRegister} method with null input.
     */
    @Test
    public void sendMapRegisterTest_nullInput() throws ExecutionException, InterruptedException {
        assertEquals(RPC_RESULT_FAILURE.isSuccessful(),
                lispSouthboundRPC.sendMapRegister(null).get().isSuccessful());

        Mockito.verifyZeroInteractions(lispSouthboundPlugin);
    }

    /**
     * Tests {@link LispSouthboundRPC#getStats} method.
     */
    @Test
    public void getStatsTest() throws ExecutionException, InterruptedException {
        final ConcurrentLispSouthboundStats stats = new ConcurrentLispSouthboundStats();
        incrementAll(stats);
        Mockito.when(lispSouthboundPlugin.getStats()).thenReturn(stats);

        // result
        final ControlMessageStats resultStats = lispSouthboundRPC.getStats().get().getResult().getControlMessageStats();

        assertEquals(stats.getRx()[0], (long) resultStats.getControlMessage().get(0).getRxCount());
        assertEquals(stats.getRx()[1], (long) resultStats.getControlMessage().get(1).getRxCount());
        assertEquals(stats.getRx()[2], (long) resultStats.getControlMessage().get(2).getRxCount());
        assertEquals(stats.getRx()[3], (long) resultStats.getControlMessage().get(3).getRxCount());
        assertEquals(stats.getRx()[4], (long) resultStats.getControlMessage().get(4).getRxCount());
        assertEquals(stats.getRx()[6], (long) resultStats.getControlMessage().get(5).getRxCount());
        assertEquals(stats.getRx()[7], (long) resultStats.getControlMessage().get(6).getRxCount());
        assertEquals(stats.getRx()[8], (long) resultStats.getControlMessage().get(7).getRxCount());
    }

    /**
     * Tests {@link LispSouthboundRPC#getStats} method with null stats.
     */
    @Test
    public void getStatsTest_withNullStats() throws ExecutionException, InterruptedException {
        final String expectedMsg = ((RpcError) RPC_RESULT_ERROR.getErrors().iterator().next()).getMessage();

        Mockito.when(lispSouthboundPlugin.getStats()).thenReturn(null);

        final Future<RpcResult<GetStatsOutput>> resultFuture = lispSouthboundRPC.getStats();
        final RpcResult<GetStatsOutput> rpcResult = resultFuture.get();

        assertEquals(RPC_RESULT_ERROR.isSuccessful(), rpcResult.isSuccessful());
        assertEquals(expectedMsg, rpcResult.getErrors().iterator().next().getMessage());
    }

    /**
     * Tests {@link LispSouthboundRPC#resetStats} method.
     */
    @Test
    public void resetStatsTest() throws ExecutionException, InterruptedException {
        final ConcurrentLispSouthboundStats stats = new ConcurrentLispSouthboundStats();
        incrementAll(stats);
        Mockito.when(lispSouthboundPlugin.getStats()).thenReturn(stats);

        assertEquals(RPC_RESULT_SUCCESS.isSuccessful(), lispSouthboundRPC.resetStats().get().isSuccessful());

        for (long rx : stats.getRx()) {
            assertEquals(0, rx);
        }
    }

    /**
     * Tests {@link LispSouthboundRPC#resetStats} method with null stats.
     */
    @Test
    public void resetStatsTest_withNullStats() throws ExecutionException, InterruptedException {
        final String expectedMsg = ((RpcError) RPC_RESULT_ERROR.getErrors().iterator().next()).getMessage();

        Mockito.when(lispSouthboundPlugin.getStats()).thenReturn(null);

        final Future<RpcResult<Void>> resultFuture = lispSouthboundRPC.resetStats();
        final RpcResult<Void> rpcResult = resultFuture.get();

        assertEquals(RPC_RESULT_ERROR.isSuccessful(), rpcResult.isSuccessful());
        assertEquals(expectedMsg, rpcResult.getErrors().iterator().next().getMessage());
    }

    private static MappingRecordItem getDefaultMappingRecordItem() {
        return new MappingRecordItemBuilder()
                .setMappingRecord(new MappingRecordBuilder().build()).build();
    }

    private static MapNotifyBuilder getDefaultMapNotifyBuilder() {
        return new MapNotifyBuilder()
                .setMappingRecordItem(Lists.newArrayList(getDefaultMappingRecordItem()));
    }

    private static MapReplyBuilder getDefaultMapReplyBuilder() {
        return new MapReplyBuilder()
                .setMappingRecordItem(Lists.newArrayList(getDefaultMappingRecordItem()));
    }

    private static MapRegisterBuilder getDefaultMapRegisterBuilder() {
        return new MapRegisterBuilder()
                .setMappingRecordItem(Lists.newArrayList(getDefaultMappingRecordItem()));
    }

    private static void incrementAll(ConcurrentLispSouthboundStats stats) {
        for (MessageType type : MessageType.values()) {
            stats.incrementRx(type.getIntValue());
        }
    }
}
