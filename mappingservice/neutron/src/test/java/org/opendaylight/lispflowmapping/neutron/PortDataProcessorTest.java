/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.binding.rev150712.PortBindingExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class PortDataProcessorTest {

    private static ILispNeutronService iLispNeutronServiceMock = Mockito.mock(ILispNeutronService.class);
    private OdlMappingserviceService odlMappingserviceServiceMock;
    private Port portMock;
    private PortBindingExtension augmentationMock;
    private Future<RpcResult<GetMappingOutput>> future;
    private RpcResult<GetMappingOutput> rpcResult;

    private static final String HOST_ID = "host-id";
    private static final String IPV4 = "192.168.0.1";
    private static final Address ADDRESS = new DistinguishedNameBuilder()
            .setDistinguishedName(new DistinguishedNameType(HOST_ID)).build();

    private PortDataProcessor portDataProcessor = new PortDataProcessor(iLispNeutronServiceMock);

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        portMock = Mockito.mock(Port.class);
        odlMappingserviceServiceMock = Mockito.mock(OdlMappingserviceService.class);
        augmentationMock = Mockito.mock(PortBindingExtension.class);
        future = Mockito.mock(Future.class);
        rpcResult = Mockito.mock(RpcResult.class);
        commonStubbing(); // common stubbing is called before invocation of each test
    }

    /**
     * Tests {@link PortDataProcessor#create} method.
     */
    @Test
    public void createTest() throws ExecutionException, InterruptedException {
        final GetMappingInput getMappingInput =
                new GetMappingInputBuilder().setEid(LispAddressUtil.asDistinguishedNameEid(HOST_ID)).build();
        final GetMappingOutput getMappingOutput = new GetMappingOutputBuilder()
                .setMappingRecord(getDefaultMappingRecord()).build();

        Mockito.when(odlMappingserviceServiceMock.getMapping(getMappingInput)).thenReturn(future);
        Mockito.when(future.get()).thenReturn(rpcResult);
        Mockito.when(rpcResult.getResult()).thenReturn(getMappingOutput);

        // expected result
        final AddMappingInput expectedResult = LispUtil
                .buildAddMappingInput(LispAddressUtil.asIpv4PrefixEid(IPV4 + "/32"),
                        Lists.newArrayList(getDefaultLocatorRecord()));
//        portDataProcessor.create(portMock);
        //Mockito.verify(odlMappingserviceServiceMock).addMapping(expectedResult);
    }

    /**
     * Tests {@link PortDataProcessor#create} method with null host Id.
     */
    @Test
    public void createTest_nullHostId() throws ExecutionException, InterruptedException {
        Mockito.when(augmentationMock.getHostId()).thenReturn(null); // overriding default stubbing

        portDataProcessor.create(portMock);
        Mockito.verifyNoMoreInteractions(odlMappingserviceServiceMock);
    }

    /**
     * Tests {@link PortDataProcessor#create} method with null mapping service.
     */
    @Test
    public void createTest_nullOdlMappingService() throws ExecutionException, InterruptedException {
        Mockito.when(iLispNeutronServiceMock.getMappingDbService()).thenReturn(null); // overriding default stubbing

//        portDataProcessor.create(portMock);
//        Mockito.verifyNoMoreInteractions(odlMappingserviceServiceMock);
    }

    /**
     * Tests {@link PortDataProcessor#delete} method.
     */
    @Test
    public void deleteTest() {
        // expected result
        final RemoveMappingInput expectedResult = LispUtil
                .buildRemoveMappingInput(LispAddressUtil.asIpv4PrefixEid(IPV4 + "/32"));
        portDataProcessor.delete(portMock);
        Mockito.verify(odlMappingserviceServiceMock).removeMapping(expectedResult);
    }

    /**
     * Stubbing common for all test methods.
     */
    public void commonStubbing() {
        Mockito.when(portMock.getAugmentation(PortBindingExtension.class)).thenReturn(augmentationMock);
        Mockito.when(iLispNeutronServiceMock.getMappingDbService()).thenReturn(odlMappingserviceServiceMock);
        Mockito.when(portMock.getFixedIps()).thenReturn(getDefaultListOfFixedIps());
        Mockito.when(augmentationMock.getHostId()).thenReturn(HOST_ID);
    }

    private static LocatorRecord getDefaultLocatorRecord() {
        return new LocatorRecordBuilder()
                .setRloc(new RlocBuilder().setAddress(ADDRESS).build())
                .build();
    }

    private static MappingRecord getDefaultMappingRecord() {
        return new MappingRecordBuilder().setLocatorRecord(Lists.newArrayList(getDefaultLocatorRecord())).build();
    }

    private static List<FixedIps> getDefaultListOfFixedIps() {
        FixedIps fixedIps = new FixedIpsBuilder().setIpAddress(new IpAddress(new Ipv4Address(IPV4))).build();

        return Lists.newArrayList(fixedIps);
    }
}
