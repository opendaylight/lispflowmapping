/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import com.google.common.collect.SetMultimap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.HostIdToEidMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

/**
 * Created by Shakib Ahmed on 7/12/17.
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(HostIdToEidMapper.class)
public class HostIdToEidMapperTest {

    private static final String HOST1 = "host1";
    private static final String HOST2 = "host2";

    private static final Eid SAMPLE_EID1 = Mockito.mock(Eid.class);
    private static final Eid SAMPLE_EID2 = Mockito.mock(Eid.class);
    private static final Eid SAMPLE_EID3 = Mockito.mock(Eid.class);
    private static final Eid SAMPLE_EID4 = Mockito.mock(Eid.class);

    @Test
    public void unprocessedPortDataAddTest() {
        HostIdToEidMapper hostIdToEidMapper = new HostIdToEidMapper();

        hostIdToEidMapper.addUnprocessedEidInHost(HOST1, SAMPLE_EID1);
        hostIdToEidMapper.addUnprocessedEidInHost(HOST1, SAMPLE_EID2);
        hostIdToEidMapper.addUnprocessedEidInHost(HOST2, SAMPLE_EID3);
        hostIdToEidMapper.addUnprocessedEidInHost(HOST2, SAMPLE_EID4);

        processedAndUnprocessedEidAmountVerify(hostIdToEidMapper, 0, 4);
    }

    private void processedAndUnprocessedEidAmountVerify(HostIdToEidMapper hostIdToEidMapper,
                                                         int processedDataCount,
                                                         int unprocessedDataCount) {
        SetMultimap<String, Eid> unprocessedDataMap = extractUnprocessedEidMap(hostIdToEidMapper);
        SetMultimap<String, Eid> processedDataMap = extractProcessedEidMap(hostIdToEidMapper);

        Assert.assertEquals(unprocessedDataCount, unprocessedDataMap.size());
        Assert.assertEquals(processedDataCount, processedDataMap.size());
    }

    private SetMultimap<String, Eid> extractProcessedEidMap(HostIdToEidMapper hostIdToEidMapper) {
        SetMultimap<String, Eid> processedHostIdToEidMapper;
        try {
            processedHostIdToEidMapper = (SetMultimap<String, Eid>) Whitebox
                    .getInternalState(hostIdToEidMapper, "processedHostIdToEidMapper");
        } catch (ClassCastException e) {
            throw e;
        }
        return processedHostIdToEidMapper;
    }

    private SetMultimap<String, Eid> extractUnprocessedEidMap(HostIdToEidMapper hostIdToEidMapper) {
        SetMultimap<String, Eid> unprocessedHostIdToEidMapper;
        try {
            unprocessedHostIdToEidMapper = (SetMultimap<String, Eid>) Whitebox
                    .getInternalState(hostIdToEidMapper, "unprocessedHostIdToEidMapper");
        } catch (ClassCastException e) {
            throw e;
        }
        return unprocessedHostIdToEidMapper;
    }
}
