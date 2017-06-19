/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.util.LispUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

/**
 * Created by Shakib Ahmed on 2/7/17.
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(OpenstackNodeInformationManager.class)
public class OpenstackNodeInformationManagerTest {

    private OdlMappingserviceService lfmDbService = Mockito.mock(OdlMappingserviceService.class);

    private static final String HOST_ID = "compute0";

    private static final Rloc SAMPLE_RLOC = Mockito.mock(Rloc.class);

    private static final Eid SAMPLE_EID_1 = Mockito.mock(Eid.class);
    private static final Eid SAMPLE_EID_2 = Mockito.mock(Eid.class);
    private static final Eid SAMPLE_EID_1_1 = Mockito.mock(Eid.class);

    @Test
    public void testScenarioRlocFirst() {
        OpenstackNodeInformationManager hostInformationManager = getDefaultHostInformationManager();

        addRlocData(hostInformationManager);

        Mockito.verify(lfmDbService, Mockito.times(0))
                .addMapping(Mockito.any(AddMappingInput.class));

        addEid1(hostInformationManager);

        AddMappingInput desiredMappingRecord = createAddMappingInput(SAMPLE_RLOC, SAMPLE_EID_1);
        Mockito.verify(lfmDbService).addMapping(desiredMappingRecord);

        destroySingleton(hostInformationManager);
    }

    @Test
    public void testScenarioPortDataFirst() {
        OpenstackNodeInformationManager hostInformationManager = getDefaultHostInformationManager();

        addEid1(hostInformationManager);

        Mockito.verify(lfmDbService, Mockito.times(0))
                .addMapping(Mockito.any(AddMappingInput.class));

        addRlocData(hostInformationManager);

        AddMappingInput desiredMappingRecord = createAddMappingInput(SAMPLE_RLOC, SAMPLE_EID_1);
        Mockito.verify(lfmDbService).addMapping(desiredMappingRecord);

        destroySingleton(hostInformationManager);
    }

    @Test
    public void testScenarioMultiplePortDataAndThenRloc() {
        OpenstackNodeInformationManager hostInformationManager = getDefaultHostInformationManager();

        addEid1(hostInformationManager);
        addEid2(hostInformationManager);

        Mockito.verify(lfmDbService, Mockito.times(0))
                .addMapping(Mockito.any(AddMappingInput.class));

        addRlocData(hostInformationManager);

        AddMappingInput desiredMappingRecord1 = createAddMappingInput(SAMPLE_RLOC, SAMPLE_EID_1);
        Mockito.verify(lfmDbService).addMapping(desiredMappingRecord1);

        AddMappingInput desiredMappingRecord2 = createAddMappingInput(SAMPLE_RLOC, SAMPLE_EID_2);
        Mockito.verify(lfmDbService).addMapping(desiredMappingRecord2);

        destroySingleton(hostInformationManager);
    }

    @Test
    public void testOnlyUnprocessedPortDataIsBeingProcessed() {
        OpenstackNodeInformationManager hostInformationManager = getDefaultHostInformationManager();

        addEid1(hostInformationManager);

        Mockito.verify(lfmDbService, Mockito.times(0))
                .addMapping(Mockito.any(AddMappingInput.class));

        addRlocData(hostInformationManager);

        addEid2(hostInformationManager);

        Mockito.verify(lfmDbService, Mockito.times(2))
                .addMapping(Mockito.any(AddMappingInput.class));

        AddMappingInput desiredMappingRecord1 = createAddMappingInput(SAMPLE_RLOC, SAMPLE_EID_1);
        Mockito.verify(lfmDbService).addMapping(desiredMappingRecord1);

        AddMappingInput desiredMappingRecord2 = createAddMappingInput(SAMPLE_RLOC, SAMPLE_EID_2);
        Mockito.verify(lfmDbService).addMapping(desiredMappingRecord2);

        destroySingleton(hostInformationManager);
    }

    @Test
    public void testProperMappingRecordRemoval() {
        OpenstackNodeInformationManager hostInformationManager = getDefaultHostInformationManager();

        addEid1(hostInformationManager);

        addRlocData(hostInformationManager);

        addUpdatedEid1(hostInformationManager);

        RemoveMappingInput removeMappingInput = LispUtil.buildRemoveMappingInput(SAMPLE_EID_1);
        Mockito.verify(lfmDbService).removeMapping(removeMappingInput);

        AddMappingInput addMappingInput = createAddMappingInput(SAMPLE_RLOC, SAMPLE_EID_1_1);
        Mockito.verify(lfmDbService).addMapping(addMappingInput);

        destroySingleton(hostInformationManager);
    }

    private void addRlocData(OpenstackNodeInformationManager hostInformationManager) {
        hostInformationManager.addRlocOfHost(HOST_ID, SAMPLE_RLOC);
    }

    private void addEid1(OpenstackNodeInformationManager hostInformationManager) {
        hostInformationManager.addEidInHost(HOST_ID, SAMPLE_EID_1);
    }

    private void addEid2(OpenstackNodeInformationManager hostInformationManager) {
        hostInformationManager.addEidInHost(HOST_ID, SAMPLE_EID_2);
    }

    private void addUpdatedEid1(OpenstackNodeInformationManager hostInformationManager) {
        hostInformationManager.attemptToDeleteExistingMappingRecord(HOST_ID, SAMPLE_EID_1);
        hostInformationManager.addEidInHost(HOST_ID, SAMPLE_EID_1_1);
    }

    private AddMappingInput createAddMappingInput(Rloc rloc, Eid eid) {
        List<Rloc> rlocList = new ArrayList<>();
        rlocList.add(rloc);
        List<LocatorRecord> locatorRecordList = LispAddressUtil.asLocatorRecords(rlocList);

        return LispUtil.buildAddMappingInput(eid, locatorRecordList);
    }

    private OpenstackNodeInformationManager getDefaultHostInformationManager() {
        OpenstackNodeInformationManager hostInformationManager = OpenstackNodeInformationManager.getInstance();
        hostInformationManager.setOdlMappingserviceService(lfmDbService);
        return hostInformationManager;
    }

    private void destroySingleton(OpenstackNodeInformationManager hostInformationManager) {
        try {
            Field field = hostInformationManager.getClass().getDeclaredField("instance");
            field.setAccessible(true);
            field.set(hostInformationManager, null);
        } catch (NoSuchFieldException e) {
            assert false;
        } catch (IllegalAccessException e) {
            assert false;
        }
    }
}
