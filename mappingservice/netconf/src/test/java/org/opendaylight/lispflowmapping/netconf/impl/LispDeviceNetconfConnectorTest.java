/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.netconf.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executors;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.BuildConnectorInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.BuildConnectorInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.RemoveConnectorInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.RemoveConnectorInputBuilder;

public class LispDeviceNetconfConnectorTest {


    @Test
    public void testRemoveConnector() throws Exception {
        LispNetconfConnector mockedConnector = getLispNetconfConnector();
        LispDeviceNetconfConnector testedLispDeviceNetconfConnector = new LispDeviceNetconfConnector(Executors.newFixedThreadPool(1), mockedConnector);

        assertEquals(testedLispDeviceNetconfConnector.removeConnector(removeInput("n1")).get().isSuccessful(), true);
        verify(mockedConnector).removeNetconfConnector(any(String.class));

        testedLispDeviceNetconfConnector.close();
    }

    @Test
    public void testRemoveConnectorFail() throws Exception {
        LispNetconfConnector mockedConnector = getLispNetconfConnector();
        LispDeviceNetconfConnector testedLispDeviceNetconfConnector = new LispDeviceNetconfConnector(Executors.newFixedThreadPool(1), mockedConnector);
        assertEquals(testedLispDeviceNetconfConnector.removeConnector(removeInput(null)).get().isSuccessful(), false);
        testedLispDeviceNetconfConnector.close();
    }

    @Test
    public void testMakeConnector() throws Exception {
        LispNetconfConnector mockedConnector = getLispNetconfConnector();
        LispDeviceNetconfConnector testedLispDeviceNetconfConnector = new LispDeviceNetconfConnector(Executors.newFixedThreadPool(1), mockedConnector);
        assertEquals(testedLispDeviceNetconfConnector.buildConnector(buildConnectorInput("n1", "1.1.1.1", 830, "user", "pass")).get().isSuccessful(), true);

        verify(mockedConnector).createNetconfConnector(any(String.class), any(Host.class), any(Integer.class), any(String.class), any(String.class));
        testedLispDeviceNetconfConnector.close();
    }

    @Test
    public void testMakeConnectorFail() throws Exception{
        LispNetconfConnector mockedConnector = getLispNetconfConnector();
        LispDeviceNetconfConnector testedLispDeviceNetconfConnector = new LispDeviceNetconfConnector(Executors.newFixedThreadPool(1), mockedConnector);

        assertEquals(testedLispDeviceNetconfConnector.buildConnector(buildConnectorInput(null, "1.1.1.1", 830, "user", "pass")).get().isSuccessful(), false);
        assertEquals(testedLispDeviceNetconfConnector.buildConnector(buildConnectorInput("n1", null, 830, "user", "pass")).get().isSuccessful(), false);
        assertEquals(testedLispDeviceNetconfConnector.buildConnector(buildConnectorInput("n1", "1.1.1.1", null, "user", "pass")).get().isSuccessful(), false);
        assertEquals(testedLispDeviceNetconfConnector.buildConnector(buildConnectorInput("n1", "1.1.1.1", 830, null, "pass")).get().isSuccessful(), false);
        assertEquals(testedLispDeviceNetconfConnector.buildConnector(buildConnectorInput("n1", "1.1.1.1", 830, "user", null)).get().isSuccessful(), false);

        testedLispDeviceNetconfConnector.close();
    }

    private BuildConnectorInput buildConnectorInput(String instance, String address, Integer port, String user, String pass) {
        BuildConnectorInputBuilder input = new BuildConnectorInputBuilder();
        input.setInstance(instance);
        input.setAddress(address == null ? null : new Host(new IpAddress(new Ipv4Address(address))));
        input.setPort(port == null ? null : new PortNumber(port));
        input.setUsername(user);
        input.setPassword(pass);
        return input.build();
    }

    private RemoveConnectorInput removeInput(String instance) {
        RemoveConnectorInputBuilder input = new RemoveConnectorInputBuilder();
        input.setInstance(instance);
        return input.build();
    }

    private static LispNetconfConnector getLispNetconfConnector() throws Exception{
        LispNetconfConnector conn =  mock(LispNetconfConnector.class);
        doNothing().when(conn).createNetconfConnector(any(String.class), any(Host.class), any(Integer.class), any(String.class), any(String.class));
        doNothing().when(conn).removeNetconfConnector(any(String.class));
        return(conn);
    }
}
