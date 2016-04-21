/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.RequestMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.
        MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.
        MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.OdlLispSbService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapNotifyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapNotifyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRequestInputBuilder;

@RunWith(MockitoJUnitRunner.class)
public class LispMappingServiceTest {
    @Mock(name = "lispSB") private static OdlLispSbService lispSBMock;
    @Mock(name = "mapResolver") private static IMapResolverAsync mapResolverMock;
    @Mock(name = "mapServer") private static IMapServerAsync mapServerMock;
    @Mock(name = "tlsMapReply") private static ThreadLocal<MapReply> tlsMapReplyMock;
    @Mock(name = "tlsMapRequest") private static ThreadLocal<Pair<MapRequest, TransportAddress>> tlsMapRequestMock;
    @Mock(name = "tlsMapNotify") private static ThreadLocal<Pair<MapNotify, List<TransportAddress>>> tlsMapNotifyMock;
    @Mock private static MapRegister mapRegisterMock;
    @InjectMocks private static LispMappingService lispMappingService;

    private static final String IPV4_STRING_1 =      "1.2.3.0";
    private static final String IPV4_STRING_2 =      "1.2.4.0";
    private static final String IPV4_SOURCE_STRING = "192.168.0.1";
    private static final String IPV4_PREFIX_STRING = "/24";

    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final Eid IPV4_SOURCE_EID = LispAddressUtil.asIpv4Eid(IPV4_SOURCE_STRING);
    private static final EidItemBuilder EID_ITEM_BUILDER = new EidItemBuilder()
            .setEidItemId("eid-item-id")
            .setEid(IPV4_PREFIX_EID_1);
    private static final MappingRecordItemBuilder MAPPING_RECORD_ITEM_BUILDER = new MappingRecordItemBuilder()
            .setMappingRecord(new MappingRecordBuilder().setEid(IPV4_PREFIX_EID_1).build());

    private static final TransportAddress TRANSPORT_ADDRESS_1 = new TransportAddressBuilder()
            .setIpAddress(new IpAddress(Ipv4Address.getDefaultInstance(IPV4_STRING_1)))
            .setPort(new PortNumber(9999)).build();
    private static final TransportAddress TRANSPORT_ADDRESS_2 = new TransportAddressBuilder()
            .setIpAddress(new IpAddress(Ipv4Address.getDefaultInstance(IPV4_STRING_2)))
            .setPort(new PortNumber(8888)).build();
    private static final TransportAddress TRANSPORT_ADDRESS = new TransportAddressBuilder()
            .setIpAddress(new IpAddress(Ipv4Address.getDefaultInstance(IPV4_STRING_1)))
            .setPort(new PortNumber(LispMessage.PORT_NUM)).build();

    /**
     * Tests {@link LispMappingService#handleMapRequest} method.
     */
    @Test
    public void handleMapRequestTest() {
        final MapRequest mapRequest = Mockito.mock(MapRequest.class);
        final MapReply mapReply = new MapReplyBuilder().build();
        Mockito.when(mapRequest.getEidItem()).thenReturn(Lists.newArrayList(EID_ITEM_BUILDER.build()));
        Mockito.when(tlsMapRequestMock.get()).thenReturn(null);
        Mockito.when(tlsMapReplyMock.get()).thenReturn(mapReply);

        final MapReply result = lispMappingService.handleMapRequest(mapRequest);

        Mockito.verify(tlsMapRequestMock).set(null);
        Mockito.verify(tlsMapRequestMock).get();
        Mockito.verifyNoMoreInteractions(tlsMapRequestMock);
        Mockito.verify(mapResolverMock).handleMapRequest(mapRequest);
        Mockito.verify(tlsMapReplyMock).set(Mockito.any(MapReply.class));
        Mockito.verify(tlsMapReplyMock).get();

        assertEquals(result, mapReply);
    }

    /**
     * Tests {@link LispMappingService#handleMapRequest} method request from non-proxy xTR.
     */
    @Test
    public void handleMapRequestTest_NonProxy() {
        final MapRequest mapRequest = Mockito.mock(MapRequest.class);
        final Pair<MapRequest, TransportAddress> pair = getDefaultMapRequestPair();
        final SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder()
                .setMapRequest(pair.getLeft())
                .setTransportAddress(pair.getRight());

        Mockito.when(mapRequest.getEidItem()).thenReturn(Lists.newArrayList(EID_ITEM_BUILDER.build()));
        Mockito.when(tlsMapRequestMock.get()).thenReturn(pair);

        assertNull(lispMappingService.handleMapRequest(mapRequest));
        Mockito.verify(lispSBMock).sendMapRequest(smrib.build());
    }

