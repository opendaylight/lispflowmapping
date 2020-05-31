/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.HostInformationManager;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.binding.rev150712.PortBindingExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class PortDataProcessorTest {

    private static ILispNeutronService iLispNeutronServiceMock = Mockito.mock(ILispNeutronService.class);
    private static HostInformationManager hostInformationManager = Mockito.mock(HostInformationManager.class);
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
    private static final String UUID_STRING = "123e4567-e89b-12d3-a456-426655440000";

    private PortDataProcessor portDataProcessor = new PortDataProcessor(iLispNeutronServiceMock);

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        portMock = Mockito.mock(Port.class);
        odlMappingserviceServiceMock = Mockito.mock(OdlMappingserviceService.class);
        augmentationMock = Mockito.mock(PortBindingExtension.class);
        future = Mockito.mock(Future.class);
        rpcResult = Mockito.mock(RpcResult.class);

        try {
            Field field = portDataProcessor.getClass().getDeclaredField("hostInformationManager");
            field.setAccessible(true);
            field.set(portDataProcessor, hostInformationManager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to set hostInformationManager", e);
        }

        // common stubbing is called before invocation of each test
        commonStubbing();
    }

    /**
     * Tests {@link PortDataProcessor#create} method.
     */
    @Test
    public void createTest()
            throws ExecutionException, InterruptedException, NoSuchMethodException,
                   IllegalAccessException, InvocationTargetException {
        portDataProcessor.create(portMock);

        Map<FixedIpsKey, FixedIps> fixedIps = portMock.nonnullFixedIps();

        for (FixedIps ip : fixedIps.values()) {
            Mockito.verify(hostInformationManager).addHostRelatedInfo(HOST_ID_1,
                    getEid(portDataProcessor, portMock, ip));
        }
    }

    /**
     * Tests {@link PortDataProcessor#create} method with null host Id.
     */
    @Test
    public void createTest_nullHostId() throws ExecutionException, InterruptedException {
        // overriding default stubbing
        Mockito.when(augmentationMock.getHostId()).thenReturn(null);

        portDataProcessor.create(portMock);

        Map<FixedIpsKey, FixedIps> fixedIps = portMock.getFixedIps();

        Mockito.verifyZeroInteractions(hostInformationManager);
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
        Mockito.when(portMock.augmentation(PortBindingExtension.class)).thenReturn(augmentationMock);
        Mockito.when(iLispNeutronServiceMock.getMappingDbService()).thenReturn(odlMappingserviceServiceMock);
        Mockito.when(portMock.getUuid()).thenReturn(new Uuid(PORT_UUID_1));
        Mockito.when(portMock.getFixedIps()).thenReturn(getDefaultListOfFixedIps());
        Mockito.when(portMock.getTenantId()).thenReturn(new Uuid(TENANT_1));
        Mockito.when(augmentationMock.getHostId()).thenReturn(HOST_ID_1);
        Mockito.when(hostInformationManager.getInstanceId(TENANT_1)).thenReturn(1L);
    }

    private static LocatorRecord getDefaultLocatorRecord() {
        return new LocatorRecordBuilder()
                .setRloc(new RlocBuilder().setAddress(ADDRESS).build())
                .build();
    }

    private static MappingRecord getDefaultMappingRecord() {
        return new MappingRecordBuilder().setLocatorRecord(Lists.newArrayList(getDefaultLocatorRecord())).build();
    }

    private static Map<FixedIpsKey, FixedIps> getDefaultListOfFixedIps() {
        IpAddress ipv4 = new IpAddress(new Ipv4Address(IPV4));
        FixedIps fixedIps = new FixedIpsBuilder().setIpAddress(ipv4).build();

        return new LinkedHashMap<>(Map.of(new FixedIpsKey(ipv4, new Uuid(UUID_STRING)), fixedIps));
    }

    private static Eid getEid(PortDataProcessor portDataProcessor, Port port, FixedIps ip)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            Method method = (portDataProcessor.getClass())
                                .getDeclaredMethod("getEid", Port.class, FixedIps.class);
            method.setAccessible(true);
            return (Eid) method.invoke(portDataProcessor, port, ip);
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e;
        }
    }
}
