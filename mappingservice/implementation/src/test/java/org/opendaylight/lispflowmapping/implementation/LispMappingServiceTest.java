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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.list.EidItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestmessage.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.OdlLispSbService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.sb.rev150904.SendMapRequestInputBuilder;

@RunWith(MockitoJUnitRunner.class)
public class LispMappingServiceTest {

    @Mock private static OdlLispSbService lispSB;
    @Mock private static IMapResolverAsync mapResolver;
    //@Mock private static IMapServerAsync mapServer;
    //@Mock private static IMappingService mapService;
    //@Mock private static NotificationService notificationService;
    //@Mock private static BindingAwareBroker broker;
    //@Mock private static BindingAwareBroker.ProviderContext session;
    @Mock private static ThreadLocal<MapReply> tlsMapReply;
    @Mock private static ThreadLocal<Pair<MapRequest, TransportAddress>> tlsMapRequest;

    @InjectMocks private static LispMappingService lispMappingService;

    private static final String IPV4_STRING_1 = "1.2.3.0";
    private static final String IPV4_PREFIX_STRING = "/24";
    private static final Eid IPV4_PREFIX_EID_1 = LispAddressUtil.asIpv4PrefixEid(IPV4_STRING_1 + IPV4_PREFIX_STRING);
    private static final EidItemBuilder EID_ITEM_BUILDER = new EidItemBuilder()
            .setEidItemId("eid-item-id")
            .setEid(IPV4_PREFIX_EID_1);

    /**
     * Tests {@link LispMappingService#handleMapRequest} method.
     */
    @Test
    public void handleMapRequestTest() {
        final MapRequest mapRequest = Mockito.mock(MapRequest.class);
        final MapReply mapReply = new MapReplyBuilder().build();
        Mockito.when(mapRequest.getEidItem()).thenReturn(Lists.newArrayList(EID_ITEM_BUILDER.build()));
        Mockito.when(tlsMapRequest.get()).thenReturn(null);
        Mockito.when(tlsMapReply.get()).thenReturn(mapReply);

        MapReply result = lispMappingService.handleMapRequest(mapRequest);

        Mockito.verify(tlsMapRequest).set(null);
        Mockito.verify(tlsMapRequest).get();
        Mockito.verifyNoMoreInteractions(tlsMapRequest);

        Mockito.verify(mapResolver).handleMapRequest(mapRequest);
        Mockito.verify(tlsMapReply).set(Mockito.any(MapReply.class));
        Mockito.verify(tlsMapReply).get();

        assertEquals(result, mapReply);
    }

    /**
     * Tests {@link LispMappingService#handleMapRequest} method in non-proxy mode.
     */
    @Test
    public void handleMapRequestTest_NonProxy() {
        final MapRequest mapRequest = Mockito.mock(MapRequest.class);
        final Pair<MapRequest, TransportAddress> pair = getDefaultPair();
        SendMapRequestInputBuilder smrib = new SendMapRequestInputBuilder()
                .setMapRequest(pair.getLeft())
                .setTransportAddress(pair.getRight());

        Mockito.when(mapRequest.getEidItem()).thenReturn(Lists.newArrayList(EID_ITEM_BUILDER.build()));
        Mockito.when(tlsMapRequest.get()).thenReturn(pair);

        assertNull(lispMappingService.handleMapRequest(mapRequest));
        Mockito.verify(lispSB).sendMapRequest(smrib.build());
    }

    private static Pair<MapRequest, TransportAddress> getDefaultPair() {
        final MapRequestBuilder mapRequestBuilder = new MapRequestBuilder()
                .setEidItem(Lists.newArrayList(EID_ITEM_BUILDER.build()));
        final TransportAddress transportAddress = null;

        return new ImmutablePair<>(mapRequestBuilder.build(), transportAddress);
    }
}
