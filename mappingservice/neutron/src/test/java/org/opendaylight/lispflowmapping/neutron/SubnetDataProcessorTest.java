/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.dataprocessors.SubnetDataProcessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SubnetDataProcessorTest {

    private static ILispNeutronService iLispNeutronServiceMock = Mockito.mock(ILispNeutronService.class);
    private static Subnet subnet = Mockito.mock(Subnet.class);
    private OdlMappingserviceService odlMappingserviceServiceMock;
    private Future<RpcResult<Void>> future;
    private RpcResult<Void> rpcResult;

    private static SubnetDataProcessor subnetDataProcessor = new SubnetDataProcessor(iLispNeutronServiceMock);

    private static final String UUID_STRING = "123e4567-e89b-12d3-a456-426655440000";
    private static final String IPV4 = "192.168.0.1";
    private static final String MASK = "/32";
    private static final Eid EID = LispAddressUtil.asIpv4PrefixEid(IPV4 + MASK);
    private static final IpPrefix IP_PREFIX = new IpPrefix(new Ipv4Prefix(IPV4 + MASK));

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        odlMappingserviceServiceMock = Mockito.mock(OdlMappingserviceService.class);
        future = Mockito.mock(Future.class);
        rpcResult = Mockito.mock(RpcResult.class);
    }

    /**
     * Tests {@link SubnetDataProcessor#create} method.
     */
    @Test
    public void createTest() throws ExecutionException, InterruptedException {
        final MappingAuthkey mappingAuthkey = new MappingAuthkeyBuilder()
                .setKeyString(UUID_STRING).setKeyType(1).build();
        final AddKeyInput addKeyInput = new AddKeyInputBuilder()
                .setEid(EID).setMappingAuthkey(mappingAuthkey).build();

        commonStubbing();
        Mockito.when(odlMappingserviceServiceMock.addKey(addKeyInput)).thenReturn(future);

        subnetDataProcessor.create(subnet);
        assertEquals(true, rpcResult.isSuccessful());
    }

    /**
     * Tests {@link SubnetDataProcessor#create} method with null mapping service.
     */
    @Test
    public void createTest_withNullMappingService() {
        Mockito.when(subnet.getCidr()).thenReturn(IP_PREFIX);
        Mockito.when(iLispNeutronServiceMock.getMappingDbService()).thenReturn(null);

        subnetDataProcessor.create(subnet);
        Mockito.verifyZeroInteractions(odlMappingserviceServiceMock);
    }

    /**
    * Tests {@link SubnetDataProcessor#delete} method.
    */
    @Test
    public void deleteTest() throws ExecutionException, InterruptedException {
        final RemoveKeyInput removeKeyInput = new RemoveKeyInputBuilder().setEid(EID).build();

        commonStubbing();
        Mockito.when(odlMappingserviceServiceMock.removeKey(removeKeyInput)).thenReturn(future);

        subnetDataProcessor.delete(subnet);
        assertEquals(true, rpcResult.isSuccessful());
    }

    /**
     * Tests {@link SubnetDataProcessor#delete} method with null mapping service.
     */
    @Test
    public void deleteTest_withNullMappingService() {
        Mockito.when(subnet.getCidr()).thenReturn(IP_PREFIX);
        Mockito.when(iLispNeutronServiceMock.getMappingDbService()).thenReturn(null);

        subnetDataProcessor.delete(subnet);
        Mockito.verifyZeroInteractions(odlMappingserviceServiceMock);
    }

    public void commonStubbing() throws ExecutionException, InterruptedException {
        Mockito.when(subnet.getCidr()).thenReturn(IP_PREFIX);
        Mockito.when(iLispNeutronServiceMock.getMappingDbService()).thenReturn(odlMappingserviceServiceMock);
        Mockito.when(subnet.getUuid()).thenReturn(new Uuid(UUID_STRING));
        Mockito.when(future.get()).thenReturn(rpcResult);
        Mockito.when(rpcResult.isSuccessful()).thenReturn(true);
    }
}
