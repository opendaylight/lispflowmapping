/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import com.google.common.collect.Lists;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.opendaylight.lispflowmapping.neutron.dataprocessors.PortDataProcessor;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.OpenstackNodeInformationManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.binding.rev150712.PortBindingExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class PortDataProcessorTest {

    private static ILispNeutronService iLispNeutronServiceMock = Mockito.mock(ILispNeutronService.class);
    private static OpenstackNodeInformationManager
            openstackNodeInformationManager = Mockito.mock(OpenstackNodeInformationManager.class);
    private OdlMappingserviceService odlMappingserviceServiceMock;
    private Port portMock;
    private PortBindingExtension augmentationMock;
    private Future<RpcResult<GetMappingOutput>> future;
    private RpcResult<GetMappingOutput> rpcResult;

    private static final String PORT_UUID_1 = "a9607d99-0afa-41cc-8f2f-8d62b6e9bc1c";
    private static final String HOST_ID_1 = "host-id1";
    private static final String TENANT_1 = "92070479-5900-498b-84e3-893a04f55709";
    private static final String IPV4 = "192.168.0.1";
    private static final Address ADDRESS = new DistinguishedNameBuilder()
            .setDistinguishedName(new DistinguishedNameType(HOST_ID_1)).build();

    private PortDataProcessor portDataProcessor;

    @Before
    public void init() {
        portDataProcessor = new PortDataProcessor(iLispNeutronServiceMock);
        portMock = Mockito.mock(Port.class);
        odlMappingserviceServiceMock = Mockito.mock(OdlMappingserviceService.class);
        augmentationMock = Mockito.mock(PortBindingExtension.class);
        future = Mockito.mock(Future.class);
        rpcResult = Mockito.mock(RpcResult.class);
        Whitebox.setInternalState(portDataProcessor,
                "openstackNodeInformationManager", openstackNodeInformationManager);
        commonStubbing(); // common stubbing is called before invocation of each test
    }

    /**
     * Tests {@link PortDataProcessor#create} method.
     */
    @Test
    public void createTest()
            throws ExecutionException, InterruptedException, NoSuchMethodException,
                   IllegalAccessException, InvocationTargetException {
        portDataProcessor.create(portMock);
        String hostId = portMock.getAugmentation(PortBindingExtension.class).getHostId();
        List<FixedIps> fixedIps = portMock.getFixedIps();

        for (FixedIps ip : fixedIps) {
            Eid eid = getEid(portDataProcessor, portMock, ip);
            Mockito.verify(openstackNodeInformationManager).addEidInHost(hostId, eid);
        }
    }

    /**
     * Tests {@link PortDataProcessor#create} method with null host Id.
     */
    @Test
    public void createTest_nullHostId() throws ExecutionException, InterruptedException {
        Mockito.when(augmentationMock.getHostId()).thenReturn(null); // overriding default stubbing

        portDataProcessor.create(portMock);
        Mockito.verifyZeroInteractions(openstackNodeInformationManager);
    }

    /**
     * Tests {@link PortDataProcessor#delete} method.
     */
    @Test
    public void deleteTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // expected result
        portDataProcessor.delete(portMock);
        String hostId = portMock.getAugmentation(PortBindingExtension.class).getHostId();

        List<FixedIps> fixedIps = portMock.getFixedIps();
        for (FixedIps ip: fixedIps) {
            Eid eid = getEid(portDataProcessor, portMock, ip);
            Mockito.verify(openstackNodeInformationManager).attemptToDeleteExistingMappingRecord(hostId, eid);
        }
    }

    /**
     * Stubbing common for all test methods.
     */
    public void commonStubbing() {
        Mockito.when(portMock.getAugmentation(PortBindingExtension.class)).thenReturn(augmentationMock);
        Mockito.when(iLispNeutronServiceMock.getMappingDbService()).thenReturn(odlMappingserviceServiceMock);
        Mockito.when(portMock.getUuid()).thenReturn(new Uuid(PORT_UUID_1));
        Mockito.when(portMock.getFixedIps()).thenReturn(getDefaultListOfFixedIps());
        Mockito.when(portMock.getTenantId()).thenReturn(new Uuid(TENANT_1));
        Mockito.when(augmentationMock.getHostId()).thenReturn(HOST_ID_1);
        Mockito.when(openstackNodeInformationManager.getInstanceId(TENANT_1)).thenAnswer(invocationOnMock -> 1L);
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

    private static Eid getEid(PortDataProcessor portDataProcessor, Port port, FixedIps ip)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            Method method = (portDataProcessor.getClass())
                                .getDeclaredMethod("getEid", Port.class, FixedIps.class);
            method.setAccessible(true);
            return (Eid) method.invoke(portDataProcessor, port, ip);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw e;
        }
    }
}