    /**
     * Tests {@link LispMappingService#handleMapRegister} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void handleMapRegisterTest() {
        final Pair<MapNotify, List<TransportAddress>> pairMock = Mockito.mock(Pair.class);

        Mockito.when(mapRegisterMock.getMappingRecordItem())
                .thenReturn(Lists.newArrayList(MAPPING_RECORD_ITEM_BUILDER.build()));
        Mockito.when(tlsMapNotifyMock.get()).thenReturn(pairMock);

        lispMappingService.handleMapRegister(mapRegisterMock);
        Mockito.verify(mapServerMock).handleMapRegister(mapRegisterMock);
        Mockito.verify(tlsMapNotifyMock).set(Mockito.any(MutablePair.class));
    }

    /**
     * Tests {@link LispMappingService#onAddMapping} method.
     */
    @Test
    public void onAddMappingTest() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegister
                mapRegister = Mockito.mock(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105
                .mapregisternotification.MapRegister.class);
        final AddMapping addMapping = Mockito.mock(AddMapping.class);

        Mockito.when(addMapping.getMapRegister()).thenReturn(mapRegister);
        Mockito.when(mapRegister.getMappingRecordItem())
                .thenReturn(Lists.newArrayList(MAPPING_RECORD_ITEM_BUILDER.build()));
        Mockito.when(tlsMapNotifyMock.get()).thenReturn(getDefaultMapNotifyPair());

