/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.PortUuidToPortDataMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

/**
 * Created by Shakib Ahmed on 2/7/17.
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(PortUuidToPortDataMapper.class)
public class PortUuidToPortDataMapperTest {

    private static final Eid SAMPLE_EID = Mockito.mock(Eid.class);
    private static final PortData PORT_DATA_1 = new PortData("1", SAMPLE_EID);
    private static final PortData PORT_DATA_2 = new PortData("2", SAMPLE_EID);
    private static final PortData PORT_DATA_3 = new PortData("3", SAMPLE_EID);
    private static final PortData PORT_DATA_4 = new PortData("4", SAMPLE_EID);

    @Test
    public void unprocessedPortDataAddTest() {
        PortUuidToPortDataMapper portUuidToPortDataMapper = new PortUuidToPortDataMapper();

        portUuidToPortDataMapper.addUnprocessedUuidToPortDataMapping(PORT_DATA_1.getPortUuid(), PORT_DATA_1);
        portUuidToPortDataMapper.addUnprocessedUuidToPortDataMapping(PORT_DATA_2.getPortUuid(), PORT_DATA_2);

        processedAndUnprocessedDataAmountVerify(portUuidToPortDataMapper, 0, 2);
    }

    @Test
    public void addPortDataToProcessedTest() {
        PortUuidToPortDataMapper portUuidToPortDataMapper = new PortUuidToPortDataMapper();

        portUuidToPortDataMapper.addPortDataToProcessed(PORT_DATA_1);
        portUuidToPortDataMapper.addPortDataToProcessed(PORT_DATA_2);

        processedAndUnprocessedDataAmountVerify(portUuidToPortDataMapper, 2, 0);
    }

    @Test
    public void transferAllProcessedPortDataToUnprocessedTest() {
        PortUuidToPortDataMapper portUuidToPortDataMapper = new PortUuidToPortDataMapper();

        portUuidToPortDataMapper.addUnprocessedUuidToPortDataMapping(PORT_DATA_3.getPortUuid(), PORT_DATA_3);
        portUuidToPortDataMapper.addUnprocessedUuidToPortDataMapping(PORT_DATA_4.getPortUuid(), PORT_DATA_4);

        portUuidToPortDataMapper.addPortDataToProcessed(PORT_DATA_1);
        portUuidToPortDataMapper.addPortDataToProcessed(PORT_DATA_2);

        portUuidToPortDataMapper.transferAllProcessedPortDataToUnprocessed();

        processedAndUnprocessedDataAmountVerify(portUuidToPortDataMapper, 0, 4);

        HashMap<String, PortData> unprocessedPortData = extractUnprocessedDataMap(portUuidToPortDataMapper);

        Assert.assertEquals(PORT_DATA_1, unprocessedPortData.get(PORT_DATA_1.getPortUuid()));
        Assert.assertEquals(PORT_DATA_2, unprocessedPortData.get(PORT_DATA_2.getPortUuid()));
        Assert.assertEquals(PORT_DATA_3, unprocessedPortData.get(PORT_DATA_3.getPortUuid()));
        Assert.assertEquals(PORT_DATA_4, unprocessedPortData.get(PORT_DATA_4.getPortUuid()));
    }

    private void processedAndUnprocessedDataAmountVerify(PortUuidToPortDataMapper portUuidToPortDataMapper,
                                                         int processedDataCount,
                                                         int unprocessedDataCount) {
        HashMap<String, PortData> unprocessedDataMap = extractUnprocessedDataMap(portUuidToPortDataMapper);
        HashMap<String, PortData> processedDataMap = extractProcessedDataMap(portUuidToPortDataMapper);

        Assert.assertEquals(unprocessedDataCount, unprocessedDataMap.size());
        Assert.assertEquals(processedDataCount, processedDataMap.size());
    }

    private HashMap<String, PortData> extractProcessedDataMap(PortUuidToPortDataMapper portUuidToPortDataMapper) {
        HashMap<String, PortData> processedDataMap;
        try {
            processedDataMap = (HashMap<String, PortData>) Whitebox
                    .getInternalState(portUuidToPortDataMapper, "processedPortUuidToPortDataMapper");
        } catch (ClassCastException e) {
            throw e;
        }
        return processedDataMap;
    }

    private HashMap<String, PortData> extractUnprocessedDataMap(PortUuidToPortDataMapper portUuidToPortDataMapper) {
        HashMap<String, PortData> unprocessedDataMap;
        try {
            unprocessedDataMap = (HashMap<String, PortData>) Whitebox
                    .getInternalState(portUuidToPortDataMapper, "unprocessedPortUuidToPortDataMapper");
        } catch (ClassCastException e) {
            throw e;
        }
        return unprocessedDataMap;
    }
}
