/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;

public class LispNeutronServiceTest {
    private static BindingAwareBroker.ProviderContext session = Mockito.mock(BindingAwareBroker.ProviderContext.class);
    private static RpcProviderRegistry rpcRegistry = Mockito.mock(RpcProviderRegistry.class);
    private static DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private IFlowMapping mappingService = Mockito.mock(IFlowMapping.class);
    private BindingAwareBroker bindingAwareBroker = Mockito.mock(BindingAwareBroker.class);

    private LispNeutronService lispNeutronService = new LispNeutronService(mappingService, bindingAwareBroker);

    /**
     * Tests {@link LispNeutronService#onSessionInitiated} method.
     */
    @Test
    public void onSessionInitiatedTest() {
        Mockito.when(session.getSALService(RpcProviderRegistry.class)).thenReturn(rpcRegistry);
        Mockito.when(session.getSALService(DataBroker.class)).thenReturn(dataBroker);

        lispNeutronService.onSessionInitiated(session);
        Mockito.verify(rpcRegistry).getRpcService(OdlMappingserviceService.class);
        Mockito.verify(session).getSALService(DataBroker.class);
    }

    /**
     * Tests {@link LispNeutronService#close} method.
     */
    @Test
    public void closeTest() {
        lispNeutronService.close();
        assertEquals(null, lispNeutronService.getMappingService());
    }
}