        lispMappingService.onAddMapping(addMapping);
        Mockito.verify(lispSBMock, Mockito.times(2)).sendMapNotify(Mockito.argThat(new TransportAddressMatch()));
    }

    /**
     * Tests {@link LispMappingService#onAddMapping} method with no TransportAddress.
     */
    @Test
    public void onAddMappingTest_noTransportAddress() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapregisternotification.MapRegister
                mapRegister = Mockito.mock(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105
                .mapregisternotification.MapRegister.class);
        final AddMapping addMapping = Mockito.mock(AddMapping.class);
        final MapNotify mapNotify = new MapNotifyBuilder().setKeyId((short) 1).build();

        Mockito.when(addMapping.getMapRegister()).thenReturn(mapRegister);
        Mockito.when(mapRegister.getMappingRecordItem())
                .thenReturn(Lists.newArrayList(MAPPING_RECORD_ITEM_BUILDER.build()));
        Mockito.when(tlsMapNotifyMock.get()).thenReturn(new MutablePair<>(mapNotify, null));
        Mockito.when(addMapping.getTransportAddress()).thenReturn(TRANSPORT_ADDRESS_1);

        // result
        final SendMapNotifyInputBuilder smnib = new SendMapNotifyInputBuilder()
                .setMapNotify(new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105
                        .mapnotifymessage.MapNotifyBuilder().setKeyId((short) 1).build())
                .setTransportAddress(TRANSPORT_ADDRESS);

        lispMappingService.onAddMapping(addMapping);
        Mockito.verify(lispSBMock).sendMapNotify(smnib.build());
    }

    /**
     * Tests {@link LispMappingService#onRequestMapping} method.
     */
    @Test
    public void onRequestMappingTest() {
        final RequestMapping requestMapping = Mockito.mock(RequestMapping.class);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequest
                mapRequest = Mockito.mock(org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105
                    .maprequestnotification.MapRequest.class);
        final MapReply mapReply = new MapReplyBuilder().build();

        Mockito.when(requestMapping.getMapRequest()).thenReturn(mapRequest);
        Mockito.when(requestMapping.getTransportAddress()).thenReturn(TRANSPORT_ADDRESS_1);
        Mockito.when(mapRequest.getEidItem()).thenReturn(Lists.newArrayList(EID_ITEM_BUILDER.build()));
        Mockito.when(tlsMapReplyMock.get()).thenReturn(mapReply);

        // result
        final SendMapReplyInputBuilder smrib = new SendMapReplyInputBuilder()
                .setMapReply(new MapReplyBuilder(mapReply).build())
                .setTransportAddress(TRANSPORT_ADDRESS_1);

        lispMappingService.onRequestMapping(requestMapping);
        Mockito.verify(lispSBMock).sendMapReply(smrib.build());
    }

    /**
     * Tests {@link LispMappingService#handleSMR} method.
     */
    @Test
    public void handleSmrTest() {
        final MapRequest mapRequest = Mockito.mock(MapRequest.class);
        final Rloc subscriber = LispAddressUtil.asIpv4Rloc(IPV4_STRING_1);

        Mockito.when(mapRequest.getSourceEid()).thenReturn(new SourceEidBuilder().setEid(IPV4_SOURCE_EID).build());
        Mockito.when(mapRequest.getEidItem()).thenReturn(Lists.newArrayList(EID_ITEM_BUILDER.build()));

        // result
        final SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder()
                .setMapRequest(new MapRequestBuilder(mapRequest).build())
                .setTransportAddress(TRANSPORT_ADDRESS);

        lispMappingService.handleSMR(mapRequest, subscriber);
        Mockito.verify(lispSBMock).sendMapRequest(smrib.build());
    }

    /**
     * Tests {@link LispMappingService#setShouldUseSmr} method.
     */
    @Test
    public void setShouldUseSmrTest() {
        final boolean value = true;

        lispMappingService.setShouldUseSmr(value);
        Mockito.verify(mapServerMock).setSubscriptionService(value);
        Mockito.verify(mapResolverMock).setSubscriptionService(value);
    }

    /**
     * Tests {@link LispMappingService#handleMapNotify} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void handleMapNotifyTest() {
        final MapNotify mapNotify = Mockito.mock(MapNotify.class);
        final List<TransportAddress> list = Mockito.mock(ArrayList.class);

        lispMappingService.handleMapNotify(mapNotify, list);
        Mockito.verify(tlsMapNotifyMock).set(Mockito.any(MutablePair.class));
    }

    /**
     * Tests {@link LispMappingService#handleNonProxyMapRequest} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void handleNonProxyMapRequestTest() {
        final MapRequest mapRequest = Mockito.mock(MapRequest.class);
        final TransportAddress transportAddress = Mockito.mock(TransportAddress.class);

        lispMappingService.handleNonProxyMapRequest(mapRequest, transportAddress);
        Mockito.verify(tlsMapRequestMock).set(Mockito.any(MutablePair.class));
    }

    /**
     * Tests {@link LispMappingService#handleMapReply} method.
     */
    @Test
    public void handleMapReplyTest() {
        final MapReply mapReply = Mockito.mock(MapReply.class);

        lispMappingService.handleMapReply(mapReply);
        Mockito.verify(tlsMapReplyMock).set(Mockito.any(MapReply.class));
    }

    /**
     * Tests {@link LispMappingService#setShouldAuthenticate} method.
     */
    @Test
    public void setShouldAuthenticateTest() {
        final boolean value = true;

        lispMappingService.setShouldAuthenticate(value);
        Mockito.verify(mapResolverMock).setShouldAuthenticate(value);
        Mockito.verify(mapServerMock).setShouldAuthenticate(value);
    }

    private static Pair<MapNotify, List<TransportAddress>> getDefaultMapNotifyPair() {
        final MapNotify mapNotify = new MapNotifyBuilder().setKeyId((short) 1).build();
        return new MutablePair<>(mapNotify, Lists.newArrayList(TRANSPORT_ADDRESS_1, TRANSPORT_ADDRESS_2));
    }

    private static Pair<MapRequest, TransportAddress> getDefaultMapRequestPair() {
        final MapRequestBuilder mapRequestBuilder = new MapRequestBuilder()
                .setEidItem(Lists.newArrayList(EID_ITEM_BUILDER.build()));

        return new ImmutablePair<>(mapRequestBuilder.build(), TRANSPORT_ADDRESS_1);
    }

    class TransportAddressMatch extends ArgumentMatcher<SendMapNotifyInput> {
        public boolean matches(Object sendMapNotify) {
            final SendMapNotifyInput sendMapNotifyInput = (SendMapNotifyInput) sendMapNotify;
            final TransportAddress notifyTransportAddress = sendMapNotifyInput.getTransportAddress();
            return TRANSPORT_ADDRESS_1.equals(notifyTransportAddress)
                    || TRANSPORT_ADDRESS_2.equals(notifyTransportAddress);
        }

    }
}
