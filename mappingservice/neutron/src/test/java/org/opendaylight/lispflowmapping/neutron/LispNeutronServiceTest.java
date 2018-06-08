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
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;

public class LispNeutronServiceTest {
    private static OdlMappingserviceService odlMappingService = Mockito.mock(OdlMappingserviceService.class);
    private static DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private final IFlowMapping mappingService = Mockito.mock(IFlowMapping.class);

    private final LispNeutronService lispNeutronService =
            new LispNeutronService(mappingService, dataBroker, odlMappingService);

    /**
     * Tests {@link LispNeutronService#Init} method.
     */
    @Test
    public void onInit() {
        lispNeutronService.init();
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
